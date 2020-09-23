package org.jitsi.impl.neomedia.codec.audio.silk;

import com.sun.media.format.WavAudioFormat;
import java.util.Arrays;
import org.jitsi.impl.neomedia.device.DeviceConfiguration;

public class Resampler {
    static final /* synthetic */ boolean $assertionsDisabled = (!Resampler.class.desiredAssertionStatus());

    static int gcd(int a, int b) {
        while (b > 0) {
            int tmp = a - ((a / b) * b);
            a = b;
            b = tmp;
        }
        return a;
    }

    static int SKP_Silk_resampler_init(SKP_Silk_resampler_state_struct S, int Fs_Hz_in, int Fs_Hz_out) {
        int up2 = 0;
        int down2 = 0;
        S.memZero();
        if (Fs_Hz_in >= 8000 && Fs_Hz_in <= 192000 && Fs_Hz_out >= 8000 && Fs_Hz_out <= 192000) {
            if (Fs_Hz_in > 96000) {
                S.nPreDownsamplers = 2;
                S.down_pre_function = "SKP_Silk_resampler_private_down4";
                S.downPreCB = new DownPreImplDown4();
            } else if (Fs_Hz_in > 48000) {
                S.nPreDownsamplers = 1;
                S.down_pre_function = "SKP_Silk_resampler_down2";
                S.downPreCB = new DownPreImplDown2();
            } else {
                S.nPreDownsamplers = 0;
                S.down_pre_function = null;
                S.downPreCB = null;
            }
            if (Fs_Hz_out > 96000) {
                S.nPostUpsamplers = 2;
                S.up_post_function = "SKP_Silk_resampler_private_up4";
                S.upPostCB = new UpPostImplUp4();
            } else if (Fs_Hz_out > 48000) {
                S.nPostUpsamplers = 1;
                S.up_post_function = "SKP_Silk_resampler_up2";
                S.upPostCB = new UpPostImplUp2();
            } else {
                S.nPostUpsamplers = 0;
                S.up_post_function = null;
                S.upPostCB = null;
            }
            if (S.nPreDownsamplers + S.nPostUpsamplers > 0) {
                S.ratio_Q16 = ((Fs_Hz_out << 13) / Fs_Hz_in) << 3;
                while (Macros.SKP_SMULWW(S.ratio_Q16, Fs_Hz_in) < Fs_Hz_out) {
                    S.ratio_Q16++;
                }
                S.batchSizePrePost = Fs_Hz_in / 100;
                Fs_Hz_in >>= S.nPreDownsamplers;
                Fs_Hz_out >>= S.nPostUpsamplers;
            }
            S.batchSize = Fs_Hz_in / 100;
            if (!(S.batchSize * 100 == Fs_Hz_in && Fs_Hz_in % 100 == 0)) {
                int cycleLen = Fs_Hz_in / gcd(Fs_Hz_in, Fs_Hz_out);
                int cyclesPerBatch = DeviceConfiguration.DEFAULT_VIDEO_HEIGHT / cycleLen;
                if (cyclesPerBatch == 0) {
                    S.batchSize = DeviceConfiguration.DEFAULT_VIDEO_HEIGHT;
                    if (!$assertionsDisabled) {
                        throw new AssertionError();
                    }
                }
                S.batchSize = cyclesPerBatch * cycleLen;
            }
            if (Fs_Hz_out > Fs_Hz_in) {
                if (Fs_Hz_out == Fs_Hz_in * 2) {
                    S.resampler_function = "SKP_Silk_resampler_private_up2_HQ_wrapper";
                    S.resamplerCB = new ResamplerImplWrapper();
                } else {
                    S.resampler_function = "SKP_Silk_resampler_private_IIR_FIR";
                    S.resamplerCB = new ResamplerImplIIRFIR();
                    up2 = 1;
                    if (Fs_Hz_in > 24000) {
                        S.up2_function = "SKP_Silk_resampler_up2";
                        S.up2CB = new Up2ImplUp2();
                    } else {
                        S.up2_function = "SKP_Silk_resampler_private_up2_HQ";
                        S.up2CB = new Up2ImplHQ();
                    }
                }
            } else if (Fs_Hz_out >= Fs_Hz_in) {
                S.resampler_function = "SKP_Silk_resampler_private_copy";
                S.resamplerCB = new ResamplerImplCopy();
            } else if (Fs_Hz_out * 4 == Fs_Hz_in * 3) {
                S.FIR_Fracs = 3;
                S.Coefs = ResamplerRom.SKP_Silk_Resampler_3_4_COEFS;
                S.resampler_function = "SKP_Silk_resampler_private_down_FIR";
                S.resamplerCB = new ResamplerImplDownFIR();
            } else if (Fs_Hz_out * 3 == Fs_Hz_in * 2) {
                S.FIR_Fracs = 2;
                S.Coefs = ResamplerRom.SKP_Silk_Resampler_2_3_COEFS;
                S.resampler_function = "SKP_Silk_resampler_private_down_FIR";
                S.resamplerCB = new ResamplerImplDownFIR();
            } else if (Fs_Hz_out * 2 == Fs_Hz_in) {
                S.FIR_Fracs = 1;
                S.Coefs = ResamplerRom.SKP_Silk_Resampler_1_2_COEFS;
                S.resampler_function = "SKP_Silk_resampler_private_down_FIR";
                S.resamplerCB = new ResamplerImplDownFIR();
            } else if (Fs_Hz_out * 8 == Fs_Hz_in * 3) {
                S.FIR_Fracs = 3;
                S.Coefs = ResamplerRom.SKP_Silk_Resampler_3_8_COEFS;
                S.resampler_function = "SKP_Silk_resampler_private_down_FIR";
                S.resamplerCB = new ResamplerImplDownFIR();
            } else if (Fs_Hz_out * 3 == Fs_Hz_in) {
                S.FIR_Fracs = 1;
                S.Coefs = ResamplerRom.SKP_Silk_Resampler_1_3_COEFS;
                S.resampler_function = "SKP_Silk_resampler_private_down_FIR";
                S.resamplerCB = new ResamplerImplDownFIR();
            } else if (Fs_Hz_out * 4 == Fs_Hz_in) {
                S.FIR_Fracs = 1;
                down2 = 1;
                S.Coefs = ResamplerRom.SKP_Silk_Resampler_1_2_COEFS;
                S.resampler_function = "SKP_Silk_resampler_private_down_FIR";
                S.resamplerCB = new ResamplerImplDownFIR();
            } else if (Fs_Hz_out * 6 == Fs_Hz_in) {
                S.FIR_Fracs = 1;
                down2 = 1;
                S.Coefs = ResamplerRom.SKP_Silk_Resampler_1_3_COEFS;
                S.resampler_function = "SKP_Silk_resampler_private_down_FIR";
                S.resamplerCB = new ResamplerImplDownFIR();
            } else if (Fs_Hz_out * 441 == Fs_Hz_in * 80) {
                S.Coefs = ResamplerRom.SKP_Silk_Resampler_80_441_ARMA4_COEFS;
                S.resampler_function = "SKP_Silk_resampler_private_IIR_FIR";
                S.resamplerCB = new ResamplerImplIIRFIR();
            } else if (Fs_Hz_out * 441 == Fs_Hz_in * WavAudioFormat.WAVE_FORMAT_VOXWARE_VR18) {
                S.Coefs = ResamplerRom.SKP_Silk_Resampler_120_441_ARMA4_COEFS;
                S.resampler_function = "SKP_Silk_resampler_private_IIR_FIR";
                S.resamplerCB = new ResamplerImplIIRFIR();
            } else if (Fs_Hz_out * 441 == Fs_Hz_in * 160) {
                S.Coefs = ResamplerRom.SKP_Silk_Resampler_160_441_ARMA4_COEFS;
                S.resampler_function = "SKP_Silk_resampler_private_IIR_FIR";
                S.resamplerCB = new ResamplerImplIIRFIR();
            } else if (Fs_Hz_out * 441 == Fs_Hz_in * 240) {
                S.Coefs = ResamplerRom.SKP_Silk_Resampler_240_441_ARMA4_COEFS;
                S.resampler_function = "SKP_Silk_resampler_private_IIR_FIR";
                S.resamplerCB = new ResamplerImplIIRFIR();
            } else if (Fs_Hz_out * 441 == Fs_Hz_in * 320) {
                S.Coefs = ResamplerRom.SKP_Silk_Resampler_320_441_ARMA4_COEFS;
                S.resampler_function = "SKP_Silk_resampler_private_IIR_FIR";
                S.resamplerCB = new ResamplerImplIIRFIR();
            } else {
                S.resampler_function = "SKP_Silk_resampler_private_IIR_FIR";
                S.resamplerCB = new ResamplerImplIIRFIR();
                up2 = 1;
                if (Fs_Hz_in > 24000) {
                    S.up2_function = "SKP_Silk_resampler_up2";
                    S.up2CB = new Up2ImplUp2();
                } else {
                    S.up2_function = "SKP_Silk_resampler_private_up2_HQ";
                    S.up2CB = new Up2ImplHQ();
                }
            }
            S.input2x = up2 | down2;
            S.invRatio_Q16 = ((Fs_Hz_in << ((up2 + 14) - down2)) / Fs_Hz_out) << 2;
            while (Macros.SKP_SMULWW(S.invRatio_Q16, Fs_Hz_out << down2) < (Fs_Hz_in << up2)) {
                S.invRatio_Q16++;
            }
            S.magic_number = 123456789;
            return 0;
        } else if ($assertionsDisabled) {
            return -1;
        } else {
            throw new AssertionError();
        }
    }

