package itba.pdc.proxy.model;

import java.util.Map;
import java.util.Set;

public abstract class HttpRequestAbstract {
	private String method;
	private String body;
	private String uri;
	private int[] version;
	private StatusRequest status = StatusRequest.OK;
	private Map<String, String> params;
	private Map<String, String> headers;

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
		status = StatusRequest.LENGTH_REQUIRED;
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

	public StatusRequest getStatusRequest() {
		return status;
	}

	public void setStatus(StatusRequest status) {
		this.status = status;
	}

	public String getHost() {
		return headers.get("host");
	}

	@Deprecated
	public Integer getPort() {
		String port = headers.get("port");
		if (port == null) {
			return 80;
		} else {
			try {
				return Integer.parseInt(port);
			} catch (NumberFormatException e) {
				return 80;
			}
		}
	}

	public String getMethod() {
		return this.method;
	}
	
	protected String getBody() {
		
	}
}
