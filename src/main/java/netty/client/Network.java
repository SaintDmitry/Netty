package netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.*;
import netty.client.controllers.ClientController;
import netty.common.settings.Settings;
import netty.common.transfers.messages.AbstractTransferMessage;

public class Network {

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
                                    new ObjectDecoder(Settings.getMessageSize(), ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new MainHandler(controller)
                            );
                        }
                    })
                    .remoteAddress(Settings.getServerAdress(), Settings.getPort());

            try {
                future = b.connect().sync();
                serverConnect = true;
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
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
//        if(isServerConnect()) {
//            ctx.writeAndFlush(msg);
//        } else {
            while (!isServerConnect()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ctx.writeAndFlush(msg);
//        }


//        if (!isServerConnect()) {
//            while (!isServerConnect()) {
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        ctx.writeAndFlush(msg);
    }

    private static boolean isServerConnect() {
        return serverConnect && ctx != null && ctx.channel().isOpen();
    }

    static void setCtx(ChannelHandlerContext ctx) {
        Network.ctx = ctx;
    }

}
