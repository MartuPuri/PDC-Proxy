package itba.pdc.admin;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface GroupMetrics {
	public Map<String, Integer> group(List<Date> data);
}
