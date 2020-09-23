package org.jitsi.service.neomedia;

import java.util.List;
import java.util.Map;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.service.neomedia.control.KeyFrameControl;
import org.jitsi.util.event.VideoListener;

public interface VideoMediaStream extends MediaStream {
    void addVideoListener(VideoListener videoListener);

    KeyFrameControl getKeyFrameControl();

    Component getLocalVisualComponent();

    QualityControl getQualityControl();

    @Deprecated
    Component getVisualComponent();

    Component getVisualComponent(long j);

    List<Component> getVisualComponents();

    void movePartialDesktopStreaming(int i, int i2);

    void removeVideoListener(VideoListener videoListener);

    void updateQualityControl(Map<String, String> map);
}
