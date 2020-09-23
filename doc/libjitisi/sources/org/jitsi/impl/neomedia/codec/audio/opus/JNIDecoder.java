package org.jitsi.impl.neomedia.codec.audio.opus;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.impl.neomedia.codec.AbstractCodec2;
import org.jitsi.impl.neomedia.jmfext.media.renderer.audio.AbstractAudioRenderer;
import org.jitsi.service.neomedia.codec.Constants;
import org.jitsi.service.neomedia.control.FECDecoderControl;
import org.jitsi.util.Logger;

public class JNIDecoder extends AbstractCodec2 implements FECDecoderControl {
    private static final Format[] SUPPORTED_INPUT_FORMATS = new Format[]{new AudioFormat(Constants.OPUS_RTP)};
    private static final Format[] SUPPORTED_OUTPUT_FORMATS;
    private static final Logger logger = Logger.getLogger(JNIDecoder.class);
    private int channels;
    private long decoder;
    private int lastFrameSizeInSamplesPerChannel;
    private long lastSeqNo;
    private int nbDecodedFec;
    private int outputFrameSize;
    private int outputSampleRate;

    static {
        Format[] formatArr = new Format[1];
        formatArr[0] = new AudioFormat(AudioFormat.LINEAR, 48000.0d, 16, 1, AbstractAudioRenderer.NATIVE_AUDIO_FORMAT_ENDIAN, 1, -1, -1.0d, Format.byteArray);
        SUPPORTED_OUTPUT_FORMATS = formatArr;
        Opus.assertOpusIsFunctional();
    }

    public JNIDecoder() {
        super("Opus JNI Decoder", AudioFormat.class, SUPPORTED_OUTPUT_FORMATS);
        this.channels = 1;
        this.decoder = 0;
        this.lastSeqNo = Buffer.SEQUENCE_UNKNOWN;
        this.nbDecodedFec = 0;
        this.inputFormats = SUPPORTED_INPUT_FORMATS;
        addControl(this);
    }

    /* access modifiers changed from: protected */
    public void doClose() {
        if (this.decoder != 0) {
            Opus.decoder_destroy(this.decoder);
            this.decoder = 0;
        }
    }

    /* access modifiers changed from: protected */
    public void doOpen() throws ResourceUnavailableException {
        if (this.decoder == 0) {
            this.decoder = Opus.decoder_create(this.outputSampleRate, this.channels);
            if (this.decoder == 0) {
                throw new ResourceUnavailableException("opus_decoder_create");
            }
            this.lastFrameSizeInSamplesPerChannel = 0;
            this.lastSeqNo = Buffer.SEQUENCE_UNKNOWN;
        }
    }

