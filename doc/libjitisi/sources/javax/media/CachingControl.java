package javax.media;

import org.jitsi.android.util.java.awt.Component;

public interface CachingControl extends Control {
    public static final long LENGTH_UNKNOWN = Long.MAX_VALUE;

    long getContentLength();

    long getContentProgress();

    Component getControlComponent();

    Component getProgressBarComponent();

    boolean isDownloading();
}
