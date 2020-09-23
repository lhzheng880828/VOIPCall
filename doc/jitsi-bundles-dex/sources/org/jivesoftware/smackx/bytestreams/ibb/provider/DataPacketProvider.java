package org.jivesoftware.smackx.bytestreams.ibb.provider;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smackx.bytestreams.ibb.packet.Data;
import org.jivesoftware.smackx.bytestreams.ibb.packet.DataPacketExtension;

public class DataPacketProvider implements PacketExtensionProvider, IQProvider {
    public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
        return new DataPacketExtension(parser.getAttributeValue("", "sid"), Long.parseLong(parser.getAttributeValue("", "seq")), parser.nextText());
    }

    public IQ parseIQ(XmlPullParser parser) throws Exception {
        return new Data((DataPacketExtension) parseExtension(parser));
    }
}
