package net.sf.fmj.media;

import net.sf.fmj.media.util.MediaThread;

/* compiled from: BasicPlayer */
class PlayThread extends MediaThread {
    BasicPlayer player;

    public PlayThread(BasicPlayer player) {
        this.player = player;
        setName(getName() + " (PlayThread)");
        useControlPriority();
    }

    public void run() {
        this.player.play();
    }
}
