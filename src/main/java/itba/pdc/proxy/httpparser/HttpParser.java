package itba.pdc.proxy.httpparser;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface HttpParser {
	public ParserCode parseMessage(ByteBuffer _buff) throws IOException;
	public String getState();
}
