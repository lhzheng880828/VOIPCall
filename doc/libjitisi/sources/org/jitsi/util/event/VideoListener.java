package org.jitsi.util.event;

import java.util.EventListener;

public interface VideoListener extends EventListener {
    void videoAdded(VideoEvent videoEvent);

    void videoRemoved(VideoEvent videoEvent);

    void videoUpdate(VideoEvent videoEvent);
}
