package org.jitsi.impl.neomedia.codec.audio.silk;

public class EncAPI {
    static final /* synthetic */ boolean $assertionsDisabled = (!EncAPI.class.desiredAssertionStatus());
    static int frame_cnt = 0;

    static int SKP_Silk_SDK_QueryEncoder(Object encState, SKP_SILK_SDK_EncControlStruct encStatus) {
        SKP_Silk_encoder_state_FLP psEnc = (SKP_Silk_encoder_state_FLP) encState;
        encStatus.API_sampleRate = psEnc.sCmn.API_fs_Hz;
        encStatus.maxInternalSampleRate = Macros.SKP_SMULBB(psEnc.sCmn.maxInternal_fs_kHz, 1000);
        encStatus.packetSize = (psEnc.sCmn.API_fs_Hz * psEnc.sCmn.PacketSize_ms) / 1000;
        encStatus.bitRate = psEnc.sCmn.TargetRate_bps;
        encStatus.packetLossPercentage = psEnc.sCmn.PacketLoss_perc;
        encStatus.complexity = psEnc.sCmn.Complexity;
        encStatus.useInBandFEC = psEnc.sCmn.useInBandFEC;
        encStatus.useDTX = psEnc.sCmn.useDTX;
        return 0;
    }

    static int SKP_Silk_SDK_InitEncoder(Object encState, SKP_SILK_SDK_EncControlStruct encStatus) {
        int ret = 0 + InitEncoderFLP.SKP_Silk_init_encoder_FLP((SKP_Silk_encoder_state_FLP) encState);
        if (ret == 0 || $assertionsDisabled) {
            ret += SKP_Silk_SDK_QueryEncoder(encState, encStatus);
            if (ret == 0 || $assertionsDisabled) {
                return ret;
            }
            throw new AssertionError();
        }
        throw new AssertionError();
    }

