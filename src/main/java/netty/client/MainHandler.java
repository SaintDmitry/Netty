package netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import netty.client.controllers.ClientController;
import netty.common.transfers.messages.FileResponse;
import netty.common.transfers.messages.StorageStructureResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class MainHandler extends ChannelInboundHandlerAdapter {
    private ClientController controller;

    MainHandler(ClientController controller) {
        this.controller = controller;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if(msg instanceof FileResponse) {
                processFileResponse((FileResponse) msg);
            } else if(msg instanceof StorageStructureResponse) {
                processStorageStructureResponse((StorageStructureResponse) msg);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Network.setCtx(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private void processStorageStructureResponse(StorageStructureResponse msg) {
        StorageStructureResponse ssr = msg;
        controller.refreshServerFilesList(ssr.getFiles(), ssr.getCurrentFolder());
    }

    private void processFileResponse(FileResponse msg) throws IOException {
        FileResponse fr = msg;
        Files.write(Paths.get(controller.getCurrentClientFolder().getFullFileName() + "/" +
                fr.getFileStructure().getName()), fr.getData(), StandardOpenOption.CREATE);
        controller.refreshLocalFilesList(controller.getCurrentClientFolder());
    }
}
