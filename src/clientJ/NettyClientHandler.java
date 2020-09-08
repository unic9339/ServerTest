package clientJ;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class NettyClientHandler extends ChannelInboundHandlerAdapter{
	
    // call channelActive when connected from server
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks", CharsetUtil.UTF_8));
	}
	
	// call channelRead when message was sent from server
	public void channelRead(ChannelHandlerContext ctx,Object msg) throws Exception {
		System.out.println("client received : "+ ((ByteBuf)msg).toString(CharsetUtil.UTF_8));
	}
	
	// call exceptionCaught when occur exception
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}
