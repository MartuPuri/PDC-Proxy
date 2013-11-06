package itba.pdc.admin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class JsonFormatter implements HistogramFormatter {

	public static void main(String args[]){
		Map<String,Integer> data = new HashMap<String, Integer>();
		data.put("0", 10);
		data.put("1",15);
		data.put("5", 40);
		data.put("3", 10);
		System.out.println(new JsonFormatter().format(data));
	}
	
	public String format(Map<String, Integer> data) {
		String to_send = "{ ";
		String end = " }";
		
		Set<String> keys = data.keySet();
		int i = 0;
		for (String k : keys) {
			to_send += "\"" + k + "\"" + " : " + "\"" + data.get(k) + "\""; 
			if(i++ != keys.size() -1)
				to_send += ", ";
		}
		to_send = to_send + end;
		return to_send;
	}
	
}
