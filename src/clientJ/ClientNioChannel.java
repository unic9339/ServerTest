package clientJ;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;

public class ClientNioChannel {
	final static String SERVER_IP ="127.0.0.1";
    final static int SERVER_PORT =1225;

	static Selector selector = null;
	private SocketChannel socketChannel = null;
	
	public static void main(String[] args) {
		new ClientNioChannel().run();
	}
	
	private void run() {
		try {
			startServer();
		}catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	
	public void startServer() throws IOException{
		initServer();
		Receive receive = new Receive();
		new Thread(receive).start();
	}
	
	private void initServer() throws IOException{
		selector = Selector.open();
		socketChannel = SocketChannel.open(new InetSocketAddress(SERVER_IP, SERVER_PORT));
		socketChannel.configureBlocking(false);
		socketChannel.register(selector, SelectionKey.OP_READ);
	}
	
	// send message to server
	private void startWriter() {
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
		try {
			while(true) {
				// TODO
				// insert TcpJsonClient source code
				
			}
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			clearBuffer(byteBuffer);
		}
	}
	
	static void clearBuffer(ByteBuffer buffer) {
		if(buffer != null)buffer.clear();
	}
}

class Receive implements Runnable{
	private CharsetDecoder decoder = null;
	
	@Override
	public void run() {
		Charset charset = Charset.forName("UTF-8");
		decoder = charset.newDecoder();
		
		try {
			while(true) {
				ClientNioChannel.selector.select();
				Iterator iterator = ClientNioChannel.selector.selectedKeys().iterator();
				
				while (iterator.hasNext()) {
					SelectionKey key = (SelectionKey)iterator.next();
					
					if (key.isReadable()) read(key);
					
					iterator.remove();
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void read(SelectionKey key) {
		SocketChannel socketChannel = (SocketChannel)key.channel();
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
		
		try {
			byteBuffer.flip();
			String data = decoder.decode(byteBuffer).toString();
			System.out.println("Receive Message - " + data);
            ClientNioChannel.clearBuffer(byteBuffer);
		}catch (IOException ex) {
			try {
				socketChannel.close();
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
