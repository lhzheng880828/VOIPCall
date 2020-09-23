package net.java.sip.communicator.impl.protocol.sip.net;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import net.java.sip.communicator.impl.protocol.sip.ProtocolProviderServiceSipImpl;
import net.java.sip.communicator.impl.protocol.sip.SipActivator;
import net.java.sip.communicator.service.certificate.CertificateService;
import net.java.sip.communicator.service.protocol.sip.SipAccountID;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.net.NetworkLayer;
import org.osgi.framework.ServiceReference;

public class SslNetworkLayer implements NetworkLayer {
    private static final String SIP_DSCP_PROPERTY = "net.java.sip.communicator.impl.protocol.SIP_DSCP";
    private static final Logger logger = Logger.getLogger(SslNetworkLayer.class);
    private CertificateService certificateVerification = null;

    public SslNetworkLayer() {
        ServiceReference guiVerifyReference = SipActivator.getBundleContext().getServiceReference(CertificateService.class.getName());
        if (guiVerifyReference != null) {
            this.certificateVerification = (CertificateService) SipActivator.getBundleContext().getService(guiVerifyReference);
        }
    }

    public ServerSocket createServerSocket(int port, int backlog, InetAddress bindAddress) throws IOException {
        return new ServerSocket(port, backlog, bindAddress);
    }

    public Socket createSocket(InetAddress address, int port) throws IOException {
        Socket sock = new Socket(address, port);
        setTrafficClass(sock);
        return sock;
    }

    public DatagramSocket createDatagramSocket() throws SocketException {
        DatagramSocket sock = new DatagramSocket();
        setTrafficClass(sock);
        return sock;
    }

    public DatagramSocket createDatagramSocket(int port, InetAddress laddr) throws SocketException {
        DatagramSocket sock = new DatagramSocket(port, laddr);
        setTrafficClass(sock);
        return sock;
    }

    public SSLServerSocket createSSLServerSocket(int port, int backlog, InetAddress bindAddress) throws IOException {
        return (SSLServerSocket) getSSLServerSocketFactory().createServerSocket(port, backlog, bindAddress);
    }

    /* access modifiers changed from: protected */
    public SSLServerSocketFactory getSSLServerSocketFactory() throws IOException {
        try {
            return this.certificateVerification.getSSLContext().getServerSocketFactory();
        } catch (GeneralSecurityException e) {
            throw new IOException(e.getMessage());
        }
    }

    private SSLSocketFactory getSSLSocketFactory(InetAddress address) throws IOException {
        ProtocolProviderServiceSipImpl provider = null;
        for (ProtocolProviderServiceSipImpl pps : ProtocolProviderServiceSipImpl.getAllInstances()) {
            if (pps.getConnection() != null && pps.getConnection().isSameInetAddress(address)) {
                provider = pps;
                break;
            }
        }
        if (provider == null) {
            throw new IOException("The provider that requested the SSL Socket could not be found");
        }
        try {
            ArrayList<String> identities = new ArrayList(2);
            SipAccountID id = (SipAccountID) provider.getAccountID();
            if (!id.getAccountPropertyBoolean("PROXY_AUTO_CONFIG", false)) {
                String proxy = id.getAccountPropertyString("PROXY_ADDRESS");
                if (proxy != null) {
                    identities.add(proxy);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Added <" + proxy + "> to list of valid SIP TLS server identities.");
                }
            }
            String userID = id.getAccountPropertyString("USER_ID");
            int index = userID.indexOf(64);
            if (index > -1) {
                identities.add(userID.substring(index + 1));
                if (logger.isDebugEnabled()) {
                    logger.debug("Added <" + userID.substring(index + 1) + "> to list of valid SIP TLS server identities.");
                }
            }
            return this.certificateVerification.getSSLContext(id.getAccountPropertyString("CLIENT_TLS_CERTIFICATE"), this.certificateVerification.getTrustManager(identities, null, new RFC5922Matcher(provider))).getSocketFactory();
        } catch (GeneralSecurityException e) {
            throw new IOException(e.getMessage());
        }
    }

    public SSLSocket createSSLSocket(InetAddress address, int port) throws IOException {
        Socket sock = (SSLSocket) getSSLSocketFactory(address).createSocket(address, port);
        setTrafficClass(sock);
        return sock;
    }

    public SSLSocket createSSLSocket(InetAddress address, int port, InetAddress myAddress) throws IOException {
        Socket sock = (SSLSocket) getSSLSocketFactory(address).createSocket(address, port, myAddress, 0);
        setTrafficClass(sock);
        return sock;
    }

    public Socket createSocket(InetAddress address, int port, InetAddress myAddress) throws IOException {
        Socket sock;
        if (myAddress != null) {
            sock = new Socket(address, port, myAddress, 0);
        } else {
            sock = new Socket(address, port);
        }
        setTrafficClass(sock);
        return sock;
    }

    public Socket createSocket(InetAddress address, int port, InetAddress myAddress, int myPort) throws IOException {
        Socket sock;
        if (myAddress != null) {
            sock = new Socket(address, port, myAddress, myPort);
        } else if (port != 0) {
            sock = new Socket();
            sock.bind(new InetSocketAddress(port));
            sock.connect(new InetSocketAddress(address, port));
        } else {
            sock = new Socket(address, port);
        }
        setTrafficClass(sock);
        return sock;
    }

    /* access modifiers changed from: protected */
    public void setTrafficClass(Socket s) {
        try {
            s.setTrafficClass(getDSCP());
        } catch (SocketException e) {
            logger.warn("Failed to set traffic class on Socket", e);
        }
    }

    /* access modifiers changed from: protected */
    public void setTrafficClass(DatagramSocket s) {
        try {
            s.setTrafficClass(getDSCP());
        } catch (SocketException e) {
            logger.warn("Failed to set traffic class on DatagramSocket", e);
        }
    }

    private int getDSCP() {
        String dscp = (String) SipActivator.getConfigurationService().getProperty(SIP_DSCP_PROPERTY);
        if (dscp != null) {
            return Integer.parseInt(dscp) << 2;
        }
        return 0;
    }
}
