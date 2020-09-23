package org.jivesoftware.smackx.bytestreams.ibb.packet;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;

public class Data extends IQ {
    private final DataPacketExtension dataPacketExtension;

    public Data(DataPacketExtension data) {
        if (data == null) {
            throw new IllegalArgumentException("Data must not be null");
        }
        this.dataPacketExtension = data;
        addExtension(data);
        setType(Type.SET);
    }

    public DataPacketExtension getDataPacketExtension() {
        return this.dataPacketExtension;
    }

    public String getChildElementXML() {
        return this.dataPacketExtension.toXML();
    }
}
