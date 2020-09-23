package org.jitsi.impl.neomedia.codec.audio.silk;

public class AnaFiltBank1 {
    static short[] A_fb1_20 = new short[]{(short) 10788};
    static short[] A_fb1_21 = new short[]{(short) -24290};

    static void SKP_Silk_ana_filt_bank_1(short[] in, int in_offset, int[] S, int S_offset, short[] outL, int outL_offset, short[] outH, int outH_offset, int[] scratch, int N) {
        int N2 = N >> 1;
        for (int k = 0; k < N2; k++) {
            int in32 = in[(k * 2) + in_offset] << 10;
            int Y = in32 - S[S_offset + 0];
            int X = Macros.SKP_SMLAWB(Y, Y, A_fb1_21[0]);
            int out_1 = S[S_offset + 0] + X;
            S[S_offset + 0] = in32 + X;
            in32 = in[((k * 2) + in_offset) + 1] << 10;
            X = Macros.SKP_SMULWB(in32 - S[S_offset + 1], A_fb1_20[0]);
            int out_2 = S[S_offset + 1] + X;
            S[S_offset + 1] = in32 + X;
            outL[outL_offset + k] = (short) SigProcFIX.SKP_SAT16(SigProcFIX.SKP_RSHIFT_ROUND(out_2 + out_1, 11));
            outH[outH_offset + k] = (short) SigProcFIX.SKP_SAT16(SigProcFIX.SKP_RSHIFT_ROUND(out_2 - out_1, 11));
        }
    }
}
