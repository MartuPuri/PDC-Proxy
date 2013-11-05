package itba.pdc.proxy.lib;

import itba.pdc.proxy.model.StatusRequest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public final class GenerateHttpResponse {

	private static int status;
	private static String body;
	private static Map<Integer, String> httpReplies = createHttpReplies();
	private static Set<String> supportedHeaders = createHeaders();
	private static List<String> supportedVersions = createVersions();

	private GenerateHttpResponse() {
	}

	private static Set<String> createHeaders() {
		Set<String> headers = new HashSet<String>();
		headers.add("Access-Control-Allow-Origin");
		headers.add("Accept-Ranges");
		headers.add("Age");
		headers.add("Allow");
		headers.add("Cache-Control");
		headers.add("Connection");
		headers.add("Content-Encoding");
		headers.add("Content-Language");
		headers.add("Content-Length");
		headers.add("Content-Location");
		headers.add("Content-MD5");
		headers.add("Content-Type");
		headers.add("Content-Disposition");
		headers.add("Content-Range");
		headers.add("Content-Type");
		headers.add("Date");
		headers.add("ETag");
		headers.add("Expires");
		headers.add("Last-Modified");
		headers.add("Link");
		headers.add("Location");
		headers.add("P3P");
		headers.add("Pragma");
		headers.add("Proxy-Authenticate");
		headers.add("Refresh");
		headers.add("Retry-After");
		headers.add("Server");
		headers.add("Set-Cookie");
		headers.add("Status");
		headers.add("Strict-Transport-Security");
		headers.add("Trailer");
		headers.add("Transfer-Encoding");
		headers.add("Vary");
		headers.add("Via");
		headers.add("Warning");
		headers.add("WWW-Authenticate");

		return headers;
	}

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

	private static List<String> createVersions() {
		supportedVersions = new ArrayList<String>();
		supportedVersions.add("HTTP/1.1");
		Collections.sort(supportedVersions, new Comparator<String>() {

			public int compare(String o1, String o2) {
				return o2.compareTo(o1);
			}
		});
		return supportedVersions;
	}

	// public String getStatusValue(){
	// if(!httpReplies.containsKey(this.status)){
	// //TODO: VER QUE HACEMOS
	// }
	// return httpReplies.get(this.status);
	// }
	//
	// public void setStatus(int status){
	// if(!httpReplies.containsKey(status)){
	// System.out.println("Invalid Status");
	// //TODO: VER QUE HACER
	// }
	// this.status = status;
	// }
	//
	// public void addHeader(String header, String value) {
	// if (!supportedHeaders.contains(header)) {
	// System.out.println("Invalid header");
	// // TODO: VER QUE HACEMOS
	// }
	// headers.put(header, value);
	// }

	// public void setBody(String body){
	// this.body = body;
	// }
	//
	private static void generateDefaultHeaders(Map<String, String> headers) {
		headers = new HashMap<String, String>();
		headers.put("Date",
				new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
		headers.put("Connection", "close");
		headers.put("Cache-Control", "no-cache");
		// this.headers.put("Status", this.status + " " +
		// this.getStatusValue());
	}

	//
	// private void unsopportedMethod(){
	// this.status = 405;
	// this.headers.put("Allow", "GET,POST,HEAD");
	// this.addDefaultHeaders();
	// }
	//
	// private void badRequest(){
	// this.status = 400;
	// this.addDefaultHeaders();
	// }
	//
	// private void lengthRequired(){
	// this.status = 411;
	// this.addDefaultHeaders();
	// }
	//
	// private void unsopportedMediaType(){
	// this.status = 415;
	// this.addDefaultHeaders();
	// }
	//
	// private void httpVersionNotSupported(){
	// this.status = 505;
	// this.addDefaultHeaders();
	// }

	public static String generateResponseError(StatusRequest status) throws IOException {
		String firstLine = generateFirstLine(status);
		String dataLine = generateDataFromFile(status);
		String headersLine = generateHeadersLine(status, dataLine.getBytes().length);

		return firstLine + "\n" + headersLine + "\n" + dataLine;
	}

	private static String generateFirstLine(StatusRequest status) {
		String firstLine;
		if (supportedVersions.isEmpty()) {
			return null;
		}
		firstLine = supportedVersions.get(0);
		String statusCode = httpReplies.get(status.getId());
		if (statusCode == null) {
			return null;
		}
		return firstLine + " " + status.getId() + " " + statusCode;
	}

	private static String generateDataFromFile(StatusRequest status)
			throws IOException {
		switch (status) {
		// TODO: Add specific headers
		case BAD_REQUEST:
			return readFile("bad_request.html");
		case CONFLICT:
			return readFile("coflict.html");
		case LENGTH_REQUIRED:
			return readFile("lenght_required.html");
		case METHOD_NOT_ALLOWED:
			return readFile("method_not_allowed.html");
		case VERSION_NOT_SUPPORTED:
			return readFile("unsupported_version.html");
		}
		return "";
	}

	private static String generateHeadersLine(StatusRequest status,
			int contentLength) {
		Map<String, String> headers = new HashMap<String, String>();
		generateDefaultHeaders(headers);
		headers.put("Content-Length", String.valueOf(contentLength));
		switch (status) {
		// TODO: Add specific headers
		case BAD_REQUEST:
			break;
		case CONFLICT:
			break;
		case LENGTH_REQUIRED:
			break;
		case METHOD_NOT_ALLOWED:
			break;
		case VERSION_NOT_SUPPORTED:
			break;
		}
		String headersLine = "";
		for (Entry<String, String> mapElement : headers.entrySet()) {
			headersLine += mapElement.getKey() + ": " + mapElement.getValue()
					+ "\n";
		}
		return headersLine;
	}

	private static String readFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");

		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(ls);
		}

		return stringBuilder.toString();
	}
}