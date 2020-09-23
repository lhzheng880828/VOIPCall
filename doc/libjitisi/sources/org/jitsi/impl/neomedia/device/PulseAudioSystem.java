package org.jitsi.impl.neomedia.device;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.media.CaptureDeviceInfo;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.media.Renderer;
import javax.media.format.AudioFormat;
import org.jitsi.impl.neomedia.MediaUtils;
import org.jitsi.impl.neomedia.device.AudioSystem.DataFlow;
import org.jitsi.impl.neomedia.jmfext.media.renderer.audio.PulseAudioRenderer;
import org.jitsi.impl.neomedia.pulseaudio.PA;
import org.jitsi.impl.neomedia.pulseaudio.PA.sink_info_cb_t;
import org.jitsi.impl.neomedia.pulseaudio.PA.source_info_cb_t;
import org.jitsi.service.version.Version;

public class PulseAudioSystem extends AudioSystem {
    private static final String LOCATOR_PROTOCOL = "pulseaudio";
    public static final String MEDIA_ROLE_EVENT = "event";
    public static final String MEDIA_ROLE_PHONE = "phone";
    private static final String NULL_DEV_CAPTURE_DEVICE_INFO_NAME = "Default";
    private long context;
    private boolean createContext;
    private long mainloop;

    public static void corkStream(long stream, boolean b) throws IOException {
        if (stream == 0) {
            throw new IOException("stream");
        }
        long o = PA.stream_cork(stream, b, null);
        if (o == 0) {
            throw new IOException("pa_stream_cork");
        }
        PA.operation_unref(o);
    }

    public static PulseAudioSystem getPulseAudioSystem() {
        AudioSystem audioSystem = AudioSystem.getAudioSystem("pulseaudio");
        return audioSystem instanceof PulseAudioSystem ? (PulseAudioSystem) audioSystem : null;
    }

    public PulseAudioSystem() throws Exception {
        super("pulseaudio", 8);
    }

    private void createContext() {
        if (this.context != 0) {
            throw new IllegalStateException("context");
        }
        startMainloop();
        long proplist;
        try {
            proplist = PA.proplist_new();
            if (proplist == 0) {
                throw new RuntimeException("pa_proplist_new");
            }
            populateContextProplist(proplist);
            long context = PA.context_new_with_proplist(PA.threaded_mainloop_get_api(this.mainloop), null, proplist);
            if (context == 0) {
                throw new RuntimeException("pa_context_new_with_proplist");
            }
            if (proplist != 0) {
                try {
                    PA.proplist_free(proplist);
                    proplist = 0;
                } catch (Throwable th) {
                    if (this.context == 0) {
                        PA.context_unref(context);
                    }
                }
            }
            Runnable stateCallback = new Runnable() {
                public void run() {
                    PulseAudioSystem.this.signalMainloop(false);
                }
            };
            lockMainloop();
            try {
                PA.context_set_state_callback(context, stateCallback);
                PA.context_connect(context, null, 0, 0);
                if (waitForContextState(context, 4) != 4) {
                    throw new IllegalStateException("context.state");
                }
                this.context = context;
                if (this.context == 0) {
                    PA.context_disconnect(context);
                }
                unlockMainloop();
                if (this.context == 0) {
                    PA.context_unref(context);
                }
                if (proplist != 0) {
                    PA.proplist_free(proplist);
                }
                if (this.context == 0) {
                    stopMainloop();
                }
            } catch (Throwable th2) {
                unlockMainloop();
            }
        } catch (Throwable th3) {
            if (this.context == 0) {
                stopMainloop();
            }
        }
    }

    public Renderer createRenderer(boolean playback) {
        MediaLocator locator;
        if (playback) {
            locator = null;
        } else {
            CaptureDeviceInfo device = getSelectedDevice(DataFlow.NOTIFY);
            if (device == null) {
                return null;
            }
            locator = device.getLocator();
        }
        PulseAudioRenderer renderer = new PulseAudioRenderer(playback ? MEDIA_ROLE_PHONE : MEDIA_ROLE_EVENT);
        if (renderer == null || locator == null) {
            return renderer;
        }
        renderer.setLocator(locator);
        return renderer;
    }

