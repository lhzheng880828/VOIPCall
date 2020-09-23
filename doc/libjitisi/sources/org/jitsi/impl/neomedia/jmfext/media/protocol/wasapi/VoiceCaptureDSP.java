package org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi;

import org.jitsi.util.Logger;

public class VoiceCaptureDSP {
    public static final String CLSID_CWMAudioAEC = "{745057c7-f353-4f2d-a7ee-58434477730e}";
    public static final int DMO_E_NOTACCEPTING = -2147220988;
    public static final int DMO_INPUT_STATUSF_ACCEPT_DATA = 1;
    public static final int DMO_OUTPUT_DATA_BUFFERF_INCOMPLETE = 16777216;
    public static final int DMO_SET_TYPEF_TEST_ONLY = 1;
    public static final String FORMAT_None = "{0f6417d6-c318-11d0-a43f-00a0c9223196}";
    public static final String FORMAT_WaveFormatEx = "{05589f81-c356-11ce-bf01-00aa0055595a}";
    public static final String IID_IMediaObject = "{d8ad0f58-5494-4102-97c5-ec798e59bcf4}";
    public static final String IID_IPropertyStore = "{886d8eeb-8cf2-4446-8d02-cdba1dbdcf99}";
    public static final String MEDIASUBTYPE_PCM = "{00000001-0000-0010-8000-00AA00389B71}";
    public static final String MEDIATYPE_Audio = "{73647561-0000-0010-8000-00aa00389b71}";
    public static final long MFPKEY_WMAAECMA_DEVICE_INDEXES;
    public static final long MFPKEY_WMAAECMA_DMO_SOURCE_MODE;
    public static final long MFPKEY_WMAAECMA_FEATR_AES;
    public static final long MFPKEY_WMAAECMA_FEATR_AGC;
    public static final long MFPKEY_WMAAECMA_FEATR_CENTER_CLIP;
    public static final long MFPKEY_WMAAECMA_FEATR_ECHO_LENGTH;
    public static final long MFPKEY_WMAAECMA_FEATR_NOISE_FILL;
    public static final long MFPKEY_WMAAECMA_FEATR_NS;
    public static final long MFPKEY_WMAAECMA_FEATURE_MODE;
    public static final long MFPKEY_WMAAECMA_MIC_GAIN_BOUNDER;
    public static final long MFPKEY_WMAAECMA_SYSTEM_MODE;
    public static final int SINGLE_CHANNEL_AEC = 0;
    private static final Logger logger = Logger.getLogger(VoiceCaptureDSP.class);

    public static native int DMO_MEDIA_TYPE_fill(long j, String str, String str2, boolean z, boolean z2, int i, String str3, long j2, int i2, long j3) throws HResultException;

    public static native void DMO_MEDIA_TYPE_setCbFormat(long j, int i);

    public static native int DMO_MEDIA_TYPE_setFormattype(long j, String str) throws HResultException;

    public static native void DMO_MEDIA_TYPE_setLSampleSize(long j, int i);

    public static native void DMO_MEDIA_TYPE_setPbFormat(long j, long j2);

    public static native long DMO_OUTPUT_DATA_BUFFER_alloc(long j, int i, long j2, long j3);

    public static native int DMO_OUTPUT_DATA_BUFFER_getDwStatus(long j);

    public static native void DMO_OUTPUT_DATA_BUFFER_setDwStatus(long j, int i);

    public static native int IMediaBuffer_AddRef(long j);

    public static native long IMediaBuffer_GetBuffer(long j) throws HResultException;

    public static native int IMediaBuffer_GetLength(long j) throws HResultException;

    public static native int IMediaBuffer_GetMaxLength(long j) throws HResultException;

    public static native int IMediaBuffer_Release(long j);

    public static native void IMediaBuffer_SetLength(long j, int i) throws HResultException;

    public static native int IMediaObject_Flush(long j) throws HResultException;

    public static native int IMediaObject_GetInputStatus(long j, int i) throws HResultException;

    public static native int IMediaObject_ProcessInput(long j, int i, long j2, int i2, long j3, long j4) throws HResultException;

    public static native int IMediaObject_ProcessOutput(long j, int i, int i2, long j2) throws HResultException;

    public static native long IMediaObject_QueryInterface(long j, String str) throws HResultException;

    public static native void IMediaObject_Release(long j);

    public static native int IMediaObject_SetInputType(long j, int i, long j2, int i2) throws HResultException;

    public static native int IMediaObject_SetOutputType(long j, int i, long j2, int i2) throws HResultException;

    public static native int IPropertyStore_SetValue(long j, long j2, int i) throws HResultException;

    public static native int IPropertyStore_SetValue(long j, long j2, boolean z) throws HResultException;

