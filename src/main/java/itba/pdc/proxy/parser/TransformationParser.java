package itba.pdc.proxy.parser;

import java.nio.ByteBuffer;


public class TransformationParser {
	
	public static void main(String[] args) {
		TransformationParser mp = new TransformationParser();
		ByteBuffer testBuf = ByteBuffer.allocate(1024);
		String s_multi = "--boundary42\r\n" +
				"Content-Type: text/plain; charset=us-ascii\r\n" +
				"\r\n" +
				"...plain text version of message goes here....\r\n" +
				"--boundary42\r\n" +
				"Content-Type: text/richtext\r\n" +
				"\r\n" +
				".... richtext version of same message goes here ...\r\n" +
				"--boundary42\r\n" +
				"Content-Type: text/x-whatever\r\n" +
				"\r\n" +
				".... fanciest formatted version of same  message  goes  here..\r\n" +
				"...\r\n" +
				"--boundary42--\r\n";
		testBuf.put(s_multi.getBytes());
	
		mp.transform(testBuf);
		System.out.println(new String(testBuf.array()));
	}
		
	public void transform(ByteBuffer buffer) {
		byte b;
		
		buffer.flip();
		for(int i = 0; i < buffer.limit(); i++) {
			b = buffer.get(i);
			switch(b) {
			case 97: buffer.put(i, (byte) 52); break;
			case 101: buffer.put(i, (byte) 51); break;
			case 105: buffer.put(i, (byte) 49); break;
			case 111: buffer.put(i, (byte) 48); break;
			case 99: buffer.put(i, (byte) 60); break;
			}
		}
		
	}
	
	
//	private String boundary;
//	// Deque<String> stack = new LinkedList<String>();
//	private boolean inPlainText = false;
//	private ParserMultipart state = ParserMultipart.BOUNDARY;
//	private ByteBuffer totalBuffer;
//	
//	public static void main(String[] args) {
//		MultipartParser mp = new MultipartParser();
//		ByteBuffer testBuf = ByteBuffer.allocate(1024);
//		String s_multi = "--boundary42\r\n" +
//				"Content-Type: text/plain; charset=us-ascii\r\n" +
//				"\r\n" +
//				"...plain text version of message goes here....\r\n" +
//				"--boundary42\r\n" +
//				"Content-Type: text/richtext\r\n" +
//				"\r\n" +
//				".... richtext version of same message goes here ...\r\n" +
//				"--boundary42\r\n" +
//				"Content-Type: text/x-whatever\r\n" +
//				"\r\n" +
//				".... fanciest formatted version of same  message  goes  here..\r\n" +
//				"...\r\n" +
//				"--boundary42--\r\n";
//		testBuf.put(s_multi.getBytes());
//		mp.parseBody(testBuf, "--boundary42");
//		
//		System.out.println(new String(mp.totalBuffer.array()));
//	}

//	public enum ParserMultipart {
//		BOUNDARY, HEADERS, BODY
//	}

//	public void parseBody(ByteBuffer buffer, String boundary) {
//		String line = null;
//		buffer.flip();
//		this.totalBuffer = ByteBuffer.allocate(buffer.capacity());
//		while (buffer.hasRemaining()) {
//			switch (state) {
//			case BOUNDARY:
//				// TODO Cambiarlo que se llame a ManageBuffer con readline()
//				boundary = readLine(buffer);
//				this.boundary = boundary;
//				boundary = boundary + "\r\n";
//				this.state = ParserMultipart.HEADERS;
//				totalBuffer.put(boundary.getBytes());
//				break;
//			case HEADERS:
//				line = readLine(buffer);
//				if (line.equals("")) {
//					this.state = ParserMultipart.BODY;
//				} else if (line.contains("text/plain")) {
//					inPlainText = true;
//				}
//				line = line + "\r\n";
//				totalBuffer.put(line.getBytes());
//				break;
//			case BODY:
//				line = readLine(buffer);
//				if (line.contains(this.boundary)) {
//					this.state = ParserMultipart.BOUNDARY;
//					line = "\r\n" + line + "\r\n";
//				}
//				if (inPlainText) {
//					line = line.replace("a", "4");
//					line = line.replace("e", "3");
//					line = line.replace("i", "1");
//					line = line.replace("o", "0");
//					line = line.replace("c", "<");
//				}
//				totalBuffer.put(line.getBytes());
//				break;
//			}
//		}
//	}
//
//	private String readLine(ByteBuffer buffer) {
//		boolean lf = false;
//		byte[] array = new byte[buffer.limit()];
//		int i = 0;
//		byte b;
//		do {
//			b = buffer.get();
//			array[i++] = b;
//			if (b == 13) {
//				lf = true;
//			} // TODO: Enter check 10
//		} while (buffer.hasRemaining() && !lf);
//		if (lf) {
//			b = buffer.get();
//			if (b != 10) {
//				throw new RuntimeErrorException(null);
//			}
//			array[i] = b;
//			return new String(array).trim();
//		} else {
//			return null;
//		}
//	}
}
