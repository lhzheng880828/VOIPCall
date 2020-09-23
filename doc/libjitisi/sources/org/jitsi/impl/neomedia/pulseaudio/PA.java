package org.jitsi.impl.neomedia.pulseaudio;

public final class PA {
    public static final int CONTEXT_AUTHORIZING = 2;
    public static final int CONTEXT_CONNECTING = 1;
    public static final int CONTEXT_FAILED = 5;
    public static final int CONTEXT_NOFAIL = 2;
    public static final int CONTEXT_NOFLAGS = 0;
    public static final int CONTEXT_READY = 4;
    public static final int CONTEXT_SETTING_NAME = 3;
    public static final int CONTEXT_TERMINATED = 6;
    public static final int CONTEXT_UNCONNECTED = 0;
    public static final int ENCODING_ANY = 0;
    public static final int ENCODING_INVALID = -1;
    public static final int ENCODING_PCM = 1;
    public static final int INVALID_INDEX = -1;
    public static final int OPERATION_CANCELLED = 2;
    public static final int OPERATION_DONE = 1;
    public static final int OPERATION_RUNNING = 0;
    public static final String PROP_APPLICATION_NAME = "application.name";
    public static final String PROP_APPLICATION_VERSION = "application.version";
    public static final String PROP_FORMAT_CHANNELS = "format.channels";
    public static final String PROP_FORMAT_RATE = "format.rate";
    public static final String PROP_MEDIA_NAME = "media.name";
    public static final String PROP_MEDIA_ROLE = "media.role";
    public static final int SAMPLE_S16LE = 3;
    public static final int SEEK_RELATIVE = 0;
    public static final int STREAM_ADJUST_LATENCY = 8192;
    public static final int STREAM_FAILED = 3;
    public static final int STREAM_NOFLAGS = 0;
    public static final int STREAM_READY = 2;
    public static final int STREAM_START_CORKED = 1;
    public static final int STREAM_TERMINATED = 4;

    public interface source_info_cb_t {
        void callback(long j, long j2, int i);
    }

    public interface sink_info_cb_t {
        void callback(long j, long j2, int i);
    }

    public interface stream_request_cb_t {
        void callback(long j, int i);
    }

    public interface context_success_cb_t {
        void callback(long j, boolean z);
    }

    public interface stream_success_cb_t {
        void callback(long j, boolean z);
    }

    public static native void buffer_attr_free(long j);

    public static native long buffer_attr_new(int i, int i2, int i3, int i4, int i5);

    public static native int context_connect(long j, String str, int i, long j2);

    public static native void context_disconnect(long j);

    public static native long context_get_sink_info_list(long j, sink_info_cb_t sink_info_cb_t);

    public static native long context_get_source_info_list(long j, source_info_cb_t source_info_cb_t);

    public static native int context_get_state(long j);

    public static native long context_new_with_proplist(long j, String str, long j2);

    public static native long context_set_sink_input_volume(long j, int i, long j2, context_success_cb_t context_success_cb_t);

    public static native long context_set_source_output_volume(long j, int i, long j2, context_success_cb_t context_success_cb_t);

    public static native void context_set_state_callback(long j, Runnable runnable);

    public static native void context_unref(long j);

    public static native void cvolume_free(long j);

    public static native long cvolume_new();

    public static native long cvolume_set(long j, int i, int i2);

    public static native int format_info_get_encoding(long j);

    public static native long format_info_get_plist(long j);

    public static native int format_info_get_prop_int(long j, String str);

    public static native String get_library_version();

    public static native int operation_get_state(long j);

    public static native void operation_unref(long j);

    public static native void proplist_free(long j);

    public static native long proplist_new();

    public static native int proplist_sets(long j, String str, String str2);

    public static native void sample_spec_free(long j);

    public static native long sample_spec_new(int i, int i2, int i3);

    public static native String sink_info_get_description(long j);

    public static native long[] sink_info_get_formats(long j);

    public static native int sink_info_get_index(long j);

    public static native int sink_info_get_monitor_source(long j);

    public static native String sink_info_get_monitor_source_name(long j);

    public static native String sink_info_get_name(long j);

    public static native int sink_info_get_sample_spec_channels(long j);

    public static native int sink_info_get_sample_spec_format(long j);

    public static native int sink_info_get_sample_spec_rate(long j);

    public static native String source_info_get_description(long j);

    public static native long[] source_info_get_formats(long j);

    public static native int source_info_get_index(long j);

    public static native int source_info_get_monitor_of_sink(long j);

    public static native String source_info_get_name(long j);

    public static native int source_info_get_sample_spec_channels(long j);

    public static native int source_info_get_sample_spec_format(long j);

    public static native int source_info_get_sample_spec_rate(long j);

    public static native int stream_connect_playback(long j, String str, long j2, int i, long j3, long j4);

    public static native int stream_connect_record(long j, String str, long j2, int i);

    public static native long stream_cork(long j, boolean z, stream_success_cb_t stream_success_cb_t);

    public static native int stream_disconnect(long j);

    public static native int stream_drop(long j);

    public static native String stream_get_device_name(long j);

    public static native int stream_get_index(long j);

    public static native int stream_get_state(long j);

    public static native long stream_new_with_proplist(long j, String str, long j2, long j3, long j4);

    public static native int stream_peek(long j, byte[] bArr, int i);

    public static native int stream_readable_size(long j);

    public static native void stream_set_read_callback(long j, stream_request_cb_t stream_request_cb_t);

    public static native void stream_set_state_callback(long j, Runnable runnable);

    public static native void stream_set_write_callback(long j, stream_request_cb_t stream_request_cb_t);

    public static native void stream_unref(long j);

    public static native int stream_writable_size(long j);

    public static native int stream_write(long j, byte[] bArr, int i, int i2, Runnable runnable, long j2, int i3);

    public static native int sw_volume_from_linear(double d);

    public static native void threaded_mainloop_free(long j);

    public static native long threaded_mainloop_get_api(long j);

    public static native void threaded_mainloop_lock(long j);

    public static native long threaded_mainloop_new();

    public static native void threaded_mainloop_signal(long j, boolean z);

    public static native int threaded_mainloop_start(long j);

    public static native void threaded_mainloop_stop(long j);

    public static native void threaded_mainloop_unlock(long j);

    public static native void threaded_mainloop_wait(long j);

    static {
        System.loadLibrary("jnpulseaudio");
    }

    private PA() {
    }
}
