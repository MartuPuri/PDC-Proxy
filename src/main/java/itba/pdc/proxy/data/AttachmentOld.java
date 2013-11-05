package itba.pdc.proxy.data;

import itba.pdc.proxy.httpparser.HttpParserRequest;
import itba.pdc.proxy.httpparser.ParserCode;
import itba.pdc.proxy.httpparser.ParserState;
import itba.pdc.proxy.lib.GenerateHttpResponse;
import itba.pdc.proxy.model.HttpRequest;
import itba.pdc.proxy.model.StatusRequest;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import javax.swing.text.Position;

/**
 * 
 * @author Martin Purita
 * @version 1.0
 * 
 *          TODO:
 */
public class AttachmentOld {
	private ProcessType processID;
	private SelectionKey oppositeKey;
	private ByteBuffer buff;
	private ParserCode parserCode = ParserCode.CONTINUE;
	private ByteBuffer totalBuff;
	private SocketChannel oppositeChannel;
	private int buffSize;
	private HttpParserRequest parser;
	private HttpRequest request;

	public AttachmentOld(ProcessType processID, int buffSize) {
		this.processID = processID;
		this.buffSize = buffSize;
		this.buff = ByteBuffer.allocate(buffSize);
		this.request = new HttpRequest();
		this.parser = new HttpParserRequest(request);
		this.totalBuff = ByteBuffer.allocate(0);
	}

	public ProcessType getProcessID() {
		return this.processID;
	}

	public SocketChannel getOppositeChannel() {
		return this.oppositeChannel;
	}

	public ByteBuffer getByteBuffer() {
		return this.buff;
	}

	public SelectionKey getOppositeKey() {
		return this.oppositeKey;
	}

	public void setOppositeKey(SelectionKey _key) {
		this.oppositeKey = _key;
	}

	public void setOppositeChannel(SocketChannel _channel) {
		this.oppositeChannel = _channel;
	}

	public void setByteBuffer(ByteBuffer _buff) {
//		if (_buff.position() == _buff.limit() &&  _buff.position() != 0) {
			_buff.flip();
//		}
		this.buff = ByteBuffer.allocate(_buff.capacity());
		this.buff.put(_buff);
	}

	public boolean requestFinished() {
		return parser.requestFinish();
	}

	public void parseByteBuffer(ByteBuffer buf) throws IOException {
//		this.buff = ByteBuffer.allocate(buf.capacity());
//		buf.flip();
//		buff.put(buf);
		// this.buff.flip();
		increaseTotalByteBuffer();
		parserCode = parser.parseMessage(buf);
	}

	private void increaseTotalByteBuffer() {
		ByteBuffer aux = ByteBuffer.allocate(this.totalBuff.capacity()
				+ this.buff.capacity());
		this.totalBuff.flip();
		aux.put(this.totalBuff);
		this.buff.flip();
		aux.put(this.buff);
		this.buff.compact();
		this.totalBuff = aux;
	}

	public ByteBuffer getTotalBuffer() {
		return this.totalBuff;
	}
	
	public String getState() {
		return this.parser.getState();
	}
	
	public String getHost() {
		return this.request.getHeader("host");
	}
	
	public boolean validRequest() {
		if (parserCode.equals(ParserCode.INVALID)) {
			return false;
		}
		return true;
	}
	
	public StatusRequest getStatusRequest() {
		return request.getStatusRequest();
	}
}