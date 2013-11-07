package itba.pdc.proxy.handler;

import itba.pdc.admin.MetricManager;
import itba.pdc.proxy.data.AttachmentAdmin;
import itba.pdc.proxy.lib.GenerateHttpResponse;
import itba.pdc.proxy.lib.ManageByteBuffer;
import itba.pdc.proxy.lib.ManageParser;
import itba.pdc.proxy.lib.ReadingState;
import itba.pdc.proxy.model.EHttpRequest;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class EHttpHandler implements TCPProtocol {
	private int bufferSize; // Size of I/O buffer

	// private Logger accessLogger = (Logger) LoggerFactory
	// .getLogger("access.log");
	// private Logger debugLog = (Logger) LoggerFactory.getLogger("debug.log");

	public EHttpHandler(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public void handleAccept(SelectionKey key) throws IOException {
		SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
		clntChan.configureBlocking(false);

		SelectionKey clientKey = clntChan.register(key.selector(),
				SelectionKey.OP_READ);
		AttachmentAdmin att = (AttachmentAdmin) key.attachment();
		AttachmentAdmin clientAtt = new AttachmentAdmin(att.getProxyType(),
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
		MetricManager.getInstance().addBytesRead(bytesRead);
		MetricManager.getInstance().addBytesWrite(808);
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
		buf.flip();
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
		EHttpRequest request = att.getRequest();
		switch (requestFinished) {
		case FINISHED:
			
			ByteBuffer responseBuffer;
			try {
				responseBuffer = ManageByteBuffer.encode(GenerateHttpResponse.generateAdminResponse(request));
				att.setBuff(responseBuffer);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			key.interestOps(SelectionKey.OP_WRITE);
			break;
		case UNFINISHED:
			key.interestOps(SelectionKey.OP_READ);
			break;
		case ERROR:
			try {
				GenerateHttpResponse.generateResponseError(request.getStatus());
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
			break;
		}
	}
}