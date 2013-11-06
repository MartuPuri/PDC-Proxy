package itba.pdc.proxy;

import itba.pdc.proxy.data.Attachment;
import itba.pdc.proxy.data.AttachmentAdmin;
import itba.pdc.proxy.data.ProcessType;
import itba.pdc.proxy.lib.GenerateHttpResponse;
import itba.pdc.proxy.lib.ManageByteBuffer;
import itba.pdc.proxy.lib.ManageParser;
import itba.pdc.proxy.lib.ReadingState;
import itba.pdc.proxy.model.HttpRequest;
import itba.pdc.proxy.model.HttpResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class HttpHandlerAdmin implements TCPProtocol {
	private int bufferSize; // Size of I/O buffer

	// private Logger accessLogger = (Logger) LoggerFactory
	// .getLogger("access.log");
	// private Logger debugLog = (Logger) LoggerFactory.getLogger("debug.log");

	public HttpHandlerAdmin(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public void handleAccept(SelectionKey key) throws IOException {
		SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
		clntChan.configureBlocking(false);

		SelectionKey clientKey = clntChan.register(key.selector(),
				SelectionKey.OP_READ);
		AttachmentAdmin att = (AttachmentAdmin) key.attachment();
		AttachmentAdmin clientAtt = new AttachmentAdmin(att.getProcessID(),
				this.bufferSize);
		clientKey.attach(clientAtt);
		// accessLogger.info("Accept new connection as admin");
	}

	public void handleRead(SelectionKey key) throws IOException {
		// Client socket channel has pending data
		AttachmentAdmin att = (AttachmentAdmin) key.attachment();
		SocketChannel channel = (SocketChannel) key.channel();

		ByteBuffer buf = att.getBuff();
		final long bytesRead = channel.read(buf);
		// System.out.println("Reading from " + att.getProcessID());
		System.out.println("BytesRead: " + bytesRead);
		if (bytesRead == -1) {
			channel.close();
			key.cancel();
		} else if (bytesRead > 0) {
			handleAdmin(key);
		} else {
			buf.compact();
		}
	}

	public void handleWrite(SelectionKey key) throws IOException {
		/*
		 * Channel is available for writing, and key is valid (i.e., client
		 * channel not closed).
		 */
		AttachmentAdmin att = (AttachmentAdmin) key.attachment();
		SocketChannel channel = (SocketChannel) key.channel();

		ByteBuffer buf = att.getBuff();
		System.out.println("Write Buff position: " + buf.position()
				+ " limit: " + buf.limit());
		buf.flip();
		try {
			System.out.println("Write to " + att.getProcessID() + " : "
					+ ManageByteBuffer.decode(buf));
		} catch (Exception e) {

		}
		// Prepare buffer for writing
		do {
			if (channel.isOpen() && channel.isConnected()) {
				channel.write(buf);
			} else {
				break;
			}
		} while (buf.hasRemaining());
		key.interestOps(SelectionKey.OP_READ);
		buf.compact(); // Make room for more data to be read in
	}

	private void handleAdmin(SelectionKey key) {
		AttachmentAdmin att = (AttachmentAdmin) key.attachment();
		ReadingState requestFinished = ManageParser.parse(att.getParser(),
				att.getBuff());
		HttpRequest request = att.getRequest();
		switch (requestFinished) {
		case FINISHED:
			ByteBuffer responseBuffer = ProcessRequest(request);
			att.setBuff(responseBuffer);
			key.interestOps(SelectionKey.OP_WRITE);
			break;
		case UNFINISHED:
			key.interestOps(SelectionKey.OP_READ);
			break;
		case ERROR:
			try {
				GenerateHttpResponse.generateResponseError(request.getStatusRequest());
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
			break;
		}
	}
}