package itba.pdc.proxy.lib;

import itba.pdc.proxy.data.Attachment;
import itba.pdc.proxy.data.ProcessType;
import itba.pdc.proxy.exceptions.InvalidBufferSizeException;
import itba.pdc.proxy.exceptions.InvalidDefaultPortException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class ConnectionManager {
	private static ConnectionManager instance = null;
	private Logger infoLogger = (Logger) LoggerFactory.getLogger("info.log");

	private ConnectionManager() throws FileNotFoundException, IOException {
		if (instance != null) {
			infoLogger
					.error("Instance of ReadProxyConfiguration already created");
			throw new IllegalArgumentException("Istance already created");
		}
	}

	public static synchronized ConnectionManager getInstance()
			throws FileNotFoundException, IOException {
		if (instance == null) {
			instance = new ConnectionManager();
		}
		return instance;
	}

	public void registerServerSocket(Selector selector) throws IOException {
		ReadProxyConfiguration proxyConfiguration = ReadProxyConfiguration
				.getInstance();
		ReadConstantsConfiguration constantsConfiguration = ReadConstantsConfiguration
				.getInstance();
		String host = proxyConfiguration.getServerIp();
		Integer port = proxyConfiguration.getServerPort();
		ServerSocketChannel listnChannel = ServerSocketChannel.open();
		if (port == null) {
			Integer defaultPort = constantsConfiguration.getServerDefaultPort();
			if (defaultPort == null) {
				throw new InvalidDefaultPortException();
			}
			port = defaultPort;
		}
		if (host == null) {
			infoLogger.info("The proxy will listen at my ip at port " + port);
			listnChannel.socket().bind(new InetSocketAddress(port));
		} else {
			infoLogger.info("The proxy will listen at " + host + " at port " + port);
			listnChannel.socket().bind(new InetSocketAddress(host, port));
		}
		listnChannel.configureBlocking(false);
		Integer bufferSize = constantsConfiguration.getBufferSize();
		if (bufferSize == null) {
			throw new InvalidBufferSizeException();
		}
		listnChannel.register(selector, SelectionKey.OP_ACCEPT, new Attachment(
				ProcessType.CLIENT, bufferSize));
	}

	public void registerAdminSocket(Selector selector) throws IOException {
		ReadProxyConfiguration proxyConfiguration = ReadProxyConfiguration
				.getInstance();
		ReadConstantsConfiguration constantsConfiguration = ReadConstantsConfiguration
				.getInstance();
		Integer port = proxyConfiguration.getAdminPort();
		ServerSocketChannel listnChannel = ServerSocketChannel.open();
		if (port == null) {
			Integer defaultPort = constantsConfiguration.getAdmingDefaultPort();
			if (defaultPort == null) {
				throw new InvalidDefaultPortException();
			}
			port = defaultPort;
		}
		infoLogger.info("The proxy will lister at my ip for admin clients at port " + port);
		listnChannel.socket().bind(new InetSocketAddress(port));
		listnChannel.configureBlocking(false);
		Integer bufferSize = constantsConfiguration.getBufferSize();
		if (bufferSize == null) {
			throw new InvalidBufferSizeException();
		}
		listnChannel.register(selector, SelectionKey.OP_ACCEPT, new Attachment(
				ProcessType.ADMIN, bufferSize));
	}
}
