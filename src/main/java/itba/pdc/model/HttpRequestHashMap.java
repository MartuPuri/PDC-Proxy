package itba.pdc.model;

import java.util.HashMap;
import java.util.Map;


public class HttpRequestHashMap implements HttpRequest{
	
	private String method;
	private int[] version;
	private Map<String, String> headers;
	private String body;
	
	public HttpRequestHashMap(){
		headers = new HashMap<String,String>();
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public int[] getVersion() {
		return version;
	}

	public void setVersion(int[] version) {
		this.version = version;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
	
	
	
}
