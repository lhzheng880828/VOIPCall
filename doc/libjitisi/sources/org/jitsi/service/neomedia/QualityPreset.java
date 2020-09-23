package org.jitsi.service.neomedia;

import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.impl.neomedia.device.DeviceConfiguration;

public class QualityPreset implements Comparable<QualityPreset> {
    public static final QualityPreset HD_QUALITY = new QualityPreset(new Dimension(1280, 720), 30.0f);
    public static final QualityPreset LO_QUALITY = new QualityPreset(new Dimension(320, 240), 15.0f);
    public static final QualityPreset SD_QUALITY = new QualityPreset(new Dimension(DeviceConfiguration.DEFAULT_VIDEO_WIDTH, DeviceConfiguration.DEFAULT_VIDEO_HEIGHT), 20.0f);
    private final float frameRate;
    private final Dimension resolution;

    public QualityPreset(Dimension resolution, float frameRate) {
        this.frameRate = frameRate;
        this.resolution = resolution;
    }

    public QualityPreset(Dimension resolution) {
        this(resolution, -1.0f);
    }

    public float getFameRate() {
        return this.frameRate;
    }

    public Dimension getResolution() {
        return this.resolution;
    }

    public int compareTo(QualityPreset o) {
        if (this.resolution == null) {
            return -1;
        }
        if (o == null || o.resolution == null) {
            return 1;
        }
        if (this.resolution.equals(o.resolution)) {
            return 0;
        }
        if (this.resolution.height >= o.resolution.height || this.resolution.width >= o.resolution.width) {
            return 1;
        }
        return -1;
    }
}
