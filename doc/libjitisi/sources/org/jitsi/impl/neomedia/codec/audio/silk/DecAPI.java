package org.jitsi.impl.neomedia.codec.audio.silk;

import java.util.Arrays;
import org.jitsi.impl.neomedia.device.DeviceConfiguration;

public class DecAPI {
    static int SKP_Silk_SDK_InitDecoder(Object decState) {
        return CreateInitDestroy.SKP_Silk_init_decoder((SKP_Silk_decoder_state) decState);
    }

    static int SKP_Silk_SDK_Decode(Object decState, SKP_SILK_SDK_DecControlStruct decControl, int lostFlag, byte[] inData, int inData_offset, int nBytesIn, short[] samplesOut, int samplesOut_offset, short[] nSamplesOut) {
        int ret = 0;
        SKP_Silk_decoder_state psDec = (SKP_Silk_decoder_state) decState;
        if (psDec.moreInternalDecoderFrames == 0) {
            psDec.nFramesDecoded = 0;
        }
        if (psDec.moreInternalDecoderFrames == 0 && lostFlag == 0 && nBytesIn > 1024) {
            lostFlag = 1;
            ret = -11;
        }
        int prev_fs_kHz = psDec.fs_kHz;
        int[] used_bytes_ptr = new int[1];
        ret += DecodeFrame.SKP_Silk_decode_frame(psDec, samplesOut, samplesOut_offset, nSamplesOut, inData, inData_offset, nBytesIn, lostFlag, used_bytes_ptr);
        if (used_bytes_ptr[0] != 0) {
            if (psDec.nBytesLeft <= 0 || psDec.FrameTermination != 1 || psDec.nFramesDecoded >= 5) {
                psDec.moreInternalDecoderFrames = 0;
                psDec.nFramesInPacket = psDec.nFramesDecoded;
                if (psDec.vadFlag == 1) {
                    if (psDec.FrameTermination == 0) {
                        psDec.no_FEC_counter++;
                        if (psDec.no_FEC_counter > 10) {
                            psDec.inband_FEC_offset = 0;
                        }
                    } else if (psDec.FrameTermination == 2) {
                        psDec.inband_FEC_offset = 1;
                        psDec.no_FEC_counter = 0;
                    } else if (psDec.FrameTermination == 3) {
                        psDec.inband_FEC_offset = 2;
                        psDec.no_FEC_counter = 0;
                    }
                }
            } else {
                psDec.moreInternalDecoderFrames = 1;
            }
        }
        int ret2;
        if (48000 < decControl.API_sampleRate || 8000 > decControl.API_sampleRate) {
            ret2 = -10;
            return -10;
        }
        if (psDec.fs_kHz * 1000 != decControl.API_sampleRate) {
            short[] samplesOut_tmp = new short[960];
            Typedef.SKP_assert(psDec.fs_kHz <= 48);
            System.arraycopy(samplesOut, samplesOut_offset + 0, samplesOut_tmp, 0, nSamplesOut[0]);
            if (!(prev_fs_kHz == psDec.fs_kHz && psDec.prev_API_sampleRate == decControl.API_sampleRate)) {
                ret = Resampler.SKP_Silk_resampler_init(psDec.resampler_state, psDec.fs_kHz * 1000, decControl.API_sampleRate);
            }
            ret += Resampler.SKP_Silk_resampler(psDec.resampler_state, samplesOut, samplesOut_offset, samplesOut_tmp, 0, nSamplesOut[0]);
            nSamplesOut[0] = (short) ((nSamplesOut[0] * decControl.API_sampleRate) / (psDec.fs_kHz * 1000));
        }
        psDec.prev_API_sampleRate = decControl.API_sampleRate;
        decControl.frameSize = psDec.frame_length;
        decControl.framesPerPacket = psDec.nFramesInPacket;
        decControl.inBandFECOffset = psDec.inband_FEC_offset;
        decControl.moreInternalDecoderFrames = psDec.moreInternalDecoderFrames;
        ret2 = ret;
        return ret;
    }

