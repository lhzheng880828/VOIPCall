package net.sf.fmj.ejmf.toolkit.media;

import javax.media.Time;

public class SyncStartThread extends Thread {
    private AbstractController controller;
    private Time timeBaseStartTime;

    public SyncStartThread(AbstractController controller, Time timeBaseStartTime) {
        this.controller = controller;
        this.timeBaseStartTime = timeBaseStartTime;
    }

    public void run() {
        this.controller.synchronousSyncStart(this.timeBaseStartTime);
    }
}
