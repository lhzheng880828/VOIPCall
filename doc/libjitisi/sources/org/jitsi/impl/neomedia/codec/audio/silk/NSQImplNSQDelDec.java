package org.jitsi.impl.neomedia.codec.audio.silk;

/* compiled from: ControlCodecFLP */
class NSQImplNSQDelDec implements NoiseShapingQuantizerFP {
    NSQImplNSQDelDec() {
    }

    public void NoiseShapingQuantizer(SKP_Silk_encoder_state psEnc, SKP_Silk_encoder_control psEncCtrl, SKP_Silk_nsq_state NSQ, short[] x, byte[] q, int arg6, short[] arg7, short[] arg8, short[] arg9, int[] arg10, int[] arg11, int[] arg12, int[] arg13, int arg14, int arg15) {
        NSQDelDec.SKP_Silk_NSQ_del_dec(psEnc, psEncCtrl, NSQ, x, q, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15);
    }
}
