package netty.common.transfers.messages;

import netty.common.transfers.objects.FileStructure;

public class FileRemoveFromServer extends AbstractTransferMessage {

    private FileStructure fileStructure;

    public FileRemoveFromServer(FileStructure fileStructure) {
        this.fileStructure = fileStructure;
    }

    public FileStructure getFileStructure() {
        return fileStructure;
    }
}
