package org.jitsi.impl.neomedia.codec.audio.silk;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.impl.neomedia.codec.AbstractCodec2;
import org.jitsi.service.neomedia.control.FECDecoderControl;
import org.jitsi.util.Logger;

public class JavaDecoder extends AbstractCodec2 {
    static final int FRAME_DURATION = 20;
    private static final int MAX_FRAMES_PER_PAYLOAD = 5;
    private static final Format[] SUPPORTED_INPUT_FORMATS = JavaEncoder.SUPPORTED_OUTPUT_FORMATS;
    private static final Format[] SUPPORTED_OUTPUT_FORMATS = JavaEncoder.SUPPORTED_INPUT_FORMATS;
    private SKP_SILK_SDK_DecControlStruct decControl;
    private SKP_Silk_decoder_state decState;
    private short frameLength;
    private int framesPerPayload;
    private long lastSeqNo;
    private short[] lbrrBytes;
    private byte[] lbrrData;
    private final Logger logger;
    /* access modifiers changed from: private */
    public int nbFECDecoded;
    private int nbFECNotDecoded;
    private int nbPacketsDecoded;
    private int nbPacketsLost;
    private final short[] outputLength;

    private class Stats implements FECDecoderControl {
        private Stats() {
        }

        public int fecPacketsDecoded() {
            return JavaDecoder.this.nbFECDecoded;
        }

        public Component getControlComponent() {
            return null;
        }
    }

    public JavaDecoder() {
        super("SILK Decoder", AudioFormat.class, SUPPORTED_OUTPUT_FORMATS);
        this.lastSeqNo = Buffer.SEQUENCE_UNKNOWN;
        this.lbrrBytes = new short[1];
        this.lbrrData = new byte[250];
        this.logger = Logger.getLogger(JavaDecoder.class);
        this.nbFECDecoded = 0;
        this.nbFECNotDecoded = 0;
        this.nbPacketsDecoded = 0;
        this.nbPacketsLost = 0;
        this.outputLength = new short[1];
        this.inputFormats = SUPPORTED_INPUT_FORMATS;
        addControl(new Stats());
    }

