package org.jitsi.impl.neomedia.codec.audio.silk;

public class LTPScaleCtrlFLP {
    static final float[] LTPScaleThresholds = new float[]{0.95f, 0.8f, 0.5f, 0.4f, 0.3f, 0.2f, 0.15f, 0.1f, 0.08f, 0.075f, 0.0f};
    static final int NB_THRESHOLDS = 11;

    static void SKP_Silk_LTP_scale_ctrl_FLP(SKP_Silk_encoder_state_FLP psEnc, SKP_Silk_encoder_control_FLP psEncCtrl) {
        int i = 10;
        psEnc.HPLTPredCodGain = Math.max(psEncCtrl.LTPredCodGain - psEnc.prevLTPredCodGain, 0.0f) + (psEnc.HPLTPredCodGain * 0.5f);
        psEnc.prevLTPredCodGain = psEncCtrl.LTPredCodGain;
        float g_limit = SigProcFLP.SKP_sigmoid((((psEncCtrl.LTPredCodGain * 0.5f) + (psEnc.HPLTPredCodGain * 0.5f)) - 6.0f) * 0.5f);
        psEncCtrl.sCmn.LTP_scaleIndex = 0;
        int round_loss = psEnc.sCmn.PacketLoss_perc;
        if (round_loss < 0) {
            round_loss = 0;
        }
        if (psEnc.sCmn.nFramesInPayloadBuf == 0) {
            int i2;
            round_loss += (psEnc.sCmn.PacketSize_ms / 20) - 1;
            float[] fArr = LTPScaleThresholds;
            if (round_loss < 10) {
                i2 = round_loss;
            } else {
                i2 = 10;
            }
            float thrld1 = fArr[i2];
            float[] fArr2 = LTPScaleThresholds;
            if (round_loss + 1 < 10) {
                i = round_loss + 1;
            }
            float thrld2 = fArr2[i];
            if (g_limit > thrld1) {
                psEncCtrl.sCmn.LTP_scaleIndex = 2;
            } else if (g_limit > thrld2) {
                psEncCtrl.sCmn.LTP_scaleIndex = 1;
            }
        }
        psEncCtrl.LTP_scale = ((float) TablesOther.SKP_Silk_LTPScales_table_Q14[psEncCtrl.sCmn.LTP_scaleIndex]) / 16384.0f;
    }
}
