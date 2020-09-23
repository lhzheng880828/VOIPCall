package net.sf.fmj.media;

/* compiled from: BasicController */
class PrefetchWorkThread extends StateTransitionWorkThread {
    public PrefetchWorkThread(BasicController mc) {
        this.controller = mc;
        setName(getName() + ": " + mc);
    }

    /* access modifiers changed from: protected */
    public void aborted() {
        this.controller.abortPrefetch();
    }

    /* access modifiers changed from: protected */
    public void completed() {
        this.controller.completePrefetch();
    }

    /* access modifiers changed from: protected */
    public void failed() {
        this.controller.doFailedPrefetch();
    }

    /* access modifiers changed from: protected */
    public boolean process() {
        return this.controller.doPrefetch();
    }
}
