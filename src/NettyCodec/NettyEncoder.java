package NettyCodec;

import java.nio.charset.Charset;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class NettyEncoder extends MessageToByteEncoder<MsgType>{

	@Override
	protected void encode(ChannelHandlerContext ctx, MsgType msgType, ByteBuf out) throws Exception {
		System.out.println("encode");
		// See if message type is EMPTY type. If it is EMPTY，the message dosn't need to be wrote unto channel
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
