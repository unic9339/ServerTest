package NettyCodec;

import java.util.List;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class NettyDecoder extends ByteToMessageDecoder{

	public void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
		System.out.println("Decoding...");
		
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
	};
}
