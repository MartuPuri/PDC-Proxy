package itba.pdc.proxy;

import itba.pdc.proxy.data.AttachmentProxy;
import itba.pdc.proxy.data.ProcessType;
import itba.pdc.proxy.data.ProxyType;
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

public class HttpHandler implements TCPProtocol {
	private int bufferSize; // Size of I/O buffer
	private int bytes = 0;
	private Logger accessLogger = (Logger) LoggerFactory
			.getLogger("access.log");
	private Logger debugLog = (Logger) LoggerFactory.getLogger("debug.log");

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
	}

	public void handleRead(SelectionKey key) throws IOException {
		// Client socket channel has pending data
		AttachmentProxy att = (AttachmentProxy) key.attachment();
		SocketChannel channel = (SocketChannel) key.channel();

		ByteBuffer buf = att.getBuff();
		final long bytesRead = channel.read(buf);
		// System.out.println("Reading from " + att.getProcessID());
		System.out.println("BytesRead: " + bytesRead);
		if (bytesRead == -1) {
			accessLogger.info("Connection with " + att.getProcessID()
					+ " close");
			channel.close();
			key.cancel();
		} else if (bytesRead > 0) {
			switch (att.getProcessID()) {
			case CLIENT:
				handleClient(key);
				break;
			case SERVER:
				bytes += bytesRead;
				System.out.println("Bytes: " + bytes + " Bytes read: "
						+ bytesRead);
				handleServer(key);
				break;
			default:
				debugLog.error("Trying to read from an invalid process");
				break;
			}

		} else {
			buf.compact();
		}

		// key.interestOps(SelectionKey.OP_READ);
		// } else if (att.getProcessID().equals(ProcessType.ADMIN)) {
		// String data =
		// "<html><body><h1>Bahui la tenes adentro</h1></body></html>";
		// String response =
		// "HTTP/1.1 200 OK\nDate: Fri, 31 Dec 1999 23:59:59 GMT\nContent-Type: text/html\nContent-Length: "
		// + data.getBytes().length + "\n\n";
		// String aux = response + data;
		// ByteBuffer bufferResponse = ByteBuffer.allocate(aux.length());
		// bufferResponse.put(aux.getBytes());
		// bufferResponse.position(0);
		// att.setByteBuffer(bufferResponse);
		// key.interestOps(SelectionKey.OP_WRITE);
		// }
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
		// System.out.println("Client status: " + att.getParser().getState());
		// System.out.println("Client request: " + requestFinished);
		switch (requestFinished) {
		case FINISHED:
			HttpRequest request = att.getRequest();
			SocketChannel oppositeChannel = null;
			SelectionKey oppositeKey = null;
			try {
				oppositeChannel = SocketChannel.open(new InetSocketAddress(
						request.getHost(), request.getPort()));
				// oppositeChannel = SocketChannel
				// .open(new InetSocketAddress("10.6.0.158", 8080));
				oppositeChannel.configureBlocking(false);
			} catch (Exception e) {
				accessLogger
						.error("Trying to connect to an invalid host or invalid port: "
								+ request.getHost() + ", " + request.getPort());
				return;
			}
			try {
				oppositeKey = oppositeChannel.register(key.selector(),
						SelectionKey.OP_WRITE);
			} catch (ClosedChannelException e) {
				debugLog.error("Trying to register a key in a closed channel");
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
		// System.out.println("Entra server");
		AttachmentProxy att = (AttachmentProxy) key.attachment();
		ReadingState responseFinished = ManageParser.parse(att.getParser(),
				att.getBuff());
		// System.out.println("Server response: " + responseFinished);
		switch (responseFinished) {
		case FINISHED:
			if (att.getOppositeKey().isValid()) {
				att.getOppositeKey().interestOps(SelectionKey.OP_WRITE);
				AttachmentProxy oppositeAtt = (AttachmentProxy) (AttachmentProxy) att
						.getOppositeKey().attachment();
				HttpResponse response = att.getResponse();
				oppositeAtt.setBuff(response.getStream());
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