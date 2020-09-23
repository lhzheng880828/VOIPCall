package org.jitsi.impl.neomedia.codec.audio.silk;

public class EncodeParameters {
    static final /* synthetic */ boolean $assertionsDisabled = (!EncodeParameters.class.desiredAssertionStatus());

    static void SKP_Silk_encode_parameters(SKP_Silk_encoder_state psEncC, SKP_Silk_encoder_control psEncCtrlC, SKP_Silk_range_coder_state psRC, byte[] q) {
        int i;
        if (psEncC.nFramesInPayloadBuf == 0) {
            i = 0;
            while (i < 3 && TablesOther.SKP_Silk_SamplingRates_table[i] != psEncC.fs_kHz) {
                i++;
            }
            RangeCoder.SKP_Silk_range_encoder(psRC, i, TablesOther.SKP_Silk_SamplingRates_CDF, 0);
        }
        int typeOffset = (psEncCtrlC.sigtype * 2) + psEncCtrlC.QuantOffsetType;
        if (psEncC.nFramesInPayloadBuf == 0) {
            RangeCoder.SKP_Silk_range_encoder(psRC, typeOffset, TablesTypeOffset.SKP_Silk_type_offset_CDF, 0);
        } else {
            RangeCoder.SKP_Silk_range_encoder(psRC, typeOffset, TablesTypeOffset.SKP_Silk_type_offset_joint_CDF[psEncC.typeOffsetPrev], 0);
        }
        psEncC.typeOffsetPrev = typeOffset;
        if (psEncC.nFramesInPayloadBuf == 0) {
            RangeCoder.SKP_Silk_range_encoder(psRC, psEncCtrlC.GainsIndices[0], TablesGain.SKP_Silk_gain_CDF[psEncCtrlC.sigtype], 0);
        } else {
            RangeCoder.SKP_Silk_range_encoder(psRC, psEncCtrlC.GainsIndices[0], TablesGain.SKP_Silk_delta_gain_CDF, 0);
        }
        for (i = 1; i < 4; i++) {
            RangeCoder.SKP_Silk_range_encoder(psRC, psEncCtrlC.GainsIndices[i], TablesGain.SKP_Silk_delta_gain_CDF, 0);
        }
        SKP_Silk_NLSF_CB_struct psNLSF_CB = psEncC.psNLSF_CB[psEncCtrlC.sigtype];
        RangeCoder.SKP_Silk_range_encoder_multi(psRC, psEncCtrlC.NLSFIndices, psNLSF_CB.StartPtr, psNLSF_CB.nStages);
        if ($assertionsDisabled || psEncC.useInterpolatedNLSFs == 1 || psEncCtrlC.NLSFInterpCoef_Q2 == 4) {
            RangeCoder.SKP_Silk_range_encoder(psRC, psEncCtrlC.NLSFInterpCoef_Q2, TablesOther.SKP_Silk_NLSF_interpolation_factor_CDF, 0);
            if (psEncCtrlC.sigtype == 0) {
                if (psEncC.fs_kHz == 8) {
                    RangeCoder.SKP_Silk_range_encoder(psRC, psEncCtrlC.lagIndex, TablesPitchLag.SKP_Silk_pitch_lag_NB_CDF, 0);
                } else if (psEncC.fs_kHz == 12) {
                    RangeCoder.SKP_Silk_range_encoder(psRC, psEncCtrlC.lagIndex, TablesPitchLag.SKP_Silk_pitch_lag_MB_CDF, 0);
                } else if (psEncC.fs_kHz == 16) {
                    RangeCoder.SKP_Silk_range_encoder(psRC, psEncCtrlC.lagIndex, TablesPitchLag.SKP_Silk_pitch_lag_WB_CDF, 0);
                } else {
                    RangeCoder.SKP_Silk_range_encoder(psRC, psEncCtrlC.lagIndex, TablesPitchLag.SKP_Silk_pitch_lag_SWB_CDF, 0);
                }
                if (psEncC.fs_kHz == 8) {
                    RangeCoder.SKP_Silk_range_encoder(psRC, psEncCtrlC.contourIndex, TablesPitchLag.SKP_Silk_pitch_contour_NB_CDF, 0);
                } else {
                    RangeCoder.SKP_Silk_range_encoder(psRC, psEncCtrlC.contourIndex, TablesPitchLag.SKP_Silk_pitch_contour_CDF, 0);
                }
                RangeCoder.SKP_Silk_range_encoder(psRC, psEncCtrlC.PERIndex, TablesLTP.SKP_Silk_LTP_per_index_CDF, 0);
                for (int k = 0; k < 4; k++) {
                    RangeCoder.SKP_Silk_range_encoder(psRC, psEncCtrlC.LTPIndex[k], TablesLTP.SKP_Silk_LTP_gain_CDF_ptrs[psEncCtrlC.PERIndex], 0);
                }
                RangeCoder.SKP_Silk_range_encoder(psRC, psEncCtrlC.LTP_scaleIndex, TablesOther.SKP_Silk_LTPscale_CDF, 0);
            }
            RangeCoder.SKP_Silk_range_encoder(psRC, psEncCtrlC.Seed, TablesOther.SKP_Silk_Seed_CDF, 0);
            EncodePulses.SKP_Silk_encode_pulses(psRC, psEncCtrlC.sigtype, psEncCtrlC.QuantOffsetType, q, psEncC.frame_length);
            RangeCoder.SKP_Silk_range_encoder(psRC, psEncC.vadFlag, TablesOther.SKP_Silk_vadflag_CDF, 0);
            return;
        }
        throw new AssertionError();
    }
}
