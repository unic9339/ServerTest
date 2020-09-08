package clientJ;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TcpClient {

	final static String SERVER_IP ="127.0.0.1";
    final static int SERVER_PORT =1225;
    static String Name ="";
     
    public static void main(String[] args) {
         
        Socket socket =null;
        Scanner sc = new Scanner(System.in);
        
        int selectTable; 
        
        try {
            /** 소켓통신 시작 */
            socket =new Socket(SERVER_IP,SERVER_PORT);
            System.out.print("insert Name :  ");
            Name = sc.nextLine();
    
            /** Client에서 Server로 보내기 위한 통로 */
            OutputStream os = socket.getOutputStream();
            /** Server에서 보낸 값을 받기 위한 통로 */
            InputStream is = socket.getInputStream();
     
            os.write( Name.getBytes() );
            os.flush();
             
            byte[] data =new byte[16];
            int n = is.read(data);
            final String resultFromServer =new String(data,0,n);
             
            System.out.println(resultFromServer);

            while (true) {
            	System.out.println("select modbus id");
            	System.out.println("0 = exit");
            	selectTable = sc.nextInt();
            	
            	if(selectTable == 0) {
            		System.out.println("close client");
            		socket.close();
            		sc.close();
            		break;
            	}else {
            		os.write(selectTable);
            		os.flush();
				}
            }
             
        }catch (UnknownHostException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        } finally {
			try {
				if (socket != null && !socket.isClosed()) {
		            socket.close();
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
			sc.close();
		}
         
    }
}
