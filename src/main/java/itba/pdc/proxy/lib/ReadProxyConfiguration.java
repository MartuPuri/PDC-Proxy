package itba.pdc.proxy.lib;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class ReadProxyConfiguration {
	private static ReadProxyConfiguration instance;
	private Logger infoLogger = (Logger) LoggerFactory.getLogger("info.log");
	private Properties prop;
	
	private ReadProxyConfiguration() throws FileNotFoundException, IOException {
		if (instance != null) {
			infoLogger.error("Instance of ReadProxyConfiguration already created");
			throw new IllegalArgumentException("Istance already created");
		}
		prop = new Properties();
		prop.load(new FileInputStream("src/main/resources/proxy.properties"));
	}
	
	public static synchronized ReadProxyConfiguration getInstance() throws FileNotFoundException, IOException {
		if (instance == null) {
			instance = new ReadProxyConfiguration();
		}
		return instance;
	}
	
	public String getServerIp() {
		String ip = prop.getProperty("server-ip");
		if (ip.isEmpty()) {
			return null;
		}
		return ip;
	}
	
	public Integer getServerPort() {
		String port = prop.getProperty("server-port");
		if (port.isEmpty()) {
			return null;
		}
		return Integer.parseInt(port);
	}
	
	public Integer getAdminPort() {
		String port = prop.getProperty("admin-port");
		if (port.isEmpty()) {
			return null;
		}
		return Integer.parseInt(port);
	}
	
}
