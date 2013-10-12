package itba.pdc.proxy.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PersistentConnection {
	private Map<String, Attachment> connections;
	private static PersistentConnection instance = null;
	
	private PersistentConnection() {
		if (instance != null) {
			throw new IllegalStateException("Already instantied");
		}
	}
	
	public static PersistentConnection getInstance() {
		if (instance == null) {
			instance = new PersistentConnection();
			instance.connections = new HashMap<String, Attachment>();
		}
		return instance; 
	}
	
	public boolean addConnection(String _host, Attachment _att) {
		if (_host == null || _att == null) {
			return false;
		}
		if (this.connections.containsKey(_host)) {
			return false;
		}
		this.connections.put(_host, _att);
		return true;
	}
	
	public void closeConnection(String _host) throws IOException {
		if (_host != null) {
			Attachment att = this.connections.get(_host);
			if (att != null) {
				att.closeChannel();
			}
		}
	}
	
	public Attachment getConnection(String _host) {
		if (_host != null) {
			return connections.get(_host);
		}
		return null;
	}
	
}
