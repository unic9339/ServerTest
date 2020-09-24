package NettyCodec;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class NettyChannelInitializer extends ChannelInitializer<SocketChannel>{

	private NettyTaskHandler nettyChannelHandler;
	@Override
	protected void initChannel(SocketChannel socketChannel) throws Exception {
		socketChannel.pipeline().addLast(new NettyDecoder());
		socketChannel.pipeline().addLast(nettyChannelHandler);
		socketChannel.pipeline().addLast(new NettyEncoder());
	}
	

}
