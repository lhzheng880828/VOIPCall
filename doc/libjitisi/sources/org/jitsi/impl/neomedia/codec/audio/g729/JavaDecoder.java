package org.jitsi.impl.neomedia.codec.audio.g729;

import javax.media.Buffer;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;
import org.jitsi.impl.neomedia.ArrayIOUtils;
import org.jitsi.impl.neomedia.codec.AbstractCodec2;

public class JavaDecoder extends AbstractCodec2 {
    private static final short BIT_0 = (short) 127;
    private static final short BIT_1 = (short) 129;
    private static final int INPUT_FRAME_SIZE_IN_BYTES = 10;
    private static final int L_FRAME = 80;
    private static final int OUTPUT_FRAME_SIZE_IN_BYTES = 160;
    private static final int SERIAL_SIZE = 82;
    private static final short SIZE_WORD = (short) 80;
    private static final short SYNC_WORD = (short) 27425;
    private Decoder decoder;
    private short[] serial;
    private short[] sp16;

    public JavaDecoder() {
        super("G.729 Decoder", AudioFormat.class, new AudioFormat[]{new AudioFormat(AudioFormat.LINEAR, 8000.0d, 16, 1, 0, 1)});
        this.inputFormats = new AudioFormat[]{new AudioFormat(AudioFormat.G729_RTP, 8000.0d, -1, 1)};
    }

    private void depacketize(byte[] inputFrame, int inputFrameOffset, short[] serial) {
        serial[0] = SYNC_WORD;
        serial[1] = SIZE_WORD;
        for (int s = 0; s < L_FRAME; s++) {
            short s2;
            int i = s + 2;
            if ((inputFrame[(s / 8) + inputFrameOffset] & (1 << (7 - (s % 8)))) != 0) {
                s2 = BIT_1;
            } else {
                s2 = BIT_0;
            }
            serial[i] = s2;
        }
    }

    /* access modifiers changed from: protected */
    public void doClose() {
        this.serial = null;
        this.sp16 = null;
        this.decoder = null;
    }

    /* access modifiers changed from: protected */
    public void doOpen() throws ResourceUnavailableException {
        this.serial = new short[SERIAL_SIZE];
        this.sp16 = new short[L_FRAME];
        this.decoder = new Decoder();
    }

    /* access modifiers changed from: protected */
    public int doProcess(Buffer inBuffer, Buffer outBuffer) {
        int inLength = inBuffer.getLength();
        if (inLength < 10) {
            discardOutputBuffer(outBuffer);
            return 4;
        }
        byte[] in = (byte[]) inBuffer.getData();
        int inOffset = inBuffer.getOffset();
        depacketize(in, inOffset, this.serial);
        inLength -= 10;
        inBuffer.setLength(inLength);
        inBuffer.setOffset(inOffset + 10);
        this.decoder.process(this.serial, this.sp16);
        writeShorts(this.sp16, AbstractCodec2.validateByteArraySize(outBuffer, outBuffer.getOffset() + OUTPUT_FRAME_SIZE_IN_BYTES, true), outBuffer.getOffset());
        outBuffer.setLength(OUTPUT_FRAME_SIZE_IN_BYTES);
        if (inLength > 0) {
            return 0 | 2;
        }
        return 0;
    }

    private static void writeShorts(short[] in, byte[] out, int outOffset) {
        int i = 0;
        int o = outOffset;
        while (i < in.length) {
            ArrayIOUtils.writeShort(in[i], out, o);
            i++;
            o += 2;
        }
    }
}
