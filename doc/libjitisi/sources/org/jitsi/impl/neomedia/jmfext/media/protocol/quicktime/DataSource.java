package org.jitsi.impl.neomedia.jmfext.media.protocol.quicktime;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.media.PlugInManager;
import javax.media.control.FormatControl;
import javax.media.control.FrameRateControl;
import javax.media.format.VideoFormat;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.impl.neomedia.control.FrameRateControlAdapter;
import org.jitsi.impl.neomedia.device.DeviceSystem;
import org.jitsi.impl.neomedia.jmfext.media.protocol.AbstractVideoPushBufferCaptureDevice;
import org.jitsi.impl.neomedia.quicktime.NSErrorException;
import org.jitsi.impl.neomedia.quicktime.QTCaptureDevice;
import org.jitsi.impl.neomedia.quicktime.QTCaptureDeviceInput;
import org.jitsi.impl.neomedia.quicktime.QTCaptureSession;
import org.jitsi.util.Logger;

public class DataSource extends AbstractVideoPushBufferCaptureDevice {
    private static final Logger logger = Logger.getLogger(DataSource.class);
    private static Format[] supportedFormats;
    private QTCaptureSession captureSession;
    private QTCaptureDevice device;

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
                float frameRate = -1.0f;
                boolean frameRateFromQuickTimeStream = false;
                synchronized (DataSource.this.getStreamSyncRoot()) {
                    Object[] streams = DataSource.this.streams();
                    if (streams != null && streams.length != 0) {
                        for (Object stream : streams) {
                            QuickTimeStream quickTimeStream = (QuickTimeStream) stream;
                            if (quickTimeStream != null) {
                                frameRate = quickTimeStream.getFrameRate();
                                frameRateFromQuickTimeStream = true;
                                if (frameRate != -1.0f) {
                                    break;
                                }
                            }
                        }
                    }
                }
                return frameRateFromQuickTimeStream ? frameRate : this.frameRate;
            }

            public float setFrameRate(float frameRate) {
                float setFrameRate = -1.0f;
                boolean frameRateFromQuickTimeStream = false;
                synchronized (DataSource.this.getStreamSyncRoot()) {
                    Object[] streams = DataSource.this.streams();
                    if (!(streams == null || streams.length == 0)) {
                        for (Object stream : streams) {
                            QuickTimeStream quickTimeStream = (QuickTimeStream) stream;
                            if (quickTimeStream != null) {
                                float quickTimeStreamFrameRate = quickTimeStream.setFrameRate(frameRate);
                                if (quickTimeStreamFrameRate != -1.0f) {
                                    setFrameRate = quickTimeStreamFrameRate;
                                }
                                frameRateFromQuickTimeStream = true;
                            }
                        }
                    }
                }
                if (frameRateFromQuickTimeStream) {
                    return setFrameRate;
                }
                this.frameRate = frameRate;
                return this.frameRate;
            }
        };
    }

    /* access modifiers changed from: protected */
    public QuickTimeStream createStream(int streamIndex, FormatControl formatControl) {
        QuickTimeStream stream = new QuickTimeStream(this, formatControl);
        if (this.captureSession != null) {
            try {
                this.captureSession.addOutput(stream.captureOutput);
            } catch (NSErrorException nseex) {
                logger.error("Failed to addOutput to QTCaptureSession", nseex);
                throw new UndeclaredThrowableException(nseex);
            }
        }
        return stream;
    }

    /* access modifiers changed from: protected */
    public void doConnect() throws IOException {
        IOException ioex;
        super.doConnect();
        try {
            if (this.device.open()) {
                QTCaptureDeviceInput deviceInput = QTCaptureDeviceInput.deviceInputWithDevice(this.device);
                this.captureSession = new QTCaptureSession();
                try {
                    this.captureSession.addInput(deviceInput);
                    synchronized (getStreamSyncRoot()) {
                        Object[] streams = streams();
                        if (streams != null) {
                            for (Object stream : streams) {
                                if (stream != null) {
                                    try {
                                        this.captureSession.addOutput(((QuickTimeStream) stream).captureOutput);
                                    } catch (NSErrorException nseex) {
                                        logger.error("Failed to addOutput to QTCaptureSession", nseex);
                                        ioex = new IOException();
                                        ioex.initCause(nseex);
                                        throw ioex;
                                    }
                                }
                            }
                        }
                    }
                    return;
                } catch (NSErrorException nseex2) {
                    ioex = new IOException();
                    ioex.initCause(nseex2);
                    throw ioex;
                }
            }
            throw new IOException("Failed to open QTCaptureDevice");
        } catch (NSErrorException nseex22) {
            ioex = new IOException();
            ioex.initCause(nseex22);
            throw ioex;
        }
    }

    /* access modifiers changed from: protected */
    public void doDisconnect() {
        super.doDisconnect();
        if (this.captureSession != null) {
            this.captureSession.close();
            this.captureSession = null;
        }
        this.device.close();
    }

    /* access modifiers changed from: protected */
    public void doStart() throws IOException {
        this.captureSession.startRunning();
        super.doStart();
    }

    /* access modifiers changed from: protected */
    public void doStop() throws IOException {
        super.doStop();
        this.captureSession.stopRunning();
    }

    /* access modifiers changed from: protected */
    public Format[] getSupportedFormats(int streamIndex) {
        return getSupportedFormats(super.getSupportedFormats(streamIndex));
    }

    private static synchronized Format[] getSupportedFormats(Format[] genericFormats) {
        Format[] formatArr;
        synchronized (DataSource.class) {
            if (supportedFormats == null || supportedFormats.length <= 0) {
                List<Format> specificFormats = new LinkedList();
                for (Format genericFormat : genericFormats) {
                    VideoFormat genericVideoFormat = (VideoFormat) genericFormat;
                    if (genericVideoFormat.getSize() == null) {
                        Iterator it = PlugInManager.getPlugInList(new VideoFormat(genericVideoFormat.getEncoding()), null, 2).iterator();
                        while (it.hasNext()) {
                            for (Format supportedInputFormat : PlugInManager.getSupportedInputFormats((String) it.next(), 2)) {
                                if (supportedInputFormat instanceof VideoFormat) {
                                    Dimension size = ((VideoFormat) supportedInputFormat).getSize();
                                    if (size != null) {
                                        specificFormats.add(genericFormat.intersects(new VideoFormat(null, size, -1, null, -1.0f)));
                                    }
                                }
                            }
                        }
                    }
                    specificFormats.add(genericFormat);
                }
                supportedFormats = (Format[]) specificFormats.toArray(new Format[specificFormats.size()]);
                formatArr = (Format[]) supportedFormats.clone();
            } else {
                formatArr = (Format[]) supportedFormats.clone();
            }
        }
        return formatArr;
    }

    private void setDevice(QTCaptureDevice device) {
        if (this.device != device) {
            this.device = device;
        }
    }

    /* access modifiers changed from: protected */
    public Format setFormat(int streamIndex, Format oldValue, Format newValue) {
        return newValue instanceof VideoFormat ? newValue : super.setFormat(streamIndex, oldValue, newValue);
    }

    public void setLocator(MediaLocator locator) {
        QTCaptureDevice device;
        super.setLocator(locator);
        locator = getLocator();
        if (locator == null || !DeviceSystem.LOCATOR_PROTOCOL_QUICKTIME.equalsIgnoreCase(locator.getProtocol())) {
            device = null;
        } else {
            device = QTCaptureDevice.deviceWithUniqueID(locator.getRemainder());
        }
        setDevice(device);
    }
}
