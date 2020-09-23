package org.jitsi.impl.neomedia.jmfext.media.renderer.audio;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.GainControl;
import javax.media.MediaLocator;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;
import org.jitsi.impl.neomedia.device.AudioSystem;
import org.jitsi.impl.neomedia.device.AudioSystem.DataFlow;
import org.jitsi.impl.neomedia.device.MacCoreAudioDevice;
import org.jitsi.impl.neomedia.device.MacCoreaudioSystem;
import org.jitsi.impl.neomedia.device.MacCoreaudioSystem.UpdateAvailableDeviceListListener;
import org.jitsi.service.neomedia.BasicVolumeControl;

public class MacCoreaudioRenderer extends AbstractAudioRenderer<MacCoreaudioSystem> {
    private static final Format[] EMPTY_SUPPORTED_INPUT_FORMATS = new Format[0];
    private static final String PLUGIN_NAME = "MacCoreaudio Renderer";
    private static final Format[] SUPPORTED_INPUT_FORMATS;
    private static final double[] SUPPORTED_INPUT_SAMPLE_RATES = new double[]{8000.0d, 11025.0d, 16000.0d, 22050.0d, 32000.0d, 44100.0d, 48000.0d};
    private byte[] buffer;
    private String deviceUID;
    private boolean isStopping;
    private int nbBufferData;
    /* access modifiers changed from: private */
    public Object startStopMutex;
    private Lock stopLock;
    /* access modifiers changed from: private */
    public long stream;
    private Format[] supportedInputFormats;
    private final UpdateAvailableDeviceListListener updateAvailableDeviceListListener;

    static {
        int count = SUPPORTED_INPUT_SAMPLE_RATES.length;
        SUPPORTED_INPUT_FORMATS = new Format[count];
        for (int i = 0; i < count; i++) {
            SUPPORTED_INPUT_FORMATS[i] = new AudioFormat(AudioFormat.LINEAR, SUPPORTED_INPUT_SAMPLE_RATES[i], 16, -1, 0, 1, -1, -1.0d, Format.byteArray);
        }
    }

    public MacCoreaudioRenderer() {
        this(true);
    }

    public MacCoreaudioRenderer(boolean enableVolumeControl) {
        DataFlow dataFlow;
        String str = AudioSystem.LOCATOR_PROTOCOL_MACCOREAUDIO;
        if (enableVolumeControl) {
            dataFlow = DataFlow.PLAYBACK;
        } else {
            dataFlow = DataFlow.NOTIFY;
        }
        super(str, dataFlow);
        this.deviceUID = null;
        this.stream = 0;
        this.startStopMutex = new Object();
        this.buffer = null;
        this.nbBufferData = 0;
        this.isStopping = false;
        this.stopLock = new ReentrantLock();
        this.updateAvailableDeviceListListener = new UpdateAvailableDeviceListListener() {
            private boolean start = false;

            public void didUpdateAvailableDeviceList() throws Exception {
                synchronized (MacCoreaudioRenderer.this.startStopMutex) {
                    MacCoreaudioRenderer.this.updateDeviceUID();
                    if (this.start) {
                        MacCoreaudioRenderer.this.open();
                        MacCoreaudioRenderer.this.start();
                    }
                }
            }

            public void willUpdateAvailableDeviceList() throws Exception {
                synchronized (MacCoreaudioRenderer.this.startStopMutex) {
                    this.start = false;
                    if (MacCoreaudioRenderer.this.stream != 0) {
                        this.start = true;
                        MacCoreaudioRenderer.this.stop();
                    }
                }
            }
        };
        MacCoreaudioSystem.addUpdateAvailableDeviceListListener(this.updateAvailableDeviceListListener);
    }

    public void close() {
        stop();
        super.close();
    }

    public String getName() {
        return PLUGIN_NAME;
    }

    public Format[] getSupportedInputFormats() {
        if (this.supportedInputFormats == null) {
            updateDeviceUID();
            if (this.deviceUID == null) {
                this.supportedInputFormats = SUPPORTED_INPUT_FORMATS;
            } else {
                int maxOutputChannels = Math.min(MacCoreAudioDevice.countOutputChannels(this.deviceUID), 2);
                List<Format> supportedInputFormats = new ArrayList(SUPPORTED_INPUT_FORMATS.length);
                for (Format supportedInputFormat : SUPPORTED_INPUT_FORMATS) {
                    getSupportedInputFormats(supportedInputFormat, 1, maxOutputChannels, supportedInputFormats);
                }
                this.supportedInputFormats = supportedInputFormats.isEmpty() ? EMPTY_SUPPORTED_INPUT_FORMATS : (Format[]) supportedInputFormats.toArray(EMPTY_SUPPORTED_INPUT_FORMATS);
            }
        }
        if (this.supportedInputFormats.length == 0) {
            return EMPTY_SUPPORTED_INPUT_FORMATS;
        }
        return (Format[]) this.supportedInputFormats.clone();
    }

    private void getSupportedInputFormats(Format format, int minOutputChannels, int maxOutputChannels, List<Format> supportedInputFormats) {
        AudioFormat audioFormat = (AudioFormat) format;
        int sampleSizeInBits = audioFormat.getSampleSizeInBits();
        double sampleRate = audioFormat.getSampleRate();
        float minRate = MacCoreAudioDevice.getMinimalNominalSampleRate(this.deviceUID, true, MacCoreaudioSystem.isEchoCancelActivated());
        float maxRate = MacCoreAudioDevice.getMaximalNominalSampleRate(this.deviceUID, true, MacCoreaudioSystem.isEchoCancelActivated());
        for (int channels = minOutputChannels; channels <= maxOutputChannels; channels++) {
            if (sampleRate >= ((double) minRate) && sampleRate <= ((double) maxRate)) {
                supportedInputFormats.add(new AudioFormat(audioFormat.getEncoding(), sampleRate, sampleSizeInBits, channels, audioFormat.getEndian(), audioFormat.getSigned(), -1, -1.0d, audioFormat.getDataType()));
            }
        }
    }

