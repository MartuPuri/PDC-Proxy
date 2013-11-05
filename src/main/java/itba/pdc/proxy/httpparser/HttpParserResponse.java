package itba.pdc.proxy.httpparser;

import itba.pdc.proxy.model.HttpRequest;
import itba.pdc.proxy.model.HttpResponse;
import itba.pdc.proxy.model.StatusRequest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class HttpParserResponse implements HttpParser {
	private HttpResponse response;
	private ParserState state;
	private ByteBuffer buffer;
	private int bytes = 0;

	public HttpParserResponse(HttpResponse response) {
		this.response = response;
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
			return ParserCode.INVALID;
		}
	}

	private void concatBuffer(ByteBuffer _buff) {
		System.out.println("Concat: buffer position:" + buffer.position() + "  buffer. limit: " + buffer.limit());
		System.out.println("Concat: _BUFF position:" + _buff.position() + "  _BUFF. limit: " + _buff.limit());
		ByteBuffer aux = ByteBuffer.allocate(buffer.position()
				+ _buff.position());
		_buff.flip();
		buffer.flip();
		aux.put(buffer);
		aux.put(_buff);
		buffer = aux;
		System.out.println("Buffer limit: " + buffer.limit() + " position: " + buffer.position());
	}

	private String readLine() {
		System.out.println("Read line Buffer limit: " + buffer.limit() + " position: " + buffer.position());
		String s = new String(buffer.array());
		System.out.println("S: " + s);
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
		bytes += index + matchLength;
		System.out.println("Read: " + line.getBytes().length);
		System.out.println("Bytes read from buffer: " + bytes);
		if (index < length) {
			System.out.println("s.length: " + s.length());
			System.out.println("Diffrence: " + (length - index - matchLength));
			String tail = s.substring(index + matchLength, length);
			System.out.println("Tail: " + tail);
			System.out.println("Tail.length:" + tail.length());
			System.out.println("tail.getBytes().length: " + tail.getBytes().length);
			buffer = ByteBuffer.allocate(tail.getBytes().length);
			buffer.put(tail.getBytes());
		} else if (index == length) {
			buffer = ByteBuffer.allocate(0);
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

//		if (cmd.length != 3) {
//			System.out.println("BAD REQUEST");// TODO: Change for log
//			// request.invalidRequestLine(response);
//			return ParserCode.INVALID;
//		}

		if (cmd[0].indexOf("HTTP/") == 0 && cmd[0].indexOf('.') > 5) {
			temp = cmd[0].substring(5).split("\\.");
			try {
				version[0] = Integer.parseInt(temp[0]);
				version[1] = Integer.parseInt(temp[1]);
				if (!response.validVersion(version)) {
					// TODO: Add log
					return ParserCode.INVALID;
				}
			} catch (NumberFormatException nfe) {
				// TODO: Add Log
				// request.invalidRequestLine(response);
				return ParserCode.INVALID;
			}
		} else {
			// request.invalidRequestLine(response);
			return ParserCode.INVALID;
		}

		Integer code = Integer.parseInt(cmd[1]);
		// TODO: Add Log
		response.setVersion(version);
		response.setCode(code);
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
				// TODO: request.invalidHeader(response);
				// TODO: Add log
				return ParserCode.INVALID;
			} else {
				String headerType = line.substring(0, idx).toLowerCase();
				String headerValue = line.substring(idx + 1).trim();
				System.out.println("Header type: " + headerType + "  headerValue: " + headerValue);
				response.addHeader(headerType, headerValue);
				// TODO: Add log
			}
			line = readLine();
//			System.out.println("Header line: " + line);
			if (line == null) {
				return ParserCode.LOOP;
			}
		}
		System.out.println("Finish header: pos: " + buffer.position() + " lmit: " + buffer.limit());
//		buffer.flip();
//		System.out.println("Buff: " + new String(buffer.array()));
//		int position = buffer.position() - 8;
//		buffer.position(position);
		state = ParserState.DATA;
		return ParserCode.CONTINUE;
	}

	private ParserCode parseData() {
		if (!response.bodyEnable()) {
			return ParserCode.INVALID;
		}
		Integer bytes = Integer.parseInt(response.getHeader("content-length"));
		String data = readBuffer(bytes);
		if (data == null) {
			return ParserCode.LOOP;
		}
//		FileWriter f;
//		try {
//			f = new FileWriter("imagen.txt");
//			BufferedWriter bw = new BufferedWriter(f);
//			System.out.println("BYTES DATA: " + data.getBytes().le);
//			bw.write(data);
//			bw.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		response.setBody(this.buffer);
		this.state = ParserState.END;
		return ParserCode.VALID;
	}

	private String readBuffer(Integer contentLength) {
//		System.out.println("Limit: " + this.buffer.limit() + " position: " + this.buffer.position());
		System.out.println("length: " + contentLength);
		if (this.buffer.limit() >= contentLength) {
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
