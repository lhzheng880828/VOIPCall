package org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi;

import org.jitsi.util.Logger;

public class WASAPI {
    public static final int AUDCLNT_E_NOT_STOPPED = MAKE_HRESULT(1, FACILIY_AUDCLNT, 5);
    public static final int AUDCLNT_SHAREMODE_SHARED = 0;
    public static final int AUDCLNT_STREAMFLAGS_EVENTCALLBACK = 262144;
    public static final int AUDCLNT_STREAMFLAGS_LOOPBACK = 131072;
    public static final int AUDCLNT_STREAMFLAGS_NOPERSIST = 524288;
    public static final int CLSCTX_ALL = 23;
    public static final String CLSID_MMDeviceEnumerator = "{bcde0395-e52f-467c-8e3d-c4579291692e}";
    public static final int COINIT_MULTITHREADED = 0;
    public static final int DEVICE_STATE_ACTIVE = 1;
    private static final int FACILIY_AUDCLNT = 2185;
    public static final String IID_IAudioCaptureClient = "{c8adbd64-e71e-48a0-a4de-185c395cd317}";
    public static final String IID_IAudioClient = "{1cb9ad4c-dbfa-4c32-b178-c2f568a703b2}";
    public static final String IID_IAudioRenderClient = "{f294acfc-3146-4483-a7bf-addca7c260e2}";
    public static final String IID_IMMDeviceEnumerator = "{a95664d2-9614-4f35-a746-de8db63617e6}";
    public static final String IID_IMMEndpoint = "{1be09788-6894-4089-8586-9a2a6c265ac5}";
    public static final long PKEY_Device_FriendlyName;
    public static final int RPC_E_CHANGED_MODE = -2147417850;
    private static final int SEVERITY_ERROR = 1;
    private static final int SEVERITY_SUCCESS = 0;
    public static final int STGM_READ = 0;
    public static final int S_FALSE = 1;
    public static final int S_OK = 0;
    public static final int WAIT_ABANDONED = 128;
    public static final int WAIT_FAILED = -1;
    public static final int WAIT_OBJECT_0 = 0;
    public static final int WAIT_TIMEOUT = 258;
    public static final char WAVE_FORMAT_PCM = '\u0001';
    public static final int eAll = 2;
    public static final int eCapture = 1;
    public static final int eRender = 0;

    public static native void CloseHandle(long j) throws HResultException;

    public static native String CoCreateGuid() throws HResultException;

    public static native long CoCreateInstance(String str, long j, int i, String str2) throws HResultException;

    public static native int CoInitializeEx(long j, int i) throws HResultException;

    public static native void CoTaskMemFree(long j);

    public static native void CoUninitialize();

    public static native long CreateEvent(long j, boolean z, boolean z2, String str) throws HResultException;

    public static native int IAudioCaptureClient_GetNextPacketSize(long j) throws HResultException;

    public static native int IAudioCaptureClient_Read(long j, byte[] bArr, int i, int i2, int i3, int i4, int i5, int i6) throws HResultException;

    public static native void IAudioCaptureClient_Release(long j);

    public static native int IAudioClient_GetBufferSize(long j) throws HResultException;

    public static native int IAudioClient_GetCurrentPadding(long j) throws HResultException;

    public static native long IAudioClient_GetDefaultDevicePeriod(long j) throws HResultException;

    public static native long IAudioClient_GetMinimumDevicePeriod(long j) throws HResultException;

    public static native long IAudioClient_GetService(long j, String str) throws HResultException;

    public static native int IAudioClient_Initialize(long j, int i, int i2, long j2, long j3, long j4, String str) throws HResultException;

    public static native long IAudioClient_IsFormatSupported(long j, int i, long j2) throws HResultException;

    public static native void IAudioClient_Release(long j);

    public static native void IAudioClient_SetEventHandle(long j, long j2) throws HResultException;

    public static native int IAudioClient_Start(long j) throws HResultException;

