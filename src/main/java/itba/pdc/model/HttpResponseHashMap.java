package itba.pdc.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpResponseHashMap {
	private static final Map<Integer, String> httpReplies = createMap();

    private static Map<Integer, String> createMap() {
        Map<Integer, String> result = new HashMap<Integer, String>();
        result.put(100, "Continue");
        result.put(101, "Switching Protocols");
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
        result.put(209, "Continue");
        return Collections.unmodifiableMap(result);
    }
    	{ "409", "Conflict" },
		{ "410", "Gone" }, { "411", "Length Required" },
		{ "412", "Precondition Failed" },
		{ "413", "Request Entity Too Large" },
		{ "414", "Request-URI Too Long" },
		{ "415", "Unsupported Media Type" },
		{ "416", "Requested Range Not Satisfiable" },
		{ "417", "Expectation Failed" },
		{ "500", "Internal Server Error" }, { "501", "Not Implemented" },
		{ "502", "Bad Gateway" }, { "503", "Service Unavailable" },
		{ "504", "Gateway Timeout" },
		{ "505", "HTTP Version Not Supported" } };
	private int status_code;
	private String body;
	private Map<String,String> status;
	
	public HttpResponseHashMap(){
		status = new HashMap<String,String>();
	}
	
}
