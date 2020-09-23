package net.java.sip.communicator.service.googlecontacts;

public interface GoogleContactsConnection {

    public enum ConnectionStatus {
        ERROR_INVALID_CREDENTIALS,
        ERROR_UNKNOWN,
        SUCCESS
    }

    ConnectionStatus connect();

    String getLogin();

    String getPassword();

    String getPrefix();

    void setLogin(String str);

    void setPassword(String str);
}
