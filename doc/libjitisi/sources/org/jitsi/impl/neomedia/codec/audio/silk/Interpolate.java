package org.jitsi.impl.neomedia.codec.audio.silk;

public class Interpolate {
    static final /* synthetic */ boolean $assertionsDisabled = (!Interpolate.class.desiredAssertionStatus());

    static void SKP_Silk_interpolate(int[] xi, int[] x0, int[] x1, int ifact_Q2, int d) {
        if (!$assertionsDisabled && ifact_Q2 < 0) {
            throw new AssertionError();
        } else if ($assertionsDisabled || ifact_Q2 <= 4) {
            for (int i = 0; i < d; i++) {
                xi[i] = x0[i] + (((x1[i] - x0[i]) * ifact_Q2) >> 2);
            }
        } else {
            throw new AssertionError();
        }
    }
}
