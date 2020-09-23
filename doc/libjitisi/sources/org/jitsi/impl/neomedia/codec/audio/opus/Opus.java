package org.jitsi.impl.neomedia.codec.audio.opus;

public class Opus {
    public static final int BANDWIDTH_FULLBAND = 1105;
    public static final int BANDWIDTH_MEDIUMBAND = 1102;
    public static final int BANDWIDTH_NARROWBAND = 1101;
    public static final int BANDWIDTH_SUPERWIDEBAND = 1104;
    public static final int BANDWIDTH_WIDEBAND = 1103;
    public static final int INVALID_PACKET = -4;
    public static final int MAX_PACKET = 1276;
    public static final int OPUS_AUTO = -1000;
    public static final int OPUS_OK = 0;

    public static native int decode(long j, byte[] bArr, int i, int i2, byte[] bArr2, int i3, int i4, int i5);

    public static native long decoder_create(int i, int i2);

    public static native void decoder_destroy(long j);

    public static native int decoder_get_nb_samples(long j, byte[] bArr, int i, int i2);

    public static native int decoder_get_size(int i);

    public static native int encode(long j, byte[] bArr, int i, int i2, byte[] bArr2, int i3, int i4);

    public static native long encoder_create(int i, int i2);

    public static native void encoder_destroy(long j);

    public static native int encoder_get_bandwidth(long j);

    public static native int encoder_get_bitrate(long j);

    public static native int encoder_get_dtx(long j);

    public static native int encoder_get_inband_fec(long j);

    public static native int encoder_get_size(int i);

    public static native int encoder_get_vbr(long j);

    public static native int encoder_get_vbr_constraint(long j);

    public static native int encoder_set_bandwidth(long j, int i);

    public static native int encoder_set_bitrate(long j, int i);

    public static native int encoder_set_complexity(long j, int i);

    public static native int encoder_set_dtx(long j, int i);

    public static native int encoder_set_force_channels(long j, int i);

    public static native int encoder_set_inband_fec(long j, int i);

    public static native int encoder_set_max_bandwidth(long j, int i);

    public static native int encoder_set_packet_loss_perc(long j, int i);

    public static native int encoder_set_vbr(long j, int i);

    public static native int encoder_set_vbr_constraint(long j, int i);

    public static native int packet_get_bandwidth(byte[] bArr, int i);

    public static native int packet_get_nb_channels(byte[] bArr, int i);

    public static native int packet_get_nb_frames(byte[] bArr, int i, int i2);

    static {
        System.loadLibrary("jnopus");
    }

    public static void assertOpusIsFunctional() {
        decoder_get_size(1);
        encoder_get_size(1);
    }
}
