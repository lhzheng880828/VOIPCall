package net.sf.fmj.media;

/* compiled from: BasicController */
class ConfigureWorkThread extends StateTransitionWorkThread {
    public ConfigureWorkThread(BasicController mc) {
        this.controller = mc;
        setName(getName() + ": " + mc);
    }

    /* access modifiers changed from: protected */
    public void aborted() {
        this.controller.abortConfigure();
    }

    /* access modifiers changed from: protected */
    public void completed() {
        this.controller.completeConfigure();
    }

    /* access modifiers changed from: protected */
    public void failed() {
        this.controller.doFailedConfigure();
    }

    /* access modifiers changed from: protected */
    public boolean process() {
        return this.controller.doConfigure();
    }
}
