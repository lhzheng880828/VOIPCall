package net.sf.fmj.media.renderer.audio;

import javax.media.Buffer;
import javax.media.CachingControl;
import javax.media.Clock;
import javax.media.ClockStoppedException;
import javax.media.Control;
import javax.media.Drainable;
import javax.media.Format;
import javax.media.GainControl;
import javax.media.IncompatibleTimeBaseException;
import javax.media.Owned;
import javax.media.Prefetchable;
import javax.media.Renderer;
import javax.media.Time;
import javax.media.TimeBase;
import javax.media.control.BufferControl;
import javax.media.format.AudioFormat;
import net.sf.fmj.ejmf.toolkit.util.TimeSource;
import net.sf.fmj.media.BasicPlugIn;
import net.sf.fmj.media.Log;
import net.sf.fmj.media.MediaTimeBase;
import net.sf.fmj.media.renderer.audio.device.AudioOutput;
import org.jitsi.android.util.java.awt.Component;

public abstract class AudioRenderer extends BasicPlugIn implements Renderer, Prefetchable, Drainable, Clock {
    static int DefaultMaxBufferSize = 4000;
    static int DefaultMinBufferSize = 62;
    long bufLenReq;
    protected BufferControl bufferControl;
    protected int bytesPerSec;
    protected long bytesWritten;
    protected AudioFormat devFormat;
    protected AudioOutput device;
    protected boolean devicePaused;
    protected GainControl gainControl;
    protected AudioFormat inputFormat;
    TimeBase master;
    long mediaTimeAnchor;
    protected Control peakVolumeMeter;
    protected boolean prefetched;
    float rate;
    protected boolean resetted;
    long startTime;
    protected boolean started;
    long stopTime;
    Format[] supportedFormats;
    long ticksSinceLastReset;
    protected TimeBase timeBase;
    private Object writeLock;

    class AudioTimeBase extends MediaTimeBase {
        AudioRenderer renderer;

        AudioTimeBase(AudioRenderer r) {
            this.renderer = r;
        }

        public long getMediaTime() {
            long j = 0;
            if (AudioRenderer.this.rate != 1.0f && AudioRenderer.this.rate != 0.0f) {
                if (AudioRenderer.this.device != null) {
                    j = AudioRenderer.this.device.getMediaNanoseconds();
                }
                return (long) (((float) j) / AudioRenderer.this.rate);
            } else if (AudioRenderer.this.device != null) {
                return AudioRenderer.this.device.getMediaNanoseconds();
            } else {
                return 0;
            }
        }
    }

    class BC implements BufferControl, Owned {
        AudioRenderer renderer;

        BC(AudioRenderer ar) {
            this.renderer = ar;
        }

        public long getBufferLength() {
            return AudioRenderer.this.bufLenReq;
        }

        public Component getControlComponent() {
            return null;
        }

        public boolean getEnabledThreshold() {
            return false;
        }

        public long getMinimumThreshold() {
            return 0;
        }

        public Object getOwner() {
            return this.renderer;
        }

        public long setBufferLength(long time) {
            if (time < ((long) AudioRenderer.DefaultMinBufferSize)) {
                AudioRenderer.this.bufLenReq = (long) AudioRenderer.DefaultMinBufferSize;
            } else if (time > ((long) AudioRenderer.DefaultMaxBufferSize)) {
                AudioRenderer.this.bufLenReq = (long) AudioRenderer.DefaultMaxBufferSize;
            } else {
                AudioRenderer.this.bufLenReq = time;
            }
            return AudioRenderer.this.bufLenReq;
        }

        public void setEnabledThreshold(boolean b) {
        }

        public long setMinimumThreshold(long time) {
            return 0;
        }
    }

    public abstract AudioOutput createDevice(AudioFormat audioFormat);

