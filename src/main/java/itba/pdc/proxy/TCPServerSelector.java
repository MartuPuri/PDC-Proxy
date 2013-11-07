package itba.pdc.proxy;

import itba.pdc.proxy.data.Attachment;
import itba.pdc.proxy.data.ProxyType;
import itba.pdc.proxy.handler.EHttpHandler;
import itba.pdc.proxy.handler.HttpHandler;
import itba.pdc.proxy.handler.TCPProtocol;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public class TCPServerSelector {
    private static final int BUFSIZE = 1500; // Buffer size (bytes)
    private static final int TIMEOUT = 3000; // Wait timeout (milliseconds)

    public static void main(String[] args) throws IOException {
        if (args.length != 0) { // Test for correct # of args
            throw new IllegalArgumentException("The application did not use params");
        }
        // Create a selector to multiplex listening sockets and connections
        Selector selector = Selector.open();
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        connectionManager.registerServerSocket(selector);
        connectionManager.registerAdminSocket(selector);
        // Create listening socket channel for each port and register selector
//        for (String arg : args) {
//            ServerSocketChannel listnChannel = ServerSocketChannel.open();
//            listnChannel.socket().bind(new InetSocketAddress(Integer.parseInt(arg)));
//            listnChannel.configureBlocking(false); // must be nonblocking to
//                                                   // register
//            // Register selector with channel. The returned key is ignored
//            listnChannel.register(selector, SelectionKey.OP_ACCEPT, new Attachment(ProcessType.CLIENT, BUFSIZE));
//            
//        }
        // Create a handler that will implement the protocol
        TCPProtocol http = new HttpHandler(BUFSIZE);
        TCPProtocol ehttp = new EHttpHandler(BUFSIZE);
        TCPProtocol protocol = http;
        while (true) { // Run forever, processing available I/O operations
            // Wait for some channel to be ready (or timeout)
            if (selector.select(TIMEOUT) == 0) { // returns # of ready chans
                System.out.print(".");
                continue;
            }
            // Get iterator on set of keys with I/O to process
            Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
            while (keyIter.hasNext()) {
                SelectionKey key = keyIter.next(); // Key is bit mask
                // Server socket channel has pending connection requests?
                Attachment att = (Attachment) key.attachment();
                ProxyType proxyType = att.getProxyType();
                switch (proxyType) {
				case ADMIN:
					protocol = ehttp;
					break;
				case PROXY:
					protocol = http;
				default:
					break;
				}
                if (!key.isValid()) {
					continue;
				}
                if (key.isAcceptable()) {
                    protocol.handleAccept(key);
                }
                // Client socket channel has pending data?
                if (key.isReadable()) {
                    protocol.handleRead(key);
                }
                // Client socket channel is available for writing and
                // key is valid (i.e., channel not closed)?
                if (key.isValid() && key.isWritable()) {
                    protocol.handleWrite(key);
                }
                keyIter.remove(); // remove from set of selected keys
            }
        }
    }
}