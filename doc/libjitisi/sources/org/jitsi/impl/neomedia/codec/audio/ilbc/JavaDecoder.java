package org.jitsi.impl.neomedia.codec.audio.ilbc;

import com.sun.media.controls.SilenceSuppressionAdapter;
import java.util.ArrayList;
import java.util.List;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.AudioFormat;
import org.jitsi.impl.neomedia.codec.AbstractCodec2;

public class JavaDecoder extends AbstractCodec2 {
    private ilbc_decoder dec;
    private int inputLength;
    private List<Integer> offsets;

    public JavaDecoder() {
        super("iLBC Decoder", AudioFormat.class, new Format[]{new AudioFormat(AudioFormat.LINEAR)});
        this.offsets = new ArrayList();
        this.inputFormats = new Format[]{new AudioFormat("ilbc/rtp", 8000.0d, 16, 1, -1, -1)};
        addControl(new SilenceSuppressionAdapter(this, false, false));
    }

    /* access modifiers changed from: protected */
    public void doClose() {
        this.dec = null;
        this.inputLength = 0;
    }

    /* access modifiers changed from: protected */
    public void doOpen() {
    }

    /* access modifiers changed from: protected */
    public int doProcess(Buffer inputBuffer, Buffer outputBuffer) {
        byte[] input = (byte[]) inputBuffer.getData();
        int inputLength = inputBuffer.getLength();
        if (this.offsets.size() == 0 && ((inputLength > 38 && inputLength != 50) || inputLength > 50)) {
            int nb = 0;
            int len = 0;
            if (inputLength % 38 == 0) {
                nb = inputLength % 38;
                len = 38;
            } else if (inputLength % 50 == 0) {
                nb = inputLength % 50;
                len = 50;
            }
            if (this.inputLength != len) {
                initDec(len);
            }
            for (int i = 0; i < nb; i++) {
                this.offsets.add(new Integer((i * len) + inputLength));
            }
        } else if (this.inputLength != inputLength) {
            initDec(inputLength);
        }
        int outputLength = this.dec.ULP_inst.blockl * 2;
        byte[] output = AbstractCodec2.validateByteArraySize(outputBuffer, outputLength, false);
        int offsetToAdd = 0;
        if (this.offsets.size() > 0) {
            offsetToAdd = ((Integer) this.offsets.remove(0)).intValue();
        }
        this.dec.decode(output, 0, input, inputBuffer.getOffset() + offsetToAdd, (short) 1);
        updateOutput(outputBuffer, getOutputFormat(), outputLength, 0);
        if (this.offsets.size() > 0) {
            return 0 | 2;
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public Format[] getMatchingOutputFormats(Format inputFormat) {
        AudioFormat inputAudioFormat = (AudioFormat) inputFormat;
        return new AudioFormat[]{new AudioFormat(AudioFormat.LINEAR, inputAudioFormat.getSampleRate(), 16, 1, 0, 1)};
    }

    private void initDec(int inputLength) {
        int mode;
        switch (inputLength) {
            case 38:
                mode = 20;
                break;
            case 50:
                mode = 30;
                break;
            default:
                throw new IllegalArgumentException("inputLength");
        }
        this.dec = new ilbc_decoder(mode, 1);
        this.inputLength = inputLength;
    }
}
