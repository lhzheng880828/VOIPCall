package net.java.sip.communicator.impl.protocol.sip.net;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import javax.net.ssl.SSLServerSocket;

public class AndroidNetworkLayer extends SslNetworkLayer {
    public static final String IN6_ADDR_ANY = "::";

    public ServerSocket createServerSocket(int port, int backlog, InetAddress bindAddress) throws IOException {
        if (bindAddress.getHostAddress().equals(IN6_ADDR_ANY)) {
            return new ServerSocket(port, backlog);
        }
        return super.createServerSocket(port, backlog, bindAddress);
    }

    public DatagramSocket createDatagramSocket(int port, InetAddress laddr) throws SocketException {
        if (!laddr.getHostAddress().equals(IN6_ADDR_ANY)) {
            return super.createDatagramSocket(port, laddr);
        }
        DatagramSocket sock = new DatagramSocket(port);
        setTrafficClass(sock);
        return sock;
    }

    public SSLServerSocket createSSLServerSocket(int port, int backlog, InetAddress bindAddress) throws IOException {
        if (bindAddress.getHostAddress().equals(IN6_ADDR_ANY)) {
            return (SSLServerSocket) getSSLServerSocketFactory().createServerSocket(port, backlog);
        }
        return super.createSSLServerSocket(port, backlog, bindAddress);
    }
}
