package com.geekbrains.common.transfers.messages;

import com.geekbrains.common.transfers.objects.FileStructure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileResponse extends AbstractTransferMessage {

    private FileStructure fileStructure;
    private FileStructure folder;
    private byte[] data;

    public FileResponse(Path path, FileStructure serverFolder) throws IOException {
        fileStructure = new FileStructure(path);
        if (path.toFile().isFile()) {
            data = Files.readAllBytes(path);
        }
        this.folder = serverFolder;
    }

    public FileStructure getFileStructure() {
        return fileStructure;
    }

    public byte[] getData() {
        return data;
    }

    public FileStructure getFolder() {
        return folder;
    }
}
