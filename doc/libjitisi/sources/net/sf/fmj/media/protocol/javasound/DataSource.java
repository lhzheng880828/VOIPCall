package net.sf.fmj.media.protocol.javasound;

import com.lti.utils.UnsignedUtils;
import com.lti.utils.synchronization.CloseableThread;
import com.lti.utils.synchronization.SynchronizedBoolean;
import com.lti.utils.synchronization.SynchronizedObjectHolder;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.Buffer;
import javax.media.CaptureDeviceInfo;
import javax.media.Format;
import javax.media.Owned;
import javax.media.Time;
import javax.media.control.BufferControl;
import javax.media.control.FormatControl;
import javax.media.control.FrameProcessingControl;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import net.sf.fmj.media.AbstractGainControl;
import net.sf.fmj.media.renderer.audio.JavaSoundUtils;
import net.sf.fmj.utility.LoggerSingleton;
import net.sf.fmj.utility.RingBuffer;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.android.util.javax.sound.sampled.AudioFormat;
import org.jitsi.android.util.javax.sound.sampled.AudioSystem;
import org.jitsi.android.util.javax.sound.sampled.DataLine;
import org.jitsi.android.util.javax.sound.sampled.Line;
import org.jitsi.android.util.javax.sound.sampled.LineUnavailableException;
import org.jitsi.android.util.javax.sound.sampled.Mixer.Info;
import org.jitsi.android.util.javax.sound.sampled.TargetDataLine;

public class DataSource extends PushBufferDataSource implements CaptureDevice {
    private static final String CONTENT_TYPE = "raw";
    private static final boolean TRACE = true;
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = LoggerSingleton.logger;
    /* access modifiers changed from: private */
    public int buflen;
    /* access modifiers changed from: private */
    public long buflenMS = 20;
    /* access modifiers changed from: private */
    public boolean connected;
    protected Object[] controls;
    /* access modifiers changed from: private */
    public boolean enabled = true;
    private Format[] formatsArray;
    private AudioFormat javaSoundAudioFormat;
    /* access modifiers changed from: private */
    public RingBuffer jitterBuffer = new RingBuffer(2);
    /* access modifiers changed from: private */
    public javax.media.format.AudioFormat jmfAudioFormat;
    /* access modifiers changed from: private */
    public PeakVolumeMeter levelControl = new PeakVolumeMeter();
    private MyPushBufferStream pushBufferStream;
    /* access modifiers changed from: private|final */
    public final SynchronizedBoolean started = new SynchronizedBoolean(false);
    /* access modifiers changed from: private */
    public TargetDataLine targetDataLine;

    private class FPC implements FrameProcessingControl, Owned {
        private FPC() {
        }

        public Component getControlComponent() {
            return null;
        }

        public int getFramesDropped() {
            return DataSource.this.jitterBuffer.getOverrunCounter();
        }

        public Object getOwner() {
            return DataSource.this;
        }

        public void setFramesBehind(float numFrames) {
        }

        public boolean setMinimalProcessing(boolean newMinimalProcessing) {
            return false;
        }
    }

    private class JavaSoundBufferControl implements BufferControl, Owned {
        private JavaSoundBufferControl() {
        }

        public long getBufferLength() {
            return DataSource.this.buflenMS;
        }

        public Component getControlComponent() {
            return null;
        }

        public boolean getEnabledThreshold() {
            return false;
        }

        public long getMinimumThreshold() {
            return -1;
        }

        public Object getOwner() {
            return DataSource.this;
        }

        public long setBufferLength(long time) {
            boolean isStarted = false;
            if (DataSource.this.started.getValue()) {
                isStarted = true;
                try {
                    DataSource.this.stop();
                } catch (IOException e) {
                    DataSource.logger.log(Level.WARNING, "" + e, e);
                }
            }
            if (time < 20) {
                time = 20;
            } else if (time > 5000) {
                time = 5000;
            }
            DataSource.this.buflenMS = time;
            if (DataSource.this.connected) {
                DataSource.this.disconnect();
                try {
                    DataSource.this.connect();
                } catch (IOException e2) {
                    DataSource.logger.log(Level.WARNING, "" + e2, e2);
                }
            }
            if (isStarted) {
                try {
                    DataSource.this.start();
                } catch (IOException e22) {
                    DataSource.logger.log(Level.WARNING, "" + e22, e22);
                }
            }
            return DataSource.this.buflenMS;
        }

