package itba.pdc.proxy.parser;

import itba.pdc.proxy.httpparser.enums.ParserCode;
import itba.pdc.proxy.httpparser.enums.ParserState;
import itba.pdc.proxy.httpparser.interfaces.HttpParser;
import itba.pdc.proxy.lib.ManageByteBuffer;
import itba.pdc.proxy.model.HttpMessage;
import itba.pdc.proxy.model.StatusRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class HttpParserRequest implements HttpParser {
	private HttpMessage request;
	private ParserState state;
	private ByteBuffer buffer;

	public HttpParserRequest(HttpMessage _request) {
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
		ByteBuffer aux = ByteBuffer.allocate(buffer.position()
				+ _buff.position());
		_buff.flip();
		buffer.flip();
		aux.put(buffer);
		aux.put(_buff);
		buffer = aux;
	}

	private ParserCode parseMethod() throws UnsupportedEncodingException {
		String prms[], cmd[], temp[];
		int idx, i, version[] = { 0, 0 };
		String line = ManageByteBuffer.readLine(this.buffer);
		;
		Map<String, String> params = null;

		if (line == null) {
			return ParserCode.LOOP;
		}

		cmd = line.split("\\s");

		if (cmd.length != 3) {
			request.setStatusRequest(StatusRequest.BAD_REQUEST);
			return ParserCode.INVALID;
		}

		if (cmd[2].indexOf("HTTP/") == 0 && cmd[2].indexOf('.') > 5) {
			temp = cmd[2].substring(5).split("\\.");
			try {
				version[0] = Integer.parseInt(temp[0]);
				version[1] = Integer.parseInt(temp[1]);
				if (!request.validVersion(version)) {
					return ParserCode.INVALID;
				}
			} catch (NumberFormatException nfe) {
				return ParserCode.INVALID;
			}
		} else {
			return ParserCode.INVALID;
		}

		if (request.validMethod(cmd[0])) {
			String method = cmd[0];
			String uri;
			idx = cmd[1].indexOf('?');
			if (idx < 0) {
				uri = cmd[1];
				System.out.println("Real uri: " + uri);
			} else {
				uri = URLDecoder.decode(cmd[1].substring(0, idx), "ISO-8859-1");
				System.out.println("Semi uri: " + uri);
				prms = cmd[1].substring(idx + 1).split("&");
				params = new HashMap<String, String>();
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
			}
			request.setUri(uri);
			if (params != null) {
				request.setParams(params);
			}
		} else {
			return ParserCode.INVALID;
		}
		request.setMethod(cmd[0]);
		request.setVersion(version);
		this.state = ParserState.HEADERS;
		return ParserCode.CONTINUE;
	}

	private ParserCode parseHeaders() throws IOException {
		int idx;

		String line = ManageByteBuffer.readLine(this.buffer);
		;
		if (line == null) {
			return ParserCode.LOOP;
		}
		while (!line.trim().equals("")) {
			idx = line.indexOf(':');
			if (idx < 0) {
				request.setStatusRequest(StatusRequest.CONFLICT);
				return ParserCode.INVALID;
			} else {
				String headerType = line.substring(0, idx).toLowerCase();
				String headerValue = line.substring(idx + 1).trim();
				request.addHeader(headerType, headerValue);
			}
			line = ManageByteBuffer.readLine(this.buffer);
			;
			if (line == null) {
				return ParserCode.LOOP;
			}
		}

		if (!request.bodyEnable()) {
			state = ParserState.END;
			return ParserCode.VALID;
		} else {
			state = ParserState.DATA;
			return ParserCode.CONTINUE;
		}
	}

	private ParserCode parseData() {
		if (!request.bodyEnable()) {
			return ParserCode.INVALID;
		}
		Integer bytes = Integer.parseInt(request.getHeader("content-length"));
		if (!readBuffer(bytes)) {
			return ParserCode.LOOP;
		}
		request.setBody(this.buffer);
		this.state = ParserState.END;
		return ParserCode.VALID;
	}

	private boolean readBuffer(Integer contentLength) {
		if (this.buffer.capacity() >= contentLength) {
			return true;
		}
		return false;
	}

	public String getState() {
		return this.state.toString();
	}

}