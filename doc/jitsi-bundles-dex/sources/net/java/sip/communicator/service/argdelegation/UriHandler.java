package net.java.sip.communicator.service.argdelegation;

public interface UriHandler {
    public static final String PROTOCOL_PROPERTY = "ProtocolName";

    String getProtocol();

    void handleUri(String str);
}
