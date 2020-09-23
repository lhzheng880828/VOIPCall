package org.jitsi.impl.neomedia.codec.audio.silk;

/* compiled from: Resampler */
class ResamplerImplIIRFIR implements ResamplerFP {
    ResamplerImplIIRFIR() {
    }

    public void resampler_function(Object state, short[] out, int outOffset, short[] in, int inOffset, int len) {
        ResamplerPrivateIIRFIR.SKP_Silk_resampler_private_IIR_FIR(state, out, outOffset, in, inOffset, len);
    }
}
