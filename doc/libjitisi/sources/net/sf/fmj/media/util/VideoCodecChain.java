package net.sf.fmj.media.util;

import javax.media.Format;
import javax.media.format.RGBFormat;
import javax.media.format.UnsupportedFormatException;
import javax.media.format.VideoFormat;
import javax.media.format.YUVFormat;
import javax.media.renderer.VideoRenderer;
import org.jitsi.android.util.java.awt.Component;

public class VideoCodecChain extends CodecChain {
    public VideoCodecChain(VideoFormat vf) throws UnsupportedFormatException {
        if (vf.getSize() == null || vf == null) {
            throw new UnsupportedFormatException(vf);
        } else if (!buildChain(vf)) {
            throw new UnsupportedFormatException(vf);
        }
    }

    public Component getControlComponent() {
        if (this.renderer instanceof VideoRenderer) {
            return ((VideoRenderer) this.renderer).getComponent();
        }
        return null;
    }

    /* access modifiers changed from: 0000 */
    public boolean isRawFormat(Format format) {
        return (format instanceof RGBFormat) || (format instanceof YUVFormat) || (format.getEncoding() != null && (format.getEncoding().equalsIgnoreCase(VideoFormat.JPEG) || format.getEncoding().equalsIgnoreCase(VideoFormat.MPEG)));
    }
}
