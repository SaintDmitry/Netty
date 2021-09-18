package com.geekbrains.client;

import com.geekbrains.client.controllers.ClientController;
import com.geekbrains.common.settings.Settings;
import com.geekbrains.common.transfers.messages.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Network {

    private static final Logger log = LogManager.getLogger();

    private static EventLoopGroup mainGroup;
    private static ChannelFuture future;
    private static ChannelHandlerContext ctx;
    private static boolean serverConnect;


    public static void start(ClientController controller) {
        Thread clientThread = new Thread(() -> {
            mainGroup = new NioEventLoopGroup();

            Bootstrap b = new Bootstrap();
            b.group(mainGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(
                                    new ObjectDecoder(Settings.MESSAGE_SIZE, ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new MainHandler(controller)
                            );
                        }
                    })
                    .remoteAddress(Settings.SERVER_ADDRESS, Settings.PORT);

            try {
                future = b.connect().sync();
                serverConnect = true;
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.error("Network start error: " + e);
                stop();
            }
        });
        clientThread.setDaemon(true);
        clientThread.start();
    }

    public static void stop() {
        mainGroup.shutdownGracefully();
        serverConnect = false;
        System.exit(0);
    }

    public static void sendMsg(AbstractTransferMessage msg) {
        while (!isServerConnect()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.error("Network sendMsg error: " + e);
            }
        }
        ctx.writeAndFlush(msg);
    }

    private static boolean isServerConnect() {
        return serverConnect && ctx != null && ctx.channel().isOpen();
    }

    static void setCtx(ChannelHandlerContext ctx) {
        Network.ctx = ctx;
    }

}
