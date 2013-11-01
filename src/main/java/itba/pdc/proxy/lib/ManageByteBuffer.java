package itba.pdc.proxy.lib;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public final class ManageByteBuffer {
	private static Charset charset = Charset.forName("UTF-8");
	private static CharsetEncoder encoder = charset.newEncoder();
	private static CharsetDecoder decoder = charset.newDecoder();

	private ManageByteBuffer() {
		throw new IllegalAccessError("This class cannot be instantiated");
	}

	public static ByteBuffer encode(String message) {
		try {
			return encoder.encode(CharBuffer.wrap(message));
		} catch (Exception e) {
//			e.printStackTrace();
		}
		return null;
	}

	public static String decode(ByteBuffer buffer) {
		String data = "";
		try {
			int old_position = buffer.position();
			data = decoder.decode(buffer).toString();
			// reset buffer's position to its original so it is not altered:
			buffer.position(old_position);
		} catch (Exception e) {
//			e.printStackTrace();
			return "";
		}
		return data;
	}
}
