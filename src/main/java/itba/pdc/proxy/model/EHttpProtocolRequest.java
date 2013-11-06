package itba.pdc.proxy.model;

import java.util.HashSet;
import java.util.Set;

public class EHttpProtocolRequest extends HttpRequest {
	
	private static final Set<String> supportedMethods = createMethods();
	private static final Set<String> supportedHeaders = createHeaders();
	
	protected static Set<String> createMethods() {
		Set<String> headers = new HashSet<String>();
		headers.add("GET");
		headers.add("POST");
		return headers;
	}
	
	protected static Set<String> createHeaders(String method) {
		Set<String> headers = new HashSet<String>();
			headers.add("accept");
			headers.add("authorization");
			headers.add("date");
			headers.add("host");
		return headers;
	}
}
