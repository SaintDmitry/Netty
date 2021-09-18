package com.geekbrains.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class NewFolderWindowController {

    @FXML
    private HBox newFolderNode;

    @FXML
    private Label newFolderInfoLabel;

    @FXML
    private TextField newFolderNameTextField;

    @FXML
    private Button newFolderOKButton, newFolderCancelButton;

    public HBox getNewFolderNode() {
        return newFolderNode;
    }

    public Label getNewFolderInfoLabel() {
        return newFolderInfoLabel;
    }

    public TextField getNewFolderNameTextField() {
        return newFolderNameTextField;
    }

    public Button getNewFolderOKButton() {
        return newFolderOKButton;
    }

    public Button getNewFolderCancelButton() {
        return newFolderCancelButton;
    }
}
