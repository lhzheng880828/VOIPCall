package net.java.sip.communicator.impl.protocol.sip.xcap;

import net.java.sip.communicator.impl.protocol.sip.xcap.model.xcapcaps.XCapCapsType;

public interface XCapCapsClient {
    public static final String CONTENT_TYPE = "application/xcap-caps+xml";
    public static final String DOCUMENT_FORMAT = "xcap-caps/global/index";

    XCapCapsType getXCapCaps() throws XCapException;
}
