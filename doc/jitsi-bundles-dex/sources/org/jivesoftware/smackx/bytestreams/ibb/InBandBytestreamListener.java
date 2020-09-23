package org.jivesoftware.smackx.bytestreams.ibb;

import org.jivesoftware.smackx.bytestreams.BytestreamListener;
import org.jivesoftware.smackx.bytestreams.BytestreamRequest;

public abstract class InBandBytestreamListener implements BytestreamListener {
    public abstract void incomingBytestreamRequest(InBandBytestreamRequest inBandBytestreamRequest);

    public void incomingBytestreamRequest(BytestreamRequest request) {
        incomingBytestreamRequest((InBandBytestreamRequest) request);
    }
}
