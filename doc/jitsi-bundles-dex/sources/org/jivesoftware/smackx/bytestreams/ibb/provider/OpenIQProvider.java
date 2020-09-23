package org.jivesoftware.smackx.bytestreams.ibb.provider;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager.StanzaType;
import org.jivesoftware.smackx.bytestreams.ibb.packet.Open;

public class OpenIQProvider implements IQProvider {
    public IQ parseIQ(XmlPullParser parser) throws Exception {
        StanzaType stanza;
        String sessionID = parser.getAttributeValue("", "sid");
        int blockSize = Integer.parseInt(parser.getAttributeValue("", "block-size"));
        String stanzaValue = parser.getAttributeValue("", "stanza");
        if (stanzaValue == null) {
            stanza = StanzaType.IQ;
        } else {
            stanza = StanzaType.valueOf(stanzaValue.toUpperCase());
        }
        return new Open(sessionID, blockSize, stanza);
    }
}
