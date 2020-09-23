package org.jitsi.impl.neomedia.codec.video.h264;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.VideoFormat;
import javax.media.format.YUVFormat;
import net.sf.fmj.media.AbstractCodec;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.impl.neomedia.NeomediaServiceUtils;
import org.jitsi.impl.neomedia.codec.AbstractCodec2;
import org.jitsi.impl.neomedia.codec.FFmpeg;
import org.jitsi.impl.neomedia.format.ParameterizedVideoFormat;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.neomedia.codec.Constants;
import org.jitsi.service.neomedia.control.KeyFrameControl;
import org.jitsi.service.neomedia.control.KeyFrameControl.KeyFrameRequestee;
import org.jitsi.service.neomedia.event.RTCPFeedbackEvent;
import org.jitsi.service.neomedia.event.RTCPFeedbackListener;
import org.jitsi.util.Logger;

public class JNIEncoder extends AbstractCodec implements RTCPFeedbackListener {
    public static final String[] AVAILABLE_PRESETS = new String[]{"ultrafast", "superfast", "veryfast", "faster", "fast", "medium", "slow", "slower", "veryslow"};
    public static final String BASELINE_PROFILE = "baseline";
    public static final boolean DEFAULT_DEFAULT_INTRA_REFRESH = true;
    public static final String DEFAULT_DEFAULT_PROFILE = "main";
    public static final int DEFAULT_FRAME_RATE = 15;
    public static final String DEFAULT_INTRA_REFRESH_PNAME = "org.jitsi.impl.neomedia.codec.video.h264.defaultIntraRefresh";
    public static final int DEFAULT_KEYINT = 150;
    public static final String DEFAULT_PRESET = AVAILABLE_PRESETS[0];
    public static final String DEFAULT_PROFILE_PNAME = "net.java.sip.communicator.impl.neomedia.codec.video.h264.defaultProfile";
    public static final String HIGH_PROFILE = "high";
    public static final String KEYINT_PNAME = "org.jitsi.impl.neomedia.codec.video.h264.keyint";
    public static final String MAIN_PROFILE = "main";
    public static final String PACKETIZATION_MODE_FMTP = "packetization-mode";
    private static final long PLI_INTERVAL = 3000;
    private static final String PLUGIN_NAME = "H.264 Encoder";
    public static final String PRESET_PNAME = "org.jitsi.impl.neomedia.codec.video.h264.preset";
    static final Format[] SUPPORTED_OUTPUT_FORMATS;
    public static final int X264_KEYINT_MAX_INFINITE = 1073741824;
    public static final int X264_KEYINT_MIN_AUTO = 0;
    private static final Logger logger = Logger.getLogger(JNIEncoder.class);
    private Map<String, String> additionalCodecSettings;
    private long avFrame;
    private long avctx;
    private boolean forceKeyFrame;
    private KeyFrameControl keyFrameControl;
    private KeyFrameRequestee keyFrameRequestee;
    private int keyint;
    private int lastKeyFrame;
    private long lastKeyFrameRequestTime;
    private String packetizationMode;
    private long rawFrameBuffer;
    private int rawFrameLen;
    private boolean secondKeyFrame;

    static {
        Format[] formatArr = new Format[2];
        formatArr[0] = new ParameterizedVideoFormat(Constants.H264, PACKETIZATION_MODE_FMTP, "0");
        formatArr[1] = new ParameterizedVideoFormat(Constants.H264, PACKETIZATION_MODE_FMTP, "1");
        SUPPORTED_OUTPUT_FORMATS = formatArr;
    }

    private static int getProfileForConfig(String profile) {
        if (BASELINE_PROFILE.equalsIgnoreCase(profile)) {
            return 66;
        }
        if (HIGH_PROFILE.equalsIgnoreCase(profile)) {
            return 100;
        }
        return 77;
    }

    public JNIEncoder() {
        this.forceKeyFrame = true;
        this.lastKeyFrameRequestTime = System.currentTimeMillis();
        this.secondKeyFrame = true;
        this.inputFormats = new Format[]{new YUVFormat(null, -1, Format.byteArray, -1.0f, 2, -1, -1, -1, -1, -1)};
        this.inputFormat = null;
        this.outputFormat = null;
    }

    public synchronized void close() {
        if (this.opened) {
            this.opened = false;
            super.close();
            if (this.avctx != 0) {
                FFmpeg.avcodec_close(this.avctx);
                FFmpeg.av_free(this.avctx);
                this.avctx = 0;
            }
            if (this.avFrame != 0) {
                FFmpeg.avcodec_free_frame(this.avFrame);
                this.avFrame = 0;
            }
            if (this.rawFrameBuffer != 0) {
                FFmpeg.av_free(this.rawFrameBuffer);
                this.rawFrameBuffer = 0;
            }
            if (this.keyFrameRequestee != null) {
                if (this.keyFrameControl != null) {
                    this.keyFrameControl.removeKeyFrameRequestee(this.keyFrameRequestee);
                }
                this.keyFrameRequestee = null;
            }
        }
    }

