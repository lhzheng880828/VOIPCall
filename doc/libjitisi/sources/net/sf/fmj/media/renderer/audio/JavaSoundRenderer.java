package net.sf.fmj.media.renderer.audio;

import com.lti.utils.UnsignedUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.Buffer;
import javax.media.Codec;
import javax.media.Format;
import javax.media.Owned;
import javax.media.Renderer;
import javax.media.ResourceUnavailableException;
import javax.media.control.BufferControl;
import javax.media.control.FrameProcessingControl;
import net.sf.fmj.media.AbstractGainControl;
import net.sf.fmj.media.AudioFormatCompleter;
import net.sf.fmj.media.codec.audio.ulaw.Decoder;
import net.sf.fmj.utility.ControlCollection;
import net.sf.fmj.utility.LoggerSingleton;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.android.util.javax.sound.sampled.AudioFormat;
import org.jitsi.android.util.javax.sound.sampled.AudioSystem;
import org.jitsi.android.util.javax.sound.sampled.BooleanControl;
import org.jitsi.android.util.javax.sound.sampled.CompoundControl;
import org.jitsi.android.util.javax.sound.sampled.CompoundControl.Type;
import org.jitsi.android.util.javax.sound.sampled.Control;
import org.jitsi.android.util.javax.sound.sampled.DataLine.Info;
import org.jitsi.android.util.javax.sound.sampled.FloatControl;
import org.jitsi.android.util.javax.sound.sampled.LineUnavailableException;
import org.jitsi.android.util.javax.sound.sampled.SourceDataLine;

public class JavaSoundRenderer implements Renderer {
    private static final boolean NON_BLOCKING = false;
    private static final Logger logger = LoggerSingleton.logger;
    private AudioFormat audioFormat;
    /* access modifiers changed from: private */
    public Boolean bufferSizeChanged = new Boolean(false);
    private int buflen;
    /* access modifiers changed from: private */
    public long buflenMS = -1;
    private Codec codec;
    private final Buffer codecBuffer = new Buffer();
    private final ControlCollection controls = new ControlCollection();
    /* access modifiers changed from: private */
    public int framesDropped = 0;
    private javax.media.format.AudioFormat inputFormat;
    private long lastSequenceNumber = -1;
    private PeakVolumeMeter levelControl;
    private String name = "FMJ Audio Renderer";
    private AudioFormat sampledFormat;
    private SourceDataLine sourceLine;
    private Format[] supportedInputFormats;

    private class FPC implements FrameProcessingControl, Owned {
        private FPC() {
        }

        public Component getControlComponent() {
            return null;
        }

        public int getFramesDropped() {
            return JavaSoundRenderer.this.framesDropped;
        }

        public Object getOwner() {
            return JavaSoundRenderer.this;
        }

        public void setFramesBehind(float numFrames) {
        }

        public boolean setMinimalProcessing(boolean newMinimalProcessing) {
            return false;
        }
    }

    private class JavaSoundRendererBufferControl implements BufferControl, Owned {
        private JavaSoundRendererBufferControl() {
        }

        public long getBufferLength() {
            return JavaSoundRenderer.this.buflenMS;
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
            return JavaSoundRenderer.this;
        }

        public long setBufferLength(long time) {
            JavaSoundRenderer.this.buflenMS = time;
            synchronized (JavaSoundRenderer.this.bufferSizeChanged) {
                JavaSoundRenderer.this.bufferSizeChanged = Boolean.TRUE;
            }
            return JavaSoundRenderer.this.buflenMS;
        }

        public void setEnabledThreshold(boolean b) {
        }

