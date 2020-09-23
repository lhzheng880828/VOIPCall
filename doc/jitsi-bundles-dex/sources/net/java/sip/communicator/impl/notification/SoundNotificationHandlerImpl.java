package net.java.sip.communicator.impl.notification;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import net.java.sip.communicator.service.notification.NotificationData;
import net.java.sip.communicator.service.notification.SoundNotificationAction;
import net.java.sip.communicator.service.notification.SoundNotificationHandler;
import org.jitsi.service.audionotifier.AbstractSCAudioClip;
import org.jitsi.service.audionotifier.AudioNotifierService;
import org.jitsi.service.audionotifier.SCAudioClip;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.util.Logger;
import org.jitsi.util.OSUtils;
import org.jitsi.util.StringUtils;

public class SoundNotificationHandlerImpl implements SoundNotificationHandler {
    private static final String PROP_DISABLE_NOTIFICATION_DURING_CALL = "net.java.sip.communicator.impl.notification.disableNotificationDuringCall";
    /* access modifiers changed from: private|static */
    public static Logger logger = Logger.getLogger(SoundNotificationHandlerImpl.class);
    private boolean mute;
    private Map<SCAudioClip, NotificationData> playedClips = new WeakHashMap();

    private static class PCSpeakerClip extends AbstractSCAudioClip {
        private Method beepMethod = null;
        private Object toolkit = null;

        public PCSpeakerClip() {
            super(null, NotificationActivator.getAudioNotifier());
            try {
                this.toolkit = Class.forName("java.awt.Toolkit").getMethod("getDefaultToolkit", new Class[0]).invoke(null, new Object[0]);
                this.beepMethod = this.toolkit.getClass().getMethod("beep", new Class[0]);
            } catch (Throwable t) {
                SoundNotificationHandlerImpl.logger.error("Cannot load awt.Toolkit", t);
            }
        }

        /* access modifiers changed from: protected */
        public boolean runOnceInPlayThread() {
            try {
                if (this.beepMethod != null) {
                    this.beepMethod.invoke(this.toolkit, new Object[0]);
                }
                return true;
            } catch (Throwable t) {
                if (!(t instanceof ThreadDeath)) {
                    return false;
                }
                ThreadDeath t2 = (ThreadDeath) t;
            }
        }
    }

    private enum SCAudioClipDevice {
        NOTIFICATION,
        PC_SPEAKER,
        PLAYBACK
    }

    public String getActionType() {
        return "SoundAction";
    }

    public boolean isMute() {
        return this.mute;
    }

    private void play(SoundNotificationAction action, NotificationData data, SCAudioClipDevice device) {
        AudioNotifierService audioNotifService = NotificationActivator.getAudioNotifier();
        if (audioNotifService != null && !StringUtils.isNullOrEmpty(action.getDescriptor(), true)) {
            ConfigurationService cfg = NotificationActivator.getConfigurationService();
            if (cfg == null || !cfg.getBoolean(PROP_DISABLE_NOTIFICATION_DURING_CALL, false) || !SCAudioClipDevice.PLAYBACK.equals(device) || NotificationActivator.getUIService().getInProgressCalls().isEmpty()) {
                SCAudioClip audio = null;
                switch (device) {
                    case NOTIFICATION:
                    case PLAYBACK:
                        audio = audioNotifService.createAudio(action.getDescriptor(), SCAudioClipDevice.PLAYBACK.equals(device));
                        break;
                    case PC_SPEAKER:
                        if (!OSUtils.IS_ANDROID) {
                            audio = new PCSpeakerClip();
                            break;
                        }
                        break;
                }
                if (audio != null) {
                    synchronized (this.playedClips) {
                        this.playedClips.put(audio, data);
                    }
                    try {
                        audio.play(action.getLoopInterval(), (Callable) data.getExtra("SoundNotificationHandler.loopCondition"));
                        synchronized (this.playedClips) {
                            if (!true) {
                                this.playedClips.remove(audio);
                            }
                        }
                    } catch (Throwable th) {
                        synchronized (this.playedClips) {
                            if (!false) {
                                this.playedClips.remove(audio);
                            }
                        }
                    }
                }
            }
        }
    }

    public void setMute(boolean mute) {
        this.mute = mute;
        if (mute) {
            AudioNotifierService ans = NotificationActivator.getAudioNotifier();
            if (ans != null && ans.isMute() != this.mute) {
                ans.setMute(this.mute);
            }
        }
    }

    public void start(SoundNotificationAction action, NotificationData data) {
        if (!isMute()) {
            boolean playOnlyOnPlayback = true;
            AudioNotifierService audioNotifService = NotificationActivator.getAudioNotifier();
            if (audioNotifService != null) {
                playOnlyOnPlayback = audioNotifService.audioOutAndNotificationsShareSameDevice();
            }
            if (!playOnlyOnPlayback) {
                if (action.isSoundNotificationEnabled()) {
                    play(action, data, SCAudioClipDevice.NOTIFICATION);
                }
                if (action.isSoundPlaybackEnabled()) {
                    play(action, data, SCAudioClipDevice.PLAYBACK);
                }
            } else if (action.isSoundNotificationEnabled() || action.isSoundPlaybackEnabled()) {
                play(action, data, SCAudioClipDevice.PLAYBACK);
            }
            if (action.isSoundPCSpeakerEnabled()) {
                play(action, data, SCAudioClipDevice.PC_SPEAKER);
            }
        }
    }

    public void stop(NotificationData data) {
        if (NotificationActivator.getAudioNotifier() != null) {
            List<SCAudioClip> clipsToStop = new ArrayList();
            synchronized (this.playedClips) {
                Iterator<Entry<SCAudioClip, NotificationData>> i = this.playedClips.entrySet().iterator();
                while (i.hasNext()) {
                    Entry<SCAudioClip, NotificationData> e = (Entry) i.next();
                    if (e.getValue() == data) {
                        clipsToStop.add(e.getKey());
                        i.remove();
                    }
                }
            }
            for (SCAudioClip clip : clipsToStop) {
                try {
                    clip.stop();
                } catch (Throwable t) {
                    logger.error("Error stopping audio clip", t);
                }
            }
        }
    }

    public boolean isPlaying(NotificationData data) {
        if (NotificationActivator.getAudioNotifier() != null) {
            synchronized (this.playedClips) {
                for (Entry<SCAudioClip, NotificationData> e : this.playedClips.entrySet()) {
                    if (e.getValue() == data) {
                        boolean isStarted = ((SCAudioClip) e.getKey()).isStarted();
                        return isStarted;
                    }
                }
            }
        }
        return false;
    }
}
