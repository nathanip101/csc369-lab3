package csc369;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.hadoop.io.Text;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class HadoopApp {

	public static void main(String[] args)
			throws IOException, InterruptedException, ClassNotFoundException, URISyntaxException {
		Configuration conf1 = new Configuration();
		conf1.set("mapreduce.input.keyvaluelinerecordreader.key.value.separator", ",");

		Job job1 = Job.getInstance(conf1);
		String[] otherArgs = new GenericOptionsParser(conf1, args).getRemainingArgs();

		Configuration conf2 = new Configuration();
		conf1.set("mapreduce.input.keyvaluelinerecordreader.key.value.separator", ",");

		Job job2 = Job.getInstance(conf2);
		

		if (otherArgs.length < 3) {
			System.out.println("Expected parameters: <job class> [<input dir>]+ <output dir>");
			System.exit(-1);
		} else if ("CountryRequestCount".equalsIgnoreCase(otherArgs[0])) {
			job1.setMapperClass(CountryRequestCount.MapperImpl.class);
			job1.setReducerClass(CountryRequestCount.ReducerImpl.class);
			job1.setOutputKeyClass(CountryRequestCount.OUTPUT_KEY_CLASS);
			job1.setOutputValueClass(CountryRequestCount.OUTPUT_VALUE_CLASS);
			FileInputFormat.setInputPaths(job1, new Path(otherArgs[1]));
			job1.addCacheFile(new URI(args[2]));
			FileOutputFormat.setOutputPath(job1, new Path(otherArgs[3]));
			System.exit(job1.waitForCompletion(true) ? 0 : 1);
		} else if ("CountryURLRequestCount".equalsIgnoreCase(otherArgs[0])) {
			job1.setMapperClass(CountryURLRequestCount.MapperImpl.class);
			job1.setReducerClass(CountryURLRequestCount.ReducerImpl.class);
			job1.setOutputKeyClass(CountryURLRequestCount.OUTPUT_KEY_CLASS);
			job1.setOutputValueClass(CountryURLRequestCount.OUTPUT_VALUE_CLASS);
			FileInputFormat.setInputPaths(job1, new Path(otherArgs[1]));
			job1.addCacheFile(new URI(args[2]));
			FileOutputFormat.setOutputPath(job1, new Path("temp"));

			if(job1.waitForCompletion(true)) {
				job2.setMapperClass(CountryURLRequestCount.SortMapper.class);
				job2.setReducerClass(CountryURLRequestCount.SortReducer.class);
				job2.setOutputKeyClass(Text.class);
				job2.setOutputValueClass(Text.class);
				FileInputFormat.setInputPaths(job2, new Path("temp"));
				FileOutputFormat.setOutputPath(job2, new Path(otherArgs[3]));
			} else {
				System.exit(0);
			}
			System.exit(job2.waitForCompletion(true) ? 0 : 1);
		} else if ("CountryVisitorsCount".equalsIgnoreCase(otherArgs[0])) {
			job1.setMapperClass(CountryVisitorsCount.MapperImpl.class);
			job1.setReducerClass(CountryVisitorsCount.ReducerImpl.class);
			job1.setOutputKeyClass(CountryVisitorsCount.OUTPUT_KEY_CLASS);
			job1.setOutputValueClass(CountryVisitorsCount.OUTPUT_VALUE_CLASS);
			FileInputFormat.setInputPaths(job1, new Path(otherArgs[1]));
			job1.addCacheFile(new URI(args[2]));
			FileOutputFormat.setOutputPath(job1, new Path(otherArgs[3]));
			System.exit(job1.waitForCompletion(true) ? 0 : 1);
		} else {
			System.out.println("Unrecognized job: " + otherArgs[0]);
			System.exit(-1);
		}
		
	}
}
