package org.jivesoftware.smack.provider;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.IQ;

public interface IQProvider {
    IQ parseIQ(XmlPullParser xmlPullParser) throws Exception;
}
