package org.jitsi.impl.neomedia.codec.audio.silk;

/* compiled from: Resampler */
class Up2ImplUp2 implements Up2FP {
    Up2ImplUp2() {
    }

    public void up2_function(int[] state, short[] out, int outOffset, short[] in, int inOffset, int len) {
        ResamplerUp2.SKP_Silk_resampler_up2(state, 0, out, outOffset, in, inOffset, len);
    }
}
