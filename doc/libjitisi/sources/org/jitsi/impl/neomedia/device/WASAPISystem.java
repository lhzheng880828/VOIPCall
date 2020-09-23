package org.jitsi.impl.neomedia.device;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.media.Codec;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.media.PlugInManager;
import javax.media.format.AudioFormat;
import org.jitsi.impl.neomedia.device.AudioSystem.DataFlow;
import org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.HResultException;
import org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.IMMNotificationClient;
import org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.MMNotificationClient;
import org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.NativelySupportedAudioFormat;
import org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.VoiceCaptureDSP;
import org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.WASAPI;
import org.jitsi.impl.neomedia.jmfext.media.renderer.audio.AbstractAudioRenderer;
import org.jitsi.impl.neomedia.jmfext.media.renderer.audio.WASAPIRenderer;
import org.jitsi.service.neomedia.codec.Constants;
import org.jitsi.util.Logger;

public class WASAPISystem extends AudioSystem {
    public static final long DEFAULT_BUFFER_DURATION = 20;
    public static final long DEFAULT_DEVICE_PERIOD = 10;
    private static final String LOCATOR_PROTOCOL = "wasapi";
    private static String audioSessionGuid;
    private static final Logger logger = Logger.getLogger(WASAPISystem.class);
    private long aecIMediaObject;
    private List<AudioFormat> aecSupportedFormats;
    private long iMMDeviceEnumerator;
    private IMMNotificationClient pNotify;
    private long waveformatex;

    public static int CoInitializeEx() throws HResultException {
        try {
            return WASAPI.CoInitializeEx(0, 0);
        } catch (HResultException hre) {
            int hr = hre.getHResult();
            switch (hr) {
                case WASAPI.RPC_E_CHANGED_MODE /*-2147417850*/:
                    return 1;
                case 0:
                case 1:
                    return hr;
                default:
                    throw hre;
            }
        }
    }

    public static AudioFormat[] getFormatsToInitializeIAudioClient(AudioFormat format) {
        int channels;
        switch (format.getChannels()) {
            case 1:
                channels = 2;
                break;
            case 2:
                channels = 1;
                break;
            default:
                return new AudioFormat[]{format};
        }
        return new AudioFormat[]{format, new AudioFormat(format.getEncoding(), format.getSampleRate(), format.getSampleSizeInBits(), channels, 0, 1, -1, -1.0d, format.getDataType())};
    }

    public static int getSampleSizeInBytes(AudioFormat format) {
        int sampleSizeInBits = format.getSampleSizeInBits();
        switch (sampleSizeInBits) {
            case 8:
                return 1;
            case 16:
                return 2;
            default:
                return sampleSizeInBits / 8;
        }
    }

    public static void WAVEFORMATEX_fill(long waveformatex, AudioFormat audioFormat) {
        if (AudioFormat.LINEAR.equals(audioFormat.getEncoding())) {
            int channels = audioFormat.getChannels();
            if (channels == -1) {
                throw new IllegalArgumentException("audioFormat.channels");
            }
            int sampleRate = (int) audioFormat.getSampleRate();
            if (sampleRate == -1) {
                throw new IllegalArgumentException("audioFormat.sampleRate");
            }
            int sampleSizeInBits = audioFormat.getSampleSizeInBits();
            if (sampleSizeInBits == -1) {
                throw new IllegalArgumentException("audioFormat.sampleSizeInBits");
            }
            char nBlockAlign = (char) ((channels * sampleSizeInBits) / 8);
            WASAPI.WAVEFORMATEX_fill(waveformatex, 1, (char) channels, sampleRate, sampleRate * nBlockAlign, nBlockAlign, (char) sampleSizeInBits, 0);
            return;
        }
        throw new IllegalArgumentException("audioFormat.encoding");
    }

    WASAPISystem() throws Exception {
        super("wasapi", 31);
    }