    private Format[] getMatchingOutputFormats(Format inputFormat) {
        VideoFormat inputVideoFormat = (VideoFormat) inputFormat;
        String[] packetizationModes = this.packetizationMode == null ? new String[]{"0", "1"} : new String[]{this.packetizationMode};
        Format[] matchingOutputFormats = new Format[packetizationModes.length];
        Dimension size = inputVideoFormat.getSize();
        float frameRate = inputVideoFormat.getFrameRate();
        for (int index = packetizationModes.length - 1; index >= 0; index--) {
            matchingOutputFormats[index] = new ParameterizedVideoFormat(Constants.H264, size, -1, Format.byteArray, frameRate, ParameterizedVideoFormat.toMap(PACKETIZATION_MODE_FMTP, packetizationModes[index]));
        }
        return matchingOutputFormats;
    }

    public String getName() {
        return PLUGIN_NAME;
    }

    public Format[] getSupportedOutputFormats(Format in) {
        if (in == null) {
            return SUPPORTED_OUTPUT_FORMATS;
        }
        if (!(in instanceof VideoFormat) || AbstractCodec2.matches(in, this.inputFormats) == null) {
            return new Format[0];
        }
        return getMatchingOutputFormats(in);
    }

    private boolean isKeyFrame() {
        if (!this.forceKeyFrame) {
            return this.lastKeyFrame == this.keyint;
        } else {
            if (this.secondKeyFrame) {
                this.secondKeyFrame = false;
                this.forceKeyFrame = true;
                return true;
            }
            this.forceKeyFrame = false;
            return true;
        }
    }

    /* access modifiers changed from: private */
    public boolean keyFrameRequest() {
        long now = System.currentTimeMillis();
        if (now > this.lastKeyFrameRequestTime + PLI_INTERVAL) {
            this.lastKeyFrameRequestTime = now;
            this.forceKeyFrame = true;
        }
        return true;
    }

