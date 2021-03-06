package itba.pdc.proxy;

import java.nio.ByteBuffer;

import javax.management.RuntimeErrorException;

public class ReadByteBuffer {
	public static void main(String[] args) {
			String line = "HTTP/1.0 200 OK\r\n" +
					"Cache-Control: max-age=72000\r\n" +
					"\r\n" +
					"HO  ";
			System.out.println(line.length());
			System.out.println(line.getBytes().length);
			ByteBuffer buffer = ByteBuffer.allocate(line.getBytes().length);
			buffer.put(line.getBytes());
			String text = readLine(buffer);
			while (!text.trim().equals("")) {
				System.out.println("Line: " + text);
				text = readLine(buffer);
			}
			System.out.println(readLine(buffer));
			
	}

	public static String readLine(ByteBuffer buffer) {
		boolean lf = false;
		byte[] array = new byte[buffer.limit()];
		int i = 0;
		byte b;
		buffer.flip();
		do {
			b = buffer.get();
			array[i++] = b;
			if (b == 13) {
				lf = true;
			}
		} while (buffer.hasRemaining() && !lf);
		if (lf) {
			b = buffer.get();
			if (b != 10) {
				throw new RuntimeErrorException(null);
			}
			array[i] = b;
			buffer.compact();
			int position = buffer.position();
			buffer.limit(position);
			return new String(array);
		} else {
			return new String(array);
		}

	}
}
