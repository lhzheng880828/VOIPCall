package org.jitsi.impl.neomedia.codec.audio.silk;

import com.lti.utils.UnsignedUtils;
import java.lang.reflect.Array;

public class VAD {
    static final /* synthetic */ boolean $assertionsDisabled;
    static int[] tiltWeights = new int[]{30000, 6000, -12000, -12000};

    static {
        boolean z;
        if (VAD.class.desiredAssertionStatus()) {
            z = false;
        } else {
            z = true;
        }
        $assertionsDisabled = z;
    }

    static int SKP_Silk_VAD_Init(SKP_Silk_VAD_state psSilk_VAD) {
        int b;
        for (b = 0; b < 4; b++) {
            psSilk_VAD.NoiseLevelBias[b] = Math.max(50 / (b + 1), 1);
        }
        for (b = 0; b < 4; b++) {
            psSilk_VAD.NL[b] = psSilk_VAD.NoiseLevelBias[b] * 100;
            psSilk_VAD.inv_NL[b] = Integer.MAX_VALUE / psSilk_VAD.NL[b];
        }
        psSilk_VAD.counter = 15;
        for (b = 0; b < 4; b++) {
            psSilk_VAD.NrgRatioSmth_Q8[b] = 25600;
        }
        return 0;
    }

    static int SKP_Silk_VAD_GetSA_Q8(SKP_Silk_VAD_state psSilk_VAD, int[] pSA_Q8, int[] pSNR_dB_Q7, int[] pQuality_Q15, int[] pTilt_Q15, short[] pIn, int pIn_offset, int framelength) {
        int[] scratch = new int[720];
        int sumSquared = 0;
        short[][] X = (short[][]) Array.newInstance(Short.TYPE, new int[]{4, 240});
        int[] Xnrg = new int[4];
        int[] NrgToNoiseRatio_Q8 = new int[4];
        if (!$assertionsDisabled && 480 < framelength) {
            throw new AssertionError();
        } else if ($assertionsDisabled || framelength <= 512) {
            int i;
            short[] sArr;
            int b;
            int speech_nrg;
            AnaFiltBank1.SKP_Silk_ana_filt_bank_1(pIn, pIn_offset, psSilk_VAD.AnaState, 0, X[0], 0, X[3], 0, scratch, framelength);
            AnaFiltBank1.SKP_Silk_ana_filt_bank_1(X[0], 0, psSilk_VAD.AnaState1, 0, X[0], 0, X[2], 0, scratch, framelength >> 1);
            AnaFiltBank1.SKP_Silk_ana_filt_bank_1(X[0], 0, psSilk_VAD.AnaState2, 0, X[0], 0, X[1], 0, scratch, framelength >> 2);
            int decimated_framelength = framelength >> 3;
            X[0][decimated_framelength - 1] = (short) (X[0][decimated_framelength - 1] >> 1);
            short HPstateTmp = X[0][decimated_framelength - 1];
            for (i = decimated_framelength - 1; i > 0; i--) {
                X[0][i - 1] = (short) (X[0][i - 1] >> 1);
                sArr = X[0];
                sArr[i] = (short) (sArr[i] - X[0][i - 1]);
            }
            sArr = X[0];
            sArr[0] = (short) (sArr[0] - psSilk_VAD.HPstate);
            psSilk_VAD.HPstate = HPstateTmp;
            for (b = 0; b < 4; b++) {
                int dec_subframe_length = (framelength >> Math.min(4 - b, 3)) >> 2;
                int dec_subframe_offset = 0;
                Xnrg[b] = psSilk_VAD.XnrgSubfr[b];
                for (int s = 0; s < 4; s++) {
                    sumSquared = 0;
                    i = 0;
                    while (i < dec_subframe_length) {
                        int x_tmp = X[b][i + dec_subframe_offset] >> 3;
                        sumSquared = Macros.SKP_SMLABB(sumSquared, x_tmp, x_tmp);
                        if ($assertionsDisabled || sumSquared >= 0) {
                            i++;
                        } else {
                            throw new AssertionError();
                        }
                    }
                    if (s < 3) {
                        Xnrg[b] = SigProcFIX.SKP_ADD_POS_SAT32(Xnrg[b], sumSquared);
                    } else {
                        Xnrg[b] = SigProcFIX.SKP_ADD_POS_SAT32(Xnrg[b], sumSquared >> 1);
                    }
                    dec_subframe_offset += dec_subframe_length;
                }
                psSilk_VAD.XnrgSubfr[b] = sumSquared;
            }
            SKP_Silk_VAD_GetNoiseLevels(Xnrg, psSilk_VAD);
            sumSquared = 0;
            int input_tilt = 0;
            for (b = 0; b < 4; b++) {
                speech_nrg = Xnrg[b] - psSilk_VAD.NL[b];
                if (speech_nrg > 0) {
                    if ((Xnrg[b] & -8388608) == 0) {
                        NrgToNoiseRatio_Q8[b] = (Xnrg[b] << 8) / (psSilk_VAD.NL[b] + 1);
                    } else {
                        NrgToNoiseRatio_Q8[b] = Xnrg[b] / ((psSilk_VAD.NL[b] >> 8) + 1);
                    }
                    int SNR_Q7 = Lin2log.SKP_Silk_lin2log(NrgToNoiseRatio_Q8[b]) - 1024;
                    sumSquared = Macros.SKP_SMLABB(sumSquared, SNR_Q7, SNR_Q7);
                    if (speech_nrg < 1048576) {
                        SNR_Q7 = Macros.SKP_SMULWB(Inlines.SKP_Silk_SQRT_APPROX(speech_nrg) << 6, SNR_Q7);
                    }
                    input_tilt = Macros.SKP_SMLAWB(input_tilt, tiltWeights[b], SNR_Q7);
                } else {
                    NrgToNoiseRatio_Q8[b] = 256;
                }
            }
            pSNR_dB_Q7[0] = (short) (Inlines.SKP_Silk_SQRT_APPROX(sumSquared / 4) * 3);
            int SA_Q15 = SigmQ15.SKP_Silk_sigm_Q15(Macros.SKP_SMULWB(45000, pSNR_dB_Q7[0]) - 128);
            pTilt_Q15[0] = (SigmQ15.SKP_Silk_sigm_Q15(input_tilt) - 16384) << 1;
            speech_nrg = 0;
            for (b = 0; b < 4; b++) {
                speech_nrg += (b + 1) * ((Xnrg[b] - psSilk_VAD.NL[b]) >> 4);
            }
            if (speech_nrg <= 0) {
                SA_Q15 >>= 1;
            } else if (speech_nrg < 32768) {
                SA_Q15 = Macros.SKP_SMULWB(32768 + Inlines.SKP_Silk_SQRT_APPROX(speech_nrg << 15), SA_Q15);
            }
            pSA_Q8[0] = Math.min(SA_Q15 >> 7, UnsignedUtils.MAX_UBYTE);
            int smooth_coef_Q16 = Macros.SKP_SMULWB(4096, Macros.SKP_SMULWB(SA_Q15, SA_Q15));
            for (b = 0; b < 4; b++) {
                psSilk_VAD.NrgRatioSmth_Q8[b] = Macros.SKP_SMLAWB(psSilk_VAD.NrgRatioSmth_Q8[b], NrgToNoiseRatio_Q8[b] - psSilk_VAD.NrgRatioSmth_Q8[b], smooth_coef_Q16);
                pQuality_Q15[b] = SigmQ15.SKP_Silk_sigm_Q15((((Lin2log.SKP_Silk_lin2log(psSilk_VAD.NrgRatioSmth_Q8[b]) - 1024) * 3) - 2048) >> 4);
            }
            return 0;
        } else {
            throw new AssertionError();
        }
    }

