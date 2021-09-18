package com.geekbrains.server;

import javax.swing.*;
import java.awt.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Server extends JFrame {

    private static final Logger log = LogManager.getLogger();

    private static final String APPLICATION_NAME = "Netty cloud server";

    private MenuItem buttonStart;
    private MenuItem buttonStop;

    public static void main(String[] args) {
        new Server().start();
    }

    private Server() {
        setVisible(false);
        putToTray();
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
            log.error("Add to tray error: " + e);
        }
    }
}
