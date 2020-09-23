package org.jitsi.service.audionotifier;

import java.util.concurrent.Callable;

public interface SCAudioClip {
    boolean isStarted();

    void play();

    void play(int i, Callable<Boolean> callable);

    void stop();
}
