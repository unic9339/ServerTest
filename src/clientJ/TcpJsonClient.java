package clientJ;

import java.net.Socket;

public class TcpJsonClient {

	final static String SERVER_IP ="127.0.0.1";
    final static int SERVER_PORT =1225;
    
	public static void main(String[] args) {
		
		Socket socket = null;
		
		new ClientTask(socket, SERVER_IP, SERVER_PORT).run();

	}
}
