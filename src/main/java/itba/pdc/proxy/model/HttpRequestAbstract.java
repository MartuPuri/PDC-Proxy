package itba.pdc.proxy.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class HttpRequestAbstract {
	private String method;
	private String body;
	private String uri;
	private int[] version = new int[2];
	private StatusRequest status = StatusRequest.OK;
	private Map<String, String> params = new HashMap<String, String>();
	private Map<String, String> headers = new HashMap<String, String>();

	// private static final Set<String> supportedMethods = createMethods();
	// private static final Set<String> supportedHeaders = createHeaders();

	public void addHeader(String header, String value) {
		headers.put(header, value);
	}

	public void setVersion(int[] version) {
		this.version[0] = version[0];
		this.version[1] = version[1];
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public void setParams(Map<String, String> params) {
		this.params.putAll(params);
	}

	public void setBody(String body) {
		if (!headers.containsKey("content-length")) {
			System.out.println("Missing content-length");
			// TODO: VER QUE HACEMOS
		}
		this.body = body;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public boolean bodyEnable() {
		if (headers.containsKey("content-length")) {
			return true;
		}
		return false;
	}

	public boolean validVersion(int[] version) {
		if (version[0] != 1 && !(version[1] == 1 || version[1] == 0)) {
			System.out.println("Invalid version");
			// TODO: VER QUE HACER
			status = StatusRequest.VERSION_NOT_SUPPORTED;
			return false;
		}
		return true;
	}

	public String getHeader(String key) {
		return headers.get(key);
	}

	public void setStatus(StatusRequest status) {
		this.status = status;
	}

	public String getHost() {
		return headers.get("host");
	}

	public String getMethod() {
		return this.method;
	}

	/*
	 * TODO: Change this for buffer
	 */
	@Deprecated
	public String getBody() {
		return body;
	}

	public String getUri() {
		return uri;
	}

	public int[] getVersion() {
		return version;
	}

	public StatusRequest getStatus() {
		return status;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}
}
