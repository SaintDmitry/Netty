package netty.common.transfers.messages;

import netty.common.transfers.objects.FileStructure;

public class CreateNewDirectoryOnServer extends AbstractTransferMessage {

    private FileStructure currentServerFolder;

    public CreateNewDirectoryOnServer(FileStructure currentServerFolder) {
        this.currentServerFolder = currentServerFolder;
    }

    public FileStructure getCurrentServerFolder() {
        return currentServerFolder;
    }
}