        public void setEnabledThreshold(boolean b) {
        }

        public long setMinimumThreshold(long time) {
            return -1;
        }
    }

    private class JavaSoundFormatControl implements FormatControl, Owned {
        private JavaSoundFormatControl() {
        }

        public Component getControlComponent() {
            return null;
        }

        public Format getFormat() {
            return DataSource.this.jmfAudioFormat;
        }

        public Object getOwner() {
            return DataSource.this;
        }

        public Format[] getSupportedFormats() {
            return DataSource.this.getSupportedFormats();
        }

        public boolean isEnabled() {
            return DataSource.this.enabled;
        }

        public void setEnabled(boolean enabled) {
            DataSource.this.enabled = enabled;
        }

        public Format setFormat(Format format) {
            DataSource.this.setJMFAudioFormat((javax.media.format.AudioFormat) format);
            if (DataSource.this.connected) {
                DataSource.this.disconnect();
                try {
                    DataSource.this.connect();
                } catch (IOException e) {
                    DataSource.logger.log(Level.WARNING, "" + e, e);
                    return null;
                }
            }
            return DataSource.this.jmfAudioFormat;
        }
    }

    private class JitterBufferControl implements BufferControl, Owned {
        private JitterBufferControl() {
        }

        public long getBufferLength() {
            return DataSource.this.buflenMS * ((long) DataSource.this.jitterBuffer.size());
        }

        public Component getControlComponent() {
            return null;
        }

        public boolean getEnabledThreshold() {
            return false;
        }

        public long getMinimumThreshold() {
            return -1;
        }

        public Object getOwner() {
            return DataSource.this;
        }

        public long setBufferLength(long time) {
            int jitterbuflen = (int) (time / DataSource.this.buflenMS);
            if (jitterbuflen < 1) {
                jitterbuflen = 1;
            }
            DataSource.this.jitterBuffer.resize(jitterbuflen);
            return ((long) jitterbuflen) * DataSource.this.buflenMS;
        }

        public void setEnabledThreshold(boolean b) {
        }

        public long setMinimumThreshold(long time) {
            return -1;
        }
    }

    private class MyPushBufferStream implements PushBufferStream {
        private AvailabilityThread availabilityThread;
        private long sequenceNumber;
        /* access modifiers changed from: private|final */
        public final SynchronizedObjectHolder<BufferTransferHandler> transferHandlerHolder;

        private class AvailabilityThread extends CloseableThread {
            private AvailabilityThread() {
            }

