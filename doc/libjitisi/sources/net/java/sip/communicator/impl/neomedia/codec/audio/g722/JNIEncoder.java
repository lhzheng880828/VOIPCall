package net.java.sip.communicator.impl.neomedia.codec.audio.g722;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;
import net.sf.fmj.ejmf.toolkit.util.TimeSource;
import org.jitsi.impl.neomedia.codec.AbstractCodec2;

public class JNIEncoder extends AbstractCodec2 {
    private long encoder;

    private static native void g722_encoder_close(long j);

    private static native long g722_encoder_open();

    private static native void g722_encoder_process(long j, byte[] bArr, int i, byte[] bArr2, int i2, int i3);

    static {
        System.loadLibrary("jng722");
    }

    public JNIEncoder() {
        super("G.722 JNI Encoder", AudioFormat.class, JNIDecoder.SUPPORTED_INPUT_FORMATS);
        this.inputFormats = JNIDecoder.SUPPORTED_OUTPUT_FORMATS;
    }

    /* access modifiers changed from: private */
    public long computeDuration(long length) {
        return (TimeSource.MICROS_PER_SEC * length) / 8;
    }

    /* access modifiers changed from: protected */
    public void doClose() {
        g722_encoder_close(this.encoder);
    }

    /* access modifiers changed from: protected */
    public void doOpen() throws ResourceUnavailableException {
        this.encoder = g722_encoder_open();
        if (this.encoder == 0) {
            throw new ResourceUnavailableException("g722_encoder_open");
        }
    }

    /* access modifiers changed from: protected */
    public int doProcess(Buffer inputBuffer, Buffer outputBuffer) {
        int inputOffset = inputBuffer.getOffset();
        int inputLength = inputBuffer.getLength();
        byte[] input = (byte[]) inputBuffer.getData();
        int outputOffset = outputBuffer.getOffset();
        int outputLength = inputLength / 4;
        g722_encoder_process(this.encoder, input, inputOffset, AbstractCodec2.validateByteArraySize(outputBuffer, outputOffset + outputLength, true), outputOffset, outputLength);
        outputBuffer.setDuration(computeDuration((long) outputLength));
        outputBuffer.setFormat(getOutputFormat());
        outputBuffer.setLength(outputLength);
        return 0;
    }

    public Format getOutputFormat() {
        Format outputFormat = super.getOutputFormat();
        if (outputFormat != null && outputFormat.getClass() == AudioFormat.class) {
            AudioFormat outputAudioFormat = (AudioFormat) outputFormat;
            setOutputFormat(new AudioFormat(outputAudioFormat.getEncoding(), outputAudioFormat.getSampleRate(), outputAudioFormat.getSampleSizeInBits(), outputAudioFormat.getChannels(), outputAudioFormat.getEndian(), outputAudioFormat.getSigned(), outputAudioFormat.getFrameSizeInBits(), outputAudioFormat.getFrameRate(), outputAudioFormat.getDataType()) {
                private static final long serialVersionUID = 0;

                public long computeDuration(long length) {
                    return JNIEncoder.this.computeDuration(length);
                }
            });
        }
        return outputFormat;
    }
}
