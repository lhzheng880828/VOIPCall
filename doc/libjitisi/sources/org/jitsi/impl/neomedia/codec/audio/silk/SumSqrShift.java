package org.jitsi.impl.neomedia.codec.audio.silk;

public class SumSqrShift {
    static void SKP_Silk_sum_sqr_shift(int[] energy, int[] shift, short[] x, int x_offset, int len) {
        int in32;
        int nrg = 0;
        int i = 0;
        int shft = 0;
        len--;
        while (i < len) {
            in32 = x[x_offset + i];
            nrg = SigProcFIX.SKP_SMLATT_ovflw(SigProcFIX.SKP_SMLABB_ovflw(nrg, in32, in32), in32, in32);
            i += 2;
            if (nrg < 0) {
                nrg = (int) ((((long) nrg) & 4294967295L) >>> 2);
                shft = 2;
                break;
            }
        }
        while (i < len) {
            in32 = x[x_offset + i];
            nrg += SigProcFIX.SKP_SMLATT_ovflw(Macros.SKP_SMULBB(in32, in32), in32, in32) >>> shft;
            if (nrg < 0) {
                nrg >>>= 2;
                shft += 2;
            }
            i += 2;
        }
        if (i == len) {
            nrg += Macros.SKP_SMULBB(x[x_offset + i], x[x_offset + i]) >>> shft;
        }
        if ((-1073741824 & nrg) != 0) {
            nrg >>>= 2;
            shft += 2;
        }
        shift[0] = shft;
        energy[0] = nrg;
    }
}
