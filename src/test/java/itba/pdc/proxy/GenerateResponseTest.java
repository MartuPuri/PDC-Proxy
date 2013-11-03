package itba.pdc.proxy;

import itba.pdc.model.HttpResponse;
import itba.pdc.model.StatusRequest;

import java.io.File;
import java.io.IOException;

public class GenerateResponseTest {
	public static void main(String[] args) throws IOException {
	    File classpathRoot = new File("bad_request.html");
		System.out.println(classpathRoot.getCanonicalPath());
		System.out.println(HttpResponse.generateResponseError(StatusRequest.CONFLICT));
		System.out.println(HttpResponse.generateResponseError(StatusRequest.BAD_REQUEST));
	}
}