    static int SKP_Silk_resampler_clear(SKP_Silk_resampler_state_struct S) {
        Arrays.fill(S.sDown2, 0);
        Arrays.fill(S.sIIR, 0);
        Arrays.fill(S.sFIR, 0);
        Arrays.fill(S.sDownPre, 0);
        Arrays.fill(S.sUpPost, 0);
        return 0;
    }

    static int SKP_Silk_resampler(SKP_Silk_resampler_state_struct S, short[] out, int out_offset, short[] in, int in_offset, int inLen) {
        if (S.magic_number == 123456789) {
            if (S.nPreDownsamplers + S.nPostUpsamplers > 0) {
                short[] in_buf = new short[DeviceConfiguration.DEFAULT_VIDEO_HEIGHT];
                short[] out_buf = new short[DeviceConfiguration.DEFAULT_VIDEO_HEIGHT];
                while (inLen > 0) {
                    int nSamplesIn = SigProcFIX.SKP_min(inLen, S.batchSizePrePost);
                    int nSamplesOut = Macros.SKP_SMULWB(S.ratio_Q16, nSamplesIn);
                    Typedef.SKP_assert((nSamplesIn >> S.nPreDownsamplers) <= DeviceConfiguration.DEFAULT_VIDEO_HEIGHT);
                    Typedef.SKP_assert((nSamplesOut >> S.nPostUpsamplers) <= DeviceConfiguration.DEFAULT_VIDEO_HEIGHT);
                    if (S.nPreDownsamplers > 0) {
                        S.down_pre_function(S.sDownPre, in_buf, 0, in, in_offset, nSamplesIn);
                        if (S.nPostUpsamplers > 0) {
                            S.resampler_function(S, out_buf, 0, in_buf, 0, nSamplesIn >> S.nPreDownsamplers);
                            S.up_post_function(S.sUpPost, out, out_offset, out_buf, 0, nSamplesOut >> S.nPostUpsamplers);
                        } else {
                            S.resampler_function(S, out, out_offset, in_buf, 0, nSamplesIn >> S.nPreDownsamplers);
                        }
                    } else {
                        S.resampler_function(S, out_buf, 0, in, in_offset, nSamplesIn >> S.nPreDownsamplers);
                        S.up_post_function(S.sUpPost, out, out_offset, out_buf, 0, nSamplesOut >> S.nPostUpsamplers);
                    }
                    in_offset += nSamplesIn;
                    out_offset += nSamplesOut;
                    inLen -= nSamplesIn;
                }
            } else {
                S.resampler_function(S, out, out_offset, in, in_offset, inLen);
            }
            return 0;
        } else if ($assertionsDisabled) {
            return -1;
        } else {
            throw new AssertionError();
        }
    }
}
