package org.jivesoftware.smackx.filetransfer;

import java.io.InputStream;
import java.io.OutputStream;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.FromContainsFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager;
import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamRequest;
import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamSession;
import org.jivesoftware.smackx.bytestreams.ibb.packet.Open;
import org.jivesoftware.smackx.packet.StreamInitiation;

public class IBBTransferNegotiator extends StreamNegotiator {
    private Connection connection;
    private InBandBytestreamManager manager;

    private static class ByteStreamRequest extends InBandBytestreamRequest {
        private ByteStreamRequest(InBandBytestreamManager manager, Open byteStreamRequest) {
            super(manager, byteStreamRequest);
        }
    }

    private static class IBBOpenSidFilter extends PacketTypeFilter {
        private String sessionID;

        public IBBOpenSidFilter(String sessionID) {
            super(Open.class);
            if (sessionID == null) {
                throw new IllegalArgumentException("StreamID cannot be null");
            }
            this.sessionID = sessionID;
        }

        public boolean accept(Packet packet) {
            if (!super.accept(packet)) {
                return false;
            }
            Open bytestream = (Open) packet;
            if (this.sessionID.equals(bytestream.getSessionID()) && Type.SET.equals(bytestream.getType())) {
                return true;
            }
            return false;
        }
    }

    protected IBBTransferNegotiator(Connection connection) {
        this.connection = connection;
        this.manager = InBandBytestreamManager.getByteStreamManager(connection);
    }

    public OutputStream createOutgoingStream(String streamID, String initiator, String target) throws XMPPException {
        InBandBytestreamSession session = this.manager.establishSession(target, streamID);
        session.setCloseBothStreamsEnabled(true);
        return session.getOutputStream();
    }

    public InputStream createIncomingStream(StreamInitiation initiation) throws XMPPException {
        this.manager.ignoreBytestreamRequestOnce(initiation.getSessionID());
        return negotiateIncomingStream(initiateIncomingStream(this.connection, initiation));
    }

    public PacketFilter getInitiationPacketFilter(String from, String streamID) {
        this.manager.ignoreBytestreamRequestOnce(streamID);
        return new AndFilter(new FromContainsFilter(from), new IBBOpenSidFilter(streamID));
    }

    public String[] getNamespaces() {
        return new String[]{InBandBytestreamManager.NAMESPACE};
    }

    /* access modifiers changed from: 0000 */
    public InputStream negotiateIncomingStream(Packet streamInitiation) throws XMPPException {
        InBandBytestreamSession session = new ByteStreamRequest(this.manager, (Open) streamInitiation).accept();
        session.setCloseBothStreamsEnabled(true);
        return session.getInputStream();
    }

    public void cleanup() {
    }
}
