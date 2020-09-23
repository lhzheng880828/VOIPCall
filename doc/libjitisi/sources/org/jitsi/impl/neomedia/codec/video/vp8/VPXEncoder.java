package org.jitsi.impl.neomedia.codec.video.vp8;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.VideoFormat;
import javax.media.format.YUVFormat;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.impl.neomedia.NeomediaServiceUtils;
import org.jitsi.impl.neomedia.codec.AbstractCodec2;
import org.jitsi.impl.neomedia.codec.video.VPX;
import org.jitsi.service.neomedia.codec.Constants;
import org.jitsi.util.Logger;

public class VPXEncoder extends AbstractCodec2 {
    private static final int DEFAULT_HEIGHT = 480;
    private static final int DEFAULT_WIDTH = 640;
    private static final int INTERFACE = 1;
    private static final VideoFormat[] SUPPORTED_OUTPUT_FORMATS = new VideoFormat[]{new VideoFormat(Constants.VP8)};
    private static final Logger logger = Logger.getLogger(VPXEncoder.class);
    private long cfg;
    private long context;
    private long flags;
    private long frameCount;
    private int height;
    private long img;
    private long[] iter;
    private boolean leftoverPackets;
    private long pkt;
    private int width;

    public VPXEncoder() {
        super("VP8 Encoder", VideoFormat.class, SUPPORTED_OUTPUT_FORMATS);
        this.cfg = 0;
        this.context = 0;
        this.flags = 0;
        this.frameCount = 0;
        this.img = 0;
        this.iter = new long[1];
        this.leftoverPackets = false;
        this.pkt = 0;
        this.width = 640;
        this.height = 480;
        this.inputFormats = new VideoFormat[]{new YUVFormat(null, -1, Format.byteArray, -1.0f, 2, -1, -1, -1, -1, -1)};
        this.inputFormat = null;
        this.outputFormat = null;
    }

    /* access modifiers changed from: protected */
    public void doClose() {
        if (logger.isDebugEnabled()) {
            logger.debug("Closing encoder");
        }
        if (this.context != 0) {
            VPX.codec_destroy(this.context);
            VPX.free(this.context);
            this.context = 0;
        }
        if (this.img != 0) {
            VPX.free(this.img);
            this.img = 0;
        }
        if (this.cfg != 0) {
            VPX.free(this.cfg);
            this.cfg = 0;
        }
    }

    /* access modifiers changed from: protected */
    public void doOpen() throws ResourceUnavailableException {
        this.img = VPX.img_malloc();
        if (this.img == 0) {
            throw new RuntimeException("Could not img_malloc()");
        }
        VPX.img_set_fmt(this.img, 258);
        VPX.img_set_bps(this.img, 12);
        VPX.img_set_w(this.img, this.width);
        VPX.img_set_d_w(this.img, this.width);
        VPX.img_set_h(this.img, this.height);
        VPX.img_set_d_h(this.img, this.height);
        this.cfg = VPX.codec_enc_cfg_malloc();
        if (this.cfg == 0) {
            throw new RuntimeException("Could not codec_enc_cfg_malloc()");
        }
        VPX.codec_enc_config_default(1, this.cfg, 0);
        VPX.codec_enc_cfg_set_rc_target_bitrate(this.cfg, NeomediaServiceUtils.getMediaServiceImpl().getDeviceConfiguration().getVideoBitrate());
        VPX.codec_enc_cfg_set_rc_resize_allowed(this.cfg, 1);
        VPX.codec_enc_cfg_set_rc_end_usage(this.cfg, 1);
        VPX.codec_enc_cfg_set_kf_mode(this.cfg, 1);
        VPX.codec_enc_cfg_set_w(this.cfg, this.width);
        VPX.codec_enc_cfg_set_h(this.cfg, this.height);
        VPX.codec_enc_cfg_set_error_resilient(this.cfg, 3);
        this.context = VPX.codec_ctx_malloc();
        int ret = VPX.codec_enc_init(this.context, 1, this.cfg, this.flags);
        if (ret != 0) {
            throw new RuntimeException("Failed to initialize encoder, libvpx error:\n" + VPX.codec_err_to_string(ret));
        } else if (this.inputFormat == null) {
            throw new ResourceUnavailableException("No input format selected");
        } else if (this.outputFormat == null) {
            throw new ResourceUnavailableException("No output format selected");
        } else if (logger.isDebugEnabled()) {
            logger.debug("VP8 encoder opened succesfully");
        }
    }

