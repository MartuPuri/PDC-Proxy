package itba.pdc.proxy;

import itba.pdc.proxy.data.AttachmentOld;
import itba.pdc.proxy.data.ProcessType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class HttpProtocolOld implements TCPProtocol {
	private int bufSize; // Size of I/O buffer

	public HttpProtocolOld(int bufSize) {
		this.bufSize = bufSize;
	}

	public void handleAccept(SelectionKey key) throws IOException {
		SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
		clntChan.configureBlocking(false); // Must be nonblocking to register
		// Register the selector with new channel for read and attach byte
		// buffer
		SelectionKey clientKey = clntChan.register(key.selector(),
				SelectionKey.OP_READ);
		AttachmentOld att = (AttachmentOld) key.attachment();
		AttachmentOld clientAtt = new AttachmentOld(att.getProcessID(), this.bufSize);
		clientKey.attach(clientAtt);
		System.out.println("PROCESS: "
				+ ((AttachmentOld) key.attachment()).getProcessID());
	}

	public void handleRead(SelectionKey key) throws IOException {
		// Client socket channel has pending data
		AttachmentOld att = (AttachmentOld) key.attachment();
		SocketChannel channel = (SocketChannel) key.channel();

		ByteBuffer buf = att.getByteBuffer();
		System.out.println("process: " + att.getProcessID());
		System.out.println("Position in read: " + buf.position());
		System.out.println("Limit in read: " + buf.limit());
		long bytesRead = 0;
//		try {
			// if (buf.position() == 0 && buf.limit() == 0) {
			// System.out.println("Bytes: " + bytesRead);
			// return;
			// }
			bytesRead = channel.read(buf);
			System.out.println("Bytes Read: " + bytesRead);
//		} catch (IOException e) {
//			key.cancel();
//			if (att.getOppositeKey() != null) {
//				att.getOppositeKey().cancel();
//				att.getOppositeChannel().close();
//			}
//			channel.close();
//			return;
//		}
		if (bytesRead == -1) { // Did the other end close?
			channel.close();
		} else if (bytesRead > 0) {
			// TODO: Hardcoding persistent connections. Fix this when the http
			if (att.getProcessID().equals(ProcessType.CLIENT)) {
				att.parseByteBuffer(buf);
				if (att.validRequest()) {
					if (att.requestFinished()) {
						SelectionKey oppositeKey;
						SocketChannel oppositeChannel;
						oppositeChannel = SocketChannel
								.open(new InetSocketAddress(att.getHost(), 80));
						oppositeChannel.configureBlocking(false);
						oppositeKey = oppositeChannel.register(key.selector(),
								SelectionKey.OP_WRITE);
						AttachmentOld serverAtt = new AttachmentOld(
								ProcessType.SERVER, this.bufSize);
						serverAtt.setOppositeKey(key);
						serverAtt.setOppositeChannel((SocketChannel) key
								.channel());
						oppositeKey.attach(serverAtt);
						att.setOppositeChannel(oppositeChannel);
						att.setOppositeKey(oppositeKey);
						// Indicate via key that reading/writing are both of
						// interest
						// now.
						// Attachment oppositeAtt = (Attachment)
						// oppositeKey.attachment();
						serverAtt.setByteBuffer(att.getTotalBuffer());
						// oppositeKey.interestOps(SelectionKey.OP_READ);
					} else {
						key.interestOps(SelectionKey.OP_READ);
					}
				} else {
//					HttpResponse.generateResponse(att.getStatusRequest());
				}
			} else if (att.getProcessID().equals(ProcessType.SERVER)) {
				if (att.getOppositeKey().isValid()) {
					att.getOppositeKey().interestOps(SelectionKey.OP_WRITE);
					((AttachmentOld) att.getOppositeKey().attachment())
							.setByteBuffer(buf);
				}
				// key.interestOps(SelectionKey.OP_READ);
			} else if (att.getProcessID().equals(ProcessType.ADMIN)) {
				String data = "<html><body><h1>Bahui la tenes adentro</h1></body></html>";
				String response = "HTTP/1.1 200 OK\nDate: Fri, 31 Dec 1999 23:59:59 GMT\nContent-Type: text/html\nContent-Length: "
						+ data.getBytes().length + "\n\n";
				String aux = response + data;
				ByteBuffer bufferResponse = ByteBuffer.allocate(aux.length());
				bufferResponse.put(aux.getBytes());
				bufferResponse.position(0);
				att.setByteBuffer(bufferResponse);
				key.interestOps(SelectionKey.OP_WRITE);
			}
		}
	}

	public void handleWrite(SelectionKey key) throws IOException {
		/*
		 * Channel is available for writing, and key is valid (i.e., client
		 * channel not closed).
		 */
		// Retrieve data read earlier
		AttachmentOld att = (AttachmentOld) key.attachment();
		SocketChannel channel = (SocketChannel) key.channel();

		ByteBuffer buf = att.getByteBuffer();
		buf.flip();
		System.out.println("Limit in write: " + buf.limit());
		System.out.println("Position in write: " + buf.position());
		System.out.println("Writing to " + att.getProcessID() + ": "
				+ new String(buf.array()));
		// Prepare buffer for writing
		do {
//			try {
				channel.write(buf);
//			} catch (IOException e) {
//				// channel.close();
//				e.printStackTrace();
//				System.out.println("Write exception");
//				return;
//			}
		} while (buf.hasRemaining());
		if (!buf.hasRemaining()) { // Buffer completely written?
			// Nothing left, so no longer interested in writes
			key.interestOps(SelectionKey.OP_READ);
		}
		buf.compact(); // Make room for more data to be read in
	}
}