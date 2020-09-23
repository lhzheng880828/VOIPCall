package org.jitsi.impl.neomedia.jmfext.media.protocol.directshow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.media.control.FormatControl;
import javax.media.control.FrameRateControl;
import javax.media.protocol.PushBufferStream;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.impl.neomedia.codec.FFmpeg;
import org.jitsi.impl.neomedia.codec.video.AVFrameFormat;
import org.jitsi.impl.neomedia.control.FrameRateControlAdapter;
import org.jitsi.impl.neomedia.device.DeviceSystem;
import org.jitsi.impl.neomedia.jmfext.media.protocol.AbstractVideoPushBufferCaptureDevice;
import org.jitsi.util.Logger;

public class DataSource extends AbstractVideoPushBufferCaptureDevice {
    private static final int[] DS_TO_FFMPEG_PIX_FMTS = new int[]{DSFormat.RGB24, FFmpeg.PIX_FMT_RGB24, DSFormat.RGB32, FFmpeg.PIX_FMT_RGB32, DSFormat.ARGB32, 27, DSFormat.YUY2, 1, DSFormat.MJPG, 13, DSFormat.UYVY, 17, DSFormat.Y411, 18, DSFormat.Y41P, 7, DSFormat.NV12, 25, DSFormat.I420, 0};
    private static final Logger logger = Logger.getLogger(DataSource.class);
    private DSCaptureDevice device;
    private DSManager manager;

    public static int getDSPixFmt(int ffmpegPixFmt) {
        for (int i = 0; i < DS_TO_FFMPEG_PIX_FMTS.length; i += 2) {
            if (DS_TO_FFMPEG_PIX_FMTS[i + 1] == ffmpegPixFmt) {
                return DS_TO_FFMPEG_PIX_FMTS[i];
            }
        }
        return -1;
    }

    public static int getFFmpegPixFmt(int dsPixFmt) {
        for (int i = 0; i < DS_TO_FFMPEG_PIX_FMTS.length; i += 2) {
            if (DS_TO_FFMPEG_PIX_FMTS[i] == dsPixFmt) {
                return DS_TO_FFMPEG_PIX_FMTS[i + 1];
            }
        }
        return -1;
    }

    public DataSource() {
        this(null);
    }

    public DataSource(MediaLocator locator) {
        super(locator);
    }

    /* access modifiers changed from: protected */
    public FrameRateControl createFrameRateControl() {
        return new FrameRateControlAdapter() {
            private float frameRate = -1.0f;

            public float getFrameRate() {
                return this.frameRate;
            }

            public float setFrameRate(float frameRate) {
                this.frameRate = frameRate;
                return this.frameRate;
            }
        };
    }

    /* access modifiers changed from: protected */
    public DirectShowStream createStream(int streamIndex, FormatControl formatControl) {
        DirectShowStream stream = new DirectShowStream(this, formatControl);
        if (logger.isTraceEnabled()) {
            DSCaptureDevice device = this.device;
            if (device != null) {
                for (DSFormat supportedFormat : device.getSupportedFormats()) {
                    logger.trace("width= " + supportedFormat.getWidth() + ", height= " + supportedFormat.getHeight() + ", pixelFormat= " + supportedFormat.getPixelFormat());
                }
            }
        }
        return stream;
    }

    /* access modifiers changed from: protected */
    public void doConnect() throws IOException {
        super.doConnect();
        boolean connected = false;
        try {
            DSCaptureDevice device = getDevice();
            device.connect();
            synchronized (getStreamSyncRoot()) {
                for (PushBufferStream stream : getStreams()) {
                    ((DirectShowStream) stream).setDevice(device);
                }
            }
            connected = true;
        } finally {
            if (!connected) {
                doDisconnect();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void doDisconnect() {
        try {
            synchronized (getStreamSyncRoot()) {
                for (PushBufferStream stream : getStreams()) {
                    try {
                        ((DirectShowStream) stream).setDevice(null);
                    } catch (IOException ioe) {
                        logger.error("Failed to disconnect " + stream.getClass().getName(), ioe);
                    }
                }
            }
        } finally {
            if (this.device != null) {
                this.device.disconnect();
                this.device = null;
            }
            if (this.manager != null) {
                this.manager.dispose();
                this.manager = null;
            }
            super.doDisconnect();
        }
    }

    private DSCaptureDevice getDevice() {
        DSCaptureDevice device = this.device;
        if (device == null) {
            MediaLocator locator = getLocator();
            if (locator == null) {
                throw new IllegalStateException("locator");
            } else if (locator.getProtocol().equalsIgnoreCase(DeviceSystem.LOCATOR_PROTOCOL_DIRECTSHOW)) {
                String remainder = locator.getRemainder();
                if (remainder == null) {
                    throw new IllegalStateException("locator.remainder");
                }
                if (this.manager == null) {
                    this.manager = new DSManager();
                }
                try {
                    for (DSCaptureDevice d : this.manager.getCaptureDevices()) {
                        if (remainder.equals(d.getName())) {
                            device = d;
                            break;
                        }
                    }
                    if (device != null) {
                        this.device = device;
                    }
                    if (this.device == null) {
                        this.manager.dispose();
                        this.manager = null;
                    }
                } catch (Throwable th) {
                    if (this.device == null) {
                        this.manager.dispose();
                        this.manager = null;
                    }
                }
            } else {
                throw new IllegalStateException("locator.protocol");
            }
        }
        return device;
    }

    /* access modifiers changed from: protected */
    public Format[] getSupportedFormats(int streamIndex) {
        DSCaptureDevice device = this.device;
        if (device == null) {
            return super.getSupportedFormats(streamIndex);
        }
        DSFormat[] deviceFmts = device.getSupportedFormats();
        List<Format> fmts = new ArrayList(deviceFmts.length);
        for (DSFormat deviceFmt : deviceFmts) {
            Dimension size = new Dimension(deviceFmt.getWidth(), deviceFmt.getHeight());
            int devicePixFmt = deviceFmt.getPixelFormat();
            int pixFmt = getFFmpegPixFmt(devicePixFmt);
            if (pixFmt != -1) {
                fmts.add(new AVFrameFormat(size, -1.0f, pixFmt, devicePixFmt));
            }
        }
        return (Format[]) fmts.toArray(new Format[fmts.size()]);
    }

    /* access modifiers changed from: protected */
    public Format setFormat(int streamIndex, Format oldValue, Format newValue) {
        return DirectShowStream.isSupportedFormat(newValue) ? newValue : super.setFormat(streamIndex, oldValue, newValue);
    }
}
