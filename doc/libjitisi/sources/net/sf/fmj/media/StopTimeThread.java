package net.sf.fmj.media;

/* compiled from: BasicController */
class StopTimeThread extends TimedActionThread {
    public StopTimeThread(BasicController mc, long nanoseconds) {
        super(mc, nanoseconds);
        setName(getName() + ": StopTimeThread");
        this.wakeupTime = getTime() + nanoseconds;
    }

    /* access modifiers changed from: protected */
    public void action() {
        this.controller.stopAtTime();
    }

    /* access modifiers changed from: protected */
    public long getTime() {
        return this.controller.getMediaNanoseconds();
    }
}
