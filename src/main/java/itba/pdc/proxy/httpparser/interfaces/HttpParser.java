package itba.pdc.proxy.httpparser.interfaces;

import itba.pdc.proxy.httpparser.enums.ParserCode;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface HttpParser {
	public ParserCode parseMessage(ByteBuffer _buff) throws IOException;
	public String getState();
}
