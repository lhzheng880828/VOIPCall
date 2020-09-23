package org.jitsi.impl.neomedia.codec.audio.silk;

import java.lang.reflect.Array;
import org.jitsi.impl.neomedia.device.DeviceConfiguration;
import org.jitsi.impl.neomedia.portaudio.Pa;

public class PitchAnalysisCoreFLP {
    static final /* synthetic */ boolean $assertionsDisabled = (!PitchAnalysisCoreFLP.class.desiredAssertionStatus() ? true : $assertionsDisabled);
    static final int SCRATCH_SIZE = 22;
    static final float eps = 1.1920929E-7f;

    static float SKP_P_log2(double x) {
        return (float) (3.32192809488736d * Math.log10(x));
    }

    static int SKP_Silk_pitch_analysis_core_FLP(float[] signal, int[] pitch_out, int[] lagIndex, int[] contourIndex, float[] LTPCorr, int prevLag, float search_thres1, float search_thres2, int Fs_kHz, int complexity) {
        float[] signal_8kHz = new float[320];
        float[] signal_4kHz = new float[160];
        float[] scratch_mem = new float[2880];
        float[] filt_state = new float[7];
        float[][] C = (float[][]) Array.newInstance(Float.TYPE, new int[]{4, 221});
        float[] CC = new float[11];
        int[] d_srch = new int[24];
        short[] d_comp = new short[221];
        float[][][] energies_st3 = (float[][][]) Array.newInstance(Float.TYPE, new int[]{4, 34, 5});
        float[][][] cross_corr_st3 = (float[][][]) Array.newInstance(Float.TYPE, new int[]{4, 34, 5});
        if (!$assertionsDisabled && Fs_kHz != 8 && Fs_kHz != 12 && Fs_kHz != 16 && Fs_kHz != 24) {
            throw new AssertionError();
        } else if (!$assertionsDisabled && complexity < 0) {
            throw new AssertionError();
        } else if (!$assertionsDisabled && complexity > 2) {
            throw new AssertionError();
        } else if (!$assertionsDisabled && (search_thres1 < 0.0f || search_thres1 > 1.0f)) {
            throw new AssertionError();
        } else if ($assertionsDisabled || (search_thres2 >= 0.0f && search_thres2 <= 1.0f)) {
            int i_djinn;
            int j_djinn;
            int i;
            float[] basis_ptr;
            int basis_ptr_offset;
            double cross_corr;
            float[] fArr;
            int d;
            int sf_length = (Fs_kHz * 40) >> 3;
            int sf_length_4kHz = 160 >> 3;
            int sf_length_8kHz = 320 >> 3;
            int min_lag = Fs_kHz * 2;
            int max_lag = Fs_kHz * 18;
            for (i_djinn = 0; i_djinn < 4; i_djinn++) {
                for (j_djinn = 0; j_djinn < 221; j_djinn++) {
                    C[i_djinn][j_djinn] = 0.0f;
                }
            }
            short[] signal_8;
            if (Fs_kHz == 12) {
                short[] signal_12 = new short[DeviceConfiguration.DEFAULT_VIDEO_HEIGHT];
                signal_8 = new short[320];
                int[] R23 = new int[6];
                for (i_djinn = 0; i_djinn < 6; i_djinn++) {
                    R23[i_djinn] = 0;
                }
                SigProcFLP.SKP_float2short_array(signal_12, 0, signal, 0, DeviceConfiguration.DEFAULT_VIDEO_HEIGHT);
                ResamplerDown23.SKP_Silk_resampler_down2_3(R23, 0, signal_8, 0, signal_12, 0, DeviceConfiguration.DEFAULT_VIDEO_HEIGHT);
                SigProcFLP.SKP_short2float_array(signal_8kHz, 0, signal_8, 0, 320);
            } else if (Fs_kHz == 16) {
                if (complexity == 2) {
                    for (i_djinn = 0; i_djinn < 4; i_djinn++) {
                        filt_state[i_djinn] = 0.0f;
                    }
                    Decimate2CoarseFLP.SKP_Silk_decimate2_coarse_FLP(signal, 0, filt_state, 0, signal_8kHz, 0, scratch_mem, 0, 320);
                } else {
                    for (i_djinn = 0; i_djinn < 2; i_djinn++) {
                        filt_state[i_djinn] = 0.0f;
                    }
                    Decimate2CoarsestFLP.SKP_Silk_decimate2_coarsest_FLP(signal, 0, filt_state, 0, signal_8kHz, 0, scratch_mem, 0, 320);
                }
            } else if (Fs_kHz == 24) {
                short[] signal_24 = new short[960];
                signal_8 = new short[320];
                int[] filt_state_fix = new int[8];
                SigProcFLP.SKP_float2short_array(signal_24, 0, signal, 0, 960);
                for (i_djinn = 0; i_djinn < 8; i_djinn++) {
                    filt_state_fix[i_djinn] = 0;
                }
                ResamplerDown3.SKP_Silk_resampler_down3(filt_state_fix, 0, signal_8, 0, signal_24, 0, 960);
                SigProcFLP.SKP_short2float_array(signal_8kHz, 0, signal_8, 0, 320);
            } else if ($assertionsDisabled || Fs_kHz == 8) {
                for (i_djinn = 0; i_djinn < 320; i_djinn++) {
                    signal_8kHz[i_djinn] = signal[i_djinn];
                }
            } else {
                throw new AssertionError();
            }
            if (complexity == 2) {
                for (i_djinn = 0; i_djinn < 4; i_djinn++) {
                    filt_state[i_djinn] = 0.0f;
                }
                Decimate2CoarseFLP.SKP_Silk_decimate2_coarse_FLP(signal_8kHz, 0, filt_state, 0, signal_4kHz, 0, scratch_mem, 0, 160);
            } else {
                for (i_djinn = 0; i_djinn < 4; i_djinn++) {
                    filt_state[i_djinn] = 0.0f;
                }
                Decimate2CoarsestFLP.SKP_Silk_decimate2_coarsest_FLP(signal_8kHz, 0, filt_state, 0, signal_4kHz, 0, scratch_mem, 0, 160);
            }
            for (i = 160 - 1; i > 0; i--) {
                signal_4kHz[i] = signal_4kHz[i] + signal_4kHz[i - 1];
            }
            float[] target_ptr = signal_4kHz;
            int target_ptr_offset = 160 >> 1;
            int k = 0;
            while (k < 2) {
                if (!$assertionsDisabled && target_ptr_offset < 0) {
                    throw new AssertionError();
                } else if ($assertionsDisabled || target_ptr_offset + 40 <= 160) {
                    basis_ptr = target_ptr;
                    basis_ptr_offset = target_ptr_offset - 8;
                    if (!$assertionsDisabled && basis_ptr_offset < 0) {
                        throw new AssertionError();
                    } else if ($assertionsDisabled || basis_ptr_offset + 40 <= 160) {
                        cross_corr = InnerProductFLP.SKP_Silk_inner_product_FLP(target_ptr, target_ptr_offset, basis_ptr, basis_ptr_offset, sf_length_8kHz);
                        double normalizer = EnergyFLP.SKP_Silk_energy_FLP(basis_ptr, basis_ptr_offset, sf_length_8kHz) + 1000.0d;
                        fArr = C[0];
                        fArr[8] = fArr[8] + ((float) (cross_corr / Math.sqrt(normalizer)));
                        d = 8 + 1;
                        while (d <= 72) {
                            basis_ptr_offset--;
                            if (!$assertionsDisabled && basis_ptr_offset < 0) {
                                throw new AssertionError();
                            } else if ($assertionsDisabled || basis_ptr_offset + 40 <= 160) {
                                cross_corr = InnerProductFLP.SKP_Silk_inner_product_FLP(target_ptr, target_ptr_offset, basis_ptr, basis_ptr_offset, sf_length_8kHz);
                                normalizer += (double) ((basis_ptr[basis_ptr_offset + 0] * basis_ptr[basis_ptr_offset + 0]) - (basis_ptr[basis_ptr_offset + 40] * basis_ptr[basis_ptr_offset + 40]));
                                fArr = C[0];
                                fArr[d] = fArr[d] + ((float) (cross_corr / Math.sqrt(normalizer)));
                                d++;
                            } else {
                                throw new AssertionError();
                            }
                        }
                        target_ptr_offset += 40;
                        k++;
                    } else {
                        throw new AssertionError();
                    }
                } else {
                    throw new AssertionError();
                }
            }
            for (i = 72; i >= 8; i--) {
                fArr = C[0];
                fArr[i] = fArr[i] - ((C[0][i] * ((float) i)) / 4096.0f);
            }
            int length_d_srch = complexity + 5;
            if ($assertionsDisabled || length_d_srch <= 24) {
                SortFLP.SKP_Silk_insertion_sort_decreasing_FLP(C[0], 8, d_srch, 65, length_d_srch);
                float Cmax = C[0][8];
                target_ptr = signal_4kHz;
                target_ptr_offset = 160 >> 1;
                double energy = 1000.0d;
                for (i = 0; i < 80; i++) {
                    energy += (double) (target_ptr[i + 80] * target_ptr[i + 80]);
                }
                if (energy / 16.0d > ((double) (Cmax * Cmax))) {
                    for (i_djinn = 0; i_djinn < 4; i_djinn++) {
                        pitch_out[i_djinn] = 0;
                    }
                    LTPCorr[0] = 0.0f;
                    lagIndex[0] = 0;
                    contourIndex[0] = 0;
                    return 1;
                }
                float threshold = search_thres1 * Cmax;
                for (i = 0; i < length_d_srch; i++) {
                    if (C[0][8 + i] <= threshold) {
                        length_d_srch = i;
                        break;
                    }
                    d_srch[i] = (d_srch[i] + 8) << 1;
                }
                if ($assertionsDisabled || length_d_srch > 0) {
                    int j;
                    float prevLag_log2;
                    int nb_cbks_stage2;
                    float CCmax_new;
                    for (i = 16 - 5; i < 149; i++) {
                        d_comp[i] = (short) 0;
                    }
                    for (i = 0; i < length_d_srch; i++) {
                        d_comp[d_srch[i]] = (short) 1;
                    }
                    for (i = 144 + 3; i >= 16; i--) {
                        d_comp[i] = (short) (d_comp[i] + (d_comp[i - 1] + d_comp[i - 2]));
                    }
                    length_d_srch = 0;
                    for (i = 16; i < 145; i++) {
                        if (d_comp[i + 1] > (short) 0) {
                            d_srch[length_d_srch] = i;
                            length_d_srch++;
                        }
                    }
                    for (i = 144 + 3; i >= 16; i--) {
                        d_comp[i] = (short) (d_comp[i] + ((d_comp[i - 1] + d_comp[i - 2]) + d_comp[i - 3]));
                    }
                    int length_d_comp = 0;
                    for (i = 16; i < 148; i++) {
                        if (d_comp[i] > (short) 0) {
                            d_comp[length_d_comp] = (short) (i - 2);
                            length_d_comp++;
                        }
                    }
                    for (i_djinn = 0; i_djinn < 4; i_djinn++) {
                        for (j_djinn = 0; j_djinn < 221; j_djinn++) {
                            C[i_djinn][j_djinn] = 0.0f;
                        }
                    }
                    target_ptr = signal_8kHz;
                    target_ptr_offset = 160;
                    k = 0;
                    while (k < 4) {
                        if (!$assertionsDisabled && target_ptr_offset < 0) {
                            throw new AssertionError();
                        } else if ($assertionsDisabled || target_ptr_offset + 40 <= 320) {
                            double energy_tmp = EnergyFLP.SKP_Silk_energy_FLP(target_ptr, target_ptr_offset, sf_length_8kHz);
                            j = 0;
                            while (j < length_d_comp) {
                                d = d_comp[j];
                                basis_ptr = target_ptr;
                                basis_ptr_offset = target_ptr_offset - d;
                                if (!$assertionsDisabled && basis_ptr_offset < 0) {
                                    throw new AssertionError();
                                } else if ($assertionsDisabled || basis_ptr_offset + 40 <= 320) {
                                    cross_corr = InnerProductFLP.SKP_Silk_inner_product_FLP(basis_ptr, basis_ptr_offset, target_ptr, target_ptr_offset, sf_length_8kHz);
                                    energy = EnergyFLP.SKP_Silk_energy_FLP(basis_ptr, basis_ptr_offset, sf_length_8kHz);
                                    if (cross_corr > Pa.LATENCY_UNSPECIFIED) {
                                        C[k][d] = (float) ((cross_corr * cross_corr) / ((energy * energy_tmp) + 1.1920928955078125E-7d));
                                    } else {
                                        C[k][d] = 0.0f;
                                    }
                                    j++;
                                } else {
                                    throw new AssertionError();
                                }
                            }
                            target_ptr_offset += 40;
                            k++;
                        } else {
                            throw new AssertionError();
                        }
                    }
                    float CCmax = 0.0f;
                    float CCmax_b = -1000.0f;
                    int CBimax = 0;
                    int lag = -1;
                    if (prevLag > 0) {
                        if (Fs_kHz == 12) {
                            prevLag = (prevLag << 1) / 3;
                        } else if (Fs_kHz == 16) {
                            prevLag >>= 1;
                        } else if (Fs_kHz == 24) {
                            prevLag /= 3;
                        }
                        prevLag_log2 = SKP_P_log2((double) prevLag);
                    } else {
                        prevLag_log2 = 0.0f;
                    }
                    if (Fs_kHz != 8 || complexity <= 0) {
                        nb_cbks_stage2 = 3;
                    } else {
                        nb_cbks_stage2 = 11;
                    }
                    for (k = 0; k < length_d_srch; k++) {
                        d = d_srch[k];
                        for (j = 0; j < nb_cbks_stage2; j++) {
                            CC[j] = 0.0f;
                            for (i = 0; i < 4; i++) {
                                CC[j] = CC[j] + C[i][PitchEstTables.SKP_Silk_CB_lags_stage2[i][j] + d];
                            }
                        }
                        CCmax_new = -1000.0f;
                        int CBimax_new = 0;
                        for (i = 0; i < nb_cbks_stage2; i++) {
                            if (CC[i] > CCmax_new) {
                                CCmax_new = CC[i];
                                CBimax_new = i;
                            }
                        }
                        CCmax_new = Math.max(CCmax_new, 0.0f);
                        float CCmax_new_b = CCmax_new;
                        float lag_log2 = SKP_P_log2((double) d);
                        CCmax_new_b -= 0.8f * lag_log2;
                        if (prevLag > 0) {
                            float delta_lag_log2_sqr = lag_log2 - prevLag_log2;
                            delta_lag_log2_sqr *= delta_lag_log2_sqr;
                            CCmax_new_b -= ((0.8f * LTPCorr[0]) * delta_lag_log2_sqr) / (0.5f + delta_lag_log2_sqr);
                        }
                        if (CCmax_new_b > CCmax_b && CCmax_new > (4.0f * search_thres2) * search_thres2) {
                            CCmax_b = CCmax_new_b;
                            CCmax = CCmax_new;
                            lag = d;
                            CBimax = CBimax_new;
                        }
                    }
                    if (lag == -1) {
                        for (i_djinn = 0; i_djinn < 4; i_djinn++) {
                            pitch_out[i_djinn] = 0;
                        }
                        LTPCorr[0] = 0.0f;
                        lagIndex[0] = 0;
                        contourIndex[0] = 0;
                        return 1;
                    }
                    if (Fs_kHz > 8) {
                        if ($assertionsDisabled || lag == SigProcFIX.SKP_SAT16(lag)) {
                            if (Fs_kHz == 12) {
                                lag = SigProcFIX.SKP_RSHIFT_ROUND(Macros.SKP_SMULBB(lag, 3), 1);
                            } else if (Fs_kHz == 16) {
                                lag <<= 1;
                            } else {
                                lag = Macros.SKP_SMULBB(lag, 3);
                            }
                            lag = SigProcFIX.SKP_LIMIT_int(lag, min_lag, max_lag);
                            int start_lag = Math.max(lag - 2, min_lag);
                            int end_lag = Math.min(lag + 2, max_lag);
                            int lag_new = lag;
                            CBimax = 0;
                            if ($assertionsDisabled || CCmax >= 0.0f) {
                                LTPCorr[0] = (float) Math.sqrt((double) (CCmax / 4.0f));
                                CCmax = -1000.0f;
                                SKP_P_Ana_calc_corr_st3(cross_corr_st3, signal, 0, start_lag, sf_length, complexity);
                                SKP_P_Ana_calc_energy_st3(energies_st3, signal, 0, start_lag, sf_length, complexity);
                                int lag_counter = 0;
                                if ($assertionsDisabled || lag == SigProcFIX.SKP_SAT16(lag)) {
                                    float contour_bias = 0.05f / ((float) lag);
                                    int cbk_size = PitchEstTables.SKP_Silk_cbk_sizes_stage3[complexity];
                                    int cbk_offset = PitchEstTables.SKP_Silk_cbk_offsets_stage3[complexity];
                                    for (d = start_lag; d <= end_lag; d++) {
                                        for (j = cbk_offset; j < cbk_offset + cbk_size; j++) {
                                            cross_corr = Pa.LATENCY_UNSPECIFIED;
                                            energy = 1.1920928955078125E-7d;
                                            for (k = 0; k < 4; k++) {
                                                energy += (double) energies_st3[k][j][lag_counter];
                                                cross_corr += (double) cross_corr_st3[k][j][lag_counter];
                                            }
                                            if (cross_corr > Pa.LATENCY_UNSPECIFIED) {
                                                int diff = j - 17;
                                                CCmax_new = ((float) ((cross_corr * cross_corr) / energy)) * (1.0f - ((((float) diff) * contour_bias) * ((float) diff)));
                                            } else {
                                                CCmax_new = 0.0f;
                                            }
                                            if (CCmax_new > CCmax) {
                                                CCmax = CCmax_new;
                                                lag_new = d;
                                                CBimax = j;
                                            }
                                        }
                                        lag_counter++;
                                    }
                                    for (k = 0; k < 4; k++) {
                                        pitch_out[k] = PitchEstTables.SKP_Silk_CB_lags_stage3[k][CBimax] + lag_new;
                                    }
                                    lagIndex[0] = lag_new - min_lag;
                                    contourIndex[0] = CBimax;
                                } else {
                                    throw new AssertionError();
                                }
                            }
                            throw new AssertionError();
                        }
                        throw new AssertionError();
                    } else if ($assertionsDisabled || CCmax >= 0.0f) {
                        LTPCorr[0] = (float) Math.sqrt((double) (CCmax / 4.0f));
                        for (k = 0; k < 4; k++) {
                            pitch_out[k] = PitchEstTables.SKP_Silk_CB_lags_stage2[k][CBimax] + lag;
                        }
                        lagIndex[0] = lag - min_lag;
                        contourIndex[0] = CBimax;
                    } else {
                        throw new AssertionError();
                    }
                    if ($assertionsDisabled || lagIndex[0] >= 0) {
                        return 0;
                    }
                    throw new AssertionError();
                }
                throw new AssertionError();
            }
            throw new AssertionError();
        } else {
            throw new AssertionError();
        }
    }

