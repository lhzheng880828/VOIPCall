package org.jitsi.impl.neomedia.codec.audio.silk;

/* compiled from: Resampler */
class ResamplerImplCopy implements ResamplerFP {
    ResamplerImplCopy() {
    }

    public void resampler_function(Object state, short[] out, int outOffset, short[] in, int inOffset, int len) {
        ResamplerPrivateCopy.SKP_Silk_resampler_private_copy(state, out, outOffset, in, inOffset, len);
    }
}
