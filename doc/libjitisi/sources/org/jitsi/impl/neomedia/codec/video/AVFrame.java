package org.jitsi.impl.neomedia.codec.video;

import javax.media.Buffer;
import javax.media.Format;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.impl.neomedia.codec.FFmpeg;

public class AVFrame {
    private ByteBuffer data;
    private boolean free;
    private long ptr;

    public static int read(Buffer buffer, Format format, ByteBuffer data) {
        AVFrame frame;
        AVFrameFormat frameFormat = (AVFrameFormat) format;
        Object o = buffer.getData();
        if (o instanceof AVFrame) {
            frame = (AVFrame) o;
        } else {
            frame = new AVFrame();
            buffer.setData(frame);
        }
        return frame.avpicture_fill(data, frameFormat);
    }

    public AVFrame() {
        this.ptr = FFmpeg.avcodec_alloc_frame();
        if (this.ptr == 0) {
            throw new OutOfMemoryError("avcodec_alloc_frame()");
        }
        this.free = true;
    }

    public AVFrame(long ptr) {
        if (ptr == 0) {
            throw new IllegalArgumentException("ptr");
        }
        this.ptr = ptr;
        this.free = false;
    }

    public synchronized int avpicture_fill(ByteBuffer data, AVFrameFormat format) {
        int ret;
        Dimension size = format.getSize();
        ret = FFmpeg.avpicture_fill(this.ptr, data.getPtr(), format.getPixFmt(), size.width, size.height);
        if (ret >= 0) {
            if (this.data != null) {
                this.data.free();
            }
            this.data = data;
        }
        return ret;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            free();
        } finally {
            super.finalize();
        }
    }

    public synchronized void free() {
        if (this.free && this.ptr != 0) {
            FFmpeg.avcodec_free_frame(this.ptr);
            this.free = false;
            this.ptr = 0;
        }
        if (this.data != null) {
            this.data.free();
            this.data = null;
        }
    }

    public synchronized ByteBuffer getData() {
        return this.data;
    }

    public synchronized long getPtr() {
        return this.ptr;
    }
}
