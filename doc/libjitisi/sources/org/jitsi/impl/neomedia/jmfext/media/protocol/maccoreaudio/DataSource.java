package org.jitsi.impl.neomedia.jmfext.media.protocol.maccoreaudio;

import java.io.IOException;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.media.control.FormatControl;
import javax.media.protocol.PullBufferStream;
import org.jitsi.impl.neomedia.device.AudioSystem;
import org.jitsi.impl.neomedia.jmfext.media.protocol.AbstractPullBufferCaptureDevice;

public class DataSource extends AbstractPullBufferCaptureDevice {
    private final boolean audioQualityImprovement;
    private final Format[] supportedFormats;

    public DataSource() {
        this.supportedFormats = null;
        this.audioQualityImprovement = true;
    }

    public DataSource(MediaLocator locator) {
        this(locator, null, true);
    }

    public DataSource(MediaLocator locator, Format[] supportedFormats, boolean audioQualityImprovement) {
        super(locator);
        this.supportedFormats = supportedFormats == null ? null : (Format[]) supportedFormats.clone();
        this.audioQualityImprovement = audioQualityImprovement;
    }

    /* access modifiers changed from: protected */
    public MacCoreaudioStream createStream(int streamIndex, FormatControl formatControl) {
        return new MacCoreaudioStream(this, formatControl, this.audioQualityImprovement);
    }

    /* access modifiers changed from: protected */
    public void doConnect() throws IOException {
        super.doConnect();
        String deviceID = getDeviceID();
        synchronized (getStreamSyncRoot()) {
            for (PullBufferStream stream : getStreams()) {
                ((MacCoreaudioStream) stream).setDeviceUID(deviceID);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void doDisconnect() {
        try {
            synchronized (getStreamSyncRoot()) {
                Object[] streams = streams();
                if (streams != null) {
                    for (Object stream : streams) {
                        ((MacCoreaudioStream) stream).setDeviceUID(null);
                    }
                }
            }
        } finally {
            super.doDisconnect();
        }
    }

    private String getDeviceID() {
        MediaLocator locator = getLocator();
        if (locator != null) {
            return getDeviceID(locator);
        }
        throw new IllegalStateException("locator");
    }

    public static String getDeviceID(MediaLocator locator) {
        if (locator == null) {
            throw new NullPointerException("locator");
        } else if (AudioSystem.LOCATOR_PROTOCOL_MACCOREAUDIO.equalsIgnoreCase(locator.getProtocol())) {
            String remainder = locator.getRemainder();
            if (remainder == null || remainder.charAt(0) != '#') {
                return remainder;
            }
            return remainder.substring(1);
        } else {
            throw new IllegalArgumentException("locator.protocol");
        }
    }

    /* access modifiers changed from: protected */
    public Format[] getSupportedFormats(int streamIndex) {
        return this.supportedFormats == null ? super.getSupportedFormats(streamIndex) : this.supportedFormats;
    }
}
