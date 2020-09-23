package net.sf.fmj.media;

import net.sf.fmj.media.util.LoopThread;

/* compiled from: BasicRendererModule */
class RenderThread extends LoopThread {
    BasicRendererModule module;

    public RenderThread(BasicRendererModule module) {
        this.module = module;
        setName(getName() + ": " + module.renderer);
        useVideoPriority();
    }

    /* access modifiers changed from: protected */
    public boolean process() {
        return this.module.doProcess();
    }
}
