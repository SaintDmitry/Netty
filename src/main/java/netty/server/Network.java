package netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.*;
import netty.common.settings.Settings;

class Network {

    private static EventLoopGroup mainGroup;
    private static EventLoopGroup workerGroup;
    private static ChannelFuture future;

    static void start() {
        Thread serverThread = new Thread(() -> {
            mainGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();

            ServerBootstrap b = new ServerBootstrap();
            b.group(mainGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(
                                    new ObjectDecoder(Settings.getMessageSize(), ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new MainHandler()
                            );
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            try {
                future = b.bind(Settings.getPort()).sync();
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
                stop();
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }

    static void stop() {
        future.channel().close();
        mainGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

}
