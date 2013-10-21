package itba.pdc.model;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HttpResponse {
	
	private int status;
	private String body;
	private Map<String,String> headers;
	
	private static final Map<Integer, String> httpReplies = createHttpReplies();
	private static final Set<String> supportedHeaders = createHeaders();
    
	public HttpResponse(){
	}
	private static Set<String> createHeaders(){
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

	public String getStatusValue(){
		if(!httpReplies.containsKey(this.status)){
			//TODO: VER QUE HACEMOS
		}
		return httpReplies.get(this.status);
	}
	
	public void setStatus(int status){
		if(!httpReplies.containsKey(status)){
			System.out.println("Invalid Status");
			//TODO: VER QUE HACER
		}
		this.status = status;
	}

	public void addHeader(String header, String value) {
		if (!supportedHeaders.contains(header)) {
			System.out.println("Invalid header");
			// TODO: VER QUE HACEMOS
		}
		headers.put(header, value);
	}

	public void setBody(String body){
		this.body = body;
	}

	private void addDefaultHeaders(){
		this.headers.put("Date", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
		this.headers.put("Status", this.status + " " + this.getStatusValue());
	}
	
	private void unsopportedMethod(){
		this.status = 405;
		this.headers.put("Allow", "GET,POST,HEAD");
		this.addDefaultHeaders();
	}
	
	private void badRequest(){
		this.status = 400;
		this.addDefaultHeaders();
	}

	private void lengthRequired(){
		this.status = 411;
		this.addDefaultHeaders();
	}
	
	private void unsopportedMediaType(){
		this.status = 415;
		this.addDefaultHeaders();
	}
	
	private void httpVersionNotSupported(){
		this.status = 505;
		this.addDefaultHeaders();
	}

}
