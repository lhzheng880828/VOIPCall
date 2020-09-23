package org.jivesoftware.smackx.provider;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smackx.packet.Header;

public class HeaderProvider implements PacketExtensionProvider {
    public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
        String name = parser.getAttributeValue(null, "name");
        String value = null;
        parser.next();
        if (parser.getEventType() == 4) {
            value = parser.getText();
        }
        while (parser.getEventType() != 3) {
            parser.next();
        }
        return new Header(name, value);
    }
}
