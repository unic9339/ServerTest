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
	
	
	// search for ChannelInboundHandler 
	public class ServerMessageHandler extends ChannelInboundHandlerAdapter{
	
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			// TODO Auto-generated method stub
			 ByteBuf buf = (ByteBuf) msg;
		    	/*
		    	List<Object> out = new ArrayList<>();
		        MessageDecoder msgdecoder = new MessageDecoder();
		        msgdecoder.decode(ctx, buf, out);
		        */
		        short magicNum = buf.readShort();
		        System.out.println("buf>readShort(): " + magicNum);
			    if (magicNum == 0x1234) {
		            System.out.println( "received packet is about gw");   	    	
			    }
		        System.out.println("buf>readByte(): " + buf.readByte());
		        int type = buf.readByte();
		        System.out.println("msg_type: " + type);
		        short length = buf.readShort();     
		        System.out.println("body's length: " + length);
		        //System.out.println("buf>readByte(): "+ buf.readByte());
		        //use byte array to read the data, same as file IO
		        //byte[] reg = new byte[buf.readableBytes()];
		    	byte[] reg = new byte[length];
		        //System.out.println("readableBytes(): " + buf.readableBytes());
		        buf.readBytes(reg);
		        //byte to string
		        String body = new String(reg, "UTF-8");
		        System.out.println( "server received body: " + body);
		              
		}
		
		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.channelReadComplete(ctx);
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			cause.printStackTrace();
	        ctx.close();
		}
		
		@Override
		public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.channelRegistered(ctx);
		}
	}

}
