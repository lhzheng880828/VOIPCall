package org.jitsi.impl.neomedia.codec.audio.silk;

/* compiled from: Resampler */
class ResamplerImplDownFIR implements ResamplerFP {
    ResamplerImplDownFIR() {
    }

    public void resampler_function(Object state, short[] out, int outOffset, short[] in, int inOffset, int len) {
        ResamplerPrivateDownFIR.SKP_Silk_resampler_private_down_FIR(state, out, outOffset, in, inOffset, len);
    }
}
