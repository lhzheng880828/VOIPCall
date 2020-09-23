package org.jitsi.impl.neomedia.codec.audio.silk;

/* compiled from: StructsFLP */
class SKP_Silk_encoder_state_FLP {
    float BufferedInChannel_ms;
    float HPLTPredCodGain;
    float LTPCorr;
    float SNR_dB;
    float avgGain;
    float inBandFEC_SNR_comp;
    float mu_LTP;
    NoiseShapingQuantizerFP noiseShapingQuantizerCB;
    float pitchEstimationThreshold;
    float prevLTPredCodGain;
    SKP_Silk_NLSF_CB_FLP[] psNLSF_CB_FLP = new SKP_Silk_NLSF_CB_FLP[2];
    SKP_Silk_encoder_state sCmn = new SKP_Silk_encoder_state();
    SKP_Silk_nsq_state sNSQ = new SKP_Silk_nsq_state();
    SKP_Silk_nsq_state sNSQ_LBRR = new SKP_Silk_nsq_state();
    SKP_Silk_predict_state_FLP sPred = new SKP_Silk_predict_state_FLP();
    SKP_Silk_prefilter_state_FLP sPrefilt = new SKP_Silk_prefilter_state_FLP();
    SKP_Silk_shape_state_FLP sShape = new SKP_Silk_shape_state_FLP();
    float speech_activity;
    float variable_HP_smth1;
    float variable_HP_smth2;
    float[] x_buf = new float[1080];
    int x_buf_offset;

    SKP_Silk_encoder_state_FLP() {
    }

    /* access modifiers changed from: 0000 */
    public void NoiseShapingQuantizer(SKP_Silk_encoder_state psEnc, SKP_Silk_encoder_control psEncCtrl, SKP_Silk_nsq_state NSQ, short[] x, byte[] q, int arg6, short[] arg7, short[] arg8, short[] arg9, int[] arg10, int[] arg11, int[] arg12, int[] arg13, int arg14, int arg15) {
        this.noiseShapingQuantizerCB.NoiseShapingQuantizer(psEnc, psEncCtrl, NSQ, x, q, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15);
    }
}
