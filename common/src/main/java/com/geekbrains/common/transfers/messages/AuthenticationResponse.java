package com.geekbrains.common.transfers.messages;

public class AuthenticationResponse extends AbstractTransferMessage {
    private boolean authenticated;
    private String nickname;

    public AuthenticationResponse(boolean autenticated, String nickname) {
        this.authenticated = autenticated;
        this.nickname = nickname;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String getNickname() {
        return nickname;
    }
}
