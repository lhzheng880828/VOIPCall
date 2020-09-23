package net.java.sip.communicator.impl.neomedia.codec.audio.g722;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;
import net.sf.fmj.ejmf.toolkit.util.TimeSource;
import org.jitsi.impl.neomedia.codec.AbstractCodec2;
import org.jitsi.service.neomedia.codec.Constants;

public class JNIDecoder extends AbstractCodec2 {
    static final Format[] SUPPORTED_INPUT_FORMATS = new Format[]{new AudioFormat(Constants.G722_RTP, 8000.0d, -1, 1)};
    static final Format[] SUPPORTED_OUTPUT_FORMATS = new Format[]{new AudioFormat(AudioFormat.LINEAR, 16000.0d, 16, 1, 0, 1, -1, -1.0d, Format.byteArray)};
    private long decoder;

    private static native void g722_decoder_close(long j);

    private static native long g722_decoder_open();

    private static native void g722_decoder_process(long j, byte[] bArr, int i, byte[] bArr2, int i2, int i3);

    static {
        System.loadLibrary("jng722");
    }

    public JNIDecoder() {
        super("G.722 JNI Decoder", AudioFormat.class, SUPPORTED_OUTPUT_FORMATS);
        this.inputFormats = SUPPORTED_INPUT_FORMATS;
    }

    /* access modifiers changed from: protected */
    public void doClose() {
        g722_decoder_close(this.decoder);
    }

    /* access modifiers changed from: protected */
    public void doOpen() throws ResourceUnavailableException {
        this.decoder = g722_decoder_open();
        if (this.decoder == 0) {
            throw new ResourceUnavailableException("g722_decoder_open");
        }
    }

    /* access modifiers changed from: protected */
    public int doProcess(Buffer inputBuffer, Buffer outputBuffer) {
        byte[] input = (byte[]) inputBuffer.getData();
        int outputOffset = outputBuffer.getOffset();
        int outputLength = inputBuffer.getLength() * 4;
        g722_decoder_process(this.decoder, input, inputBuffer.getOffset(), AbstractCodec2.validateByteArraySize(outputBuffer, outputOffset + outputLength, true), outputOffset, outputLength);
        outputBuffer.setDuration((((long) outputLength) * TimeSource.MICROS_PER_SEC) / 32);
        outputBuffer.setFormat(getOutputFormat());
        outputBuffer.setLength(outputLength);
        return 0;
    }
}
