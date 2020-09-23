package org.jivesoftware.smackx.filetransfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.FromMatchesFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.bytestreams.socks5.Socks5BytestreamManager;
import org.jivesoftware.smackx.bytestreams.socks5.Socks5BytestreamRequest;
import org.jivesoftware.smackx.bytestreams.socks5.packet.Bytestream;
import org.jivesoftware.smackx.packet.StreamInitiation;

public class Socks5TransferNegotiator extends StreamNegotiator {
    private Connection connection;
    private Socks5BytestreamManager manager = Socks5BytestreamManager.getBytestreamManager(this.connection);

    private static class ByteStreamRequest extends Socks5BytestreamRequest {
        private ByteStreamRequest(Socks5BytestreamManager manager, Bytestream byteStreamRequest) {
            super(manager, byteStreamRequest);
        }
    }

    private static class BytestreamSIDFilter extends PacketTypeFilter {
        private String sessionID;

        public BytestreamSIDFilter(String sessionID) {
            super(Bytestream.class);
            if (sessionID == null) {
                throw new IllegalArgumentException("StreamID cannot be null");
            }
            this.sessionID = sessionID;
        }

        public boolean accept(Packet packet) {
            if (!super.accept(packet)) {
                return false;
            }
            Bytestream bytestream = (Bytestream) packet;
            if (this.sessionID.equals(bytestream.getSessionID()) && Type.SET.equals(bytestream.getType())) {
                return true;
            }
            return false;
        }
    }

    Socks5TransferNegotiator(Connection connection) {
        this.connection = connection;
    }

    public OutputStream createOutgoingStream(String streamID, String initiator, String target) throws XMPPException {
        try {
            return this.manager.establishSession(target, streamID).getOutputStream();
        } catch (IOException e) {
            throw new XMPPException("error establishing SOCKS5 Bytestream", e);
        } catch (InterruptedException e2) {
            throw new XMPPException("error establishing SOCKS5 Bytestream", e2);
        }
    }

    public InputStream createIncomingStream(StreamInitiation initiation) throws XMPPException, InterruptedException {
        this.manager.ignoreBytestreamRequestOnce(initiation.getSessionID());
        return negotiateIncomingStream(initiateIncomingStream(this.connection, initiation));
    }

    public PacketFilter getInitiationPacketFilter(String from, String streamID) {
        this.manager.ignoreBytestreamRequestOnce(streamID);
        return new AndFilter(new FromMatchesFilter(from), new BytestreamSIDFilter(streamID));
    }

    public String[] getNamespaces() {
        return new String[]{Socks5BytestreamManager.NAMESPACE};
    }

    /* access modifiers changed from: 0000 */
    public InputStream negotiateIncomingStream(Packet streamInitiation) throws XMPPException, InterruptedException {
        try {
            PushbackInputStream stream = new PushbackInputStream(new ByteStreamRequest(this.manager, (Bytestream) streamInitiation).accept().getInputStream());
            stream.unread(stream.read());
            return stream;
        } catch (IOException e) {
            throw new XMPPException("Error establishing input stream", e);
        }
    }

    public void cleanup() {
    }
}
