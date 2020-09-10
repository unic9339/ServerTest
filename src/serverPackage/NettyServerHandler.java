package serverPackage;

import org.json.JSONArray;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import packageController.KeywordSql;

public class NettyServerHandler extends ChannelInboundHandlerAdapter{
	
	final static String url = "jdbc:mysql://localhost:3306/edget?serverTimezone=UTC";
    final static String user = "Kim";
    final static String password = "gokei";

	// echoServer Handler start 
	
//	// send message what was sent from client
	// start when data was sent from client
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		ByteBuf buf = (ByteBuf)msg;
		byte [] reg = new byte[buf.readableBytes()];
		buf.readBytes(reg);
		
		String body = new String(reg, "UTF-8");
		System.out.println( "sercer received: " + body);
		
		JSONArray sqlArray = new JSONArray();
		sqlArray = new KeywordSql().SqlTask(url, user, password, body);
		
		String responStr = sqlArray.toString();
		System.out.println("Json = "+responStr);
		ByteBuf responBuf = Unpooled.copiedBuffer(responStr.getBytes());
		ctx.write(responBuf);
	}
	
	
	// flush when channel was read
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		
		ctx.flush();
	}
	// echoServer Handler end


	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}
