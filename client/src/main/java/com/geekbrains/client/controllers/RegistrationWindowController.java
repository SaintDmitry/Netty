package com.geekbrains.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class RegistrationWindowController {

    @FXML
    private HBox registerNode;

    @FXML
    private TextField nicknameTextFieldReg, loginTextFieldReg, passwordTextFieldReg;

    @FXML
    private Button backToLoginButton, registrationButton;

    @FXML
    private Label registrationInfoLabel;

    public HBox getRegisterNode() {
        return registerNode;
    }

    public TextField getNicknameTextFieldReg() {
        return nicknameTextFieldReg;
    }

    public TextField getLoginTextFieldReg() {
        return loginTextFieldReg;
    }

    public TextField getPasswordTextFieldReg() {
        return passwordTextFieldReg;
    }

    public Button getBackToLoginButton() {
        return backToLoginButton;
    }

    public Button getRegistrationButton() {
        return registrationButton;
    }

    public Label getRegistrationInfoLabel() {
        return registrationInfoLabel;
    }
}
