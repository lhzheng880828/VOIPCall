package org.jitsi.impl.neomedia.codec.video;

import javax.media.Buffer;
import javax.media.Effect;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.impl.neomedia.codec.AbstractCodec2;
import org.jitsi.impl.neomedia.codec.FFmpeg;
import org.jitsi.util.Logger;

public class HFlip extends AbstractCodec2 implements Effect {
    private static final Format[] SUPPORTED_FORMATS = new Format[]{new AVFrameFormat()};
    private static final String VSINK_FFSINK_NAME = "nullsink";
    private static final String VSRC_BUFFER_NAME = "buffer";
    private static final Logger logger = Logger.getLogger(HFlip.class);
    private long buffer;
    private long ffsink;
    private long graph = 0;
    private boolean graphIsPending = true;
    private int height;
    private long outputFilterBufferRef;
    private long outputFrame;
    private int pixFmt = -1;
    private int width;

    public HFlip() {
        super("FFmpeg HFlip Filter", AVFrameFormat.class, SUPPORTED_FORMATS);
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void doClose() {
        try {
            if (this.outputFrame != 0) {
                FFmpeg.avcodec_free_frame(this.outputFrame);
                this.outputFrame = 0;
            }
            reset();
        } catch (Throwable th) {
            reset();
        }
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void doOpen() throws ResourceUnavailableException {
        this.outputFrame = FFmpeg.avcodec_alloc_frame();
        if (this.outputFrame == 0) {
            String reason = "avcodec_alloc_frame: " + this.outputFrame;
            logger.error(reason);
            throw new ResourceUnavailableException(reason);
        }
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized int doProcess(Buffer inputBuffer, Buffer outputBuffer) {
        int i;
        if (this.outputFilterBufferRef != 0) {
            FFmpeg.avfilter_unref_buffer(this.outputFilterBufferRef);
            this.outputFilterBufferRef = 0;
        }
        Format format = (AVFrameFormat) inputBuffer.getFormat();
        Dimension size = format.getSize();
        int pixFmt = format.getPixFmt();
        if (!(this.width == size.width && this.height == size.height && this.pixFmt == pixFmt)) {
            reset();
        }
        if (this.graph == 0) {
            String errorReason = null;
            int error = 0;
            long buffer = 0;
            long ffsink = 0;
            if (this.graphIsPending) {
                this.graphIsPending = false;
                this.graph = FFmpeg.avfilter_graph_alloc();
                if (this.graph == 0) {
                    errorReason = "avfilter_graph_alloc";
                } else {
                    error = FFmpeg.avfilter_graph_parse(this.graph, "buffer=" + size.width + ":" + size.height + ":" + pixFmt + ":1:1000000:1:1,hflip," + VSINK_FFSINK_NAME, 0, 0, 0);
                    if (error == 0) {
                        String parsedFilterNameFormat = "Parsed_%2$s_%1$d";
                        String parsedFilterName = String.format(parsedFilterNameFormat, new Object[]{Integer.valueOf(0), VSRC_BUFFER_NAME});
                        buffer = FFmpeg.avfilter_graph_get_filter(this.graph, parsedFilterName);
                        if (buffer == 0) {
                            errorReason = "avfilter_graph_get_filter: buffer/" + parsedFilterName;
                        } else {
                            parsedFilterName = String.format(parsedFilterNameFormat, new Object[]{Integer.valueOf(2), VSINK_FFSINK_NAME});
                            ffsink = FFmpeg.avfilter_graph_get_filter(this.graph, parsedFilterName);
                            if (ffsink == 0) {
                                errorReason = "avfilter_graph_get_filter: nullsink/" + parsedFilterName;
                            } else {
                                error = FFmpeg.avfilter_graph_config(this.graph, 0);
                                if (error != 0) {
                                    errorReason = "avfilter_graph_config";
                                }
                            }
                        }
                    } else {
                        errorReason = "avfilter_graph_parse";
                    }
                    if (!(errorReason == null && error == 0)) {
                        FFmpeg.avfilter_graph_free(this.graph);
                        this.graph = 0;
                    }
                }
            }
            if (this.graph == 0) {
                if (errorReason != null) {
                    StringBuilder stringBuilder = new StringBuilder(errorReason);
                    if (error != 0) {
                        stringBuilder.append(": ").append(error);
                    }
                    stringBuilder.append(", format ").append(format);
                    logger.error(stringBuilder);
                }
                i = 1;
            } else {
                this.width = size.width;
                this.height = size.height;
                this.pixFmt = pixFmt;
                this.buffer = buffer;
                this.ffsink = ffsink;
            }
        }
        this.outputFilterBufferRef = FFmpeg.get_filtered_video_frame(((AVFrame) inputBuffer.getData()).getPtr(), this.width, this.height, this.pixFmt, this.buffer, this.ffsink, this.outputFrame);
        if (this.outputFilterBufferRef == 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("get_filtered_video_frame");
            }
            i = 1;
        } else {
            Object out = outputBuffer.getData();
            if (!((out instanceof AVFrame) && ((AVFrame) out).getPtr() == this.outputFrame)) {
                outputBuffer.setData(new AVFrame(this.outputFrame));
            }
            outputBuffer.setDiscard(inputBuffer.isDiscard());
            outputBuffer.setDuration(inputBuffer.getDuration());
            outputBuffer.setEOM(inputBuffer.isEOM());
            outputBuffer.setFlags(inputBuffer.getFlags());
            outputBuffer.setFormat(format);
            outputBuffer.setHeader(inputBuffer.getHeader());
            outputBuffer.setLength(inputBuffer.getLength());
            outputBuffer.setSequenceNumber(inputBuffer.getSequenceNumber());
            outputBuffer.setTimeStamp(inputBuffer.getTimeStamp());
            i = 0;
        }
        return i;
    }

    public synchronized void reset() {
        if (this.outputFilterBufferRef != 0) {
            FFmpeg.avfilter_unref_buffer(this.outputFilterBufferRef);
            this.outputFilterBufferRef = 0;
        }
        if (this.graph != 0) {
            FFmpeg.avfilter_graph_free(this.graph);
            this.graph = 0;
            this.graphIsPending = true;
            this.width = 0;
            this.height = 0;
            this.pixFmt = -1;
            this.buffer = 0;
            this.ffsink = 0;
        }
    }
}
