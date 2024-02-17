package tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;

/**
 * @author Zygon
 */
public class TempStatistics{

	private static double average = 0.0;
	private static int count = 0;
	private static LinkedList<Double> results = new LinkedList<>();

	public synchronized static void addValue(long length){
		average = (length + (count * average)) / (count + 1);
		count++;
		if(count == 2000000000){
			results.add(average);
			count = 0;
			average = 0.0d;
		}
	}

	public static void dumpResults() throws IOException{
		File f = new File("results_" + System.currentTimeMillis() + ".brent");
		if(!f.exists()){
			f.createNewFile();
		}
		FileOutputStream fous = new FileOutputStream(f);
		PrintStream out = new PrintStream(fous);
		out.println("Current Average: " + average);
		for(Double result : results){
			out.println("Past Average: " + result);
		}
		out.flush();
		out.close();
		fous.close();
	}
}