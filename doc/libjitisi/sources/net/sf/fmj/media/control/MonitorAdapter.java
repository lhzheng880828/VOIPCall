package net.sf.fmj.media.control;

import javax.media.Format;
import javax.media.Owned;
import javax.media.control.MonitorControl;
import javax.media.format.AudioFormat;
import javax.media.format.UnsupportedFormatException;
import javax.media.format.VideoFormat;
import net.sf.fmj.media.CircularBuffer;
import net.sf.fmj.media.Log;
import net.sf.fmj.media.util.AudioCodecChain;
import net.sf.fmj.media.util.CodecChain;
import net.sf.fmj.media.util.LoopThread;
import net.sf.fmj.media.util.VideoCodecChain;
import org.jitsi.android.util.java.awt.BorderLayout;
import org.jitsi.android.util.java.awt.Checkbox;
import org.jitsi.android.util.java.awt.Color;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.android.util.java.awt.Container;
import org.jitsi.android.util.java.awt.MenuItem;
import org.jitsi.android.util.java.awt.Panel;
import org.jitsi.android.util.java.awt.PopupMenu;
import org.jitsi.android.util.java.awt.event.ActionEvent;
import org.jitsi.android.util.java.awt.event.ActionListener;
import org.jitsi.android.util.java.awt.event.ItemEvent;
import org.jitsi.android.util.java.awt.event.ItemListener;
import org.jitsi.android.util.java.awt.event.MouseAdapter;
import org.jitsi.android.util.java.awt.event.MouseEvent;
import org.jitsi.android.util.java.awt.event.MouseListener;

public class MonitorAdapter implements MonitorControl, Owned {
    static VideoFormat mpegVideo = new VideoFormat(VideoFormat.MPEG_RTP);
    CircularBuffer bufferQ;
    protected Checkbox cbEnabled = null;
    protected CodecChain cc = null;
    protected boolean closed = false;
    protected Component controlComponent = null;
    protected boolean enabled = false;
    protected Format format = null;
    protected int[] frameRates = new int[]{0, 1, 2, 5, 7, 10, 15, 20, 30, 60, 90};
    protected float inFrameRate = 0.0f;
    protected long lastPreviewTime = 0;
    protected LoopThread loopThread;
    protected MouseListener ml = null;
    Object owner;
    protected float previewFrameRate = 30.0f;
    protected long previewInterval = 33333333;
    protected PopupMenu rateMenu = null;
    protected Component visualComponent = null;

    public MonitorAdapter(Format f, Object owner) {
        this.format = f;
        this.owner = owner;
    }

