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
		System.out.println("Accept");
		Attachment clientAtt = new Attachment(clientKey, ProcessType.CLIENT,
				clntChan, this.bufSize);
		clientKey.attach(clientAtt);
	}

	public void handleRead(SelectionKey key) throws IOException {
		// Client socket channel has pending data
		Attachment att = (Attachment) key.attachment();
		SocketChannel channel = att.getChannel();
		System.out.println("READING FROM : " + att.getProcessID());
		ByteBuffer buf = att.getByteBuffer();
		System.out.println("Position: " + buf.position() + " Limit: "
				+ buf.limit() + " Remaining: " + buf.remaining()
				+ " Capacity: " + buf.capacity());
		long bytesRead = channel.read(buf);
		System.out.println("Bytes read: " + bytesRead);
		att.setBuffer(buf);
		// ByteBuffer b = ByteBuffer.allocate(buf.capacity());
		// buf.flip();
		// b.put(buf);
		// System.out.println("Read: " + new String(b.array()));
		// buf.flip();
		// buf.limit(limit);
		// System.out.println("Total Buffer yes: " + new
		// String(att.getTotalByteBuffer().array()));
		if (bytesRead == -1) { // Did the other end close?
			System.out.println("Cerre");
			channel.close();
		} else if (bytesRead > 0) {
			// TODO: Hardcoding persistent connections. Fix this when the http
			// parser is complete
			SelectionKey oppositeKey;
			SocketChannel oppositeChannel;
			if (att.getProcessID().equals(ProcessType.CLIENT)) {
				att.parseByteBuffer();
				// System.out.println("att.requestFinished(): " +
				// att.requestFinished());
				System.out.println("state: " + att.getState());
				if (att.requestFinished()) {
					System.out.println("TOTAL: "
							+ new String(att.getTotalByteBuffer().array()));
					String host = att.getHost();
					Integer port = att.getPort();
					oppositeChannel = SocketChannel.open(new InetSocketAddress(
							host, port));
					oppositeChannel.configureBlocking(false);
					oppositeKey = oppositeChannel.register(key.selector(),
							SelectionKey.OP_WRITE);
					Attachment serverAtt = new Attachment(oppositeKey,
							ProcessType.SERVER, oppositeChannel, this.bufSize,
							att.getKey(), channel);
					oppositeKey.attach(serverAtt);
					att.setOppositeChannel(oppositeChannel);
					att.setOppositeKey(oppositeKey);
					serverAtt.setTotalByteBuffer(att.getTotalByteBuffer());
					// oppositeKey.interestOps(SelectionKey.OP_WRITE);
					key.interestOps(SelectionKey.OP_READ);
				} else {
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
				// READ FROM SERVER
				// ByteBuffer p = ByteBuffer.allocate(700);
				// buf.flip();
				// p.put(buf);
				// System.out.println("FUCK: " + new String(p.array()));
				// System.out.println("PORQUE ENTRAS");
				oppositeKey = att.getOppositeKey();
				((Attachment) oppositeKey.attachment()).setTotalByteBuffer(buf);
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
		// System.out.println("PROCESS: " + att.getProcessID());
		SocketChannel channel = att.getChannel();
		SocketChannel oppositeChannel = att.getOppositeChannel();
		SelectionKey oppositeKey = att.getOppositeKey();

		ByteBuffer buf = att.getTotalByteBuffer();
		System.out.println("WRITING TO PROCESS: " + att.getProcessID());
		// System.out.println("DATA: " + new String(buf.array()));
		buf.flip(); // Prepare buffer for writing
		do {
			channel.write(buf);
			System.out.println("REMAINING: " + buf.remaining());
		} while (buf.hasRemaining());
		// Buffer completely written?
		// Nothing left, so no longer interested in writes
		buf.compact(); // Make room for more data to be read in
		key.interestOps(SelectionKey.OP_READ);
	}
}