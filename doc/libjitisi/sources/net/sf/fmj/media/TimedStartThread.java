package net.sf.fmj.media;

/* compiled from: BasicController */
class TimedStartThread extends TimedActionThread {
    public TimedStartThread(BasicController mc, long tbt) {
        super(mc, tbt);
        setName(getName() + ": TimedStartThread");
    }

    /* access modifiers changed from: protected */
    public void action() {
        this.controller.doStart();
    }

    /* access modifiers changed from: protected */
    public long getTime() {
        return this.controller.getTimeBase().getNanoseconds();
    }
}
