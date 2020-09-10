package clientJ;

import java.util.Scanner;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import packageController.KeywordTask;

public class NettyClientHandler extends ChannelInboundHandlerAdapter{
	
	private Scanner sc = new Scanner(System.in);
	
    static int gwMode = 1;
    
    // call channelActive when connected from server
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("gwst_gw -> 1");
		System.out.println("gwst_delist -> 2");
		System.out.println("gwst_logger -> 3");
		System.out.println("gwst_mblist -> 4");
		System.out.println("gwst_mbct -> 5");
		System.out.println("gwst_mbid -> 6");
		System.out.println("gwst_mbidct -> 7");
		System.out.println("insert mode number : ");
		gwMode = sc.nextInt();
		// TODO keyword -> protocol
		String header = new KeywordTask().keyString(gwMode);
		ctx.writeAndFlush(Unpooled.copiedBuffer(header, CharsetUtil.UTF_8));
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
