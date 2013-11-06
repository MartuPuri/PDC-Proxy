package itba.pdc.proxy.model;

import java.util.Map;

@Deprecated
public interface HttpMessage {
	
	public void addHeader(String header, String value);
	public void setVersion(int[] version);
	public void setMethod(String method);
	public void setParams(Map<String, String> params);
	public void setBody(String body);
	public void setUri(String uri);
	public boolean bodyEnable();
}