    static void SKP_Silk_VAD_GetNoiseLevels(int[] pX, SKP_Silk_VAD_state psSilk_VAD) {
        int min_coef;
        if (psSilk_VAD.counter < 1000) {
            min_coef = 32767 / ((psSilk_VAD.counter >> 4) + 1);
        } else {
            min_coef = 0;
        }
        int k = 0;
        while (k < 4) {
            int nl = psSilk_VAD.NL[k];
            if ($assertionsDisabled || nl >= 0) {
                int nrg = SigProcFIX.SKP_ADD_POS_SAT32(pX[k], psSilk_VAD.NoiseLevelBias[k]);
                if ($assertionsDisabled || nrg > 0) {
                    int inv_nrg = Integer.MAX_VALUE / nrg;
                    if ($assertionsDisabled || inv_nrg >= 0) {
                        int coef;
                        if (nrg > (nl << 3)) {
                            coef = 128;
                        } else if (nrg < nl) {
                            coef = 1024;
                        } else {
                            coef = Macros.SKP_SMULWB(Macros.SKP_SMULWW(inv_nrg, nl), 2048);
                        }
                        psSilk_VAD.inv_NL[k] = Macros.SKP_SMLAWB(psSilk_VAD.inv_NL[k], inv_nrg - psSilk_VAD.inv_NL[k], Math.max(coef, min_coef));
                        if ($assertionsDisabled || psSilk_VAD.inv_NL[k] >= 0) {
                            nl = Integer.MAX_VALUE / psSilk_VAD.inv_NL[k];
                            if ($assertionsDisabled || nl >= 0) {
                                psSilk_VAD.NL[k] = Math.min(nl, 16777215);
                                k++;
                            } else {
                                throw new AssertionError();
                            }
                        }
                        throw new AssertionError();
                    }
                    throw new AssertionError();
                }
                throw new AssertionError();
            }
            throw new AssertionError();
        }
        psSilk_VAD.counter++;
    }
}
