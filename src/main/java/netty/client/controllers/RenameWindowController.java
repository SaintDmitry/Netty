package netty.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

public class RenameWindowController {

    @FXML
    private HBox renameNode;

    @FXML
    private TextField newNameTextField;

    @FXML
    private Button renameButton;

    public HBox getRenameNode() {
        return renameNode;
    }

    public TextField getNewNameTextField() {
        return newNameTextField;
    }

    public Button getRenameButton() {
        return renameButton;
    }
}
