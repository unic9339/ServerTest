package NettyCodec;

public class MsgType {
	private short magicNum;
	private byte version;
	private MsgTypeEnum msgType;
    private short length;
	private String body;
	
	public short getMagicNum() {
		return magicNum;
	}
	public void setMagicNum(short magicNum) {
		this.magicNum = magicNum;
	}
	
	public byte getVersion() {
		return version;
	}
	public void setVersion(byte version) {
		this.version = version;
	}
	
	public MsgTypeEnum getMsgType() {
		return msgType;
	}
	public void setMsgType(MsgTypeEnum msgType) {
		this.msgType = msgType;
	}
	
	public short getLength() {
		return length;
	}
	public void setLength(short length) {
		this.length = length;
	}
	
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
}
