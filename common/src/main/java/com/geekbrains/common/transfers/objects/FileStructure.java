package com.geekbrains.common.transfers.objects;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileStructure implements Serializable {
    private String parent;
    private String name;
    private boolean isDirectory;
    private String toString;

    public FileStructure(Path path) {
        if(path.getParent() != null) {
            this.parent = path.getParent().toString();
        }
        this.name = path.getFileName().toString();
        this.isDirectory = path.toFile().isDirectory();
    }

    public FileStructure(Path path, String toString) {
        this(path);
        this.toString = toString;
    }

    public String getFullFileName() {
        return parent == null ? name : parent + File.separator + name;
    }

    public Path getPath() {
        return Paths.get(getFullFileName());
    }

    public String getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    @Override
    public String toString() {
        return toString == null ? name : toString;
    }
}
