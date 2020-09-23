package org.jitsi.impl.neomedia.codec.audio.silk;

/* compiled from: Resampler */
class DownPreImplDown2 implements DownPreFP {
    DownPreImplDown2() {
    }

    public void down_pre_function(int[] state, short[] out, int outOffset, short[] in, int inOffset, int len) {
        ResamplerDown2.SKP_Silk_resampler_down2(state, 0, out, outOffset, in, inOffset, len);
    }
}
