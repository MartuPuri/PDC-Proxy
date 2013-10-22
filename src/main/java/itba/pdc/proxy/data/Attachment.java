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
	private SelectionKey key;
	private SelectionKey oppositeKey;
	private ByteBuffer buff;
	private ByteBuffer totalBuff;
	private SocketChannel oppositeChannel;
	private SocketChannel channel;
	private HttpParserRequest parser;
	private HttpRequest request = new HttpRequest();
	private HttpResponse response = new HttpResponse();
	private int buffSize;

	public Attachment(SelectionKey _key, ProcessType _processID,
			SocketChannel _channel, int _buffSize, SelectionKey _oppositeKey,
			SocketChannel _oppositeChannel) {
		this(_key, _processID, _channel, _buffSize);
		this.oppositeKey = _oppositeKey;
		this.oppositeChannel = _oppositeChannel;
	}

	public Attachment(SelectionKey _key, ProcessType _processID,
			SocketChannel _channel, int _buffSize) {
		this(_processID);
		this.key = _key;
		this.channel = _channel;
		this.buffSize = _buffSize;
		this.buff = ByteBuffer.allocateDirect(_buffSize);
	}

	public Attachment(ProcessType _processID) {
		this.parser = new HttpParserRequest(this.request, this.response);
		this.totalBuff = ByteBuffer.allocate(0);
		this.processID = _processID;
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

	public SelectionKey getKey() {
		return this.key;
	}

	public SocketChannel getChannel() {
		return this.channel;
	}

	public void setOppositeKey(SelectionKey _key) {
		this.oppositeKey = _key;
	}

	public void setOppositeChannel(SocketChannel _channel) {
		this.oppositeChannel = _channel;
	}

	public void setByteBuffer(ByteBuffer _buff) {
		this.buff = _buff;
	}
	
	public ParserCode parseByteBuffer() throws IOException {
		this.buff.flip();
		increaseTotalByteBuffer();
//		this.buff.flip();
		return parser.parseMessage(this.buff);
	}
	
	private void increaseTotalByteBuffer() {
		ByteBuffer aux = ByteBuffer.allocate(this.totalBuff.capacity() + this.buff.capacity());
		this.totalBuff.flip();
		aux.put(this.totalBuff);
		aux.put(this.buff);
		this.totalBuff = aux;
	}

	public boolean requestFinished() {
		return parser.requestFinish();
	}
	
	public String getHost() {
		return request.getHeader("host");
	}

	public Integer getPort() {
		String port = request.getHeader("Port");
		if (port == null) {
			return 80;
		}
		return Integer.parseInt(port);
	}

	public ByteBuffer getTotalByteBuffer() {
		return this.totalBuff;
	}

	public void setBuffer(ByteBuffer _buff) {
		this.buff = _buff;
	}
	
	public void resetBuffer() {
//		this.buff = ByteBuffer.allocate(this.buffSize);
		this.buff.compact();
	}

	@Deprecated
	public String getState() {
		return parser.getState();
	}

	public void setTotalByteBuffer(ByteBuffer totalByteBuffer) {
		this.totalBuff = totalByteBuffer;
	}
}