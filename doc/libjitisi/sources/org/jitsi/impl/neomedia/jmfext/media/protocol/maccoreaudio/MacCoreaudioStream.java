package org.jitsi.impl.neomedia.jmfext.media.protocol.maccoreaudio;

import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.GainControl;
import javax.media.control.FormatControl;
import javax.media.format.AudioFormat;
import org.jitsi.impl.neomedia.MediaServiceImpl;
import org.jitsi.impl.neomedia.NeomediaServiceUtils;
import org.jitsi.impl.neomedia.codec.AbstractCodec2;
import org.jitsi.impl.neomedia.device.MacCoreAudioDevice;
import org.jitsi.impl.neomedia.device.MacCoreaudioSystem;
import org.jitsi.impl.neomedia.device.MacCoreaudioSystem.UpdateAvailableDeviceListListener;
import org.jitsi.impl.neomedia.jmfext.media.protocol.AbstractPullBufferStream;
import org.jitsi.service.neomedia.BasicVolumeControl;
import org.jitsi.util.Logger;

public class MacCoreaudioStream extends AbstractPullBufferStream<DataSource> {
    private static final Logger logger = Logger.getLogger(MacCoreaudioStream.class);
    private final boolean audioQualityImprovement;
    private byte[] buffer = null;
    private int bytesPerBuffer;
    /* access modifiers changed from: private */
    public String deviceUID;
    private AudioFormat format = null;
    private Vector<byte[]> freeBufferList = new Vector();
    private Vector<byte[]> fullBufferList = new Vector();
    private final GainControl gainControl;
    private int nbBufferData = 0;
    private int sequenceNumber = 0;
    /* access modifiers changed from: private */
    public Object startStopMutex = new Object();
    private Lock stopLock = new ReentrantLock();
    /* access modifiers changed from: private */
    public long stream = 0;
    private final UpdateAvailableDeviceListListener updateAvailableDeviceListListener = new UpdateAvailableDeviceListListener() {
        private String deviceUID = null;
        private boolean start = false;

        public void didUpdateAvailableDeviceList() throws Exception {
            synchronized (MacCoreaudioStream.this.startStopMutex) {
                if (MacCoreaudioStream.this.stream == 0 && this.start) {
                    MacCoreaudioStream.this.setDeviceUID(this.deviceUID);
                    MacCoreaudioStream.this.start();
                }
                this.deviceUID = null;
                this.start = false;
            }
        }

        public void willUpdateAvailableDeviceList() throws Exception {
            synchronized (MacCoreaudioStream.this.startStopMutex) {
                if (MacCoreaudioStream.this.stream == 0) {
                    this.deviceUID = null;
                    this.start = false;
                } else {
                    this.deviceUID = MacCoreaudioStream.this.deviceUID;
                    this.start = true;
                    MacCoreaudioStream.this.stop();
                    MacCoreaudioStream.this.setDeviceUID(null);
                }
            }
        }
    };

    public MacCoreaudioStream(DataSource dataSource, FormatControl formatControl, boolean audioQualityImprovement) {
        GainControl gainControl = null;
        super(dataSource, formatControl);
        this.audioQualityImprovement = audioQualityImprovement;
        MediaServiceImpl mediaServiceImpl = NeomediaServiceUtils.getMediaServiceImpl();
        if (mediaServiceImpl != null) {
            gainControl = (GainControl) mediaServiceImpl.getInputVolumeControl();
        }
        this.gainControl = gainControl;
        MacCoreaudioSystem.addUpdateAvailableDeviceListListener(this.updateAvailableDeviceListListener);
    }

    private void connect() {
        AudioFormat format = (AudioFormat) getFormat();
        int channels = format.getChannels();
        if (channels == -1) {
            channels = 1;
        }
        int sampleSizeInBits = format.getSampleSizeInBits();
        double sampleRate = format.getSampleRate();
        this.bytesPerBuffer = ((sampleSizeInBits / 8) * channels) * ((int) ((20.0d * sampleRate) / ((double) (channels * 1000))));
        this.format = new AudioFormat(AudioFormat.LINEAR, sampleRate, sampleSizeInBits, channels, 0, 1, -1, -1.0d, Format.byteArray);
    }