    /* access modifiers changed from: protected */
    public void doClose() {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Packets decoded normally: " + this.nbPacketsDecoded);
            this.logger.debug("Packets decoded with FEC: " + this.nbFECDecoded);
            this.logger.debug("Packets lost (subsequent missing):" + this.nbPacketsLost);
            this.logger.debug("Packets lost (no FEC in subsequent): " + this.nbFECNotDecoded);
        }
        this.decState = null;
        this.decControl = null;
    }

    /* access modifiers changed from: protected */
    public void doOpen() throws ResourceUnavailableException {
        this.decState = new SKP_Silk_decoder_state();
        if (DecAPI.SKP_Silk_SDK_InitDecoder(this.decState) != 0) {
            throw new ResourceUnavailableException("DecAPI.SKP_Silk_SDK_InitDecoder");
        }
        AudioFormat inputFormat = (AudioFormat) getInputFormat();
        double sampleRate = inputFormat.getSampleRate();
        int channels = inputFormat.getChannels();
        this.decControl = new SKP_SILK_SDK_DecControlStruct();
        this.decControl.API_sampleRate = (int) sampleRate;
        this.frameLength = (short) ((int) (((20.0d * sampleRate) * ((double) channels)) / 1000.0d));
        this.lastSeqNo = Buffer.SEQUENCE_UNKNOWN;
    }

    /* access modifiers changed from: protected */
    public int doProcess(Buffer inBuffer, Buffer outBuffer) {
        int lostFlag;
        int processed;
        byte[] in = (byte[]) inBuffer.getData();
        int inOffset = inBuffer.getOffset();
        int inLength = inBuffer.getLength();
        short[] out = validateShortArraySize(outBuffer, this.frameLength);
        long seqNo = inBuffer.getSequenceNumber();
        int lostSeqNoCount = AbstractCodec2.calculateLostSeqNoCount(this.lastSeqNo, seqNo);
        boolean decodeFEC = lostSeqNoCount != 0;
        if ((inBuffer.getFlags() & Buffer.FLAG_SKIP_FEC) != 0) {
            decodeFEC = false;
            if (this.logger.isTraceEnabled()) {
                this.logger.trace("Not decoding FEC/PLC for " + seqNo + " because of Buffer.FLAG_SKIP_FEC.");
            }
        }
        if (decodeFEC) {
            this.lbrrBytes[0] = (short) 0;
            DecAPI.SKP_Silk_SDK_search_for_LBRR(in, inOffset, (short) inLength, lostSeqNoCount, this.lbrrData, 0, this.lbrrBytes);
            if (this.logger.isTraceEnabled()) {
                this.logger.trace("Packet loss detected. Last seen " + this.lastSeqNo + ", current " + seqNo);
                this.logger.trace("Looking for FEC data, found " + this.lbrrBytes[0] + " bytes");
            }
            this.outputLength[0] = this.frameLength;
            if (this.lbrrBytes[0] == (short) 0) {
                lostFlag = 1;
            } else if (DecAPI.SKP_Silk_SDK_Decode(this.decState, this.decControl, 0, this.lbrrData, 0, this.lbrrBytes[0], out, 0, this.outputLength) == 0) {
                this.nbFECDecoded++;
                outBuffer.setDuration(20000000);
                outBuffer.setLength(this.outputLength[0]);
                outBuffer.setOffset(0);
                outBuffer.setFlags(outBuffer.getFlags() | 16777216);
                outBuffer.setFlags(outBuffer.getFlags() & -33554433);
                this.lastSeqNo = AbstractCodec2.incrementSeqNo(this.lastSeqNo);
                lostFlag = 0;
                return 2;
            } else {
                this.nbFECNotDecoded++;
                if (lostSeqNoCount != 0) {
                    this.nbPacketsLost += lostSeqNoCount;
                }
                this.lastSeqNo = seqNo;
                lostFlag = 0;
                return 1;
            }
        }
        if (lostSeqNoCount != 0) {
            this.nbPacketsLost += lostSeqNoCount;
        }
        lostFlag = 0;
        this.outputLength[0] = this.frameLength;
        if (DecAPI.SKP_Silk_SDK_Decode(this.decState, this.decControl, lostFlag, in, inOffset, inLength, out, 0, this.outputLength) == 0) {
            outBuffer.setDuration(20000000);
            outBuffer.setLength(this.outputLength[0]);
            outBuffer.setOffset(0);
            if (lostFlag == 0) {
                outBuffer.setFlags(outBuffer.getFlags() & -50331649);
                if (this.decControl.moreInternalDecoderFrames == 0) {
                    this.nbPacketsDecoded++;
                    processed = 0;
                } else {
                    this.framesPerPayload++;
                    if (this.framesPerPayload >= 5) {
                        this.nbPacketsDecoded++;
                        processed = 0;
                    } else {
                        processed = 2;
                    }
                }
                this.lastSeqNo = seqNo;
            } else {
                outBuffer.setFlags(outBuffer.getFlags() & -16777217);
                outBuffer.setFlags(outBuffer.getFlags() | 33554432);
                processed = 2;
                this.lastSeqNo = AbstractCodec2.incrementSeqNo(this.lastSeqNo);
            }
        } else {
            processed = 1;
            if (lostFlag == 1) {
                this.nbFECNotDecoded++;
                if (lostSeqNoCount != 0) {
                    this.nbPacketsLost += lostSeqNoCount;
                }
            }
            this.lastSeqNo = seqNo;
        }
        if ((processed & 2) == 2) {
            return processed;
        }
        this.framesPerPayload = 0;
        return processed;
    }

    /* access modifiers changed from: protected */
    public Format[] getMatchingOutputFormats(Format inputFormat) {
        return JavaEncoder.getMatchingOutputFormats(inputFormat, SUPPORTED_INPUT_FORMATS, SUPPORTED_OUTPUT_FORMATS);
    }
}
