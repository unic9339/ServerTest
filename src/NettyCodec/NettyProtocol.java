package NettyCodec;

import java.io.OutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

import io.netty.buffer.ByteBuf;

public class NettyProtocol {
	
	private Connection conn = null;
//	private Statement stmt = null;
	private PreparedStatement pstmt = null;
	private ResultSet rs = null;
	
	final static String url = "jdbc:mysql://localhost:3306/edget?serverTimezone=UTC";
    final static String user = "Kim";
    final static String password = "gokei";
	
	byte[] one;
    byte[] two;
    byte[] thr;
    byte[] fou;
    byte[] dest;
    int type = 0;
    ByteBuf respByteBuf;
	
	public ByteBuf NettyProtocol(Object msg) throws Exception{
		ByteBuf buf = (ByteBuf) msg;
		
		short magicNum = buf.readShort();
		System.out.println("buf>readShort(): " + magicNum);
		
		if(magicNum == 0x1234) {
			System.out.println( "received packet is about gw"); 
		}
		System.out.println("buf>readByte(): " + buf.readByte());
		
		type = buf.readByte();
	    System.out.println("msg_type: " + type);
	    short length = buf.readShort();     
	    System.out.println("body's length: " + length);
	    
	    byte[] reg = new byte[length];
	    buf.readBytes(reg);
	    
	    String body = new String(reg, "UTF-8");
        System.out.println( "server received body: " + body);
        
        if (type%2 == 1 && CheckGw(body)) {
        	switch (type) {
			case 1:
				
				break;
			case 3:
							
				break;
			case 5:
				
				break;
			case 7:
				
				break;
			case 9:
				break;
	

			default:
				break;
			}
        }else {
        	
		}
        
        return respByteBuf;
	}
	
	private Boolean CheckGw(String body) {
		boolean isExist = false;
		String sql= new String();
		
		try {
			conn = DriverManager.getConnection(url, user, password);
			
			sql = "select count(*) from gw where esn = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, body);
			
			rs = pstmt.executeQuery();
			
			dbQuery(conn, pstmt, rs, sql);
			isExist = true;
			
		}catch (SQLException ex) {
			ex.printStackTrace();
			isExist = false;
		}
		
		return isExist;
	}
	
	private static void dbQuery(Connection conn, PreparedStatement ptmt, ResultSet rs, String sql) {
		System.out.println("Sql: "+ sql);
		
		try {
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			
			JSONArray array = new JSONArray();
	
			while(rs.next()) {
				JSONObject jsonObj = new JSONObject();
				for(int i = 1; i<= columnCount; i++) {
					String columnName = metaData.getColumnLabel(i);
					String value = rs.getString(columnName);
					jsonObj.put(columnName, value);
				}
				array.put(jsonObj);
			}
			
			System.out.println("JSON Data : "+array.toString());
			System.out.println("\n-------------------------------------\n");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
