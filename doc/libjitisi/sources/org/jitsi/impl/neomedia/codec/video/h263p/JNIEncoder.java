package org.jitsi.impl.neomedia.codec.video.h263p;

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
import org.jitsi.service.neomedia.codec.Constants;

public class JNIEncoder extends AbstractCodec {
    private static final int DEFAULT_FRAME_RATE = 30;
    private static final Format[] DEFAULT_OUTPUT_FORMATS = new Format[]{new VideoFormat(Constants.H263P)};
    private static final int IFRAME_INTERVAL = 300;
    private static final String PLUGIN_NAME = "H.263+ Encoder";
    private long avFrame;
    private long avcontext;
    private int encFrameLen;
    private int framesSinceLastIFrame;
    private long rawFrameBuffer;

    public JNIEncoder() {
        this.avcontext = 0;
        this.avFrame = 0;
        this.encFrameLen = 0;
        this.framesSinceLastIFrame = 301;
        this.rawFrameBuffer = 0;
        this.inputFormats = new Format[]{new YUVFormat(null, -1, Format.byteArray, -1.0f, 2, -1, -1, -1, -1, -1)};
        this.inputFormat = null;
        this.outputFormat = null;
    }

    public synchronized void close() {
        if (this.opened) {
            this.opened = false;
            super.close();
            FFmpeg.avcodec_close(this.avcontext);
            FFmpeg.av_free(this.avcontext);
            this.avcontext = 0;
            FFmpeg.avcodec_free_frame(this.avFrame);
            this.avFrame = 0;
            FFmpeg.av_free(this.rawFrameBuffer);
            this.rawFrameBuffer = 0;
        }
    }

    private Format[] getMatchingOutputFormats(Format in) {
        VideoFormat videoIn = (VideoFormat) in;
        return new VideoFormat[]{new VideoFormat(Constants.H263P, videoIn.getSize(), -1, Format.byteArray, videoIn.getFrameRate())};
    }

    public String getName() {
        return PLUGIN_NAME;
    }

    public Format[] getSupportedOutputFormats(Format in) {
        if (in == null) {
            return DEFAULT_OUTPUT_FORMATS;
        }
        if (!(in instanceof VideoFormat) || AbstractCodec2.matches(in, this.inputFormats) == null) {
            return new Format[0];
        }
        return getMatchingOutputFormats(in);
    }

    public synchronized void open() throws ResourceUnavailableException {
        if (!this.opened) {
            if (this.inputFormat == null) {
                throw new ResourceUnavailableException("No input format selected");
            } else if (this.outputFormat == null) {
                throw new ResourceUnavailableException("No output format selected");
            } else {
                VideoFormat outputVideoFormat = this.outputFormat;
                Dimension size = outputVideoFormat.getSize();
                int width = size.width;
                int height = size.height;
                long avcodec = FFmpeg.avcodec_find_encoder(20);
                this.avcontext = FFmpeg.avcodec_alloc_context3(avcodec);
                FFmpeg.avcodeccontext_set_pix_fmt(this.avcontext, 0);
                FFmpeg.avcodeccontext_set_size(this.avcontext, width, height);
                FFmpeg.avcodeccontext_set_qcompress(this.avcontext, 0.6f);
                int bitRate = NeomediaServiceUtils.getMediaServiceImpl().getDeviceConfiguration().getVideoBitrate() * 1000;
                int frameRate = (int) outputVideoFormat.getFrameRate();
                if (frameRate == -1) {
                    frameRate = DEFAULT_FRAME_RATE;
                }
                FFmpeg.avcodeccontext_set_bit_rate(this.avcontext, bitRate);
                FFmpeg.avcodeccontext_set_bit_rate_tolerance(this.avcontext, bitRate / (frameRate - 1));
                FFmpeg.avcodeccontext_set_time_base(this.avcontext, 1, frameRate);
                FFmpeg.avcodeccontext_set_mb_decision(this.avcontext, 0);
                FFmpeg.avcodeccontext_add_flags(this.avcontext, 2048);
                FFmpeg.avcodeccontext_add_flags(this.avcontext, 33554432);
                FFmpeg.avcodeccontext_add_flags(this.avcontext, 16777216);
                FFmpeg.avcodeccontext_add_flags(this.avcontext, FFmpeg.CODEC_FLAG_H263P_SLICE_STRUCT);
                FFmpeg.avcodeccontext_set_me_method(this.avcontext, 6);
                FFmpeg.avcodeccontext_set_me_subpel_quality(this.avcontext, 2);
                FFmpeg.avcodeccontext_set_me_range(this.avcontext, 18);
                FFmpeg.avcodeccontext_set_me_cmp(this.avcontext, 256);
                FFmpeg.avcodeccontext_set_scenechange_threshold(this.avcontext, 40);
                FFmpeg.avcodeccontext_set_gop_size(this.avcontext, 300);
                if (FFmpeg.avcodec_open2(this.avcontext, avcodec, new String[0]) < 0) {
                    throw new ResourceUnavailableException("Could not open codec. (size= " + width + "x" + height + ")");
                }
                this.encFrameLen = ((width * height) * 3) / 2;
                this.rawFrameBuffer = FFmpeg.av_malloc(this.encFrameLen);
                this.avFrame = FFmpeg.avcodec_alloc_frame();
                int sizeInBytes = width * height;
                FFmpeg.avframe_set_data(this.avFrame, this.rawFrameBuffer, (long) sizeInBytes, (long) (sizeInBytes / 4));
                FFmpeg.avframe_set_linesize(this.avFrame, width, width / 2, width / 2);
                this.opened = true;
                super.open();
            }
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
            if (!(inFormat == this.inputFormat || inFormat.matches(this.inputFormat))) {
                setInputFormat(inFormat);
            }
            if (inBuffer.getLength() < 3) {
                outBuffer.setDiscard(true);
                reset();
            } else {
                byte[] out;
                FFmpeg.memcpy(this.rawFrameBuffer, (byte[]) inBuffer.getData(), inBuffer.getOffset(), this.encFrameLen);
                if (this.framesSinceLastIFrame >= 300) {
                    FFmpeg.avframe_set_key_frame(this.avFrame, true);
                    this.framesSinceLastIFrame = 0;
                } else {
                    this.framesSinceLastIFrame++;
                    FFmpeg.avframe_set_key_frame(this.avFrame, false);
                }
                Object outData = outBuffer.getData();
                if (outData instanceof byte[]) {
                    out = (byte[]) outData;
                    if (out.length < this.encFrameLen) {
                        out = null;
                    }
                } else {
                    out = null;
                }
                if (out == null) {
                    out = new byte[this.encFrameLen];
                }
                int outputLength = FFmpeg.avcodec_encode_video(this.avcontext, out, out.length, this.avFrame);
                outBuffer.setData(out);
                outBuffer.setLength(outputLength);
                outBuffer.setOffset(0);
                outBuffer.setTimeStamp(inBuffer.getTimeStamp());
            }
        }
        return 0;
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
