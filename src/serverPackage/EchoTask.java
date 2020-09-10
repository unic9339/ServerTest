package serverPackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
//import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class EchoTask implements Runnable{
	
	private Socket socket;
	
	private Connection conn = null;
//	private Statement stmt = null;
	private PreparedStatement pstmt = null;
	private ResultSet rs = null;
	
	final static String url = "jdbc:mysql://localhost:3306/edget?serverTimezone=UTC";
    final static String user = "Kim";
    final static String password = "gokei";
    
    static String gwst_gw = "gwst_gw";          // quering a gw dada by esn
    static String gwst_delist = "gwst_delist";	// quering a list of device by gw_esn
    static String gwst_logger = "gwst_logger";	// quering logger data by gw_esn
    static String gwst_mblist = "gwst_mblist";	// quering alist of modbus_id
    static String gwst_mbct = "gwst_mbct";	// quering count of modbus_id
    static String gwst_mbid = "gwst_mbid";	// quering modbus i/f data by modbus_id
    static String gwst_mbidct = "gwst_mbidct";	// quering length of modbus i/f data by modbus_id
    static String gwed = "_gwed";  //end symbal
    
    public EchoTask(Socket socket) {
		this.socket = socket;
		System.out.printf("%s [%s] :Accepted!\n", new Date(), Thread.currentThread().getName());
	}

	@Override
	public void run() {
		
		try {
			conn = DriverManager.getConnection(url, user,password);
			// after connecting，get inputStream from socket, and establish buffer for reading
			InputStream inputStream = socket.getInputStream();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			OutputStream out = socket.getOutputStream();
			
			String line = "";
			String readLine = "";
			String gwesn = new String();
			String gwesn2 = new String();
			
			while ((readLine = in.readLine()) != null) {
				line+=readLine;
			}
			String sql= new String();
			String keystart, keyend;
			System.out.println("read line: " + line + "\n");
			
			if (line.contains(gwst_gw) && line.contains(gwed)) {
				keystart = gwst_gw;
			    keyend = gwed;
		    	//System.out.println("gw matched, line length: " + line.length() + "\n");
		    	int index1 = line.indexOf(gwst_gw);
		    	int index2 = line.indexOf(gwed);
		    	gwesn = line;
		    	gwesn2 = gwesn.substring(index1+7, index2);
		    	/*
		    	if(gwesn.equals(gwesn2)) {
			    	System.out.println("gwesn == gwesn2\n");
		    	}else {
			    	System.out.println("gwesn <> gwesn2\n");
		    	} */

		    	//System.out.println("gwesn matched: " + gwesn +"\n");
		    	System.out.println("gwesn2 define: " + gwesn2 +"\n");
	            sql = "select esn, plant_id, name, smlogger_esn, sam_t, ser_id1, ser_id2, user_id from gw where esn =?";
	            pstmt = conn.prepareStatement(sql);
	            pstmt.setString(1, gwesn2);
	            System.out.println(pstmt);
	            rs = pstmt.executeQuery();
	            dbQuery(socket, conn, pstmt, rs, line, sql, keystart, keyend);
			} else if (line.contains(gwst_delist) && line.contains(gwed)) {
				keystart = gwst_delist;
				keyend = gwed;
			    //System.out.println("devices matched, line length: " + line.length() + "\n");
			    int index1 = line.indexOf(gwst_delist);
			   	int index2 = line.indexOf(gwed);
		    	gwesn = line;
		    	gwesn = gwesn.substring(index1+11, index2);
		    	/*
		    	if(gwesn.equals(gwesn2)) {
			    	System.out.println("gwesn == gwesn2\n");
		    	}else {
			    	System.out.println("gwesn <> gwesn2\n");
		    	} */
		    	//System.out.println("gwesn matched: " + gwesn +"\n");
		    	System.out.println("gwesn2 define: " + gwesn2 +"\n");
	            sql = "select unit_id, modbus_id, gw_esn from pcs where gw_esn =?";
	            pstmt = conn.prepareStatement(sql);
	            pstmt.setString(1, gwesn);
	            rs = pstmt.executeQuery();
	            dbQuery(socket, conn, pstmt, rs, line, sql, keystart, keyend);
		  } else if (line.contains(gwst_logger) && line.contains(gwed)) {
				keystart = gwst_logger;
				keyend = gwed;
			    //System.out.println("smlogger matched, line length: " + line.length() + "\n");
			    int index1 = line.indexOf(gwst_logger);
			   	int index2 = line.indexOf(gwed);
		    	gwesn = line;
		    	gwesn = gwesn.substring(index1+11, index2);
		    	/*
		    	if(gwesn.equals(gwesn2)) {
			    	System.out.println("gwesn == gwesn2\n");
		    	}else {
			    	System.out.println("gwesn <> gwesn2\n");
		    	} */
		    	//System.out.println("gwesn matched: " + gwesn +"\n");
		    	System.out.println("gwesn2 define: " + gwesn2 +"\n");
		    	sql = "select esn, modbus_id, name, ip_addr, mtcp_port from smlogger where gw_esn =?";	            pstmt = conn.prepareStatement(sql);
	            pstmt.setString(1, gwesn);
	            rs = pstmt.executeQuery();
	            dbQuery(socket, conn, pstmt, rs, line, sql, keystart, keyend);
		  } else if(line.contains(gwst_mbct) && line.contains(gwed)) {
			    keystart = gwst_mbct;
			    keyend = gwed;
			    //System.out.println("gw mbcout matched, line length: " + line.length() + "\n");
			    int index1 = line.indexOf(gwst_mbct);
			   	int index2 = line.indexOf(gwed);
		    	gwesn = line;
		    	gwesn = gwesn.substring(index1+9, index2);
		    	/*
		    	if(gwesn.equals(gwesn2)) {
			    	System.out.println("gwesn == gwesn2\n");
		    	}else {
			    	System.out.println("gwesn <> gwesn2\n");
		    	} */
		    	//System.out.println("gwesn matched: " + gwesn +"\n");
		    	System.out.println("gwesn2 define: " + gwesn2 +"\n");
	            sql = "select count(distinct modbus_id) from pcs where gw_esn =?";
	            pstmt = conn.prepareStatement(sql);
	            pstmt.setString(1, gwesn);
	            rs = pstmt.executeQuery();
	            dbQuery(socket, conn, pstmt, rs, line, sql, keystart, keyend);
		  } else if(line.contains(gwst_mblist)  && line.contains(gwed)) {
			    keystart = gwst_mblist;
			    keyend = gwed;
			    //System.out.println("gw mb list matched, line length: " + line.length() + "\n");
			    int index1 = line.indexOf(gwst_mblist);
			   	int index2 = line.indexOf(gwed);
		    	gwesn = line;
		    	gwesn = gwesn.substring(index1+11, index2);
		    	/*
		    	if(gwesn.equals(gwesn2)) {
			    	System.out.println("gwesn == gwesn2\n");
		    	}else {
			    	System.out.println("gwesn <> gwesn2\n");
		    	} */
		    	//System.out.println("gwesn matched: " + gwesn +"\n");
		    	System.out.println("gwesn2 define: " + gwesn2 +"\n");
	            sql = "select distinct modbus_id from pcs where gw_esn =?";
	            pstmt = conn.prepareStatement(sql);
	            pstmt.setString(1, gwesn);
	            rs = pstmt.executeQuery();
	            dbQuery(socket, conn, pstmt, rs, line, sql, keystart, keyend);
		  } else if(line.contains("modbus_id")) {
			    keystart = "";
			    keyend = "";
		    	System.out.println("modbus_id matched\n");
	            sql = "select modbus_id, count(modbus_id) from modbus";
	            pstmt = conn.prepareStatement(sql);
	            rs = pstmt.executeQuery();
	            dbQuery(socket, conn, pstmt, rs, line, sql, keystart, keyend);
		  } else if (line.contains(gwst_mbid) && line.contains(gwed)) {
				keystart = gwst_mbid;
				keyend = gwed;
			    //System.out.println("modbus1 matched, line length: " + line.length() + "\n");
			    int index1 = line.indexOf(gwst_mbid);
			   	int index2 = line.indexOf(gwed);
		    	gwesn = line;
		    	gwesn = gwesn.substring(index1+9, index2);
		    	/*
		    	if(gwesn.equals(gwesn2)) {
			    	System.out.println("gwesn == gwesn2\n");
		    	}else {
			    	System.out.println("gwesn <> gwesn2\n");
		    	} */
		    	//System.out.println("gwesn matched: " + gwesn +"\n");
		    	System.out.println("gwesn2 define: " + gwesn2 +"\n");
	            sql = "select modbus_id, name, type, gain, reg_addr, num from modbus where modbus_id = ?";
	            pstmt = conn.prepareStatement(sql);
	            pstmt.setString(1, gwesn);
	            rs = pstmt.executeQuery();
	            dbQuery(socket, conn, pstmt, rs, line, sql, keystart, keyend);
		  } else if (line.contains("mb2esnst_") && line.contains("_mb2esned")) {
				keystart = "mb2esnst_";
				keyend = "_mb2esned";
			    //System.out.println("modbus2 matched, line length: " + line.length() + "\n");
			    int index1 = line.indexOf("mb2esnst_");
			   	int index2 = line.indexOf("_mb2esned");
		    	gwesn = line;
		    	gwesn = gwesn.substring(index1+9, index2);
		    	/*
		    	if(gwesn.equals(gwesn2)) {
			    	System.out.println("gwesn == gwesn2\n");
		    	}else {
			    	System.out.println("gwesn <> gwesn2\n");
		    	} */
		    	//System.out.println("gwesn matched: " + gwesn +"\n");
		    	System.out.println("gwesn2 define: " + gwesn2 +"\n");
	            sql = "select A.modbus_id, B.name, B.type, B.gain, B.reg_addr, B.num from pcs as A left join modbus as B on A.modbus_id = B.modbus_id where A.gw_esn = ? and A.modbus_id = ?";
	            pstmt = conn.prepareStatement(sql);
	            pstmt.setString(1, gwesn);
	            pstmt.setString(2, "hw02");
	            rs = pstmt.executeQuery();
	            dbQuery(socket, conn, pstmt, rs, line, sql, keystart, keyend);
		  } else {
		    	System.out.println("power json data are matched.\n");
				JSONObject obj = new JSONObject(line);
				Map<String, Object> map = new HashMap<>();
				for (Object key : obj.keySet()) {
					map.put((String) key, obj.get((String) key));
				}
				//System.out.println(map);

				sql = "INSERT INTO power_history(time, logger_seri_no, inv_esn_no, upv1, upv2, upv3, upv4,"
					      + "upv5, upv6, upv7, upv8, ipv1, ipv2, ipv3, ipv4, ipv5, ipv6, ipv7, ipv8, uac1, uac2,"
					      + "uac3, iac1, iac2, iac3, status, error, temp, cos, fac, pac, qac, eac, cycle_time)"
					      + "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
					      + "?, ?, ?, ?, ?, ?, ?, ?)";

                String Time1 = (String)map.get("Time");
			    //System.out.println(Time1);
			    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	            LocalDateTime Time2 = LocalDateTime.parse(Time1, formatter);

	            //System.out.println(Time2);

                pstmt = conn.prepareStatement(sql);
                //pstmt.setObject(1, map.get("Time"));
	            pstmt.setObject(1, Time2);
                pstmt.setObject(2, map.get("Logger_seri_no"));
                pstmt.setObject(3, map.get("Inv_sn"));
                pstmt.setFloat(4, Float.valueOf(map.get("Upv1").toString()).floatValue());
                pstmt.setFloat(5, Float.valueOf(map.get("Upv2").toString()).floatValue());
                pstmt.setFloat(6, Float.valueOf(map.get("Upv3").toString()).floatValue());
                pstmt.setFloat(7, Float.valueOf(map.get("Upv4").toString()).floatValue());
                pstmt.setFloat(8, Float.valueOf(map.get("Upv5").toString()).floatValue());
                pstmt.setFloat(9, Float.valueOf(map.get("Upv6").toString()).floatValue());
                pstmt.setFloat(10, Float.valueOf(map.get("Upv7").toString()).floatValue());
                pstmt.setFloat(11, Float.valueOf(map.get("Upv8").toString()).floatValue());
                pstmt.setFloat(12, Float.valueOf(map.get("Ipv1").toString()).floatValue());
                pstmt.setFloat(13, Float.valueOf(map.get("Ipv2").toString()).floatValue());
                pstmt.setFloat(14, Float.valueOf(map.get("Ipv3").toString()).floatValue());
                pstmt.setFloat(15, Float.valueOf(map.get("Ipv4").toString()).floatValue());
                pstmt.setFloat(16, Float.valueOf(map.get("Ipv5").toString()).floatValue());
                pstmt.setFloat(17, Float.valueOf(map.get("Ipv6").toString()).floatValue());
                pstmt.setFloat(18, Float.valueOf(map.get("Ipv7").toString()).floatValue());
                pstmt.setFloat(19, Float.valueOf(map.get("Ipv8").toString()).floatValue());
                pstmt.setFloat(20, Float.valueOf(map.get("Uac1").toString()).floatValue());
                pstmt.setFloat(21, Float.valueOf(map.get("Uac2").toString()).floatValue());
                pstmt.setFloat(22, Float.valueOf(map.get("Uac3").toString()).floatValue());
                pstmt.setFloat(23, Float.valueOf(map.get("Iac1").toString()).floatValue());
                pstmt.setFloat(24, Float.valueOf(map.get("Iac2").toString()).floatValue());
                pstmt.setFloat(25, Float.valueOf(map.get("Iac3").toString()).floatValue());
                pstmt.setFloat(26, Float.valueOf(map.get("Status").toString()).floatValue());
                pstmt.setFloat(27, Float.valueOf(map.get("Error").toString()).floatValue());
                pstmt.setFloat(28, Float.valueOf(map.get("Temp").toString()).floatValue());
                pstmt.setFloat(29, Float.valueOf(map.get("Cos").toString()).floatValue());
                pstmt.setFloat(30, Float.valueOf(map.get("Fac").toString()).floatValue());
                pstmt.setFloat(31, Float.valueOf(map.get("Pac").toString()).floatValue());
                pstmt.setFloat(32, Float.valueOf(map.get("Qac").toString()).floatValue());
                pstmt.setFloat(33, Float.valueOf(map.get("Eac").toString()).floatValue());
                pstmt.setFloat(34, Float.valueOf(map.get("Cycle_Time").toString()).floatValue());
                pstmt.executeUpdate();
                pstmt.close();
		  }
	      inputStream.close();
	      socket.close();
			
		}catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void dbQuery(Socket socket, Connection conn, PreparedStatement ptmt, ResultSet rs, String line, String sql, String keyStart, String keyEnd) {
		System.out.println("get message from client: "+ line);
		System.out.println("Sql: "+ sql);
		
		try {
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			
			JSONArray array = new JSONArray();
			if(keyStart != "") {
				array.put(keyStart);
			}
			while(rs.next()) {
				JSONObject jsonObj = new JSONObject();
				for(int i = 1; i<= columnCount; i++) {
					String columnName = metaData.getColumnLabel(i);
					String value = rs.getString(columnName);
					jsonObj.put(columnName, value);
				}
				array.put(jsonObj);
			}
			if(keyEnd != "") {
				array.put(keyEnd);
			}
			System.out.println("JSON Data : "+array.toString());
			OutputStream outputStream = socket.getOutputStream();
			outputStream.close();
			System.out.println("\n-------------------------------------\n");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}
