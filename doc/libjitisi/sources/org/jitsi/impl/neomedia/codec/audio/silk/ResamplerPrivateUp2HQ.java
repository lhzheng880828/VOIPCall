package org.jitsi.impl.neomedia.codec.audio.silk;

public class ResamplerPrivateUp2HQ {
    static final /* synthetic */ boolean $assertionsDisabled = (!ResamplerPrivateUp2HQ.class.desiredAssertionStatus());

    static void SKP_Silk_resampler_private_up2_HQ(int[] S, int S_offset, short[] out, int out_offset, short[] in, int in_offset, int len) {
        if (!$assertionsDisabled && ResamplerRom.SKP_Silk_resampler_up2_hq_0[0] <= (short) 0) {
            throw new AssertionError();
        } else if (!$assertionsDisabled && ResamplerRom.SKP_Silk_resampler_up2_hq_0[1] >= (short) 0) {
            throw new AssertionError();
        } else if (!$assertionsDisabled && ResamplerRom.SKP_Silk_resampler_up2_hq_1[0] <= (short) 0) {
            throw new AssertionError();
        } else if ($assertionsDisabled || ResamplerRom.SKP_Silk_resampler_up2_hq_1[1] < (short) 0) {
            for (int k = 0; k < len; k++) {
                int in32 = in[in_offset + k] << 10;
                int X = Macros.SKP_SMULWB(in32 - S[S_offset], ResamplerRom.SKP_Silk_resampler_up2_hq_0[0]);
                int out32_1 = S[S_offset] + X;
                S[S_offset] = in32 + X;
                int Y = out32_1 - S[S_offset + 1];
                X = Macros.SKP_SMLAWB(Y, Y, ResamplerRom.SKP_Silk_resampler_up2_hq_0[1]);
                int out32_2 = S[S_offset + 1] + X;
                S[S_offset + 1] = out32_1 + X;
                out32_2 = Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(out32_2, S[S_offset + 5], ResamplerRom.SKP_Silk_resampler_up2_hq_notch[2]), S[S_offset + 4], ResamplerRom.SKP_Silk_resampler_up2_hq_notch[1]);
                out32_1 = Macros.SKP_SMLAWB(out32_2, S[S_offset + 4], ResamplerRom.SKP_Silk_resampler_up2_hq_notch[0]);
                S[S_offset + 5] = out32_2 - S[S_offset + 5];
                out[(k * 2) + out_offset] = (short) SigProcFIX.SKP_SAT16(Macros.SKP_SMLAWB(256, out32_1, ResamplerRom.SKP_Silk_resampler_up2_hq_notch[3]) >> 9);
                X = Macros.SKP_SMULWB(in32 - S[S_offset + 2], ResamplerRom.SKP_Silk_resampler_up2_hq_1[0]);
                out32_1 = S[S_offset + 2] + X;
                S[S_offset + 2] = in32 + X;
                Y = out32_1 - S[S_offset + 3];
                X = Macros.SKP_SMLAWB(Y, Y, ResamplerRom.SKP_Silk_resampler_up2_hq_1[1]);
                out32_2 = S[S_offset + 3] + X;
                S[S_offset + 3] = out32_1 + X;
                out32_2 = Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(out32_2, S[S_offset + 4], ResamplerRom.SKP_Silk_resampler_up2_hq_notch[2]), S[S_offset + 5], ResamplerRom.SKP_Silk_resampler_up2_hq_notch[1]);
                out32_1 = Macros.SKP_SMLAWB(out32_2, S[S_offset + 5], ResamplerRom.SKP_Silk_resampler_up2_hq_notch[0]);
                S[S_offset + 4] = out32_2 - S[S_offset + 4];
                out[((k * 2) + out_offset) + 1] = (short) SigProcFIX.SKP_SAT16(Macros.SKP_SMLAWB(256, out32_1, ResamplerRom.SKP_Silk_resampler_up2_hq_notch[3]) >> 9);
            }
        } else {
            throw new AssertionError();
        }
    }

    static void SKP_Silk_resampler_private_up2_HQ_wrapper(Object SS, short[] out, int out_offset, short[] in, int in_offset, int len) {
        SKP_Silk_resampler_private_up2_HQ(((SKP_Silk_resampler_state_struct) SS).sIIR, 0, out, out_offset, in, in_offset, len);
    }
}
