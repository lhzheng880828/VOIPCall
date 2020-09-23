package org.jitsi.impl.neomedia.device;

import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.rtp.ReceiveStream;
import org.jitsi.impl.neomedia.protocol.PushBufferDataSourceDelegate;

public class ReceiveStreamPushBufferDataSource extends PushBufferDataSourceDelegate<PushBufferDataSource> {
    private final ReceiveStream receiveStream;
    private boolean suppressDisconnect;

    public ReceiveStreamPushBufferDataSource(ReceiveStream receiveStream, PushBufferDataSource dataSource) {
        super(dataSource);
        this.receiveStream = receiveStream;
    }

    public ReceiveStreamPushBufferDataSource(ReceiveStream receiveStream, PushBufferDataSource dataSource, boolean suppressDisconnect) {
        this(receiveStream, dataSource);
        setSuppressDisconnect(suppressDisconnect);
    }

    public void disconnect() {
        if (!this.suppressDisconnect) {
            super.disconnect();
        }
    }

    public ReceiveStream getReceiveStream() {
        return this.receiveStream;
    }

    public PushBufferStream[] getStreams() {
        return ((PushBufferDataSource) this.dataSource).getStreams();
    }

    public void setSuppressDisconnect(boolean suppressDisconnect) {
        this.suppressDisconnect = suppressDisconnect;
    }
}
