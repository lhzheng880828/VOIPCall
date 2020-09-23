package org.jitsi.impl.neomedia.codec.audio.silk;

public class ScaleCopyVectorFLP {
    static void SKP_Silk_scale_copy_vector_FLP(float[] data_out, int data_out_offset, float[] data_in, int data_in_offset, float gain, int dataSize) {
        int dataSize4 = dataSize & 65532;
        int i = 0;
        while (i < dataSize4) {
            data_out[(data_out_offset + i) + 0] = data_in[(data_in_offset + i) + 0] * gain;
            data_out[(data_out_offset + i) + 1] = data_in[(data_in_offset + i) + 1] * gain;
            data_out[(data_out_offset + i) + 2] = data_in[(data_in_offset + i) + 2] * gain;
            data_out[(data_out_offset + i) + 3] = data_in[(data_in_offset + i) + 3] * gain;
            i += 4;
        }
        while (i < dataSize) {
            data_out[data_out_offset + i] = data_in[data_in_offset + i] * gain;
            i++;
        }
    }
}