    public static native int IAudioClient_Stop(long j) throws HResultException;

    public static native void IAudioRenderClient_Release(long j);

    public static native int IAudioRenderClient_Write(long j, byte[] bArr, int i, int i2, int i3, int i4, int i5, int i6) throws HResultException;

    public static native int IMMDeviceCollection_GetCount(long j) throws HResultException;

    public static native long IMMDeviceCollection_Item(long j, int i) throws HResultException;

    public static native void IMMDeviceCollection_Release(long j);

    public static native long IMMDeviceEnumerator_EnumAudioEndpoints(long j, int i, int i2) throws HResultException;

    public static native long IMMDeviceEnumerator_GetDevice(long j, String str) throws HResultException;

    public static native void IMMDeviceEnumerator_Release(long j);

    public static native long IMMDevice_Activate(long j, String str, int i, long j2) throws HResultException;

    public static native String IMMDevice_GetId(long j) throws HResultException;

    public static native int IMMDevice_GetState(long j) throws HResultException;

    public static native long IMMDevice_OpenPropertyStore(long j, int i) throws HResultException;

    public static native long IMMDevice_QueryInterface(long j, String str) throws HResultException;

    public static native void IMMDevice_Release(long j);

    public static native int IMMEndpoint_GetDataFlow(long j) throws HResultException;

    public static native void IMMEndpoint_Release(long j);

    public static native String IPropertyStore_GetString(long j, long j2) throws HResultException;

    public static native void IPropertyStore_Release(long j);

    public static native long PSPropertyKeyFromString(String str) throws HResultException;

    public static native void ResetEvent(long j) throws HResultException;

    public static native long WAVEFORMATEX_alloc();

    public static native void WAVEFORMATEX_fill(long j, char c, char c2, int i, int i2, char c3, char c4, char c5);

    public static native char WAVEFORMATEX_getCbSize(long j);

    public static native int WAVEFORMATEX_getNAvgBytesPerSec(long j);

    public static native char WAVEFORMATEX_getNBlockAlign(long j);

    public static native char WAVEFORMATEX_getNChannels(long j);

    public static native int WAVEFORMATEX_getNSamplesPerSec(long j);

    public static native char WAVEFORMATEX_getWBitsPerSample(long j);

    public static native char WAVEFORMATEX_getWFormatTag(long j);

    public static native void WAVEFORMATEX_setCbSize(long j, char c);

    public static native void WAVEFORMATEX_setNAvgBytesPerSec(long j, int i);

    public static native void WAVEFORMATEX_setNBlockAlign(long j, char c);

    public static native void WAVEFORMATEX_setNChannels(long j, char c);

    public static native void WAVEFORMATEX_setNSamplesPerSec(long j, int i);

    public static native void WAVEFORMATEX_setWBitsPerSample(long j, char c);

    public static native void WAVEFORMATEX_setWFormatTag(long j, char c);

    public static native int WAVEFORMATEX_sizeof();

    public static native int WaitForSingleObject(long j, long j2) throws HResultException;

    static {
        System.loadLibrary("jnwasapi");
        String pszString = "{a45c254e-df1c-4efd-8020-67d146a850e0} 14";
        try {
            PKEY_Device_FriendlyName = PSPropertyKeyFromString(pszString);
            if (PKEY_Device_FriendlyName == 0) {
                throw new IllegalStateException("PKEY_Device_FriendlyName");
            }
        } catch (HResultException hre) {
            Logger.getLogger(WASAPI.class).error("PSPropertyKeyFromString(" + pszString + ")", hre);
            throw new RuntimeException(hre);
        }
    }

    public static boolean FAILED(int hresult) {
        return hresult < 0;
    }

    private static int MAKE_HRESULT(int sev, int fac, int code) {
        return (((sev & 1) << 31) | ((fac & 32767) << 16)) | (65535 & code);
    }

    public static boolean SUCCEEDED(int hresult) {
        return hresult >= 0;
    }

    private WASAPI() {
    }
}
