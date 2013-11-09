package itba.pdc.proxy.model;

import itba.pdc.admin.MetricManager;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class HttpResponse {

	private Integer code;
	private String messageCode;
	private ByteBuffer body;
	private int[] version;
	private Map<String, String> headers;
	private static Map<Integer, String> httpReplies = createHttpReplies();
	private Logger debugLogger = (Logger) LoggerFactory.getLogger("debug.log");

	private static Map<Integer, String> createHttpReplies() {
		Map<Integer, String> result = new HashMap<Integer, String>();
		result.put(100, "Continue");
		result.put(200, "OK");
		result.put(201, "Created");
		result.put(202, "Accepted");
		result.put(203, "Non-Authoritative Information");
		result.put(204, "No Content");
		result.put(205, "Reset Content");
		result.put(206, "Partial Content");
		result.put(300, "Multiple Choices");
		result.put(301, "Moved Permanently");
		result.put(302, "Found");
		result.put(303, "See Other");
		result.put(304, "Not Modified");
		result.put(305, "Not Modified");
		result.put(306, "(Unused)");
		result.put(307, "Temporary Redirect");
		result.put(400, "Bad Request");
		result.put(401, "Unauthorized");
		result.put(402, "Payment Required");
		result.put(403, "Forbidden");
		result.put(404, "Not Found");
		result.put(405, "Method Not Allowed");
		result.put(406, "Not Acceptable");
		result.put(407, "Proxy Authentication Required");
		result.put(408, "Request Timeout");
		result.put(409, "Conflict");
		result.put(410, "Gone");
		result.put(411, "Length Required");
		result.put(412, "Precondition Failed");
		result.put(413, "Request Entity Too Large");
		result.put(414, "Request-URI Too Long");
		result.put(415, "Request-URI Too Long");
		result.put(416, "Requested Range Not Satisfiable");
		result.put(417, "Expectation Failed");
		result.put(500, "Internal Server Error");
		result.put(501, "Not Implemented");
		result.put(502, "Bad Gateway");
		result.put(503, "Service Unavailable");
		result.put(504, "Gateway Timeout");
		result.put(505, "Http Version Not Supported");

		return Collections.unmodifiableMap(result);
	}

	public HttpResponse() {
		this.headers = new HashMap<String, String>();
		this.version = new int[2];
	}

	public void addHeader(String header, String value) {
		headers.put(header, value);
	}

	public void setVersion(int[] version) {
		this.version[0] = version[0];
		this.version[1] = version[1];
	}

	public void setCode(Integer code) {
		MetricManager.getInstance().addStatusCode(code);
		this.code = code;
		this.messageCode = httpReplies.get(code);
	}

	public Integer getStatusCode() {
		return this.code;
	}

	public void setBody(ByteBuffer buffer) {
		if (!headers.containsKey("content-length")) {
			System.out.println("Missing content-length");
			// TODO: VER QUE HACEMOS
		}
		this.body = ByteBuffer.allocate(buffer.limit());
		buffer.flip();
		this.body.put(buffer);
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
			return false;
		}
		return true;
	}

	public String getHeader(String key) {
		return headers.get(key);
	}

	public ByteBuffer getStream() {
		final StringBuilder builder = new StringBuilder();
		builder.append("HTTP/").append(version[0]).append(".")
				.append(version[1]).append(code).append(" ")
				.append(messageCode).append("\r\n");
		for (Entry<String, String> entry : headers.entrySet()) {
			builder.append(entry.getKey()).append(": ")
					.append(entry.getValue()).append("\r\n");
		}
		builder.append("\n");
		final String head = builder.toString();
		ByteBuffer buff = ByteBuffer.allocate(head.getBytes().length
				+ body.position());
		buff.put(head.getBytes());
		body.flip();
		buff.put(body);
		return buff;
	}
}
