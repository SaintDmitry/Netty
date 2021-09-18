package netty.common.transfers.messages;

import netty.common.transfers.objects.FileStructure;

public class FileRequest extends AbstractTransferMessage {

    private FileStructure fileStructure;

    public FileRequest(FileStructure fileStructure) {
        this.fileStructure = fileStructure;
    }

    public FileStructure getFileStructure() {
        return fileStructure;
    }
}
