package itba.pdc.proxy;

import itba.pdc.proxy.data.Attachment;
import itba.pdc.proxy.data.PersistentConnection;
import itba.pdc.proxy.data.ProcessType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class HttpProtocol implements TCPProtocol {
	private int bufSize; // Size of I/O buffer
	private PersistentConnection connections;

	public HttpProtocol(int bufSize) {
		this.bufSize = bufSize;
		this.connections = PersistentConnection.getInstance();
	}

	public void handleAccept(SelectionKey key) throws IOException {
		SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
		clntChan.configureBlocking(false); // Must be nonblocking to register
		// Register the selector with new channel for read and attach byte
		// buffer
		SelectionKey clientKey = clntChan.register(key.selector(),
				SelectionKey.OP_READ);
		// SocketChannel serverChannel = SocketChannel.open(new
		// InetSocketAddress("192.168.1.108", 8080));
		// serverChannel.configureBlocking(false);
		// SelectionKey serverKey = serverChannel.register(key.selector(),
		// SelectionKey.OP_READ);
		// Attachment serverAtt = new Attachment(serverKey, ProcessType.SERVER,
		// serverChannel,
		// this.bufSize, clientKey, clntChan);
		// serverKey.attach(serverAtt);
		Attachment clientAtt = new Attachment(clientKey, ProcessType.CLIENT,
				clntChan, this.bufSize);
		clientKey.attach(clientAtt);
	}

	public void handleRead(SelectionKey key) throws IOException {
		// Client socket channel has pending data
		Attachment att = (Attachment) key.attachment();
		System.out.println("PROCESS: " + att.getProcessID());
		SocketChannel channel = att.getChannel();
		// SocketChannel oppositeChannel = att.getOppositeChannel();
		// SelectionKey oppositeKey = att.getOppositeKey();

		ByteBuffer buf = att.getByteBuffer();
		long bytesRead = channel.read(buf);
		if (bytesRead == -1) { // Did the other end close?
			channel.close();
		} else if (bytesRead > 0) {
			// TODO: Hardcoding persistent connections. Fix this when the http
			// parser is complete
			SelectionKey oppositeKey;
			SocketChannel oppositeChannel;
			if (att.getProcessID().equals(ProcessType.CLIENT)) {
				//String port = new String(buf.array()).trim();
				Attachment serverAtt;// = connections.getConnection(port);
				//if (serverAtt != null) {
//					oppositeKey = serverAtt.getKey();
//					oppositeKey.attach(serverAtt);
//					oppositeChannel = serverAtt.getChannel();
//					System.out.println("User existing connections");
//				} else {
					oppositeChannel = SocketChannel.open(new InetSocketAddress(
							"www.google.com", 80));
					oppositeChannel.configureBlocking(false);
					oppositeKey = oppositeChannel.register(key.selector(),
							SelectionKey.OP_READ);
					serverAtt = new Attachment(oppositeKey, ProcessType.SERVER,
							oppositeChannel, this.bufSize, att.getKey(),
							channel);
					oppositeKey.attach(serverAtt);
					connections.addConnection("80", serverAtt);
					System.out.println("Use new connections");
				//}
				att.setOppositeChannel(oppositeChannel);
				att.setOppositeKey(oppositeKey);
				// Indicate via key that reading/writing are both of interest
				// now.
				// Attachment oppositeAtt = (Attachment)
				// oppositeKey.attachment();
				serverAtt.setByteBuffer(buf);
			} else {
				oppositeKey = att.getOppositeKey();
			}
			oppositeKey.interestOps(SelectionKey.OP_WRITE);
			key.interestOps(SelectionKey.OP_READ);
		}
	}

	public void handleWrite(SelectionKey key) throws IOException {
		/*
		 * Channel is available for writing, and key is valid (i.e., client
		 * channel not closed).
		 */
		// Retrieve data read earlier
		Attachment att = (Attachment) key.attachment();
		System.out.println("PROCESS: " + att.getProcessID());
		SocketChannel channel = att.getChannel();
		SocketChannel oppositeChannel = att.getOppositeChannel();
		SelectionKey oppositeKey = att.getOppositeKey();

		ByteBuffer buf = att.getByteBuffer();
		buf.flip(); // Prepare buffer for writing
		channel.write(buf);
		if (!buf.hasRemaining()) { // Buffer completely written?
			// Nothing left, so no longer interested in writes
			key.interestOps(SelectionKey.OP_READ);
		}
		buf.compact(); // Make room for more data to be read in
	}
}