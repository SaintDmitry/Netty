package com.geekbrains.common.transfers.messages;

import com.geekbrains.common.transfers.objects.FileStructure;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileResponseUNUSED extends AbstractTransferMessage {
//
//    private FileStructure fileStructure;
//    private byte[] data;
//
//    public FileResponse(Path path) throws IOException {
//        fileStructure = new FileStructure(path);
//        if (path.toFile().isFile()) {
//            data = Files.readAllBytes(path);
//        }
//    }
//
//    public FileStructure getFileStructure() {
//        return fileStructure;
//    }
//
//    public byte[] getData() {
//        return data;
//    }
}
