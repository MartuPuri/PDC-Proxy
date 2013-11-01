package itba.pdc.httpparser;

import itba.pdc.model.HttpRequest;
import itba.pdc.model.StatusRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class HttpParserRequest {
	private HttpRequest request;
	private ParserState state;
	private ByteBuffer buffer;
	private StatusRequest statusRequest = StatusRequest.OK;

	public HttpParserRequest(HttpRequest _request) {
		this.request = _request;
		this.state = ParserState.METHOD;
		this.buffer = ByteBuffer.allocate(0);
	}

	public ParserCode parseMessage(ByteBuffer _buff) throws IOException {
		ParserCode code;
		concatBuffer(_buff);
		switch (state) {
		case METHOD:
			code = parseMethod();
			if (code.equals(ParserCode.LOOP)
					|| !code.equals(ParserCode.CONTINUE)) {
				return code;
			}
		case HEADERS:
			code = parseHeaders();
			if (code.equals(ParserCode.LOOP)
					|| !code.equals(ParserCode.CONTINUE)) {
				return code;
			}
		case DATA:
			code = parseData();
			if (code.equals(ParserCode.LOOP)
					|| !code.equals(ParserCode.CONTINUE)) {
				return code;
			}
		case END:
			return ParserCode.VALID;
		default:
			// throw new InvalidParserState();
			return ParserCode.INVALID;
		}
	}

	private void concatBuffer(ByteBuffer _buff) {
		ByteBuffer aux = ByteBuffer.allocate(buffer.capacity()
				+ _buff.capacity());
		_buff.flip();
		buffer.flip();
		aux.put(buffer);
		aux.put(_buff);
		buffer = aux;
	}

	private String readLine() {
		String s = new String(buffer.array());
		String match = "\r\n";
		int index = s.indexOf(match);
		int matchLength = match.length();
		if (index < 0) {
			match = "\n";
			index = s.indexOf(match);
			if (index < 0) {
				return null;
			}
			matchLength = match.length();
		}
		int length = s.length();
		String line = s.substring(0, index);
		if (index < length) {
			int c = buffer.capacity();
			String tail = s.substring(index + matchLength, length);
			buffer.position(index + matchLength);
			buffer.compact();
		} else if (index == length) {
			buffer = ByteBuffer.allocate(length - index);
		}
		return line;
	}

	private ParserCode parseMethod() throws UnsupportedEncodingException {
		String prms[], cmd[], temp[];
		int idx, i, version[] = { 0, 0 };
		String line = readLine();

		if (line == null) {
			return ParserCode.LOOP;
		}

		cmd = line.split("\\s");

		if (cmd.length != 3) {
			statusRequest = StatusRequest.BAD_REQUEST;
			System.out.println("BAD REQUEST");// TODO: Change for log
			// request.invalidRequestLine(response);
			return ParserCode.INVALID;
		}

		if (cmd[2].indexOf("HTTP/") == 0 && cmd[2].indexOf('.') > 5) {
			temp = cmd[2].substring(5).split("\\.");
			try {
				version[0] = Integer.parseInt(temp[0]);
				version[1] = Integer.parseInt(temp[1]);
				if (!request.validVersion(version)) {
					// TODO: Add log
					statusRequest = request.getStatusRequest();
					return ParserCode.INVALID;
				}
			} catch (NumberFormatException nfe) {
				// TODO: Add Log
				// request.invalidRequestLine(response);
				statusRequest = request.getStatusRequest();
				return ParserCode.INVALID;
			}
		} else {
			// request.invalidRequestLine(response);
			statusRequest = request.getStatusRequest();
			return ParserCode.INVALID;
		}

		if (request.validMethod(cmd[0])) {
			String method = cmd[0];
			// System.out.println(cmd);
			String uri;
			idx = cmd[1].indexOf('?');
			if (idx < 0) {
				uri = cmd[1];
			} else {
				uri = URLDecoder.decode(cmd[1].substring(0, idx), "ISO-8859-1");
				prms = cmd[1].substring(idx + 1).split("&");
				Map<String, String> params = new HashMap<String, String>();
				for (i = 0; i < prms.length; i++) {
					temp = prms[i].split("=");
					if (temp.length == 2) {
						params.put(URLDecoder.decode(temp[0], "ISO-8859-1"),
								URLDecoder.decode(temp[1], "ISO-8859-1"));
					} else if (temp.length == 1
							&& prms[i].indexOf('=') == prms[i].length() - 1) {
						params.put(URLDecoder.decode(temp[0], "ISO-8859-1"), "");
					}
				}
				request.setParams(params);
			}
			request.setUri(uri);
		} else {
			statusRequest = request.getStatusRequest();
			return ParserCode.INVALID;
		}
		// TODO: Add Log
		request.setMethod(cmd[0]);
		request.setVersion(version);
		this.state = ParserState.HEADERS;
		return ParserCode.CONTINUE;
	}

	private ParserCode parseHeaders() throws IOException {
		int idx;

		String line = readLine();
		if (line == null) {
			// TODO: Log
			return ParserCode.LOOP;
		}
		while (!line.trim().equals("")) {
			idx = line.indexOf(':');
			if (idx < 0) {
				statusRequest = StatusRequest.CONFLICT;
				// TODO: request.invalidHeader(response);
				// TODO: Add log
				return ParserCode.INVALID;
			} else {
				String headerType = line.substring(0, idx).toLowerCase();
				String headerValue = line.substring(idx + 1).trim();
				request.addHeader(headerType, headerValue);
				// TODO: Add log
			}
			line = readLine();
			if (line == null) {
				return ParserCode.LOOP;
			}
		}
		String s = new String(buffer.array()).trim();
		if (s.equals("")) {
			// TODO: Add Log
			state = ParserState.END;
			return ParserCode.VALID;
		} else {
			// TODO : Add log
			state = ParserState.DATA;
			return ParserCode.CONTINUE;
		}
	}

	private ParserCode parseData() {
		if (!request.bodyEnable()) {
			statusRequest = request.getStatusRequest();
			return ParserCode.INVALID;
		}
		Integer bytes = Integer.parseInt(request.getHeader("Content-Length"));
		String data = readBuffer(bytes);
		if (data == null) {
			return ParserCode.LOOP;
		}
		request.setBody(data);
		this.state = ParserState.END;
		return ParserCode.VALID;
	}

	private String readBuffer(Integer contentLength) {
		if (this.buffer.capacity() >= contentLength) {
			return new String(this.buffer.array());
		}
		return null;
	}

	public boolean requestIsComplete() {
		return state == ParserState.END;
	}

	public boolean requestFinish() {
		return this.state == ParserState.END;
	}

	public String getState() {
		return this.state.toString();
	}
}
