package itba.pdc.proxy.data;

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
	private SocketChannel oppositeChannel;
	private SocketChannel channel;
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
}