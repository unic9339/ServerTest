package serverPackage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolServer {
	
	private static final int PORT_NUM = 1225;

	public static void main(String[] args) {
		ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 100, 300, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>(100));
		
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(PORT_NUM);
			
			System.out.println("EchoServer (port="+serverSocket.getLocalPort()+")");
			while(true) {
				Socket socket = serverSocket.accept();
				executor.submit(new EchoTask(socket));
			}
		}catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if (serverSocket != null) {
					serverSocket.close();
				}
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
