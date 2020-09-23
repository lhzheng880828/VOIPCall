package org.jitsi.impl.neomedia.codec.audio.silk;

public class ProcessGainsFLP {
    static final /* synthetic */ boolean $assertionsDisabled = (!ProcessGainsFLP.class.desiredAssertionStatus());

    static void SKP_Silk_process_gains_FLP(SKP_Silk_encoder_state_FLP psEnc, SKP_Silk_encoder_control_FLP psEncCtrl) {
        int k;
        float[] fArr;
        SKP_Silk_shape_state_FLP psShapeSt = psEnc.sShape;
        int[] pGains_Q16 = new int[4];
        if (psEncCtrl.sCmn.sigtype == 0) {
            float s = 1.0f - (0.5f * SigProcFLP.SKP_sigmoid(0.25f * (psEncCtrl.LTPredCodGain - 12.0f)));
            for (k = 0; k < 4; k++) {
                fArr = psEncCtrl.Gains;
                fArr[k] = fArr[k] * s;
            }
        }
        float InvMaxSqrVal = (float) (Math.pow(2.0d, (double) (0.33f * (21.0f - psEncCtrl.current_SNR_dB))) / ((double) psEnc.sCmn.subfr_length));
        for (k = 0; k < 4; k++) {
            float gain = psEncCtrl.Gains[k];
            gain = (float) Math.sqrt((double) ((gain * gain) + (psEncCtrl.ResNrg[k] * InvMaxSqrVal)));
            fArr = psEncCtrl.Gains;
            if (gain >= 32767.0f) {
                gain = 32767.0f;
            }
            fArr[k] = gain;
        }
        for (k = 0; k < 4; k++) {
            pGains_Q16[k] = (int) (psEncCtrl.Gains[k] * 65536.0f);
        }
        int[] LastGainIndex_ptr = new int[]{psShapeSt.LastGainIndex};
        GainQuant.SKP_Silk_gains_quant(psEncCtrl.sCmn.GainsIndices, pGains_Q16, LastGainIndex_ptr, psEnc.sCmn.nFramesInPayloadBuf);
        psShapeSt.LastGainIndex = LastGainIndex_ptr[0];
        for (k = 0; k < 4; k++) {
            psEncCtrl.Gains[k] = ((float) pGains_Q16[k]) / 65536.0f;
        }
        if (psEncCtrl.sCmn.sigtype == 0) {
            if (psEncCtrl.LTPredCodGain + psEncCtrl.input_tilt > 1.0f) {
                psEncCtrl.sCmn.QuantOffsetType = 0;
            } else {
                psEncCtrl.sCmn.QuantOffsetType = 1;
            }
        }
        if (psEncCtrl.sCmn.sigtype == 0) {
            psEncCtrl.Lambda = (((1.2f - (0.4f * psEnc.speech_activity)) - (0.3f * psEncCtrl.input_quality)) + (0.2f * ((float) psEncCtrl.sCmn.QuantOffsetType))) - (0.1f * psEncCtrl.coding_quality);
        } else {
            psEncCtrl.Lambda = (((1.2f - (0.4f * psEnc.speech_activity)) - (0.4f * psEncCtrl.input_quality)) + (0.4f * ((float) psEncCtrl.sCmn.QuantOffsetType))) - (0.1f * psEncCtrl.coding_quality);
        }
        if (!$assertionsDisabled && psEncCtrl.Lambda < 0.0f) {
            throw new AssertionError();
        } else if (!$assertionsDisabled && psEncCtrl.Lambda >= 2.0f) {
            throw new AssertionError();
        }
    }
}
