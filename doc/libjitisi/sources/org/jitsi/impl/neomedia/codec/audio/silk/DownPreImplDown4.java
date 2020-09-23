package org.jitsi.impl.neomedia.codec.audio.silk;

/* compiled from: Resampler */
class DownPreImplDown4 implements DownPreFP {
    DownPreImplDown4() {
    }

    public void down_pre_function(int[] state, short[] out, int outOffset, short[] in, int inOffset, int len) {
        ResamplerPrivateDown4.SKP_Silk_resampler_private_down4(state, 0, out, outOffset, in, inOffset, len);
    }
}