    /* access modifiers changed from: protected */
    public int doProcess(Buffer inBuffer, Buffer outBuffer) {
        Format inFormat = inBuffer.getFormat();
        if (inFormat != null && inFormat != this.inputFormat && !inFormat.equals(this.inputFormat) && setInputFormat(inFormat) == null) {
            return 1;
        }
        long seqNo = inBuffer.getSequenceNumber();
        int lostSeqNoCount = AbstractCodec2.calculateLostSeqNoCount(this.lastSeqNo, seqNo);
        boolean decodeFEC = (lostSeqNoCount == 0 || this.lastFrameSizeInSamplesPerChannel == 0) ? false : true;
        if ((inBuffer.getFlags() & Buffer.FLAG_SKIP_FEC) != 0) {
            decodeFEC = false;
            if (logger.isTraceEnabled()) {
                logger.trace("Not decoding FEC/PLC for " + seqNo + " because of Buffer.FLAG_SKIP_FEC.");
            }
        }
        byte[] in = (byte[]) inBuffer.getData();
        int inOffset = inBuffer.getOffset();
        int inLength = inBuffer.getLength();
        int outLength = 0;
        int totalFrameSizeInSamplesPerChannel = 0;
        int frameSizeInSamplesPerChannel;
        int frameSizeInBytes;
        int outOffset;
        if (decodeFEC) {
            if (lostSeqNoCount != 1) {
                inLength = 0;
            }
            frameSizeInSamplesPerChannel = Opus.decode(this.decoder, in, inOffset, inLength, AbstractCodec2.validateByteArraySize(outBuffer, 0 + (this.lastFrameSizeInSamplesPerChannel * this.outputFrameSize), null != null), 0, this.lastFrameSizeInSamplesPerChannel, 1);
            if (frameSizeInSamplesPerChannel > 0) {
                frameSizeInBytes = frameSizeInSamplesPerChannel * this.outputFrameSize;
                outLength = 0 + frameSizeInBytes;
                outOffset = 0 + frameSizeInBytes;
                totalFrameSizeInSamplesPerChannel = 0 + frameSizeInSamplesPerChannel;
                int flags = outBuffer.getFlags();
                int i = (in == null || inLength == 0) ? 33554432 : 16777216;
                outBuffer.setFlags(i | flags);
                this.nbDecodedFec++;
            }
            this.lastSeqNo = AbstractCodec2.incrementSeqNo(this.lastSeqNo);
        } else {
            frameSizeInSamplesPerChannel = Opus.decoder_get_nb_samples(this.decoder, in, inOffset, inLength);
            frameSizeInSamplesPerChannel = Opus.decode(this.decoder, in, inOffset, inLength, AbstractCodec2.validateByteArraySize(outBuffer, 0 + (this.outputFrameSize * frameSizeInSamplesPerChannel), null != null), 0, frameSizeInSamplesPerChannel, 0);
            if (frameSizeInSamplesPerChannel > 0) {
                frameSizeInBytes = frameSizeInSamplesPerChannel * this.outputFrameSize;
                outLength = 0 + frameSizeInBytes;
                outOffset = 0 + frameSizeInBytes;
                totalFrameSizeInSamplesPerChannel = 0 + frameSizeInSamplesPerChannel;
                outBuffer.setFlags(outBuffer.getFlags() & -50331649);
                this.lastFrameSizeInSamplesPerChannel = frameSizeInSamplesPerChannel;
            }
            this.lastSeqNo = seqNo;
        }
        if (outLength > 0) {
            outBuffer.setDuration(((((long) (this.channels * totalFrameSizeInSamplesPerChannel)) * 1000) * 1000) / ((long) this.outputSampleRate));
            outBuffer.setFormat(getOutputFormat());
            outBuffer.setLength(outLength);
            outBuffer.setOffset(0);
        } else {
            outBuffer.setLength(0);
            discardOutputBuffer(outBuffer);
        }
        if (this.lastSeqNo == seqNo) {
            return 0;
        }
        return 2;
    }

    public int fecPacketsDecoded() {
        return this.nbDecodedFec;
    }

    public Component getControlComponent() {
        return null;
    }

    /* access modifiers changed from: protected */
    public Format[] getMatchingOutputFormats(Format inputFormat) {
        AudioFormat af = (AudioFormat) inputFormat;
        return new Format[]{new AudioFormat(AudioFormat.LINEAR, af.getSampleRate(), 16, 1, AbstractAudioRenderer.NATIVE_AUDIO_FORMAT_ENDIAN, 1, -1, -1.0d, Format.byteArray)};
    }

    public Format setInputFormat(Format format) {
        Format inFormat = super.setInputFormat(format);
        if (inFormat != null && this.outputFormat == null) {
            setOutputFormat(SUPPORTED_OUTPUT_FORMATS[0]);
        }
        return inFormat;
    }

    public Format setOutputFormat(Format format) {
        Format setOutputFormat = super.setOutputFormat(format);
        if (setOutputFormat != null) {
            AudioFormat af = (AudioFormat) setOutputFormat;
            this.outputFrameSize = (af.getSampleSizeInBits() / 8) * af.getChannels();
            this.outputSampleRate = (int) af.getSampleRate();
        }
        return setOutputFormat;
    }
}
