package org.jitsi.impl.neomedia.codec.video.h264;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.VideoFormat;
import net.sf.fmj.media.AbstractCodec;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.impl.neomedia.codec.AbstractCodec2;
import org.jitsi.impl.neomedia.codec.FFmpeg;
import org.jitsi.impl.neomedia.codec.video.AVFrame;
import org.jitsi.impl.neomedia.codec.video.AVFrameFormat;
import org.jitsi.service.neomedia.codec.Constants;
import org.jitsi.service.neomedia.control.KeyFrameControl;

public class JNIDecoder extends AbstractCodec {
    private static final VideoFormat[] DEFAULT_OUTPUT_FORMATS = new VideoFormat[]{new AVFrameFormat(0)};
    private static final String PLUGIN_NAME = "H.264 Decoder";
    private long avctx;
    private AVFrame avframe;
    private boolean gotPictureAtLeastOnce;
    private final boolean[] got_picture;
    private int height;
    private KeyFrameControl keyFrameControl;
    private final VideoFormat[] outputFormats;
    private int width;

    public JNIDecoder() {
        this.got_picture = new boolean[1];
        this.inputFormats = new VideoFormat[]{new VideoFormat(Constants.H264)};
        this.outputFormats = DEFAULT_OUTPUT_FORMATS;
    }

    public boolean checkFormat(Format format) {
        return format.getEncoding().equals(Constants.H264_RTP);
    }

    public synchronized void close() {
        if (this.opened) {
            this.opened = false;
            super.close();
            FFmpeg.avcodec_close(this.avctx);
            FFmpeg.av_free(this.avctx);
            this.avctx = 0;
            if (this.avframe != null) {
                this.avframe.free();
                this.avframe = null;
            }
            this.gotPictureAtLeastOnce = false;
        }
    }

    private float ensureFrameRate(float frameRate) {
        return frameRate;
    }

    /* access modifiers changed from: protected */
    public Format[] getMatchingOutputFormats(Format inputFormat) {
        VideoFormat inputVideoFormat = (VideoFormat) inputFormat;
        return new Format[]{new AVFrameFormat(inputVideoFormat.getSize(), ensureFrameRate(inputVideoFormat.getFrameRate()), 0)};
    }

    public String getName() {
        return PLUGIN_NAME;
    }

    public Format[] getSupportedOutputFormats(Format inputFormat) {
        if (inputFormat == null) {
            return this.outputFormats;
        }
        if (!(inputFormat instanceof VideoFormat) || AbstractCodec2.matches(inputFormat, this.inputFormats) == null) {
            return new Format[0];
        }
        return getMatchingOutputFormats(inputFormat);
    }

    public synchronized void open() throws ResourceUnavailableException {
        if (!this.opened) {
            if (this.avframe != null) {
                this.avframe.free();
                this.avframe = null;
            }
            this.avframe = new AVFrame();
            long avcodec = FFmpeg.avcodec_find_decoder(28);
            this.avctx = FFmpeg.avcodec_alloc_context3(avcodec);
            FFmpeg.avcodeccontext_set_workaround_bugs(this.avctx, 1);
            FFmpeg.avcodeccontext_add_flags2(this.avctx, 32768);
            if (FFmpeg.avcodec_open2(this.avctx, avcodec, new String[0]) < 0) {
                throw new RuntimeException("Could not open codec CODEC_ID_H264");
            }
            this.gotPictureAtLeastOnce = false;
            this.opened = true;
            super.open();
        }
    }

    public synchronized int process(Buffer in, Buffer out) {
        int i;
        if (!checkInputBuffer(in)) {
            i = 1;
        } else if (isEOM(in) || !this.opened) {
            propagateEOM(out);
            i = 0;
        } else if (in.isDiscard()) {
            out.setDiscard(true);
            i = 0;
        } else {
            this.got_picture[0] = false;
            FFmpeg.avcodec_decode_video(this.avctx, this.avframe.getPtr(), this.got_picture, (byte[]) in.getData(), in.getLength());
            if (this.got_picture[0]) {
                this.gotPictureAtLeastOnce = true;
                int width = FFmpeg.avcodeccontext_get_width(this.avctx);
                int height = FFmpeg.avcodeccontext_get_height(this.avctx);
                if (width > 0 && height > 0 && !(this.width == width && this.height == height)) {
                    this.width = width;
                    this.height = height;
                    this.outputFormat = new AVFrameFormat(new Dimension(this.width, this.height), ensureFrameRate(((VideoFormat) in.getFormat()).getFrameRate()), 0);
                }
                out.setFormat(this.outputFormat);
                if (out.getData() != this.avframe) {
                    out.setData(this.avframe);
                }
                if (Long.MIN_VALUE == Long.MIN_VALUE) {
                    out.setTimeStamp(-1);
                } else {
                    out.setTimeStamp(Long.MIN_VALUE);
                    out.setFlags((out.getFlags() | 256) & -4225);
                }
                i = 0;
            } else {
                if (!((in.getFlags() & 2048) == 0 || this.keyFrameControl == null)) {
                    this.keyFrameControl.requestKeyFrame(!this.gotPictureAtLeastOnce);
                }
                out.setDiscard(true);
                i = 0;
            }
        }
        return i;
    }

    public Format setInputFormat(Format format) {
        Format setFormat = super.setInputFormat(format);
        if (setFormat != null) {
            reset();
        }
        return setFormat;
    }

    public void setKeyFrameControl(KeyFrameControl keyFrameControl) {
        this.keyFrameControl = keyFrameControl;
    }
}
