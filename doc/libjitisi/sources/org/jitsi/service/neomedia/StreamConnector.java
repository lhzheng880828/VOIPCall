package org.jitsi.service.neomedia;

import java.net.DatagramSocket;
import java.net.Socket;

public interface StreamConnector {

    public enum Protocol {
        UDP,
        TCP
    }

    void close();

    DatagramSocket getControlSocket();

    Socket getControlTCPSocket();

    DatagramSocket getDataSocket();

    Socket getDataTCPSocket();

    Protocol getProtocol();

    void started();

    void stopped();
}
