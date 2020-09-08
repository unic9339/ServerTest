package serverJ;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class TcpServerTest extends Thread{

	final static int SERVER_PORT =1225;
    final static String MESSAGE_TO_SERVER ="Connected";
    static String TABLE_NAME = "";
     
    public static void main(String[] args) {
         
        ServerSocket serverSocket =null;
         
        try {
            serverSocket =new ServerSocket(SERVER_PORT);
             
             
        }catch (IOException e) {
            e.printStackTrace();
        }
         
        
        try {
            while (true) {
                System.out.println("socket connecting..(port=" + serverSocket.getLocalPort() + "");
                Socket socket = serverSocket.accept();
                System.out.println("host : "+socket.getInetAddress()+" | connecting success");
             
                /** Server에서 보낸 값을 받기 위한 통로 */
                InputStream is = socket.getInputStream();
                /** Server에서 Client로 보내기 위한 통로 */
                OutputStream os = socket.getOutputStream();
          
                 
                byte[] data =new byte[16];
                int n = is.read(data);
                final String messageFromClient =new String(data,0,n);
                
                System.out.println(messageFromClient + " is connected");
                
                
                os.write( MESSAGE_TO_SERVER.getBytes() );
                os.flush();              
                
                if("exit".equals(TABLE_NAME)) {
                	System.out.println("close Server to client");
                }else {
//                	os.write( TABLE_NAME.getBytes() );
//                    os.flush();
                    System.out.println("id = "+TABLE_NAME);
//                	n = is.read(data);
//                	TABLE_NAME = new String(data, 0, n);
                 	new SetServer(socket,TABLE_NAME).start();
                }
            }
             
        }catch (IOException e) {
            e.printStackTrace();
        }
         
    }
}

class SetServer extends Thread{

	private Socket socket;
	
	String tableName = "";

	Connection conn = null;
	Statement stmt = null;
	PreparedStatement ps = null;
	ResultSet rs = null;
	
	@Override
	public void run() {
		try {
			setDbca();
		}catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	
	public SetServer(Socket socket,String name) {
		this.socket = socket;
		tableName = name;
		System.out.println(tableName);
	}
	
	public void setDbca() {
		String url = "jdbc:mysql://localhost:3306/edget?serverTimezone=UTC";
		String user = "Kim";
		String password = "gokei";	
		
		String selectSql = "SELECT * from modbus where id = ?";
		
		try {
			conn = DriverManager.getConnection(url, user, password);
			System.out.println("Success into DB");

            ps = conn.prepareStatement(selectSql);
			ps.setObject(1, tableName);
			rs = ps.executeQuery();
			while(rs.next()) {
				String id = rs.getString("id");
				System.out.println("id = "+id);
				
				String modbus_id = rs.getString("modbus_id");
				System.out.println("modbus_id = "+modbus_id);
				
				String name = rs.getString("name");
				System.out.println("name = "+name);
				
				String type = rs.getString("type");	
				System.out.println("type = "+type);
				
				String gain = rs.getString("gain");
				System.out.println("gain = "+gain);
				
				String reg_addr = rs.getString("reg_addr");
				System.out.println("reg_addr = "+reg_addr);
				
				String num = rs.getString("num");
				System.out.println("num = "+num);
			}
			ps.close();

		}catch (SQLException e) {
			System.out.println("DB Load Fail " + e.toString());
		}
	}
}

class SocketRun implements Runnable{
	
	private Socket socket = null;
	SocketRun(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
}

