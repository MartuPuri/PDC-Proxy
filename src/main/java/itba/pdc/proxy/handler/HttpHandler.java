package itba.pdc.proxy.handler;

import itba.pdc.admin.MetricManager;
import itba.pdc.proxy.ConnectionManager;
import itba.pdc.proxy.data.AttachmentProxy;
import itba.pdc.proxy.data.ProcessType;
import itba.pdc.proxy.data.ProxyType;
import itba.pdc.proxy.httpparser.HttpParserResponse;
import itba.pdc.proxy.lib.ManageParser;
import itba.pdc.proxy.lib.ReadingState;
import itba.pdc.proxy.model.HttpRequest;
import itba.pdc.proxy.model.HttpResponse;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class HttpHandler implements TCPProtocol {
	private int bufferSize; // Size of I/O buffer
	private int bytes = 0;
	private Logger accessLogger = (Logger) LoggerFactory
			.getLogger("access.log");
	private Logger debugLog = (Logger) LoggerFactory.getLogger("debug.log");
	private static final MetricManager metricManager = MetricManager.getInstance();
	
	public HttpHandler(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public void handleAccept(SelectionKey key) throws IOException {
		SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
		clntChan.configureBlocking(false);

		SelectionKey clientKey = clntChan.register(key.selector(),
				SelectionKey.OP_READ);
		AttachmentProxy att = (AttachmentProxy) key.attachment();
		AttachmentProxy clientAtt = new AttachmentProxy(att.getProcessID(),
				att.getProxyType(), this.bufferSize);
		clientKey.attach(clientAtt);
		accessLogger.info("Accept new connection");
		metricManager.addAccess();
	}

	public void handleRead(SelectionKey key) throws IOException {
		AttachmentProxy att = (AttachmentProxy) key.attachment();
		SocketChannel channel = (SocketChannel) key.channel();

		ByteBuffer buf = att.getBuff();
		final long bytesRead = channel.read(buf);
		if (bytesRead == -1) {
			if (att.getProcessID().equals(ProcessType.SERVER)) {
				HttpParserResponse parser = (HttpParserResponse) att
						.getParser();
				if (parser.isConnectionClose()) {
					if (att.getOppositeKey().isValid()) {
						att.getResponse().setBody(parser.getBuffer());
						att.getOppositeKey().interestOps(SelectionKey.OP_WRITE);
						AttachmentProxy oppositeAtt = (AttachmentProxy) (AttachmentProxy) att
								.getOppositeKey().attachment();
						HttpResponse response = att.getResponse();
						oppositeAtt.setBuff(response.getStream());
					}
				}
			}
			accessLogger.info("Connection with " + att.getProcessID()
					+ " close");
			channel.close();
			key.cancel();
		} else if (bytesRead > 0) {
			metricManager.addBytesRead(bytesRead);
			switch (att.getProcessID()) {
			case CLIENT:
				handleClient(key);
				break;
			case SERVER:
				bytes += bytesRead;
				handleServer(key);
				break;
			default:
				debugLog.error("Trying to read from an invalid process");
				break;
			}

		} else {
			System.out.println(new String(buf.array()));
			buf.compact();
		}
	}

	public void handleWrite(SelectionKey key) throws IOException {
		/*
		 * Channel is available for writing, and key is valid (i.e., client
		 * channel not closed).
		 */
		// Retrieve data read earlier
		AttachmentProxy att = (AttachmentProxy) key.attachment();
		SocketChannel channel = (SocketChannel) key.channel();

		ByteBuffer buf = att.getBuff();
		buf.flip();
		debugLog.debug("Write to " + att.getProcessID() + ": \n" + new String(buf.array()));
		// Prepare buffer for writing
		do {
			if (channel.isOpen() && channel.isConnected()) {
				channel.write(buf);
			} else {
				break;
			}
		} while (buf.hasRemaining());
		if (!buf.hasRemaining()) { // Buffer completely written?
			// Nothing left, so no longer interested in writes
			key.interestOps(SelectionKey.OP_READ);
		}
		buf.compact(); // Make room for more data to be read in
	}

	private void handleClient(SelectionKey key) {
		AttachmentProxy att = (AttachmentProxy) key.attachment();
		ReadingState requestFinished = ManageParser.parse(att.getParser(),
				att.getBuff());
		switch (requestFinished) {
		case FINISHED:
			HttpRequest request = att.getRequest();
			SocketChannel oppositeChannel = null;
			SelectionKey oppositeKey = null;
			try {
				oppositeChannel = ConnectionManager.getInstance().getChannel(
						request.getHost(), request.getPort());
				oppositeChannel.configureBlocking(false);
			} catch (Exception e) {
				accessLogger
						.error("Trying to connect to an invalid host or invalid port: "
								+ request.getHost() + ", " + request.getPort());
				e.printStackTrace();
				return;
			}
			try {
				oppositeKey = oppositeChannel.register(key.selector(),
						SelectionKey.OP_WRITE);
			} catch (ClosedChannelException e) {
				debugLog.error("Trying to register a key in a closed channel");
				e.printStackTrace();
				return;
			}
			AttachmentProxy serverAtt = new AttachmentProxy(ProcessType.SERVER,
					ProxyType.PROXY, this.bufferSize);

			serverAtt.setOppositeKey(key);
			serverAtt.setOppositeChannel((SocketChannel) key.channel());

			att.setOppositeChannel(oppositeChannel);
			att.setOppositeKey(oppositeKey);
			ByteBuffer requestBuffer = request.getStream();
			serverAtt.setBuff(requestBuffer);
			oppositeKey.attach(serverAtt);
			break;
		case UNFINISHED:
			key.interestOps(SelectionKey.OP_READ);
			break;
		case ERROR:
			// HttpResponse.generateResponse(att.getStatusRequest());
			break;
		}
		// att.getBuff().compact();
	}

	private void handleServer(SelectionKey key) {
		AttachmentProxy att = (AttachmentProxy) key.attachment();
		ReadingState responseFinished = ManageParser.parse(att.getParser(),
				att.getBuff());
		switch (responseFinished) {
		case FINISHED:
			if (att.getOppositeKey().isValid()) {
				att.getOppositeKey().interestOps(SelectionKey.OP_WRITE);
				AttachmentProxy oppositeAtt = (AttachmentProxy) (AttachmentProxy) att
						.getOppositeKey().attachment();
				HttpResponse response = att.getResponse();
				oppositeAtt.setBuff(response.getStream());
				
				metricManager.addStatusCode(response.getStatusCode());
				// att.getBuff().compact();
			}
			break;
		case UNFINISHED:
			key.interestOps(SelectionKey.OP_READ);
			break;
		case ERROR:
			// HttpResponse.generateResponse(att.getStatusRequest());
			break;
		}
	}
}