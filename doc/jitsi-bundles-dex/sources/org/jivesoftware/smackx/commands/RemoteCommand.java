package org.jivesoftware.smackx.commands;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.commands.AdHocCommand.Action;
import org.jivesoftware.smackx.packet.AdHocCommandData;

public class RemoteCommand extends AdHocCommand {
    private Connection connection;
    private String jid;
    private long packetReplyTimeout = ((long) SmackConfiguration.getPacketReplyTimeout());
    private String sessionID;

    protected RemoteCommand(Connection connection, String node, String jid) {
        this.connection = connection;
        this.jid = jid;
        setNode(node);
    }

    public void cancel() throws XMPPException {
        executeAction(Action.cancel, this.packetReplyTimeout);
    }

    public void complete(Form form) throws XMPPException {
        executeAction(Action.complete, form, this.packetReplyTimeout);
    }

    public void execute() throws XMPPException {
        executeAction(Action.execute, this.packetReplyTimeout);
    }

    public void execute(Form form) throws XMPPException {
        executeAction(Action.execute, form, this.packetReplyTimeout);
    }

    public void next(Form form) throws XMPPException {
        executeAction(Action.next, form, this.packetReplyTimeout);
    }

    public void prev() throws XMPPException {
        executeAction(Action.prev, this.packetReplyTimeout);
    }

    private void executeAction(Action action, long packetReplyTimeout) throws XMPPException {
        executeAction(action, null, packetReplyTimeout);
    }

    private void executeAction(Action action, Form form, long timeout) throws XMPPException {
        AdHocCommandData data = new AdHocCommandData();
        data.setType(Type.SET);
        data.setTo(getOwnerJID());
        data.setNode(getNode());
        data.setSessionID(this.sessionID);
        data.setAction(action);
        if (form != null) {
            data.setForm(form.getDataFormToSend());
        }
        PacketCollector collector = this.connection.createPacketCollector(new PacketIDFilter(data.getPacketID()));
        this.connection.sendPacket(data);
        Packet response = collector.nextResult(timeout);
        collector.cancel();
        if (response == null) {
            throw new XMPPException("No response from server on status set.");
        } else if (response.getError() != null) {
            throw new XMPPException(response.getError());
        } else {
            AdHocCommandData responseData = (AdHocCommandData) response;
            this.sessionID = responseData.getSessionID();
            super.setData(responseData);
        }
    }

    public String getOwnerJID() {
        return this.jid;
    }

    public long getPacketReplyTimeout() {
        return this.packetReplyTimeout;
    }

    public void setPacketReplyTimeout(long packetReplyTimeout) {
        this.packetReplyTimeout = packetReplyTimeout;
    }
}
