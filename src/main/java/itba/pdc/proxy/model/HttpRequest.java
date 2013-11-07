package itba.pdc.proxy.model;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class HttpRequest extends HttpRequestAbstract implements HttpMessage {

	// private String method;
	// private String body;
	// private String uri;
	// private int[] version;
	// private StatusRequest status = StatusRequest.OK;
	// private Map<String, String> params;
	// private Map<String, String> headers;
	private static final Set<String> supportedMethods = createMethods();
	private static final Set<String> supportedHeaders = createHeaders();
	private Logger debugLogger = (Logger) LoggerFactory.getLogger("debug.log");
	private int port = 80;

	protected static Set<String> createHeaders() {
		Set<String> headers = new HashSet<String>();
		headers.add("accept");
		// headers.add("Accept-Charset");
		// headers.add("Accept-Encoding");
		// headers.add("Accept-Language");
		// headers.add("Accept-Datetime");
		// headers.add("Authorization");
		// headers.add("Cache-Control");
		headers.add("connection");
		// headers.add("Cookie");
		headers.add("content-length");
		// headers.add("Content-MD5");
		// headers.add("Content-Type");
		headers.add("date");
		headers.add("expect");
		headers.add("from");
		headers.add("host");
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

	protected static Set<String> createMethods() {
		Set<String> headers = new HashSet<String>();
		headers.add("GET");
		headers.add("POST");
		headers.add("HEAD");
		return headers;
	}

	public HttpRequest() {
		// this.params = new HashMap<String, String>();
		// this.headers = new HashMap<String, String>();
		// this.version = new int[2];
	}

	@Override
	public void addHeader(String header, String value) {
		if (!supportedHeaders.contains(header)) {
			System.out.println("Invalid header");
			// TODO: VER QUE HACEMOS
		}
		if (header.equals("host")) {
			int idx = value.indexOf(":");
			int length = value.length();
			if (idx > 0) {
				port = Integer.parseInt(value.substring(idx + 1, length));
				super.addHeader(header, value.substring(0, idx));
			} else {
				super.addHeader(header, value);
			}
		}
	}

	public ByteBuffer getStream() {
		String line = "";

		// TODO: Fix query
		String firstLine = super.getMethod() + " " + super.getUri() + " HTTP/"
				+ super.getVersion()[0] + "." + super.getVersion()[1] + "\n";
		String headersLine = "";
		for (Entry<String, String> entry : super.getHeaders().entrySet()) {
			if (!entry.getKey().contains("encoding")) {
				if (entry.getKey().equals("host")) {
					headersLine += entry.getKey() + ": " + entry.getValue()
							+ ":" + port + "\n";
				} else {
					headersLine += entry.getKey() + ": " + entry.getValue()
							+ "\n";
				}
			}
		}
		line += firstLine + headersLine;
		if (bodyEnable()) {
			line += super.getBody();
		}
		line += "\n";
		debugLogger.debug("Request: \n" + line);
		ByteBuffer buff = ByteBuffer.allocate(line.getBytes().length);
		buff.put(line.getBytes());
		return buff;
	}

	public boolean validMethod(String method) {
		if (supportedMethods.contains(method)) {
			return true;
		}
		super.setStatus(StatusRequest.METHOD_NOT_ALLOWED);
		// status = StatusRequest.METHOD_NOT_ALLOWED;
		return false;
	}

	public void setStatusRequest(StatusRequest status) {
		super.setStatus(status);
	}

	public Integer getPort() {
		return port;
	}
}
