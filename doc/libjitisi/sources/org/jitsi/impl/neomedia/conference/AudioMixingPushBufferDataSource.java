package org.jitsi.impl.neomedia.conference;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.LinkedList;
import javax.media.CaptureDeviceInfo;
import javax.media.Time;
import javax.media.control.BufferControl;
import javax.media.control.FormatControl;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import org.jitsi.impl.neomedia.control.AbstractControls;
import org.jitsi.impl.neomedia.protocol.InbandDTMFDataSource;
import org.jitsi.impl.neomedia.protocol.MuteDataSource;
import org.jitsi.service.neomedia.DTMFInbandTone;
import org.jitsi.util.Logger;

public class AudioMixingPushBufferDataSource extends PushBufferDataSource implements CaptureDevice, MuteDataSource, InbandDTMFDataSource {
    private static final Logger logger = Logger.getLogger(AudioMixingPushBufferDataSource.class);
    final AudioMixer audioMixer;
    private boolean connected;
    private boolean mute = false;
    private AudioMixingPushBufferStream outStream;
    private boolean started;
    private final LinkedList<DTMFInbandTone> tones = new LinkedList();

    public AudioMixingPushBufferDataSource(AudioMixer audioMixer) {
        this.audioMixer = audioMixer;
    }

    public void addDTMF(DTMFInbandTone tone) {
        this.tones.add(tone);
    }

    public void addInDataSource(DataSource inDataSource) {
        this.audioMixer.addInDataSource(inDataSource, this);
    }

    public synchronized void connect() throws IOException {
        if (!this.connected) {
            this.audioMixer.connect();
            this.connected = true;
        }
    }

    public synchronized void disconnect() {
        try {
            stop();
            if (this.connected) {
                this.outStream = null;
                this.connected = false;
                this.audioMixer.disconnect();
            }
        } catch (IOException ioex) {
            throw new UndeclaredThrowableException(ioex);
        }
    }

    private BufferControl getBufferControl() {
        return this.audioMixer.getBufferControl();
    }

    public CaptureDeviceInfo getCaptureDeviceInfo() {
        return this.audioMixer.getCaptureDeviceInfo();
    }

    public String getContentType() {
        return this.audioMixer.getContentType();
    }

    public Object getControl(String controlType) {
        return AbstractControls.getControl(this, controlType);
    }

    public Object[] getControls() {
        BufferControl bufferControl = getBufferControl();
        FormatControl[] formatControls = getFormatControls();
        if (bufferControl == null) {
            return formatControls;
        }
        if (formatControls == null || formatControls.length < 1) {
            return new Object[]{bufferControl};
        }
        FormatControl[] controls = new Object[(formatControls.length + 1)];
        controls[0] = bufferControl;
        System.arraycopy(formatControls, 0, controls, 1, formatControls.length);
        return controls;
    }

    public Time getDuration() {
        return this.audioMixer.getDuration();
    }

    public FormatControl[] getFormatControls() {
        return this.audioMixer.getFormatControls();
    }

    public short[] getNextToneSignal(double sampleRate, int sampleSizeInBits) {
        return ((DTMFInbandTone) this.tones.poll()).getAudioSamples(sampleRate, sampleSizeInBits);
    }

    public synchronized PushBufferStream[] getStreams() {
        if (this.connected && this.outStream == null) {
            AudioMixerPushBufferStream audioMixerOutStream = this.audioMixer.getOutStream();
            if (audioMixerOutStream != null) {
                this.outStream = new AudioMixingPushBufferStream(audioMixerOutStream, this);
                if (this.started) {
                    try {
                        this.outStream.start();
                    } catch (IOException ioex) {
                        logger.error("Failed to start " + this.outStream.getClass().getSimpleName() + " with hashCode " + this.outStream.hashCode(), ioex);
                    }
                }
            }
        }
        return this.outStream == null ? new PushBufferStream[0] : new PushBufferStream[]{this.outStream};
    }

    public boolean isMute() {
        return this.mute;
    }

    public boolean isSendingDTMF() {
        return !this.tones.isEmpty();
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public synchronized void start() throws IOException {
        if (!this.started) {
            this.started = true;
            if (this.outStream != null) {
                this.outStream.start();
            }
        }
    }

    public synchronized void stop() throws IOException {
        if (this.started) {
            this.started = false;
            if (this.outStream != null) {
                this.outStream.stop();
            }
        }
    }

    public void updateInDataSource(DataSource inDataSource) {
        this.audioMixer.getOutStream();
    }
}