    private void configureSupportedFormats(int dataFlow, List<AudioFormat> formats) {
        switch (dataFlow) {
            case 0:
                int count = formats.size();
                for (int i = 0; i < count; i++) {
                    Format outFormat = (AudioFormat) formats.get(i);
                    AudioFormat inFormat = new AudioFormat(AudioFormat.LINEAR, -1.0d, -1, outFormat.getChannels(), AbstractAudioRenderer.NATIVE_AUDIO_FORMAT_ENDIAN, 1, -1, -1.0d, Format.byteArray);
                    List<String> classNames = PlugInManager.getPlugInList(inFormat, outFormat, 2);
                    if (!(classNames == null || classNames.isEmpty())) {
                        for (String className : classNames) {
                            try {
                                Codec codec = (Codec) Class.forName(className).newInstance();
                                Format[] inFormats = codec.getSupportedInputFormats();
                                if (inFormats != null) {
                                    for (Format aInFormat : inFormats) {
                                        if ((aInFormat instanceof AudioFormat) && inFormat.matches(aInFormat)) {
                                            Format[] outFormats = codec.getSupportedOutputFormats(aInFormat);
                                            boolean add = false;
                                            if (outFormats != null) {
                                                Format[] arr$ = outFormats;
                                                int len$ = arr$.length;
                                                int i$ = 0;
                                                while (i$ < len$) {
                                                    if (outFormat.matches(arr$[i$])) {
                                                        add = true;
                                                    } else {
                                                        i$++;
                                                    }
                                                }
                                            }
                                            if (add && !formats.contains(aInFormat)) {
                                                formats.add((AudioFormat) aInFormat);
                                            }
                                        }
                                    }
                                }
                            } catch (Throwable t) {
                                if (t instanceof ThreadDeath) {
                                    ThreadDeath t2 = (ThreadDeath) t;
                                }
                            }
                        }
                        continue;
                    }
                }
                return;
            case 1:
                List<AudioFormat> aecSupportedFormats = getAECSupportedFormats();
                if (!aecSupportedFormats.isEmpty()) {
                    for (AudioFormat format : aecSupportedFormats) {
                        if (!formats.contains(format)) {
                            formats.add(format);
                        }
                    }
                    return;
                }
                return;
            default:
                throw new IllegalArgumentException("dataFlow");
        }
    }

    /* access modifiers changed from: protected */
    public void doInitialize() throws Exception {
        List<CaptureDeviceInfo2> captureDevices;
        List<CaptureDeviceInfo2> playbackDevices;
        synchronized (this) {
            CoInitializeEx();
            if (this.iMMDeviceEnumerator == 0) {
                this.iMMDeviceEnumerator = WASAPI.CoCreateInstance(WASAPI.CLSID_MMDeviceEnumerator, 0, 23, WASAPI.IID_IMMDeviceEnumerator);
                if (this.iMMDeviceEnumerator == 0) {
                    throw new IllegalStateException("iMMDeviceEnumerator");
                }
                MMNotificationClient.RegisterEndpointNotificationCallback(this.pNotify);
            }
            long iMMDeviceCollection = WASAPI.IMMDeviceEnumerator_EnumAudioEndpoints(this.iMMDeviceEnumerator, 2, 1);
            if (iMMDeviceCollection == 0) {
                throw new RuntimeException("IMMDeviceEnumerator_EnumAudioEndpoints");
            }
            try {
                int count = WASAPI.IMMDeviceCollection_GetCount(iMMDeviceCollection);
                captureDevices = new ArrayList(count);
                playbackDevices = new ArrayList(count);
                if (count > 0) {
                    maybeInitializeAEC();
                    for (int i = 0; i < count; i++) {
                        long iMMDevice = WASAPI.IMMDeviceCollection_Item(iMMDeviceCollection, i);
                        if (iMMDevice == 0) {
                            throw new RuntimeException("IMMDeviceCollection_Item");
                        }
                        try {
                            doInitializeIMMDevice(iMMDevice, captureDevices, playbackDevices);
                        } catch (Throwable t) {
                            if (t instanceof ThreadDeath) {
                                ThreadDeath t2 = (ThreadDeath) t;
                            } else {
                                logger.error("Failed to doInitialize for IMMDevice at index " + i, t);
                            }
                        } finally {
                            WASAPI.IMMDevice_Release(iMMDevice);
                        }
                    }
                    maybeUninitializeAEC();
                }
                WASAPI.IMMDeviceCollection_Release(iMMDeviceCollection);
            } catch (Throwable th) {
                WASAPI.IMMDeviceCollection_Release(iMMDeviceCollection);
            }
        }
        setCaptureDevices(captureDevices);
        setPlaybackDevices(playbackDevices);
    }