    static int SKP_Silk_SDK_Encode(Object encState, SKP_SILK_SDK_EncControlStruct encControl, short[] samplesIn, int samplesIn_offset, int nSamplesIn, byte[] outData, int outData_offset, short[] nBytesOut) {
        SKP_Silk_encoder_state_FLP psEnc = (SKP_Silk_encoder_state_FLP) encState;
        int i;
        if (!$assertionsDisabled && encControl == null) {
            throw new AssertionError();
        } else if ((encControl.API_sampleRate == 8000 || encControl.API_sampleRate == 12000 || encControl.API_sampleRate == 16000 || encControl.API_sampleRate == 24000 || encControl.API_sampleRate == 32000 || encControl.API_sampleRate == 44100 || encControl.API_sampleRate == 48000) && (encControl.maxInternalSampleRate == 8000 || encControl.maxInternalSampleRate == 12000 || encControl.maxInternalSampleRate == 16000 || encControl.maxInternalSampleRate == 24000)) {
            int API_fs_Hz = encControl.API_sampleRate;
            int max_internal_fs_kHz = encControl.maxInternalSampleRate / 1000;
            int PacketSize_ms = (encControl.packetSize * 1000) / API_fs_Hz;
            int TargetRate_bps = encControl.bitRate;
            int PacketLoss_perc = encControl.packetLossPercentage;
            int UseInBandFEC = encControl.useInBandFEC;
            int Complexity = encControl.complexity;
            int UseDTX = encControl.useDTX;
            psEnc.sCmn.API_fs_Hz = API_fs_Hz;
            psEnc.sCmn.maxInternal_fs_kHz = max_internal_fs_kHz;
            psEnc.sCmn.useInBandFEC = UseInBandFEC;
            int input_ms = (nSamplesIn * 1000) / API_fs_Hz;
            if (input_ms % 10 != 0 || nSamplesIn < 0) {
                if ($assertionsDisabled) {
                    i = -1;
                    return -1;
                }
                throw new AssertionError();
            } else if (nSamplesIn <= (psEnc.sCmn.PacketSize_ms * API_fs_Hz) / 1000) {
                int ret = ControlCodecFLP.SKP_Silk_control_encoder_FLP(psEnc, API_fs_Hz, max_internal_fs_kHz, PacketSize_ms, TargetRate_bps, PacketLoss_perc, UseInBandFEC, UseDTX, input_ms, Complexity);
                if (ret == 0) {
                    if (Math.min(API_fs_Hz, max_internal_fs_kHz * 1000) == 24000 && psEnc.sCmn.sSWBdetect.SWB_detected == 0 && psEnc.sCmn.sSWBdetect.WB_detected == 0) {
                        DetectSWBInput.SKP_Silk_detect_SWB_input(psEnc.sCmn.sSWBdetect, samplesIn, samplesIn_offset, nSamplesIn);
                    }
                    short MaxBytesOut = (short) 0;
                    while (true) {
                        int nSamplesFromInput;
                        int nSamplesToBuffer = psEnc.sCmn.frame_length - psEnc.sCmn.inputBufIx;
                        if (API_fs_Hz == Macros.SKP_SMULBB(1000, psEnc.sCmn.fs_kHz)) {
                            nSamplesToBuffer = Math.min(nSamplesToBuffer, nSamplesIn);
                            nSamplesFromInput = nSamplesToBuffer;
                            System.arraycopy(samplesIn, samplesIn_offset, psEnc.sCmn.inputBuf, psEnc.sCmn.inputBufIx, nSamplesFromInput);
                        } else {
                            nSamplesToBuffer = Math.min(nSamplesToBuffer, ((psEnc.sCmn.fs_kHz * nSamplesIn) * 1000) / API_fs_Hz);
                            nSamplesFromInput = (nSamplesToBuffer * API_fs_Hz) / (psEnc.sCmn.fs_kHz * 1000);
                            ret += Resampler.SKP_Silk_resampler(psEnc.sCmn.resampler_state, psEnc.sCmn.inputBuf, psEnc.sCmn.inputBufIx, samplesIn, samplesIn_offset, nSamplesFromInput);
                        }
                        samplesIn_offset += nSamplesFromInput;
                        nSamplesIn -= nSamplesFromInput;
                        SKP_Silk_encoder_state sKP_Silk_encoder_state = psEnc.sCmn;
                        sKP_Silk_encoder_state.inputBufIx += nSamplesToBuffer;
                        if (psEnc.sCmn.inputBufIx >= psEnc.sCmn.frame_length) {
                            if (MaxBytesOut == (short) 0) {
                                short[] MaxBytesOut_ptr = new short[]{nBytesOut[0]};
                                ret = EncodeFrameFLP.SKP_Silk_encode_frame_FLP(psEnc, outData, outData_offset, MaxBytesOut_ptr, psEnc.sCmn.inputBuf, 0);
                                if (ret == 0 || $assertionsDisabled) {
                                    MaxBytesOut = MaxBytesOut_ptr[0];
                                } else {
                                    throw new AssertionError();
                                }
                            }
                            ret = EncodeFrameFLP.SKP_Silk_encode_frame_FLP(psEnc, outData, outData_offset, nBytesOut, psEnc.sCmn.inputBuf, 0);
                            if (ret != 0 && !$assertionsDisabled) {
                                throw new AssertionError();
                            } else if (!($assertionsDisabled || nBytesOut[0] == (short) 0)) {
                                throw new AssertionError();
                            }
                            psEnc.sCmn.inputBufIx = 0;
                        } else {
                            nBytesOut[0] = MaxBytesOut;
                            if (!(psEnc.sCmn.useDTX == 0 || psEnc.sCmn.inDTX == 0)) {
                                nBytesOut[0] = (short) 0;
                            }
                            i = ret;
                            return ret;
                        }
                    }
                } else if ($assertionsDisabled) {
                    i = ret;
                    return ret;
                } else {
                    throw new AssertionError();
                }
            } else if ($assertionsDisabled) {
                i = -1;
                return -1;
            } else {
                throw new AssertionError();
            }
        } else if ($assertionsDisabled) {
            i = -2;
            return -2;
        } else {
            throw new AssertionError();
        }
    }
}
