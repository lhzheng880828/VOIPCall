package org.jitsi.impl.neomedia.codec.audio.silk;

import org.jitsi.impl.neomedia.portaudio.Pa;

public class InnerProductFLP {
    static double SKP_Silk_inner_product_FLP(float[] data1, int data1_offset, float[] data2, int data2_offset, int dataSize) {
        double result = Pa.LATENCY_UNSPECIFIED;
        int i = 0;
        while (i < (dataSize & 65532)) {
            result += (double) ((((data1[(data1_offset + i) + 0] * data2[(data2_offset + i) + 0]) + (data1[(data1_offset + i) + 1] * data2[(data2_offset + i) + 1])) + (data1[(data1_offset + i) + 2] * data2[(data2_offset + i) + 2])) + (data1[(data1_offset + i) + 3] * data2[(data2_offset + i) + 3]));
            i += 4;
        }
        while (i < dataSize) {
            result += (double) (data1[data1_offset + i] * data2[data2_offset + i]);
            i++;
        }
        return result;
    }
}