    public AudioRenderer() {
        this.device = null;
        this.timeBase = null;
        this.started = false;
        this.prefetched = false;
        this.resetted = false;
        this.devicePaused = true;
        this.peakVolumeMeter = null;
        this.bytesWritten = 0;
        this.writeLock = new Object();
        this.mediaTimeAnchor = 0;
        this.startTime = CachingControl.LENGTH_UNKNOWN;
        this.stopTime = CachingControl.LENGTH_UNKNOWN;
        this.ticksSinceLastReset = 0;
        this.rate = 1.0f;
        this.master = null;
        this.bufLenReq = 200;
        this.timeBase = new AudioTimeBase(this);
        this.bufferControl = new BC(this);
    }

    /* access modifiers changed from: protected */
    public boolean checkInput(Buffer buffer) {
        Format format = buffer.getFormat();
        if (this.device == null || this.devFormat == null || !this.devFormat.equals(format)) {
            if (initDevice((AudioFormat) format)) {
                this.devFormat = (AudioFormat) format;
            } else {
                buffer.setDiscard(true);
                return false;
            }
        }
        return true;
    }

    public void close() {
        stop();
        if (this.device != null) {
            pauseDevice();
            this.device.flush();
            this.mediaTimeAnchor = getMediaNanoseconds();
            this.ticksSinceLastReset = 0;
            this.device.dispose();
        }
        this.device = null;
    }