    public void open() throws ResourceUnavailableException {
        synchronized (this.startStopMutex) {
            if (this.stream == 0) {
                MacCoreaudioSystem.willOpenStream();
                try {
                    if (!updateDeviceUID()) {
                        throw new ResourceUnavailableException("No locator/MediaLocator is set.");
                    } else if (this.inputFormat == null) {
                        throw new ResourceUnavailableException("inputFormat not set");
                    } else {
                        MacCoreaudioSystem.didOpenStream();
                    }
                } catch (Throwable th) {
                    MacCoreaudioSystem.didOpenStream();
                }
            }
            super.open();
        }
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void playbackDevicePropertyChange(PropertyChangeEvent ev) {
        synchronized (this.startStopMutex) {
            stop();
            updateDeviceUID();
            start();
        }
    }

    public int process(Buffer buffer) {
        synchronized (this.startStopMutex) {
            if (!(this.stream == 0 || this.isStopping)) {
                GainControl gainControl = getGainControl();
                if (gainControl != null) {
                    BasicVolumeControl.applyGain(gainControl, (byte[]) buffer.getData(), buffer.getOffset(), buffer.getLength());
                }
                int length = buffer.getLength();
                int maxNbBuffers = 2000 / 20;
                updateBufferLength(Math.min(this.nbBufferData + length, length * 100));
                if (this.nbBufferData + length > this.buffer.length) {
                    length = this.buffer.length - this.nbBufferData;
                }
                System.arraycopy((byte[]) buffer.getData(), buffer.getOffset(), this.buffer, this.nbBufferData, length);
                this.nbBufferData += length;
            }
        }
        return 0;
    }

    public void setLocator(MediaLocator locator) {
        super.setLocator(locator);
        updateDeviceUID();
        this.supportedInputFormats = null;
    }

    public void start() {
        boolean z = true;
        synchronized (this.startStopMutex) {
            if (this.stream == 0 && this.deviceUID != null) {
                int nbChannels = ((AudioFormat) this.inputFormat).getChannels();
                if (nbChannels == -1) {
                    nbChannels = 1;
                }
                MacCoreaudioSystem.willOpenStream();
                String str = this.deviceUID;
                float sampleRate = (float) ((AudioFormat) this.inputFormat).getSampleRate();
                int sampleSizeInBits = ((AudioFormat) this.inputFormat).getSampleSizeInBits();
                if (((AudioFormat) this.inputFormat).getEndian() != 1) {
                    z = false;
                }
                this.stream = MacCoreAudioDevice.startStream(str, this, sampleRate, nbChannels, sampleSizeInBits, false, z, false, false, MacCoreaudioSystem.isEchoCancelActivated());
                MacCoreaudioSystem.didOpenStream();
            }
        }
    }

    public void stop() {
        boolean doStop = false;
        synchronized (this.startStopMutex) {
            if (!(this.stream == 0 || this.deviceUID == null || this.isStopping)) {
                doStop = true;
                this.isStopping = true;
                long startTime = System.currentTimeMillis();
                long currentTime = startTime;
                while (this.nbBufferData > 0 && currentTime - startTime < 500) {
                    try {
                        this.startStopMutex.wait(500);
                    } catch (InterruptedException e) {
                    }
                    currentTime = System.currentTimeMillis();
                }
            }
        }
        if (doStop) {
            this.stopLock.lock();
            try {
                synchronized (this.startStopMutex) {
                    if (!(this.stream == 0 || this.deviceUID == null)) {
                        MacCoreAudioDevice.stopStream(this.deviceUID, this.stream);
                        this.stream = 0;
                        this.buffer = null;
                        this.nbBufferData = 0;
                        this.isStopping = false;
                    }
                }
            } finally {
                this.stopLock.unlock();
            }
        }
    }

    public void writeOutput(byte[] buffer, int bufferLength) {
        if (this.stopLock.tryLock()) {
            try {
                synchronized (this.startStopMutex) {
                    updateBufferLength(bufferLength);
                    int length = this.nbBufferData;
                    if (bufferLength < length) {
                        length = bufferLength;
                    }
                    System.arraycopy(this.buffer, 0, buffer, 0, length);
                    if (length < bufferLength) {
                        Arrays.fill(buffer, length, bufferLength, (byte) 0);
                    }
                    this.nbBufferData -= length;
                    if (this.nbBufferData > 0) {
                        System.arraycopy(this.buffer, length, this.buffer, 0, this.nbBufferData);
                    } else {
                        this.startStopMutex.notify();
                    }
                }
            } finally {
                this.stopLock.unlock();
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean updateDeviceUID() {
        MediaLocator locator = getLocator();
        if (locator != null) {
            String remainder = locator.getRemainder();
            if (remainder != null && remainder.length() > 1) {
                synchronized (this.startStopMutex) {
                    this.deviceUID = remainder.substring(1);
                }
                return true;
            }
        }
        return false;
    }

    private void updateBufferLength(int newLength) {
        synchronized (this.startStopMutex) {
            if (this.buffer == null) {
                this.buffer = new byte[newLength];
                this.nbBufferData = 0;
            } else if (newLength > this.buffer.length) {
                byte[] newBuffer = new byte[newLength];
                System.arraycopy(this.buffer, 0, newBuffer, 0, this.nbBufferData);
                this.buffer = newBuffer;
            }
        }
    }
}
