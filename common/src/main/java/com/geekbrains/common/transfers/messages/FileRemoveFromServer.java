package com.geekbrains.common.transfers.messages;

import com.geekbrains.common.transfers.objects.FileStructure;

public class FileRemoveFromServer extends AbstractTransferMessage {

    private FileStructure fileStructure;

    public FileRemoveFromServer(FileStructure fileStructure) {
        this.fileStructure = fileStructure;
    }

    public FileStructure getFileStructure() {
        return fileStructure;
    }
}
