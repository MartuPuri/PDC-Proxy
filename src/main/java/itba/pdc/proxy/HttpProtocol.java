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
		SocketChannel serverChannel = SocketChannel.open(new InetSocketAddress("192.168.1.108", 8080));
		serverChannel.configureBlocking(false);
		SelectionKey serverKey = serverChannel.register(key.selector(), SelectionKey.OP_READ);
		Attachment serverAtt = new Attachment(serverKey, ProcessType.SERVER, serverChannel,
				this.bufSize, clientKey, clntChan);
		serverKey.attach(serverAtt);
		Attachment clientAtt = new Attachment(clientKey, ProcessType.CLIENT, clntChan,
				this.bufSize, serverKey, serverChannel);
		clientKey.attach(clientAtt);
	}

	public void handleRead(SelectionKey key) throws IOException {
		// Client socket channel has pending data
		Attachment att = (Attachment) key.attachment();
		System.out.println("PROCESS: " + att.getProcessID());
		SocketChannel channel = att.getChannel();
		SocketChannel oppositeChannel = att.getOppositeChannel();
		SelectionKey oppositeKey = att.getOppositeKey();
		
		ByteBuffer buf = att.getByteBuffer();
		long bytesRead = channel.read(buf);
		if (bytesRead == -1) { // Did the other end close?
			channel.close();
		} else if (bytesRead > 0) {
			// Indicate via key that reading/writing are both of interest now.
			
			Attachment oppositeAtt = (Attachment) oppositeKey.attachment();
			oppositeAtt.setByteBuffer(buf);
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