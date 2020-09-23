package net.sf.fmj.media.control;

import net.sf.fmj.media.util.LoopThread;

/* compiled from: MonitorAdapter */
class MonitorThread extends LoopThread {
    MonitorAdapter ad;

    public MonitorThread(MonitorAdapter ad) {
        setName(getName() + " : MonitorAdapter");
        useVideoPriority();
        this.ad = ad;
    }

    /* access modifiers changed from: protected */
    public boolean process() {
        return this.ad.doProcess();
    }
}
