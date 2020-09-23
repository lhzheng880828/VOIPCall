package org.jitsi.impl.neomedia.jmfext.media.protocol;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;
import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Controls;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.media.control.FormatControl;
import javax.media.control.FrameRateControl;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.DataSource;
import javax.media.protocol.SourceStream;
import net.sf.fmj.media.util.RTPInfo;
import org.jitsi.impl.neomedia.control.AbstractControls;
import org.jitsi.impl.neomedia.control.AbstractFormatControl;
import org.jitsi.impl.neomedia.control.ControlsAdapter;
import org.jitsi.impl.neomedia.jmfext.media.protocol.AbstractBufferStream;
import org.jitsi.util.Logger;

public abstract class AbstractBufferCaptureDevice<AbstractBufferStreamT extends AbstractBufferStream<?>> implements CaptureDevice, Controls {
    private static final FormatControl[] EMPTY_FORMAT_CONTROLS = new FormatControl[0];
    private static final Logger logger = Logger.getLogger(AbstractBufferCaptureDevice.class);
    private boolean connected = false;
    private final Object controlsSyncRoot = new Object();
    private FormatControl[] formatControls;
    private FrameRateControl[] frameRateControls;
    private final ReentrantLock lock = new ReentrantLock();
    private RTPInfo[] rtpInfos;
    private boolean started = false;
    private final Object streamSyncRoot = new Object();
    private AbstractBufferStream<?>[] streams;

    public abstract AbstractBufferStreamT createStream(int i, FormatControl formatControl);

    public abstract void doConnect() throws IOException;

    public abstract void doDisconnect();

    public abstract void doStart() throws IOException;

    public abstract void doStop() throws IOException;

    public abstract CaptureDeviceInfo getCaptureDeviceInfo();

    public abstract Format getFormat(int i, Format format);

    public abstract Format[] getSupportedFormats(int i);

    public abstract Format setFormat(int i, Format format, Format format2);

    public void connect() throws IOException {
        lock();
        try {
            if (!this.connected) {
                doConnect();
                this.connected = true;
            }
            unlock();
        } catch (Throwable th) {
            unlock();
        }
    }

    /* access modifiers changed from: protected */
    public FormatControl createFormatControl(final int streamIndex) {
        return new AbstractFormatControl() {
            private Format format;

            public Format getFormat() {
                this.format = AbstractBufferCaptureDevice.this.internalGetFormat(streamIndex, this.format);
                return this.format;
            }

            public Format[] getSupportedFormats() {
                return AbstractBufferCaptureDevice.this.getSupportedFormats(streamIndex);
            }

            public Format setFormat(Format format) {
                Format setFormat = super.setFormat(format);
                if (setFormat != null) {
                    setFormat = AbstractBufferCaptureDevice.this.internalSetFormat(streamIndex, setFormat, format);
                    if (setFormat != null) {
                        this.format = setFormat;
                    }
                }
                return setFormat;
            }
        };
    }

    /* access modifiers changed from: protected */
    public FormatControl[] createFormatControls() {
        if (createFormatControl(0) == null) {
            return EMPTY_FORMAT_CONTROLS;
        }
        return new FormatControl[]{createFormatControl(0)};
    }

    /* access modifiers changed from: protected */
    public FrameRateControl createFrameRateControl() {
        return null;
    }

    /* access modifiers changed from: protected */
    public RTPInfo createRTPInfo() {
        return new RTPInfo() {
            public String getCNAME() {
                return null;
            }
        };
    }

    /* access modifiers changed from: final */
    public final void defaultDoStart() throws IOException {
        synchronized (getStreamSyncRoot()) {
            if (this.streams != null) {
                for (AbstractBufferStream<?> stream : this.streams) {
                    stream.start();
                }
            }
        }
    }

