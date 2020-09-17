package NettyCodec;

public enum MsgTypeEnum {
	REQ_GW((byte)1), RES_GW((byte)2), REQ_DEVLIST((byte)3), RES_DEVLIST((byte)4), REQ_DEVCT((byte)5), RES_DEVCT((byte)6),
	REQ_DEVS((byte)7), RES_DEVS((byte)8), REQ_DEV((byte)9), RES_DEV((byte)10), REQ_SML((byte)11),  RES_SML((byte)12),
	REQ_MBLIST((byte)13), RES_MBLIST((byte)14), REQ_MBCT((byte)15), RES_MBCT((byte)16), REQ_MBS((byte)17), RES_MBS((byte)18),
	REQ_MB((byte)19), RES_MB((byte)20), PING((byte)21), PONG((byte)22), EMPTY((byte)23);
	
	private byte type;
	 
	private MsgTypeEnum(byte type) {
	    this.type = type;
	}
	 
	public int getType() {
	    return type;
	}
	 
	public static MsgTypeEnum get(byte type) {
	    for (MsgTypeEnum value : values()) {
	        if (value.type == type) {
	            return value;
	        }
	    }
	    throw new RuntimeException("unsupported type: " + type);
	}
}
