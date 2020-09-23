package net.sf.fmj.media.codec.audio.gsm;

import com.lti.utils.UnsignedUtils;
import org.rubycoder.gsm.GSMEncoder;

public class GSMEncoderUtil {
    private static final int GSM_BYTES = 33;
    private static final int PCM_BYTES = 320;
    private static final int PCM_INTS = 160;
    private static GSMEncoder encoder = new GSMEncoder();

    public static void gsmEncode(boolean bigEndian, byte[] data, int offset, int length, byte[] decoded) {
        for (int i = offset; i < length / PCM_BYTES; i++) {
            int[] input = new int[PCM_INTS];
            byte[] output = new byte[GSM_BYTES];
            for (int j = 0; j < PCM_INTS; j++) {
                int index = j << 1;
                int index2 = index + 1;
                input[j] = data[(i * PCM_BYTES) + index];
                input[j] = input[j] << 8;
                index = index2 + 1;
                input[j] = input[j] | (data[(i * PCM_BYTES) + index2] & UnsignedUtils.MAX_UBYTE);
            }
            encoder.encode(output, input);
            System.arraycopy(output, 0, decoded, i * GSM_BYTES, GSM_BYTES);
        }
    }
}
