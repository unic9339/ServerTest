package packageController;

public class KeywordTask {

    static String gwst_gw = "gwst_gw";          // quering a gw dada by esn
    static String gwst_delist = "gwst_delist";	// quering a list of device by gw_esn
    static String gwst_logger = "gwst_logger";	// quering logger data by gw_esn
    static String gwst_mblist = "gwst_mblist";	// quering alist of modbus_id
    static String gwst_mbct = "gwst_mbct";	// quering count of modbus_id
    static String gwst_mbid = "gwst_mbid";	// quering modbus i/f data by modbus_id
    static String gwst_mbidct = "gwst_mbidct";	// quering length of modbus i/f data by modbus_id
    static String gwed = "_gwed";  //end symbal

    
	public String keyString(int gwMode) {
		String serNum = "open107vstm32f107vct6";
		
		String keywordLine = new String();
		switch (gwMode) {
		case 1:
			keywordLine = gwst_gw + serNum + gwed;
			System.out.println(keywordLine+"\n");
			break;
		case 2:
			keywordLine = gwst_delist + serNum + gwed;
			System.out.println(keywordLine+"\n");
			break;
		case 3:
			keywordLine = gwst_logger + serNum + gwed;
			System.out.println(keywordLine+"\n");
			break;
		case 4:
			keywordLine = gwst_mblist + serNum + gwed;
			System.out.println(keywordLine+"\n");
			break;
		case 5:
			keywordLine = gwst_mbct + serNum + gwed;
			System.out.println(keywordLine+"\n");
			break;
		case 6:
			keywordLine = gwst_mbid + serNum + gwed;
			System.out.println(keywordLine+"\n");
			break;
		case 7:
			keywordLine = gwst_mbidct + serNum + gwed;
			System.out.println(keywordLine+"\n");
			break;
		default:
			keywordLine = gwst_gw + serNum + gwed;
			System.out.println(keywordLine+"\n");
			break;
		} 
		return keywordLine;
	}

}
