package org.jitsi.service.audionotifier;

public interface AudioNotifierService {
    boolean audioOutAndNotificationsShareSameDevice();

    SCAudioClip createAudio(String str);

    SCAudioClip createAudio(String str, boolean z);

    boolean isMute();

    void setMute(boolean z);
}
