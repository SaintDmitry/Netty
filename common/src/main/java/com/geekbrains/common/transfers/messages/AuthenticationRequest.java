package com.geekbrains.common.transfers.messages;

public class AuthenticationRequest extends AbstractTransferMessage {

    private String login, pasword;

    public AuthenticationRequest(String username, String pasword) {
        this.login = username;
        this.pasword = pasword;
    }

    public String getLogin() {
        return login;
    }

    public String getPasword() {
        return pasword;
    }
}
