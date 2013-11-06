package itba.pdc.proxy.model;

import java.awt.HeadlessException;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class EHttpRequest extends HttpRequestAbstract {
	
	private static final Set<String> supportedMethods = createMethods();
	private static final Set<String> supportedHeaders = createHeaders();
	private StatusRequest status = StatusRequest.OK;
	
	private static Set<String> createMethods() {
		Set<String> headers = new HashSet<String>();
		headers.add("GET");
		headers.add("POST");
		return headers;
	}
	
	private static Set<String> createHeaders() {
		Set<String> headers = new HashSet<String>();
			headers.add("accept");
			headers.add("authorization");
			headers.add("date");
			headers.add("host");
		return headers;
	}
	
	@Override
	public void setUri(String uri) {
		super.setUri(uri);
		if (uri.equals("bytes")) {
			status = StatusRequest.BYTES;
		} else if (uri.equals("histogram")) {
			status = StatusRequest.HISTOGRAM;
		} else if (uri.equals("status")) {
			status = StatusRequest.STATUS;
		} else if (uri.equals("accesses")) {
			status = StatusRequest.ACCESSES;
		} else if (uri.equals("filter")) {
			status = StatusRequest.FILTER;
		}
	}
	
	@Override
	public void setParams(Map<String, String> params) {
		super.setParams(params);
		for (Entry<String, String> p : params.entrySet()) {
			if (status.equals(StatusRequest.HISTOGRAM)) {
				if (p.getKey().equals("code")) {
//					super.addHeader(p.ge, value);
				}
			}
		}
	}
	
	public void add(String a , String p) {
		super.addHeader(a, p);
	}
}
