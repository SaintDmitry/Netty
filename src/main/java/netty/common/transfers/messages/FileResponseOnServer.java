package netty.common.transfers.messages;

import netty.common.transfers.objects.FileStructure;

import java.io.IOException;
import java.nio.file.Path;

public class FileResponseOnServer extends FileResponse {

    private FileStructure serverFolder;

    public FileResponseOnServer(Path path, FileStructure serverFolder) throws IOException {
        super(path);
        this.serverFolder = serverFolder;
    }

    public FileStructure getServerFolder() {
        return serverFolder;
    }
}
