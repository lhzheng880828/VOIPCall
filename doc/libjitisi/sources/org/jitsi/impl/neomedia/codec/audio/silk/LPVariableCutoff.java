package org.jitsi.impl.neomedia.codec.audio.silk;

import javax.media.Buffer;

public class LPVariableCutoff {
    static final /* synthetic */ boolean $assertionsDisabled = (!LPVariableCutoff.class.desiredAssertionStatus());

    static void SKP_Silk_LP_interpolate_filter_taps(int[] B_Q28, int[] A_Q28, int ind, int fac_Q16) {
        int i_djinn;
        int nb;
        int na;
        if (ind >= 4) {
            for (i_djinn = 0; i_djinn < 3; i_djinn++) {
                B_Q28[i_djinn] = TablesOther.SKP_Silk_Transition_LP_B_Q28[4][i_djinn];
            }
            for (i_djinn = 0; i_djinn < 2; i_djinn++) {
                A_Q28[i_djinn] = TablesOther.SKP_Silk_Transition_LP_A_Q28[4][i_djinn];
            }
        } else if (fac_Q16 <= 0) {
            for (i_djinn = 0; i_djinn < 3; i_djinn++) {
                B_Q28[i_djinn] = TablesOther.SKP_Silk_Transition_LP_B_Q28[ind][i_djinn];
            }
            for (i_djinn = 0; i_djinn < 2; i_djinn++) {
                A_Q28[i_djinn] = TablesOther.SKP_Silk_Transition_LP_A_Q28[ind][i_djinn];
            }
        } else if (fac_Q16 == SigProcFIX.SKP_SAT16(fac_Q16)) {
            for (nb = 0; nb < 3; nb++) {
                B_Q28[nb] = Macros.SKP_SMLAWB(TablesOther.SKP_Silk_Transition_LP_B_Q28[ind][nb], TablesOther.SKP_Silk_Transition_LP_B_Q28[ind + 1][nb] - TablesOther.SKP_Silk_Transition_LP_B_Q28[ind][nb], fac_Q16);
            }
            for (na = 0; na < 2; na++) {
                A_Q28[na] = Macros.SKP_SMLAWB(TablesOther.SKP_Silk_Transition_LP_A_Q28[ind][na], TablesOther.SKP_Silk_Transition_LP_A_Q28[ind + 1][na] - TablesOther.SKP_Silk_Transition_LP_A_Q28[ind][na], fac_Q16);
            }
        } else if (fac_Q16 == 32768) {
            for (nb = 0; nb < 3; nb++) {
                B_Q28[nb] = SigProcFIX.SKP_RSHIFT(TablesOther.SKP_Silk_Transition_LP_B_Q28[ind][nb] + TablesOther.SKP_Silk_Transition_LP_B_Q28[ind + 1][nb], 1);
            }
            for (na = 0; na < 2; na++) {
                A_Q28[na] = SigProcFIX.SKP_RSHIFT(TablesOther.SKP_Silk_Transition_LP_A_Q28[ind][na] + TablesOther.SKP_Silk_Transition_LP_A_Q28[ind + 1][na], 1);
            }
        } else if ($assertionsDisabled || Buffer.FLAG_SKIP_FEC - fac_Q16 == SigProcFIX.SKP_SAT16(Buffer.FLAG_SKIP_FEC - fac_Q16)) {
            for (nb = 0; nb < 3; nb++) {
                B_Q28[nb] = Macros.SKP_SMLAWB(TablesOther.SKP_Silk_Transition_LP_B_Q28[ind + 1][nb], TablesOther.SKP_Silk_Transition_LP_B_Q28[ind][nb] - TablesOther.SKP_Silk_Transition_LP_B_Q28[ind + 1][nb], Buffer.FLAG_SKIP_FEC - fac_Q16);
            }
            for (na = 0; na < 2; na++) {
                A_Q28[na] = Macros.SKP_SMLAWB(TablesOther.SKP_Silk_Transition_LP_A_Q28[ind + 1][na], TablesOther.SKP_Silk_Transition_LP_A_Q28[ind][na] - TablesOther.SKP_Silk_Transition_LP_A_Q28[ind + 1][na], Buffer.FLAG_SKIP_FEC - fac_Q16);
            }
        } else {
            throw new AssertionError();
        }
    }

    static void SKP_Silk_LP_variable_cutoff(SKP_Silk_LP_state psLP, short[] out, int out_offset, short[] in, int in_offset, int frame_length) {
        int[] B_Q28 = new int[3];
        int[] A_Q28 = new int[2];
        if (!$assertionsDisabled && psLP.transition_frame_no < 0) {
            throw new AssertionError();
        } else if ($assertionsDisabled || ((psLP.transition_frame_no <= 128 && psLP.mode == 0) || (psLP.transition_frame_no <= 256 && psLP.mode == 1))) {
            if (psLP.transition_frame_no > 0) {
                int fac_Q16;
                int ind;
                if (psLP.mode == 0) {
                    if (psLP.transition_frame_no < 128) {
                        fac_Q16 = psLP.transition_frame_no << 11;
                        ind = fac_Q16 >> 16;
                        fac_Q16 -= ind << 16;
                        if (!$assertionsDisabled && ind < 0) {
                            throw new AssertionError();
                        } else if ($assertionsDisabled || ind < 5) {
                            SKP_Silk_LP_interpolate_filter_taps(B_Q28, A_Q28, ind, fac_Q16);
                            psLP.transition_frame_no++;
                        } else {
                            throw new AssertionError();
                        }
                    } else if (psLP.transition_frame_no == 128) {
                        SKP_Silk_LP_interpolate_filter_taps(B_Q28, A_Q28, 4, 0);
                    }
                } else if (psLP.mode == 1) {
                    if (psLP.transition_frame_no < 256) {
                        fac_Q16 = (256 - psLP.transition_frame_no) << 10;
                        ind = fac_Q16 >> 16;
                        fac_Q16 -= ind << 16;
                        if (!$assertionsDisabled && ind < 0) {
                            throw new AssertionError();
                        } else if ($assertionsDisabled || ind < 5) {
                            SKP_Silk_LP_interpolate_filter_taps(B_Q28, A_Q28, ind, fac_Q16);
                            psLP.transition_frame_no++;
                        } else {
                            throw new AssertionError();
                        }
                    } else if (psLP.transition_frame_no == 256) {
                        SKP_Silk_LP_interpolate_filter_taps(B_Q28, A_Q28, 0, 0);
                    }
                }
            }
            if (psLP.transition_frame_no > 0) {
                BiquadAlt.SKP_Silk_biquad_alt(in, in_offset, B_Q28, A_Q28, psLP.In_LP_State, out, out_offset, frame_length);
                return;
            }
            for (int i_djinn = 0; i_djinn < frame_length; i_djinn++) {
                out[out_offset + i_djinn] = in[in_offset + i_djinn];
            }
        } else {
            throw new AssertionError();
        }
    }
}
