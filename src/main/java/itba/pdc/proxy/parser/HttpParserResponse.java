package itba.pdc.proxy.parser;

import itba.pdc.proxy.lib.ManageByteBuffer;
import itba.pdc.proxy.model.HttpResponse;
import itba.pdc.proxy.parser.enums.ParserCode;
import itba.pdc.proxy.parser.enums.ParserState;
import itba.pdc.proxy.parser.interfaces.HttpParser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Map.Entry;

import javax.management.RuntimeErrorException;

public class HttpParserResponse implements HttpParser {
	private HttpResponse response;
	private ParserState state;
	private ByteBuffer buffer;
	private boolean connectionClose = false;

	public HttpParserResponse(HttpResponse response) {
		this.response = response;
		this.state = ParserState.METHOD;
		this.buffer = ByteBuffer.allocate(0);
	}

	/**
	 * 
	 * @author mpurita
	 * 
	 * @param Receive
	 *            the buffer that the socket read
	 * 
	 * @return A code that indicate if the parser is valid or invalid when the
	 *         request is finished or continue in the other case
	 * 
	 */
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
//		System.out.println("buff: " + new String(buffer.array()));
//		System.out.println("buff2: " + new String(_buff.array()));
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

		if (line == null) {
			return ParserCode.LOOP;
		}

		cmd = line.split("\\s");

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

		String line = ManageByteBuffer.readLine(this.buffer);
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
				response.addHeader(headerType, headerValue);
				// TODO: Add log
			}
			line = ManageByteBuffer.readLine(this.buffer);
			if (line == null) {
				return ParserCode.LOOP;
			}
		}
		state = ParserState.DATA;
		return ParserCode.CONTINUE;
	}

	private ParserCode parseData() {
		String chunked = response.getHeader("transfer-encoding");
		if (chunked != null) {
			//TODO: Manage chunked
			return manageChunked();
		} else if (response.bodyEnable()) {
			Integer bytes = Integer.parseInt(response
					.getHeader("content-length"));
			if (!readBuffer(bytes)) {
				return ParserCode.LOOP;
			}
			response.setBody(this.buffer);
			this.state = ParserState.END;
			return ParserCode.VALID;
		} else {
			connectionClose = true;
			return ParserCode.LOOP;
		}
	}

	private ParserCode manageChunked() {
		Integer chunkedSize = response.getChunkSize();
//		System.out.println("Buffer: " + new String(buffer.array()));
		if (chunkedSize == null) {
			String hexa = ManageByteBuffer.readLine(buffer);
			System.out.println("Hexa: " + hexa);
			Integer size = Integer.parseInt(hexa, 16);
			System.out.println("Size: " + size);
			response.setChunkedSize(size);
			response.addChunkedBuffer(buffer);
			return ParserCode.LOOP;
		} else {
			if (response.isChunkComplete()) {
				String hexa = ManageByteBuffer.readLine(buffer);
				System.out.println("Hexa: " + hexa);
				Integer size = Integer.parseInt(hexa, 16);
				if (size == 0) {
					System.out.println("entra");
				}
				System.out.println("Size: " + size);
				response.setChunkedSize(size);
			}
			if (response.addChunkedBuffer(buffer)) {
				System.out.println("Termina");
				response.setBody(response.getChunkedBuffer());
				System.out.println("Length: " + response.getLength());
				response.addHeader("content-length", response.getLength());
				response.removeHeader("transfer-encoding");
				state = ParserState.END;
				return ParserCode.VALID;
			}
			return ParserCode.LOOP;
		}
	}

	private boolean readBuffer(Integer contentLength) {
		if (this.buffer.limit() == contentLength) {
			return true;
		}
		return false;
	}

	public String getState() {
		return this.state.toString();
	}

	public boolean isConnectionClose() {
		return connectionClose;
	}

	public ByteBuffer getBuffer() {
		return this.buffer;
	}
}