    /* access modifiers changed from: final */
    public final void defaultDoStop() throws IOException {
        synchronized (getStreamSyncRoot()) {
            if (this.streams != null) {
                for (AbstractBufferStream<?> stream : this.streams) {
                    stream.stop();
                }
            }
        }
    }

    /* access modifiers changed from: final */
    public final Object[] defaultGetControls() {
        FormatControl[] formatControls = internalGetFormatControls();
        int formatControlCount = formatControls == null ? 0 : formatControls.length;
        FrameRateControl[] frameRateControls = internalGetFrameRateControls();
        int frameRateControlCount = frameRateControls == null ? 0 : frameRateControls.length;
        RTPInfo[] rtpInfos = internalGetRTPInfos();
        int rtpInfoCount = rtpInfos == null ? 0 : rtpInfos.length;
        if (formatControlCount == 0 && frameRateControlCount == 0 && rtpInfoCount == 0) {
            return ControlsAdapter.EMPTY_CONTROLS;
        }
        Object[] controls = new Object[((formatControlCount + frameRateControlCount) + rtpInfoCount)];
        int offset = 0;
        if (formatControlCount != 0) {
            System.arraycopy(formatControls, 0, controls, 0, formatControlCount);
            offset = 0 + formatControlCount;
        }
        if (frameRateControlCount != 0) {
            System.arraycopy(frameRateControls, 0, controls, offset, frameRateControlCount);
            offset += frameRateControlCount;
        }
        if (rtpInfoCount == 0) {
            return controls;
        }
        System.arraycopy(rtpInfos, 0, controls, offset, rtpInfoCount);
        offset += rtpInfoCount;
        return controls;
    }

    /* access modifiers changed from: final */
    public final Format defaultGetFormat(int streamIndex, Format oldValue) {
        if (oldValue != null) {
            return oldValue;
        }
        Format[] supportedFormats = getSupportedFormats(streamIndex);
        Format format = (supportedFormats == null || supportedFormats.length < 1) ? null : supportedFormats[0];
        return format;
    }

    /* access modifiers changed from: final */
    public final Format[] defaultGetSupportedFormats(int streamIndex) {
        CaptureDeviceInfo captureDeviceInfo = getCaptureDeviceInfo();
        return captureDeviceInfo == null ? null : captureDeviceInfo.getFormats();
    }

    public void disconnect() {
        lock();
        try {
            stop();
        } catch (IOException ioex) {
            logger.error("Failed to stop " + getClass().getSimpleName(), ioex);
        } catch (Throwable th) {
            unlock();
        }
        if (this.connected) {
            doDisconnect();
            this.connected = false;
        }
        unlock();
    }

    public static CaptureDeviceInfo getCaptureDeviceInfo(DataSource captureDevice) {
        Vector<CaptureDeviceInfo> captureDeviceInfos = CaptureDeviceManager.getDeviceList(null);
        MediaLocator locator = captureDevice.getLocator();
        Iterator i$ = captureDeviceInfos.iterator();
        while (i$.hasNext()) {
            CaptureDeviceInfo captureDeviceInfo = (CaptureDeviceInfo) i$.next();
            if (captureDeviceInfo.getLocator().toString().equals(locator.toString())) {
                return captureDeviceInfo;
            }
        }
        return null;
    }

    public Object getControl(String controlType) {
        return AbstractControls.getControl(this, controlType);
    }

    public Object[] getControls() {
        return defaultGetControls();
    }

    public FormatControl[] getFormatControls() {
        return AbstractFormatControl.getFormatControls(this);
    }

    /* access modifiers changed from: 0000 */
    public Object getStreamSyncRoot() {
        return this.streamSyncRoot;
    }

