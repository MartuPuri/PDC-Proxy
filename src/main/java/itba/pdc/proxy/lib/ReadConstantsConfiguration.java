package itba.pdc.proxy.lib;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class ReadConstantsConfiguration {
	private static ReadConstantsConfiguration instance;
	private Logger infoLogger = (Logger) LoggerFactory.getLogger("info.log");
	private Properties prop;
	
	private ReadConstantsConfiguration() throws FileNotFoundException, IOException {
		if (instance != null) {
			infoLogger.error("Instance of ReadProxyConfiguration already created");
			throw new IllegalArgumentException("Istance already created");
		}
		prop = new Properties();
		prop.load(new FileInputStream("src/main/resources/constants.properties"));
	}
	
	public static synchronized ReadConstantsConfiguration getInstance() throws FileNotFoundException, IOException {
		if (instance == null) {
			instance = new ReadConstantsConfiguration();
		}
		return instance;
	}
	
	public Integer getBufferSize() {
		String bufferSize = (String) prop.get("buffer-size");
		if (bufferSize.isEmpty()) {
			infoLogger.error("The buffer size can not be empty");
			return null;
		}
		try {
			Integer size = Integer.parseInt(bufferSize);
			if (size <= 0) {
				infoLogger.error("The buffer size must be a number greater than 0");
				return null;
			}
			return size;
		} catch (NumberFormatException e) {
			infoLogger.error("The buffer size must be a number");
			return null;
		}
	}
	
	public Integer getServerDefaultPort() {
		String defaultPort = (String) prop.get("server-default-port");
		if (defaultPort.isEmpty()) {
			infoLogger.error("The default port can not be empty");
			return null;
		}
		try {
			Integer port = Integer.parseInt(defaultPort);
			if (port <= 1024) {
				infoLogger.error("The default must be greater than 1024");
				return null;
			}
			return port;
		} catch (NumberFormatException e) {
			infoLogger.error("The default port must be a number");
			return null;
		}
	}
	
	public Integer getAdmingDefaultPort() {
		String defaultPort = (String) prop.get("admin-default-port");
		if (defaultPort.isEmpty()) {
			infoLogger.error("The default port can not be empty");
			return null;
		}
		try {
			Integer port = Integer.parseInt(defaultPort);
			if (port <= 1024) {
				infoLogger.error("The default must be greater than 1024");
				return null;
			}
			return port;
		} catch (NumberFormatException e) {
			infoLogger.error("The default port must be a number");
			return null;
		}
	}
	
}
