import java.util.HashMap;

/*
 * RetrievedData, output data from querying the broken server
 * Note: not much guidance for format in challenge problem, other than
 * noting whether or not the query was successful.
 * 
 * Thus, I decided to put data in hashmaps, since I'm assuming that you will
 * want to access the queried return data, at some point which will be easier to
 * work with using ids mapped to the JSONString.
 */
public class RetrievedData{
	//Private information storage
	private HashMap<Long, Boolean> succesful = new HashMap<Long, Boolean>();
	private HashMap<Long, String> data = new HashMap<Long, String>();
	
	//Constructor
	public RetrievedData() {}

	//long ID for the customer, boolean for query success, JSONString from the query
	public void addRequest(long id, boolean completed, String jsonString)
	{
		succesful.put(id, completed);
		data.put(id, jsonString);
	}

	//get successful queries within the 200 MS
	public HashMap<Long, Boolean> getSuccess() {
		return succesful;
	}

	//get data from the queries
	public HashMap<Long, String> getData() {
		return data;
	}
}
