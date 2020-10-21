package clientJ;

import NettyCodec.NettyEncodeDecode;
import NettyCodec.NettyEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ClientNetty {

	private static final String HOST = "127.0.0.1";
	private static final int PORT_NUM = 1225;
	static final int MESSAGE_SIZE = 256;
	
	public static void main(String[] args) {
		EventLoopGroup group = new NioEventLoopGroup();
		
		try {
			Bootstrap bs = new Bootstrap();
			bs.group(group)
			.channel(NioSocketChannel.class)
			.option(ChannelOption.TCP_NODELAY, true)
			.handler(new ChannelInitializer<Channel>() {

				@Override
				protected void initChannel(Channel sc) throws Exception {
					ChannelPipeline cp = sc.pipeline();
//					cp.addLast(new NettyClientHandler());
					
//					cp.addLast("Decode", new NettyEncodeDecode.NettyDecoder());
					cp.addLast(new NettyProtoHandler());
//					cp.addLast("Encode", new NettyEncodeDecode.NettyEncoder());

				}
			});
			
			ChannelFuture cf = bs.connect(HOST, PORT_NUM).sync();
			cf.channel().closeFuture().sync();
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			group.shutdownGracefully();
		}

	}

}
