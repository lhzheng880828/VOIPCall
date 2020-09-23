package org.jitsi.impl.neomedia.codec.video.vp8;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.VideoFormat;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.impl.neomedia.codec.AbstractCodec2;
import org.jitsi.impl.neomedia.codec.FFmpeg;
import org.jitsi.impl.neomedia.codec.video.AVFrame;
import org.jitsi.impl.neomedia.codec.video.AVFrameFormat;
import org.jitsi.impl.neomedia.codec.video.VPX;
import org.jitsi.service.neomedia.codec.Constants;
import org.jitsi.util.Logger;

public class VPXDecoder extends AbstractCodec2 {
    private static final int INTERFACE = 0;
    private static final VideoFormat[] SUPPORTED_OUTPUT_FORMATS = new VideoFormat[]{new AVFrameFormat(0)};
    private static final Logger logger = Logger.getLogger(VPXDecoder.class);
    private long cfg;
    private long context;
    private int height;
    private long img;
    private long[] iter;
    private boolean leftoverFrames;
    private int width;

    public VPXDecoder() {
        super("VP8 VPX Decoder", VideoFormat.class, SUPPORTED_OUTPUT_FORMATS);
        this.cfg = 0;
        this.context = 0;
        this.img = 0;
        this.iter = new long[1];
        this.leftoverFrames = false;
        this.inputFormat = null;
        this.outputFormat = null;
        this.inputFormats = new VideoFormat[]{new VideoFormat(Constants.VP8)};
    }

    /* access modifiers changed from: protected */
    public void doClose() {
        if (logger.isDebugEnabled()) {
            logger.debug("Closing decoder");
        }
        if (this.context != 0) {
            VPX.codec_destroy(this.context);
            VPX.free(this.context);
        }
        if (this.cfg != 0) {
            VPX.free(this.cfg);
        }
    }

    /* access modifiers changed from: protected */
    public void doOpen() throws ResourceUnavailableException {
        this.context = VPX.codec_ctx_malloc();
        int ret = VPX.codec_dec_init(this.context, 0, 0, 0);
        if (ret != 0) {
            throw new RuntimeException("Failed to initialize decoder, libvpx error:\n" + VPX.codec_err_to_string(ret));
        } else if (logger.isDebugEnabled()) {
            logger.debug("VP8 decoder opened succesfully");
        }
    }

    /* access modifiers changed from: protected */
    public int doProcess(Buffer inputBuffer, Buffer outputBuffer) {
        if (this.leftoverFrames) {
            updateOutputFormat(VPX.img_get_d_w(this.img), VPX.img_get_d_h(this.img), ((VideoFormat) inputBuffer.getFormat()).getFrameRate());
            outputBuffer.setFormat(this.outputFormat);
            outputBuffer.setData(makeAVFrame(this.img));
            outputBuffer.setLength(((this.width * this.height) * 3) / 2);
            outputBuffer.setTimeStamp(-1);
        } else {
            int ret = VPX.codec_decode(this.context, (byte[]) inputBuffer.getData(), inputBuffer.getOffset(), inputBuffer.getLength(), 0, 0);
            if (ret != 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Discarding a frame, codec_decode() error: " + VPX.codec_err_to_string(ret));
                }
                outputBuffer.setDiscard(true);
                return 0;
            }
            this.leftoverFrames = false;
            this.iter[0] = 0;
            this.img = VPX.codec_get_frame(this.context, this.iter);
            if (this.img == 0) {
                outputBuffer.setDiscard(true);
                return 0;
            }
            updateOutputFormat(VPX.img_get_d_w(this.img), VPX.img_get_d_h(this.img), ((VideoFormat) inputBuffer.getFormat()).getFrameRate());
            outputBuffer.setFormat(this.outputFormat);
            outputBuffer.setData(makeAVFrame(this.img));
            outputBuffer.setLength(((this.width * this.height) * 3) / 2);
            outputBuffer.setTimeStamp(-1);
        }
        this.img = VPX.codec_get_frame(this.context, this.iter);
        if (this.img == 0) {
            this.leftoverFrames = false;
            return 0;
        }
        this.leftoverFrames = true;
        return 2;
    }

    /* access modifiers changed from: protected */
    public Format[] getMatchingOutputFormats(Format inputFormat) {
        VideoFormat inputVideoFormat = (VideoFormat) inputFormat;
        return new Format[]{new AVFrameFormat(inputVideoFormat.getSize(), inputVideoFormat.getFrameRate(), 0)};
    }

    private AVFrame makeAVFrame(long img) {
        AVFrame avframe = new AVFrame();
        long p0 = VPX.img_get_plane0(img);
        long p1 = VPX.img_get_plane1(img);
        FFmpeg.avframe_set_data(avframe.getPtr(), p0, p1 - p0, VPX.img_get_plane2(img) - p1);
        FFmpeg.avframe_set_linesize(avframe.getPtr(), VPX.img_get_stride0(img), VPX.img_get_stride1(img), VPX.img_get_stride2(img));
        return avframe;
    }

    public Format setInputFormat(Format format) {
        Format setFormat = super.setInputFormat(format);
        if (setFormat != null) {
            reset();
        }
        return setFormat;
    }

    private void updateOutputFormat(int width, int height, float frameRate) {
        if (width > 0 && height > 0) {
            if (this.width != width || this.height != height) {
                this.width = width;
                this.height = height;
                this.outputFormat = new AVFrameFormat(new Dimension(width, height), frameRate, 0);
            }
        }
    }
}
