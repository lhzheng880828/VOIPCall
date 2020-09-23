package net.java.sip.communicator.impl.protocol.jabber.extensions.colibri;

import org.jitsi.service.neomedia.StreamConnector;
import org.jitsi.service.neomedia.StreamConnectorDelegate;

public class ColibriStreamConnector extends StreamConnectorDelegate<StreamConnector> {
    public ColibriStreamConnector(StreamConnector streamConnector) {
        super(streamConnector);
    }

    public void close() {
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            ColibriStreamConnector.super.close();
        } finally {
            ColibriStreamConnector.super.finalize();
        }
    }
}
