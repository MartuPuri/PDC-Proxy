package itba.pdc.proxy;

import itba.pdc.proxy.data.Attachment;
import itba.pdc.proxy.data.ProcessType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class HttpProtocol implements TCPProtocol {
	private int bufSize; // Size of I/O buffer
	private static Logger debugLogger = (Logger) LoggerFactory.getLogger("debug.log");
	private static Logger accessLogger = (Logger) LoggerFactory.getLogger("access.log");

	public HttpProtocol(int bufSize) {
		this.bufSize = bufSize;
	}

	public void handleAccept(SelectionKey key) throws IOException {
		SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
		clntChan.configureBlocking(false); // Must be nonblocking to register
		// Register the selector with new channel for read and attach byte
		// buffer
		SelectionKey clientKey = clntChan.register(key.selector(),
				SelectionKey.OP_READ);
		Attachment clientAtt = new Attachment(ProcessType.CLIENT, this.bufSize);
		clientKey.attach(clientAtt);
		System.out.println("ENTRA");
		accessLogger.info("Accepting new connection");
	}

	public void handleRead(SelectionKey key) throws IOException {
		// Client socket channel has pending data
		Attachment att = (Attachment) key.attachment();
		SocketChannel channel = (SocketChannel) key.channel();

		ByteBuffer buf = att.getByteBuffer();
		long bytesRead = channel.read(buf);
		if (bytesRead == -1) { // Did the other end close?
			accessLogger.info("Close connection from " + att.getProcessID());
			channel.close();
		} else if (bytesRead > 0) {
			// TODO: Hardcoding persistent connections. Fix this when the http
			if (att.getProcessID().equals(ProcessType.CLIENT)) {
				debugLogger.info("Reading from client");
				att.parseByteBuffer(buf);
				if (att.requestFinished()) {
					debugLogger.info("Request complete");
					String host = att.getHost();
					Integer port = att.getPort();
					SocketChannel oppositeChannel = SocketChannel.open(new InetSocketAddress(
							host, port));
					accessLogger.info("Open new connection to " + host + " at " + port);
					oppositeChannel.configureBlocking(false);
					SelectionKey oppositeKey = oppositeChannel.register(key.selector(),
							SelectionKey.OP_WRITE);
					Attachment serverAtt = new Attachment(ProcessType.SERVER,
							this.bufSize);
					serverAtt.setOppositeKey(key);
					serverAtt.setOppositeChannel((SocketChannel) key.channel());
					oppositeKey.attach(serverAtt);
					att.setOppositeChannel(oppositeChannel);
					att.setOppositeKey(oppositeKey);
					serverAtt.setByteBuffer(att.getTotalBuffer());
					oppositeKey.interestOps(SelectionKey.OP_WRITE);
				}
				key.interestOps(SelectionKey.OP_READ);
			} else {
				debugLogger.info("Reading from server");
				att.getOppositeKey().interestOps(SelectionKey.OP_WRITE);
				((Attachment)att.getOppositeKey().attachment()).setByteBuffer(buf);
			}
		}
	}

	public void handleWrite(SelectionKey key) throws IOException {
		/*
		 * Channel is available for writing, and key is valid (i.e., client
		 * channel not closed).
		 */
		// Retrieve data read earlier
		Attachment att = (Attachment) key.attachment();
		SocketChannel channel = (SocketChannel) key.channel();

		ByteBuffer buf = att.getByteBuffer();
		debugLogger.info("Writing to " + att.getProcessID());
		buf.flip();
		// System.out.println("Writing to " + att.getProcessID() + ": " + new
		// String(buf.array()));
		// Prepare buffer for writing
		do {
			channel.write(buf);
		} while (buf.hasRemaining());
		if (!buf.hasRemaining()) { // Buffer completely written?
			// Nothing left, so no longer interested in writes
			key.interestOps(SelectionKey.OP_READ);
		}
		buf.compact(); // Make room for more data to be read in
	}
}