    private void updateSize(int w, int h) {
        if (logger.isInfoEnabled()) {
            logger.info("Setting new width/height: " + w + "/" + h);
        }
        this.width = w;
        this.height = h;
        if (this.img != 0) {
            VPX.img_set_w(this.img, w);
            VPX.img_set_d_w(this.img, w);
            VPX.img_set_h(this.img, h);
            VPX.img_set_d_h(this.img, h);
        }
        if (this.cfg != 0) {
            VPX.codec_enc_cfg_set_w(this.cfg, w);
            VPX.codec_enc_cfg_set_h(this.cfg, h);
            reinit();
        }
    }

    /* access modifiers changed from: protected */
    public int doProcess(Buffer inputBuffer, Buffer outputBuffer) {
        if (inputBuffer.isDiscard()) {
            outputBuffer.setDiscard(true);
            return 0;
        }
        int ret = 0;
        int size;
        if (!this.leftoverPackets) {
            this.frameCount++;
            YUVFormat format = (YUVFormat) inputBuffer.getFormat();
            Dimension formatSize = format.getSize();
            int width = formatSize.width;
            int height = formatSize.height;
            if (width > 0 && height > 0 && !(width == this.width && height == this.height)) {
                updateSize(width, height);
            }
            int strideY = format.getStrideY();
            if (strideY == -1) {
                strideY = width;
            }
            int strideUV = format.getStrideUV();
            if (strideUV == -1) {
                strideUV = width / 2;
            }
            VPX.img_set_stride0(this.img, strideY);
            VPX.img_set_stride1(this.img, strideUV);
            VPX.img_set_stride2(this.img, strideUV);
            VPX.img_set_stride3(this.img, 0);
            int offsetY = format.getOffsetY();
            if (offsetY == -1) {
                offsetY = 0;
            }
            int offsetU = format.getOffsetU();
            if (offsetU == -1) {
                offsetU = offsetY + (width * height);
            }
            int offsetV = format.getOffsetV();
            if (offsetV == -1) {
                offsetV = offsetU + ((width * height) / 4);
            }
            int result = VPX.codec_encode(this.context, this.img, (byte[]) inputBuffer.getData(), offsetY, offsetU, offsetV, this.frameCount, 1, 0, 1);
            if (result != 0) {
                logger.warn("Failed to encode a frame: " + VPX.codec_err_to_string(result));
                outputBuffer.setDiscard(true);
                return 0;
            }
            this.iter[0] = 0;
            this.pkt = VPX.codec_get_cx_data(this.context, this.iter);
            if (this.pkt == 0 || VPX.codec_cx_pkt_get_kind(this.pkt) != 0) {
                ret = 0 | 4;
            } else {
                size = VPX.codec_cx_pkt_get_size(this.pkt);
                VPX.memcpy(AbstractCodec2.validateByteArraySize(outputBuffer, size, false), VPX.codec_cx_pkt_get_data(this.pkt), size);
                outputBuffer.setOffset(0);
                outputBuffer.setLength(size);
                outputBuffer.setTimeStamp(inputBuffer.getTimeStamp());
            }
        } else if (VPX.codec_cx_pkt_get_kind(this.pkt) == 0) {
            size = VPX.codec_cx_pkt_get_size(this.pkt);
            VPX.memcpy(AbstractCodec2.validateByteArraySize(inputBuffer, size, false), VPX.codec_cx_pkt_get_data(this.pkt), size);
            outputBuffer.setOffset(0);
            outputBuffer.setLength(size);
            outputBuffer.setTimeStamp(inputBuffer.getTimeStamp());
        } else {
            ret = 0 | 4;
        }
        this.pkt = VPX.codec_get_cx_data(this.context, this.iter);
        this.leftoverPackets = this.pkt != 0;
        if (this.leftoverPackets) {
            return ret | 2;
        }
        return ret;
    }

    /* access modifiers changed from: protected */
    public Format[] getMatchingOutputFormats(Format inputFormat) {
        VideoFormat inputVideoFormat = (VideoFormat) inputFormat;
        return new VideoFormat[]{new VideoFormat(Constants.VP8, inputVideoFormat.getSize(), -1, Format.byteArray, inputVideoFormat.getFrameRate())};
    }

    private void reinit() {
        if (this.context != 0) {
            VPX.codec_destroy(this.context);
        }
        int ret = VPX.codec_enc_init(this.context, 1, this.cfg, this.flags);
        if (ret != 0) {
            throw new RuntimeException("Failed to re-initialize encoder, libvpx error:\n" + VPX.codec_err_to_string(ret));
        }
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
        this.outputFormat = new VideoFormat(videoFormat.getEncoding(), size, -1, Format.byteArray, videoFormat.getFrameRate());
        return this.outputFormat;
    }
}