    public synchronized void open() throws ResourceUnavailableException {
        if (!this.opened) {
            VideoFormat inputVideoFormat = (VideoFormat) this.inputFormat;
            VideoFormat outputVideoFormat = (VideoFormat) this.outputFormat;
            Dimension size = null;
            if (inputVideoFormat != null) {
                size = inputVideoFormat.getSize();
            }
            if (size == null && outputVideoFormat != null) {
                size = outputVideoFormat.getSize();
            }
            if (size == null) {
                throw new ResourceUnavailableException("The input video frame width and height are not set.");
            }
            String str;
            int width = size.width;
            int height = size.height;
            ConfigurationService cfg = LibJitsi.getConfigurationService();
            boolean intraRefresh = true;
            int keyint = DEFAULT_KEYINT;
            String preset = DEFAULT_PRESET;
            String profile = "main";
            if (cfg != null) {
                intraRefresh = cfg.getBoolean(DEFAULT_INTRA_REFRESH_PNAME, true);
                keyint = cfg.getInt(KEYINT_PNAME, DEFAULT_KEYINT);
                preset = cfg.getString(PRESET_PNAME, preset);
                profile = cfg.getString(DEFAULT_PROFILE_PNAME, profile);
            }
            if (this.additionalCodecSettings != null) {
                for (Entry<String, String> e : this.additionalCodecSettings.entrySet()) {
                    String k = (String) e.getKey();
                    String v = (String) e.getValue();
                    if ("h264.intrarefresh".equals(k)) {
                        if ("false".equals(v)) {
                            intraRefresh = false;
                        }
                    } else if ("h264.profile".equals(k) && (BASELINE_PROFILE.equals(v) || HIGH_PROFILE.equals(v) || "main".equals(v))) {
                        profile = v;
                    }
                }
            }
            long avcodec = FFmpeg.avcodec_find_encoder(28);
            this.avctx = FFmpeg.avcodec_alloc_context3(avcodec);
            FFmpeg.avcodeccontext_set_pix_fmt(this.avctx, 0);
            FFmpeg.avcodeccontext_set_size(this.avctx, width, height);
            FFmpeg.avcodeccontext_set_qcompress(this.avctx, 0.6f);
            int bitRate = NeomediaServiceUtils.getMediaServiceImpl().getDeviceConfiguration().getVideoBitrate() * 1000;
            int frameRate = -1;
            if (outputVideoFormat != null) {
                frameRate = (int) outputVideoFormat.getFrameRate();
            }
            if (frameRate == -1 && inputVideoFormat != null) {
                frameRate = (int) inputVideoFormat.getFrameRate();
            }
            if (frameRate == -1) {
                frameRate = 15;
            }
            FFmpeg.avcodeccontext_set_bit_rate(this.avctx, bitRate);
            FFmpeg.avcodeccontext_set_bit_rate_tolerance(this.avctx, bitRate / frameRate);
            FFmpeg.avcodeccontext_set_rc_max_rate(this.avctx, bitRate);
            FFmpeg.avcodeccontext_set_sample_aspect_ratio(this.avctx, 0, 0);
            FFmpeg.avcodeccontext_set_thread_count(this.avctx, 1);
            FFmpeg.avcodeccontext_set_time_base(this.avctx, 1, frameRate);
            FFmpeg.avcodeccontext_set_ticks_per_frame(this.avctx, 2);
            FFmpeg.avcodeccontext_set_quantizer(this.avctx, 30, 31, 4);
            FFmpeg.avcodeccontext_set_mb_decision(this.avctx, 0);
            FFmpeg.avcodeccontext_set_rc_eq(this.avctx, "blurCplx^(1-qComp)");
            FFmpeg.avcodeccontext_add_flags(this.avctx, 2048);
            if (intraRefresh) {
                FFmpeg.avcodeccontext_add_flags2(this.avctx, FFmpeg.CODEC_FLAG2_INTRA_REFRESH);
            }
            FFmpeg.avcodeccontext_set_me_method(this.avctx, 7);
            FFmpeg.avcodeccontext_set_me_subpel_quality(this.avctx, 2);
            FFmpeg.avcodeccontext_set_me_range(this.avctx, 16);
            FFmpeg.avcodeccontext_set_me_cmp(this.avctx, 256);
            FFmpeg.avcodeccontext_set_scenechange_threshold(this.avctx, 40);
            FFmpeg.avcodeccontext_set_rc_buffer_size(this.avctx, 10);
            FFmpeg.avcodeccontext_set_gop_size(this.avctx, keyint);
            FFmpeg.avcodeccontext_set_i_quant_factor(this.avctx, 0.71428573f);
            FFmpeg.avcodeccontext_set_refs(this.avctx, 1);
            FFmpeg.avcodeccontext_set_keyint_min(this.avctx, 0);
            if (this.packetizationMode == null || "0".equals(this.packetizationMode)) {
                FFmpeg.avcodeccontext_set_rtp_payload_size(this.avctx, 1024);
            }
            try {
                FFmpeg.avcodeccontext_set_profile(this.avctx, getProfileForConfig(profile));
            } catch (UnsatisfiedLinkError e2) {
                logger.warn("The FFmpeg JNI library is out-of-date.");
            }
            long j = this.avctx;
            String[] strArr = new String[12];
            strArr[0] = "intra-refresh";
            if (intraRefresh) {
                str = "1";
            } else {
                str = "0";
            }
            strArr[1] = str;
            strArr[2] = "keyint";
            strArr[3] = Integer.toString(keyint);
            strArr[4] = "partitions";
            strArr[5] = "b8x8,i4x4,p8x8";
            strArr[6] = "preset";
            strArr[7] = preset;
            strArr[8] = "thread_type";
            strArr[9] = "slice";
            strArr[10] = "tune";
            strArr[11] = "zerolatency";
            if (FFmpeg.avcodec_open2(j, avcodec, strArr) < 0) {
                throw new ResourceUnavailableException("Could not open codec. (size= " + width + "x" + height + ")");
            }
            this.rawFrameLen = ((width * height) * 3) / 2;
            this.rawFrameBuffer = FFmpeg.av_malloc(this.rawFrameLen);
            this.avFrame = FFmpeg.avcodec_alloc_frame();
            int sizeInBytes = width * height;
            FFmpeg.avframe_set_data(this.avFrame, this.rawFrameBuffer, (long) sizeInBytes, (long) (sizeInBytes / 4));
            FFmpeg.avframe_set_linesize(this.avFrame, width, width / 2, width / 2);
            this.forceKeyFrame = true;
            this.keyint = keyint;
            this.lastKeyFrame = 0;
            if (this.keyFrameRequestee == null) {
                this.keyFrameRequestee = new KeyFrameRequestee() {
                    public boolean keyFrameRequest() {
                        return JNIEncoder.this.keyFrameRequest();
                    }
                };
            }
            if (this.keyFrameControl != null) {
                this.keyFrameControl.addKeyFrameRequestee(-1, this.keyFrameRequestee);
            }
            this.opened = true;
            super.open();
        }
    }