    /* access modifiers changed from: protected */
    public Format doGetFormat() {
        return this.format == null ? super.doGetFormat() : this.format;
    }

    public void read(Buffer buffer) throws IOException {
        int length = 0;
        byte[] data = AbstractCodec2.validateByteArraySize(buffer, this.bytesPerBuffer, false);
        synchronized (this.startStopMutex) {
            while (this.fullBufferList.size() == 0 && this.stream != 0) {
                try {
                    this.startStopMutex.wait();
                } catch (InterruptedException e) {
                }
            }
            if (this.stream != 0) {
                this.freeBufferList.add(data);
                data = (byte[]) this.fullBufferList.remove(0);
                length = data.length;
            }
        }
        if (!(length == 0 || this.gainControl == null)) {
            BasicVolumeControl.applyGain(this.gainControl, data, 0, length);
        }
        long bufferTimeStamp = System.nanoTime();
        buffer.setData(data);
        buffer.setFlags(128);
        if (this.format != null) {
            buffer.setFormat(this.format);
        }
        buffer.setHeader(null);
        buffer.setLength(length);
        buffer.setOffset(0);
        int i = this.sequenceNumber;
        this.sequenceNumber = i + 1;
        buffer.setSequenceNumber((long) i);
        buffer.setTimeStamp(bufferTimeStamp);
    }

    /* access modifiers changed from: 0000 */
    public void setDeviceUID(String deviceUID) {
        synchronized (this.startStopMutex) {
            if (this.deviceUID != null) {
                try {
                    stop();
                } catch (IOException ioex) {
                    logger.info(ioex);
                }
                this.format = null;
            }
            this.deviceUID = deviceUID;
            if (this.deviceUID != null) {
                connect();
            }
        }
    }

    public void start() throws IOException {
        boolean z = true;
        synchronized (this.startStopMutex) {
            if (this.stream == 0 && this.deviceUID != null) {
                this.buffer = new byte[this.bytesPerBuffer];
                this.nbBufferData = 0;
                this.fullBufferList.clear();
                this.freeBufferList.clear();
                MacCoreaudioSystem.willOpenStream();
                String str = this.deviceUID;
                float sampleRate = (float) this.format.getSampleRate();
                int channels = this.format.getChannels();
                int sampleSizeInBits = this.format.getSampleSizeInBits();
                if (this.format.getEndian() != 1) {
                    z = false;
                }
                this.stream = MacCoreAudioDevice.startStream(str, this, sampleRate, channels, sampleSizeInBits, false, z, false, true, MacCoreaudioSystem.isEchoCancelActivated());
                MacCoreaudioSystem.didOpenStream();
            }
        }
    }

    public void stop() throws IOException {
        this.stopLock.lock();
        try {
            synchronized (this.startStopMutex) {
                if (!(this.stream == 0 || this.deviceUID == null)) {
                    MacCoreAudioDevice.stopStream(this.deviceUID, this.stream);
                    this.stream = 0;
                    this.fullBufferList.clear();
                    this.freeBufferList.clear();
                    this.startStopMutex.notify();
                }
            }
        } finally {
            this.stopLock.unlock();
        }
    }

    public void readInput(byte[] buffer, int bufferLength) {
        int nbCopied = 0;
        while (bufferLength > 0) {
            int length = this.buffer.length - this.nbBufferData;
            if (bufferLength < length) {
                length = bufferLength;
            }
            System.arraycopy(buffer, nbCopied, this.buffer, this.nbBufferData, length);
            this.nbBufferData += length;
            nbCopied += length;
            bufferLength -= length;
            if (this.nbBufferData == this.buffer.length) {
                this.fullBufferList.add(this.buffer);
                this.buffer = null;
                this.nbBufferData = 0;
                if (this.stopLock.tryLock()) {
                    try {
                        synchronized (this.startStopMutex) {
                            this.startStopMutex.notify();
                            if (this.freeBufferList.size() > 0) {
                                this.buffer = (byte[]) this.freeBufferList.remove(0);
                            }
                        }
                    } finally {
                        this.stopLock.unlock();
                    }
                }
                if (this.buffer == null) {
                    this.buffer = new byte[this.bytesPerBuffer];
                }
            }
        }
    }
}
