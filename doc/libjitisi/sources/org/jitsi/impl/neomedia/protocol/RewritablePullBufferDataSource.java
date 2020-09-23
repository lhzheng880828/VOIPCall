package org.jitsi.impl.neomedia.protocol;

import java.io.IOException;
import java.util.LinkedList;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;
import org.jitsi.impl.neomedia.control.AbstractControls;
import org.jitsi.service.neomedia.DTMFInbandTone;

public class RewritablePullBufferDataSource extends PullBufferDataSourceDelegate<PullBufferDataSource> implements MuteDataSource, InbandDTMFDataSource {
    private boolean mute;
    /* access modifiers changed from: private|final */
    public final LinkedList<DTMFInbandTone> tones = new LinkedList();

    private class MutePullBufferStream extends SourceStreamDelegate<PullBufferStream> implements PullBufferStream {
        private MutePullBufferStream(PullBufferStream stream) {
            super(stream);
        }

        public Format getFormat() {
            return ((PullBufferStream) this.stream).getFormat();
        }

        public void read(Buffer buffer) throws IOException {
            ((PullBufferStream) this.stream).read(buffer);
            if (RewritablePullBufferDataSource.this.isSendingDTMF()) {
                RewritablePushBufferDataSource.sendDTMF(buffer, (DTMFInbandTone) RewritablePullBufferDataSource.this.tones.poll());
            } else if (RewritablePullBufferDataSource.this.isMute()) {
                RewritablePushBufferDataSource.mute(buffer);
            }
        }

        public boolean willReadBlock() {
            return ((PullBufferStream) this.stream).willReadBlock();
        }
    }

    public RewritablePullBufferDataSource(PullBufferDataSource dataSource) {
        super(dataSource);
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public boolean isMute() {
        return this.mute;
    }

    public void addDTMF(DTMFInbandTone tone) {
        this.tones.add(tone);
    }

    public boolean isSendingDTMF() {
        return !this.tones.isEmpty();
    }

    public PullBufferDataSource getWrappedDataSource() {
        return (PullBufferDataSource) this.dataSource;
    }

    public Object getControl(String controlType) {
        return (InbandDTMFDataSource.class.getName().equals(controlType) || MuteDataSource.class.getName().equals(controlType)) ? this : AbstractControls.queryInterface(this.dataSource, controlType);
    }

    public PullBufferStream[] getStreams() {
        PullBufferStream[] streams = ((PullBufferDataSource) this.dataSource).getStreams();
        if (streams != null) {
            for (int streamIndex = 0; streamIndex < streams.length; streamIndex++) {
                streams[streamIndex] = new MutePullBufferStream(streams[streamIndex]);
            }
        }
        return streams;
    }
}
