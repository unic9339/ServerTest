package clientJ;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.Scanner;

public class ClientTask implements Runnable{

	private Socket socket;
	private Scanner sc = new Scanner(System.in);
	
	private Connection conn = null;
	private Statement stmt = null;
	private PreparedStatement pstmt = null;
	private ResultSet rs = null;
	
	final static String url = "jdbc:mysql://localhost:3306/edget?serverTimezone=UTC";
    final static String user = "Kim";
    final static String password = "gokei";
    
    static String testGw = "open107vstm32f107vct6";
    
    static String SERVER_IP ="";
    static int SERVER_PORT = 0;
    static int gwMode = 1;
    
    static String gwst_gw = "gwst_gw";          // quering a gw dada by esn
    static String gwst_delist = "gwst_delist";	// quering a list of device by gw_esn
    static String gwst_logger = "gwst_logger";	// quering logger data by gw_esn
    static String gwst_mblist = "gwst_mblist";	// quering alist of modbus_id
    static String gwst_mbct = "gwst_mbct";	// quering count of modbus_id
    static String gwst_mbid = "gwst_mbid";	// quering modbus i/f data by modbus_id
    static String gwst_mbidct = "gwst_mbidct";	// quering length of modbus i/f data by modbus_id
    static String gwed = "_gwed";  //end symbal
    
	public ClientTask(Socket socket, String serverIp, int port) {
		this.socket = socket;
		ClientTask.SERVER_IP = serverIp;
		ClientTask.SERVER_PORT = port;
		
		System.out.printf("%s [%s] :Accepted!\n", new Date(), Thread.currentThread().getName());
	}
	
	@Override
	public void run() {
		try {
			socket = new Socket(SERVER_IP, SERVER_PORT);
			// Client to Server gate
			OutputStream os = socket.getOutputStream();
			// Server to Client gate
			InputStream is = socket.getInputStream();
			
			System.out.println("exit -> 0");
			System.out.println("gwst_gw -> 1");
			System.out.println("gwst_delist -> 2");
			System.out.println("gwst_logger -> 3");
			System.out.println("gwst_mblist -> 4");
			System.out.println("gwst_mbct -> 5");
			System.out.println("gwst_mbid -> 6");
			System.out.println("gwst_mbidct -> 7");
			System.out.println("insert mode number : ");
			
			gwMode = sc.nextInt();
			
			String gwesn = new String();
			String gwesn2 = new String();
			gwesn2 = testGw;
			
			switch (gwMode) {
			case 1:
				gwesn = gwst_gw + gwesn2 + gwed;
				System.out.println(gwesn+"\n");
				os.write(gwesn.getBytes());	
				break;
			case 2:
				gwesn = gwst_delist + gwesn2 + gwed;
				System.out.println(gwesn+"\n");
				os.write(gwesn.getBytes());	
				break;
			case 3:
				gwesn = gwst_logger + gwesn2 + gwed;
				System.out.println(gwesn+"\n");
				os.write(gwesn.getBytes());	
				break;
			case 4:
				gwesn = gwst_mblist + gwesn2 + gwed;
				System.out.println(gwesn+"\n");
				os.write(gwesn.getBytes());	
				break;
			case 5:
				gwesn = gwst_mbct + gwesn2 + gwed;
				System.out.println(gwesn+"\n");
				os.write(gwesn.getBytes());	
				break;
			case 6:
				gwesn = gwst_mbid + gwesn2 + gwed;
				System.out.println(gwesn+"\n");
				os.write(gwesn.getBytes());	
				break;
			case 7:
				gwesn = gwst_mbidct + gwesn2 + gwed;
				System.out.println(gwesn+"\n");
				os.write(gwesn.getBytes());	
				break;
			default:
				gwesn = gwst_gw + gwesn2 + gwed;
				System.out.println(gwesn+"\n");
				os.write(gwesn.getBytes());	
				break;
			} 
//			is = socket.getInputStream();
//			String line = "";
//			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
//			String readLine = "";
//			
//			while ((readLine = in.readLine()) != null) {
//				line+=readLine;
//			}
//			System.out.println("line : "+line);
			
		}catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if (socket != null && !socket.isClosed()) {
					socket.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
