package org.jivesoftware.smackx.provider;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.packet.DelayInfo;
import org.jivesoftware.smackx.packet.DelayInformation;

public class DelayInfoProvider extends DelayInformationProvider {
    public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
        return new DelayInfo((DelayInformation) super.parseExtension(parser));
    }
}
