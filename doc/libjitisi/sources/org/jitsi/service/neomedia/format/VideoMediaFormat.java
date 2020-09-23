package org.jitsi.service.neomedia.format;

import org.jitsi.android.util.java.awt.Dimension;

public interface VideoMediaFormat extends MediaFormat {
    float getFrameRate();

    Dimension getSize();
}
