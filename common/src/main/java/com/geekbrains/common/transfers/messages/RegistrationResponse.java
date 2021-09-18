package com.geekbrains.common.transfers.messages;

public class RegistrationResponse extends AbstractTransferMessage {

    private boolean registrated;

    public RegistrationResponse(boolean registrated) {
        this.registrated = registrated;
    }

    public boolean isRegistrated() {
        return registrated;
    }
}
