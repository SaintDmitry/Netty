package com.geekbrains.common.transfers.messages;


import com.geekbrains.common.transfers.objects.FileStructure;

public class StorageStructureRequest extends AbstractTransferMessage {

    private FileStructure fileStructure;

    public StorageStructureRequest(FileStructure fileStructure) {
        this.fileStructure = fileStructure;
    }

    public FileStructure getFileStructure() {
        return fileStructure;
    }
}
