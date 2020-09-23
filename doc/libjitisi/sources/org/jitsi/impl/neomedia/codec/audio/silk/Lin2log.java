package org.jitsi.impl.neomedia.codec.audio.silk;

public class Lin2log {
    static int SKP_Silk_lin2log(int inLin) {
        int[] lz_ptr = new int[1];
        int[] frac_Q7_ptr = new int[1];
        Inlines.SKP_Silk_CLZ_FRAC(inLin, lz_ptr, frac_Q7_ptr);
        int lz = lz_ptr[0];
        int frac_Q7 = frac_Q7_ptr[0];
        return SigProcFIX.SKP_LSHIFT(31 - lz, 7) + Macros.SKP_SMLAWB(frac_Q7, SigProcFIX.SKP_MUL(frac_Q7, 128 - frac_Q7), 179);
    }
}
