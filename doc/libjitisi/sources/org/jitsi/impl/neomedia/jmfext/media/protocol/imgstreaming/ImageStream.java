package org.jitsi.impl.neomedia.jmfext.media.protocol.imgstreaming;

import java.io.IOException;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.control.FormatControl;
import javax.media.format.VideoFormat;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.image.BufferedImage;
import org.jitsi.impl.neomedia.codec.video.AVFrame;
import org.jitsi.impl.neomedia.codec.video.AVFrameFormat;
import org.jitsi.impl.neomedia.codec.video.ByteBuffer;
import org.jitsi.impl.neomedia.imgstreaming.DesktopInteract;
import org.jitsi.impl.neomedia.imgstreaming.DesktopInteractImpl;
import org.jitsi.impl.neomedia.imgstreaming.ImgStreamingUtils;
import org.jitsi.impl.neomedia.jmfext.media.protocol.AbstractVideoPullBufferStream;
import org.jitsi.impl.neomedia.jmfext.media.protocol.ByteBufferPool;
import org.jitsi.util.Logger;

public class ImageStream extends AbstractVideoPullBufferStream<DataSource> {
    private static final Logger logger = Logger.getLogger(ImageStream.class);
    private final ByteBufferPool byteBufferPool = new ByteBufferPool();
    private DesktopInteract desktopInteract = null;
    private int displayIndex = -1;
    private long seqNo = 0;
    private int x = 0;
    private int y = 0;

    ImageStream(DataSource dataSource, FormatControl formatControl) {
        super(dataSource, formatControl);
    }

    /* access modifiers changed from: protected */
    public void doRead(Buffer buffer) throws IOException {
        Format format = buffer.getFormat();
        if (format == null) {
            format = getFormat();
            if (format != null) {
                buffer.setFormat(format);
            }
        }
        if (format instanceof AVFrameFormat) {
            AVFrame frame;
            AVFrame o = buffer.getData();
            if (o instanceof AVFrame) {
                frame = o;
            } else {
                frame = new AVFrame();
                buffer.setData(frame);
            }
            AVFrameFormat avFrameFormat = (AVFrameFormat) format;
            ByteBuffer data = readScreenNative(avFrameFormat.getSize());
            if (data == null) {
                throw new IOException("Failed to grab screen.");
            } else if (frame.avpicture_fill(data, avFrameFormat) < 0) {
                data.free();
                throw new IOException("avpicture_fill");
            }
        }
        byte[] bytes = readScreen((byte[]) buffer.getData(), ((VideoFormat) format).getSize());
        buffer.setData(bytes);
        buffer.setOffset(0);
        buffer.setLength(bytes.length);
        buffer.setHeader(null);
        buffer.setTimeStamp(System.nanoTime());
        buffer.setSequenceNumber(this.seqNo);
        buffer.setFlags(32896);
        this.seqNo++;
    }

    public byte[] readScreen(byte[] output, Dimension dim) {
        Dimension formatSize = ((VideoFormat) getFormat()).getSize();
        int width = formatSize.width;
        int height = formatSize.height;
        byte[] data = null;
        int size = (width * height) * 4;
        if (output == null || output.length < size) {
            output = new byte[size];
        }
        byte[] data2;
        if (this.desktopInteract.captureScreen(this.displayIndex, this.x, this.y, dim.width, dim.height, output)) {
            data2 = null;
            return output;
        }
        System.out.println("failed to grab with native! " + output.length);
        BufferedImage screen = this.desktopInteract.captureScreen();
        if (screen != null) {
            data = ImgStreamingUtils.getImageBytes(ImgStreamingUtils.getScaledImage(screen, width, height, 2), output);
        }
        data2 = data;
        return data;
    }

    private ByteBuffer readScreenNative(Dimension dim) {
        boolean b;
        int size = ((dim.width * dim.height) * 4) + 8;
        ByteBuffer data = this.byteBufferPool.getBuffer(size);
        data.setLength(size);
        try {
            b = this.desktopInteract.captureScreen(this.displayIndex, this.x, this.y, dim.width, dim.height, data.getPtr(), data.getLength());
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                ThreadDeath t2 = (ThreadDeath) t;
            } else {
                b = false;
            }
        }
        if (b) {
            return data;
        }
        data.free();
        return null;
    }

    public void setDisplayIndex(int displayIndex) {
        this.displayIndex = displayIndex;
    }

    public void setOrigin(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void start() throws IOException {
        super.start();
        if (this.desktopInteract == null) {
            try {
                this.desktopInteract = new DesktopInteractImpl();
            } catch (Exception e) {
                logger.warn("Cannot create DesktopInteract object!");
            }
        }
    }

    public void stop() throws IOException {
        try {
            if (logger.isInfoEnabled()) {
                logger.info("Stop stream");
            }
            super.stop();
            this.byteBufferPool.drain();
        } catch (Throwable th) {
            super.stop();
            this.byteBufferPool.drain();
        }
    }
}
