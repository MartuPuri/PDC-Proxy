package itba.pdc.httpparser;

import itba.pdc.model.HttpMessage;
import itba.pdc.model.HttpRequest;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import exceptions.InvalidParserState;

public class HttpParserRequest {
	private HttpMessage request;
	private HttpMessage response;
	private ParserState state;
	private ByteBuffer buffer;

	public HttpParserRequest(HttpMessage _request, HttpMessage _response) {
		this.request = _request;
		this.response = _response;
		this.state = ParserState.METHOD;
		this.buffer = ByteBuffer.allocate(0);
	}

	public ParserCode parseMessage(ByteBuffer _buff) {
		concatBuffer(_buff);
		switch (state) {
		case METHOD:
			return parseMethod();
		case HEADERS:
			return parseHeaders();
		case DATA:
			return parseData();
		case END:
			return ParserCode.VALID;
		default:
			throw new InvalidParserState();
		}
	}

	private void concatBuffer(ByteBuffer _buff) {
		buffer.flip();
		_buff.flip();
		ByteBuffer aux = ByteBuffer.allocate(buffer.capacity()
				+ _buff.capacity());
		aux.put(this.buffer);
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
			buffer = ByteBuffer.allocate(length - index - 1);
			String tail = s.substring(index + matchLength, length);
			buffer.put(tail.getBytes());
		} else if (index == length) {
			buffer = ByteBuffer.allocate(length - index);
		}
		buffer.flip();
		return line;
	}

	private ParserCode parseMethod() {
		String prms[], cmd[], temp[];
		int idx, i, version[];
		String line = readLine();

		if (line == null) {
			return ParserCode.CONTINUE;
		}
		int length = line.length();
		int j = 0;
		while (j < length && Character.isWhitespace(line.charAt(j++)))
			;
		line = line.substring(j);

		cmd = line.split("\\s");

		if (cmd.length != 3) {
			System.out.println("BAD REQUEST");// TODO: Change for log
			request.invalidRequestLine(response);
			return ParserCode.INVALID;
		}

		if (cmd[2].indexOf("HTTP/") == 0 && cmd[2].indexOf('.') > 5) {
			temp = cmd[2].substring(5).split("\\.");
			try {
				version[0] = Integer.parseInt(temp[0]);
				version[1] = Integer.parseInt(temp[1]);
				if (!request.validVersion(version)) {
					// TODO: Add log
					request.invalidVersion(response);
					return ParserCode.INVALID;
				}
			} catch (NumberFormatException nfe) {
				// TODO: Add Log
				request.invalidRequestLine(response);
				return ParserCode.INVALID;
			}
		} else {
			request.invalidRequestLine(response);
			return ParserCode.INVALID;
		}

		if (cmd[0].equals("GET") || cmd[0].equals("HEAD")
				|| cmd[0].equals("POST")) {
			String method = cmd[0];
			String uri;
			// System.out.println(cmd);
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
		}
		// TODO: Add Log
		request.setMethod(cmd[0]);
		request.setVersion(version);
		request.setUri(uri);
		return ParserCode.VALID;
	}

	private ParserCode parseHeaders() throws IOException {
		int idx;

		String line = readLine();
		if (line == null) {
			// TODO: Log
			return ParserCode.CONTINUE;
		}
		// if (line.equals("")) {
		// this.state = State.DATA;
		// }
		while (!line.equals("")) {
			idx = line.indexOf(':');
			if (idx < 0) {
				request.invalidHeader(response);
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
				return ParserCode.CONTINUE;
			}
		}
		String s = new String(buffer.array()).trim();
		if (s.equals("")) {
			// TODO: Add Log
			state = ParserState.END;
		} else {
			// TODO : Add log
			state = ParserState.DATA;
		}
	}
	
	public boolean requestIsComplete() {
		return state == ParserState.END;
	}
}
