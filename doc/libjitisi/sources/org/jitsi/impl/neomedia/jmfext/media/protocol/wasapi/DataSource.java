package org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.media.control.FormatControl;
import javax.media.format.AudioFormat;
import javax.media.protocol.PushBufferStream;
import org.jitsi.impl.neomedia.device.AudioSystem;
import org.jitsi.impl.neomedia.device.WASAPISystem;
import org.jitsi.impl.neomedia.jmfext.media.protocol.AbstractPushBufferCaptureDevice;
import org.jitsi.util.Logger;

public class DataSource extends AbstractPushBufferCaptureDevice {
    private static final Logger logger = Logger.getLogger(DataSource.class);
    final boolean aec;
    final WASAPISystem audioSystem;

    public DataSource() {
        this(null);
    }

    public DataSource(MediaLocator locator) {
        super(locator);
        this.audioSystem = (WASAPISystem) AudioSystem.getAudioSystem(AudioSystem.LOCATOR_PROTOCOL_WASAPI);
        boolean z = this.audioSystem.isDenoise() || this.audioSystem.isEchoCancel();
        this.aec = z;
    }

    /* access modifiers changed from: protected */
    public WASAPIStream createStream(int streamIndex, FormatControl formatControl) {
        return new WASAPIStream(this, formatControl);
    }

    /* access modifiers changed from: protected */
    public void doConnect() throws IOException {
        super.doConnect();
        MediaLocator locator = getLocator();
        synchronized (getStreamSyncRoot()) {
            for (PushBufferStream stream : getStreams()) {
                ((WASAPIStream) stream).setLocator(locator);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void doDisconnect() {
        try {
            synchronized (getStreamSyncRoot()) {
                for (PushBufferStream stream : getStreams()) {
                    try {
                        ((WASAPIStream) stream).setLocator(null);
                    } catch (IOException ioe) {
                        logger.error("Failed to disconnect " + stream.getClass().getName(), ioe);
                    }
                }
            }
        } finally {
            super.doDisconnect();
        }
    }

    /* access modifiers changed from: 0000 */
    public Format[] getIAudioClientSupportedFormats() {
        return getIAudioClientSupportedFormats(0);
    }

    private Format[] getIAudioClientSupportedFormats(int streamIndex) {
        Format[] superSupportedFormats = super.getSupportedFormats(streamIndex);
        if (superSupportedFormats == null || superSupportedFormats.length == 0) {
            return superSupportedFormats;
        }
        List<Format> supportedFormats = new ArrayList(superSupportedFormats.length);
        for (Format format : superSupportedFormats) {
            if ((format instanceof NativelySupportedAudioFormat) && !supportedFormats.contains(format)) {
                supportedFormats.add(format);
            }
        }
        int supportedFormatCount = supportedFormats.size();
        return supportedFormatCount != superSupportedFormats.length ? (Format[]) supportedFormats.toArray(new Format[supportedFormatCount]) : superSupportedFormats;
    }

    /* access modifiers changed from: protected */
    public Format[] getSupportedFormats(int streamIndex) {
        if (!this.aec) {
            return getIAudioClientSupportedFormats(streamIndex);
        }
        List<AudioFormat> aecSupportedFormats = this.audioSystem.getAECSupportedFormats();
        return (Format[]) aecSupportedFormats.toArray(new Format[aecSupportedFormats.size()]);
    }
}