    public synchronized int process(Buffer inBuffer, Buffer outBuffer) {
        if (isEOM(inBuffer)) {
            propagateEOM(outBuffer);
            reset();
        } else if (inBuffer.isDiscard()) {
            outBuffer.setDiscard(true);
            reset();
        } else {
            Format inFormat = inBuffer.getFormat();
            if (!(inFormat == this.inputFormat || inFormat.equals(this.inputFormat))) {
                setInputFormat(inFormat);
            }
            if (inBuffer.getLength() < 10) {
                outBuffer.setDiscard(true);
                reset();
            } else {
                FFmpeg.memcpy(this.rawFrameBuffer, (byte[]) inBuffer.getData(), inBuffer.getOffset(), this.rawFrameLen);
                boolean keyFrame = isKeyFrame();
                FFmpeg.avframe_set_key_frame(this.avFrame, keyFrame);
                if (keyFrame) {
                    this.lastKeyFrame = 0;
                } else {
                    this.lastKeyFrame++;
                }
                byte[] out = AbstractCodec2.validateByteArraySize(outBuffer, this.rawFrameLen, false);
                outBuffer.setLength(FFmpeg.avcodec_encode_video(this.avctx, out, out.length, this.avFrame));
                outBuffer.setOffset(0);
                outBuffer.setTimeStamp(inBuffer.getTimeStamp());
            }
        }
        return 0;
    }

    public void rtcpFeedbackReceived(RTCPFeedbackEvent event) {
        if (event.getPayloadType() == RTCPFeedbackEvent.PT_PS) {
            switch (event.getFeedbackMessageType()) {
                case 1:
                case 4:
                    if (logger.isTraceEnabled()) {
                        logger.trace("Scheduling a key-frame, because we received an RTCP PLI or FIR.");
                    }
                    keyFrameRequest();
                    return;
                default:
                    return;
            }
        }
    }

    public void setAdditionalCodecSettings(Map<String, String> additionalCodecSettings) {
        this.additionalCodecSettings = additionalCodecSettings;
    }

    public Format setInputFormat(Format format) {
        if (!(format instanceof VideoFormat) || AbstractCodec2.matches(format, this.inputFormats) == null) {
            return null;
        }
        YUVFormat yuvFormat = (YUVFormat) format;
        if (yuvFormat.getOffsetU() > yuvFormat.getOffsetV()) {
            return null;
        }
        this.inputFormat = AbstractCodec2.specialize(yuvFormat, Format.byteArray);
        return this.inputFormat;
    }

    public void setKeyFrameControl(KeyFrameControl keyFrameControl) {
        if (this.keyFrameControl != keyFrameControl) {
            if (!(this.keyFrameControl == null || this.keyFrameRequestee == null)) {
                this.keyFrameControl.removeKeyFrameRequestee(this.keyFrameRequestee);
            }
            this.keyFrameControl = keyFrameControl;
            if (this.keyFrameControl != null && this.keyFrameRequestee != null) {
                this.keyFrameControl.addKeyFrameRequestee(-1, this.keyFrameRequestee);
            }
        }
    }

    public Format setOutputFormat(Format format) {
        if (!(format instanceof VideoFormat) || AbstractCodec2.matches(format, getMatchingOutputFormats(this.inputFormat)) == null) {
            return null;
        }
        VideoFormat videoFormat = (VideoFormat) format;
        Dimension size = null;
        if (this.inputFormat != null) {
            size = ((VideoFormat) this.inputFormat).getSize();
        }
        if (size == null && format.matches(this.outputFormat)) {
            size = ((VideoFormat) this.outputFormat).getSize();
        }
        Map<String, String> fmtps = null;
        if (format instanceof ParameterizedVideoFormat) {
            fmtps = ((ParameterizedVideoFormat) format).getFormatParameters();
        }
        if (fmtps == null) {
            fmtps = new HashMap();
        }
        if (this.packetizationMode != null) {
            fmtps.put(PACKETIZATION_MODE_FMTP, this.packetizationMode);
        }
        this.outputFormat = new ParameterizedVideoFormat(videoFormat.getEncoding(), size, -1, Format.byteArray, videoFormat.getFrameRate(), fmtps);
        return this.outputFormat;
    }

    public void setPacketizationMode(String packetizationMode) {
        if (packetizationMode == null || "0".equals(packetizationMode)) {
            this.packetizationMode = "0";
        } else if ("1".equals(packetizationMode)) {
            this.packetizationMode = "1";
        } else {
            throw new IllegalArgumentException("packetizationMode");
        }
    }
}