    public int computeBufferSize(AudioFormat f) {
        long bufLen;
        long bytesPerSecond = (long) (((f.getSampleRate() * ((double) f.getChannels())) * ((double) f.getSampleSizeInBits())) / 8.0d);
        if (this.bufLenReq < ((long) DefaultMinBufferSize)) {
            bufLen = (long) DefaultMinBufferSize;
        } else if (this.bufLenReq > ((long) DefaultMaxBufferSize)) {
            bufLen = (long) DefaultMaxBufferSize;
        } else {
            bufLen = this.bufLenReq;
        }
        return (int) ((long) (((float) bytesPerSecond) * (((float) bufLen) / 1000.0f)));
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    /* JADX WARNING: Missing block: B:23:0x005a, code skipped:
            r9 = r14.writeLock;
     */
    /* JADX WARNING: Missing block: B:24:0x005c, code skipped:
            monitor-enter(r9);
     */
    /* JADX WARNING: Missing block: B:27:0x005f, code skipped:
            if (r14.devicePaused == false) goto L_0x0067;
     */
    /* JADX WARNING: Missing block: B:28:0x0061, code skipped:
            monitor-exit(r9);
     */
    /* JADX WARNING: Missing block: B:34:0x0067, code skipped:
            if (r5 <= 0) goto L_0x0080;
     */
    /* JADX WARNING: Missing block: B:37:0x006b, code skipped:
            if (r14.resetted != false) goto L_0x0080;
     */
    /* JADX WARNING: Missing block: B:38:0x006d, code skipped:
            r3 = r14.device.write(r1, r4, r5);
            r14.bytesWritten += (long) r3;
     */
    /* JADX WARNING: Missing block: B:39:0x0079, code skipped:
            r4 = r4 + r3;
            r5 = r5 - r3;
     */
    /* JADX WARNING: Missing block: B:44:0x0080, code skipped:
            monitor-exit(r9);
     */
    /* JADX WARNING: Missing block: B:45:0x0081, code skipped:
            r15.setLength(0);
            r15.setOffset(0);
     */
    /* JADX WARNING: Missing block: B:60:?, code skipped:
            return 2;
     */
    /* JADX WARNING: Missing block: B:61:?, code skipped:
            return 0;
     */
    /* JADX WARNING: Missing block: B:62:?, code skipped:
            return 0;
     */
    public int doProcessData(javax.media.Buffer r15) {
        /*
        r14 = this;
        r8 = 2;
        r7 = 0;
        r6 = r15.getData();
        r6 = (byte[]) r6;
        r1 = r6;
        r1 = (byte[]) r1;
        r5 = r15.getLength();
        r4 = r15.getOffset();
        r3 = 0;
        monitor-enter(r14);
        r6 = r14.started;	 Catch:{ all -> 0x0064 }
        if (r6 != 0) goto L_0x0059;
    L_0x0019:
        r6 = r14.devicePaused;	 Catch:{ all -> 0x0064 }
        if (r6 != 0) goto L_0x0020;
    L_0x001d:
        r14.pauseDevice();	 Catch:{ all -> 0x0064 }
    L_0x0020:
        r6 = 0;
        r14.resetted = r6;	 Catch:{ all -> 0x0064 }
        r6 = r14.device;	 Catch:{ all -> 0x0064 }
        r0 = r6.bufferAvailable();	 Catch:{ all -> 0x0064 }
        if (r0 <= r5) goto L_0x002c;
    L_0x002b:
        r0 = r5;
    L_0x002c:
        if (r0 <= 0) goto L_0x003a;
    L_0x002e:
        r6 = r14.device;	 Catch:{ all -> 0x0064 }
        r3 = r6.write(r1, r4, r0);	 Catch:{ all -> 0x0064 }
        r10 = r14.bytesWritten;	 Catch:{ all -> 0x0064 }
        r12 = (long) r3;	 Catch:{ all -> 0x0064 }
        r10 = r10 + r12;
        r14.bytesWritten = r10;	 Catch:{ all -> 0x0064 }
    L_0x003a:
        r6 = r5 - r3;
        r15.setLength(r6);	 Catch:{ all -> 0x0064 }
        r6 = r15.getLength();	 Catch:{ all -> 0x0064 }
        if (r6 > 0) goto L_0x004b;
    L_0x0045:
        r6 = r15.isEOM();	 Catch:{ all -> 0x0064 }
        if (r6 == 0) goto L_0x0056;
    L_0x004b:
        r6 = r4 + r3;
        r15.setOffset(r6);	 Catch:{ all -> 0x0064 }
        r6 = 1;
        r14.prefetched = r6;	 Catch:{ all -> 0x0064 }
        monitor-exit(r14);	 Catch:{ all -> 0x0064 }
        r6 = r8;
    L_0x0055:
        return r6;
    L_0x0056:
        monitor-exit(r14);	 Catch:{ all -> 0x0064 }
        r6 = r7;
        goto L_0x0055;
    L_0x0059:
        monitor-exit(r14);	 Catch:{ all -> 0x0064 }
        r9 = r14.writeLock;
        monitor-enter(r9);
        r6 = r14.devicePaused;	 Catch:{ all -> 0x0089 }
        if (r6 == 0) goto L_0x0067;
    L_0x0061:
        monitor-exit(r9);	 Catch:{ all -> 0x0089 }
        r6 = r8;
        goto L_0x0055;
    L_0x0064:
        r6 = move-exception;
        monitor-exit(r14);	 Catch:{ all -> 0x0064 }
        throw r6;
    L_0x0067:
        if (r5 <= 0) goto L_0x0080;
    L_0x0069:
        r6 = r14.resetted;	 Catch:{ NullPointerException -> 0x007c }
        if (r6 != 0) goto L_0x0080;
    L_0x006d:
        r6 = r14.device;	 Catch:{ NullPointerException -> 0x007c }
        r3 = r6.write(r1, r4, r5);	 Catch:{ NullPointerException -> 0x007c }
        r10 = r14.bytesWritten;	 Catch:{ NullPointerException -> 0x007c }
        r12 = (long) r3;	 Catch:{ NullPointerException -> 0x007c }
        r10 = r10 + r12;
        r14.bytesWritten = r10;	 Catch:{ NullPointerException -> 0x007c }
        r4 = r4 + r3;
        r5 = r5 - r3;
        goto L_0x0067;
    L_0x007c:
        r2 = move-exception;
        monitor-exit(r9);	 Catch:{ all -> 0x0089 }
        r6 = r7;
        goto L_0x0055;
    L_0x0080:
        monitor-exit(r9);	 Catch:{ all -> 0x0089 }
        r15.setLength(r7);
        r15.setOffset(r7);
        r6 = r7;
        goto L_0x0055;
    L_0x0089:
        r6 = move-exception;
        monitor-exit(r9);	 Catch:{ all -> 0x0089 }
        throw r6;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.media.renderer.audio.AudioRenderer.doProcessData(javax.media.Buffer):int");
    }

    public synchronized void drain() {
        if (this.started && this.device != null) {
            this.device.drain();
        }
        this.prefetched = false;
    }

    public Object[] getControls() {
        return new Control[]{this.gainControl, this.bufferControl};
    }

    public long getLatency() {
        return (((this.bytesWritten * 1000) / ((long) this.bytesPerSec)) * TimeSource.MICROS_PER_SEC) - getMediaNanoseconds();
    }

    public long getMediaNanoseconds() {
        return ((this.device != null ? this.device.getMediaNanoseconds() : 0) + this.mediaTimeAnchor) - this.ticksSinceLastReset;
    }

    public Time getMediaTime() {
        return new Time(getMediaNanoseconds());
    }

    public float getRate() {
        return this.rate;
    }

    public Time getStopTime() {
        return new Time(this.stopTime);
    }

    public Format[] getSupportedInputFormats() {
        return this.supportedFormats;
    }

    public Time getSyncTime() {
        return new Time(0);
    }

    public TimeBase getTimeBase() {
        if (this.master != null) {
            return this.master;
        }
        return this.timeBase;
    }

    /* access modifiers changed from: protected */
    public boolean initDevice(AudioFormat format) {
        if (format == null) {
            System.err.println("AudioRenderer: ERROR: Unknown AudioFormat");
            return false;
        } else if (format.getSampleRate() == -1.0d || format.getSampleSizeInBits() == -1) {
            Log.error("Cannot initialize audio renderer with format: " + format);
            return false;
        } else {
            if (this.device != null) {
                this.device.drain();
                pauseDevice();
                this.mediaTimeAnchor = getMediaNanoseconds();
                this.ticksSinceLastReset = 0;
                this.device.dispose();
                this.device = null;
            }
            AudioFormat audioFormat = new AudioFormat(format.getEncoding(), format.getSampleRate(), format.getSampleSizeInBits(), format.getChannels(), format.getEndian(), format.getSigned());
            this.device = createDevice(audioFormat);
            if (this.device == null || !this.device.initialize(audioFormat, computeBufferSize(audioFormat))) {
                this.device = null;
                return false;
            }
            this.device.setMute(this.gainControl.getMute());
            this.device.setGain((double) this.gainControl.getDB());
            if (!(this.rate == 1.0f || this.rate == this.device.setRate(this.rate))) {
                System.err.println("The AudioRenderer does not support the given rate: " + this.rate);
                this.device.setRate(1.0f);
            }
            if (this.started) {
                resumeDevice();
            }
            this.bytesPerSec = (int) (((format.getSampleRate() * ((double) format.getChannels())) * ((double) format.getSampleSizeInBits())) / 8.0d);
            return true;
        }
    }

    public boolean isPrefetched() {
        return this.prefetched;
    }

    public Time mapToTimeBase(Time t) throws ClockStoppedException {
        return new Time(((long) (((float) (t.getNanoseconds() - this.mediaTimeAnchor)) / this.rate)) + this.startTime);
    }

    /* access modifiers changed from: declared_synchronized */
    public synchronized void pauseDevice() {
        if (!(this.devicePaused || this.device == null)) {
            this.device.pause();
            this.devicePaused = true;
        }
        if (this.timeBase instanceof AudioTimeBase) {
            ((AudioTimeBase) this.timeBase).mediaStopped();
        }
    }

    public int process(Buffer buffer) {
        int rtn = processData(buffer);
        if (buffer.isEOM() && rtn != 2) {
            drain();
            pauseDevice();
        }
        return rtn;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Missing block: B:8:0x000c, code skipped:
            r1 = (javax.media.format.AudioFormat) r13.getFormat();
            r4 = (int) r1.getSampleRate();
            r5 = r1.getSampleSizeInBits();
            r2 = (long) ((r13.getLength() * 1000) / (((r5 / 8) * r4) * r1.getChannels()));
     */
    /* JADX WARNING: Missing block: B:10:?, code skipped:
            java.lang.Thread.sleep((long) ((int) (((float) r2) / getRate())));
     */
    public void processByWaiting(javax.media.Buffer r13) {
        /*
        r12 = this;
        r10 = 0;
        monitor-enter(r12);
        r7 = r12.started;	 Catch:{ all -> 0x0046 }
        if (r7 != 0) goto L_0x000b;
    L_0x0006:
        r7 = 1;
        r12.prefetched = r7;	 Catch:{ all -> 0x0046 }
        monitor-exit(r12);	 Catch:{ all -> 0x0046 }
    L_0x000a:
        return;
    L_0x000b:
        monitor-exit(r12);	 Catch:{ all -> 0x0046 }
        r1 = r13.getFormat();
        r1 = (javax.media.format.AudioFormat) r1;
        r8 = r1.getSampleRate();
        r4 = (int) r8;
        r5 = r1.getSampleSizeInBits();
        r0 = r1.getChannels();
        r7 = r13.getLength();
        r7 = r7 * 1000;
        r8 = r5 / 8;
        r8 = r8 * r4;
        r8 = r8 * r0;
        r7 = r7 / r8;
        r2 = (long) r7;
        r7 = (float) r2;
        r8 = r12.getRate();
        r7 = r7 / r8;
        r6 = (int) r7;
        r8 = (long) r6;
        java.lang.Thread.sleep(r8);	 Catch:{ Exception -> 0x0049 }
    L_0x0036:
        r13.setLength(r10);
        r13.setOffset(r10);
        r8 = r12.mediaTimeAnchor;
        r10 = 1000000; // 0xf4240 float:1.401298E-39 double:4.940656E-318;
        r10 = r10 * r2;
        r8 = r8 + r10;
        r12.mediaTimeAnchor = r8;
        goto L_0x000a;
    L_0x0046:
        r7 = move-exception;
        monitor-exit(r12);	 Catch:{ all -> 0x0046 }
        throw r7;
    L_0x0049:
        r7 = move-exception;
        goto L_0x0036;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.media.renderer.audio.AudioRenderer.processByWaiting(javax.media.Buffer):void");
    }

    /* access modifiers changed from: protected */
    public int processData(Buffer buffer) {
        if (checkInput(buffer)) {
            return doProcessData(buffer);
        }
        return 1;
    }

    public void reset() {
        this.resetted = true;
        this.mediaTimeAnchor = getMediaNanoseconds();
        if (this.device != null) {
            this.device.flush();
            this.ticksSinceLastReset = this.device.getMediaNanoseconds();
        } else {
            this.ticksSinceLastReset = 0;
        }
        this.prefetched = false;
    }

    /* access modifiers changed from: declared_synchronized */
    public synchronized void resumeDevice() {
        if (this.timeBase instanceof AudioTimeBase) {
            ((AudioTimeBase) this.timeBase).mediaStarted();
        }
        if (this.devicePaused && this.device != null) {
            this.device.resume();
            this.devicePaused = false;
        }
    }

    public Format setInputFormat(Format format) {
        for (Format matches : this.supportedFormats) {
            if (matches.matches(format)) {
                this.inputFormat = (AudioFormat) format;
                return format;
            }
        }
        return null;
    }

    public void setMediaTime(Time now) {
        this.mediaTimeAnchor = now.getNanoseconds();
    }

    public float setRate(float factor) {
        if (this.device != null) {
            this.rate = this.device.setRate(factor);
        } else {
            this.rate = 1.0f;
        }
        return this.rate;
    }

    public void setStopTime(Time t) {
        this.stopTime = t.getNanoseconds();
    }

    public void setTimeBase(TimeBase master) throws IncompatibleTimeBaseException {
        if (!(master instanceof AudioTimeBase)) {
            Log.warning("AudioRenderer cannot be controlled by time bases other than its own: " + master);
        }
        this.master = master;
    }

    public void start() {
        syncStart(getTimeBase().getTime());
    }

    public synchronized void stop() {
        this.started = false;
        this.prefetched = false;
        synchronized (this.writeLock) {
            pauseDevice();
        }
    }

    public synchronized void syncStart(Time at) {
        this.started = true;
        this.prefetched = true;
        this.resetted = false;
        resumeDevice();
        this.startTime = at.getNanoseconds();
    }
}
