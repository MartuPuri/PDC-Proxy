package itba.pdc.proxy;

import itba.pdc.proxy.lib.ManageByteBuffer;

import java.nio.ByteBuffer;

public class TextBuffer {
	public static void main(String[] args) {
		String a = "HTTP/1.0 200 OK" +
"Cache-Control: max-age=72000" +
"Content-Type: image/jpeg" +
"Last-Modified: Mon, 23 Jan 2012 11:37:49 GMT" +
"Accept-Ranges: bytes" +
"ETag: \"a1e296fc3d9cc1:0\"" +
"Server: Microsoft-IIS/7.5" +
"X-Powered-By: ASP.NET" +
"Date: Tue, 05 Nov 2013 21:08:12 GMT" +
"Content-Length: 102006" +
"X-Cache: MISS from thorium" +
"X-Cache-Lookup: HIT from thorium:3128" +
"Via: 1.1 thorium:3128 (squid/2.7.STABLE3)" +
"Connection: keep-alive" +
"Proxy-Connection: keep-alive" +
"" +
"���� JFIF  ";
		
		System.out.println("hola: " + a.length());
		ByteBuffer c = ByteBuffer.allocate(a.getBytes().length);
		c.put(a.getBytes());
		System.out.println("c: " + c);
	}
}
