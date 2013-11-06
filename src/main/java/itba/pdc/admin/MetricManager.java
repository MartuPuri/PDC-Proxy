/*
 * MetricManager
 */

package itba.pdc.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MetricManager {
	private static final MetricManager instance = new MetricManager();
	
	private int accesses;
	private int bytes;
	private Map<Integer,List<Date>> to_histogram;
	
	private MetricManager(){
		if(instance!=null)
			throw	new IllegalStateException();
		to_histogram = new HashMap<Integer,List<Date>>();
	}
	
	public static MetricManager getInstance(){
		return instance;
	}
	
	public void addAccess(){
		this.accesses++;
	}
	
	public void addBytes(int qty){
		 bytes = bytes + qty;
	}
	
	public void addStatusCode(Integer code){
		List<Date> events = this.to_histogram.get(code);
		if(events == null)
			events = new ArrayList<Date>();
		events.add(new Date());
	}
	
	public String generateHistogram(Integer code, HistogramFormatter format, GroupMetrics groupby){
		Map<String,Integer> to_format = groupby.group(this.to_histogram.get(code));
		return format.format(to_format);
	}
	
}
