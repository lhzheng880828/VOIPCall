package net.sf.fmj.ejmf.toolkit.util;

import org.jitsi.android.util.java.awt.event.ActionEvent;
import org.jitsi.android.util.java.awt.event.ActionListener;
import org.jitsi.android.util.javax.swing.Timer;
import org.jitsi.android.util.javax.swing.event.EventListenerList;

public class SourcedTimer implements ActionListener {
    protected static int _defaultGran = 1000;
    private Timer baseTimer;
    /* access modifiers changed from: private */
    public SourcedTimerEvent event;
    private EventListenerList listenerList;
    /* access modifiers changed from: private */
    public Object[] listeners;
    private TimeSource source;
    private boolean started;

    public SourcedTimer(TimeSource src) {
        this(src, _defaultGran);
    }

    public SourcedTimer(TimeSource src, int granularity) {
        this.listenerList = null;
        this.started = false;
        this.source = src;
        this.event = new SourcedTimerEvent(this, 0);
        this.baseTimer = new Timer(granularity, this);
        this.baseTimer.setInitialDelay(0);
    }

    public SourcedTimer(TimeSource src, Timer timer) {
        this.listenerList = null;
        this.started = false;
        this.source = src;
        this.event = new SourcedTimerEvent(this, 0);
        this.baseTimer = timer;
    }

    public void actionPerformed(ActionEvent e) {
        runNotifyThread(this.source.getTime());
    }

    public void addSourcedTimerListener(SourcedTimerListener l) {
        if (this.listenerList == null) {
            this.listenerList = new EventListenerList();
        }
        this.listenerList.add(SourcedTimerListener.class, l);
    }

    public long getConversionDivisor() {
        return this.source.getConversionDivisor();
    }

    private void runNotifyThread(long nsecs) {
        this.event.setTime(nsecs);
        this.listeners = this.listenerList.getListenerList();
        new Thread("SourcedTimer Notify Thread") {
            public void run() {
                for (int i = SourcedTimer.this.listeners.length - 2; i >= 0; i -= 2) {
                    if (SourcedTimer.this.listeners[i] == SourcedTimerListener.class) {
                        ((SourcedTimerListener) SourcedTimer.this.listeners[i + 1]).timerUpdate(SourcedTimer.this.event);
                    }
                }
            }
        }.start();
    }

    public void start() {
        if (!this.started) {
            this.baseTimer.start();
            runNotifyThread(0);
        }
    }

    public void stop() {
        this.started = false;
        this.baseTimer.stop();
        runNotifyThread(this.source.getTime());
    }
}
