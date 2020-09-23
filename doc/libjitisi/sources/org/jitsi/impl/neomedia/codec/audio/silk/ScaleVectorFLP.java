package org.jitsi.impl.neomedia.codec.audio.silk;

public class ScaleVectorFLP {
    static void SKP_Silk_scale_vector_FLP(float[] data1, int data1_offset, float gain, int dataSize) {
        int i;
        int dataSize4 = dataSize & 65532;
        int i2 = 0;
        while (i2 < dataSize4) {
            i = (data1_offset + i2) + 0;
            data1[i] = data1[i] * gain;
            i = (data1_offset + i2) + 1;
            data1[i] = data1[i] * gain;
            i = (data1_offset + i2) + 2;
            data1[i] = data1[i] * gain;
            i = (data1_offset + i2) + 3;
            data1[i] = data1[i] * gain;
            i2 += 4;
        }
        while (i2 < dataSize) {
            i = data1_offset + i2;
            data1[i] = data1[i] * gain;
            i2++;
        }
    }
}
