package netty.common.settings;

import netty.common.transfers.objects.FileStructure;

import java.nio.file.Paths;

public class Settings {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 54321;
    private static final int MESSAGE_SIZE = 2000 * 1024 * 1024;
    private static final FileStructure serverStorage = new FileStructure(Paths.get("server_storage"));


    public static String getServerAdress() {
        return SERVER_ADDRESS;
    }

    public static int getPort() {
        return PORT;
    }

    public static int getMessageSize() {
        return MESSAGE_SIZE;
    }

    public static FileStructure getServerStorage() {
        return serverStorage;
    }
}
