package org.jitsi.impl.neomedia.codec.audio.gsm;

import com.lti.utils.UnsignedUtils;
import org.rubycoder.gsm.GSMDecoder;
import org.rubycoder.gsm.InvalidGSMFrameException;

public class GSMDecoderUtil {
    private static final int GSM_BYTES = 33;
    private static final int PCM_BYTES = 320;
    private static final int PCM_INTS = 160;
    private static GSMDecoder decoder = new GSMDecoder();

    public static void gsmDecode(boolean bigEndian, byte[] data, int offset, int length, byte[] decoded) {
        for (int i = 0; i < length / GSM_BYTES; i++) {
            int[] output = new int[PCM_INTS];
            byte[] input = new byte[GSM_BYTES];
            System.arraycopy(data, i * GSM_BYTES, input, 0, GSM_BYTES);
            try {
                decoder.decode(input, output);
            } catch (InvalidGSMFrameException e) {
                e.printStackTrace();
            }
            for (int j = 0; j < PCM_INTS; j++) {
                int index = j << 1;
                if (bigEndian) {
                    decoded[(i * PCM_BYTES) + index] = (byte) ((output[j] & 65280) >> 8);
                    decoded[(i * PCM_BYTES) + (index + 1)] = (byte) (output[j] & UnsignedUtils.MAX_UBYTE);
                } else {
                    decoded[(i * PCM_BYTES) + index] = (byte) (output[j] & UnsignedUtils.MAX_UBYTE);
                    decoded[(i * PCM_BYTES) + (index + 1)] = (byte) ((output[j] & 65280) >> 8);
                }
            }
        }
    }
}
