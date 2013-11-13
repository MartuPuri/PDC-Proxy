package itba.pdc.admin.filter;

import itba.pdc.proxy.model.StatusRequest;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;


public class ManageFilter {
	private static ManageFilter instance = null;
	private ConcurrentLinkedQueue<Filter> filters = null;

	private ManageFilter() {
		if (instance != null) {
			throw new IllegalAccessError("This class is already isntantiated");
		}
		filters = new ConcurrentLinkedQueue<Filter>();
	}

	public static ManageFilter getInstace() {
		if (instance == null) {
			instance = new ManageFilter();
		}
		return instance;
	}

	public void addOrRemoveFilter(StatusRequest status) {
		switch (status) {
		case TRANSFORMER:
			TransformationFilter f = TransformationFilter.getInstace();
			if (filters.contains(f)) {
				filters.remove(f);
			} else {
				filters.add(f);
			}
			break;
		default:
			break;
		}
	}
	
	public void doFilters(ByteBuffer buffer) {
		for (Filter f : filters) {
			f.doFilter(buffer);
		}
	}
}
