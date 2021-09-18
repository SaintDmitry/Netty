package netty.common.transfers.messages;

import netty.common.transfers.objects.FileStructure;

import java.util.Arrays;
import java.util.List;

public class StorageStructureResponse extends AbstractTransferMessage {

    private FileStructure[] fs;
    private FileStructure currentFolder;

    public StorageStructureResponse(List<FileStructure> files, FileStructure currentFolder) {
        this.fs = new FileStructure[files.size()];
        files.toArray(this.fs);
        this.currentFolder = currentFolder;
    }

    public List<FileStructure> getFiles() {
        return Arrays.asList(fs);
    }

    public FileStructure getCurrentFolder() {
        return currentFolder;
    }
}
