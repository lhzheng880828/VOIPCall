package org.jivesoftware.smackx.provider;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smackx.packet.PrivateData;

public interface PrivateDataProvider {
    PrivateData parsePrivateData(XmlPullParser xmlPullParser) throws Exception;
}
