package org.jitsi.impl.neomedia.codec.audio.mp3;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;
import org.jitsi.impl.neomedia.codec.AbstractCodec2;
import org.jitsi.impl.neomedia.codec.FFmpeg;
import org.jitsi.util.Logger;

public class JNIEncoder extends AbstractCodec2 {
    private static final Format[] SUPPORTED_INPUT_FORMATS;
    private static final Format[] SUPPORTED_OUTPUT_FORMATS = new Format[]{new AudioFormat(AudioFormat.MPEGLAYER3)};
    private static final Logger logger = Logger.getLogger(JNIEncoder.class);
    private long avctx;
    private int frameSizeInBytes;
    private byte[] prevInput;
    private int prevInputLength;

    static {
        Format[] formatArr = new Format[1];
        formatArr[0] = new AudioFormat(AudioFormat.LINEAR, -1.0d, 16, -1, 0, 1, -1, -1.0d, Format.byteArray);
        SUPPORTED_INPUT_FORMATS = formatArr;
        if (FFmpeg.avcodec_find_encoder(FFmpeg.CODEC_ID_MP3) == 0) {
            throw new RuntimeException("Could not find FFmpeg encoder CODEC_ID_MP3");
        }
    }

    public JNIEncoder() {
        super("MP3 JNI Encoder", AudioFormat.class, SUPPORTED_OUTPUT_FORMATS);
        this.inputFormats = SUPPORTED_INPUT_FORMATS;
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void doClose() {
        if (this.avctx != 0) {
            FFmpeg.avcodec_close(this.avctx);
            FFmpeg.av_free(this.avctx);
            this.avctx = 0;
        }
        this.prevInput = null;
        this.prevInputLength = 0;
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void doOpen() throws ResourceUnavailableException {
        long encoder = FFmpeg.avcodec_find_encoder(FFmpeg.CODEC_ID_MP3);
        if (encoder == 0) {
            throw new ResourceUnavailableException("Could not find FFmpeg encoder CODEC_ID_MP3");
        }
        this.avctx = FFmpeg.avcodec_alloc_context3(encoder);
        if (this.avctx == 0) {
            throw new ResourceUnavailableException("Could not allocate AVCodecContext for FFmpeg encoder CODEC_ID_MP3");
        }
        AudioFormat inputFormat;
        int channels;
        int sampleRate;
        try {
            inputFormat = (AudioFormat) getInputFormat();
            channels = inputFormat.getChannels();
            sampleRate = (int) inputFormat.getSampleRate();
            if (channels == -1) {
                channels = 1;
            }
            FFmpeg.avcodeccontext_set_bit_rate(this.avctx, 128000);
            FFmpeg.avcodeccontext_set_channels(this.avctx, channels);
            FFmpeg.avcodeccontext_set_sample_fmt(this.avctx, 1);
        } catch (UnsatisfiedLinkError e) {
            logger.warn("The FFmpeg JNI library is out-of-date.");
        } catch (Throwable th) {
            if (-1 < 0) {
                FFmpeg.av_free(this.avctx);
                this.avctx = 0;
            }
        }
        if (sampleRate != -1) {
            FFmpeg.avcodeccontext_set_sample_rate(this.avctx, sampleRate);
        }
        int avcodec_open = FFmpeg.avcodec_open2(this.avctx, encoder, new String[0]);
        this.frameSizeInBytes = (FFmpeg.avcodeccontext_get_frame_size(this.avctx) * (inputFormat.getSampleSizeInBits() / 8)) * channels;
        if (avcodec_open < 0) {
            FFmpeg.av_free(this.avctx);
            this.avctx = 0;
        }
        if (this.avctx == 0) {
            throw new ResourceUnavailableException("Could not open FFmpeg encoder CODEC_ID_MP3");
        }
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized int doProcess(Buffer inputBuffer, Buffer outputBuffer) {
        int i;
        byte[] input = (byte[]) inputBuffer.getData();
        int inputLength = inputBuffer.getLength();
        int inputOffset = inputBuffer.getOffset();
        if (this.prevInputLength > 0 || inputLength < this.frameSizeInBytes) {
            int newPrevInputLength = Math.min(this.frameSizeInBytes - this.prevInputLength, inputLength);
            if (newPrevInputLength > 0) {
                if (this.prevInput == null) {
                    this.prevInput = new byte[this.frameSizeInBytes];
                    this.prevInputLength = 0;
                }
                System.arraycopy(input, inputOffset, this.prevInput, this.prevInputLength, newPrevInputLength);
                inputBuffer.setLength(inputLength - newPrevInputLength);
                inputBuffer.setOffset(inputOffset + newPrevInputLength);
                this.prevInputLength += newPrevInputLength;
                if (this.prevInputLength == this.frameSizeInBytes) {
                    input = this.prevInput;
                    inputLength = this.prevInputLength;
                    inputOffset = 0;
                    this.prevInputLength = 0;
                } else {
                    i = 4;
                }
            }
        } else {
            inputBuffer.setLength(inputLength - this.frameSizeInBytes);
            inputBuffer.setOffset(this.frameSizeInBytes + inputOffset);
        }
        Object outputData = outputBuffer.getData();
        byte[] output = outputData instanceof byte[] ? (byte[]) outputData : null;
        int outputOffset = outputBuffer.getOffset();
        int minOutputLength = Math.max(16384, inputLength);
        if (output == null || output.length - outputOffset < minOutputLength) {
            output = new byte[minOutputLength];
            outputBuffer.setData(output);
            outputOffset = 0;
            outputBuffer.setOffset(0);
        }
        int outputLength = FFmpeg.avcodec_encode_audio(this.avctx, output, outputOffset, output.length - outputOffset, input, inputOffset);
        if (outputLength < 0) {
            i = 1;
        } else {
            outputBuffer.setLength(outputLength);
            i = inputBuffer.getLength() > 0 ? 2 : 0;
        }
        return i;
    }
}