    static void SKP_P_Ana_calc_corr_st3(float[][][] cross_corr_st3, float[] signal, int signal_offset, int start_lag, int sf_length, int complexity) {
        float[] scratch_mem = new float[22];
        if (!$assertionsDisabled && complexity < 0) {
            throw new AssertionError();
        } else if ($assertionsDisabled || complexity <= 2) {
            int cbk_offset = PitchEstTables.SKP_Silk_cbk_offsets_stage3[complexity];
            int cbk_size = PitchEstTables.SKP_Silk_cbk_sizes_stage3[complexity];
            float[] target_ptr = signal;
            int target_ptr_offset = signal_offset + (sf_length << 2);
            for (int k = 0; k < 4; k++) {
                int lag_counter = 0;
                short j = PitchEstTables.SKP_Silk_Lag_range_stage3[complexity][k][0];
                while (j <= PitchEstTables.SKP_Silk_Lag_range_stage3[complexity][k][1]) {
                    float[] basis_ptr = target_ptr;
                    int basis_ptr_offset = target_ptr_offset - (start_lag + j);
                    if ($assertionsDisabled || lag_counter < 22) {
                        scratch_mem[lag_counter] = (float) InnerProductFLP.SKP_Silk_inner_product_FLP(target_ptr, target_ptr_offset, basis_ptr, basis_ptr_offset, sf_length);
                        lag_counter++;
                        j++;
                    } else {
                        throw new AssertionError();
                    }
                }
                int delta = PitchEstTables.SKP_Silk_Lag_range_stage3[complexity][k][0];
                for (int i = cbk_offset; i < cbk_offset + cbk_size; i++) {
                    int idx = PitchEstTables.SKP_Silk_CB_lags_stage3[k][i] - delta;
                    int j2 = 0;
                    while (j2 < 5) {
                        if (!$assertionsDisabled && idx + j2 >= 22) {
                            throw new AssertionError();
                        } else if ($assertionsDisabled || idx + j2 < lag_counter) {
                            cross_corr_st3[k][i][j2] = scratch_mem[idx + j2];
                            j2++;
                        } else {
                            throw new AssertionError();
                        }
                    }
                }
                target_ptr_offset += sf_length;
            }
        } else {
            throw new AssertionError();
        }
    }

