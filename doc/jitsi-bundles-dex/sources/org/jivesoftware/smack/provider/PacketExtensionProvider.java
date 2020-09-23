package org.jivesoftware.smack.provider;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;

public interface PacketExtensionProvider {
    PacketExtension parseExtension(XmlPullParser xmlPullParser) throws Exception;
}
