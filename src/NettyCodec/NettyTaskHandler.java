package NettyCodec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class NettyTaskHandler extends ChannelInboundHandlerAdapter{

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("connecting");
		NettyProtocol netPro = new NettyProtocol();
//		ByteBuf buf = (ByteBuf)msg;
		ByteBuf buf = netPro.NettyProtocol(msg);
		
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
        byte[] reg = new byte[length];
        buf.readBytes(reg);

        String body = new String(reg, "UTF-8");
        System.out.println( "server received body: " + body);
	}
	
	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		new NettyEncodeDecode.NettyDecoder();
		ctx.flush();
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		new NettyEncodeDecode.NettyEncoder();
		ctx.flush();
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
	
	// byte配列を文字列に変換
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
