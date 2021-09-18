package netty.client.controllers;

import com.sun.javafx.scene.control.skin.LabeledText;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import netty.client.Client;
import netty.client.Network;
import netty.common.transfers.messages.*;
import netty.common.transfers.objects.FileStructure;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class ClientController {

    @FXML
    private VBox rootNode;

    @FXML
    private ListView<FileStructure> clientFilesList, serverFilesList;

    private FileStructure currentClientFolder;
    private FileStructure currentServerFolder;
    private Desktop desktop;

    public FileStructure getCurrentClientFolder() {
        return currentClientFolder;
    }

    private void deleteFile(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createNewDirectory(String newFolderPath, int step) {
        String path = newFolderPath;
        if (step > 0) {
            path = path + " (" + step + ")";
        }

        if (Files.notExists(Paths.get(path))) {
            try {
                Files.createDirectory(Paths.get(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            step++;
            createNewDirectory(newFolderPath, step);
        }
    }

    private void renameFileOnClient(Path path) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("/fxml/renameScene.fxml"));
            Parent root = fxmlLoader.load();
            Scene renameScene = new Scene(root, 300, 15);
            Stage renameWindow = new Stage();
            renameWindow.setTitle("Enter new filename");
            renameWindow.setScene(renameScene);
            renameWindow.initModality(Modality.WINDOW_MODAL);
            renameWindow.initOwner(rootNode.getScene().getWindow());
            renameWindow.setResizable(false);
            renameWindow.show();

            RenameWindowController renameWindowController = fxmlLoader.getController();
            TextField newNameTextField = renameWindowController.getNewNameTextField();
            newNameTextField.setText(path.getFileName().toString());

            Button renameButton = renameWindowController.getRenameButton();
            renameButton.setOnAction(event -> {
                renameWindow.close();
                File oldFile = path.toFile();
                File newFile = new File(path.getParent().toString() + "\\" + newNameTextField.getText());

                oldFile.renameTo(newFile);

                refreshLocalFilesList(currentClientFolder);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void renameFileOnServer(FileStructure oldFile) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("/fxml/renameScene.fxml"));
            Parent root = fxmlLoader.load();
            Scene renameScene = new Scene(root, 300, 15);
            Stage renameWindow = new Stage();
            renameWindow.setTitle("Enter new filename");
            renameWindow.setScene(renameScene);
            renameWindow.initModality(Modality.WINDOW_MODAL);
            renameWindow.initOwner(rootNode.getScene().getWindow());
            renameWindow.setResizable(false);
            renameWindow.show();

            RenameWindowController renameWindowController = fxmlLoader.getController();
            TextField newNameTextField = renameWindowController.getNewNameTextField();
            newNameTextField.setText(oldFile.getName());

            Button renameButton = renameWindowController.getRenameButton();
            renameButton.setOnAction(event -> {
                renameWindow.close();
                Network.sendMsg(new FileRenameOnServer(oldFile, new FileStructure
                        (Paths.get(currentServerFolder.getFullFileName() + "\\" + newNameTextField.getText()))));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onStartApp() {
        currentClientFolder = new FileStructure(Paths.get("client_storage"));
        refreshLocalFilesList(currentClientFolder);
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
        }
        setUIListeners();
    }

    private void setUIListeners() {
        ContextMenu clientContextMenu = new ContextMenu();
        Menu clientNew = new Menu("New");
        MenuItem createNewFolderOnClient = new MenuItem("New folder");
        createNewFolderOnClient.setOnAction(event -> {
            String newFolderPath = currentClientFolder + "\\New folder";
            createNewDirectory(newFolderPath, 0);
            refreshLocalFilesList(currentClientFolder);
        });
        clientNew.getItems().add(createNewFolderOnClient);
        clientContextMenu.getItems().add(clientNew);

        MenuItem uploadFromClient = new MenuItem("Upload");
        uploadFromClient.setOnAction(event -> {
            FileStructure fs = clientFilesList.getSelectionModel().getSelectedItem();
            try {
                Network.sendMsg(new FileResponseOnServer(fs.getPath(), currentServerFolder));
            } catch (IOException e) {
                e.printStackTrace();
                refreshLocalFilesList(currentClientFolder);
            }
        });
        uploadFromClient.setUserData(Boolean.TRUE);
        clientContextMenu.getItems().add(uploadFromClient);

        MenuItem renameClientFile = new MenuItem("Rename");
        renameClientFile.setOnAction(event -> {
            FileStructure fs = clientFilesList.getSelectionModel().getSelectedItem();
            renameFileOnClient(fs.getPath());
            refreshLocalFilesList(currentClientFolder);
        });
        renameClientFile.setUserData(Boolean.TRUE);
        clientContextMenu.getItems().add(renameClientFile);

        MenuItem deleteFromClient = new MenuItem("Delete");
        deleteFromClient.setOnAction(event -> {
            FileStructure fs = clientFilesList.getSelectionModel().getSelectedItem();
            deleteFile(fs.getPath());
            refreshLocalFilesList(currentClientFolder);
        });
        deleteFromClient.setUserData(Boolean.TRUE);
        clientContextMenu.getItems().add(deleteFromClient);

        MenuItem refreshClientFiles = new MenuItem("Refresh");
        refreshClientFiles.setOnAction(event -> {
            refreshLocalFilesList(currentClientFolder);
        });
        clientContextMenu.getItems().add(refreshClientFiles);

        //_________________________________________________SERVER________________________________________________
        ContextMenu serverContextMenu = new ContextMenu();

        Menu serverNew = new Menu("New");
        MenuItem createNewFolderOnServer = new MenuItem("New Folder");
        createNewFolderOnServer.setOnAction(event -> {
            Network.sendMsg(new CreateNewDirectoryOnServer(currentServerFolder));
        });
        serverNew.getItems().add(createNewFolderOnServer);
        serverContextMenu.getItems().add(serverNew);

        MenuItem downloadFromServer = new MenuItem("Download");
        downloadFromServer.setOnAction(event -> {
            FileStructure fs = serverFilesList.getSelectionModel().getSelectedItem();
            Network.sendMsg(new FileRequest(fs));
        });
        downloadFromServer.setUserData(Boolean.TRUE);
        serverContextMenu.getItems().add(downloadFromServer);

        MenuItem renameFileOnServer = new MenuItem("Rename");
        renameFileOnServer.setOnAction(event -> {
            FileStructure oldFile = serverFilesList.getSelectionModel().getSelectedItem();
            renameFileOnServer(oldFile);
        });
        renameFileOnServer.setUserData(Boolean.TRUE);
        serverContextMenu.getItems().add(renameFileOnServer);

        MenuItem deleteFromServer = new MenuItem("Delete");
        deleteFromServer.setOnAction(event -> {
            FileStructure fs = serverFilesList.getSelectionModel().getSelectedItem();
            Network.sendMsg(new FileRemoveFromServer(fs));
        });
        deleteFromServer.setUserData(Boolean.TRUE);
        serverContextMenu.getItems().add(deleteFromServer);

        MenuItem refreshServerFiles = new MenuItem("Refresh");
        refreshServerFiles.setOnAction(event -> {
            Network.sendMsg(new StorageStructureRequest(currentServerFolder));
        });
        serverContextMenu.getItems().add(refreshServerFiles);

        //_____________________________Client_file_clicked____________________________________________________________
        clientFilesList.setOnMouseClicked(event -> {
            FileStructure fs = clientFilesList.getSelectionModel().getSelectedItem();
            hideContextMenus(clientContextMenu, serverContextMenu);
            try {
                if ((event.getTarget() instanceof ListCell && !((ListCell) event.getTarget()).isEmpty())
                        || event.getTarget() instanceof LabeledText) { //Клик по файлу/папке
                    if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2 && fs != null) {
                        if (fs.isFolder()) { //Проваливаемся в папку
                            refreshLocalFilesList(fs);
                        } else {
                            if (desktop != null) { //Открываем файл
                                try {
                                    desktop.open(new File(fs.getFullFileName()));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else if (event.getButton().equals(MouseButton.SECONDARY) && fs != null) { //ПКМ по файлу/папке
                        for (MenuItem menuItem : clientContextMenu.getItems()) {
                            if (!menuItem.isVisible()) {
                                menuItem.setVisible(true);
                            }
                        }
                        clientContextMenu.show(clientFilesList, event.getScreenX(), event.getScreenY());
                    }
                } else if (event.getButton().equals(MouseButton.SECONDARY)) { //ПКМ не по файлу/папке
                    for (MenuItem menuItem : clientContextMenu.getItems()) {
                        if (menuItem.isVisible() && (menuItem.getUserData() !=
                                null && menuItem.getUserData().equals(Boolean.TRUE))) {
                            menuItem.setVisible(false);
                        }
                    }
                    clientContextMenu.show(clientFilesList, event.getScreenX(), event.getScreenY());
                }

            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        });
        //_____________________________Server_file_clicked____________________________________________________________
        serverFilesList.setOnMouseClicked(event -> {
            FileStructure fs = serverFilesList.getSelectionModel().getSelectedItem();
            hideContextMenus(clientContextMenu, serverContextMenu);
            try {
                if ((event.getTarget() instanceof ListCell && ! ((ListCell) event.getTarget()).isEmpty()) ||
                event.getTarget() instanceof LabeledText) { //Клик по файлу/папке
                    if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2 && fs != null) {
                        if (fs.isFolder()) { //Если папка - открываем содержимое
                            Network.sendMsg(new StorageStructureRequest(fs));
                        } else { //Если файл - скачиваем
                            Network.sendMsg(new FileRequest(fs));
                        }
                    } else if (event.getButton().equals(MouseButton.SECONDARY) && fs != null) { //ПКМ по файлу/папке
                        for (MenuItem menuItem : serverContextMenu.getItems()) {
                            if (!menuItem.isVisible()) {
                                menuItem.setVisible(true);
                            }
                        }
                        serverContextMenu.show(serverFilesList, event.getScreenX(), event.getScreenY());
                    }
                } else if (event.getButton().equals(MouseButton.SECONDARY)) { //ПКМ не по файлу/папке
                    for (MenuItem menuItem : serverContextMenu.getItems()) {
                        if (menuItem.isVisible() && (menuItem.getUserData() != null
                                && menuItem.getUserData().equals(Boolean.TRUE))) {
                            menuItem.setVisible(false);
                        }
                    }
                    serverContextMenu.show(serverFilesList, event.getScreenX(), event.getScreenY());
                }
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        });
    }

    private void hideContextMenus(ContextMenu... contextMenus) {
        for (ContextMenu contextMenu : contextMenus) {
            if (contextMenu.isShowing()) {
                contextMenu.hide();
            }
        }
    }

    private static void updateUI(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }

    public void refreshLocalFilesList(FileStructure folder) {
        updateUI(() -> {
            currentClientFolder = folder;
            clientFilesList.getItems().clear();
            if (folder.getParent() != null) {
                clientFilesList.getItems().add(new FileStructure(Paths.get(folder.getParent()), "..."));
            }
            try {
                Files.walk(folder.getPath(), 1)
                        .forEach(path -> {
                            if (!folder.getFullFileName().equals(path.toString())) {
                                clientFilesList.getItems().add(new FileStructure(path));
                            }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    public void refreshServerFilesList(List<FileStructure> files, FileStructure currentServerFolder) {
        updateUI(() -> {
            serverFilesList.getItems().clear();
            files.forEach(fs -> serverFilesList.getItems().add(fs));
            this.currentServerFolder = currentServerFolder;
        });
    }

}
