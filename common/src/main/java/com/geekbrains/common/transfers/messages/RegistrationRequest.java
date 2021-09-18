package com.geekbrains.common.transfers.messages;

public class RegistrationRequest extends AbstractTransferMessage {

    private String nickname, login, password;

    public RegistrationRequest(String nickname, String username, String password) {
        this.nickname = nickname;
        this.login = username;
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
