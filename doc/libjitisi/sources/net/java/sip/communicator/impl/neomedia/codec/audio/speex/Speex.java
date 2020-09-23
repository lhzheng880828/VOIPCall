package net.java.sip.communicator.impl.neomedia.codec.audio.speex;

public final class Speex {
    public static final int SPEEX_GET_FRAME_SIZE = 3;
    public static final int SPEEX_MODEID_NB = 0;
    public static final int SPEEX_MODEID_UWB = 2;
    public static final int SPEEX_MODEID_WB = 1;
    public static final int SPEEX_RESAMPLER_QUALITY_VOIP = 3;
    public static final int SPEEX_SET_ENH = 0;
    public static final int SPEEX_SET_QUALITY = 4;
    public static final int SPEEX_SET_SAMPLING_RATE = 24;

    public static native void speex_bits_destroy(long j);

    public static native long speex_bits_init();

    public static native int speex_bits_nbytes(long j);

    public static native void speex_bits_read_from(long j, byte[] bArr, int i, int i2);

    public static native int speex_bits_remaining(long j);

    public static native void speex_bits_reset(long j);

    public static native int speex_bits_write(long j, byte[] bArr, int i, int i2);

    public static native int speex_decode_int(long j, long j2, byte[] bArr, int i);

    public static native int speex_decoder_ctl(long j, int i);

    public static native int speex_decoder_ctl(long j, int i, int i2);

    public static native void speex_decoder_destroy(long j);

    public static native long speex_decoder_init(long j);

    public static native int speex_encode_int(long j, byte[] bArr, int i, long j2);

    public static native int speex_encoder_ctl(long j, int i);

    public static native int speex_encoder_ctl(long j, int i, int i2);

    public static native void speex_encoder_destroy(long j);

    public static native long speex_encoder_init(long j);

    public static native long speex_lib_get_mode(int i);

    public static native void speex_resampler_destroy(long j);

    public static native long speex_resampler_init(int i, int i2, int i3, int i4, long j);

    public static native int speex_resampler_process_interleaved_int(long j, byte[] bArr, int i, int i2, byte[] bArr2, int i3, int i4);

    public static native int speex_resampler_set_rate(long j, int i, int i2);

    static {
        System.loadLibrary("jnspeex");
    }

    public static void assertSpeexIsFunctional() {
        speex_lib_get_mode(0);
    }

    private Speex() {
    }
}