    private void doInitializeIMMDevice(long iMMDevice, List<CaptureDeviceInfo2> captureDevices, List<CaptureDeviceInfo2> playbackDevices) throws HResultException {
        String id = WASAPI.IMMDevice_GetId(iMMDevice);
        if (id == null) {
            throw new RuntimeException("IMMDevice_GetId");
        }
        long iAudioClient = WASAPI.IMMDevice_Activate(iMMDevice, WASAPI.IID_IAudioClient, 23, 0);
        if (iAudioClient == 0) {
            throw new RuntimeException("IMMDevice_Activate");
        }
        try {
            List<AudioFormat> formats = getIAudioClientSupportedFormats(iAudioClient);
            if (formats != null && !formats.isEmpty()) {
                List<CaptureDeviceInfo2> devices;
                String name = null;
                try {
                    name = getIMMDeviceFriendlyName(iMMDevice);
                } catch (Throwable t) {
                    if (t instanceof ThreadDeath) {
                        ThreadDeath t2 = (ThreadDeath) t;
                    }
                    logger.warn("Failed to retrieve the PKEY_Device_FriendlyName of IMMDevice " + id, t);
                }
                if (name == null || name.length() == 0) {
                    name = id;
                }
                int dataFlow = getIMMDeviceDataFlow(iMMDevice);
                switch (dataFlow) {
                    case 0:
                        devices = playbackDevices;
                        break;
                    case 1:
                        devices = captureDevices;
                        break;
                    default:
                        devices = null;
                        logger.error("Failed to retrieve dataFlow from IMMEndpoint " + id);
                        break;
                }
                if (devices != null) {
                    configureSupportedFormats(dataFlow, formats);
                    if (!formats.isEmpty()) {
                        devices.add(new CaptureDeviceInfo2(name, new MediaLocator("wasapi:" + id), (Format[]) formats.toArray(new Format[formats.size()]), id, null, null));
                    }
                }
            }
        } finally {
            WASAPI.IAudioClient_Release(iAudioClient);
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            synchronized (this) {
                if (this.iMMDeviceEnumerator != 0) {
                    WASAPI.IMMDeviceEnumerator_Release(this.iMMDeviceEnumerator);
                    this.iMMDeviceEnumerator = 0;
                }
            }
        } finally {
            super.finalize();
        }
    }

    public List<AudioFormat> getAECSupportedFormats() {
        List<AudioFormat> aecSupportedFormats = this.aecSupportedFormats;
        if (aecSupportedFormats == null) {
            return Collections.emptyList();
        }
        return aecSupportedFormats;
    }

    private List<AudioFormat> getIAudioClientSupportedFormats(long iAudioClient) throws HResultException {
        List<AudioFormat> supportedFormats = new ArrayList();
        char nChannels = 1;
        while (nChannels <= 2) {
            for (double d : Constants.AUDIO_SAMPLE_RATES) {
                int nSamplesPerSec = (int) d;
                char wBitsPerSample = 16;
                while (wBitsPerSample > 0) {
                    char nBlockAlign = (char) ((nChannels * wBitsPerSample) / 8);
                    WASAPI.WAVEFORMATEX_fill(this.waveformatex, 1, nChannels, nSamplesPerSec, nSamplesPerSec * nBlockAlign, nBlockAlign, wBitsPerSample, 0);
                    long pClosestMatch = WASAPI.IAudioClient_IsFormatSupported(iAudioClient, 0, this.waveformatex);
                    if (pClosestMatch != 0) {
                        try {
                            AudioFormat supportedFormat;
                            if (pClosestMatch != this.waveformatex) {
                                if (WASAPI.WAVEFORMATEX_getWFormatTag(pClosestMatch) == 1) {
                                    nChannels = WASAPI.WAVEFORMATEX_getNChannels(pClosestMatch);
                                    nSamplesPerSec = WASAPI.WAVEFORMATEX_getNSamplesPerSec(pClosestMatch);
                                    wBitsPerSample = WASAPI.WAVEFORMATEX_getWBitsPerSample(pClosestMatch);
                                } else if (pClosestMatch != this.waveformatex) {
                                    WASAPI.CoTaskMemFree(pClosestMatch);
                                }
                            }
                            if (nChannels == 2) {
                                supportedFormat = new NativelySupportedAudioFormat(AudioFormat.LINEAR, (double) nSamplesPerSec, wBitsPerSample, 1, AbstractAudioRenderer.NATIVE_AUDIO_FORMAT_ENDIAN, 1, -1, -1.0d, Format.byteArray);
                                if (!supportedFormats.contains(supportedFormat)) {
                                    supportedFormats.add(supportedFormat);
                                }
                            }
                            supportedFormat = new NativelySupportedAudioFormat(AudioFormat.LINEAR, (double) nSamplesPerSec, wBitsPerSample, nChannels, AbstractAudioRenderer.NATIVE_AUDIO_FORMAT_ENDIAN, 1, -1, -1.0d, Format.byteArray);
                            if (!supportedFormats.contains(supportedFormat)) {
                                supportedFormats.add(supportedFormat);
                            }
                            if (pClosestMatch != this.waveformatex) {
                                WASAPI.CoTaskMemFree(pClosestMatch);
                            }
                        } catch (Throwable th) {
                            if (pClosestMatch != this.waveformatex) {
                                WASAPI.CoTaskMemFree(pClosestMatch);
                            }
                        }
                    }
                    wBitsPerSample = (char) (wBitsPerSample - 8);
                }
            }
            nChannels = (char) (nChannels + 1);
        }
        return supportedFormats;
    }

