package org.jitsi.impl.neomedia.codec.audio.silk;

import org.jitsi.impl.neomedia.portaudio.Pa;

public class HPVariableCutoffFLP {
    static int frame_cnt = 0;

    static void SKP_Silk_HP_variable_cutoff_FLP(SKP_Silk_encoder_state_FLP psEnc, SKP_Silk_encoder_control_FLP psEncCtrl, short[] out, int out_offset, short[] in, int in_offset) {
        int[] B_Q28 = new int[3];
        int[] A_Q28 = new int[2];
        if (psEnc.sCmn.prev_sigtype == 0) {
            float pitch_freq_log = MainFLP.SKP_Silk_log2((double) ((1000.0f * ((float) psEnc.sCmn.fs_kHz)) / ((float) psEnc.sCmn.prevLag)));
            float quality = psEncCtrl.input_quality_bands[0];
            float delta_freq = ((pitch_freq_log - ((quality * quality) * (pitch_freq_log - MainFLP.SKP_Silk_log2(80.0d)))) + (0.5f * (0.6f - quality))) - psEnc.variable_HP_smth1;
            if (((double) delta_freq) < Pa.LATENCY_UNSPECIFIED) {
                delta_freq *= 3.0f;
            }
            float smth_coef = 0.1f * psEnc.speech_activity;
            psEnc.variable_HP_smth1 += smth_coef * SigProcFLP.SKP_LIMIT_float(delta_freq, -0.4f, 0.4f);
        }
        psEnc.variable_HP_smth2 += 0.015f * (psEnc.variable_HP_smth1 - psEnc.variable_HP_smth2);
        psEncCtrl.pitch_freq_low_Hz = (float) Math.pow(2.0d, (double) psEnc.variable_HP_smth2);
        psEncCtrl.pitch_freq_low_Hz = SigProcFLP.SKP_LIMIT_float(psEncCtrl.pitch_freq_low_Hz, 80.0f, 150.0f);
        float Fc = (float) ((2.8274333133295944d * ((double) psEncCtrl.pitch_freq_low_Hz)) / ((double) (1000.0f * ((float) psEnc.sCmn.fs_kHz))));
        float r = 1.0f - (0.92f * Fc);
        B_Q28[0] = SigProcFLP.SKP_float2int((double) (2.68435456E8f * r));
        B_Q28[1] = SigProcFLP.SKP_float2int((double) (-5.3687091E8f * r));
        B_Q28[2] = B_Q28[0];
        A_Q28[0] = SigProcFLP.SKP_float2int((double) ((-5.3687091E8f * r) * (1.0f - ((0.5f * Fc) * Fc))));
        A_Q28[1] = SigProcFLP.SKP_float2int((double) ((2.68435456E8f * r) * r));
        BiquadAlt.SKP_Silk_biquad_alt(in, in_offset, B_Q28, A_Q28, psEnc.sCmn.In_HP_State, out, out_offset, psEnc.sCmn.frame_length);
    }
}
