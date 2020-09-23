package net.sf.fmj.media.rtp;

import java.util.Vector;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.RemoteListener;
import javax.media.rtp.SendStreamListener;
import javax.media.rtp.SessionListener;
import javax.media.rtp.event.RTPEvent;
import javax.media.rtp.event.ReceiveStreamEvent;
import javax.media.rtp.event.RemoteEvent;
import javax.media.rtp.event.SendStreamEvent;
import javax.media.rtp.event.SessionEvent;
import net.sf.fmj.media.rtp.util.RTPMediaThread;

public class RTPEventHandler extends RTPMediaThread {
    private Vector<RTPEvent> eventQueue = new Vector();
    private boolean killed = false;
    private RTPSessionMgr sm;

    public RTPEventHandler(RTPSessionMgr sm) {
        super("RTPEventHandler");
        this.sm = sm;
        useControlPriority();
        setDaemon(true);
        start();
    }

    public synchronized void close() {
        this.killed = true;
        notifyAll();
    }

    /* access modifiers changed from: protected */
    public void dispatchEvents() {
        synchronized (this) {
            while (this.eventQueue.size() == 0 && !this.killed) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
            if (this.killed) {
                return;
            }
            RTPEvent evt = (RTPEvent) this.eventQueue.elementAt(0);
            this.eventQueue.removeElementAt(0);
            processEvent(evt);
        }
    }

    public synchronized void postEvent(RTPEvent evt) {
        this.eventQueue.addElement(evt);
        notifyAll();
    }

    /* access modifiers changed from: protected */
    public void processEvent(RTPEvent evt) {
        int i;
        if (evt instanceof SessionEvent) {
            for (i = 0; i < this.sm.sessionlistener.size(); i++) {
                SessionListener sl = (SessionListener) this.sm.sessionlistener.elementAt(i);
                if (sl != null) {
                    sl.update((SessionEvent) evt);
                }
            }
        } else if (evt instanceof RemoteEvent) {
            for (i = 0; i < this.sm.remotelistener.size(); i++) {
                RemoteListener sl2 = (RemoteListener) this.sm.remotelistener.elementAt(i);
                if (sl2 != null) {
                    sl2.update((RemoteEvent) evt);
                }
            }
        } else if (evt instanceof ReceiveStreamEvent) {
            for (i = 0; i < this.sm.streamlistener.size(); i++) {
                ReceiveStreamListener sl3 = (ReceiveStreamListener) this.sm.streamlistener.elementAt(i);
                if (sl3 != null) {
                    sl3.update((ReceiveStreamEvent) evt);
                }
            }
        } else if (evt instanceof SendStreamEvent) {
            for (i = 0; i < this.sm.sendstreamlistener.size(); i++) {
                SendStreamListener sl4 = (SendStreamListener) this.sm.sendstreamlistener.elementAt(i);
                if (sl4 != null) {
                    sl4.update((SendStreamEvent) evt);
                }
            }
        }
    }

    public void run() {
        while (!this.killed) {
            dispatchEvents();
        }
    }
}
