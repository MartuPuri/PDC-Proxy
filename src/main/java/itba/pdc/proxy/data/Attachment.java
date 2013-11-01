package itba.pdc.proxy.data;

import itba.pdc.httpparser.HttpParserRequest;
import itba.pdc.httpparser.ParserCode;
import itba.pdc.httpparser.ParserState;
import itba.pdc.model.HttpRequest;
import itba.pdc.model.HttpResponse;
import itba.pdc.model.StatusRequest;

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
public class Attachment {
	private ByteBuffer buff;
	private ProcessType processID;
	private SelectionKey oppositeKey;
	private SocketChannel oppositeChannel;
	private HttpParserRequest parser;
	private HttpRequest request;

	public Attachment(ProcessType processID, int buffSize) {
		this.processID = processID;
		this.buff = ByteBuffer.allocate(buffSize);
		this.request = new HttpRequest();
		this.parser = new HttpParserRequest(request);
	}

	public ByteBuffer getBuff() {
		return buff;
	}

	public ProcessType getProcessID() {
		return processID;
	}

	public SelectionKey getOppositeKey() {
		return oppositeKey;
	}

	public SocketChannel getOppositeChannel() {
		return oppositeChannel;
	}

	public HttpParserRequest getParser() {
		return parser;
	}

	public HttpRequest getRequest() {
		return request;
	}

	public void setOppositeKey(SelectionKey oppositeKey) {
		this.oppositeKey = oppositeKey;
	}

	public void setOppositeChannel(SocketChannel oppositeChannel) {
		this.oppositeChannel = oppositeChannel;
	}

	public void setBuff(ByteBuffer buff) {
		this.buff = buff;
	}
}