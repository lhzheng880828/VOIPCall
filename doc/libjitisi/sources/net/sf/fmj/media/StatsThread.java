package net.sf.fmj.media;

import javax.media.Controller;
import net.sf.fmj.media.util.LoopThread;

/* compiled from: BasicPlayer */
class StatsThread extends LoopThread {
    int pausecount = -1;
    BasicPlayer player;

    public StatsThread(BasicPlayer p) {
        this.player = p;
    }

    /* access modifiers changed from: protected */
    public boolean process() {
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
        if (!waitHereIfPaused()) {
            return false;
        }
        if (this.player.getState() == Controller.Started) {
            this.pausecount = -1;
            this.player.updateStats();
        } else if (this.pausecount < 5) {
            this.pausecount++;
            this.player.updateStats();
        }
        return true;
    }
}
