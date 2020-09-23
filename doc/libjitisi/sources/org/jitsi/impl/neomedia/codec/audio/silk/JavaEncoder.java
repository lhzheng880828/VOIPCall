package org.jitsi.impl.neomedia.codec.audio.silk;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.impl.neomedia.codec.AbstractCodec2;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.neomedia.codec.Constants;
import org.jitsi.service.neomedia.control.PacketLossAwareEncoder;
import org.jitsi.util.Logger;

public class JavaEncoder extends AbstractCodec2 implements PacketLossAwareEncoder {
    private static final int BITRATE = 40000;
    private static final int COMPLEXITY = 2;
    static final int MAX_BYTES_PER_FRAME = 250;
    private static final int MIN_PACKET_LOSS_PERCENTAGE = 3;
    static final Format[] SUPPORTED_INPUT_FORMATS;
    static final Format[] SUPPORTED_OUTPUT_FORMATS;
    private static final double[] SUPPORTED_SAMPLE_RATES = new double[]{8000.0d, 12000.0d, 16000.0d, 24000.0d};
    private static final boolean USE_DTX = false;
    private boolean alwaysAssumePacketLoss;
    /* access modifiers changed from: private */
    public int duration;
    private SKP_SILK_SDK_EncControlStruct encControl;
    private SKP_Silk_encoder_state_FLP encState;
    private final Logger logger;
    private final short[] outputLength;
    private boolean useFec;

    static {
        int supportedCount = SUPPORTED_SAMPLE_RATES.length;
        SUPPORTED_INPUT_FORMATS = new Format[supportedCount];
        SUPPORTED_OUTPUT_FORMATS = new Format[supportedCount];
        for (int i = 0; i < supportedCount; i++) {
            double supportedSampleRate = SUPPORTED_SAMPLE_RATES[i];
            SUPPORTED_INPUT_FORMATS[i] = new AudioFormat(AudioFormat.LINEAR, supportedSampleRate, 16, 1, 0, 1, -1, -1.0d, Format.shortArray);
            SUPPORTED_OUTPUT_FORMATS[i] = new AudioFormat(Constants.SILK_RTP, supportedSampleRate, -1, 1, -1, -1, -1, -1.0d, Format.byteArray);
        }
    }

    public JavaEncoder() {
        super("SILK Encoder", AudioFormat.class, SUPPORTED_OUTPUT_FORMATS);
        this.logger = Logger.getLogger(JavaEncoder.class);
        this.alwaysAssumePacketLoss = true;
        this.duration = 20000000;
        this.outputLength = new short[1];
        this.inputFormats = SUPPORTED_INPUT_FORMATS;
        ConfigurationService cfg = LibJitsi.getConfigurationService();
        this.useFec = cfg.getBoolean(Constants.PROP_SILK_FEC, true);
        this.alwaysAssumePacketLoss = cfg.getBoolean(Constants.PROP_SILK_ASSUME_PL, true);
        String satStr = cfg.getString(Constants.PROP_SILK_FEC_SAT, "0.5");
        float sat = DefineFLP.LBRR_SPEECH_ACTIVITY_THRES;
        if (!(satStr == null || satStr.length() == 0)) {
            try {
                sat = Float.parseFloat(satStr);
            } catch (NumberFormatException e) {
            }
        }
        DefineFLP.LBRR_SPEECH_ACTIVITY_THRES = sat;
        addControl(this);
    }

    /* access modifiers changed from: protected */
    public void doClose() {
        this.encState = null;
        this.encControl = null;
    }

    /* access modifiers changed from: protected */
    public void doOpen() throws ResourceUnavailableException {
        int i = 0;
        this.encState = new SKP_Silk_encoder_state_FLP();
        this.encControl = new SKP_SILK_SDK_EncControlStruct();
        if (EncAPI.SKP_Silk_SDK_InitEncoder(this.encState, this.encControl) != 0) {
            throw new ResourceUnavailableException("EncAPI.SKP_Silk_SDK_InitEncoder");
        }
        AudioFormat inputFormat = (AudioFormat) getInputFormat();
        double sampleRate = inputFormat.getSampleRate();
        int channels = inputFormat.getChannels();
        this.encControl.API_sampleRate = (int) sampleRate;
        this.encControl.bitRate = BITRATE;
        this.encControl.complexity = 2;
        this.encControl.maxInternalSampleRate = this.encControl.API_sampleRate;
        setExpectedPacketLoss(0);
        this.encControl.packetSize = (int) (((20.0d * sampleRate) * ((double) channels)) / 1000.0d);
        this.encControl.useDTX = 0;
        SKP_SILK_SDK_EncControlStruct sKP_SILK_SDK_EncControlStruct = this.encControl;
        if (this.useFec) {
            i = 1;
        }
        sKP_SILK_SDK_EncControlStruct.useInBandFEC = i;
    }

