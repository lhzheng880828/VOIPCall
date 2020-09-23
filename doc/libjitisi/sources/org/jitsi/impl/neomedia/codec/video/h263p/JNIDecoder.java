package org.jitsi.impl.neomedia.codec.video.h263p;

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

public class JNIDecoder extends AbstractCodec {
    private static final VideoFormat[] DEFAULT_OUTPUT_FORMATS = new VideoFormat[]{new AVFrameFormat(0)};
    private static final String PLUGIN_NAME = "H.263+ Decoder";
    private long avcontext;
    private long avframe;
    private final boolean[] got_picture;
    private int height;
    private final VideoFormat[] outputFormats;
    private int width;

    public JNIDecoder() {
        this.avcontext = 0;
        this.avframe = 0;
        this.got_picture = new boolean[1];
        this.height = 0;
        this.width = 0;
        this.inputFormats = new VideoFormat[]{new VideoFormat(Constants.H263P)};
        this.outputFormats = DEFAULT_OUTPUT_FORMATS;
    }

    public boolean checkFormat(Format format) {
        return format.getEncoding().equals("h263-1998/rtp");
    }

    public synchronized void close() {
        if (this.opened) {
            this.opened = false;
            super.close();
            FFmpeg.avcodec_close(this.avcontext);
            FFmpeg.av_free(this.avcontext);
            this.avcontext = 0;
            FFmpeg.avcodec_free_frame(this.avframe);
            this.avframe = 0;
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
            long avcodec = FFmpeg.avcodec_find_decoder(5);
            this.avcontext = FFmpeg.avcodec_alloc_context3(avcodec);
            FFmpeg.avcodeccontext_set_workaround_bugs(this.avcontext, 1);
            if (FFmpeg.avcodec_open2(this.avcontext, avcodec, new String[0]) < 0) {
                throw new RuntimeException("Could not open codec CODEC_ID_H263");
            }
            this.avframe = FFmpeg.avcodec_alloc_frame();
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
            FFmpeg.avcodec_decode_video(this.avcontext, this.avframe, this.got_picture, (byte[]) in.getData(), in.getLength());
            if (this.got_picture[0]) {
                int width = FFmpeg.avcodeccontext_get_width(this.avcontext);
                int height = FFmpeg.avcodeccontext_get_height(this.avcontext);
                if (width > 0 && height > 0 && !(this.width == width && this.height == height)) {
                    this.width = width;
                    this.height = height;
                    this.outputFormat = new AVFrameFormat(new Dimension(this.width, this.height), ensureFrameRate(((VideoFormat) in.getFormat()).getFrameRate()), 0);
                }
                out.setFormat(this.outputFormat);
                Object outData = out.getData();
                if (!((outData instanceof AVFrame) && ((AVFrame) outData).getPtr() == this.avframe)) {
                    out.setData(new AVFrame(this.avframe));
                }
                if (Long.MIN_VALUE == Long.MIN_VALUE) {
                    out.setTimeStamp(-1);
                } else {
                    out.setTimeStamp(Long.MIN_VALUE);
                    out.setFlags((out.getFlags() | 256) & -4225);
                }
                i = 0;
            } else {
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
}
