package org.jitsi.service.neomedia;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.neomedia.StreamConnector.Protocol;
import org.jitsi.util.Logger;

public class DefaultStreamConnector implements StreamConnector {
    public static final int BIND_RETRIES_DEFAULT_VALUE = 50;
    public static final String BIND_RETRIES_PROPERTY_NAME = "net.java.sip.communicator.service.media.BIND_RETRIES";
    public static final String MAX_PORT_NUMBER_PROPERTY_NAME = "net.java.sip.communicator.service.media.MAX_PORT_NUMBER";
    public static final String MIN_PORT_NUMBER_PROPERTY_NAME = "net.java.sip.communicator.service.media.MIN_PORT_NUMBER";
    private static final Logger logger = Logger.getLogger(DefaultStreamConnector.class);
    private static int maxPort = -1;
    private static int minPort = -1;
    private final InetAddress bindAddr;
    protected DatagramSocket controlSocket;
    protected DatagramSocket dataSocket;

    public DefaultStreamConnector() {
        this(null, null);
    }

    private static synchronized DatagramSocket createDatagramSocket(InetAddress bindAddr) {
        DatagramSocket datagramSocket;
        synchronized (DefaultStreamConnector.class) {
            ConfigurationService cfg = LibJitsi.getConfigurationService();
            int bindRetries = 50;
            if (cfg != null) {
                bindRetries = cfg.getInt(BIND_RETRIES_PROPERTY_NAME, 50);
            }
            if (maxPort < 0) {
                maxPort = 6000;
                if (cfg != null) {
                    maxPort = cfg.getInt(MAX_PORT_NUMBER_PROPERTY_NAME, maxPort);
                }
            }
            int i = 0;
            while (i < bindRetries) {
                if (minPort < 0 || minPort > maxPort) {
                    minPort = 5000;
                    if (cfg != null) {
                        minPort = cfg.getInt(MIN_PORT_NUMBER_PROPERTY_NAME, minPort);
                    }
                }
                int port = minPort;
                minPort = port + 1;
                if (bindAddr != null) {
                    datagramSocket = new DatagramSocket(port, bindAddr);
                    break;
                }
                try {
                    datagramSocket = new DatagramSocket(port);
                    break;
                } catch (SocketException se) {
                    logger.warn("Retrying a bind because of a failure to bind to address " + bindAddr + " and port " + port, se);
                    i++;
                }
            }
            datagramSocket = null;
        }
        return datagramSocket;
    }

    public DefaultStreamConnector(InetAddress bindAddr) {
        this.bindAddr = bindAddr;
    }

    public DefaultStreamConnector(DatagramSocket dataSocket, DatagramSocket controlSocket) {
        this.controlSocket = controlSocket;
        this.dataSocket = dataSocket;
        this.bindAddr = null;
    }

    public void close() {
        if (this.controlSocket != null) {
            this.controlSocket.close();
        }
        if (this.dataSocket != null) {
            this.dataSocket.close();
        }
    }

    public DatagramSocket getControlSocket() {
        if (this.controlSocket == null && this.bindAddr != null) {
            this.controlSocket = createDatagramSocket(this.bindAddr);
        }
        return this.controlSocket;
    }

    public DatagramSocket getDataSocket() {
        if (this.dataSocket == null && this.bindAddr != null) {
            this.dataSocket = createDatagramSocket(this.bindAddr);
        }
        return this.dataSocket;
    }

    public Socket getDataTCPSocket() {
        return null;
    }

    public Socket getControlTCPSocket() {
        return null;
    }

    public Protocol getProtocol() {
        return Protocol.UDP;
    }

    public void started() {
    }

    public void stopped() {
    }
}