    public <SourceStreamT extends SourceStream> SourceStreamT[] getStreams(Class<SourceStreamT> clz) {
        SourceStream[] internalGetStreams;
        synchronized (getStreamSyncRoot()) {
            internalGetStreams = internalGetStreams(clz);
        }
        return internalGetStreams;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Missing block: B:16:0x0020, code skipped:
            if (r0 == false) goto L_?;
     */
    /* JADX WARNING: Missing block: B:17:0x0022, code skipped:
            monitor-enter(r5);
     */
    /* JADX WARNING: Missing block: B:19:?, code skipped:
            r5.lock.unlock();
     */
    /* JADX WARNING: Missing block: B:20:0x0028, code skipped:
            monitor-exit(r5);
     */
    /* JADX WARNING: Missing block: B:71:?, code skipped:
            return r2;
     */
    /* JADX WARNING: Missing block: B:72:?, code skipped:
            return r2;
     */
    public javax.media.Format internalGetFormat(int r6, javax.media.Format r7) {
        /*
        r5 = this;
        monitor-enter(r5);
        r3 = r5.lock;	 Catch:{ all -> 0x002a }
        r0 = r3.tryLock();	 Catch:{ all -> 0x002a }
        monitor-exit(r5);	 Catch:{ all -> 0x002a }
        if (r0 == 0) goto L_0x0031;
    L_0x000a:
        r4 = r5.getStreamSyncRoot();	 Catch:{ all -> 0x0042 }
        monitor-enter(r4);	 Catch:{ all -> 0x0042 }
        r3 = r5.streams;	 Catch:{ all -> 0x003f }
        if (r3 == 0) goto L_0x0030;
    L_0x0013:
        r3 = r5.streams;	 Catch:{ all -> 0x003f }
        r1 = r3[r6];	 Catch:{ all -> 0x003f }
        if (r1 == 0) goto L_0x0030;
    L_0x0019:
        r2 = r1.internalGetFormat();	 Catch:{ all -> 0x003f }
        if (r2 == 0) goto L_0x0030;
    L_0x001f:
        monitor-exit(r4);	 Catch:{ all -> 0x003f }
        if (r0 == 0) goto L_0x0029;
    L_0x0022:
        monitor-enter(r5);
        r3 = r5.lock;	 Catch:{ all -> 0x002d }
        r3.unlock();	 Catch:{ all -> 0x002d }
        monitor-exit(r5);	 Catch:{ all -> 0x002d }
    L_0x0029:
        return r2;
    L_0x002a:
        r3 = move-exception;
        monitor-exit(r5);	 Catch:{ all -> 0x002a }
        throw r3;
    L_0x002d:
        r3 = move-exception;
        monitor-exit(r5);	 Catch:{ all -> 0x002d }
        throw r3;
    L_0x0030:
        monitor-exit(r4);	 Catch:{ all -> 0x003f }
    L_0x0031:
        if (r0 == 0) goto L_0x003a;
    L_0x0033:
        monitor-enter(r5);
        r3 = r5.lock;	 Catch:{ all -> 0x004d }
        r3.unlock();	 Catch:{ all -> 0x004d }
        monitor-exit(r5);	 Catch:{ all -> 0x004d }
    L_0x003a:
        r2 = r5.getFormat(r6, r7);
        goto L_0x0029;
    L_0x003f:
        r3 = move-exception;
        monitor-exit(r4);	 Catch:{ all -> 0x003f }
        throw r3;	 Catch:{ all -> 0x0042 }
    L_0x0042:
        r3 = move-exception;
        if (r0 == 0) goto L_0x004c;
    L_0x0045:
        monitor-enter(r5);
        r4 = r5.lock;	 Catch:{ all -> 0x0050 }
        r4.unlock();	 Catch:{ all -> 0x0050 }
        monitor-exit(r5);	 Catch:{ all -> 0x0050 }
    L_0x004c:
        throw r3;
    L_0x004d:
        r3 = move-exception;
        monitor-exit(r5);	 Catch:{ all -> 0x004d }
        throw r3;
    L_0x0050:
        r3 = move-exception;
        monitor-exit(r5);	 Catch:{ all -> 0x0050 }
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.jmfext.media.protocol.AbstractBufferCaptureDevice.internalGetFormat(int, javax.media.Format):javax.media.Format");
    }

    private FormatControl[] internalGetFormatControls() {
        FormatControl[] formatControlArr;
        synchronized (this.controlsSyncRoot) {
            if (this.formatControls == null) {
                this.formatControls = createFormatControls();
            }
            formatControlArr = this.formatControls;
        }
        return formatControlArr;
    }

    private FrameRateControl[] internalGetFrameRateControls() {
        FrameRateControl[] frameRateControlArr;
        synchronized (this.controlsSyncRoot) {
            if (this.frameRateControls == null) {
                this.frameRateControls = createFrameRateControl() == null ? new FrameRateControl[0] : new FrameRateControl[]{createFrameRateControl()};
            }
            frameRateControlArr = this.frameRateControls;
        }
        return frameRateControlArr;
    }

    private RTPInfo[] internalGetRTPInfos() {
        RTPInfo[] rTPInfoArr;
        synchronized (this.controlsSyncRoot) {
            if (this.rtpInfos == null) {
                this.rtpInfos = createRTPInfo() == null ? new RTPInfo[0] : new RTPInfo[]{createRTPInfo()};
            }
            rTPInfoArr = this.rtpInfos;
        }
        return rTPInfoArr;
    }

    private <SourceStreamT extends SourceStream> SourceStreamT[] internalGetStreams(Class<SourceStreamT> clz) {
        if (this.streams == null) {
            FormatControl[] formatControls = internalGetFormatControls();
            if (formatControls != null) {
                int formatControlCount = formatControls.length;
                this.streams = new AbstractBufferStream[formatControlCount];
                for (int i = 0; i < formatControlCount; i++) {
                    this.streams[i] = createStream(i, formatControls[i]);
                }
                if (this.started) {
                    AbstractBufferStream[] arr$ = this.streams;
                    int len$ = arr$.length;
                    int i$ = 0;
                    while (i$ < len$) {
                        try {
                            arr$[i$].start();
                            i$++;
                        } catch (IOException ioex) {
                            throw new UndeclaredThrowableException(ioex);
                        }
                    }
                }
            }
        }
        int streamCount = this.streams == null ? 0 : this.streams.length;
        SourceStream[] clone = (SourceStream[]) ((SourceStream[]) Array.newInstance(clz, streamCount));
        if (streamCount != 0) {
            System.arraycopy(this.streams, 0, clone, 0, streamCount);
        }
        return clone;
    }

    /* access modifiers changed from: private */
    public Format internalSetFormat(int streamIndex, Format oldValue, Format newValue) {
        lock();
        try {
            Format internalSetFormat;
            synchronized (getStreamSyncRoot()) {
                if (this.streams != null) {
                    AbstractBufferStream<?> stream = this.streams[streamIndex];
                    if (stream != null) {
                        internalSetFormat = stream.internalSetFormat(newValue);
                    }
                }
                unlock();
                return setFormat(streamIndex, oldValue, newValue);
            }
            return internalSetFormat;
        } finally {
            unlock();
        }
    }

    private synchronized void lock() {
        this.lock.lock();
    }

    public void start() throws IOException {
        lock();
        try {
            if (!this.started) {
                if (this.connected) {
                    doStart();
                    this.started = true;
                } else {
                    throw new IOException(getClass().getName() + " not connected");
                }
            }
            unlock();
        } catch (Throwable th) {
            unlock();
        }
    }

    public void stop() throws IOException {
        lock();
        try {
            if (this.started) {
                doStop();
                this.started = false;
            }
            unlock();
        } catch (Throwable th) {
            unlock();
        }
    }

    /* access modifiers changed from: 0000 */
    public AbstractBufferStream<?>[] streams() {
        return this.streams;
    }

    private synchronized void unlock() {
        this.lock.unlock();
    }
}
