package itba.pdc.proxy.data;

import itba.pdc.proxy.httpparser.HttpParser;
import itba.pdc.proxy.httpparser.HttpParserRequest;
import itba.pdc.proxy.model.EHttpRequest;
import itba.pdc.proxy.model.HttpRequest;
import itba.pdc.proxy.model.HttpResponse;

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
public class AttachmentAdmin {
	private ByteBuffer buff;
	private ProcessType processID;
	private HttpParser parser;
	private EHttpRequest request;

	public AttachmentAdmin(ProcessType processID, int buffSize) {
		this.processID = processID;
		this.buff = ByteBuffer.allocate(buffSize);
		this.request = new EHttpRequest();
		this.parser = new HttpParserRequest(request);
	}

	public ByteBuffer getBuff() {
		return buff;
	}

	public ProcessType getProcessID() {
		return processID;
	}

	public HttpParser getParser() {
		return parser;
	}

	public EHttpRequest getRequest() {
		return request;
	}

	public void setBuff(ByteBuffer buff) {
		this.buff = ByteBuffer.allocate(buff.capacity());
		buff.flip();
		this.buff.put(buff);
	}
}