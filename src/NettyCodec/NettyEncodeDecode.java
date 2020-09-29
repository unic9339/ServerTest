package NettyCodec;

import java.nio.charset.Charset;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.CharsetUtil;

public class NettyEncodeDecode {
	
	public static class NettyEncoder extends MessageToByteEncoder<MsgType>{
		@Override
		protected void encode(ChannelHandlerContext ctx, MsgType msgType, ByteBuf out) throws Exception {
			System.out.println("NettyEncodeDecode -> Encoding... ");
			System.out.println("Encode: " + msgType.getBody());
			System.out.println("Contxt: " +ctx.toString());
			System.out.println("ByteBuf: "+ out.toString());
			
			if (msgType.getMsgType() != MsgTypeEnum.EMPTY) {
		        out.writeShort(Constants.MAGIC_NUMBER);   // write magic number
		        out.writeByte(1);	// write version	 
		        out.writeByte(msgType.getMsgType().getType());	//  write current message type
		        
		        if (null == msgType.getBody()) {
		            out.writeInt(0);  //if body is empty, 0 is wrote, showing that length of body is zero
		        } else {
		            out.writeInt(msgType.getBody().length());
		            out.writeCharSequence(msgType.getBody(), Charset.defaultCharset());
		        }
		    }
		}
	}
	
	public static class NettyDecoder extends ByteToMessageDecoder {
		@Override
		protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
			System.out.println("NettyEncodeDecode -> Decoding... ");
			System.out.println("Context: " +byteBuf.readableBytes());
			
			if(byteBuf.readableBytes() < 4)
				return;
			else {
				MsgType message = new MsgType();
				message.setMagicNum(byteBuf.readShort());
				message.setVersion(byteBuf.readByte()); // read version
			    message.setMsgType(MsgTypeEnum.get(byteBuf.readByte()));	// read current message type
			    message.setLength(byteBuf.readShort()); //read length of the body
		    	byte[] reg = new byte[message.getLength()];
		        //System.out.println("readableBytes(): " + buf.readableBytes());
		        byteBuf.readBytes(reg);
		        //byte to string
		        String body = new String(reg, "UTF-8");
			    message.setBody(body.toString());
			    //message.getBody();
		        //System.out.println("message.getBody(): "+ message.getBody());
			    out.add(message);
			}
		}
	}

}
