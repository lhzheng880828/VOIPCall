package org.jitsi.impl.neomedia.codec.audio.opus;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.impl.neomedia.codec.AbstractCodec2;
import org.jitsi.impl.neomedia.jmfext.media.renderer.audio.AbstractAudioRenderer;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.neomedia.codec.Constants;
import org.jitsi.service.neomedia.control.FormatParametersAwareCodec;
import org.jitsi.service.neomedia.control.PacketLossAwareEncoder;
import org.jitsi.util.Logger;

public class JNIEncoder extends AbstractCodec2 implements FormatParametersAwareCodec, PacketLossAwareEncoder {
    private static final Format[] SUPPORTED_INPUT_FORMATS;
    static final double[] SUPPORTED_INPUT_SAMPLE_RATES = new double[]{48000.0d};
    private static final Format[] SUPPORTED_OUTPUT_FORMATS = new Format[]{new AudioFormat(Constants.OPUS_RTP, 48000.0d, -1, 2, -1, -1, -1, -1.0d, Format.byteArray)};
    private static final Logger logger = Logger.getLogger(JNIEncoder.class);
    private int bandwidth;
    private int bitrate;
    private int channels;
    private int complexity;
    private long encoder;
    private int frameSizeInBytes;
    private final int frameSizeInMillis;
    private int frameSizeInSamplesPerChannel;
    private int minPacketLoss;
    private byte[] prevIn;
    private int prevInLength;
    private boolean useDtx;
    private boolean useFec;

    static {
        Opus.assertOpusIsFunctional();
        int supportedInputCount = SUPPORTED_INPUT_SAMPLE_RATES.length;
        SUPPORTED_INPUT_FORMATS = new Format[supportedInputCount];
        for (int i = 0; i < supportedInputCount; i++) {
            SUPPORTED_INPUT_FORMATS[i] = new AudioFormat(AudioFormat.LINEAR, SUPPORTED_INPUT_SAMPLE_RATES[i], 16, 1, AbstractAudioRenderer.NATIVE_AUDIO_FORMAT_ENDIAN, 1, -1, -1.0d, Format.byteArray);
        }
    }

    public JNIEncoder() {
        super("Opus JNI Encoder", AudioFormat.class, SUPPORTED_OUTPUT_FORMATS);
        this.channels = 1;
        this.encoder = 0;
        this.frameSizeInMillis = 20;
        this.minPacketLoss = 0;
        this.prevIn = null;
        this.prevInLength = 0;
        this.inputFormats = SUPPORTED_INPUT_FORMATS;
        addControl(this);
    }

    /* access modifiers changed from: protected */
    public void doClose() {
        if (this.encoder != 0) {
            Opus.encoder_destroy(this.encoder);
            this.encoder = 0;
        }
    }

    /* access modifiers changed from: protected */
    public void doOpen() throws ResourceUnavailableException {
        int i = 1;
        AudioFormat inputFormat = (AudioFormat) getInputFormat();
        int sampleRate = (int) inputFormat.getSampleRate();
        this.channels = inputFormat.getChannels();
        this.encoder = Opus.encoder_create(sampleRate, this.channels);
        if (this.encoder == 0) {
            throw new ResourceUnavailableException("opus_encoder_create()");
        }
        int i2;
        ConfigurationService cfg = LibJitsi.getConfigurationService();
        String bandwidthStr = cfg.getString(Constants.PROP_OPUS_BANDWIDTH, "auto");
        this.bandwidth = Opus.OPUS_AUTO;
        if ("fb".equals(bandwidthStr)) {
            this.bandwidth = Opus.BANDWIDTH_FULLBAND;
        } else if ("swb".equals(bandwidthStr)) {
            this.bandwidth = Opus.BANDWIDTH_SUPERWIDEBAND;
        } else if ("wb".equals(bandwidthStr)) {
            this.bandwidth = Opus.BANDWIDTH_WIDEBAND;
        } else if ("mb".equals(bandwidthStr)) {
            this.bandwidth = Opus.BANDWIDTH_MEDIUMBAND;
        } else if ("nb".equals(bandwidthStr)) {
            this.bandwidth = Opus.BANDWIDTH_NARROWBAND;
        }
        Opus.encoder_set_bandwidth(this.encoder, this.bandwidth);
        this.bitrate = cfg.getInt(Constants.PROP_OPUS_BITRATE, 32) * 1000;
        if (this.bitrate < 500) {
            this.bitrate = 500;
        } else if (this.bitrate > 512000) {
            this.bitrate = 512000;
        }
        Opus.encoder_set_bitrate(this.encoder, this.bitrate);
        this.complexity = cfg.getInt(Constants.PROP_OPUS_COMPLEXITY, 0);
        if (this.complexity != 0) {
            Opus.encoder_set_complexity(this.encoder, this.complexity);
        }
        this.useFec = cfg.getBoolean(Constants.PROP_OPUS_FEC, true);
        long j = this.encoder;
        if (this.useFec) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        Opus.encoder_set_inband_fec(j, i2);
        this.minPacketLoss = cfg.getInt(Constants.PROP_OPUS_MIN_EXPECTED_PACKET_LOSS, 1);
        Opus.encoder_set_packet_loss_perc(this.encoder, this.minPacketLoss);
        this.useDtx = cfg.getBoolean(Constants.PROP_OPUS_DTX, true);
        j = this.encoder;
        if (!this.useDtx) {
            i = 0;
        }
        Opus.encoder_set_dtx(j, i);
        if (logger.isDebugEnabled()) {
            String bw;
            switch (Opus.encoder_get_bandwidth(this.encoder)) {
                case Opus.BANDWIDTH_MEDIUMBAND /*1102*/:
                    bw = "mb";
                    break;
                case Opus.BANDWIDTH_WIDEBAND /*1103*/:
                    bw = "wb";
                    break;
                case Opus.BANDWIDTH_SUPERWIDEBAND /*1104*/:
                    bw = "swb";
                    break;
                case Opus.BANDWIDTH_FULLBAND /*1105*/:
                    bw = "fb";
                    break;
                default:
                    bw = "nb";
                    break;
            }
            logger.debug("Encoder settings: audio bandwidth " + bw + ", bitrate " + Opus.encoder_get_bitrate(this.encoder) + ", DTX " + Opus.encoder_get_dtx(this.encoder) + ", FEC " + Opus.encoder_get_inband_fec(this.encoder));
        }
    }

