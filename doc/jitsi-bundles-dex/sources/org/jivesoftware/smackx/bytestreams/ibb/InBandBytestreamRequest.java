package org.jivesoftware.smackx.bytestreams.ibb;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smackx.bytestreams.BytestreamRequest;
import org.jivesoftware.smackx.bytestreams.ibb.packet.Open;

public class InBandBytestreamRequest implements BytestreamRequest {
    private final Open byteStreamRequest;
    private final InBandBytestreamManager manager;

    protected InBandBytestreamRequest(InBandBytestreamManager manager, Open byteStreamRequest) {
        this.manager = manager;
        this.byteStreamRequest = byteStreamRequest;
    }

    public String getFrom() {
        return this.byteStreamRequest.getFrom();
    }

    public String getSessionID() {
        return this.byteStreamRequest.getSessionID();
    }

    public InBandBytestreamSession accept() throws XMPPException {
        Connection connection = this.manager.getConnection();
        InBandBytestreamSession ibbSession = new InBandBytestreamSession(connection, this.byteStreamRequest, this.byteStreamRequest.getFrom());
        this.manager.getSessions().put(this.byteStreamRequest.getSessionID(), ibbSession);
        connection.sendPacket(IQ.createResultIQ(this.byteStreamRequest));
        return ibbSession;
    }

    public void reject() {
        this.manager.replyRejectPacket(this.byteStreamRequest);
    }
}
