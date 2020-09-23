package net.sf.fmj.media;

/* compiled from: BasicController */
class RealizeWorkThread extends StateTransitionWorkThread {
    public RealizeWorkThread(BasicController mc) {
        this.controller = mc;
        setName(getName() + ": " + mc);
    }

    /* access modifiers changed from: protected */
    public void aborted() {
        this.controller.abortRealize();
    }

    /* access modifiers changed from: protected */
    public void completed() {
        this.controller.completeRealize();
    }

    /* access modifiers changed from: protected */
    public void failed() {
        this.controller.doFailedRealize();
    }

    /* access modifiers changed from: protected */
    public boolean process() {
        return this.controller.doRealize();
    }
}
