package org.jitsi.impl.neomedia.device;

import javax.media.Codec;
import javax.media.ConfigureCompleteEvent;
import javax.media.ControllerEvent;
import javax.media.Player;
import javax.media.Processor;
import javax.media.Renderer;
import javax.media.UnsupportedPlugInException;
import javax.media.control.TrackControl;
import javax.media.format.AudioFormat;
import org.jitsi.impl.neomedia.audiolevel.AudioLevelEffect;
import org.jitsi.impl.neomedia.jmfext.media.renderer.audio.AbstractAudioRenderer;
import org.jitsi.service.neomedia.VolumeControl;
import org.jitsi.service.neomedia.event.SimpleAudioLevelListener;
import org.jitsi.util.Logger;

public class AudioMediaDeviceSession extends MediaDeviceSession {
    private static final Logger logger = Logger.getLogger(AudioMediaDeviceSession.class);
    private final AudioLevelEffect localUserAudioLevelEffect = new AudioLevelEffect();
    private VolumeControl outputVolumeControl;
    private final AudioLevelEffect streamAudioLevelEffect = new AudioLevelEffect();

    protected AudioMediaDeviceSession(AbstractMediaDevice device) {
        super(device);
    }

    public void copyPlayback(MediaDeviceSession deviceSession) {
        AudioMediaDeviceSession amds = (AudioMediaDeviceSession) deviceSession;
        setStreamAudioLevelListener(amds.streamAudioLevelEffect.getAudioLevelListener());
        setLocalUserAudioLevelListener(amds.localUserAudioLevelEffect.getAudioLevelListener());
    }

    /* access modifiers changed from: protected */
    public Renderer createRenderer(Player player, TrackControl trackControl) {
        Renderer renderer = super.createRenderer(player, trackControl);
        if (renderer != null) {
            setVolumeControl(renderer, this.outputVolumeControl);
        }
        return renderer;
    }

    public int getLastMeasuredAudioLevel(long ssrc) {
        return -1;
    }

    public int getLastMeasuredLocalUserAudioLevel() {
        return -1;
    }

    /* access modifiers changed from: protected */
    public void playerConfigureComplete(Processor player) {
        super.playerConfigureComplete(player);
        TrackControl[] tcs = player.getTrackControls();
        if (tcs != null) {
            for (TrackControl tc : tcs) {
                if (tc.getFormat() instanceof AudioFormat) {
                    try {
                        registerStreamAudioLevelJMFEffect(tc);
                        return;
                    } catch (UnsupportedPlugInException upie) {
                        logger.error("Failed to register stream audio level Effect", upie);
                        return;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void processorControllerUpdate(ControllerEvent event) {
        super.processorControllerUpdate(event);
        if (event instanceof ConfigureCompleteEvent) {
            Processor processor = (Processor) event.getSourceController();
            if (processor != null) {
                registerLocalUserAudioLevelEffect(processor);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void registerLocalUserAudioLevelEffect(Processor processor) {
        try {
            TrackControl[] tcs = processor.getTrackControls();
            if (tcs != null) {
                for (TrackControl tc : tcs) {
                    if (tc.getFormat() instanceof AudioFormat) {
                        tc.setCodecChain(new Codec[]{this.localUserAudioLevelEffect});
                        return;
                    }
                }
            }
        } catch (UnsupportedPlugInException ex) {
            logger.error("Effects are not supported by the datasource.", ex);
        }
    }

    private void registerStreamAudioLevelJMFEffect(TrackControl trackControl) throws UnsupportedPlugInException {
        trackControl.setCodecChain(new Codec[]{this.streamAudioLevelEffect});
    }

    public void setLocalUserAudioLevelListener(SimpleAudioLevelListener listener) {
        this.localUserAudioLevelEffect.setAudioLevelListener(listener);
    }

    public void setOutputVolumeControl(VolumeControl outputVolumeControl) {
        this.outputVolumeControl = outputVolumeControl;
    }

    public void setStreamAudioLevelListener(SimpleAudioLevelListener listener) {
        this.streamAudioLevelEffect.setAudioLevelListener(listener);
    }

    public static void setVolumeControl(Renderer renderer, VolumeControl volumeControl) {
        if (renderer instanceof AbstractAudioRenderer) {
            ((AbstractAudioRenderer) renderer).setVolumeControl(volumeControl);
        }
    }
}
