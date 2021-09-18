package com.geekbrains.server;

import com.geekbrains.common.settings.Settings;

import com.geekbrains.server.dbutils.DBRequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.SQLException;

class Network {

    private static final Logger log = LogManager.getLogger();

    private static EventLoopGroup mainGroup;
    private static EventLoopGroup workerGroup;
    private static ChannelFuture future;

    static void start() {
        Thread serverThread = new Thread(() -> {
            dbConnect();
            mainGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            ServerBootstrap b = new ServerBootstrap();
            b.group(mainGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(
                                    new ObjectDecoder(Settings.MESSAGE_SIZE, ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new MainHandler()
                            );
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            try {
                future = b.bind(Settings.PORT).sync();
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.error("Network start error: " + e);
                stop();
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }

    static void stop() {
        dbDisconnect();
        future.channel().close();
        mainGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    static void dbConnect() {
        try {
            DBRequestHandler.connect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void dbDisconnect() {
        DBRequestHandler.disconnect();
    }
}
