package packageController;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONArray;
import org.json.JSONObject;

public class KeywordSql{
	
	private Connection conn = null;
	private Statement stmt = null;
	private PreparedStatement pstmt = null;
	private ResultSet rs = null;
	
	static String sqlUrl = "";
    static String sqlUser = "";
    static String sqlPassword ="";
    static String sqlMode = "";
    
    static String gwst_gw = "gwst_gw";          // quering a gw dada by esn
    static String gwst_delist = "gwst_delist";	// quering a list of device by gw_esn
    static String gwst_logger = "gwst_logger";	// quering logger data by gw_esn
    static String gwst_mblist = "gwst_mblist";	// quering alist of modbus_id
    static String gwst_mbct = "gwst_mbct";	// quering count of modbus_id
    static String gwst_mbid = "gwst_mbid";	// quering modbus i/f data by modbus_id
    static String gwst_mbidct = "gwst_mbidct";	// quering length of modbus i/f data by modbus_id
    static String gwed = "_gwed";  //end symbal
    
	public JSONArray SqlTask(String url, String user, String pw, String mode) {
		
		JSONArray returnArray = new JSONArray();
		sqlUrl = url;
		sqlUser = user;
		sqlPassword = pw;
		sqlMode = mode;
		
		try {
			conn = DriverManager.getConnection(sqlUrl, sqlUser, sqlPassword);
			
			String keystart, keyend, ens;
			String sql= new String();
			
			if(sqlMode.contains(gwst_gw) && sqlMode.contains(gwed)) {
				keystart = gwst_gw;
			    keyend = gwed;
			    
			    ens = subEsn(sqlMode, keystart, keyend);
			    System.out.println("SerNum = " + ens);
			    sql = "select esn, plant_id, name, smlogger_esn, sam_t, ser_id1, ser_id2, user_id from gw where esn =?";
	            pstmt = conn.prepareStatement(sql);
	            pstmt.setString(1, ens);
	            System.out.println(pstmt);
	            rs = pstmt.executeQuery();
	            
	            returnArray = dbQuery(conn, pstmt, rs, sqlMode, sql, keystart, keyend);
			}else if(sqlMode.contains(gwst_delist) && sqlMode.contains(gwed)) {
				keystart = gwst_delist;
			    keyend = gwed;
			    
			    ens = subEsn(sqlMode, keystart, keyend);
			    System.out.println("SerNum = " + ens);
	            sql = "select unit_id, modbus_id, gw_esn from pcs where gw_esn =?";
	            pstmt = conn.prepareStatement(sql);
	            pstmt.setString(1, ens);
	            System.out.println(pstmt);
	            rs = pstmt.executeQuery();
	            
	            returnArray = dbQuery(conn, pstmt, rs, sqlMode, sql, keystart, keyend);    
			}else if(sqlMode.contains(gwst_logger) && sqlMode.contains(gwed)) {
				keystart = gwst_logger;
			    keyend = gwed;
							
			    ens = subEsn(sqlMode, keystart, keyend);
			    System.out.println("SerNum = " + ens);
	            sql = "select esn, modbus_id, name, ip_addr, mtcp_port from smlogger where gw_esn =?";
	            pstmt = conn.prepareStatement(sql);
	            pstmt.setString(1, ens);
	            System.out.println(pstmt);
	            rs = pstmt.executeQuery();
	            
	            returnArray = dbQuery(conn, pstmt, rs, sqlMode, sql, keystart, keyend);
			}else if(sqlMode.contains(gwst_mblist) && sqlMode.contains(gwed)) {
				keystart = gwst_mblist;
			    keyend = gwed;
				
			    ens = subEsn(sqlMode, keystart, keyend);
			    System.out.println("SerNum = " + ens);
	            sql = "select distinct modbus_id from pcs where gw_esn =?";
	            pstmt = conn.prepareStatement(sql);
	            pstmt.setString(1, ens);
	            System.out.println(pstmt);
	            rs = pstmt.executeQuery();
	            
	            returnArray = dbQuery(conn, pstmt, rs, sqlMode, sql, keystart, keyend);
			}else if(sqlMode.contains(gwst_mbct) && sqlMode.contains(gwed)) {
				keystart = gwst_mbct;
			    keyend = gwed;
			    
			    ens = subEsn(sqlMode, keystart, keyend);
			    System.out.println("SerNum = " + ens);
	            sql = "select count(distinct modbus_id) from pcs where gw_esn =?";
	            pstmt = conn.prepareStatement(sql);
	            pstmt.setString(1, ens);
	            System.out.println(pstmt);
	            rs = pstmt.executeQuery();
	            
	            returnArray = dbQuery(conn, pstmt, rs, sqlMode, sql, keystart, keyend);
			}else if(sqlMode.contains("modbus_id")) {
				keystart = "";
			    keyend = "";
				
			    ens = subEsn(sqlMode, keystart, keyend);
			    System.out.println("SerNum = " + ens);
	            sql = "select modbus_id, count(modbus_id) from modbus";
	            pstmt = conn.prepareStatement(sql);
	            pstmt.setString(1, ens);
	            System.out.println(pstmt);
	            rs = pstmt.executeQuery();
	            
	            returnArray = dbQuery(conn, pstmt, rs, sqlMode, sql, keystart, keyend);
			}else if(sqlMode.contains(gwst_mbid) && sqlMode.contains(gwed)) {
				keystart = gwst_mbid;
			    keyend = gwed;
				
			    ens = subEsn(sqlMode, keystart, keyend);
			    System.out.println("SerNum = " + ens);
	            sql = "select modbus_id, name, type, gain, reg_addr, num from modbus where modbus_id = ?";
	            pstmt = conn.prepareStatement(sql);
	            pstmt.setString(1, ens);
	            System.out.println(pstmt);
	            rs = pstmt.executeQuery();
	            
	            returnArray = dbQuery(conn, pstmt, rs, sqlMode, sql, keystart, keyend);
			}else if(sqlMode.contains("mb2esnst_") && sqlMode.contains("_mb2esned")) {
				keystart = "mb2esnst_";
			    keyend = "_mb2esned";
				
			    ens = subEsn(sqlMode, keystart, keyend);
			    System.out.println("SerNum = " + ens);
	            sql = "select A.modbus_id, B.name, B.type, B.gain, B.reg_addr, B.num from pcs as A left join modbus as B on A.modbus_id = B.modbus_id where A.gw_esn = ? and A.modbus_id = ?";
	            pstmt = conn.prepareStatement(sql);
	            pstmt.setString(1, ens);
	            System.out.println(pstmt);
	            rs = pstmt.executeQuery();
	            
	            returnArray = dbQuery(conn, pstmt, rs, sqlMode, sql, keystart, keyend);
			}else {
				returnArray = null;
			}
					
		}catch (SQLException ex) {
			ex.printStackTrace();
		}finally {
			try {
				if (conn != null && !conn.isClosed()) conn.close();
			}catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return returnArray;
	}
	
	private String subEsn(String line, String keyStart, String keyEnd) {
		
		int index1 = line.indexOf(keyStart);
    	int index2 = line.indexOf(keyEnd);
    	
    	return line.substring(index1+7, index2);		
	}
	
	public static JSONArray dbQuery(Connection conn, PreparedStatement ptmt, ResultSet rs, String line, String sql, String keyStart, String keyEnd) {
		System.out.println("get message from client: "+ line);
		System.out.println("Sql: "+ sql);
		JSONArray array = new JSONArray();
		
		try {
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
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
			System.out.println("\n-------------------------------------\n");
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return array;
	}

}
