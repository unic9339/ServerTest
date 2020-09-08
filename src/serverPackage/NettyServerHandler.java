package serverPackage;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyServerHandler extends SimpleChannelInboundHandler<Object>{

	// echoServer Handler start 
	
//	// send message what was sent from client
//	@Override
//	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//		ctx.write(msg);
//	}
//	
//	// flush when channel was read
//	@Override
//	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//		ctx.flush();
//	}
//	// echoServer Handler end

	// start when data was sent from client
	@Override
	protected void channelRead0(ChannelHandlerContext arg0, Object arg1) throws Exception {
		
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}
