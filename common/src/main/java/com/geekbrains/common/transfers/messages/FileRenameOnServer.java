package com.geekbrains.common.transfers.messages;

import com.geekbrains.common.transfers.objects.FileStructure;

public class FileRenameOnServer extends AbstractTransferMessage {

    private FileStructure oldFilename;
    private FileStructure newFilename;

    public FileRenameOnServer(FileStructure oldFilename, FileStructure newFilename) {
        this.oldFilename = oldFilename;
        this.newFilename = newFilename;
    }

    public FileStructure getOldFilename() {
        return oldFilename;
    }

    public FileStructure getNewFilename() {
        return newFilename;
    }
}
