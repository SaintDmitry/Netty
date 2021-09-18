package com.geekbrains.common.transfers.messages;

import com.geekbrains.common.transfers.objects.FileStructure;

public class FileRequest extends AbstractTransferMessage {

    private FileStructure fileStructure;
    private FileStructure currentClientFolder;

    public FileRequest(FileStructure fileStructure, FileStructure currentClientFolder) {
        this.fileStructure = fileStructure;
        this.currentClientFolder = currentClientFolder;
    }

    public FileStructure getFileStructure() {
        return fileStructure;
    }

    public FileStructure getCurrentClientFolder() {
        return currentClientFolder;
    }
}
