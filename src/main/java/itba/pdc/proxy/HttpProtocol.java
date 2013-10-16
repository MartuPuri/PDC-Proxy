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
//		System.out.println("PROCESS: " + att.getProcessID());
		SocketChannel channel = att.getChannel();
		// SocketChannel oppositeChannel = att.getOppositeChannel();
		// SelectionKey oppositeKey = att.getOppositeKey();

		ByteBuffer buf = att.getByteBuffer();
		System.out.println("Total Buffer yes: " + new String(att.getTotalByteBuffer().array()));
		long bytesRead = channel.read(buf);
		att.setBuffer(buf);
		if (bytesRead == -1) { // Did the other end close?
			channel.close();
		} else if (bytesRead > 0) {
			// TODO: Hardcoding persistent connections. Fix this when the http
			// parser is complete
			SelectionKey oppositeKey;
			SocketChannel oppositeChannel;
			if (att.getProcessID().equals(ProcessType.CLIENT)) {
				att.parseByteBuffer();
				System.out.println("att.requestFinished(): " + att.requestFinished());
				System.out.println("state: " + att.getState());
				if (att.requestFinished()) {
					String host = att.getHost();
					Integer port = att.getPort();
					oppositeChannel = SocketChannel.open(new InetSocketAddress(
							host, port));
					oppositeChannel.configureBlocking(false);
					oppositeKey = oppositeChannel.register(key.selector(),
							SelectionKey.OP_READ);
					Attachment serverAtt = new Attachment(oppositeKey,
							ProcessType.SERVER, oppositeChannel, this.bufSize,
							att.getKey(), channel);
					oppositeKey.attach(serverAtt);
					att.setOppositeChannel(oppositeChannel);
					att.setOppositeKey(oppositeKey);
					serverAtt.setByteBuffer(att.getTotalByteBuffer());
					System.out.println("TOTAL: " + new String(att.getTotalByteBuffer().array()));
					oppositeKey.interestOps(SelectionKey.OP_WRITE);
					key.interestOps(SelectionKey.OP_READ);
				} else {
					System.out.println("Entra aqui");
					att.resetBuffer();
					key.interestOps(SelectionKey.OP_READ);
				}
				// connections.addConnection("80", serverAtt);
				// System.out.println("Use new connections");
				// }
				// Indicate via key that reading/writing are both of interest
				// now.
				// Attachment oppositeAtt = (Attachment)
				// oppositeKey.attachment();
			} else {
				System.out.println("PORQUE ENTRAS");
				oppositeKey = att.getOppositeKey();
				oppositeKey.interestOps(SelectionKey.OP_WRITE);
				key.interestOps(SelectionKey.OP_READ);
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
//		System.out.println("PROCESS: " + att.getProcessID());
		SocketChannel channel = att.getChannel();
		SocketChannel oppositeChannel = att.getOppositeChannel();
		SelectionKey oppositeKey = att.getOppositeKey();

		ByteBuffer buf = att.getByteBuffer();
		System.out.println("PROCESS: " + att.getProcessID());
		System.out.println("WRITE: " + new String(buf.array()));
		buf.flip(); // Prepare buffer for writing
		channel.write(buf);
		if (!buf.hasRemaining()) { // Buffer completely written?
			// Nothing left, so no longer interested in writes
			key.interestOps(SelectionKey.OP_READ);
		}
		buf.compact(); // Make room for more data to be read in
	}
}