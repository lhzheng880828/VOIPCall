package org.jitsi.impl.neomedia.codec.audio.silk;

/* compiled from: Resampler */
class Up2ImplHQ implements Up2FP {
    Up2ImplHQ() {
    }

    public void up2_function(int[] state, short[] out, int outOffset, short[] in, int inOffset, int len) {
        ResamplerPrivateUp2HQ.SKP_Silk_resampler_private_up2_HQ(state, 0, out, outOffset, in, inOffset, len);
    }
}
