package org.jivesoftware.smackx.bytestreams.ibb;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.bytestreams.ibb.packet.Close;

class CloseListener implements PacketListener {
    private final PacketFilter closeFilter = new AndFilter(new PacketTypeFilter(Close.class), new IQTypeFilter(Type.SET));
    private final InBandBytestreamManager manager;

    protected CloseListener(InBandBytestreamManager manager) {
        this.manager = manager;
    }

    public void processPacket(Packet packet) {
        Close closeRequest = (Close) packet;
        InBandBytestreamSession ibbSession = (InBandBytestreamSession) this.manager.getSessions().get(closeRequest.getSessionID());
        if (ibbSession == null) {
            this.manager.replyItemNotFoundPacket(closeRequest);
            return;
        }
        ibbSession.closeByPeer(closeRequest);
        this.manager.getSessions().remove(closeRequest.getSessionID());
    }

    /* access modifiers changed from: protected */
    public PacketFilter getFilter() {
        return this.closeFilter;
    }
}
