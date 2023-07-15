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

public class CountryURLRequestCount {

    public static final Class<Text> OUTPUT_KEY_CLASS = Text.class;
    public static final Class<IntWritable> OUTPUT_VALUE_CLASS = IntWritable.class;

    public static class MapperImpl extends Mapper<LongWritable, Text, Text, IntWritable> {
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
            context.write(new Text(country + "\t" + parts[6]), one);
        }
    }

    public static class ReducerImpl extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable totalCount = new IntWritable();

        @Override
        protected void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum++;
            }
            totalCount.set(sum);
            context.write(key, totalCount);
        }
    }

    public static class SortMapper extends Mapper<LongWritable, Text, Text, Text> {

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            String[] parts = line.split("\t");
            String country = parts[0];
            String url = parts[1];
            String count = parts[2];

            context.write(new Text(country), new Text(url + "\t" + count));
        }
    }

    public static class SortReducer extends Reducer<Text, Text, Text, IntWritable> {
        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            TreeMap<Integer, List<String>> countToClientMap = new TreeMap<>(Collections.reverseOrder());
            for (Text value : values) {
                String[] parts = value.toString().split("\t");
                String url = parts[0];
                int count = Integer.parseInt(parts[1]);

                List<String> countryUrls = countToClientMap.getOrDefault(count, new ArrayList<>());
                countryUrls.add(key.toString() + "\t" + url);
                countToClientMap.put(count, countryUrls);
            }

            for (Map.Entry<Integer, List<String>> entry : countToClientMap.entrySet()) {
                int count = entry.getKey();
                List<String> clients = entry.getValue();
                for (String client : clients) {
                    context.write(new Text(client), new IntWritable(count));
                }
            }
        }
    }
}