    public long createStream(int sampleRate, int channels, String mediaName, String mediaRole) throws IllegalStateException, RuntimeException {
        long context = getContext();
        if (context == 0) {
            throw new IllegalStateException("context");
        }
        long sampleSpec = PA.sample_spec_new(3, sampleRate, channels);
        if (sampleSpec == 0) {
            throw new RuntimeException("pa_sample_spec_new");
        }
        long proplist;
        try {
            proplist = PA.proplist_new();
            if (proplist == 0) {
                throw new RuntimeException("pa_proplist_new");
            }
            PA.proplist_sets(proplist, PA.PROP_MEDIA_NAME, mediaRole);
            PA.proplist_sets(proplist, PA.PROP_MEDIA_ROLE, mediaRole);
            long stream = PA.stream_new_with_proplist(context, null, sampleSpec, 0, proplist);
            if (stream == 0) {
                throw new RuntimeException("pa_stream_new_with_proplist");
            }
            long ret = stream;
            if (ret == 0) {
                PA.stream_unref(stream);
            }
            if (proplist != 0) {
                PA.proplist_free(proplist);
            }
            if (sampleSpec != 0) {
                PA.sample_spec_free(sampleSpec);
            }
            return ret;
        } catch (Throwable th) {
            if (sampleSpec != 0) {
                PA.sample_spec_free(sampleSpec);
            }
        }
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void doInitialize() throws Exception {
        long context = getContext();
        final List<CaptureDeviceInfo2> captureDevices = new LinkedList();
        final List<Format> captureDeviceFormats = new LinkedList();
        source_info_cb_t anonymousClass2 = new source_info_cb_t() {
            public void callback(long c, long i, int eol) {
                if (eol == 0 && i != 0) {
                    try {
                        PulseAudioSystem.this.sourceInfoListCallback(c, i, captureDevices, captureDeviceFormats);
                    } catch (Throwable th) {
                        PulseAudioSystem.this.signalMainloop(false);
                    }
                }
                PulseAudioSystem.this.signalMainloop(false);
            }
        };
        List<CaptureDeviceInfo2> playbackDevices = new LinkedList();
        final List<Format> playbackDeviceFormats = new LinkedList();
        final List<CaptureDeviceInfo2> list = playbackDevices;
        sink_info_cb_t anonymousClass3 = new sink_info_cb_t() {
            public void callback(long c, long i, int eol) {
                if (eol == 0 && i != 0) {
                    try {
                        PulseAudioSystem.this.sinkInfoListCallback(c, i, list, playbackDeviceFormats);
                    } catch (Throwable th) {
                        PulseAudioSystem.this.signalMainloop(false);
                    }
                }
                PulseAudioSystem.this.signalMainloop(false);
            }
        };
        lockMainloop();
        long o;
        try {
            o = PA.context_get_source_info_list(context, anonymousClass2);
            if (o == 0) {
                throw new RuntimeException("pa_context_get_source_info_list");
            }
            while (PA.operation_get_state(o) == 0) {
                waitMainloop();
            }
            PA.operation_unref(o);
            o = PA.context_get_sink_info_list(context, anonymousClass3);
            if (o == 0) {
                throw new RuntimeException("pa_context_get_sink_info_list");
            }
            while (PA.operation_get_state(o) == 0) {
                waitMainloop();
            }
            PA.operation_unref(o);
            unlockMainloop();
            if (!captureDeviceFormats.isEmpty()) {
                captureDevices.add(0, new CaptureDeviceInfo2(NULL_DEV_CAPTURE_DEVICE_INFO_NAME, new MediaLocator("pulseaudio:"), (Format[]) captureDeviceFormats.toArray(new Format[captureDeviceFormats.size()]), null, null, null));
            }
            if (!playbackDevices.isEmpty()) {
                playbackDevices.add(0, new CaptureDeviceInfo2(NULL_DEV_CAPTURE_DEVICE_INFO_NAME, new MediaLocator("pulseaudio:"), null, null, null, null));
            }
            setCaptureDevices(captureDevices);
            setPlaybackDevices(playbackDevices);
        } catch (Throwable th) {
            unlockMainloop();
        }
    }

    public synchronized long getContext() {
        if (this.context == 0) {
            if (!this.createContext) {
                this.createContext = true;
                createContext();
            }
            if (this.context == 0) {
                throw new IllegalStateException("context");
            }
        }
        return this.context;
    }

    public void lockMainloop() {
        PA.threaded_mainloop_lock(this.mainloop);
    }

    private void populateContextProplist(long proplist) {
        String applicationName = System.getProperty(Version.PNAME_APPLICATION_NAME);
        if (applicationName != null) {
            PA.proplist_sets(proplist, PA.PROP_APPLICATION_NAME, applicationName);
        }
        String applicationVersion = System.getProperty(Version.PNAME_APPLICATION_VERSION);
        if (applicationVersion != null) {
            PA.proplist_sets(proplist, PA.PROP_APPLICATION_VERSION, applicationVersion);
        }
    }

    public void signalMainloop(boolean waitForAccept) {
        PA.threaded_mainloop_signal(this.mainloop, waitForAccept);
    }

    /* access modifiers changed from: private */
    public void sinkInfoListCallback(long context, long sinkInfo, List<CaptureDeviceInfo2> deviceList, List<Format> list) {
        if (PA.sink_info_get_sample_spec_format(sinkInfo) == 3) {
            String description = PA.sink_info_get_description(sinkInfo);
            String name = PA.sink_info_get_name(sinkInfo);
            if (description == null) {
                description = name;
            }
            deviceList.add(new CaptureDeviceInfo2(description, new MediaLocator("pulseaudio:" + name), null, null, null, null));
        }
    }

    /* access modifiers changed from: private */
    public void sourceInfoListCallback(long context, long sourceInfo, List<CaptureDeviceInfo2> deviceList, List<Format> formatList) {
        if (PA.source_info_get_monitor_of_sink(sourceInfo) == -1 && PA.source_info_get_sample_spec_format(sourceInfo) == 3) {
            int channels = PA.source_info_get_sample_spec_channels(sourceInfo);
            int rate = PA.source_info_get_sample_spec_rate(sourceInfo);
            List<Format> sourceInfoFormatList = new LinkedList();
            if (MediaUtils.MAX_AUDIO_CHANNELS != -1 && MediaUtils.MAX_AUDIO_CHANNELS < channels) {
                channels = MediaUtils.MAX_AUDIO_CHANNELS;
            }
            if (MediaUtils.MAX_AUDIO_SAMPLE_RATE != -1.0d && MediaUtils.MAX_AUDIO_SAMPLE_RATE < ((double) rate)) {
                rate = (int) MediaUtils.MAX_AUDIO_SAMPLE_RATE;
            }
            AudioFormat audioFormat = new AudioFormat(AudioFormat.LINEAR, (double) rate, 16, channels, 0, 1, -1, -1.0d, Format.byteArray);
            if (!sourceInfoFormatList.contains(audioFormat)) {
                sourceInfoFormatList.add(audioFormat);
                if (!formatList.contains(audioFormat)) {
                    formatList.add(audioFormat);
                }
            }
            if (!formatList.isEmpty()) {
                String description = PA.source_info_get_description(sourceInfo);
                String name = PA.source_info_get_name(sourceInfo);
                if (description == null) {
                    description = name;
                }
                List<CaptureDeviceInfo2> list = deviceList;
                list.add(new CaptureDeviceInfo2(description, new MediaLocator("pulseaudio:" + name), (Format[]) sourceInfoFormatList.toArray(new Format[sourceInfoFormatList.size()]), null, null, null));
            }
        }
    }

    private void startMainloop() {
        if (this.mainloop != 0) {
            throw new IllegalStateException("mainloop");
        }
        long mainloop = PA.threaded_mainloop_new();
        if (mainloop == 0) {
            throw new RuntimeException("pa_threaded_mainloop_new");
        }
        try {
            if (PA.threaded_mainloop_start(mainloop) < 0) {
                throw new RuntimeException("pa_threaded_mainloop_start");
            }
            this.mainloop = mainloop;
        } finally {
            if (this.mainloop == 0) {
                PA.threaded_mainloop_free(mainloop);
            }
        }
    }

    private void stopMainloop() {
        if (this.mainloop == 0) {
            throw new IllegalStateException("mainloop");
        }
        long mainloop = this.mainloop;
        this.mainloop = 0;
        PA.threaded_mainloop_stop(mainloop);
        PA.threaded_mainloop_free(mainloop);
    }

    public String toString() {
        return "PulseAudio";
    }

    public void unlockMainloop() {
        PA.threaded_mainloop_unlock(this.mainloop);
    }

    private int waitForContextState(long context, int stateToWaitFor) {
        int state;
        while (true) {
            state = PA.context_get_state(context);
            if (5 == state || stateToWaitFor == state || 6 == state) {
                return state;
            }
            waitMainloop();
        }
        return state;
    }

    public int waitForStreamState(long stream, int stateToWaitFor) {
        int state;
        while (true) {
            state = PA.stream_get_state(stream);
            if (stateToWaitFor == state || 3 == state || 4 == state) {
                return state;
            }
            waitMainloop();
        }
        return state;
    }

    public void waitMainloop() {
        PA.threaded_mainloop_wait(this.mainloop);
    }
}
