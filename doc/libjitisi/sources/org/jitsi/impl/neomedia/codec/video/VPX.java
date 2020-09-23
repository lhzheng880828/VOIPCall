package org.jitsi.impl.neomedia.codec.video;

public class VPX {
    public static final int CODEC_CX_FRAME_PKT = 0;
    public static final int CODEC_LIST_END = 9;
    public static final int CODEC_OK = 0;
    public static final int CODEC_USE_OUTPUT_PARTITION = 131072;
    public static final int CODEC_USE_XMA = 1;
    public static final int DL_REALTIME = 1;
    public static final int ERROR_RESILIENT_DEFAULT = 1;
    public static final int ERROR_RESILIENT_PARTITIONS = 2;
    public static final int IMG_FMT_I420 = 258;
    public static final int INTEFACE_VP8_DEC = 0;
    public static final int INTERFACE_VP8_ENC = 1;
    public static final int KF_MODE_AUTO = 1;
    public static final int KF_MODE_DISABLED = 1;
    public static final int RC_MODE_CBR = 1;
    public static final int RC_MODE_CQ = 2;
    public static final int RC_MODE_VBR = 0;

    static class StreamInfo {
        int h;
        boolean is_kf;
        int w;

        StreamInfo(int iface, byte[] buf, int buf_offset, int buf_size) {
            long si = VPX.stream_info_malloc();
            if (VPX.codec_peek_stream_info(iface, buf, buf_offset, buf_size, si) == 0) {
                this.w = VPX.stream_info_get_w(si);
                this.h = VPX.stream_info_get_h(si);
                this.is_kf = VPX.stream_info_get_is_kf(si) != 0;
                if (si != 0) {
                    VPX.free(si);
                }
            }
        }

        public int getW() {
            return this.w;
        }

        public int getH() {
            return this.h;
        }

        public boolean isKf() {
            return this.is_kf;
        }
    }

    public static native long codec_ctx_malloc();

    public static native long codec_cx_pkt_get_data(long j);

    public static native int codec_cx_pkt_get_kind(long j);

    public static native int codec_cx_pkt_get_size(long j);

    public static native long codec_dec_cfg_malloc();

    public static native void codec_dec_cfg_set_h(long j, int i);

    public static native void codec_dec_cfg_set_w(long j, int i);

    public static native int codec_dec_init(long j, int i, long j2, long j3);

    public static native int codec_decode(long j, byte[] bArr, int i, int i2, long j2, long j3);

    public static native int codec_destroy(long j);

    public static native long codec_enc_cfg_malloc();

    public static native void codec_enc_cfg_set_error_resilient(long j, int i);

    public static native void codec_enc_cfg_set_h(long j, int i);

    public static native void codec_enc_cfg_set_kf_max_dist(long j, int i);

    public static native void codec_enc_cfg_set_kf_min_dist(long j, int i);

    public static native void codec_enc_cfg_set_kf_mode(long j, int i);

    public static native void codec_enc_cfg_set_profile(long j, int i);

    public static native void codec_enc_cfg_set_rc_buf_initial_sz(long j, int i);

    public static native void codec_enc_cfg_set_rc_buf_optimal_sz(long j, int i);

    public static native void codec_enc_cfg_set_rc_buf_sz(long j, int i);

    public static native void codec_enc_cfg_set_rc_dropframe_thresh(long j, int i);

    public static native void codec_enc_cfg_set_rc_end_usage(long j, int i);

    public static native void codec_enc_cfg_set_rc_max_quantizer(long j, int i);

    public static native void codec_enc_cfg_set_rc_min_quantizer(long j, int i);

    public static native void codec_enc_cfg_set_rc_overshoot_pct(long j, int i);

    public static native void codec_enc_cfg_set_rc_resize_allowed(long j, int i);

    public static native void codec_enc_cfg_set_rc_resize_down_thresh(long j, int i);

    public static native void codec_enc_cfg_set_rc_resize_up_thresh(long j, int i);

    public static native void codec_enc_cfg_set_rc_target_bitrate(long j, int i);

    public static native void codec_enc_cfg_set_rc_undershoot_pct(long j, int i);

    public static native void codec_enc_cfg_set_threads(long j, int i);

    public static native void codec_enc_cfg_set_w(long j, int i);

    public static native int codec_enc_config_default(int i, long j, int i2);

    public static native int codec_enc_config_set(long j, long j2);

    public static native int codec_enc_init(long j, int i, long j2, long j3);

    public static native int codec_encode(long j, long j2, byte[] bArr, int i, int i2, int i3, long j3, long j4, long j5, long j6);

    public static native int codec_err_to_string(int i, byte[] bArr, int i2);

    public static native long codec_get_cx_data(long j, long[] jArr);

    public static native long codec_get_frame(long j, long[] jArr);

    public static native int codec_get_mem_map(long j, long[] jArr, long[] jArr2);

    public static native long codec_mmap_get_sz(long j);

    public static native void codec_mmap_set_base(long j, long j2);

    public static native int codec_peek_stream_info(int i, byte[] bArr, int i2, int i3, long j);

    public static native int codec_set_mem_map(long j, long j2, int i);

    public static native void free(long j);

    public static native int img_get_d_h(long j);

    public static native int img_get_d_w(long j);

    public static native int img_get_fmt(long j);

    public static native int img_get_h(long j);

    public static native long img_get_plane0(long j);

    public static native long img_get_plane1(long j);

    public static native long img_get_plane2(long j);

    public static native int img_get_stride0(long j);

    public static native int img_get_stride1(long j);

    public static native int img_get_stride2(long j);

    public static native int img_get_w(long j);

    public static native long img_malloc();

    public static native void img_set_bps(long j, int i);

    public static native void img_set_d_h(long j, int i);

    public static native void img_set_d_w(long j, int i);

    public static native void img_set_fmt(long j, int i);

    public static native void img_set_h(long j, int i);

    public static native void img_set_stride0(long j, int i);

    public static native void img_set_stride1(long j, int i);

    public static native void img_set_stride2(long j, int i);

    public static native void img_set_stride3(long j, int i);

    public static native void img_set_w(long j, int i);

    public static native void img_wrap(long j, int i, int i2, int i3, int i4, long j2);

    public static native long malloc(long j);

    public static native void memcpy(byte[] bArr, long j, int i);

    public static native int stream_info_get_h(long j);

    public static native int stream_info_get_is_kf(long j);

    public static native int stream_info_get_w(long j);

    public static native long stream_info_malloc();

    public static String codec_err_to_string(int err) {
        byte[] buf = new byte[100];
        codec_err_to_string(err, buf, buf.length);
        return new String(buf);
    }

    static {
        System.loadLibrary("jnvpx");
    }
}