    static void SKP_Silk_SDK_search_for_LBRR(byte[] inData, int inData_offset, short nBytesIn, int lost_offset, byte[] LBRRData, int LBRRData_offset, short[] nLBRRBytes) {
        SKP_Silk_decoder_state sDec = new SKP_Silk_decoder_state();
        SKP_Silk_decoder_control sDecCtrl = new SKP_Silk_decoder_control();
        int[] TempQ = new int[DeviceConfiguration.DEFAULT_VIDEO_HEIGHT];
        if (lost_offset < 1 || lost_offset > 2) {
            nLBRRBytes[0] = (short) 0;
            return;
        }
        sDec.nFramesDecoded = 0;
        sDec.fs_kHz = 0;
        Arrays.fill(sDec.prevNLSF_Q15, 0, 16, 0);
        for (int i = 0; i < 16; i++) {
            sDec.prevNLSF_Q15[i] = 0;
        }
        RangeCoder.SKP_Silk_range_dec_init(sDec.sRC, inData, inData_offset, nBytesIn);
        while (true) {
            DecodeParameters.SKP_Silk_decode_parameters(sDec, sDecCtrl, TempQ, 0);
            if (sDec.sRC.error != 0) {
                nLBRRBytes[0] = (short) 0;
                return;
            } else if (((sDec.FrameTermination - 1) & lost_offset) != 0 && sDec.FrameTermination > 0 && sDec.nBytesLeft >= 0) {
                nLBRRBytes[0] = (short) sDec.nBytesLeft;
                System.arraycopy(inData, (inData_offset + nBytesIn) - sDec.nBytesLeft, LBRRData, LBRRData_offset + 0, sDec.nBytesLeft);
                return;
            } else if (sDec.nBytesLeft <= 0 || sDec.FrameTermination != 1) {
                nLBRRBytes[0] = (short) 0;
            } else {
                sDec.nFramesDecoded++;
            }
        }
        nLBRRBytes[0] = (short) 0;
    }

    static void SKP_Silk_SDK_get_TOC(byte[] inData, short nBytesIn, SKP_Silk_TOC_struct Silk_TOC) {
        SKP_Silk_decoder_state sDec = new SKP_Silk_decoder_state();
        SKP_Silk_decoder_control sDecCtrl = new SKP_Silk_decoder_control();
        int[] TempQ = new int[DeviceConfiguration.DEFAULT_VIDEO_HEIGHT];
        sDec.nFramesDecoded = 0;
        sDec.fs_kHz = 0;
        RangeCoder.SKP_Silk_range_dec_init(sDec.sRC, inData, 0, nBytesIn);
        Silk_TOC.corrupt = 0;
        while (true) {
            DecodeParameters.SKP_Silk_decode_parameters(sDec, sDecCtrl, TempQ, 0);
            Silk_TOC.vadFlags[sDec.nFramesDecoded] = sDec.vadFlag;
            Silk_TOC.sigtypeFlags[sDec.nFramesDecoded] = sDecCtrl.sigtype;
            if (sDec.sRC.error == 0) {
                if (sDec.nBytesLeft <= 0 || sDec.FrameTermination != 1) {
                    break;
                }
                sDec.nFramesDecoded++;
            } else {
                Silk_TOC.corrupt = 1;
                break;
            }
        }
        if (Silk_TOC.corrupt != 0 || sDec.FrameTermination == 1 || sDec.nFramesInPacket > 5) {
            int i;
            Silk_TOC.corrupt = 0;
            Silk_TOC.framesInPacket = 0;
            Silk_TOC.fs_kHz = 0;
            Silk_TOC.inbandLBRR = 0;
            for (i = 0; i < Silk_TOC.vadFlags.length; i++) {
                Silk_TOC.vadFlags[i] = 0;
            }
            for (i = 0; i < Silk_TOC.sigtypeFlags.length; i++) {
                Silk_TOC.sigtypeFlags[i] = 0;
            }
            Silk_TOC.corrupt = 1;
            return;
        }
        Silk_TOC.framesInPacket = sDec.nFramesDecoded + 1;
        Silk_TOC.fs_kHz = sDec.fs_kHz;
        if (sDec.FrameTermination == 0) {
            Silk_TOC.inbandLBRR = sDec.FrameTermination;
        } else {
            Silk_TOC.inbandLBRR = sDec.FrameTermination - 1;
        }
    }

    static String SKP_Silk_SDK_get_version() {
        return "1.0.6";
    }
}
