package NettyCodec;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class NettyChannelInitializer extends ChannelInitializer<SocketChannel>{

	private NettyTaskHandler nettyChannelHandler;
	
	@Override
	protected void initChannel(SocketChannel socketChannel) throws Exception {
//		socketChannel.pipeline().addLast(new NettyDecoder());
//		socketChannel.pipeline().addLast(new NettyEncoder());
//		socketChannel.pipeline().addLast("Decoder", new NettyEncodeDecode.NettyDecoder());
//		socketChannel.pipeline().addLast("Encoder", new NettyEncodeDecode.NettyEncoder());
		System.out.println("log");
		socketChannel.pipeline().addLast(nettyChannelHandler);
	}
}
