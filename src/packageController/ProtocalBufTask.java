package packageController;

import java.nio.charset.Charset;
import java.util.List;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

public class ProtocalBufTask {

	public class Message {
		private int MagicNum;
		private byte Version;
		private MsgTypeEnum msgType;
		private String body;
		
		public int getMagicNum() {
			return MagicNum;
		}
		public void setMagicNum(int magicNum) {
			MagicNum = magicNum;
		}
		public byte getVersion() {
			return Version;
		}
		public void setVersion(byte version) {
			Version = version;
		}
		public MsgTypeEnum getMsgType() {
			return msgType;
		}
		public void setMsgType(MsgTypeEnum msgType) {
			this.msgType = msgType;
		}
		public String getBody() {
			return body;
		}
		public void setBody(String body) {
			this.body = body;
		}
	};
	
	public enum MsgTypeEnum{
		REQ_GW((byte)1), RES_GW((byte)2), REQ_DEVLIST((byte)3), RES_DEVLIST((byte)4), REQ_DEVCT((byte)5), RES_DEVCT((byte)6),
		REQ_DEVS((byte)7), RES_DEVS((byte)8), REQ_DEV((byte)9), RES_DEV((byte)10), REQ_SML((byte)11),  RES_SML((byte)12),
		REQ_MBLIST((byte)13), RES_MBLIST((byte)14), REQ_MBCT((byte)15), RES_MBCT((byte)16), REQ_MBS((byte)17), RES_MBS((byte)18),
		REQ_MB((byte)19), RES_MB((byte)20), PING((byte)21), PONG((byte)22);
		private byte type;
		
		MsgTypeEnum(byte type) {
			this.type = type;
		}
		
		public int getType() {
			return type;
		}
		
		public static MsgTypeEnum get(byte type) {
			for (MsgTypeEnum value : values()) {
				if(value.type == type) return value;
			}
			throw new RuntimeException("unsupported type: " + type);
		}
	}
	
	public class MessageEncoder extends MessageToByteEncoder<Message>{

		@Override
		protected void encode(ChannelHandlerContext ctx, Message message, ByteBuf out) throws Exception {
			out.writeInt(message.MagicNum);
			out.writeByte(message.Version);
			out.writeByte(message.getMsgType().getType());
			
			if (null == message.getBody()) {
				out.writeInt(0);
			}else {
				out.writeInt(message.getBody().length());
				out.writeCharSequence(message.getBody(), Charset.defaultCharset());
			}
		}
	}
	
	public class MessageDecoder extends ByteToMessageDecoder {

		@Override
		protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
			Message message = new Message();
			message.setMagicNum(byteBuf.readInt());
			message.setVersion(byteBuf.readByte());
			message.setMsgType(MsgTypeEnum.get(byteBuf.readByte()));
			
			int bodyLength = byteBuf.readInt();
			CharSequence body = byteBuf.readCharSequence(bodyLength, Charset.defaultCharset());
			message.setBody(body.toString());
			out.add(message);
		}
	}
	
	public class ServerMessageHandler extends ChannelInboundHandlerAdapter{
		
	}

}
