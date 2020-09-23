package org.jivesoftware.smackx.bytestreams.ibb;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.bytestreams.ibb.packet.Data;

class DataListener implements PacketListener {
    private final PacketFilter dataFilter = new AndFilter(new PacketTypeFilter(Data.class));
    private final InBandBytestreamManager manager;

    public DataListener(InBandBytestreamManager manager) {
        this.manager = manager;
    }

    public void processPacket(Packet packet) {
        Data data = (Data) packet;
        if (((InBandBytestreamSession) this.manager.getSessions().get(data.getDataPacketExtension().getSessionID())) == null) {
            this.manager.replyItemNotFoundPacket(data);
        }
    }

    /* access modifiers changed from: protected */
    public PacketFilter getFilter() {
        return this.dataFilter;
    }
}