    /* access modifiers changed from: protected */
    public int doProcess(Buffer inBuffer, Buffer outBuffer) {
        Format inFormat = inBuffer.getFormat();
        if (inFormat != null && inFormat != this.inputFormat && !inFormat.equals(this.inputFormat) && setInputFormat(inFormat) == null) {
            return 1;
        }
        byte[] in = (byte[]) inBuffer.getData();
        int inLength = inBuffer.getLength();
        int inOffset = inBuffer.getOffset();
        if (this.prevIn != null && this.prevInLength > 0) {
            if (this.prevInLength < this.frameSizeInBytes) {
                if (this.prevIn.length < this.frameSizeInBytes) {
                    byte[] newPrevIn = new byte[this.frameSizeInBytes];
                    System.arraycopy(this.prevIn, 0, newPrevIn, 0, this.prevIn.length);
                    this.prevIn = newPrevIn;
                }
                int bytesToCopyFromInToPrevIn = Math.min(this.frameSizeInBytes - this.prevInLength, inLength);
                if (bytesToCopyFromInToPrevIn > 0) {
                    System.arraycopy(in, inOffset, this.prevIn, this.prevInLength, bytesToCopyFromInToPrevIn);
                    this.prevInLength += bytesToCopyFromInToPrevIn;
                    inLength -= bytesToCopyFromInToPrevIn;
                    inBuffer.setLength(inLength);
                    inBuffer.setOffset(inOffset + bytesToCopyFromInToPrevIn);
                }
            }
            if (this.prevInLength == this.frameSizeInBytes) {
                in = this.prevIn;
                inOffset = 0;
                this.prevInLength = 0;
            } else {
                outBuffer.setLength(0);
                discardOutputBuffer(outBuffer);
                if (inLength < 1) {
                    return 0;
                }
                return 2;
            }
        } else if (inLength < 1) {
            outBuffer.setLength(0);
            discardOutputBuffer(outBuffer);
            return 0;
        } else if (inLength < this.frameSizeInBytes) {
            if (this.prevIn == null || this.prevIn.length < inLength) {
                this.prevIn = new byte[this.frameSizeInBytes];
            }
            System.arraycopy(in, inOffset, this.prevIn, 0, inLength);
            this.prevInLength = inLength;
            outBuffer.setLength(0);
            discardOutputBuffer(outBuffer);
            return 0;
        } else {
            inLength -= this.frameSizeInBytes;
            inBuffer.setLength(inLength);
            inBuffer.setOffset(this.frameSizeInBytes + inOffset);
        }
        byte[] out = AbstractCodec2.validateByteArraySize(outBuffer, Opus.MAX_PACKET, false);
        int outLength = Opus.encode(this.encoder, in, inOffset, this.frameSizeInSamplesPerChannel, out, 0, out.length);
        if (outLength < 0) {
            return 1;
        }
        if (outLength > 0) {
            outBuffer.setDuration(20000000);
            outBuffer.setFormat(getOutputFormat());
            outBuffer.setLength(outLength);
            outBuffer.setOffset(0);
        }
        if (inLength < 1) {
            return 0;
        }
        return 2;
    }

    public Component getControlComponent() {
        return null;
    }

