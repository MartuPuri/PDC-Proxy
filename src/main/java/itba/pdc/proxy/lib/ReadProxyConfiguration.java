package itba.pdc.proxy.lib;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class ReadProxyConfiguration {
	private static ReadProxyConfiguration instance;
	private Logger infoLogger = (Logger) LoggerFactory.getLogger("info.log");
	private Properties prop;
	private Map<String, String> data;
	
	private ReadProxyConfiguration() throws FileNotFoundException, IOException {
		if (instance != null) {
			infoLogger.error("Instance of ReadProxyConfiguration already created");
			throw new IllegalArgumentException("Istance already created");
		}
		prop = new Properties();
		data = new HashMap<String,String>();
		prop.load(new FileInputStream("src/main/resources/proxy.properties"));
	}
	
	public static synchronized ReadProxyConfiguration getInstance() throws FileNotFoundException, IOException {
		if (instance == null) {
			instance = new ReadProxyConfiguration();
		}
		return instance;
	}
	
	public String getServerIp() {
		return this.getString("server-ip");
	}
	
	public String getChainedIp() {
		return this.getString("chained-ip");
	}
	
	public Integer getServerPort() {
		return this.getInteger("server-port");
	}
	
	public Integer getAdminPort() {
		return this.getInteger("admin-port");
	}
	
	public Integer getChainedPort() {
		return this.getInteger("chained-port");
	}
	
	private String getString(String s) {
		String str = data.get(s);
		if(str == null){
			str = prop.getProperty(s);
			if (str.isEmpty()) {
				return null;
//				throw new IllegalStateException("No esta seteado ese parametro en el .properties");
			}
			data.put(s, str);
		}
		return str;
	}
	
	private Integer getInteger(String s){
		String value = this.getString(s);
		if (value == null) {
			return null;
		}
		return Integer.parseInt(this.getString(s));
	}
}
