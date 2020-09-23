package org.jitsi.impl.neomedia.codec.audio.silk;

public class Log2lin {
    static int SKP_Silk_log2lin(int inLog_Q7) {
        if (inLog_Q7 < 0) {
            return 0;
        }
        int out = 1 << (inLog_Q7 >> 7);
        int frac_Q7 = inLog_Q7 & 127;
        if (inLog_Q7 < 2048) {
            return SigProcFIX.SKP_ADD_RSHIFT(out, SigProcFIX.SKP_MUL(out, Macros.SKP_SMLAWB(frac_Q7, SigProcFIX.SKP_MUL(frac_Q7, 128 - frac_Q7), -174)), 7);
        }
        return SigProcFIX.SKP_MLA(out, SigProcFIX.SKP_RSHIFT(out, 7), Macros.SKP_SMLAWB(frac_Q7, SigProcFIX.SKP_MUL(frac_Q7, 128 - frac_Q7), -174));
    }
}
