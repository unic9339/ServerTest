package serverPackage;

import NettyCodec.NettyChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {
	
	private static final int PORT_NUM = 1225;

	public static void main(String[] args) throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		try {
			ServerBootstrap sb = new ServerBootstrap();
			sb.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.childHandler(new NettyChannelInitializer());
//			.childHandler(new ChannelInitializer<>() {
//
//				@Override
//				protected void initChannel(Channel ch) throws Exception {
//					ChannelPipeline p = ch.pipeline();
//					// add Handler in addlast();
////					p.addLast(new NettyServerHandler());
//					p.addLast(new NettyChannelInitializer());
//				}
//			});
			
			ChannelFuture f = sb.bind(PORT_NUM).sync();
			f.channel().closeFuture().sync();
		}finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

}
