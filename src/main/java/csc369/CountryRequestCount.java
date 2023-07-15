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

public class CountryRequestCount {

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
            context.write(new Text(country), one);
        }
    }

    public static class ReducerImpl extends Reducer<Text, IntWritable, Text, IntWritable> {
        private TreeMap<Integer, List<String>> countToClientMap = new TreeMap<>(Collections.reverseOrder());

        @Override
        protected void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum++;
            }
            List<String> clients = countToClientMap.getOrDefault(sum, new ArrayList<>());
            clients.add(key.toString());
            countToClientMap.put(sum, clients);
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
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
