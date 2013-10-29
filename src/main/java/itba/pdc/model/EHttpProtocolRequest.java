package itba.pdc.model;

import java.util.HashSet;
import java.util.Set;

public class EHttpProtocolRequest extends HttpRequest {
	
	public EHttpProtocolRequest(){
		super();
		this.removeMethod("HEAD");
	}
	
	@Override
	public void setMethod(String method) {
		super.setMethod(method);
		this.setHeaders(this.createHeaders(method));
	}
	
	@Override
	public void addHeader(String header, String value){
		super.addHeader(header, value);
		if(header.equals("Activate") && value != "OFF" && value != "ON"){
			//TODO: ver como manejarlo
		}
	}
	
	Set<String> createHeaders(String method) {
		Set<String> headers = new HashSet<String>();
		if(method.equals("GET")){
			headers.add("Accept");
			headers.add("Authorization");
		}else{
			headers.add("Activate");
		}
		return headers;
	}
}
