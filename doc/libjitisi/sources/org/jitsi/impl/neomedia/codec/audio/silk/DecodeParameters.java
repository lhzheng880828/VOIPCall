package org.jitsi.impl.neomedia.codec.audio.silk;

import java.util.Arrays;

public class DecodeParameters {
    static void SKP_Silk_decode_parameters(SKP_Silk_decoder_state psDec, SKP_Silk_decoder_control psDecCtrl, int[] q, int fullDecoding) {
        int Ix;
        int i;
        int[] Ix_ptr = new int[1];
        int[] Ixs = new int[4];
        int[] GainsIndices = new int[4];
        int[] NLSFIndices = new int[10];
        Object pNLSF_Q15 = new int[16];
        int[] pNLSF0_Q15 = new int[16];
        SKP_Silk_range_coder_state psRC = psDec.sRC;
        if (psDec.nFramesDecoded == 0) {
            RangeCoder.SKP_Silk_range_decoder(Ix_ptr, 0, psRC, TablesOther.SKP_Silk_SamplingRates_CDF, 0, 2);
            Ix = Ix_ptr[0];
            if (Ix < 0 || Ix > 3) {
                psRC.error = -7;
                return;
            }
            DecoderSetFs.SKP_Silk_decoder_set_fs(psDec, TablesOther.SKP_Silk_SamplingRates_table[Ix]);
        }
        if (psDec.nFramesDecoded == 0) {
            RangeCoder.SKP_Silk_range_decoder(Ix_ptr, 0, psRC, TablesTypeOffset.SKP_Silk_type_offset_CDF, 0, 2);
            Ix = Ix_ptr[0];
        } else {
            RangeCoder.SKP_Silk_range_decoder(Ix_ptr, 0, psRC, TablesTypeOffset.SKP_Silk_type_offset_joint_CDF[psDec.typeOffsetPrev], 0, 2);
            Ix = Ix_ptr[0];
        }
        psDecCtrl.sigtype = Ix >> 1;
        psDecCtrl.QuantOffsetType = Ix & 1;
        psDec.typeOffsetPrev = Ix;
        if (psDec.nFramesDecoded == 0) {
            RangeCoder.SKP_Silk_range_decoder(GainsIndices, 0, psRC, TablesGain.SKP_Silk_gain_CDF[psDecCtrl.sigtype], 0, 32);
        } else {
            RangeCoder.SKP_Silk_range_decoder(GainsIndices, 0, psRC, TablesGain.SKP_Silk_delta_gain_CDF, 0, 5);
        }
        for (i = 1; i < 4; i++) {
            RangeCoder.SKP_Silk_range_decoder(GainsIndices, i, psRC, TablesGain.SKP_Silk_delta_gain_CDF, 0, 5);
        }
        int[] LastGainIndex_ptr = new int[]{psDec.LastGainIndex};
        GainQuant.SKP_Silk_gains_dequant(psDecCtrl.Gains_Q16, GainsIndices, LastGainIndex_ptr, psDec.nFramesDecoded);
        psDec.LastGainIndex = LastGainIndex_ptr[0];
        SKP_Silk_NLSF_CB_struct psNLSF_CB = psDec.psNLSF_CB[psDecCtrl.sigtype];
        RangeCoder.SKP_Silk_range_decoder_multi(NLSFIndices, psRC, psNLSF_CB.StartPtr, psNLSF_CB.MiddleIx, psNLSF_CB.nStages);
        NLSFMSVQDecode.SKP_Silk_NLSF_MSVQ_decode(pNLSF_Q15, psNLSF_CB, NLSFIndices, psDec.LPC_order);
        int[] NLSFInterpCoef_Q2_ptr = new int[]{psDecCtrl.NLSFInterpCoef_Q2};
        RangeCoder.SKP_Silk_range_decoder(NLSFInterpCoef_Q2_ptr, 0, psRC, TablesOther.SKP_Silk_NLSF_interpolation_factor_CDF, 0, 4);
        psDecCtrl.NLSFInterpCoef_Q2 = NLSFInterpCoef_Q2_ptr[0];
        if (psDec.first_frame_after_reset == 1) {
            psDecCtrl.NLSFInterpCoef_Q2 = 4;
        }
        if (fullDecoding != 0) {
            NLSF2AStable.SKP_Silk_NLSF2A_stable(psDecCtrl.PredCoef_Q12[1], pNLSF_Q15, psDec.LPC_order);
            if (psDecCtrl.NLSFInterpCoef_Q2 < 4) {
                for (i = 0; i < psDec.LPC_order; i++) {
                    pNLSF0_Q15[i] = psDec.prevNLSF_Q15[i] + ((psDecCtrl.NLSFInterpCoef_Q2 * (pNLSF_Q15[i] - psDec.prevNLSF_Q15[i])) >> 2);
                }
                NLSF2AStable.SKP_Silk_NLSF2A_stable(psDecCtrl.PredCoef_Q12[0], pNLSF0_Q15, psDec.LPC_order);
            } else {
                System.arraycopy(psDecCtrl.PredCoef_Q12[1], 0, psDecCtrl.PredCoef_Q12[0], 0, psDec.LPC_order);
            }
        }
        System.arraycopy(pNLSF_Q15, 0, psDec.prevNLSF_Q15, 0, psDec.LPC_order);
        if (psDec.lossCnt != 0) {
            Bwexpander.SKP_Silk_bwexpander(psDecCtrl.PredCoef_Q12[0], psDec.LPC_order, 63570);
            Bwexpander.SKP_Silk_bwexpander(psDecCtrl.PredCoef_Q12[1], psDec.LPC_order, 63570);
        }
        if (psDecCtrl.sigtype == 0) {
            if (psDec.fs_kHz == 8) {
                RangeCoder.SKP_Silk_range_decoder(Ixs, 0, psRC, TablesPitchLag.SKP_Silk_pitch_lag_NB_CDF, 0, 43);
            } else if (psDec.fs_kHz == 12) {
                RangeCoder.SKP_Silk_range_decoder(Ixs, 0, psRC, TablesPitchLag.SKP_Silk_pitch_lag_MB_CDF, 0, 64);
            } else if (psDec.fs_kHz == 16) {
                RangeCoder.SKP_Silk_range_decoder(Ixs, 0, psRC, TablesPitchLag.SKP_Silk_pitch_lag_WB_CDF, 0, 86);
            } else {
                RangeCoder.SKP_Silk_range_decoder(Ixs, 0, psRC, TablesPitchLag.SKP_Silk_pitch_lag_SWB_CDF, 0, 128);
            }
            if (psDec.fs_kHz == 8) {
                RangeCoder.SKP_Silk_range_decoder(Ixs, 1, psRC, TablesPitchLag.SKP_Silk_pitch_contour_NB_CDF, 0, 5);
            } else {
                RangeCoder.SKP_Silk_range_decoder(Ixs, 1, psRC, TablesPitchLag.SKP_Silk_pitch_contour_CDF, 0, 17);
            }
            DecodePitch.SKP_Silk_decode_pitch(Ixs[0], Ixs[1], psDecCtrl.pitchL, psDec.fs_kHz);
            int[] PERIndex_ptr = new int[]{psDecCtrl.PERIndex};
            RangeCoder.SKP_Silk_range_decoder(PERIndex_ptr, 0, psRC, TablesLTP.SKP_Silk_LTP_per_index_CDF, 0, 1);
            psDecCtrl.PERIndex = PERIndex_ptr[0];
            short[] cbk_ptr_Q14 = TablesLTP.SKP_Silk_LTP_vq_ptrs_Q14[psDecCtrl.PERIndex];
            for (int k = 0; k < 4; k++) {
                RangeCoder.SKP_Silk_range_decoder(Ix_ptr, 0, psRC, TablesLTP.SKP_Silk_LTP_gain_CDF_ptrs[psDecCtrl.PERIndex], 0, TablesLTP.SKP_Silk_LTP_gain_CDF_offsets[psDecCtrl.PERIndex]);
                Ix = Ix_ptr[0];
                for (i = 0; i < 5; i++) {
                    psDecCtrl.LTPCoef_Q14[Macros.SKP_SMULBB(k, 5) + i] = cbk_ptr_Q14[Macros.SKP_SMULBB(Ix, 5) + i];
                }
            }
            RangeCoder.SKP_Silk_range_decoder(Ix_ptr, 0, psRC, TablesOther.SKP_Silk_LTPscale_CDF, 0, 2);
            psDecCtrl.LTP_scale_Q14 = TablesOther.SKP_Silk_LTPScales_table_Q14[Ix_ptr[0]];
        } else {
            Arrays.fill(psDecCtrl.pitchL, 0, 4, 0);
            Arrays.fill(psDecCtrl.LTPCoef_Q14, 0, 4, (short) 0);
            psDecCtrl.PERIndex = 0;
            psDecCtrl.LTP_scale_Q14 = 0;
        }
        RangeCoder.SKP_Silk_range_decoder(Ix_ptr, 0, psRC, TablesOther.SKP_Silk_Seed_CDF, 0, 2);
        psDecCtrl.Seed = Ix_ptr[0];
        DecodePulses.SKP_Silk_decode_pulses(psRC, psDecCtrl, q, psDec.frame_length);
        int[] vadFlag_ptr = new int[]{psDec.vadFlag};
        RangeCoder.SKP_Silk_range_decoder(vadFlag_ptr, 0, psRC, TablesOther.SKP_Silk_vadflag_CDF, 0, 1);
        psDec.vadFlag = vadFlag_ptr[0];
        int[] FrameTermination_ptr = new int[]{psDec.FrameTermination};
        RangeCoder.SKP_Silk_range_decoder(FrameTermination_ptr, 0, psRC, TablesOther.SKP_Silk_FrameTermination_CDF, 0, 2);
        psDec.FrameTermination = FrameTermination_ptr[0];
        int[] nBytesUsed_ptr = new int[1];
        RangeCoder.SKP_Silk_range_coder_get_length(psRC, nBytesUsed_ptr);
        psDec.nBytesLeft = psRC.bufferLength - nBytesUsed_ptr[0];
        if (psDec.nBytesLeft < 0) {
            psRC.error = -6;
        }
        if (psDec.nBytesLeft == 0) {
            RangeCoder.SKP_Silk_range_coder_check_after_decoding(psRC);
        }
    }
}
