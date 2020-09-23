package org.jitsi.impl.neomedia.codec.audio.silk;

/* compiled from: Resampler */
class UpPostImplUp4 implements UpPostFP {
    UpPostImplUp4() {
    }

    public void up_post_function(int[] state, short[] out, int outOffset, short[] in, int inOffset, int len) {
        ResamplerPrivateUp4.SKP_Silk_resampler_private_up4(state, 0, out, outOffset, in, inOffset, len);
    }
}
