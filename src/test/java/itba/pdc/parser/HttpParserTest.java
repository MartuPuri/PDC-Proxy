package itba.pdc.parser;

import itba.pdc.httpparser.HttpParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Map.Entry;

public class HttpParserTest {
	private static Charset charset = Charset.forName("UTF-8");
	private static CharsetEncoder encoder = charset.newEncoder();
	private static CharsetDecoder decoder = charset.newDecoder();

	public static void main(String[] args) throws IOException {
		String first = "GET / HTTP/1.1\nHos";
		String second = "t: www.google.com\nPort: 80";
		String third = "80\n\nEsto es todo el dato que puedo enviar";
		String fourth = " siempre y cuando no sea nada";
		String fifth = " malo. Muchas gracias\n\n";
		ByteBuffer buf = str_to_bb(first);
		System.out.println("STRING:" + buf.capacity());
		HttpParser parser = new HttpParser(buf);
		parser.parse();
		parser.pushByteBuffer(str_to_bb(second));
		parser.parse();
		parser.pushByteBuffer(str_to_bb(third));
		parser.parse();
		parser.pushByteBuffer(str_to_bb(fourth));
		parser.parse();
		parser.pushByteBuffer(str_to_bb(fifth));
		parser.parse();
		System.out.println(parser.getState());
		for (Entry<String, String> entry : parser.getHeaders().entrySet()) {
			System.out.println("key: " + entry.getKey());
			System.out.println("key: " + entry.getValue());
		};
	}

	public static ByteBuffer str_to_bb(String msg) {
		try {
			return encoder.encode(CharBuffer.wrap(msg));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String bb_to_str(ByteBuffer buffer) {
		String data = "";
		try {
			int old_position = buffer.position();
			data = decoder.decode(buffer).toString();
			// reset buffer's position to its original so it is not altered:
			buffer.position(old_position);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		return data;
	}
}
