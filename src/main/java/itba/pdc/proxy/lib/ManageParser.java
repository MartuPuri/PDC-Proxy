package itba.pdc.proxy.lib;

import itba.pdc.httpparser.HttpParserRequest;
import itba.pdc.httpparser.ParserCode;

import java.io.IOException;
import java.nio.ByteBuffer;

public final class ManageParser {

	private ManageParser() {
		throw new IllegalAccessError("This class cannot be instantiated");
	}

	public static ReadingState parseRequest(HttpParserRequest parser, ByteBuffer buff) {
		ParserCode code = ParserCode.INVALID;
		try {
			code = parser.parseMessage(buff);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return ReadingState.ERROR;
		}
		switch (code) {
		case INVALID:
			return ReadingState.ERROR;
		case VALID:
			return ReadingState.FINISHED;
		default:
			return ReadingState.UNFINISHED;
		}
	}
}
