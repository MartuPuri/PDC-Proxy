package itba.pdc.model;

import itba.pdc.proxy.lib.ManageByteBuffer;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class HttpRequest {

	private String method;
	private String body;
	private String uri;
	private int[] version;
	private StatusRequest status = StatusRequest.OK;
	private Map<String, String> params;
	private Map<String, String> headers;
	private static final Set<String> supportedMethods = createMethods();
	private static final Set<String> supportedHeaders = createHeaders();

	private static Set<String> createHeaders() {
		Set<String> headers = new HashSet<String>();
		headers.add("Accept");
		// headers.add("Accept-Charset");
		// headers.add("Accept-Encoding");
		// headers.add("Accept-Language");
		// headers.add("Accept-Datetime");
		// headers.add("Authorization");
		// headers.add("Cache-Control");
		headers.add("Connection");
		// headers.add("Cookie");
		headers.add("Content-Length");
		// headers.add("Content-MD5");
		// headers.add("Content-Type");
		headers.add("Date");
		headers.add("Expect");
		headers.add("From");
		headers.add("Host");
		// headers.add("If-Match");
		// headers.add("If-None-Match");
		// headers.add("If-Modified-Since");
		// headers.add("If-Range");
		// headers.add("Max-Forwards");
		// headers.add("If-Unmodified-Since");
		// headers.add("Origin");
		// headers.add("Pragma");
		// headers.add("Proxy-Authorization");
		// headers.add("Range");
		// headers.add("Referer");
		// headers.add("TE");
		// headers.add("Upgrade");
		// headers.add("User-Agent");
		// headers.add("Via");
		// headers.add("Warning");

		return headers;
	}

	private static Set<String> createMethods() {
		Set<String> headers = new HashSet<String>();
		headers.add("GET");
		headers.add("POST");
		headers.add("HEAD");
		return headers;
	}

	public HttpRequest() {
		this.params = new HashMap<String, String>();
		this.headers = new HashMap<String, String>();
		this.version = new int[2];
	}

	public void addHeader(String header, String value) {
		if (!supportedHeaders.contains(header)) {
			// System.out.println("Invalid header");
			// TODO: VER QUE HACEMOS
		}
		headers.put(header, value);
	}

	public void setVersion(int[] version) {
		this.version[0] = version[0];
		this.version[1] = version[1];
	}

	public void setMethod(String method) {
		if (!supportedMethods.contains(method)) {
			// System.out.println("Invalid method");
			// TODO: VER QUE HACEMOS
		}
		this.method = method;
	}

	public void setParams(Map<String, String> params) {
		this.params.putAll(params);
	}

	public void setBody(String body) {
		if (!headers.containsKey("Content-Length")) {
			System.out.println("Missing content-length");
			// TODO: VER QUE HACEMOS
		}
		this.body = body;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public boolean bodyEnable() {
		if (headers.containsKey("Content-Length")) {
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

	public boolean validMethod(String method) {
		if (supportedMethods.contains(method)) {
			return true;
		}
		status = StatusRequest.METHOD_NOT_ALLOWED;
		return false;
	}

	public String getHost() {
		return headers.get("host");
	}

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

	public ByteBuffer getStream() {
		String line = "";
		String firstLine = method + " " + uri + " HTTP/" + version[0] + "." + version[1] + "\n";
		String headersLine = "";
		for (Entry<String, String> entry : headers.entrySet()) {
			headersLine += entry.getKey() + ": " + entry.getValue() + "\n";
		}
		line += firstLine + headersLine;
		if (bodyEnable()) {
			line += body;
		}
		line += "\n";
		ByteBuffer buff = ByteBuffer.allocate(line.getBytes().length);
		buff.put(line.getBytes());
		return buff;
	}

}
