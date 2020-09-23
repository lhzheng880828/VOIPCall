package org.jitsi.impl.neomedia.codec.audio.silk;

/* compiled from: Resampler */
class ResamplerImplWrapper implements ResamplerFP {
    ResamplerImplWrapper() {
    }

    public void resampler_function(Object state, short[] out, int outOffset, short[] in, int inOffset, int len) {
        ResamplerPrivateUp2HQ.SKP_Silk_resampler_private_up2_HQ_wrapper(state, out, outOffset, in, inOffset, len);
    }
}
