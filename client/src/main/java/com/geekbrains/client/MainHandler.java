package com.geekbrains.client;

import com.geekbrains.client.controllers.ClientController;
import com.geekbrains.common.transfers.messages.*;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class MainHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LogManager.getLogger();

    private ClientController controller;

    MainHandler(ClientController controller) {
        this.controller = controller;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof AuthenticationResponse) {
                processAuthenticationResponse((AuthenticationResponse) msg);
            } else if (msg instanceof RegistrationResponse) {
                processRegistrationResponse((RegistrationResponse) msg);
            } else if(msg instanceof FileResponse) {
                processFileResponse((FileResponse) msg);
            } else if(msg instanceof StorageStructureResponse) {
                processStorageStructureResponse((StorageStructureResponse) msg);
            } else {
                log.error("Unknown inbound message error: " + msg.getClass());
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx){
        Network.setCtx(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        cause.printStackTrace();
        log.error("ExceptionCaught error: " + cause);
        ctx.close();
    }

    private void processAuthenticationResponse(AuthenticationResponse msg) {
        if (msg.isAuthenticated()) {
            controller.authSuccessful(msg.getNickname());
        }
    }

    private void processRegistrationResponse(RegistrationResponse msg) {
        if (msg.isRegistrated()) {
            controller.registrationSuccessful();
        }
    }

    private void processStorageStructureResponse(StorageStructureResponse msg) {
        controller.refreshServerFilesList(msg.getFiles(), msg.getCurrentFolder());
    }

    private void processFileResponse(FileResponse msg) {
        String fullFileName = msg.getFolder().getFullFileName() + File.separator + msg.getFileStructure().getName();
        try {
            if (Files.exists(Paths.get(fullFileName))) {
                controller.deleteFile(Paths.get(fullFileName));
            }
            if (msg.getData() != null) {
                Files.write(Paths.get(fullFileName), msg.getData(), StandardOpenOption.CREATE);
            } else {
                controller.createNewDirectory(fullFileName, 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("processFileResponse error: " + e);
        }
        controller.refreshLocalFilesList(controller.getCurrentClientFolder());
    }
}
