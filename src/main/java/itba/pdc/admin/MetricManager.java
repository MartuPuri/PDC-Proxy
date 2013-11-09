/*
 * MetricManager
 */

package itba.pdc.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class MetricManager {
	private static final MetricManager instance = new MetricManager();

	private int accesses;
	private long bytesRead = 0;
	private long bytesWrite = 0;
	private ConcurrentMap<Integer, List<Date>> to_histogram;

	private MetricManager() {
		if (instance != null)
			throw new IllegalStateException();
		to_histogram = new ConcurrentHashMap<Integer, List<Date>>();
	}

	public static MetricManager getInstance() {
		return instance;
	}

	public void addAccess() {
		this.accesses++;
	}

	public void addBytesRead(long qty) {
		bytesRead += qty;
	}
	
	@Deprecated
	public void addBytesWrite(long qty) {
		bytesWrite += qty;
	}

	public void addStatusCode(Integer code) {
		List<Date> events = this.to_histogram.get(code);
		if (events == null)
			events = new ArrayList<Date>();
		events.add(new Date());
	}

	public String generateHistogram(Integer code, HistogramFormatter format,
			GroupMetrics groupby) {
		List<Date> list = this.to_histogram.get(code);
		if (list == null || list.isEmpty()) {
			return "There is no histogram for the code: " + code;
		}
		Map<String, Integer> to_format = groupby.group(this.to_histogram
				.get(code));
		return format.format(to_format);
	}
	
	public String getBytes() {
		String bytes = "";
		bytes += "Bytes received: " + bytesRead + "\nBytes sended: " + bytesWrite + "\n";
		return bytes;
	}
}