    public Format getOutputFormat() {
        Format f = super.getOutputFormat();
        if (f == null || f.getClass() != AudioFormat.class) {
            return f;
        }
        AudioFormat af = (AudioFormat) f;
        return setOutputFormat(new AudioFormat(af.getEncoding(), af.getSampleRate(), af.getSampleSizeInBits(), af.getChannels(), af.getEndian(), af.getSigned(), af.getFrameSizeInBits(), af.getFrameRate(), af.getDataType()) {
            public long computeDuration(long length) {
                return 20000000;
            }
        });
    }

    public void setExpectedPacketLoss(int percentage) {
        if (this.opened) {
            Opus.encoder_set_packet_loss_perc(this.encoder, percentage > this.minPacketLoss ? percentage : this.minPacketLoss);
            if (logger.isTraceEnabled()) {
                logger.trace("Updating expected packet loss: " + percentage + " (minimum " + this.minPacketLoss + ")");
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x0085  */
    public void setFormatParameters(java.util.Map<java.lang.String, java.lang.String> r11) {
        /*
        r10 = this;
        r4 = 1;
        r5 = 0;
        r6 = logger;
        r6 = r6.isDebugEnabled();
        if (r6 == 0) goto L_0x0022;
    L_0x000a:
        r6 = logger;
        r7 = new java.lang.StringBuilder;
        r7.<init>();
        r8 = "Setting format parameters: ";
        r7 = r7.append(r8);
        r7 = r7.append(r11);
        r7 = r7.toString();
        r6.debug(r7);
    L_0x0022:
        r0 = 40000; // 0x9c40 float:5.6052E-41 double:1.97626E-319;
        r6 = "maxaveragebitrate";
        r1 = r11.get(r6);	 Catch:{ Exception -> 0x0087 }
        r1 = (java.lang.String) r1;	 Catch:{ Exception -> 0x0087 }
        if (r1 == 0) goto L_0x0039;
    L_0x002f:
        r6 = r1.length();	 Catch:{ Exception -> 0x0087 }
        if (r6 == 0) goto L_0x0039;
    L_0x0035:
        r0 = java.lang.Integer.parseInt(r1);	 Catch:{ Exception -> 0x0087 }
    L_0x0039:
        r6 = r10.encoder;
        r8 = r10.bitrate;
        if (r0 >= r8) goto L_0x007c;
    L_0x003f:
        org.jitsi.impl.neomedia.codec.audio.opus.Opus.encoder_set_bitrate(r6, r0);
        r6 = r10.useDtx;
        if (r6 == 0) goto L_0x007f;
    L_0x0046:
        r6 = "1";
        r7 = "usedtx";
        r7 = r11.get(r7);
        r6 = r6.equals(r7);
        if (r6 == 0) goto L_0x007f;
    L_0x0054:
        r2 = r4;
    L_0x0055:
        r8 = r10.encoder;
        if (r2 == 0) goto L_0x0081;
    L_0x0059:
        r6 = r4;
    L_0x005a:
        org.jitsi.impl.neomedia.codec.audio.opus.Opus.encoder_set_dtx(r8, r6);
        r6 = r10.useFec;
        if (r6 == 0) goto L_0x0083;
    L_0x0061:
        r6 = "useinbandfec";
        r1 = r11.get(r6);
        r1 = (java.lang.String) r1;
        if (r1 == 0) goto L_0x0073;
    L_0x006b:
        r6 = "1";
        r6 = r1.equals(r6);
        if (r6 == 0) goto L_0x0083;
    L_0x0073:
        r3 = r4;
    L_0x0074:
        r6 = r10.encoder;
        if (r3 == 0) goto L_0x0085;
    L_0x0078:
        org.jitsi.impl.neomedia.codec.audio.opus.Opus.encoder_set_inband_fec(r6, r4);
        return;
    L_0x007c:
        r0 = r10.bitrate;
        goto L_0x003f;
    L_0x007f:
        r2 = r5;
        goto L_0x0055;
    L_0x0081:
        r6 = r5;
        goto L_0x005a;
    L_0x0083:
        r3 = r5;
        goto L_0x0074;
    L_0x0085:
        r4 = r5;
        goto L_0x0078;
    L_0x0087:
        r6 = move-exception;
        goto L_0x0039;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.codec.audio.opus.JNIEncoder.setFormatParameters(java.util.Map):void");
    }

    public Format setInputFormat(Format format) {
        Format oldValue = getInputFormat();
        Format setInputFormat = super.setInputFormat(format);
        Format newValue = getInputFormat();
        if (oldValue != newValue) {
            this.frameSizeInSamplesPerChannel = (((int) ((AudioFormat) newValue).getSampleRate()) * 20) / 1000;
            this.frameSizeInBytes = (this.channels * 2) * this.frameSizeInSamplesPerChannel;
        }
        return setInputFormat;
    }
}