        public long setMinimumThreshold(long time) {
            return -1;
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

    public JavaSoundRenderer() {
        Format[] formatArr = new Format[3];
        formatArr[0] = new javax.media.format.AudioFormat(javax.media.format.AudioFormat.LINEAR, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray);
        formatArr[1] = new javax.media.format.AudioFormat(javax.media.format.AudioFormat.ULAW, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray);
        formatArr[2] = new javax.media.format.AudioFormat(javax.media.format.AudioFormat.ALAW, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray);
        this.supportedInputFormats = formatArr;
        this.levelControl = new PeakVolumeMeter();
        this.levelControl.setMute(true);
    }

    public void close() {
        logger.info("JavaSoundRenderer closing...");
        this.controls.clear();
        if (this.codec != null) {
            this.codec.close();
            this.codec = null;
        }
        this.sourceLine.close();
        this.sourceLine = null;
    }

    public Object getControl(String controlType) {
        return this.controls.getControl(controlType);
    }

    public Object[] getControls() {
        return this.controls.getControls();
    }

    public String getName() {
        return this.name;
    }

    public Format[] getSupportedInputFormats() {
        return this.supportedInputFormats;
    }

    public int hashCode() {
        return super.hashCode();
    }

    private void logControls(Control[] controls) {
        for (Control control : controls) {
            logger.fine("control: " + control);
            if (control.getType() instanceof Type) {
                logControls(((CompoundControl) control).getMemberControls());
            }
        }
    }

    public void open() throws ResourceUnavailableException {
        this.audioFormat = JavaSoundUtils.convertFormat(this.inputFormat);
        logger.info("JavaSoundRenderer opening with javax.sound format: " + this.audioFormat);
        if (!this.inputFormat.getEncoding().equals(javax.media.format.AudioFormat.LINEAR)) {
            logger.info("JavaSoundRenderer: Audio format is not linear, creating conversion");
            if (this.inputFormat.getEncoding().equals(javax.media.format.AudioFormat.ULAW)) {
                this.codec = new Decoder();
            } else if (this.inputFormat.getEncoding().equals(javax.media.format.AudioFormat.ALAW)) {
                this.codec = new net.sf.fmj.media.codec.audio.alaw.Decoder();
            } else {
                throw new ResourceUnavailableException("Unsupported input format encoding: " + this.inputFormat.getEncoding());
            }
            if (this.codec.setInputFormat(this.inputFormat) == null) {
                throw new ResourceUnavailableException("Codec rejected input format: " + this.inputFormat);
            }
            Format[] outputFormats = this.codec.getSupportedOutputFormats(this.inputFormat);
            if (outputFormats.length < 1) {
                throw new ResourceUnavailableException("Unable to get an output format for input format: " + this.inputFormat);
            }
            javax.media.format.AudioFormat codecOutputFormat = AudioFormatCompleter.complete((javax.media.format.AudioFormat) outputFormats[0]);
            if (this.codec.setOutputFormat(codecOutputFormat) == null) {
                throw new ResourceUnavailableException("Codec rejected output format: " + codecOutputFormat);
            }
            this.audioFormat = JavaSoundUtils.convertFormat(codecOutputFormat);
            this.codec.open();
            logger.info("JavaSoundRenderer: Audio format is not linear, created conversion from " + this.inputFormat + " to " + codecOutputFormat);
        }
        this.sourceLine = (SourceDataLine) AudioSystem.getLine(new Info(SourceDataLine.class, this.audioFormat));
        logger.info("JavaSoundRenderer: sourceLine=" + this.sourceLine);
        this.sourceLine.open(this.audioFormat);
        logger.info("JavaSoundRenderer: buflen=" + this.sourceLine.getBufferSize());
        FloatControl gainFloatControl = null;
        try {
            gainFloatControl = (FloatControl) this.sourceLine.getControl(FloatControl.Type.MASTER_GAIN);
        } catch (Exception e) {
            logger.log(Level.WARNING, "" + e, e);
        }
        try {
            logger.fine("JavaSoundRenderer: gainFloatControl=" + gainFloatControl);
            BooleanControl muteBooleanControl = null;
            try {
                muteBooleanControl = (BooleanControl) this.sourceLine.getControl(BooleanControl.Type.MUTE);
            } catch (Exception e2) {
                logger.log(Level.WARNING, "" + e2, e2);
            }
            logger.fine("JavaSoundRenderer: muteBooleanControl=" + muteBooleanControl);
            this.controls.addControl(new JavaSoundGainControl(gainFloatControl, muteBooleanControl));
            this.controls.addControl(new JavaSoundRendererBufferControl());
            this.controls.addControl(new FPC());
            this.controls.addControl(this.levelControl);
        } catch (LineUnavailableException e3) {
            throw new ResourceUnavailableException(e3.getMessage());
        }
    }

    public int process(Buffer buffer) {
        if (this.codec != null) {
            int codecResult = this.codec.process(buffer, this.codecBuffer);
            if (codecResult == 1) {
                return 1;
            }
            if (codecResult == 4) {
                return 0;
            }
            this.codecBuffer.setTimeStamp(buffer.getTimeStamp());
            this.codecBuffer.setFlags(buffer.getFlags());
            this.codecBuffer.setSequenceNumber(buffer.getSequenceNumber());
            buffer = this.codecBuffer;
        }
        this.levelControl.processData(buffer);
        int length = buffer.getLength();
        int offset = buffer.getOffset();
        if (buffer.getFormat().getDataType() != Format.byteArray) {
            return 1;
        }
        byte[] data = (byte[]) buffer.getData();
        try {
            synchronized (this.bufferSizeChanged) {
                if (this.bufferSizeChanged.booleanValue()) {
                    this.bufferSizeChanged = Boolean.FALSE;
                    this.sourceLine.stop();
                    this.sourceLine.flush();
                    this.sourceLine.close();
                    this.buflen = (int) (((((float) this.audioFormat.getFrameSize()) * this.audioFormat.getSampleRate()) * ((float) this.buflenMS)) / 1000.0f);
                    this.sourceLine.open(this.audioFormat, this.buflen);
                    logger.info("JavaSoundRenderer: buflen=" + this.sourceLine.getBufferSize());
                    this.sourceLine.start();
                }
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "" + ex, ex);
        }
        if (length == 0) {
            logger.finer("Buffer has zero length, flags = " + buffer.getFlags());
        }
        if (-1 == this.lastSequenceNumber) {
            this.lastSequenceNumber = buffer.getSequenceNumber();
        } else {
            if (((short) ((int) (this.lastSequenceNumber + 1))) != ((short) ((int) buffer.getSequenceNumber()))) {
                this.framesDropped += ((((short) ((int) buffer.getSequenceNumber())) - ((short) ((int) this.lastSequenceNumber))) & 65535) - 1;
            }
            this.lastSequenceNumber = buffer.getSequenceNumber();
        }
        while (length > 0) {
            int n = this.sourceLine.write(data, offset, length);
            Thread.yield();
            if (n >= length) {
                break;
            } else if (n == 0) {
                logger.warning("sourceLine.write returned 0, offset=" + offset + "; length=" + length + "; available=" + this.sourceLine.available() + "; frame size in bytes" + this.sourceLine.getFormat().getFrameSize() + "; sourceLine.isActive() = " + this.sourceLine.isActive() + "; " + this.sourceLine.isOpen() + "; sourceLine.isRunning()=" + this.sourceLine.isRunning());
                return 1;
            } else {
                offset += n;
                length -= n;
            }
        }
        if (false) {
            buffer.setLength(0);
            buffer.setOffset(0);
            return 2;
        }
        if (buffer.isEOM()) {
            this.sourceLine.drain();
        }
        return 0;
    }

    public void reset() {
        logger.info("JavaSoundRenderer resetting...");
    }

    public Format setInputFormat(Format format) {
        logger.info("JavaSoundRenderer setting input format to: " + format);
        if (!(format instanceof javax.media.format.AudioFormat)) {
            return null;
        }
        this.inputFormat = (javax.media.format.AudioFormat) format;
        return this.inputFormat;
    }

    public void start() {
        logger.info("JavaSoundRenderer starting...");
        this.sourceLine.start();
    }

    public void stop() {
        logger.info("JavaSoundRenderer stopping...");
        this.sourceLine.stop();
    }
}