    static void SKP_P_Ana_calc_energy_st3(float[][][] energies_st3, float[] signal, int signal_offset, int start_lag, int sf_length, int complexity) {
        float[] scratch_mem = new float[22];
        if (!$assertionsDisabled && complexity < 0) {
            throw new AssertionError();
        } else if ($assertionsDisabled || complexity <= 2) {
            int cbk_offset = PitchEstTables.SKP_Silk_cbk_offsets_stage3[complexity];
            int cbk_size = PitchEstTables.SKP_Silk_cbk_sizes_stage3[complexity];
            float[] target_ptr = signal;
            int target_ptr_offset = signal_offset + (sf_length << 2);
            int k = 0;
            while (k < 4) {
                float[] basis_ptr = target_ptr;
                int basis_ptr_offset = target_ptr_offset - (PitchEstTables.SKP_Silk_Lag_range_stage3[complexity][k][0] + start_lag);
                double energy = EnergyFLP.SKP_Silk_energy_FLP(basis_ptr, basis_ptr_offset, sf_length) + 0.001d;
                if ($assertionsDisabled || energy >= Pa.LATENCY_UNSPECIFIED) {
                    scratch_mem[0] = (float) energy;
                    int lag_counter = 0 + 1;
                    int i = 1;
                    while (i < (PitchEstTables.SKP_Silk_Lag_range_stage3[complexity][k][1] - PitchEstTables.SKP_Silk_Lag_range_stage3[complexity][k][0]) + 1) {
                        energy -= (double) (basis_ptr[(basis_ptr_offset + sf_length) - i] * basis_ptr[(basis_ptr_offset + sf_length) - i]);
                        if ($assertionsDisabled || energy >= Pa.LATENCY_UNSPECIFIED) {
                            energy += (double) (basis_ptr[basis_ptr_offset - i] * basis_ptr[basis_ptr_offset - i]);
                            if (!$assertionsDisabled && energy < Pa.LATENCY_UNSPECIFIED) {
                                throw new AssertionError();
                            } else if ($assertionsDisabled || lag_counter < 22) {
                                scratch_mem[lag_counter] = (float) energy;
                                lag_counter++;
                                i++;
                            } else {
                                throw new AssertionError();
                            }
                        }
                        throw new AssertionError();
                    }
                    int delta = PitchEstTables.SKP_Silk_Lag_range_stage3[complexity][k][0];
                    i = cbk_offset;
                    while (i < cbk_offset + cbk_size) {
                        int idx = PitchEstTables.SKP_Silk_CB_lags_stage3[k][i] - delta;
                        int j = 0;
                        while (j < 5) {
                            if (!$assertionsDisabled && idx + j >= 22) {
                                throw new AssertionError();
                            } else if ($assertionsDisabled || idx + j < lag_counter) {
                                energies_st3[k][i][j] = scratch_mem[idx + j];
                                if ($assertionsDisabled || energies_st3[k][i][j] >= 0.0f) {
                                    j++;
                                } else {
                                    throw new AssertionError();
                                }
                            } else {
                                throw new AssertionError();
                            }
                        }
                        i++;
                    }
                    target_ptr_offset += sf_length;
                    k++;
                } else {
                    throw new AssertionError();
                }
            }
        } else {
            throw new AssertionError();
        }
    }
}
