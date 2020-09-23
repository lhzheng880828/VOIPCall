package org.jitsi.impl.neomedia.codec;

public class FFmpeg {
    public static final long AV_NOPTS_VALUE = Long.MIN_VALUE;
    public static final int AV_NUM_DATA_POINTERS = 8;
    public static final int AV_SAMPLE_FMT_S16 = 1;
    public static final int CODEC_FLAG2_CHUNKS = 32768;
    public static final int CODEC_FLAG2_INTRA_REFRESH = 2097152;
    public static final int CODEC_FLAG_AC_PRED = 33554432;
    public static final int CODEC_FLAG_H263P_SLICE_STRUCT = 268435456;
    public static final int CODEC_FLAG_H263P_UMV = 16777216;
    public static final int CODEC_FLAG_LOOP_FILTER = 2048;
    public static final int CODEC_ID_H263 = 5;
    public static final int CODEC_ID_H263P = 20;
    public static final int CODEC_ID_H264 = 28;
    public static final int CODEC_ID_MJPEG = 8;
    public static final int CODEC_ID_MP3 = 86017;
    public static final int CODEC_ID_VP8 = 142;
    public static final int FF_BUG_AUTODETECT = 1;
    public static final int FF_CMP_CHROMA = 256;
    public static final int FF_INPUT_BUFFER_PADDING_SIZE = 8;
    public static final int FF_MB_DECISION_SIMPLE = 0;
    public static final int FF_MIN_BUFFER_SIZE = 16384;
    public static final int FF_PROFILE_H264_BASELINE = 66;
    public static final int FF_PROFILE_H264_HIGH = 100;
    public static final int FF_PROFILE_H264_MAIN = 77;
    public static final int PIX_FMT_ARGB = 27;
    public static final int PIX_FMT_BGR24_1 = 3;
    public static final int PIX_FMT_BGR32 = PIX_FMT_BGR32();
    public static final int PIX_FMT_BGR32_1 = PIX_FMT_BGR32_1();
    public static final int PIX_FMT_NONE = -1;
    public static final int PIX_FMT_NV12 = 25;
    public static final int PIX_FMT_RGB24 = PIX_FMT_RGB24();
    public static final int PIX_FMT_RGB24_1 = 2;
    public static final int PIX_FMT_RGB32 = PIX_FMT_RGB32();
    public static final int PIX_FMT_RGB32_1 = PIX_FMT_RGB32_1();
    public static final int PIX_FMT_UYVY422 = 17;
    public static final int PIX_FMT_UYYVYY411 = 18;
    public static final int PIX_FMT_YUV411P = 7;
    public static final int PIX_FMT_YUV420P = 0;
    public static final int PIX_FMT_YUVJ422P = 13;
    public static final int PIX_FMT_YUYV422 = 1;
    public static final int SWS_BICUBIC = 4;

    private static native int PIX_FMT_BGR32();

    private static native int PIX_FMT_BGR32_1();

    private static native int PIX_FMT_RGB24();

    private static native int PIX_FMT_RGB32();

    private static native int PIX_FMT_RGB32_1();

    public static native void av_free(long j);

    public static native long av_malloc(int i);

    public static native void av_register_all();

    public static native long avcodec_alloc_context3(long j);

    public static native long avcodec_alloc_frame();

    public static native int avcodec_close(long j);

    public static native int avcodec_decode_video(long j, long j2, long j3, int i);

    public static native int avcodec_decode_video(long j, long j2, boolean[] zArr, byte[] bArr, int i);

    public static native int avcodec_encode_audio(long j, byte[] bArr, int i, int i2, byte[] bArr2, int i3);

    public static native int avcodec_encode_video(long j, byte[] bArr, int i, long j2);

    public static native long avcodec_find_decoder(int i);

    public static native long avcodec_find_encoder(int i);

    public static native int avcodec_open2(long j, long j2, String... strArr);

    public static native void avcodec_register_all();

    public static native void avcodeccontext_add_flags(long j, int i);

    public static native void avcodeccontext_add_flags2(long j, int i);

    public static native int avcodeccontext_get_frame_size(long j);

    public static native int avcodeccontext_get_height(long j);

