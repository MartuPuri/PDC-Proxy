package itba.pdc.httpparser;

/**
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

@Deprecated
enum State {
	INITIAL, HEADERS, DATA, END, ERROR;
};

@Deprecated
public class HttpParserOld {
	private static final String[][] HttpReplies = { { "100", "Continue" },
			{ "101", "Switching Protocols" }, { "200", "OK" },
			{ "201", "Created" }, { "202", "Accepted" },
			{ "203", "Non-Authoritative Information" },
			{ "204", "No Content" }, { "205", "Reset Content" },
			{ "206", "Partial Content" }, { "300", "Multiple Choices" },
			{ "301", "Moved Permanently" }, { "302", "Found" },
			{ "303", "See Other" }, { "304", "Not Modified" },
			{ "305", "Use Proxy" }, { "306", "(Unused)" },
			{ "307", "Temporary Redirect" }, { "400", "Bad Request" },
			{ "401", "Unauthorized" }, { "402", "Payment Required" },
			{ "403", "Forbidden" }, { "404", "Not Found" },
			{ "405", "Method Not Allowed" }, { "406", "Not Acceptable" },
			{ "407", "Proxy Authentication Required" },
			{ "408", "Request Timeout" }, { "409", "Conflict" },
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

	@Deprecated
	private BufferedReader reader;
	private String method, url;
	private Map<String, String> headers, params;
	private int[] ver;
	private String data;
	private String tail = "";

	private ByteBuffer buff;
	private State state;
	@Deprecated
	private InputStream input;
	@Deprecated
	private BufferedReader buffReader;

	// public static void main(String args[]) {
	// File file = new File("get.txt");
	// FileInputStream fis = null;
	// try {
	// fis = new FileInputStream(file);
	//
	// System.out.println("Total file size to read (in bytes) : "
	// + fis.available());
	// HttpParser parser = new HttpParser(fis);
	// parser.parseRequest();
	//
	// } catch (IOException e) {
	// e.printStackTrace();
	// } finally {
	// try {
	// if (fis != null)
	// fis.close();
	// } catch (IOException ex) {
	// ex.printStackTrace();
	// }
	// }
	// }

	public HttpParserOld() {
		this.state = State.INITIAL;
		this.method = "";
		this.url = "";
		this.headers = new HashMap<String, String>();
		this.params = new HashMap<String, String>();
		this.ver = new int[2];
		this.buff = ByteBuffer.allocate(0);
	}

	@Deprecated
	public HttpParserOld(InputStream is) {
		this();
		this.reader = new BufferedReader(new InputStreamReader(is));
	}

	@Deprecated
	public HttpParserOld(ByteBuffer _buff) {
		this();
		this.buff = _buff;
	}

	/*
	 * funcion que pushea al inputstream, un is entrante
	 */
	@Deprecated
	public void pushInputStream(InputStream is) {
		this.input = new SequenceInputStream(this.input, is);
		this.buffReader = new BufferedReader(new InputStreamReader(this.input));
	}

	/*
	 * funcion que parsea en base a su estado actual, si cambia de estado
	 * intenta parsiar dnuevo.
	 */
	public void parse() throws IOException {
		switch (this.state) {
		case INITIAL:
			this.handleInitial();
			this.state = State.HEADERS;
			break;
		case HEADERS:
			parseHeaders();
//			this.state = State.DATA;
			break;
		case DATA:
			parseData();
//			this.state = State.END;
			break;
		case END:
			break;
		default:
			break;
		}
	}

	public void pushByteBuffer(ByteBuffer _buff) {
//		System.out.println("Buff: " + new String(this.buff.array()));
//		System.out.println("_Buff: " + new String(_buff.array()));
		int capacity = this.buff.capacity();
		int remaining = this.buff.remaining();
//		System.out.println("capacity: " + capacity + " remaining: " + remaining);
//		System.out.println("_buff.capacity(): " + _buff.capacity());
		ByteBuffer aux = ByteBuffer.allocate(remaining + _buff.capacity());
//		System.out.println("Aux capacity: " + aux.capacity());
//		System.out.println("position: " + this.buff.position());
		aux.put(this.buff);
		System.out.println("Aux text 1: " + new String(aux.array()));
		aux.put(_buff);
//		System.out.println("Aux: position: " + aux.position());
		System.out.println("Aux text 2: " + new String(aux.array()));
//		System.out.println("AUX IS: " + new String(aux.array()));
		this.buff = aux;
		this.buff.flip();
	}

	public void print() {
		System.out.println(new String(this.buff.array()));
	}

	private String readLine() {
		String s = new String(this.buff.array());
		int index = s.indexOf("\r\n");
		if(index < 0){
			index = s.indexOf("\n");
		}
		if (index < 0) {
//			System.out.println("REMAINING:" + this.buff.remaining());
			return null;
		}
		int length = s.length() - 1;
		String line = s.substring(0, index);
		if (index < length) {
			this.buff = ByteBuffer.allocate(length - index);
			this.tail = s.substring(index + 2, length + 1);
			System.out.println("tail: " + tail);
			this.buff.put(tail.getBytes());
//			this.buff.position(0);
		} else if (index == length) {
			this.buff = ByteBuffer.allocate(length - index);
//			this.buff.position(0);
		}
		this.buff.flip();
		return line;

	}

	private int handleInitial() throws IOException {
		String prms[], cmd[], temp[];
		int idx, i;

//		System.out.println("Byte buffer 1: " + new String(this.buff.array()));
		String initial = readLine();
		if (initial == null) {
			return -1;
		}
		// initial = reader.readLine();
//		System.out.println("Byte buffer 2: " + new String(this.buff.array()));
//		System.out.println("Initial = " + initial.contains("\n"));
		if (initial == null || initial.length() == 0)
			return 0;
		if (Character.isWhitespace(initial.charAt(0))) {
			// starting whitespace, return bad request
			System.out.println("BAD REQUEST");
			return 400;
		}

		cmd = initial.split("\\s");

		if (cmd.length != 3) {
			System.out.println("BAD REQUEST");
			return 400;
		}

		if (cmd[2].indexOf("HTTP/") == 0 && cmd[2].indexOf('.') > 5) {
			temp = cmd[2].substring(5).split("\\.");
			try {
				ver[0] = Integer.parseInt(temp[0]);
				ver[1] = Integer.parseInt(temp[1]);
				if (ver[0] != 1 || ver[1] != 1) {
					return 505;
				}
			} catch (NumberFormatException nfe) {
				return 400;
			}
		} else {
			return 400;
		}

		if (cmd[0].equals("GET") || cmd[0].equals("HEAD")
				|| cmd[0].equals("POST")) {
			method = cmd[0];
			System.out.println(cmd);
			idx = cmd[1].indexOf('?');
			if (idx < 0) {
				url = cmd[1];
			} else {
				url = URLDecoder.decode(cmd[1].substring(0, idx), "ISO-8859-1");
				prms = cmd[1].substring(idx + 1).split("&");
				params = new HashMap<String, String>();
				for (i = 0; i < prms.length; i++) {
					temp = prms[i].split("=");
					if (temp.length == 2) {
						// we use ISO-8859-1 as temporary charset and then
						// String.getBytes("ISO-8859-1") to get the data
						params.put(URLDecoder.decode(temp[0], "ISO-8859-1"),
								URLDecoder.decode(temp[1], "ISO-8859-1"));
					} else if (temp.length == 1
							&& prms[i].indexOf('=') == prms[i].length() - 1) {
						// handle empty string separatedly
						params.put(URLDecoder.decode(temp[0], "ISO-8859-1"), "");
					}
				}
			}
		}
		return 200;
	}

	public int parseRequest() throws IOException {
		String initial, prms[], cmd[], temp[];
		int ret, idx, i;

		ret = 200; // default is OK now
		initial = reader.readLine();
		if (initial == null || initial.length() == 0)
			return 0;
		if (Character.isWhitespace(initial.charAt(0))) {
			// starting whitespace, return bad request
			System.out.println("BAD REQUEST");
			return 400;
		}

		cmd = initial.split("\\s");
		if (cmd.length != 3) {
			System.out.println("BAD REQUEST");
			return 400;
		}

		if (cmd[2].indexOf("HTTP/") == 0 && cmd[2].indexOf('.') > 5) {
			temp = cmd[2].substring(5).split("\\.");
			try {
				ver[0] = Integer.parseInt(temp[0]);
				ver[1] = Integer.parseInt(temp[1]);
			} catch (NumberFormatException nfe) {
				ret = 400;
			}
		} else
			ret = 400;

		if (cmd[0].equals("GET") || cmd[0].equals("HEAD")) {
			method = cmd[0];
			System.out.println(cmd);
			idx = cmd[1].indexOf('?');
			if (idx < 0)
				url = cmd[1];
			else {
				url = URLDecoder.decode(cmd[1].substring(0, idx), "ISO-8859-1");
				prms = cmd[1].substring(idx + 1).split("&");

				params = new HashMap<String, String>();
				for (i = 0; i < prms.length; i++) {
					temp = prms[i].split("=");
					if (temp.length == 2) {
						// we use ISO-8859-1 as temporary charset and then
						// String.getBytes("ISO-8859-1") to get the data
						params.put(URLDecoder.decode(temp[0], "ISO-8859-1"),
								URLDecoder.decode(temp[1], "ISO-8859-1"));
					} else if (temp.length == 1
							&& prms[i].indexOf('=') == prms[i].length() - 1) {
						// handle empty string separatedly
						params.put(URLDecoder.decode(temp[0], "ISO-8859-1"), "");
					}
				}
			}
			parseHeaders();
			if (headers == null)
				ret = 400;
		} else if (cmd[0].equals("POST")) {
			ret = 501; // not implemented
		} else if (ver[0] == 1 && ver[1] >= 1) {
			if (cmd[0].equals("OPTIONS") || cmd[0].equals("PUT")
					|| cmd[0].equals("DELETE") || cmd[0].equals("TRACE")
					|| cmd[0].equals("CONNECT")) {
				ret = 501; // not implemented
			}
		} else {
			// meh not understand, bad request
			ret = 400;
		}

		if (ver[0] == 1 && ver[1] >= 1 && getHeader("Host") == null) {
			ret = 400;
		}

		return ret;
	}

	private void parseHeaders() throws IOException {
		int idx;

		// that fscking rfc822 allows multiple lines, we don't care now
		String line = readLine();
		System.out.println("line: " + line);
		if (line == null) {
			return;
		}
//		if (line.equals("")) {
//			this.state = State.DATA;
//		}
		while (!line.equals("")) {
			idx = line.indexOf(':');
			if (idx < 0) {
				headers = null;
				break;
			} else {
				headers.put(line.substring(0, idx).toLowerCase(), line
						.substring(idx + 1).trim());
			}
			line = readLine();
			if (line == null) {
				return;
			}
//			if (line.equals("")) {
//				this.state = State.DATA;
//			}
		}
		this.state = State.DATA;
		System.out.println("Tail 2: " + tail);
		String s = new String(this.buff.array()).trim();
		System.out.println(s.trim().equals(""));
		if (s.equals("")) {
			this.state = State.END;
		}
		headers.toString();
	}
	
	public void parseData() {
		String line = readLine();
		if (line == null) {
			return;
		}
		if (line.equals("")) {
			this.state = State.END;
		} else {
			this.data = line;
			parseData();
			System.out.println("Data: " + data);
		}
	}

	public String getMethod() {
		return method;
	}

	public String getHeader(String key) {
		if (headers != null)
			return (String) headers.get(key.toLowerCase());
		else
			return null;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public String getRequestURL() {
		return url;
	}

	public String getParam(String key) {
		return (String) params.get(key);
	}

	public Map<String, String> getParams() {
		return params;
	}

	public String getVersion() {
		return ver[0] + "." + ver[1];
	}

	public int compareVersion(int major, int minor) {
		if (major < ver[0])
			return -1;
		else if (major > ver[0])
			return 1;
		else if (minor < ver[1])
			return -1;
		else if (minor > ver[1])
			return 1;
		else
			return 0;
	}

	public static String getHttpReply(int codevalue) {
		String key, ret;
		int i;

		ret = null;
		key = "" + codevalue;
		for (i = 0; i < HttpReplies.length; i++) {
			if (HttpReplies[i][0].equals(key)) {
				ret = codevalue + " " + HttpReplies[i][1];
				break;
			}
		}

		return ret;
	}

	public static String getDateHeader() {
		SimpleDateFormat format;
		String ret;

		format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		ret = "Date: " + format.format(new Date()) + " GMT";

		return ret;
	}

	public State getState() {
		return this.state;
	}
	
	public String toString() {
		return this.state.toString();
	}
	
	public boolean requestFinish() {
		return this.state == State.END;
	}
}