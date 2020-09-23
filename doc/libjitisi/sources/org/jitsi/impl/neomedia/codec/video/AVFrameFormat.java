package org.jitsi.impl.neomedia.codec.video;

import javax.media.Format;
import javax.media.format.VideoFormat;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.util.Logger;

public class AVFrameFormat extends VideoFormat {
    public static final String AVFRAME = "AVFrame";
    private static final Logger logger = Logger.getLogger(AVFrameFormat.class);
    private static final long serialVersionUID = 0;
    private int deviceSystemPixFmt;
    private int pixFmt;

    public AVFrameFormat() {
        this(-1, -1);
    }

    public AVFrameFormat(Dimension size, float frameRate, int pixFmt) {
        this(size, frameRate, pixFmt, -1);
    }

    public AVFrameFormat(Dimension size, float frameRate, int pixFmt, int deviceSystemPixFmt) {
        super(AVFRAME, size, -1, AVFrame.class, frameRate);
        if (pixFmt == -1 && deviceSystemPixFmt != -1) {
            logger.warn("Specifying a device system-specific pixel format 0x" + Long.toHexString(((long) deviceSystemPixFmt) & 4294967295L) + " without a matching FFmpeg pixel format may" + " eventually lead to a failure.", new Throwable());
        }
        this.pixFmt = pixFmt;
        this.deviceSystemPixFmt = deviceSystemPixFmt;
    }

    public AVFrameFormat(int pixFmt) {
        this(pixFmt, -1);
    }

    public AVFrameFormat(int pixFmt, int deviceSystemPixFmt) {
        this(null, -1.0f, pixFmt, deviceSystemPixFmt);
    }

    public Object clone() {
        AVFrameFormat f = new AVFrameFormat(getSize(), getFrameRate(), this.pixFmt, this.deviceSystemPixFmt);
        f.copy(this);
        return f;
    }

    /* access modifiers changed from: protected */
    public void copy(Format f) {
        super.copy(f);
        if (f instanceof AVFrameFormat) {
            AVFrameFormat avFrameFormat = (AVFrameFormat) f;
            this.pixFmt = avFrameFormat.pixFmt;
            this.deviceSystemPixFmt = avFrameFormat.deviceSystemPixFmt;
        }
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof AVFrameFormat) || !super.equals(obj)) {
            return false;
        }
        if (this.pixFmt == ((AVFrameFormat) obj).pixFmt) {
            return true;
        }
        return false;
    }

    public int getDeviceSystemPixFmt() {
        return this.deviceSystemPixFmt;
    }

    public int getPixFmt() {
        return this.pixFmt;
    }

    public int hashCode() {
        return super.hashCode() + this.pixFmt;
    }

    public Format intersects(Format format) {
        Format intersection = super.intersects(format);
        if (intersection != null) {
            AVFrameFormat avFrameFormatIntersection = (AVFrameFormat) intersection;
            int i = (this.pixFmt == -1 && (format instanceof AVFrameFormat)) ? ((AVFrameFormat) format).pixFmt : this.pixFmt;
            avFrameFormatIntersection.pixFmt = i;
        }
        return intersection;
    }

    public boolean matches(Format format) {
        if (!super.matches(format)) {
            return false;
        }
        if (!(format instanceof AVFrameFormat)) {
            return true;
        }
        AVFrameFormat avFrameFormat = (AVFrameFormat) format;
        return this.pixFmt == -1 || avFrameFormat.pixFmt == -1 || this.pixFmt == avFrameFormat.pixFmt;
    }

    public String toString() {
        StringBuilder s = new StringBuilder(super.toString());
        if (this.pixFmt != -1) {
            s.append(", pixFmt ").append(this.pixFmt);
        }
        if (this.deviceSystemPixFmt != -1) {
            s.append(", deviceSystemPixFmt 0x");
            s.append(Long.toHexString(((long) this.deviceSystemPixFmt) & 4294967295L));
        }
        return s.toString();
    }
}