    private void addPopupMenu(Component visual) {
        MenuItem mi;
        this.visualComponent = visual;
        this.rateMenu = new PopupMenu("Monitor Rate");
        ActionListener rateSelect = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String action = ae.getActionCommand();
                try {
                    MonitorAdapter.this.setPreviewFrameRate((float) Integer.parseInt(action.substring(0, action.indexOf(" "))));
                } catch (Throwable t) {
                    if (t instanceof ThreadDeath) {
                        ThreadDeath t2 = (ThreadDeath) t;
                    }
                }
            }
        };
        visual.add(this.rateMenu);
        int lastAdded = 0;
        for (int i = 0; i < this.frameRates.length; i++) {
            if (((float) this.frameRates[i]) < this.inFrameRate) {
                mi = new MenuItem(this.frameRates[i] + " fps");
                this.rateMenu.add(mi);
                mi.addActionListener(rateSelect);
                lastAdded = this.frameRates[i];
            }
        }
        if (((float) lastAdded) < this.inFrameRate) {
            mi = new MenuItem(this.inFrameRate + " fps");
            this.rateMenu.add(mi);
            mi.addActionListener(rateSelect);
        }
        AnonymousClass2 anonymousClass2 = new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                if (me.isPopupTrigger()) {
                    MonitorAdapter.this.rateMenu.show(MonitorAdapter.this.visualComponent, me.getX(), me.getY());
                }
            }

            public void mousePressed(MouseEvent me) {
                if (me.isPopupTrigger()) {
                    MonitorAdapter.this.rateMenu.show(MonitorAdapter.this.visualComponent, me.getX(), me.getY());
                }
            }

            public void mouseReleased(MouseEvent me) {
                if (me.isPopupTrigger()) {
                    MonitorAdapter.this.rateMenu.show(MonitorAdapter.this.visualComponent, me.getX(), me.getY());
                }
            }
        };
        this.ml = anonymousClass2;
        visual.addMouseListener(anonymousClass2);
    }

    public void close() {
        if (this.cc != null) {
            this.loopThread.kill();
            synchronized (this.bufferQ) {
                this.closed = true;
                this.bufferQ.notifyAll();
            }
            this.cc.close();
            this.cc = null;
        }
    }

    private Object copyData(Object in) {
        byte[] out;
        if (in instanceof byte[]) {
            out = new byte[((byte[]) in).length];
            System.arraycopy(in, 0, out, 0, out.length);
            return out;
        } else if (in instanceof short[]) {
            out = new short[((short[]) in).length];
            System.arraycopy(in, 0, out, 0, out.length);
            return out;
        } else if (!(in instanceof int[])) {
            return in;
        } else {
            out = new int[((int[]) in).length];
            System.arraycopy(in, 0, out, 0, out.length);
            return out;
        }
    }

    /* JADX WARNING: Missing block: B:25:0x0031, code skipped:
            r6 = false;
     */
    /* JADX WARNING: Missing block: B:26:0x0038, code skipped:
            if ((r0.getFormat() instanceof javax.media.format.AudioFormat) == false) goto L_0x0053;
     */
    /* JADX WARNING: Missing block: B:27:0x003a, code skipped:
            r6 = true;
     */
    /* JADX WARNING: Missing block: B:28:0x003b, code skipped:
            r14.cc.process(r0, r6);
            r9 = r14.bufferQ;
     */
    /* JADX WARNING: Missing block: B:29:0x0042, code skipped:
            monitor-enter(r9);
     */
    /* JADX WARNING: Missing block: B:31:?, code skipped:
            r14.bufferQ.readReport();
            r14.bufferQ.notifyAll();
     */
    /* JADX WARNING: Missing block: B:32:0x004d, code skipped:
            monitor-exit(r9);
     */
    /* JADX WARNING: Missing block: B:38:0x0053, code skipped:
            r4 = r0.getTimeStamp();
     */
    /* JADX WARNING: Missing block: B:39:0x005e, code skipped:
            if (r4 >= (r14.lastPreviewTime + r14.previewInterval)) goto L_0x0066;
     */
    /* JADX WARNING: Missing block: B:41:0x0064, code skipped:
            if (r4 > r14.lastPreviewTime) goto L_0x008d;
     */
    /* JADX WARNING: Missing block: B:43:0x006e, code skipped:
            if (mpegVideo.matches(r14.format) == false) goto L_0x0089;
     */
    /* JADX WARNING: Missing block: B:45:0x0083, code skipped:
            if ((((byte[]) r0.getData())[r0.getOffset() + 2] & 7) != 1) goto L_0x003b;
     */
    /* JADX WARNING: Missing block: B:46:0x0085, code skipped:
            r14.lastPreviewTime = r4;
            r6 = true;
     */
    /* JADX WARNING: Missing block: B:47:0x0089, code skipped:
            r14.lastPreviewTime = r4;
            r6 = true;
     */
    /* JADX WARNING: Missing block: B:48:0x008d, code skipped:
            r6 = false;
     */
    /* JADX WARNING: Missing block: B:64:?, code skipped:
            return true;
     */
    public boolean doProcess() {
        /*
        r14 = this;
        r8 = 1;
        r9 = r14.bufferQ;
        monitor-enter(r9);
    L_0x0004:
        r7 = r14.bufferQ;	 Catch:{ all -> 0x0050 }
        r7 = r7.canRead();	 Catch:{ all -> 0x0050 }
        if (r7 != 0) goto L_0x001c;
    L_0x000c:
        r7 = r14.enabled;	 Catch:{ all -> 0x0050 }
        if (r7 == 0) goto L_0x001c;
    L_0x0010:
        r7 = r14.closed;	 Catch:{ all -> 0x0050 }
        if (r7 != 0) goto L_0x001c;
    L_0x0014:
        r7 = r14.bufferQ;	 Catch:{ Exception -> 0x001a }
        r7.wait();	 Catch:{ Exception -> 0x001a }
        goto L_0x0004;
    L_0x001a:
        r7 = move-exception;
        goto L_0x0004;
    L_0x001c:
        r7 = r14.closed;	 Catch:{ all -> 0x0050 }
        if (r7 == 0) goto L_0x0023;
    L_0x0020:
        r7 = 0;
        monitor-exit(r9);	 Catch:{ all -> 0x0050 }
    L_0x0022:
        return r7;
    L_0x0023:
        r7 = r14.enabled;	 Catch:{ all -> 0x0050 }
        if (r7 != 0) goto L_0x002a;
    L_0x0027:
        monitor-exit(r9);	 Catch:{ all -> 0x0050 }
        r7 = r8;
        goto L_0x0022;
    L_0x002a:
        r7 = r14.bufferQ;	 Catch:{ all -> 0x0050 }
        r0 = r7.read();	 Catch:{ all -> 0x0050 }
        monitor-exit(r9);	 Catch:{ all -> 0x0050 }
        r6 = 0;
        r7 = r0.getFormat();
        r7 = r7 instanceof javax.media.format.AudioFormat;
        if (r7 == 0) goto L_0x0053;
    L_0x003a:
        r6 = 1;
    L_0x003b:
        r7 = r14.cc;
        r7.process(r0, r6);
        r9 = r14.bufferQ;
        monitor-enter(r9);
        r7 = r14.bufferQ;	 Catch:{ all -> 0x008f }
        r7.readReport();	 Catch:{ all -> 0x008f }
        r7 = r14.bufferQ;	 Catch:{ all -> 0x008f }
        r7.notifyAll();	 Catch:{ all -> 0x008f }
        monitor-exit(r9);	 Catch:{ all -> 0x008f }
        r7 = r8;
        goto L_0x0022;
    L_0x0050:
        r7 = move-exception;
        monitor-exit(r9);	 Catch:{ all -> 0x0050 }
        throw r7;
    L_0x0053:
        r4 = r0.getTimeStamp();
        r10 = r14.lastPreviewTime;
        r12 = r14.previewInterval;
        r10 = r10 + r12;
        r7 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));
        if (r7 >= 0) goto L_0x0066;
    L_0x0060:
        r10 = r14.lastPreviewTime;
        r7 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));
        if (r7 > 0) goto L_0x008d;
    L_0x0066:
        r7 = mpegVideo;
        r9 = r14.format;
        r7 = r7.matches(r9);
        if (r7 == 0) goto L_0x0089;
    L_0x0070:
        r7 = r0.getData();
        r7 = (byte[]) r7;
        r2 = r7;
        r2 = (byte[]) r2;
        r1 = r0.getOffset();
        r7 = r1 + 2;
        r7 = r2[r7];
        r3 = r7 & 7;
        if (r3 != r8) goto L_0x003b;
    L_0x0085:
        r14.lastPreviewTime = r4;
        r6 = 1;
        goto L_0x003b;
    L_0x0089:
        r14.lastPreviewTime = r4;
        r6 = 1;
        goto L_0x003b;
    L_0x008d:
        r6 = 0;
        goto L_0x003b;
    L_0x008f:
        r7 = move-exception;
        monitor-exit(r9);	 Catch:{ all -> 0x008f }
        throw r7;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.media.control.MonitorAdapter.doProcess():boolean");
    }

    public void finalize() {
        if (this.visualComponent != null) {
            this.visualComponent.remove(this.rateMenu);
            this.visualComponent.removeMouseListener(this.ml);
        }
    }

    public Component getControlComponent() {
        if (this.controlComponent != null) {
            return this.controlComponent;
        }
        if (this.cc == null && !open()) {
            return null;
        }
        Container controlPanel;
        this.controlComponent = this.cc.getControlComponent();
        if ((this.format instanceof AudioFormat) && this.controlComponent != null) {
            controlPanel = new Panel();
            controlPanel.setLayout(new BorderLayout());
            this.cbEnabled = new Checkbox("Monitor Audio");
            controlPanel.add("West", this.cbEnabled);
            controlPanel.add("Center", this.controlComponent);
            this.controlComponent = controlPanel;
            controlPanel.setBackground(Color.lightGray);
        }
        if ((this.format instanceof VideoFormat) && this.controlComponent != null) {
            controlPanel = new Panel();
            controlPanel.setLayout(new BorderLayout());
            this.cbEnabled = new Checkbox("Monitor Video");
            controlPanel.add("South", this.cbEnabled);
            controlPanel.add("Center", this.controlComponent);
            addPopupMenu(this.controlComponent);
            this.controlComponent = controlPanel;
            controlPanel.setBackground(Color.lightGray);
        }
        if (this.cbEnabled != null) {
            this.cbEnabled.setState(isEnabled());
            this.cbEnabled.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent ie) {
                    MonitorAdapter.this.setEnabled(MonitorAdapter.this.cbEnabled.getState());
                }
            });
        }
        return this.controlComponent;
    }

    public Object getOwner() {
        return this.owner;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    /* access modifiers changed from: protected */
    public boolean open() {
        try {
            if (this.format instanceof VideoFormat) {
                VideoFormat vf = this.format;
                this.cc = new VideoCodecChain(vf);
                this.inFrameRate = vf.getFrameRate();
                if (this.inFrameRate < 0.0f) {
                    this.inFrameRate = 30.0f;
                }
                this.inFrameRate = ((float) ((int) (((double) (this.inFrameRate * 10.0f)) + 0.5d))) / 10.0f;
            } else if (this.format instanceof AudioFormat) {
                this.cc = new AudioCodecChain((AudioFormat) this.format);
            }
            if (this.cc == null) {
                return false;
            }
            this.bufferQ = new CircularBuffer(2);
            this.loopThread = new MonitorThread(this);
            return true;
        } catch (UnsupportedFormatException e) {
            Log.warning("Failed to initialize the monitor control: " + e);
            return false;
        }
    }

    /* JADX WARNING: Missing block: B:59:?, code skipped:
            return;
     */
    public void process(javax.media.Buffer r5) {
        /*
        r4 = this;
        if (r5 == 0) goto L_0x0021;
    L_0x0002:
        r1 = r4.previewFrameRate;
        r2 = 0;
        r1 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1));
        if (r1 <= 0) goto L_0x0021;
    L_0x0009:
        r1 = r4.format;
        if (r1 == 0) goto L_0x0021;
    L_0x000d:
        r1 = r5.isEOM();
        if (r1 != 0) goto L_0x0021;
    L_0x0013:
        r1 = r5.isDiscard();
        if (r1 != 0) goto L_0x0021;
    L_0x0019:
        r1 = r5.getFlags();
        r1 = r1 & 512;
        if (r1 == 0) goto L_0x0022;
    L_0x0021:
        return;
    L_0x0022:
        r1 = r4.format;
        r2 = r5.getFormat();
        r1 = r1.matches(r2);
        if (r1 == 0) goto L_0x0021;
    L_0x002e:
        r0 = 0;
        r2 = r4.bufferQ;
        monitor-enter(r2);
    L_0x0032:
        r1 = r4.bufferQ;	 Catch:{ all -> 0x0054 }
        r1 = r1.canWrite();	 Catch:{ all -> 0x0054 }
        if (r1 != 0) goto L_0x004a;
    L_0x003a:
        r1 = r4.enabled;	 Catch:{ all -> 0x0054 }
        if (r1 == 0) goto L_0x004a;
    L_0x003e:
        r1 = r4.closed;	 Catch:{ all -> 0x0054 }
        if (r1 != 0) goto L_0x004a;
    L_0x0042:
        r1 = r4.bufferQ;	 Catch:{ Exception -> 0x0048 }
        r1.wait();	 Catch:{ Exception -> 0x0048 }
        goto L_0x0032;
    L_0x0048:
        r1 = move-exception;
        goto L_0x0032;
    L_0x004a:
        r1 = r4.enabled;	 Catch:{ all -> 0x0054 }
        if (r1 == 0) goto L_0x0052;
    L_0x004e:
        r1 = r4.closed;	 Catch:{ all -> 0x0054 }
        if (r1 == 0) goto L_0x0057;
    L_0x0052:
        monitor-exit(r2);	 Catch:{ all -> 0x0054 }
        goto L_0x0021;
    L_0x0054:
        r1 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x0054 }
        throw r1;
    L_0x0057:
        r1 = r4.bufferQ;	 Catch:{ all -> 0x0054 }
        r0 = r1.getEmptyBuffer();	 Catch:{ all -> 0x0054 }
        monitor-exit(r2);	 Catch:{ all -> 0x0054 }
        r1 = r5.getData();
        r1 = r4.copyData(r1);
        r0.setData(r1);
        r1 = r5.getFlags();
        r0.setFlags(r1);
        r1 = r5.getFormat();
        r0.setFormat(r1);
        r2 = r5.getSequenceNumber();
        r0.setSequenceNumber(r2);
        r1 = r5.getHeader();
        r0.setHeader(r1);
        r1 = r5.getLength();
        r0.setLength(r1);
        r1 = r5.getOffset();
        r0.setOffset(r1);
        r2 = r5.getTimeStamp();
        r0.setTimeStamp(r2);
        r2 = r4.bufferQ;
        monitor-enter(r2);
        r1 = r4.bufferQ;	 Catch:{ all -> 0x00aa }
        r1.writeReport();	 Catch:{ all -> 0x00aa }
        r1 = r4.bufferQ;	 Catch:{ all -> 0x00aa }
        r1.notifyAll();	 Catch:{ all -> 0x00aa }
        monitor-exit(r2);	 Catch:{ all -> 0x00aa }
        goto L_0x0021;
    L_0x00aa:
        r1 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x00aa }
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.media.control.MonitorAdapter.process(javax.media.Buffer):void");
    }

    public void reset() {
        if (this.cc != null) {
            this.cc.reset();
        }
    }

    public boolean setEnabled(boolean on) {
        if (on) {
            if (this.cc != null) {
                this.cc.reset();
            } else if (!open()) {
                return false;
            }
            if (!this.cc.prefetch()) {
                return false;
            }
            synchronized (this.bufferQ) {
                while (this.bufferQ.canRead()) {
                    this.bufferQ.read();
                    this.bufferQ.readReport();
                }
            }
            this.enabled = true;
            this.loopThread.start();
        } else if (!(on || this.cc == null)) {
            this.loopThread.pause();
            synchronized (this.bufferQ) {
                this.enabled = false;
                this.bufferQ.notifyAll();
            }
            this.cc.deallocate();
        }
        return this.enabled;
    }

    public float setPreviewFrameRate(float value) {
        if (value > this.inFrameRate) {
            value = this.inFrameRate;
        }
        this.previewFrameRate = value;
        this.previewInterval = (long) (1.0E9d / ((double) value));
        return value;
    }
}
