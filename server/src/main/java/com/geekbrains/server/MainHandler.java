package com.geekbrains.server;

import com.geekbrains.common.settings.Settings;
import com.geekbrains.common.transfers.messages.*;
import com.geekbrains.common.transfers.objects.FileStructure;

import com.geekbrains.server.dbutils.DBRequestHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LogManager.getLogger();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg == null) {
                return;
            }
            if (msg instanceof AuthenticationRequest) {
                processAuthenticationRequest(ctx, (AuthenticationRequest) msg);
            } else if (msg instanceof RegistrationRequest) {
                processRegistrationRequest(ctx, (RegistrationRequest) msg);
            } else if (msg instanceof FileRequest) {
                processFileRequest(ctx, (FileRequest) msg);
            } else if (msg instanceof FileResponse) {
                processFileResponse(ctx, (FileResponse) msg);
            } else if (msg instanceof StorageStructureRequest) {
                processStorageStructureRequest(ctx, (StorageStructureRequest) msg);
            } else if (msg instanceof FileRemoveFromServer) {
                processFileRemoveFromServer(ctx, (FileRemoveFromServer) msg);
            } else if (msg instanceof CreateNewDirectoryOnServer) {
                processCreateNewDirectoryOnServer(ctx, (CreateNewDirectoryOnServer) msg);
            } else if (msg instanceof FileRenameOnServer) {
                processFileRenameOnServer(ctx, (FileRenameOnServer) msg);
            } else {
                log.error("Unknown inbound message error: " + msg.getClass());
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void processAuthenticationRequest(ChannelHandlerContext ctx, AuthenticationRequest msg) {
        boolean authenticated;

        String nickname = DBRequestHandler.authenticate(msg.getLogin(), msg.getPasword());
        authenticated = (nickname != null);

        if (authenticated) createClientRootDirectory(nickname);

        ctx.writeAndFlush(new AuthenticationResponse(authenticated, nickname));
    }

    private void processRegistrationRequest(ChannelHandlerContext ctx, RegistrationRequest msg) {
        boolean registrated = DBRequestHandler.registration(msg.getNickname(), msg.getLogin(), msg.getPassword());

        ctx.writeAndFlush(new RegistrationResponse(registrated));
    }

    private void processFileRenameOnServer(ChannelHandlerContext ctx, FileRenameOnServer msg) {
        File oldFilename = new File(msg.getOldFilename().getFullFileName());
        File newFilename = new File(msg.getNewFilename().getFullFileName());

        oldFilename.renameTo(newFilename);
        processStorageStructureRequest(ctx, new StorageStructureRequest(new FileStructure(Paths.get(msg.getNewFilename().getParent()))));
    }

    private void processCreateNewDirectoryOnServer(ChannelHandlerContext ctx, CreateNewDirectoryOnServer msg) {
        createNewDirectory(msg.getCurrentServerFolder().getFullFileName() + File.separator +
                msg.getNewFolderName(), 0);
    }

    private void processFileRemoveFromServer(ChannelHandlerContext ctx, FileRemoveFromServer msg) {
        deleteFile(msg.getFileStructure().getPath());
        processStorageStructureRequest(ctx, new StorageStructureRequest(new FileStructure(Paths.get(msg.getFileStructure().getParent()))));
    }

    private void processStorageStructureRequest(ChannelHandlerContext ctx, StorageStructureRequest msg) {
        List<FileStructure> files = new ArrayList<>();
        FileStructure folder = msg.getFileStructure();
        if(folder.getParent() != null && !folder.getParent().equals(Settings.commonServerStorage.toString())) {
            files.add(new FileStructure(Paths.get(folder.getParent()), "..."));
        }
        try {
            Files.walk(folder.getPath(), 1).forEach(path -> {
                if(!folder.getFullFileName().equals(path.toString())) {
                    files.add(new FileStructure(path));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            log.error("processStorageStructureRequest error: " + e);
            return;
        }
        ctx.writeAndFlush(new StorageStructureResponse(files, folder));
    }

    private void processFileResponse(ChannelHandlerContext ctx, FileResponse msg) {
        String fullFileName = msg.getFolder().getFullFileName() + File.separator + msg.getFileStructure().getName();
        try {
            if (Files.exists(Paths.get(fullFileName))) {
                deleteFile(Paths.get(fullFileName));
            }
            if (msg.getData() != null) {
                Files.write(Paths.get(fullFileName), msg.getData(), StandardOpenOption.CREATE);
            } else {
                createNewDirectory(fullFileName, 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("processFileResponse error: " + e);
        }
    }

    private void processFileRequest(ChannelHandlerContext ctx, FileRequest msg) {
        Path path = msg.getFileStructure().getPath();
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                FileStructure tempClientFolder = msg.getCurrentClientFolder();

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    ctx.writeAndFlush(new FileResponse(dir, tempClientFolder));
                    tempClientFolder = new FileStructure(Paths.get(tempClientFolder.getFullFileName() +
                            File.separator + dir.getName(dir.getNameCount() - 1)));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    ctx.writeAndFlush(new FileResponse(file, tempClientFolder));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    tempClientFolder = new FileStructure(Paths.get(tempClientFolder.getFullFileName().
                            replace(File.separator + dir.getName(dir.getNameCount() - 1), "")));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            log.error("processFileRequest error: " + e);
        }
    }

    private void deleteFile(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            log.error("deleteFile error: " + e);
        }
    }

    private void createNewDirectory(String newFolderPath, int step) {
        String path = newFolderPath;
        if (step > 0) {
            path = path + " (" + step + ")";
        }
        if (Files.notExists(Paths.get(path))) {
            try {
                Files.createDirectory(Paths.get(path));
            } catch (IOException e) {
                e.printStackTrace();
                log.error("createNewDirectory error: " + e);
            }
        } else {
            step++;
            createNewDirectory(newFolderPath, step);
        }
    }

    private void createClientRootDirectory(String nickname) {
        if(Files.notExists(Paths.get(Settings.commonServerStorage.getFullFileName() + File.separator + nickname))) {
            try {
                Files.createDirectory(Paths.get(Settings.commonServerStorage.getFullFileName() + File.separator + nickname));
            } catch (IOException e) {
                e.printStackTrace();
                log.error("createClientRootDirectory error: " + e);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        log.error("exceptionCaught error: " + cause);
        ctx.close();
    }
}
