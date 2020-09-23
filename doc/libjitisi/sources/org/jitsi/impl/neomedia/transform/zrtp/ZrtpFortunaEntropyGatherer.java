package org.jitsi.impl.neomedia.transform.zrtp;

import gnu.java.zrtp.utils.ZrtpFortuna;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;
import javax.media.Buffer;
import javax.media.CaptureDeviceInfo;
import javax.media.Controller;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoDataSourceException;
import javax.media.format.AudioFormat;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.protocol.SourceStream;
import org.jitsi.impl.neomedia.device.DeviceConfiguration;
import org.jitsi.util.Logger;

public class ZrtpFortunaEntropyGatherer {
    private static final int NUM_OF_SECONDS = 2;
    /* access modifiers changed from: private|static */
    public static boolean entropyOk = false;
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(ZrtpFortunaEntropyGatherer.class);
    /* access modifiers changed from: private */
    public int bytes20ms = 0;
    /* access modifiers changed from: private */
    public int bytesToGather = 0;
    /* access modifiers changed from: private|final */
    public final DeviceConfiguration deviceConfiguration;
    /* access modifiers changed from: private */
    public int gatheredEntropy = 0;

    private class GatherAudio extends Thread implements BufferTransferHandler {
        private SourceStream audioStream;
        private boolean bufferAvailable;
        private final Object bufferSync;
        private DataSource dataSource;
        private final Buffer firstBuf;

        private GatherAudio() {
            this.dataSource = null;
            this.audioStream = null;
            this.firstBuf = new Buffer();
            this.bufferAvailable = false;
            this.bufferSync = new Object();
        }

        /* access modifiers changed from: private */
        public boolean prepareAudioEntropy() {
            CaptureDeviceInfo audioCaptureDevice = ZrtpFortunaEntropyGatherer.this.deviceConfiguration.getAudioCaptureDevice();
            if (audioCaptureDevice == null) {
                return false;
            }
            MediaLocator audioCaptureDeviceLocator = audioCaptureDevice.getLocator();
            if (audioCaptureDeviceLocator == null) {
                return false;
            }
            try {
                boolean z;
                this.dataSource = Manager.createDataSource(audioCaptureDeviceLocator);
                AudioFormat af = (AudioFormat) ((CaptureDevice) this.dataSource).getFormatControls()[0].getFormat();
                int frameSize = (af.getSampleSizeInBits() / 8) * af.getChannels();
                ZrtpFortunaEntropyGatherer.this.bytesToGather = ((int) (af.getSampleRate() * 2.0d)) * frameSize;
                ZrtpFortunaEntropyGatherer.this.bytes20ms = ((int) (af.getSampleRate() / 50.0d)) * frameSize;
                if (this.dataSource instanceof PullBufferDataSource) {
                    this.audioStream = ((PullBufferDataSource) this.dataSource).getStreams()[0];
                } else {
                    this.audioStream = ((PushBufferDataSource) this.dataSource).getStreams()[0];
                    ((PushBufferStream) this.audioStream).setTransferHandler(this);
                }
                if (this.audioStream != null) {
                    z = true;
                } else {
                    z = false;
                }
                return z;
            } catch (NoDataSourceException e) {
                ZrtpFortunaEntropyGatherer.logger.warn("No data source during entropy preparation", e);
                return false;
            } catch (IOException e2) {
                ZrtpFortunaEntropyGatherer.logger.warn("Got an IO Exception during entropy preparation", e2);
                return false;
            }
        }

        public void transferData(PushBufferStream stream) {
            try {
                stream.read(this.firstBuf);
            } catch (IOException e) {
                ZrtpFortunaEntropyGatherer.logger.warn("Got IOException during transfer data", e);
            }
            synchronized (this.bufferSync) {
                this.bufferAvailable = true;
                this.bufferSync.notifyAll();
            }
        }

        public void run() {
            ZrtpFortuna fortuna = ZrtpFortuna.getInstance();
            if (this.dataSource != null && this.audioStream != null) {
                try {
                    this.dataSource.start();
                    int i = 0;
                    Random sr = new SecureRandom();
                    while (ZrtpFortunaEntropyGatherer.this.gatheredEntropy < ZrtpFortunaEntropyGatherer.this.bytesToGather) {
                        if (this.audioStream instanceof PushBufferStream) {
                            synchronized (this.bufferSync) {
                                while (!this.bufferAvailable) {
                                    try {
                                        this.bufferSync.wait();
                                    } catch (InterruptedException e) {
                                    }
                                }
                                this.bufferAvailable = false;
                            }
                        } else {
                            ((PullBufferStream) this.audioStream).read(this.firstBuf);
                        }
                        byte[] entropy = (byte[]) this.firstBuf.getData();
                        ZrtpFortunaEntropyGatherer.access$612(ZrtpFortunaEntropyGatherer.this, entropy.length);
                        byte[] srEntropy = new byte[entropy.length];
                        sr.nextBytes(srEntropy);
                        for (int j = 0; j < entropy.length; j++) {
                            entropy[j] = (byte) (entropy[j] ^ srEntropy[j]);
                        }
                        if (i < 32) {
                            fortuna.addSeedMaterial(entropy);
                        } else {
                            fortuna.addSeedMaterial(i % 3, entropy, 0, entropy.length);
                        }
                        i = ZrtpFortunaEntropyGatherer.this.gatheredEntropy / ZrtpFortunaEntropyGatherer.this.bytes20ms;
                    }
                    ZrtpFortunaEntropyGatherer.entropyOk = true;
                    if (ZrtpFortunaEntropyGatherer.logger.isInfoEnabled()) {
                        ZrtpFortunaEntropyGatherer.logger.info("GatherEntropy got: " + ZrtpFortunaEntropyGatherer.this.gatheredEntropy + " bytes");
                    }
                    this.audioStream = null;
                    this.dataSource.disconnect();
                } catch (IOException e2) {
                    this.audioStream = null;
                    this.dataSource.disconnect();
                } catch (Throwable th) {
                    this.audioStream = null;
                    this.dataSource.disconnect();
                    throw th;
                }
                fortuna.nextBytes(new byte[Controller.Realized]);
            }
        }
    }

    static /* synthetic */ int access$612(ZrtpFortunaEntropyGatherer x0, int x1) {
        int i = x0.gatheredEntropy + x1;
        x0.gatheredEntropy = i;
        return i;
    }

    public ZrtpFortunaEntropyGatherer(DeviceConfiguration deviceConfiguration) {
        this.deviceConfiguration = deviceConfiguration;
    }

    public static boolean isEntropyOk() {
        return entropyOk;
    }

    /* access modifiers changed from: protected */
    public int getGatheredEntropy() {
        return this.gatheredEntropy;
    }

    public boolean setEntropy() {
        GatherAudio gatherer = new GatherAudio();
        boolean retValue = gatherer.prepareAudioEntropy();
        if (retValue) {
            gatherer.start();
        }
        return retValue;
    }
}