    public static native int avcodeccontext_get_pix_fmt(long j);

    public static native int avcodeccontext_get_width(long j);

    public static native void avcodeccontext_set_b_frame_strategy(long j, int i);

    public static native void avcodeccontext_set_bit_rate(long j, int i);

    public static native void avcodeccontext_set_bit_rate_tolerance(long j, int i);

    public static native void avcodeccontext_set_channels(long j, int i);

    public static native void avcodeccontext_set_chromaoffset(long j, int i);

    public static native void avcodeccontext_set_gop_size(long j, int i);

    public static native void avcodeccontext_set_i_quant_factor(long j, float f);

    public static native void avcodeccontext_set_keyint_min(long j, int i);

    public static native void avcodeccontext_set_max_b_frames(long j, int i);

    public static native void avcodeccontext_set_mb_decision(long j, int i);

    public static native void avcodeccontext_set_me_cmp(long j, int i);

    public static native void avcodeccontext_set_me_method(long j, int i);

    public static native void avcodeccontext_set_me_range(long j, int i);

    public static native void avcodeccontext_set_me_subpel_quality(long j, int i);

    public static native void avcodeccontext_set_pix_fmt(long j, int i);

    public static native void avcodeccontext_set_profile(long j, int i);

    public static native void avcodeccontext_set_qcompress(long j, float f);

    public static native void avcodeccontext_set_quantizer(long j, int i, int i2, int i3);

    public static native void avcodeccontext_set_rc_buffer_size(long j, int i);

    public static native void avcodeccontext_set_rc_eq(long j, String str);

    public static native void avcodeccontext_set_rc_max_rate(long j, int i);

    public static native void avcodeccontext_set_refs(long j, int i);

    public static native void avcodeccontext_set_rtp_payload_size(long j, int i);

    public static native void avcodeccontext_set_sample_aspect_ratio(long j, int i, int i2);

    public static native void avcodeccontext_set_sample_fmt(long j, int i);

    public static native void avcodeccontext_set_sample_rate(long j, int i);

    public static native void avcodeccontext_set_scenechange_threshold(long j, int i);

    public static native void avcodeccontext_set_size(long j, int i, int i2);

    public static native void avcodeccontext_set_thread_count(long j, int i);

    public static native void avcodeccontext_set_ticks_per_frame(long j, int i);

    public static native void avcodeccontext_set_time_base(long j, int i, int i2);

    public static native void avcodeccontext_set_trellis(long j, int i);

    public static native void avcodeccontext_set_workaround_bugs(long j, int i);

    public static native long avfilter_graph_alloc();

    public static native int avfilter_graph_config(long j, long j2);

    public static native void avfilter_graph_free(long j);

    public static native long avfilter_graph_get_filter(long j, String str);

    public static native int avfilter_graph_parse(long j, String str, long j2, long j3, long j4);

    public static native void avfilter_register_all();

    public static native void avfilter_unref_buffer(long j);

    public static native long avframe_get_pts(long j);

    public static native void avframe_set_data(long j, long j2, long j3, long j4);

    public static native void avframe_set_key_frame(long j, boolean z);

    public static native void avframe_set_linesize(long j, int i, int i2, int i3);

    public static native int avpicture_fill(long j, long j2, int i, int i2, int i3);

    public static native long get_filtered_video_frame(long j, int i, int i2, int i3, long j2, long j3, long j4);

    public static native void memcpy(long j, byte[] bArr, int i, int i2);

    public static native void memcpy(int[] iArr, int i, int i2, long j);

    public static native void sws_freeContext(long j);

    public static native long sws_getCachedContext(long j, int i, int i2, int i3, int i4, int i5, int i6, int i7);

    public static native int sws_scale(long j, long j2, int i, int i2, Object obj, int i3, int i4, int i5);

    public static native int sws_scale(long j, Object obj, int i, int i2, int i3, int i4, int i5, Object obj2, int i6, int i7, int i8);

    static {
        System.loadLibrary("jnffmpeg");
        av_register_all();
        avcodec_register_all();
        avfilter_register_all();
    }

    public static void avcodec_free_frame(long frame) {
        av_free(frame);
    }
}
