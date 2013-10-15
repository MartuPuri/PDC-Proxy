package itba.pdc.proxy;

import itba.pdc.proxy.data.Attachment;
import itba.pdc.proxy.data.ProcessType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

public class TCPServerSelector {
    private static final int BUFSIZE = 256; // Buffer size (bytes)
    private static final int TIMEOUT = 3000; // Wait timeout (milliseconds)

    public static void main(String[] args) throws IOException {
        if (args.length < 1) { // Test for correct # of args
            throw new IllegalArgumentException("Parameter(s): <Port> ...");
        }
        // Create a selector to multiplex listening sockets and connections
        Selector selector = Selector.open();
        // Create listening socket channel for each port and register selector
        for (String arg : args) {
        	//The open static method creates an instance of SocketChannel.
            ServerSocketChannel listnChannel = ServerSocketChannel.open();
            //The connection to the server is made by the bind method.
            listnChannel.socket().bind(new InetSocketAddress(Integer.parseInt(arg)));
            //The configureBlocking(false) invocation sets the channel as nonblocking. 
            listnChannel.configureBlocking(false); // must be nonblocking to
                                                   // register
            // Register selector with channel. The returned key is ignored
            // The register method associates the selector to the socket channel.
            listnChannel.register(selector, SelectionKey.OP_ACCEPT, new Attachment(ProcessType.CLIENT));
            
        }
        // Create a handler that will implement the protocol
        TCPProtocol protocol = new HttpProtocol(BUFSIZE);
        while (true) { // Run forever, processing available I/O operations
            // Wait for some channel to be ready (or timeout)
        	//  the select method blocks the execution and waits for events recorded on the selector.
            if (selector.select(TIMEOUT) == 0) { // returns # of ready chans
                System.out.print(".");
                continue;
            }
            // Get iterator on set of keys with I/O to process
            Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
            while (keyIter.hasNext()) {
                SelectionKey key = keyIter.next(); // Key is bit mask
                // Server socket channel has pending connection requests?
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