package csc369;

import java.io.IOException;
import java.io.File;
import java.net.URI;
import java.util.*;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

public class CountryVisitorsCount {

    public static final Class<Text> OUTPUT_KEY_CLASS = Text.class;
    public static final Class<Text> OUTPUT_VALUE_CLASS = Text.class;

    public static class MapperImpl extends Mapper<LongWritable, Text, Text, Text> {
        private final IntWritable one = new IntWritable(1);
        private Map<String, String> hostnameToCountryMap = new HashMap<>();

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            URI[] cache = context.getCacheFiles();
            if (cache != null && cache.length > 0) {
                try (Scanner scanner = new Scanner(new File(cache[0].toString()))) {
                    while (scanner.hasNextLine()) {
                        String[] parts = scanner.nextLine().split(",");
                        String hostname = parts[0];
                        String country = parts[1];
                        hostnameToCountryMap.put(hostname, country);
                    }
                }
            }
        }

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            String[] parts = line.split(" ");
            String hostname = parts[0];
            String country = hostnameToCountryMap.getOrDefault(hostname, "Unknown");
            context.write(new Text(parts[6]), new Text(country));
        }
    }

    public static class ReducerImpl extends Reducer<Text, Text, Text, Text> {
        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            String outstring = "";
            SortedSet<String> countrySet = new TreeSet<>();

            for (Text val : values) {
                countrySet.add(val.toString());
            }

            String[] countryArray = countrySet.toArray(new String[0]);
            outstring = String.join(", ", countryArray);
            context.write(key, new Text(outstring));
        }
    }
}
