package netty.common.transfers.messages;

import netty.common.transfers.objects.FileStructure;

public class StorageStructureRequest extends AbstractTransferMessage {

    private FileStructure fileStructure;

    public StorageStructureRequest(FileStructure fileStructure) {
        this.fileStructure = fileStructure;
    }

    public FileStructure getFileStructure() {
        return fileStructure;
    }
}