    private List<AudioFormat> getIMediaObjectSupportedFormats(long iMediaObject) throws HResultException {
        List<AudioFormat> supportedFormats = new ArrayList();
        long pmt = VoiceCaptureDSP.MoCreateMediaType(0);
        if (pmt == 0) {
            throw new OutOfMemoryError("MoCreateMediaType");
        }
        int hresult;
        try {
            hresult = VoiceCaptureDSP.DMO_MEDIA_TYPE_fill(pmt, VoiceCaptureDSP.MEDIATYPE_Audio, VoiceCaptureDSP.MEDIASUBTYPE_PCM, true, false, 0, VoiceCaptureDSP.FORMAT_WaveFormatEx, 0, WASAPI.WAVEFORMATEX_sizeof() + 0, this.waveformatex);
            if (WASAPI.FAILED(hresult)) {
                throw new HResultException(hresult, "DMO_MEDIA_TYPE_fill");
            }
            for (char nChannels = 1; nChannels <= 2; nChannels = (char) (nChannels + 1)) {
                for (double d : Constants.AUDIO_SAMPLE_RATES) {
                    int nSamplesPerSec = (int) d;
                    for (char wBitsPerSample = 16; wBitsPerSample > 0; wBitsPerSample = (char) (wBitsPerSample - 8)) {
                        char nBlockAlign = (char) ((nChannels * wBitsPerSample) / 8);
                        WASAPI.WAVEFORMATEX_fill(this.waveformatex, 1, nChannels, nSamplesPerSec, nSamplesPerSec * nBlockAlign, nBlockAlign, wBitsPerSample, 0);
                        VoiceCaptureDSP.DMO_MEDIA_TYPE_setLSampleSize(pmt, wBitsPerSample / 8);
                        hresult = VoiceCaptureDSP.IMediaObject_SetOutputType(iMediaObject, 0, pmt, 1);
                        if (hresult == 0) {
                            AudioFormat supportedFormat = new AudioFormat(AudioFormat.LINEAR, (double) nSamplesPerSec, wBitsPerSample, nChannels, AbstractAudioRenderer.NATIVE_AUDIO_FORMAT_ENDIAN, 1, -1, -1.0d, Format.byteArray);
                            if (!supportedFormats.contains(supportedFormat)) {
                                supportedFormats.add(supportedFormat);
                            }
                        }
                    }
                }
            }
            VoiceCaptureDSP.DMO_MEDIA_TYPE_setCbFormat(pmt, 0);
            VoiceCaptureDSP.DMO_MEDIA_TYPE_setFormattype(pmt, VoiceCaptureDSP.FORMAT_None);
            VoiceCaptureDSP.DMO_MEDIA_TYPE_setPbFormat(pmt, 0);
            VoiceCaptureDSP.MoDeleteMediaType(pmt);
            return supportedFormats;
        } catch (HResultException hre) {
            hresult = hre.getHResult();
        } catch (Throwable th) {
            VoiceCaptureDSP.DMO_MEDIA_TYPE_setCbFormat(pmt, 0);
            VoiceCaptureDSP.DMO_MEDIA_TYPE_setFormattype(pmt, VoiceCaptureDSP.FORMAT_None);
            VoiceCaptureDSP.DMO_MEDIA_TYPE_setPbFormat(pmt, 0);
            VoiceCaptureDSP.MoDeleteMediaType(pmt);
        }
    }

    public synchronized long getIMMDevice(String id) throws HResultException {
        long iMMDeviceEnumerator;
        iMMDeviceEnumerator = this.iMMDeviceEnumerator;
        if (iMMDeviceEnumerator == 0) {
            throw new IllegalStateException("iMMDeviceEnumerator");
        }
        return WASAPI.IMMDeviceEnumerator_GetDevice(iMMDeviceEnumerator, id);
    }

    public int getIMMDeviceDataFlow(long iMMDevice) throws HResultException {
        long iMMEndpoint = WASAPI.IMMDevice_QueryInterface(iMMDevice, WASAPI.IID_IMMEndpoint);
        if (iMMEndpoint == 0) {
            throw new RuntimeException("IMMDevice_QueryInterface");
        }
        try {
            int dataFlow = WASAPI.IMMEndpoint_GetDataFlow(iMMEndpoint);
            switch (dataFlow) {
                case 0:
                case 1:
                case 2:
                    return dataFlow;
                default:
                    throw new RuntimeException("IMMEndpoint_GetDataFlow");
            }
        } finally {
            WASAPI.IMMEndpoint_Release(iMMEndpoint);
        }
    }

