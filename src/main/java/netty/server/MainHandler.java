package netty.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import netty.common.settings.Settings;
import netty.common.transfers.messages.*;
import netty.common.transfers.objects.FileStructure;

public class MainHandler extends ChannelInboundHandlerAdapter {

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
            }
        } else {
            step++;
            createNewDirectory(newFolderPath, step);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) {
                return;
            }
            if (msg instanceof FileRequest) {
               processFileRequest(ctx, (FileRequest) msg);
            } else if (msg instanceof FileResponseOnServer) {
                processFileResponseOnServer(ctx, (FileResponseOnServer) msg);
            } else if (msg instanceof StorageStructureRequest) {
                processStorageStructureRequest(ctx, (StorageStructureRequest) msg);
            } else if (msg instanceof FileRemoveFromServer) {
                processFileRemoveFromServer(ctx, (FileRemoveFromServer) msg);
            } else if (msg instanceof CreateNewDirectoryOnServer) {
                processCreateNewDirectoryOnServer(ctx, (CreateNewDirectoryOnServer) msg);
            } else if (msg instanceof FileRenameOnServer) {
                processFileRenameOnServer(ctx, (FileRenameOnServer) msg);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void processFileRenameOnServer(ChannelHandlerContext ctx, FileRenameOnServer msg) {
        File oldFilename = new File(msg.getOldFilename().getFullFileName());
        File newFilename = new File(msg.getNewFilename().getFullFileName());

        oldFilename.renameTo(newFilename);
        processStorageStructureRequest(ctx, new StorageStructureRequest(new FileStructure(Paths.get(msg.getNewFilename().getParent()))));
    }

    private void processCreateNewDirectoryOnServer(ChannelHandlerContext ctx, CreateNewDirectoryOnServer msg) {
        createNewDirectory(msg.getCurrentServerFolder().getFullFileName() + "\\New folder", 0);
        Path path = null;
        if(msg.getCurrentServerFolder().getParent() == null) {
            path = Settings.getServerStorage().getPath();
        } else {
            path = Paths.get(msg.getCurrentServerFolder().getParent());
        }
    }

    private void processFileRemoveFromServer(ChannelHandlerContext ctx, FileRemoveFromServer msg) {
        deleteFile(msg.getFileStructure().getPath());
        processStorageStructureRequest(ctx, new StorageStructureRequest(new FileStructure(Paths.get(msg.getFileStructure().getParent()))));
    }

    private void processStorageStructureRequest(ChannelHandlerContext ctx, StorageStructureRequest msg) {
        List<FileStructure> files = new ArrayList<>();
        FileStructure folder = msg.getFileStructure();
        if(folder.getParent() != null) {
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
            return;
        }
        ctx.writeAndFlush(new StorageStructureResponse(files, folder));
    }

    private void processFileResponseOnServer(ChannelHandlerContext ctx, FileResponseOnServer msg) {
        String fullFileName = msg.getServerFolder().getFullFileName() + "/" + msg.getFileStructure().getName();
        try {
            Files.write(Paths.get(fullFileName), msg.getData(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        processStorageStructureRequest(ctx, new StorageStructureRequest(msg.getServerFolder()));
    }

    private void processFileRequest(ChannelHandlerContext ctx, FileRequest msg) {
        if(Files.exists(msg.getFileStructure().getPath())) {
            FileResponse fr = null;
            try {
                fr = new FileResponse(msg.getFileStructure().getPath());
                ctx.writeAndFlush(fr);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
