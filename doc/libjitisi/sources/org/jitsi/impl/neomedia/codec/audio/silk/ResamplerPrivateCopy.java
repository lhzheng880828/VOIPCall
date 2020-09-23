package org.jitsi.impl.neomedia.codec.audio.silk;

public class ResamplerPrivateCopy {
    static void SKP_Silk_resampler_private_copy(Object SS, short[] out, int out_offset, short[] in, int in_offset, int inLen) {
        for (int k = 0; k < inLen; k++) {
            out[out_offset + k] = in[in_offset + k];
        }
    }
}