    public static native long MediaBuffer_alloc(int i);

    public static native int MediaBuffer_pop(long j, byte[] bArr, int i, int i2) throws HResultException;

    public static native int MediaBuffer_push(long j, byte[] bArr, int i, int i2) throws HResultException;

    public static native long MoCreateMediaType(int i) throws HResultException;

    public static native void MoDeleteMediaType(long j) throws HResultException;

    public static native void MoFreeMediaType(long j) throws HResultException;

    public static native void MoInitMediaType(long j, int i) throws HResultException;

    static {
        String fmtid = "{6f52c567-0360-4bd2-9617-ccbf1421c939} ";
        String pszString = null;
        try {
            pszString = fmtid + "4";
            long _MFPKEY_WMAAECMA_DEVICE_INDEXES = WASAPI.PSPropertyKeyFromString(pszString);
            if (_MFPKEY_WMAAECMA_DEVICE_INDEXES == 0) {
                throw new IllegalStateException("MFPKEY_WMAAECMA_DEVICE_INDEXES");
            }
            long _MFPKEY_WMAAECMA_DMO_SOURCE_MODE = WASAPI.PSPropertyKeyFromString(fmtid + "3");
            if (_MFPKEY_WMAAECMA_DMO_SOURCE_MODE == 0) {
                throw new IllegalStateException("MFPKEY_WMAAECMA_DMO_SOURCE_MODE");
            }
            long _MFPKEY_WMAAECMA_SYSTEM_MODE = WASAPI.PSPropertyKeyFromString(fmtid + "2");
            if (_MFPKEY_WMAAECMA_SYSTEM_MODE == 0) {
                throw new IllegalStateException("MFPKEY_WMAAECMA_SYSTEM_MODE");
            }
            if (false) {
                if (_MFPKEY_WMAAECMA_DMO_SOURCE_MODE != 0) {
                    WASAPI.CoTaskMemFree(_MFPKEY_WMAAECMA_DMO_SOURCE_MODE);
                    _MFPKEY_WMAAECMA_DMO_SOURCE_MODE = 0;
                }
                if (_MFPKEY_WMAAECMA_SYSTEM_MODE != 0) {
                    WASAPI.CoTaskMemFree(_MFPKEY_WMAAECMA_SYSTEM_MODE);
                    _MFPKEY_WMAAECMA_SYSTEM_MODE = 0;
                }
            }
            MFPKEY_WMAAECMA_DEVICE_INDEXES = _MFPKEY_WMAAECMA_DEVICE_INDEXES;
            MFPKEY_WMAAECMA_DMO_SOURCE_MODE = _MFPKEY_WMAAECMA_DMO_SOURCE_MODE;
            MFPKEY_WMAAECMA_SYSTEM_MODE = _MFPKEY_WMAAECMA_SYSTEM_MODE;
            MFPKEY_WMAAECMA_FEATR_AES = maybePSPropertyKeyFromString(fmtid + "10");
            MFPKEY_WMAAECMA_FEATR_AGC = maybePSPropertyKeyFromString(fmtid + "9");
            MFPKEY_WMAAECMA_FEATR_CENTER_CLIP = maybePSPropertyKeyFromString(fmtid + "12");
            MFPKEY_WMAAECMA_FEATR_ECHO_LENGTH = maybePSPropertyKeyFromString(fmtid + "7");
            MFPKEY_WMAAECMA_FEATR_NOISE_FILL = maybePSPropertyKeyFromString(fmtid + "13");
            MFPKEY_WMAAECMA_FEATR_NS = maybePSPropertyKeyFromString(fmtid + "8");
            MFPKEY_WMAAECMA_FEATURE_MODE = maybePSPropertyKeyFromString(fmtid + "5");
            MFPKEY_WMAAECMA_MIC_GAIN_BOUNDER = maybePSPropertyKeyFromString(fmtid + "21");
        } catch (HResultException hre) {
            Logger.getLogger(VoiceCaptureDSP.class).error("PSPropertyKeyFromString(" + pszString + ")", hre);
            throw new RuntimeException(hre);
        } catch (Throwable th) {
            if (true) {
                if (0 != 0) {
                    WASAPI.CoTaskMemFree(0);
                }
                if (0 != 0) {
                    WASAPI.CoTaskMemFree(0);
                }
            }
        }
    }

    private static long maybePSPropertyKeyFromString(String pszString) {
        try {
            return WASAPI.PSPropertyKeyFromString(pszString);
        } catch (HResultException hre) {
            logger.error("PSPropertyKeyFromString " + pszString, hre);
            return 0;
        }
    }

    private VoiceCaptureDSP() {
    }
}