    /* access modifiers changed from: protected */
    public int doProcess(Buffer inputBuffer, Buffer outputBuffer) {
        int processed;
        short[] inputData = (short[]) inputBuffer.getData();
        int inputLength = inputBuffer.getLength();
        int inputOffset = inputBuffer.getOffset();
        if (inputLength > this.encControl.packetSize) {
            inputLength = this.encControl.packetSize;
        }
        byte[] outputData = AbstractCodec2.validateByteArraySize(outputBuffer, MAX_BYTES_PER_FRAME, false);
        this.outputLength[0] = (short) 250;
        if (EncAPI.SKP_Silk_SDK_Encode(this.encState, this.encControl, inputData, inputOffset, inputLength, outputData, 0, this.outputLength) == 0) {
            outputBuffer.setLength(this.outputLength[0]);
            outputBuffer.setOffset(0);
            processed = 0;
        } else {
            processed = 1;
        }
        inputBuffer.setLength(inputBuffer.getLength() - inputLength);
        inputBuffer.setOffset(inputBuffer.getOffset() + inputLength);
        if (processed == 1) {
            return processed;
        }
        if (processed == 0) {
            updateOutput(outputBuffer, getOutputFormat(), outputBuffer.getLength(), outputBuffer.getOffset());
            outputBuffer.setDuration((long) this.duration);
        }
        if (inputBuffer.getLength() > 0) {
            return processed | 2;
        }
        return processed;
    }

    /* access modifiers changed from: protected */
    public Format[] getMatchingOutputFormats(Format inputFormat) {
        return getMatchingOutputFormats(inputFormat, SUPPORTED_INPUT_FORMATS, SUPPORTED_OUTPUT_FORMATS);
    }

    static Format[] getMatchingOutputFormats(Format inputFormat, Format[] supportedInputFormats, Format[] supportedOutputFormats) {
        if (inputFormat == null) {
            return supportedOutputFormats;
        }
        Format matchingInputFormat = AbstractCodec2.matches(inputFormat, supportedInputFormats);
        if (matchingInputFormat == null) {
            return new Format[0];
        }
        if (AbstractCodec2.matches(new AudioFormat(null, ((AudioFormat) matchingInputFormat.intersects(inputFormat)).getSampleRate(), -1, -1, -1, -1, -1, -1.0d, null), supportedOutputFormats) == null) {
            return new Format[0];
        }
        return new Format[]{AbstractCodec2.matches(new AudioFormat(null, ((AudioFormat) matchingInputFormat.intersects(inputFormat)).getSampleRate(), -1, -1, -1, -1, -1, -1.0d, null), supportedOutputFormats).intersects(new AudioFormat(null, ((AudioFormat) matchingInputFormat.intersects(inputFormat)).getSampleRate(), -1, -1, -1, -1, -1, -1.0d, null))};
    }

    public Format getOutputFormat() {
        Format outputFormat = super.getOutputFormat();
        if (outputFormat == null || outputFormat.getClass() != AudioFormat.class) {
            return outputFormat;
        }
        AudioFormat outputAudioFormat = (AudioFormat) outputFormat;
        return setOutputFormat(new AudioFormat(outputAudioFormat.getEncoding(), outputAudioFormat.getSampleRate(), outputAudioFormat.getSampleSizeInBits(), outputAudioFormat.getChannels(), outputAudioFormat.getEndian(), outputAudioFormat.getSigned(), outputAudioFormat.getFrameSizeInBits(), outputAudioFormat.getFrameRate(), outputAudioFormat.getDataType()) {
            private static final long serialVersionUID = 0;

            public long computeDuration(long length) {
                return (long) JavaEncoder.this.duration;
            }
        });
    }

    public void setExpectedPacketLoss(int percentage) {
        if (this.opened) {
            if (this.alwaysAssumePacketLoss && 3 >= percentage) {
                percentage = 3;
            }
            this.encControl.packetLossPercentage = percentage;
            if (this.logger.isTraceEnabled()) {
                this.logger.trace("Setting expected packet loss to: " + percentage);
            }
        }
    }

    public Component getControlComponent() {
        return null;
    }
}
