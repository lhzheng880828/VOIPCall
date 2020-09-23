package org.jitsi.impl.neomedia.jmfext.media.protocol.imgstreaming;

import javax.media.MediaLocator;
import javax.media.control.FormatControl;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.impl.neomedia.control.ImgStreamingControl;
import org.jitsi.impl.neomedia.jmfext.media.protocol.AbstractVideoPullBufferCaptureDevice;

public class DataSource extends AbstractVideoPullBufferCaptureDevice {
    private final ImgStreamingControl imgStreamingControl = new ImgStreamingControl() {
        public Component getControlComponent() {
            return null;
        }

        public void setOrigin(int streamIndex, int displayIndex, int x, int y) {
            DataSource.this.setOrigin(streamIndex, displayIndex, x, y);
        }
    };

    public DataSource(MediaLocator locator) {
        super(locator);
    }

    /* access modifiers changed from: protected */
    public ImageStream createStream(int streamIndex, FormatControl formatControl) {
        int dispayIndex;
        int x;
        int y;
        String remainder = getLocator().getRemainder();
        String[] split = remainder.split(",");
        if (split == null || split.length <= 1) {
            dispayIndex = Integer.parseInt(remainder);
            x = 0;
            y = 0;
        } else {
            dispayIndex = Integer.parseInt(split[0]);
            x = Integer.parseInt(split[1]);
            y = Integer.parseInt(split[2]);
        }
        ImageStream stream = new ImageStream(this, formatControl);
        stream.setDisplayIndex(dispayIndex);
        stream.setOrigin(x, y);
        return stream;
    }

    public Object getControl(String controlType) {
        if (ImgStreamingControl.class.getName().equals(controlType)) {
            return this.imgStreamingControl;
        }
        return super.getControl(controlType);
    }

    public void setOrigin(int streamIndex, int displayIndex, int x, int y) {
        synchronized (getStreamSyncRoot()) {
            Object[] streams = streams();
            if (streams != null && streamIndex < streams.length) {
                ImageStream stream = streams[streamIndex];
                if (stream != null) {
                    stream.setDisplayIndex(displayIndex);
                    stream.setOrigin(x, y);
                }
            }
        }
    }
}
