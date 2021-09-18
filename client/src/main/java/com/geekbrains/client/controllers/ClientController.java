package com.geekbrains.client.controllers;

import com.geekbrains.client.Client;
import com.geekbrains.client.Network;
import com.geekbrains.common.settings.Settings;
import com.geekbrains.common.transfers.messages.*;
import com.geekbrains.common.transfers.objects.FileStructure;

import com.sun.javafx.scene.control.skin.LabeledText;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class ClientController {

    private static final Logger log = LogManager.getLogger();

    @FXML
    private VBox rootNode;

    @FXML
    private ListView<FileStructure> clientFilesList, serverFilesList;

    @FXML
    private Label clientNicknameFilesLabel, serverFilesLabel, clientBreadcrumbsLabel, serverBreadcrumbsLabel;

    private FileStructure currentClientFolder;
    private FileStructure currentServerFolder;
    private Desktop desktop;
    private Stage loginWindow;
    private Stage registrationWindow;
    private String newFolderName;

    public FileStructure getCurrentClientFolder() {
        return currentClientFolder;
    }

    public void deleteFile(Path path) {
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
            log.error("deleteFile error: " + e);
        }
    }

    public void createNewDirectory(String newFolderPath, int step) {
        String path = newFolderPath;
        if (step > 0) {
            path = path + " (" + step + ")";
        }

        if (Files.notExists(Paths.get(path))) {
            try {
                Files.createDirectory(Paths.get(path));
            } catch (IOException e) {
                e.printStackTrace();
                log.error("createNewDirectory error: " + e);
            }
        } else {
            step++;
            createNewDirectory(newFolderPath, step);
        }
    }

    private void newDirectoryPrepare() {
        String newFolderPath = currentClientFolder.getFullFileName() + File.separator + newFolderName;
        createNewDirectory(newFolderPath, 0);
        refreshLocalFilesList(currentClientFolder);
    }

    public void onStartApp() {
        loginWindow = new Stage();
        registrationWindow = new Stage();
        loginWindowPrepare();
        registrationWindowPrepare();
        loginWindow.show();
    }

    public void authSuccessful(String nickname) {
        updateUI(() -> {
            loginWindow.close();
            showAppScene(nickname);
        });
    }

    private void showAppScene(String nickname) {
        clientNicknameFilesLabel.setText(nickname);
        serverFilesLabel.setText("Server");
        currentClientFolder = new FileStructure(Paths.get("client_storage"));
        refreshLocalFilesList(currentClientFolder);
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
        }
        setUIListeners();
        Network.sendMsg(new StorageStructureRequest(new FileStructure(Paths.get
                (Settings.commonServerStorage.getFullFileName() + File.separator + nickname))));
    }

    public void registrationSuccessful() {
        updateUI(() -> {
            registrationWindow.close();
            loginWindow.show();
        });
    }

    private void newFolderWindow(FileStructure folder) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("/fxml/newFolderScene.fxml"));
            Parent root = fxmlLoader.load();
            Scene newFolderScene = new Scene(root, 150, 65);
            Stage newFolderWindow = new Stage();

            newFolderWindow.setTitle("New folder");
            newFolderWindow.setScene(newFolderScene);
            newFolderWindow.initModality(Modality.WINDOW_MODAL);
            newFolderWindow.initOwner(rootNode.getScene().getWindow());
            newFolderWindow.setResizable(false);
            newFolderWindow.show();

            NewFolderWindowController newFolderWindowController = fxmlLoader.getController();
            TextField newFolderNameTextField = newFolderWindowController.getNewFolderNameTextField();
            newFolderNameTextField.setText("New folder");
            Label newFolderInfoLabel = newFolderWindowController.getNewFolderInfoLabel();
            newFolderInfoLabel.setText("Insert name of new folder");
            Button newFolderOKButton = newFolderWindowController.getNewFolderOKButton();

            if (folder.getFullFileName().equals(currentClientFolder.getFullFileName())) {
                newFolderOKButton.setOnAction(event -> {
                    newFolderWindow.close();
                    newFolderName = newFolderNameTextField.getText();
                    newDirectoryPrepare();
                });
            } else {
                newFolderOKButton.setOnAction(event -> {
                    newFolderWindow.close();
                    newFolderName = newFolderNameTextField.getText();
                    Network.sendMsg(new CreateNewDirectoryOnServer(currentServerFolder, newFolderName));
                    Network.sendMsg(new StorageStructureRequest(currentServerFolder));
                });
            }
            newFolderOKButton.setDefaultButton(true);

            Button newFolderCancelButton = newFolderWindowController.getNewFolderCancelButton();
            newFolderCancelButton.setOnAction(event -> {
                newFolderWindow.close();
            });

        } catch (IOException e) {
            e.printStackTrace();
            log.error("newFolderWindow error: " + e);
        }
    }

    private void renameFileOnClient(Path path) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("/fxml/renameScene.fxml"));
            Parent root = fxmlLoader.load();
            Scene renameScene = new Scene(root, 300, 30);
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
                    File newFile = new File(path.getParent().toString() + File.separator +
                            newNameTextField.getText());

                    oldFile.renameTo(newFile);

                    refreshLocalFilesList(currentClientFolder);
                });
                renameButton.setDefaultButton(true);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("renameFileOnClient error: " + e);
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
                        (Paths.get(currentServerFolder.getFullFileName() + File.separator + newNameTextField.getText()))));
            });
        } catch (IOException e) {
            e.printStackTrace();
            log.error("renameFileOnServer error: " + e);
        }
    }

    private void setUIListeners() {
        ContextMenu clientContextMenu = getClientContextMenu();
        ContextMenu serverContextMenu = getServerContextMenu();
        clientFilesListClickHandler(clientContextMenu, serverContextMenu);
        serverFilesListClickHandler(clientContextMenu, serverContextMenu);
    }

    private void uploadToServer(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                FileStructure tempServerFolder = currentServerFolder;

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Network.sendMsg(new FileResponse(dir, tempServerFolder));
                    tempServerFolder = new FileStructure(Paths.get(tempServerFolder.getFullFileName() +
                            File.separator + dir.getName(dir.getNameCount() - 1)));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Network.sendMsg(new FileResponse(file, tempServerFolder));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    tempServerFolder = new FileStructure(Paths.get(tempServerFolder.getFullFileName().
                            replace(File.separator + dir.getName(dir.getNameCount() - 1), "")));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            log.error("uploadToServer error: " + e);
        }
    }

    private ContextMenu getClientContextMenu() {
        ContextMenu clientContextMenu = new ContextMenu();
        Menu clientNew = new Menu("New");
        MenuItem createNewFolderOnClient = new MenuItem("New folder");
        createNewFolderOnClient.setOnAction(event -> {
            newFolderWindow(currentClientFolder);
        });
        clientNew.getItems().add(createNewFolderOnClient);
        clientContextMenu.getItems().add(clientNew);

        MenuItem uploadFromClient = new MenuItem("Upload");
        uploadFromClient.setOnAction(event -> {
            uploadToServer(clientFilesList.getSelectionModel().getSelectedItem().getPath());
            Network.sendMsg(new StorageStructureRequest(currentServerFolder));
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
        return clientContextMenu;
    }

    private ContextMenu getServerContextMenu() {
        ContextMenu serverContextMenu = new ContextMenu();

        Menu serverNew = new Menu("New");
        MenuItem createNewFolderOnServer = new MenuItem("New Folder");
        createNewFolderOnServer.setOnAction(event -> {
            newFolderWindow(currentServerFolder);
        });
        serverNew.getItems().add(createNewFolderOnServer);
        serverContextMenu.getItems().add(serverNew);

        MenuItem downloadFromServer = new MenuItem("Download");
        downloadFromServer.setOnAction(event -> {
            FileStructure fs = serverFilesList.getSelectionModel().getSelectedItem();
            Network.sendMsg(new FileRequest(fs, currentClientFolder));
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
        return serverContextMenu;
    }

    private void clientFilesListClickHandler(ContextMenu clientContextMenu, ContextMenu serverContextMenu) {
        clientFilesList.setOnMouseClicked(event -> {
            FileStructure fs = clientFilesList.getSelectionModel().getSelectedItem();
            hideContextMenus(clientContextMenu, serverContextMenu);
            try {
                if ((event.getTarget() instanceof ListCell && !((ListCell) event.getTarget()).isEmpty())
                        || event.getTarget() instanceof LabeledText) { // Folder or file was clicked
                    if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2 && fs != null) {
                        if (fs.isDirectory()) { // Go inside the folder
                            refreshLocalFilesList(fs);
                        } else {
                            if (desktop != null) { // Oped file
                                try {
                                    desktop.open(new File(fs.getFullFileName()));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    log.error("clientFilesListClickHandler - open file error: " + e);
                                }
                            }
                        }
                    } else if (event.getButton().equals(MouseButton.SECONDARY) && fs != null) { // Folder or file was clicked by RMB
                        for (MenuItem menuItem : clientContextMenu.getItems()) {
                            if (!menuItem.isVisible()) {
                                menuItem.setVisible(true);
                            }
                        }
                        clientContextMenu.show(clientFilesList, event.getScreenX(), event.getScreenY());
                    }
                } else if (event.getButton().equals(MouseButton.SECONDARY)) { // RMB on empty space of filesList
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
                log.error("clientFilesListClickHandler error: " + e);
            }
        });
    }

    private void serverFilesListClickHandler(ContextMenu clientContextMenu, ContextMenu serverContextMenu) {
        serverFilesList.setOnMouseClicked(event -> {
            FileStructure fs = serverFilesList.getSelectionModel().getSelectedItem();
            hideContextMenus(clientContextMenu, serverContextMenu);
            try {
                if ((event.getTarget() instanceof ListCell && ! ((ListCell) event.getTarget()).isEmpty()) ||
                        event.getTarget() instanceof LabeledText) { // Folder or file was clicked
                    if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2 && fs != null) {
                        if (fs.isDirectory()) { // Go inside the folder
                            Network.sendMsg(new StorageStructureRequest(fs));
                        } else { // Download file
                            Network.sendMsg(new FileRequest(fs, currentClientFolder));
                        }
                    } else if (event.getButton().equals(MouseButton.SECONDARY) && fs != null) { // Folder or file was clicked by RMB
                        for (MenuItem menuItem : serverContextMenu.getItems()) {
                            if (!menuItem.isVisible()) {
                                menuItem.setVisible(true);
                            }
                        }
                        serverContextMenu.show(serverFilesList, event.getScreenX(), event.getScreenY());
                    }
                } else if (event.getButton().equals(MouseButton.SECONDARY)) { // RMB on empty space of filesList
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
                log.error("serverFilesListClickHandler error: " + e);
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
            updateClientBreadcrumbsLabel(currentClientFolder.getFullFileName());
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
                log.error("refreshLocalFilesList error: " + e);
            }
        });
    }
    public void refreshServerFilesList(List<FileStructure> files, FileStructure currentServerFolder) {
        updateUI(() -> {
            serverFilesList.getItems().clear();
            files.forEach(fs -> serverFilesList.getItems().add(fs));
            this.currentServerFolder = currentServerFolder; //TODO сделать проверку и создание папки пользователя
            updateServerBreadcrumbsLabel(currentServerFolder.getFullFileName());
        });
    }

    private void loginWindowPrepare() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("/fxml/loginScene.fxml"));
            Parent root = fxmlLoader.load();
            Scene loginScene = new Scene(root, 300, 65);
            loginWindow.setTitle("Please login");
            loginWindow.setScene(loginScene);
            loginWindow.initModality(Modality.WINDOW_MODAL);
            loginWindow.initOwner(rootNode.getScene().getWindow());
            loginWindow.setResizable(false);
            loginWindow.setOnCloseRequest(event -> {
                Platform.exit();
                System.exit(0);
            });

            LoginWindowController loginWindowController = fxmlLoader.getController();
            TextField loginTextField = loginWindowController.getLoginTextField();
            TextField passwordTextField = loginWindowController.getPasswordTextField();
            Label loginInfoLabel = loginWindowController.getLLoginInfoLabel();

            loginInfoLabel.setText("Enter correct login and password");

            Button loginButton = loginWindowController.getLoginButton();
            loginButton.setOnAction(event -> {
                Network.sendMsg(new AuthenticationRequest(loginTextField.getText(), passwordTextField.getText()));
            });
            loginButton.setDefaultButton(true);

            Button goToRegistrationButton = loginWindowController.getGoToRegistrationButton();
            goToRegistrationButton.setOnAction(event -> {
                loginWindow.close();
                registrationWindow.show();
            });
        } catch (IOException e) {
            e.printStackTrace();
            log.error("loginWindowPrepare error: " + e);
        }
    }

    private void registrationWindowPrepare() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("/fxml/registrationScene.fxml"));
            Parent root = fxmlLoader.load();
            Scene registrationScene = new Scene(root, 300, 65);
            registrationWindow.setTitle("Registration");
            registrationWindow.setScene(registrationScene);
            registrationWindow.initModality(Modality.WINDOW_MODAL);
            registrationWindow.initOwner(rootNode.getScene().getWindow());
            registrationWindow.setResizable(false);
            registrationWindow.setOnCloseRequest(event -> {
                registrationWindow.close();
                loginWindow.show();
            });

            RegistrationWindowController registrationWindowController = fxmlLoader.getController();
            TextField nicknameTextFieldReg = registrationWindowController.getNicknameTextFieldReg();
            TextField loginTextFieldReg = registrationWindowController.getLoginTextFieldReg();
            TextField passwordTextFieldReg = registrationWindowController.getPasswordTextFieldReg();
            Label registrationInfoLabel = registrationWindowController.getRegistrationInfoLabel();

            registrationInfoLabel.setText("Enter nickname, login and password to registration");

            Button backToLoginButton = registrationWindowController.getBackToLoginButton();
            backToLoginButton.setOnAction(event -> {
                registrationWindow.close();
                loginWindow.show();
            });

            Button registrationButton = registrationWindowController.getRegistrationButton();
            registrationButton.setOnAction(event -> {
                Network.sendMsg(new RegistrationRequest(nicknameTextFieldReg.getText(),
                        loginTextFieldReg.getText(), passwordTextFieldReg.getText()));
            });
            registrationButton.setDefaultButton(true);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("registrationWindowPrepare error: " + e);
        }
    }

    private void updateClientBreadcrumbsLabel(String path) {
        clientBreadcrumbsLabel.setText(path);
    }

    private void updateServerBreadcrumbsLabel(String path) {
        int storageLength = Settings.commonServerStorage.getFullFileName().length() + 1;
        serverBreadcrumbsLabel.setText(path.substring(storageLength));
    }
}
