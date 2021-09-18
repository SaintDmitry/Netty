package netty.server;

import netty.common.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Server extends JFrame {
    private static final String APPLICATION_NAME = "Netty cloud Server";

    private MenuItem buttonStart;
    private MenuItem buttonStop;


//    public void run() throws Exception {
//        EventLoopGroup mainGroup = new NioEventLoopGroup();
//        EventLoopGroup workerGroup = new NioEventLoopGroup();
//        try {
//            ServerBootstrap b = new ServerBootstrap();
//            b.group(mainGroup, workerGroup)
//                    .channel(NioServerSocketChannel.class)
//                    .childHandler(new ChannelInitializer<SocketChannel>() {
//                        protected void initChannel(SocketChannel socketChannel) throws Exception {
//                            socketChannel.pipeline().addLast(
//                                    new ObjectDecoder(50 * 1024 * 1024, ClassResolvers.cacheDisabled(null)),
//                                    new ObjectEncoder(),
//                                    new MainHandler()
//                            );
//                        }
//                    })
//                    .childOption(ChannelOption.SO_KEEPALIVE, true);
//
//            ChannelFuture future = b.bind(8189).sync();
//            future.channel().closeFuture().sync();
//        } finally {
//            mainGroup.shutdownGracefully();
//            workerGroup.shutdownGracefully();
//        }
//    }

    public static void main(String[] args) {
        new Server().start();
    }

    private Server() {
        setVisible(false);
        putToTray();

        if(Files.notExists(Paths.get(Settings.getServerStorage().getFullFileName()))) {
            try {
                Files.createDirectory(Paths.get(Settings.getServerStorage().getFullFileName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void start() {
        buttonStart.setEnabled(false);
        buttonStop.setEnabled(true);
        Network.start();
    }

    private void stop() {
        Network.stop();
        buttonStart.setEnabled(true);
        buttonStop.setEnabled(false);
    }

    private void putToTray() {
        PopupMenu trayMenu = new PopupMenu();

        buttonStart = new MenuItem("Start");
        buttonStart.addActionListener(e -> start());
        buttonStart.setEnabled(false);
        trayMenu.add(buttonStart);

        buttonStop = new MenuItem("Stop");
        buttonStop.addActionListener(e -> stop());
        buttonStop.setEnabled(false);
        trayMenu.add(buttonStop);

        MenuItem buttonExit = new MenuItem("Exit");
        buttonExit.addActionListener(e -> {
            if(buttonStop.isEnabled())
                stop();
            System.exit(0);
        });
        trayMenu.add(buttonExit);

        Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icon/icon.png"));
        TrayIcon trayIcon = new TrayIcon(icon, APPLICATION_NAME, trayMenu);
        trayIcon.setImageAutoSize(true);

        SystemTray tray = SystemTray.getSystemTray();
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
}
