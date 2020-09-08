package serverPackage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class NioServer {

	private static final int PORT_NUM = 1225;
	
	private Selector selector;
	private Vector<SocketChannel> room = new Vector<SocketChannel>(100); 

    public static void main(String[] args) {
        new NioServer().run();
    }

    public void run() {
        ServerSocketChannel serverChannel = null;
        try {
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(PORT_NUM));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.printf("NonBlockingChannelEchoServer is running... port->%d\n", serverChannel.socket().getLocalPort());
            while (selector.select() > 0) {
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectedKeys.iterator();
                while (it.hasNext()) {
                    SelectionKey selectionKey = it.next();
                    if (selectionKey.isAcceptable()) {
                        doAccept((ServerSocketChannel) selectionKey.channel());
                    } else if (selectionKey.isReadable()) {
                        doRead((SocketChannel) selectionKey.channel());
                    }
                    it.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverChannel != null && serverChannel.isOpen()) {
                try {
                    System.out.printf("NonBlockingChannelEchoServer is stopping.\n");
                    serverChannel.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private void doAccept(ServerSocketChannel serverChannel) {
        try {
            SocketChannel channel = serverChannel.accept();
            String remoteAddress = channel.socket().getRemoteSocketAddress().toString();
            System.out.printf("%s [%s] :Accepted! remoteAddress -> %s\n", new Date(), Thread.currentThread().getName(), remoteAddress);
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
            
            room.add(channel);
            System.out.println("accept : " + SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doRead(SocketChannel channel) {
        ByteBuffer buf = ByteBuffer.allocate(PORT_NUM);
        Charset charset = Charset.forName("UTF-8");
        String remoteAddress = channel.socket().getRemoteSocketAddress().toString();
        try {
            if (channel.read(buf) < 0) {
                return;
            }
            buf.flip();
            System.out.print(remoteAddress + ":" + charset.decode(buf).toString());
            buf.flip();
            channel.write(buf);
            room.remove(channel);
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.printf("%s [%s] :Closed. remoteAddress -> %s\n", new Date(), Thread.currentThread().getName(), remoteAddress);
            try {
                channel.close();
            } catch (IOException e) {
            }
        }
    }
}
