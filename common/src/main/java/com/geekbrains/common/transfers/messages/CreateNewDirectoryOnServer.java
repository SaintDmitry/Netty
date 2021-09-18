package com.geekbrains.common.transfers.messages;

import com.geekbrains.common.transfers.objects.FileStructure;

public class CreateNewDirectoryOnServer extends AbstractTransferMessage {

    private FileStructure currentServerFolder;
    private String newFolderName;

    public CreateNewDirectoryOnServer(FileStructure currentServerFolder, String newFolderName) {
        this.currentServerFolder = currentServerFolder;
        this.newFolderName = newFolderName;
    }

    public FileStructure getCurrentServerFolder() {
        return currentServerFolder;
    }

    public String getNewFolderName() {
        return newFolderName;
    }
}
