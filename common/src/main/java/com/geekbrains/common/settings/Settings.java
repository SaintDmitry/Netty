package com.geekbrains.common.settings;

import com.geekbrains.common.transfers.objects.FileStructure;

import java.nio.file.Paths;

public class Settings {
    public static final String SERVER_ADDRESS = "localhost";
    public static final String DATABASE = "jdbc:sqlite:server/src/main/resources/db/users.db";
    public static final int PORT = 54321;
    public static final int MESSAGE_SIZE = 2000 * 1024 * 1024;
    public static final FileStructure commonServerStorage = new FileStructure(Paths.get("server_storage"));
}
