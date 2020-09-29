package clientJ;

import NettyCodec.MsgType;
import NettyCodec.MsgTypeEnum;
import NettyCodec.NettyEncodeDecode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class NettyProtoHandler extends ChannelInboundHandlerAdapter{
	
	String magic_no = "gwst";
	int gw_ver = 1;
	String gw = "open107vstm32f107vct6";

	short body_len = 0;
	MsgType resMsg = new MsgType();
	byte[] one;
    byte[] two;
    byte[] thr;
    byte[] fou;
    byte[] dest;
    ByteBuf respByteBuf;
     
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	System.out.println("protocol setting");
    	
    	body_len = (short)gw.toString().getBytes().length;
        if (body_len <= 255) {
        	body_len = (short)(32512 + body_len); //change high byte 0x0000 to 0xff00, preventing 0x00 from becoming terminated null character
        }
               
    	resMsg.setMagicNum((short)0x1234);
    	resMsg.setVersion((byte)gw_ver);
    	resMsg.setMsgType(MsgTypeEnum.REQ_GW);
    	resMsg.setLength(body_len);
    	resMsg.setBody(gw);
    	
    	one = new byte[]{(byte)((resMsg.getMagicNum()>>8) & 0xff), (byte)(resMsg.getMagicNum() & 0xff)};
        two = new byte[]{(byte)(resMsg.getVersion())};
        thr = new byte[]{(byte)(resMsg.getMsgType().getType() & 0xff)};
        fou = new byte[]{(byte)((resMsg.getLength()>>8) & 0xff), (byte)(resMsg.getLength() & 0xff)};
         
        System.out.println("one Len: "+one.length);
        System.out.println("two Len: "+two.length);
        System.out.println("thr Len: "+thr.length);
        System.out.println("fou Len: "+fou.length);
        
        dest = new byte[one.length + two.length + thr.length + fou.length + resMsg.getBody().getBytes().length];
        //byte[] dest = new byte[one.length + two.length + thr.length + fou.length];
        
        System.out.println("dest: "+dest.toString());
        respByteBuf = Unpooled.copiedBuffer(dest);
         
        System.out.println("respBuf: "+respByteBuf);
        
        ctx.write(respByteBuf);
//        ctx.writeAndFlush(respByteBuf);
    }
    
	// call channelRead when message was sent from server
	public void channelRead(ChannelHandlerContext ctx,Object msg) throws Exception {
		
		System.out.println("client received : "+ ((ByteBuf)msg).toString(CharsetUtil.UTF_8));
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		new NettyEncodeDecode.NettyEncoder();
		ctx.flush();
	}
	
	// call exceptionCaught when occur exception
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
    
}
