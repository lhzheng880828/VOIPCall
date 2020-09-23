package org.jivesoftware.smackx.bytestreams.ibb.provider;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smackx.bytestreams.ibb.packet.Close;

public class CloseIQProvider implements IQProvider {
    public IQ parseIQ(XmlPullParser parser) throws Exception {
        return new Close(parser.getAttributeValue("", "sid"));
    }
}
