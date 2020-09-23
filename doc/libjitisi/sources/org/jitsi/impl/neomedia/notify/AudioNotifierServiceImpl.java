package org.jitsi.impl.neomedia.notify;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import javax.media.CaptureDeviceInfo;
import org.jitsi.impl.neomedia.NeomediaServiceUtils;
import org.jitsi.impl.neomedia.device.AudioSystem;
import org.jitsi.impl.neomedia.device.AudioSystem.DataFlow;
import org.jitsi.impl.neomedia.device.DeviceConfiguration;
import org.jitsi.service.audionotifier.AudioNotifierService;
import org.jitsi.service.audionotifier.SCAudioClip;

public class AudioNotifierServiceImpl implements AudioNotifierService, PropertyChangeListener {
    /* access modifiers changed from: private */
    public Map<AudioKey, SCAudioClip> audios;
    private final Object audiosSyncRoot = new Object();
    private final DeviceConfiguration deviceConfiguration = NeomediaServiceUtils.getMediaServiceImpl().getDeviceConfiguration();
    private boolean mute;

    private static class AudioKey {
        private final boolean playback;
        final String uri;

        /* synthetic */ AudioKey(String x0, boolean x1, AnonymousClass1 x2) {
            this(x0, x1);
        }

        private AudioKey(String uri, boolean playback) {
            this.uri = uri;
            this.playback = playback;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o == null) {
                return false;
            }
            AudioKey that = (AudioKey) o;
            if (this.playback == that.playback) {
                if (this.uri == null) {
                    if (that.uri == null) {
                        return true;
                    }
                } else if (this.uri.equals(that.uri)) {
                    return true;
                }
            }
            return false;
        }

        public int hashCode() {
            int i = 0;
            int hashCode = this.uri == null ? 0 : this.uri.hashCode();
            if (this.playback) {
                i = 1;
            }
            return hashCode + i;
        }
    }

    public AudioNotifierServiceImpl() {
        this.deviceConfiguration.addPropertyChangeListener(this);
    }

    public boolean audioOutAndNotificationsShareSameDevice() {
        AudioSystem audioSystem = getDeviceConfiguration().getAudioSystem();
        CaptureDeviceInfo notify = audioSystem.getSelectedDevice(DataFlow.NOTIFY);
        CaptureDeviceInfo playback = audioSystem.getSelectedDevice(DataFlow.PLAYBACK);
        if (notify == null) {
            if (playback == null) {
                return true;
            }
            return false;
        } else if (playback != null) {
            return notify.getLocator().equals(playback.getLocator());
        } else {
            return false;
        }
    }

    public SCAudioClip createAudio(String uri) {
        return createAudio(uri, false);
    }

    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    public org.jitsi.service.audionotifier.SCAudioClip createAudio(java.lang.String r12, boolean r13) {
        /*
        r11 = this;
        r8 = 0;
        r9 = r11.audiosSyncRoot;
        monitor-enter(r9);
        r5 = new org.jitsi.impl.neomedia.notify.AudioNotifierServiceImpl$AudioKey;	 Catch:{ all -> 0x0062 }
        r7 = 0;
        r5.m2546init(r12, r13, r7);	 Catch:{ all -> 0x0062 }
        r7 = r11.audios;	 Catch:{ all -> 0x0062 }
        if (r7 != 0) goto L_0x003b;
    L_0x000e:
        r0 = r8;
    L_0x000f:
        if (r0 != 0) goto L_0x0021;
    L_0x0011:
        r7 = r11.getDeviceConfiguration();	 Catch:{ Throwable -> 0x005a }
        r2 = r7.getAudioSystem();	 Catch:{ Throwable -> 0x005a }
        if (r2 != 0) goto L_0x0045;
    L_0x001b:
        r1 = new org.jitsi.impl.neomedia.notify.JavaSoundClipImpl;	 Catch:{ Throwable -> 0x005a }
        r1.m2553init(r12, r11);	 Catch:{ Throwable -> 0x005a }
        r0 = r1;
    L_0x0021:
        if (r0 == 0) goto L_0x0068;
    L_0x0023:
        r7 = r11.audios;	 Catch:{ all -> 0x0062 }
        if (r7 != 0) goto L_0x002e;
    L_0x0027:
        r7 = new java.util.HashMap;	 Catch:{ all -> 0x0062 }
        r7.<init>();	 Catch:{ all -> 0x0062 }
        r11.audios = r7;	 Catch:{ all -> 0x0062 }
    L_0x002e:
        r4 = r11.audios;	 Catch:{ all -> 0x0062 }
        r3 = r0;
        r0 = new org.jitsi.impl.neomedia.notify.AudioNotifierServiceImpl$1;	 Catch:{ all -> 0x0062 }
        r0.m2544init(r4, r5, r3);	 Catch:{ all -> 0x0062 }
        r1 = r0;
    L_0x0037:
        monitor-exit(r9);	 Catch:{ all -> 0x0062 }
        r0 = r1;
        r7 = r1;
    L_0x003a:
        return r7;
    L_0x003b:
        r7 = r11.audios;	 Catch:{ all -> 0x0062 }
        r7 = r7.remove(r5);	 Catch:{ all -> 0x0062 }
        r7 = (org.jitsi.service.audionotifier.SCAudioClip) r7;	 Catch:{ all -> 0x0062 }
        r0 = r7;
        goto L_0x000f;
    L_0x0045:
        r7 = "none";
        r10 = r2.getLocatorProtocol();	 Catch:{ Throwable -> 0x005a }
        r7 = r7.equalsIgnoreCase(r10);	 Catch:{ Throwable -> 0x005a }
        if (r7 == 0) goto L_0x0053;
    L_0x0051:
        r0 = 0;
        goto L_0x0021;
    L_0x0053:
        r1 = new org.jitsi.impl.neomedia.notify.AudioSystemClipImpl;	 Catch:{ Throwable -> 0x005a }
        r1.m2550init(r12, r11, r2, r13);	 Catch:{ Throwable -> 0x005a }
        r0 = r1;
        goto L_0x0021;
    L_0x005a:
        r6 = move-exception;
        r7 = r6 instanceof java.lang.ThreadDeath;	 Catch:{ all -> 0x0062 }
        if (r7 == 0) goto L_0x0065;
    L_0x005f:
        r6 = (java.lang.ThreadDeath) r6;	 Catch:{ all -> 0x0062 }
        throw r6;	 Catch:{ all -> 0x0062 }
    L_0x0062:
        r7 = move-exception;
        monitor-exit(r9);	 Catch:{ all -> 0x0062 }
        throw r7;
    L_0x0065:
        monitor-exit(r9);	 Catch:{ all -> 0x0062 }
        r7 = r8;
        goto L_0x003a;
    L_0x0068:
        r1 = r0;
        goto L_0x0037;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.notify.AudioNotifierServiceImpl.createAudio(java.lang.String, boolean):org.jitsi.service.audionotifier.SCAudioClip");
    }

    public DeviceConfiguration getDeviceConfiguration() {
        return this.deviceConfiguration;
    }

    public boolean isMute() {
        return this.mute;
    }

    public void propertyChange(PropertyChangeEvent ev) {
        String propertyName = ev.getPropertyName();
        if ("notifyDevice".equals(propertyName) || "playbackDevice".equals(propertyName)) {
            synchronized (this.audiosSyncRoot) {
                this.audios = null;
            }
        }
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }
}
