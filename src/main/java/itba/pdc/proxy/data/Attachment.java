package itba.pdc.proxy.data;

import itba.pdc.httpparser.HttpParserRequest;
import itba.pdc.httpparser.ParserCode;
import itba.pdc.model.HttpRequest;
import itba.pdc.model.HttpResponse;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * 
 * @author Martin Purita
 * @version 1.0
 * 
 *          TODO:
 */
public class Attachment {
	private ProcessType processID;
	private SelectionKey oppositeKey;
	private ByteBuffer buff;
	private ByteBuffer totalBuff;
	private SocketChannel oppositeChannel;
	private int buffSize;
	private HttpParserRequest parser;
	private HttpRequest request;
	private HttpResponse response;

	public Attachment(ProcessType processID, int buffSize) {
		this.processID = processID;
		this.buffSize = buffSize;
		this.buff = ByteBuffer.allocate(buffSize);
		this.request = new HttpRequest();
		this.response = new HttpResponse();
		this.parser = new HttpParserRequest(request, response);
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
		this.buff = ByteBuffer.allocate(_buff.capacity());
		this.buff.put(_buff);
	}

	public boolean requestFinished() {
		return parser.requestFinish();
	}

	public ParserCode parseByteBuffer(ByteBuffer buf) throws IOException {
//		this.buff = ByteBuffer.allocate(buf.capacity());
//		buf.flip();
//		buff.put(buf);
		// this.buff.flip();
		increaseTotalByteBuffer();
		return parser.parseMessage(buf);
	}

	private void increaseTotalByteBuffer() {
		ByteBuffer aux = ByteBuffer.allocate(this.totalBuff.capacity()
				+ this.buff.capacity());
		this.totalBuff.flip();
		aux.put(this.totalBuff);
		this.buff.flip();
		aux.put(this.buff);
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
}