package itba.pdc.proxy;

import itba.pdc.proxy.data.Attachment;
import itba.pdc.proxy.data.ProcessType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class HttpProtocol implements TCPProtocol {
	private int bufSize; // Size of I/O buffer

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
	}

	public void handleRead(SelectionKey key) throws IOException {
		// Client socket channel has pending data
		Attachment att = (Attachment) key.attachment();
		SocketChannel channel = (SocketChannel) key.channel();

		ByteBuffer buf = att.getByteBuffer();
		long bytesRead = 0;
		try {
			bytesRead = channel.read(buf);
			System.out.println("Bytes Read: " + bytesRead);
		} catch (IOException e) {
			e.printStackTrace();
			key.cancel();
			channel.close();
			return;
		}
		if (bytesRead == -1) { // Did the other end close?
			channel.close();
		} else if (bytesRead > 0) {
			// TODO: Hardcoding persistent connections. Fix this when the http
			if (att.getProcessID().equals(ProcessType.CLIENT)) {
				att.parseByteBuffer(buf);
				if (att.requestFinished()) {
					SelectionKey oppositeKey;
					SocketChannel oppositeChannel;
					oppositeChannel = SocketChannel.open(new InetSocketAddress(
							att.getHost(), 80));
					oppositeChannel.configureBlocking(false);
					oppositeKey = oppositeChannel.register(key.selector(),
							SelectionKey.OP_WRITE);
					Attachment serverAtt = new Attachment(ProcessType.SERVER,
							this.bufSize);
					serverAtt.setOppositeKey(key);
					serverAtt.setOppositeChannel((SocketChannel) key.channel());
					oppositeKey.attach(serverAtt);
					att.setOppositeChannel(oppositeChannel);
					att.setOppositeKey(oppositeKey);
					// Indicate via key that reading/writing are both of
					// interest
					// now.
					// Attachment oppositeAtt = (Attachment)
					// oppositeKey.attachment();
					serverAtt.setByteBuffer(att.getTotalBuffer());
//					oppositeKey.interestOps(SelectionKey.OP_READ);
				}
				key.interestOps(SelectionKey.OP_READ);
			} else {
				att.getOppositeKey().interestOps(SelectionKey.OP_WRITE);
				((Attachment) att.getOppositeKey().attachment())
						.setByteBuffer(buf);
//				key.interestOps(SelectionKey.OP_READ);
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
		buf.flip();
		// System.out.println("Writing to " + att.getProcessID() + ": " + new
		// String(buf.array()));
		// Prepare buffer for writing
		do {
			try{
				channel.write(buf);
			} catch (IOException e) {
//				channel.close();
				System.out.println("Write exception");
				return;
			}
		} while (buf.hasRemaining());
		if (!buf.hasRemaining()) { // Buffer completely written?
			// Nothing left, so no longer interested in writes
			key.interestOps(SelectionKey.OP_READ);
		}
		buf.compact(); // Make room for more data to be read in
	}
}