            public void run() {
                DataSource.logger.fine("jitterbuflen=" + DataSource.this.jitterBuffer.size());
                try {
                    byte[] data = new byte[DataSource.this.buflen];
                    while (!isClosing()) {
                        if (DataSource.this.targetDataLine.read(data, 0, data.length) > 0) {
                            BufferTransferHandler handler = (BufferTransferHandler) MyPushBufferStream.this.transferHandlerHolder.getObject();
                            if (!(handler == null || DataSource.this.jitterBuffer.put(data))) {
                                handler.transferData(MyPushBufferStream.this);
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                setClosed();
            }
        }

        private MyPushBufferStream() {
            this.sequenceNumber = 0;
            this.transferHandlerHolder = new SynchronizedObjectHolder();
        }

        public boolean endOfStream() {
            return false;
        }

        public ContentDescriptor getContentDescriptor() {
            return new ContentDescriptor("raw");
        }

        public long getContentLength() {
            return -1;
        }

        public Object getControl(String controlType) {
            return null;
        }

        public Object[] getControls() {
            return new Object[0];
        }

        public Format getFormat() {
            return DataSource.this.jmfAudioFormat;
        }

        public void read(Buffer buffer) throws IOException {
            if (DataSource.this.started.getValue()) {
                try {
                    byte[] data = (byte[]) DataSource.this.jitterBuffer.get();
                    buffer.setFlags(33024);
                    buffer.setOffset(0);
                    buffer.setData(data);
                    buffer.setLength(data.length);
                    buffer.setFormat(DataSource.this.jmfAudioFormat);
                    long j = this.sequenceNumber + 1;
                    this.sequenceNumber = j;
                    buffer.setSequenceNumber(j);
                    buffer.setTimeStamp(System.nanoTime());
                    DataSource.this.levelControl.processData(buffer);
                    return;
                } catch (Exception e) {
                    return;
                }
            }
            buffer.setOffset(0);
            buffer.setLength(0);
            buffer.setDiscard(true);
        }

        public void setTransferHandler(BufferTransferHandler transferHandler) {
            this.transferHandlerHolder.setObject(transferHandler);
        }

        public void startAvailabilityThread() {
            this.availabilityThread = new AvailabilityThread();
            this.availabilityThread.setName("AvailabilityThread for " + this);
            this.availabilityThread.setDaemon(true);
            this.availabilityThread.start();
        }

        public void stopAvailabilityThread() throws InterruptedException {
            if (this.availabilityThread != null) {
                this.availabilityThread.close();
                this.availabilityThread.waitUntilClosed();
                this.availabilityThread = null;
            }
        }
    }

    private class PeakVolumeMeter extends AbstractGainControl {
        float peakLevel;

        private PeakVolumeMeter() {
            this.peakLevel = 0.0f;
        }

        public float getLevel() {
            return this.peakLevel;
        }

        public void processData(Buffer buf) {
            if (!getMute() && !buf.isDiscard() && buf.getLength() > 0) {
                javax.media.format.AudioFormat af = (javax.media.format.AudioFormat) buf.getFormat();
                byte[] data = (byte[]) buf.getData();
                if (af.getEncoding().equalsIgnoreCase(javax.media.format.AudioFormat.LINEAR) && af.getSampleSizeInBits() == 16) {
                    int msb = 0;
                    int lsb = 1;
                    if (af.getEndian() == 0) {
                        msb = 1;
                        lsb = 0;
                    }
                    if (af.getSigned() == 1) {
                        int peak = 0;
                        int samples = data.length / 2;
                        for (int i = 0; i < samples; i++) {
                            int value = (data[(i * 2) + msb] << 8) + (data[(i * 2) + lsb] & UnsignedUtils.MAX_UBYTE);
                            if (value < 0) {
                                value = -value;
                            }
                            if (value > peak) {
                                peak = value;
                            }
                        }
                        this.peakLevel = ((float) peak) / 32768.0f;
                    }
                }
            }
        }

        public float setLevel(float level) {
            return getLevel();
        }
    }

    public static Format[] querySupportedFormats(int mixerIndex) {
        List<javax.media.format.AudioFormat> formats = new ArrayList();
        Info[] mixerInfo = AudioSystem.getMixerInfo();
        if (mixerIndex < 0 || mixerIndex >= mixerInfo.length) {
            return null;
        }
        Line.Info[] infos = AudioSystem.getMixer(mixerInfo[mixerIndex]).getTargetLineInfo();
        for (int i = 0; i < infos.length; i++) {
            if (infos[i] instanceof DataLine.Info) {
                AudioFormat[] af = ((DataLine.Info) infos[i]).getFormats();
                for (AudioFormat convertFormat : af) {
                    javax.media.format.AudioFormat jmfAudioFormat = JavaSoundUtils.convertFormat(convertFormat);
                    if (!formats.contains(jmfAudioFormat)) {
                        formats.add(jmfAudioFormat);
                    }
                }
            }
        }
        Collections.sort(formats, Collections.reverseOrder(new AudioFormatComparator()));
        return (Format[]) formats.toArray(new Format[formats.size()]);
    }

    public DataSource() {
        this.levelControl.setMute(true);
    }

    public void connect() throws IOException {
        logger.fine("connect");
        if (!this.connected) {
            try {
                if (this.jmfAudioFormat == null) {
                    javax.media.format.AudioFormat audioFormat = getSupportedFormats()[0];
                    if (audioFormat.getSampleRate() == -1.0d) {
                        setJMFAudioFormat((javax.media.format.AudioFormat) new javax.media.format.AudioFormat(audioFormat.getEncoding(), 44100.0d, -1, -1).intersects(audioFormat));
                    } else {
                        setJMFAudioFormat(audioFormat);
                    }
                }
                this.targetDataLine = (TargetDataLine) AudioSystem.getMixer(AudioSystem.getMixerInfo()[getMixerIndex()]).getLine(new DataLine.Info(TargetDataLine.class, null));
                logger.fine("targetDataLine=" + this.targetDataLine);
                this.buflen = (int) (((((float) this.javaSoundAudioFormat.getFrameSize()) * this.javaSoundAudioFormat.getSampleRate()) * ((float) this.buflenMS)) / 1000.0f);
                this.targetDataLine.open(this.javaSoundAudioFormat, this.buflen);
                logger.fine("buflen=" + this.buflen);
                this.pushBufferStream = new MyPushBufferStream();
                this.controls = new Object[]{new JavaSoundFormatControl(), new JavaSoundBufferControl(), new JitterBufferControl(), new FPC(), this.levelControl};
                this.connected = true;
            } catch (LineUnavailableException e) {
                logger.log(Level.WARNING, "" + e, e);
                throw new IOException("" + e);
            }
        }
    }

    public void disconnect() {
        logger.fine("disconnect");
        if (this.connected) {
            try {
                stop();
                if (this.targetDataLine != null) {
                    this.targetDataLine.close();
                }
                this.targetDataLine = null;
                this.pushBufferStream = null;
            } catch (IOException e) {
                logger.log(Level.WARNING, "" + e, e);
                this.targetDataLine = null;
                this.pushBufferStream = null;
            } catch (Throwable th) {
                this.targetDataLine = null;
                this.pushBufferStream = null;
                throw th;
            }
            this.connected = false;
        }
    }

    public CaptureDeviceInfo getCaptureDeviceInfo() {
        return new CaptureDeviceInfo(AudioSystem.getMixerInfo()[getMixerIndex()].getName(), getLocator(), getSupportedFormats());
    }

    public String getContentType() {
        return "raw";
    }

    public Object getControl(String controlType) {
        return null;
    }

    public Object[] getControls() {
        return this.controls;
    }

    public Time getDuration() {
        return DURATION_UNBOUNDED;
    }

    public FormatControl[] getFormatControls() {
        return new FormatControl[]{new JavaSoundFormatControl()};
    }

    private int getMixerIndex() {
        int mixerIndex = -1;
        try {
            String remainder = getLocator().getRemainder();
            if (remainder.startsWith("#")) {
                mixerIndex = Integer.parseInt(remainder.substring(1));
            }
        } catch (Exception e) {
        }
        if (-1 != mixerIndex || !getLocator().toString().startsWith("javasound://")) {
            return mixerIndex;
        }
        for (int index = 0; index < 50; index++) {
            Format[] formats = querySupportedFormats(index);
            if (formats != null && formats.length > 0) {
                return index;
            }
        }
        return mixerIndex;
    }

    public PushBufferStream[] getStreams() {
        logger.fine("getStreams");
        return new PushBufferStream[]{this.pushBufferStream};
    }

    /* access modifiers changed from: private */
    public Format[] getSupportedFormats() {
        if (this.formatsArray != null) {
            return this.formatsArray;
        }
        this.formatsArray = querySupportedFormats(getMixerIndex());
        return this.formatsArray;
    }

    private void setJavaSoundAudioFormat(AudioFormat f) {
        this.javaSoundAudioFormat = f;
        this.jmfAudioFormat = JavaSoundUtils.convertFormat(this.javaSoundAudioFormat);
    }

    /* access modifiers changed from: private */
    public void setJMFAudioFormat(javax.media.format.AudioFormat f) {
        this.jmfAudioFormat = f;
        this.javaSoundAudioFormat = JavaSoundUtils.convertFormat(this.jmfAudioFormat);
    }

    public void start() throws IOException {
        logger.fine("start");
        if (!this.started.getValue()) {
            this.targetDataLine.start();
            this.pushBufferStream.startAvailabilityThread();
            this.started.setValue(true);
        }
    }

    public void stop() throws IOException {
        logger.fine("stop");
        if (this.started.getValue()) {
            try {
                if (this.targetDataLine != null) {
                    this.targetDataLine.stop();
                    this.targetDataLine.flush();
                }
                if (this.pushBufferStream != null) {
                    this.pushBufferStream.stopAvailabilityThread();
                }
                this.started.setValue(false);
            } catch (InterruptedException e) {
                throw new InterruptedIOException();
            } catch (Throwable th) {
                this.started.setValue(false);
            }
        }
    }
}
