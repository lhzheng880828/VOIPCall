package org.jivesoftware.smackx.bytestreams.socks5;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeoutException;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.util.SyncPacketSend;
import org.jivesoftware.smackx.bytestreams.socks5.packet.Bytestream;
import org.jivesoftware.smackx.bytestreams.socks5.packet.Bytestream.StreamHost;

class Socks5ClientForInitiator extends Socks5Client {
    private Connection connection;
    private String sessionID;
    private String target;

    public Socks5ClientForInitiator(StreamHost streamHost, String digest, Connection connection, String sessionID, String target) {
        super(streamHost, digest);
        this.connection = connection;
        this.sessionID = sessionID;
        this.target = target;
    }

    public Socket getSocket(int timeout) throws IOException, XMPPException, InterruptedException, TimeoutException {
        Socket socket;
        if (this.streamHost.getJID().equals(this.connection.getUser())) {
            socket = Socks5Proxy.getSocks5Proxy().getSocket(this.digest);
            if (socket == null) {
                throw new XMPPException("target is not connected to SOCKS5 proxy");
            }
        }
        socket = super.getSocket(timeout);
        try {
            activate();
        } catch (XMPPException e) {
            socket.close();
            throw new XMPPException("activating SOCKS5 Bytestream failed", e);
        }
        return socket;
    }

    private void activate() throws XMPPException {
        SyncPacketSend.getReply(this.connection, createStreamHostActivation());
    }

    private Bytestream createStreamHostActivation() {
        Bytestream activate = new Bytestream(this.sessionID);
        activate.setMode(null);
        activate.setType(Type.SET);
        activate.setTo(this.streamHost.getJID());
        activate.setToActivate(this.target);
        return activate;
    }
}
