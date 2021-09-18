package netty.common.transfers.messages;

import netty.common.transfers.objects.FileStructure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileResponse extends AbstractTransferMessage {

    private FileStructure fileStructure;
    private byte[] data;

    public FileResponse(Path path) throws IOException {
        fileStructure = new FileStructure(path);
        data = Files.readAllBytes(path);
    }

    public FileStructure getFileStructure() {
        return fileStructure;
    }

    public byte[] getData() {
        return data;
    }
}
