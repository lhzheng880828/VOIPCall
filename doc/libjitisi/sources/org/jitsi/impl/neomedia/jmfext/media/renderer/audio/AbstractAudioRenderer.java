package org.jitsi.impl.neomedia.jmfext.media.renderer.audio;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.ByteOrder;
import javax.media.CaptureDeviceInfo;
import javax.media.Format;
import javax.media.GainControl;
import javax.media.MediaLocator;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;
import net.sf.fmj.media.util.MediaThread;
import org.jitsi.impl.neomedia.MediaServiceImpl;
import org.jitsi.impl.neomedia.NeomediaServiceUtils;
import org.jitsi.impl.neomedia.device.AudioSystem;
import org.jitsi.impl.neomedia.device.AudioSystem.DataFlow;
import org.jitsi.impl.neomedia.jmfext.media.renderer.AbstractRenderer;
import org.jitsi.service.neomedia.VolumeControl;

public abstract class AbstractAudioRenderer<T extends AudioSystem> extends AbstractRenderer<AudioFormat> {
    public static final int JAVA_AUDIO_FORMAT_ENDIAN = 1;
    public static final int NATIVE_AUDIO_FORMAT_ENDIAN = (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN ? 1 : 0);
    protected final T audioSystem;
    protected final DataFlow dataFlow;
    private GainControl gainControl;
    private MediaLocator locator;
    private final PropertyChangeListener propertyChangeListener;
    private VolumeControl volumeControl;

    protected AbstractAudioRenderer(T audioSystem) {
        this((AudioSystem) audioSystem, DataFlow.PLAYBACK);
    }

    protected AbstractAudioRenderer(T audioSystem, DataFlow dataFlow) {
        GainControl gainControl = null;
        this.propertyChangeListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                AbstractAudioRenderer.this.propertyChange(ev);
            }
        };
        if (dataFlow == DataFlow.NOTIFY || dataFlow == DataFlow.PLAYBACK) {
            this.audioSystem = audioSystem;
            this.dataFlow = dataFlow;
            if (DataFlow.PLAYBACK.equals(dataFlow)) {
                MediaServiceImpl mediaServiceImpl = NeomediaServiceUtils.getMediaServiceImpl();
                if (mediaServiceImpl != null) {
                    gainControl = (GainControl) mediaServiceImpl.getOutputVolumeControl();
                }
                this.gainControl = gainControl;
                return;
            }
            this.gainControl = null;
            return;
        }
        throw new IllegalArgumentException("dataFlow");
    }

    protected AbstractAudioRenderer(String locatorProtocol) {
        this(locatorProtocol, DataFlow.PLAYBACK);
    }

    protected AbstractAudioRenderer(String locatorProtocol, DataFlow dataFlow) {
        this(AudioSystem.getAudioSystem(locatorProtocol), dataFlow);
    }

    public void close() {
        if (this.audioSystem != null) {
            this.audioSystem.removePropertyChangeListener(this.propertyChangeListener);
        }
    }

    public Object[] getControls() {
        if (getGainControl() == null) {
            return super.getControls();
        }
        return new Object[]{getGainControl()};
    }

    /* access modifiers changed from: protected */
    public GainControl getGainControl() {
        VolumeControl volumeControl = this.volumeControl;
        GainControl gainControl = this.gainControl;
        if (volumeControl instanceof GainControl) {
            return (GainControl) volumeControl;
        }
        return gainControl;
    }

    public MediaLocator getLocator() {
        MediaLocator locator = this.locator;
        if (locator != null || this.audioSystem == null) {
            return locator;
        }
        CaptureDeviceInfo device = this.audioSystem.getSelectedDevice(this.dataFlow);
        if (device != null) {
            return device.getLocator();
        }
        return locator;
    }

    public Format[] getSupportedInputFormats() {
        return this.audioSystem.getDevice(this.dataFlow, getLocator()).getFormats();
    }

    public void open() throws ResourceUnavailableException {
        if (this.locator == null && this.audioSystem != null) {
            this.audioSystem.addPropertyChangeListener(this.propertyChangeListener);
        }
    }

    /* access modifiers changed from: protected */
    public void playbackDevicePropertyChange(PropertyChangeEvent ev) {
    }

    /* access modifiers changed from: private */
    public void propertyChange(PropertyChangeEvent ev) {
        String propertyName;
        switch (this.dataFlow) {
            case NOTIFY:
                propertyName = "notifyDevice";
                break;
            case PLAYBACK:
                propertyName = "playbackDevice";
                break;
            default:
                return;
        }
        if (propertyName.equals(ev.getPropertyName())) {
            playbackDevicePropertyChange(ev);
        }
    }

    public void setLocator(MediaLocator locator) {
        if (this.locator == null) {
            if (locator == null) {
                return;
            }
        } else if (this.locator.equals(locator)) {
            return;
        }
        this.locator = locator;
    }

    public void setVolumeControl(VolumeControl volumeControl) {
        this.volumeControl = volumeControl;
    }

    public static void useAudioThreadPriority() {
        AbstractRenderer.useThreadPriority(MediaThread.getAudioPriority());
    }
}
