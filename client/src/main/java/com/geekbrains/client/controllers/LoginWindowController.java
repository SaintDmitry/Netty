package com.geekbrains.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;


public class LoginWindowController {

    @FXML
    private HBox loginNode;

    @FXML
    private TextField loginTextField, passwordTextField;

    @FXML
    private Button loginButton, goToRegistrationButton;

    @FXML
    private Label loginInfoLabel;

    public HBox getLoginNode() {
        return loginNode;
    }

    public TextField getLoginTextField() {
        return loginTextField;
    }

    public TextField getPasswordTextField() {
        return passwordTextField;
    }

    public Button getLoginButton() {
        return loginButton;
    }

    public Button getGoToRegistrationButton() {
        return goToRegistrationButton;
    }

    public Label getLLoginInfoLabel() {
        return loginInfoLabel;
    }
}