    private String getIMMDeviceFriendlyName(long iMMDevice) throws HResultException {
        long iPropertyStore = WASAPI.IMMDevice_OpenPropertyStore(iMMDevice, 0);
        if (iPropertyStore == 0) {
            throw new RuntimeException("IMMDevice_OpenPropertyStore");
        }
        try {
            String deviceFriendlyName = WASAPI.IPropertyStore_GetString(iPropertyStore, WASAPI.PKEY_Device_FriendlyName);
            return deviceFriendlyName;
        } finally {
            WASAPI.IPropertyStore_Release(iPropertyStore);
        }
    }

    public synchronized int getIMMDeviceIndex(String id, int dataFlow) throws HResultException {
        int iMMDeviceIndex;
        long iMMDeviceEnumerator = this.iMMDeviceEnumerator;
        if (iMMDeviceEnumerator == 0) {
            throw new IllegalStateException("iMMDeviceEnumerator");
        }
        long iMMDeviceCollection = WASAPI.IMMDeviceEnumerator_EnumAudioEndpoints(iMMDeviceEnumerator, dataFlow, 1);
        if (iMMDeviceCollection == 0) {
            throw new RuntimeException("IMMDeviceEnumerator_EnumAudioEndpoints");
        }
        iMMDeviceIndex = -1;
        long iMMDevice;
        try {
            int count = WASAPI.IMMDeviceCollection_GetCount(iMMDeviceCollection);
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    iMMDevice = WASAPI.IMMDeviceCollection_Item(iMMDeviceCollection, i);
                    if (iMMDevice == 0) {
                        throw new RuntimeException("IMMDeviceCollection_Item");
                    }
                    String iMMDeviceID = WASAPI.IMMDevice_GetId(iMMDevice);
                    WASAPI.IMMDevice_Release(iMMDevice);
                    if (id.equalsIgnoreCase(iMMDeviceID)) {
                        iMMDeviceIndex = i;
                        break;
                    }
                }
            }
            WASAPI.IMMDeviceCollection_Release(iMMDeviceCollection);
        } catch (Throwable th) {
            WASAPI.IMMDeviceCollection_Release(iMMDeviceCollection);
        }
        return iMMDeviceIndex;
    }

    /* access modifiers changed from: protected */
    public String getRendererClassName() {
        return WASAPIRenderer.class.getName();
    }

    public long initializeAEC() throws Exception {
        long iMediaObject = 0;
        long iPropertyStore = 0;
        long aecIMediaObject = 0;
        try {
            iMediaObject = WASAPI.CoCreateInstance(VoiceCaptureDSP.CLSID_CWMAudioAEC, 0, 23, VoiceCaptureDSP.IID_IMediaObject);
            if (iMediaObject == 0) {
                throw new RuntimeException("CoCreateInstance");
            }
            iPropertyStore = VoiceCaptureDSP.IMediaObject_QueryInterface(iMediaObject, VoiceCaptureDSP.IID_IPropertyStore);
            if (iPropertyStore == 0) {
                throw new RuntimeException("IMediaObject_QueryInterface");
            }
            int hresult = VoiceCaptureDSP.IPropertyStore_SetValue(iPropertyStore, VoiceCaptureDSP.MFPKEY_WMAAECMA_SYSTEM_MODE, 0);
            if (WASAPI.FAILED(hresult)) {
                throw new HResultException(hresult, "IPropertyStore_SetValue MFPKEY_WMAAECMA_SYSTEM_MODE");
            }
            aecIMediaObject = iMediaObject;
            iMediaObject = 0;
            return aecIMediaObject;
        } finally {
            if (iPropertyStore != 0) {
                WASAPI.IPropertyStore_Release(iPropertyStore);
            }
            if (iMediaObject != 0) {
                VoiceCaptureDSP.IMediaObject_Release(iMediaObject);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0087 A:{SYNTHETIC, Splitter:B:29:0x0087} */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x0053 A:{Catch:{ all -> 0x00eb, all -> 0x0099, all -> 0x005b }} */
    public long initializeIAudioClient(javax.media.MediaLocator r27, org.jitsi.impl.neomedia.device.AudioSystem.DataFlow r28, int r29, long r30, long r32, javax.media.format.AudioFormat[] r34) throws org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.HResultException {
        /*
        r26 = this;
        CoInitializeEx();
        r20 = r27.getRemainder();
        r0 = r26;
        r1 = r20;
        r4 = r0.getIMMDevice(r1);
        r10 = 0;
        r9 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));
        if (r9 != 0) goto L_0x0030;
    L_0x0015:
        r9 = new java.lang.RuntimeException;
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r11 = "Failed to retrieve audio endpoint device with endpoint ID string ";
        r10 = r10.append(r11);
        r0 = r20;
        r10 = r10.append(r0);
        r10 = r10.toString();
        r9.<init>(r10);
        throw r9;
    L_0x0030:
        r24 = 0;
        r0 = r26;
        r19 = r0.getIMMDeviceDataFlow(r4);	 Catch:{ all -> 0x005b }
        r9 = org.jitsi.impl.neomedia.device.WASAPISystem.AnonymousClass2.$SwitchMap$org$jitsi$impl$neomedia$device$AudioSystem$DataFlow;	 Catch:{ all -> 0x005b }
        r10 = r28.ordinal();	 Catch:{ all -> 0x005b }
        r9 = r9[r10];	 Catch:{ all -> 0x005b }
        switch(r9) {
            case 1: goto L_0x0066;
            case 2: goto L_0x0078;
            case 3: goto L_0x0078;
            default: goto L_0x0043;
        };	 Catch:{ all -> 0x005b }
    L_0x0043:
        r6 = "{1cb9ad4c-dbfa-4c32-b178-c2f568a703b2}";
        r7 = 23;
        r8 = 0;
        r6 = org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.WASAPI.IMMDevice_Activate(r4, r6, r7, r8);	 Catch:{ all -> 0x005b }
        r10 = 0;
        r9 = (r6 > r10 ? 1 : (r6 == r10 ? 0 : -1));
        if (r9 != 0) goto L_0x0087;
    L_0x0053:
        r9 = new java.lang.RuntimeException;	 Catch:{ all -> 0x005b }
        r10 = "IMMDevice_Activate";
        r9.<init>(r10);	 Catch:{ all -> 0x005b }
        throw r9;	 Catch:{ all -> 0x005b }
    L_0x005b:
        r9 = move-exception;
        r10 = 0;
        r10 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));
        if (r10 == 0) goto L_0x0065;
    L_0x0062:
        org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.WASAPI.IMMDevice_Release(r4);
    L_0x0065:
        throw r9;
    L_0x0066:
        r9 = 2;
        r0 = r19;
        if (r0 == r9) goto L_0x0043;
    L_0x006b:
        r9 = 1;
        r0 = r19;
        if (r0 == r9) goto L_0x0043;
    L_0x0070:
        r9 = new java.lang.IllegalArgumentException;	 Catch:{ all -> 0x005b }
        r10 = "dataFlow";
        r9.<init>(r10);	 Catch:{ all -> 0x005b }
        throw r9;	 Catch:{ all -> 0x005b }
    L_0x0078:
        r9 = 2;
        r0 = r19;
        if (r0 == r9) goto L_0x0043;
    L_0x007d:
        if (r19 == 0) goto L_0x0043;
    L_0x007f:
        r9 = new java.lang.IllegalArgumentException;	 Catch:{ all -> 0x005b }
        r10 = "dataFlow";
        r9.<init>(r10);	 Catch:{ all -> 0x005b }
        throw r9;	 Catch:{ all -> 0x005b }
    L_0x0087:
        r14 = org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.WASAPI.WAVEFORMATEX_alloc();	 Catch:{ all -> 0x0099 }
        r10 = 0;
        r9 = (r14 > r10 ? 1 : (r14 == r10 ? 0 : -1));
        if (r9 != 0) goto L_0x00a4;
    L_0x0091:
        r9 = new java.lang.OutOfMemoryError;	 Catch:{ all -> 0x0099 }
        r10 = "WAVEFORMATEX_alloc";
        r9.<init>(r10);	 Catch:{ all -> 0x0099 }
        throw r9;	 Catch:{ all -> 0x0099 }
    L_0x0099:
        r9 = move-exception;
        r10 = 0;
        r10 = (r6 > r10 ? 1 : (r6 == r10 ? 0 : -1));
        if (r10 == 0) goto L_0x00a3;
    L_0x00a0:
        org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.WASAPI.IAudioClient_Release(r6);	 Catch:{ all -> 0x005b }
    L_0x00a3:
        throw r9;	 Catch:{ all -> 0x005b }
    L_0x00a4:
        r8 = 0;
        r21 = -1;
        r18 = 0;
    L_0x00a9:
        r0 = r34;
        r9 = r0.length;	 Catch:{ all -> 0x00eb }
        r0 = r18;
        if (r0 >= r9) goto L_0x00cf;
    L_0x00b0:
        r9 = r34[r18];	 Catch:{ all -> 0x00eb }
        WAVEFORMATEX_fill(r14, r9);	 Catch:{ all -> 0x00eb }
        r22 = org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.WASAPI.IAudioClient_IsFormatSupported(r6, r8, r14);	 Catch:{ all -> 0x00eb }
        r10 = 0;
        r9 = (r22 > r10 ? 1 : (r22 == r10 ? 0 : -1));
        if (r9 != 0) goto L_0x00c2;
    L_0x00bf:
        r18 = r18 + 1;
        goto L_0x00a9;
    L_0x00c2:
        r9 = (r22 > r14 ? 1 : (r22 == r14 ? 0 : -1));
        if (r9 != 0) goto L_0x00f0;
    L_0x00c6:
        r21 = r18;
        r9 = (r22 > r14 ? 1 : (r22 == r14 ? 0 : -1));
        if (r9 == 0) goto L_0x00cf;
    L_0x00cc:
        org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.WASAPI.CoTaskMemFree(r22);	 Catch:{ all -> 0x00eb }
    L_0x00cf:
        if (r21 < 0) goto L_0x00d8;
    L_0x00d1:
        r0 = r34;
        r9 = r0.length;	 Catch:{ all -> 0x00eb }
        r0 = r21;
        if (r0 < r9) goto L_0x00f8;
    L_0x00d8:
        r0 = r26;
        r1 = r28;
        r2 = r27;
        r3 = r34;
        r0.logUnsupportedFormats(r1, r2, r3);	 Catch:{ all -> 0x00eb }
        r9 = new java.lang.IllegalArgumentException;	 Catch:{ all -> 0x00eb }
        r10 = "formats";
        r9.<init>(r10);	 Catch:{ all -> 0x00eb }
        throw r9;	 Catch:{ all -> 0x00eb }
    L_0x00eb:
        r9 = move-exception;
        org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.WASAPI.CoTaskMemFree(r14);	 Catch:{ all -> 0x0099 }
        throw r9;	 Catch:{ all -> 0x0099 }
    L_0x00f0:
        r9 = (r22 > r14 ? 1 : (r22 == r14 ? 0 : -1));
        if (r9 == 0) goto L_0x00bf;
    L_0x00f4:
        org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.WASAPI.CoTaskMemFree(r22);	 Catch:{ all -> 0x00eb }
        goto L_0x00bf;
    L_0x00f8:
        r9 = 0;
        r10 = 0;
        r0 = r34;
        r1 = r21;
        java.util.Arrays.fill(r0, r9, r1, r10);	 Catch:{ all -> 0x00eb }
        r9 = 524288; // 0x80000 float:7.34684E-40 double:2.590327E-318;
        r29 = r29 | r9;
        r10 = 0;
        r9 = (r30 > r10 ? 1 : (r30 == r10 ? 0 : -1));
        if (r9 == 0) goto L_0x010f;
    L_0x010b:
        r9 = 262144; // 0x40000 float:3.67342E-40 double:1.295163E-318;
        r29 = r29 | r9;
    L_0x010f:
        r10 = -1;
        r9 = (r32 > r10 ? 1 : (r32 == r10 ? 0 : -1));
        if (r9 != 0) goto L_0x0125;
    L_0x0115:
        r10 = org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.WASAPI.IAudioClient_GetDefaultDevicePeriod(r6);	 Catch:{ all -> 0x00eb }
        r12 = 10000; // 0x2710 float:1.4013E-41 double:4.9407E-320;
        r32 = r10 / r12;
        r10 = 1;
        r9 = (r32 > r10 ? 1 : (r32 == r10 ? 0 : -1));
        if (r9 > 0) goto L_0x0125;
    L_0x0123:
        r32 = 10;
    L_0x0125:
        r10 = 3;
        r10 = r10 * r32;
        r12 = 10000; // 0x2710 float:1.4013E-41 double:4.9407E-320;
        r10 = r10 * r12;
        r12 = 0;
        r16 = audioSessionGuid;	 Catch:{ all -> 0x00eb }
        r9 = r29;
        r17 = org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.WASAPI.IAudioClient_Initialize(r6, r8, r9, r10, r12, r14, r16);	 Catch:{ all -> 0x00eb }
        if (r17 == 0) goto L_0x0140;
    L_0x0138:
        r9 = new org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.HResultException;	 Catch:{ all -> 0x00eb }
        r0 = r17;
        r9.m2493init(r0);	 Catch:{ all -> 0x00eb }
        throw r9;	 Catch:{ all -> 0x00eb }
    L_0x0140:
        r9 = 262144; // 0x40000 float:3.67342E-40 double:1.295163E-318;
        r9 = r9 & r29;
        r10 = 262144; // 0x40000 float:3.67342E-40 double:1.295163E-318;
        if (r9 != r10) goto L_0x0153;
    L_0x0148:
        r10 = 0;
        r9 = (r30 > r10 ? 1 : (r30 == r10 ? 0 : -1));
        if (r9 == 0) goto L_0x0153;
    L_0x014e:
        r0 = r30;
        org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.WASAPI.IAudioClient_SetEventHandle(r6, r0);	 Catch:{ all -> 0x00eb }
    L_0x0153:
        r24 = r6;
        r6 = 0;
        org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.WASAPI.CoTaskMemFree(r14);	 Catch:{ all -> 0x0099 }
        r10 = 0;
        r9 = (r6 > r10 ? 1 : (r6 == r10 ? 0 : -1));
        if (r9 == 0) goto L_0x0163;
    L_0x0160:
        org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.WASAPI.IAudioClient_Release(r6);	 Catch:{ all -> 0x005b }
    L_0x0163:
        r10 = 0;
        r9 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));
        if (r9 == 0) goto L_0x016c;
    L_0x0169:
        org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.WASAPI.IMMDevice_Release(r4);
    L_0x016c:
        return r24;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.device.WASAPISystem.initializeIAudioClient(javax.media.MediaLocator, org.jitsi.impl.neomedia.device.AudioSystem$DataFlow, int, long, long, javax.media.format.AudioFormat[]):long");
    }

    private void logUnsupportedFormats(DataFlow dataFlow, MediaLocator locator, Format[] unsupportedFormats) {
        Format[] supportedFormats;
        StringBuilder msg = new StringBuilder();
        msg.append("Unsupported formats: ");
        msg.append(Arrays.toString(unsupportedFormats));
        msg.append('.');
        try {
            supportedFormats = getDevice(dataFlow, locator).getFormats();
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                ThreadDeath t2 = (ThreadDeath) t;
            } else {
                supportedFormats = null;
            }
        }
        msg.append("Supported formats: ");
        msg.append(Arrays.toString(supportedFormats));
        msg.append('.');
        logger.error(msg);
    }

    private void maybeInitializeAEC() {
        if (this.aecIMediaObject == 0 && this.aecSupportedFormats == null) {
            long iMediaObject;
            try {
                iMediaObject = initializeAEC();
                List<AudioFormat> supportedFormats = getIMediaObjectSupportedFormats(iMediaObject);
                if (!supportedFormats.isEmpty()) {
                    this.aecIMediaObject = iMediaObject;
                    iMediaObject = 0;
                    this.aecSupportedFormats = Collections.unmodifiableList(supportedFormats);
                }
                if (iMediaObject != 0) {
                    VoiceCaptureDSP.IMediaObject_Release(iMediaObject);
                }
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    ThreadDeath t2 = (ThreadDeath) t;
                } else {
                    logger.error("Failed to initialize acoustic echo cancellation (AEC)", t);
                }
            }
        }
    }

    private void maybeUninitializeAEC() {
        try {
            if (this.aecIMediaObject != 0) {
                VoiceCaptureDSP.IMediaObject_Release(this.aecIMediaObject);
                this.aecIMediaObject = 0;
            }
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                ThreadDeath t2 = (ThreadDeath) t;
            } else {
                logger.error("Failed to uninitialize acoustic echo cancellation (AEC)", t);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void postInitialize() {
        try {
            super.postInitialize();
        } finally {
            if (this.waveformatex != 0) {
                WASAPI.CoTaskMemFree(this.waveformatex);
                this.waveformatex = 0;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void preInitialize() {
        super.preInitialize();
        if (this.waveformatex != 0) {
            WASAPI.CoTaskMemFree(this.waveformatex);
            this.waveformatex = 0;
        }
        this.waveformatex = WASAPI.WAVEFORMATEX_alloc();
        if (this.waveformatex == 0) {
            throw new OutOfMemoryError("WAVEFORMATEX_alloc");
        }
        if (this.pNotify == null) {
            this.pNotify = new IMMNotificationClient() {
                public void OnDefaultDeviceChanged(int flow, int role, String pwstrDefaultDevice) {
                }

                public void OnDeviceAdded(String pwstrDeviceId) {
                    WASAPISystem.this.reinitialize(pwstrDeviceId);
                }

                public void OnDeviceRemoved(String pwstrDeviceId) {
                    WASAPISystem.this.reinitialize(pwstrDeviceId);
                }

                public void OnDeviceStateChanged(String pwstrDeviceId, int dwNewState) {
                    WASAPISystem.this.reinitialize(pwstrDeviceId);
                }

                public void OnPropertyValueChanged(String pwstrDeviceId, long key) {
                }
            };
        }
        if (audioSessionGuid == null) {
            try {
                audioSessionGuid = WASAPI.CoCreateGuid();
            } catch (HResultException hre) {
                logger.warn("Failed to generate a new audio session GUID", hre);
            }
        }
    }

    /* access modifiers changed from: private */
    public void reinitialize(String deviceId) {
        try {
            DeviceSystem.invokeDeviceSystemInitialize(this, true);
        } catch (Exception e) {
            logger.error("Failed to reinitialize " + getClass().getName(), e);
        }
    }

    public String toString() {
        return "Windows Audio Session API (WASAPI)";
    }
}
