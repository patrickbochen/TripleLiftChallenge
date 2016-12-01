
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class TripleLiftConnect {
	//URL not including specific query
	private static final String GET_URL_BASE = 
			"http://dan.triplelift.net/code_test.php?advertiser_id=";
	private static final int TOTAL_TIME = 200; //Total time alloted to all requests
	private static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS; //Time unit
	//Maximum possible threads alloted to ExecutorService
	private static final int MAX_THREADS = 50;
	
	public static void main (String[] args) {
		long[] input = new long[]{123, 456, 789};
		runAggregate(input);
	}
	
	//Run method
	public static RetrievedData runAggregate(long[] input) {
		ArrayList<String> jsonAllData = null;
		ArrayList<Callable<String>> callables = createCallables(input); //First Create callables
		try {
			//Perform requests
			 jsonAllData = execute(callables);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Save retrieved data into desired format
		int counter = 0; //Cycles through input
		RetrievedData aggregateData = new RetrievedData();
		
		for(String s: jsonAllData)
		{
			aggregateData.addRequest(input[counter], s.equals(""), s);
			//System.out.println(input[counter] + ": " + s); Uncomment to print retrieved data
			counter++;
		}
		System.out.println("execute done");
		return aggregateData;
	}
	
	//Private method that creates callable objects (GetUrl) from an array of ids
	private static ArrayList<Callable<String>> createCallables(long[] adIds) {
		
		ArrayList<Callable<String>> callables = new ArrayList<Callable<String>>();
		for (long id: adIds) {
			callables.add(new GetUrl(id) );
		}
		return callables;
	}
	
	//Uses the ExecutorService on a list of GetUrl objects
	//returns ArrayList of jsonString corresponding to the ids
	//Note: ExecutorService automates multithreads, and also has a time cap ability
	private static ArrayList<String> execute(ArrayList<Callable<String>> callables) throws InterruptedException {
		
		ArrayList<String> jsonStrings = new ArrayList<String>();
		
		//Initialize executor, create threads bounded by MAX_THREADS
		ExecutorService executor = Executors.newFixedThreadPool(Math.min(callables.size(), MAX_THREADS));
		//InvokeAll returns futures in corresponding sequence to the input list of callables
		ArrayList<Future<String>> futures = 
				(ArrayList<Future<String>>) executor.invokeAll(callables, TOTAL_TIME, TIME_UNIT);
		//Save the strings from Futures
		//has a blank string if the thread was cancelled or didn't finish in time
		for(Future<String> future : futures){
		    try {
		    	if (!future.isCancelled() && future.isDone())
		    		jsonStrings.add(future.get());
		    	else
		    		jsonStrings.add("");
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				jsonStrings.add("");
			} catch (CancellationException e) {
				//e.printStackTrace();
				jsonStrings.add("");
			}
		}
		executor.shutdown();
		return jsonStrings;
	}
	
	//Http get request of a given string
	//Takes in a string for the url, returns a json string of the corresponding data
	private static String sendGet(String getUrl) throws IOException {
		URL obj = new URL(getUrl);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		int responseCode = con.getResponseCode();
		
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) {
	            sb.append(line + '\n');
	        }
			in.close();
			return sb.toString();
			
		}
		else
			return null;
	}
	
	//Private callable object class
	//used to save id for callable object and change into url
	private static class GetUrl implements Callable<String>{
		private final String getUrl;
		
		public GetUrl(long id) {
			this.getUrl = GET_URL_BASE + Long.toString(id);
		}
		//Call method performs http get request for the given url
		public String call() throws Exception {
			return sendGet(getUrl);
		}
	}
}
