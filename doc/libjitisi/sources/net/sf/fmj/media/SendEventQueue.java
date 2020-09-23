package net.sf.fmj.media;

import javax.media.ControllerEvent;
import net.sf.fmj.media.util.ThreadedEventQueue;

/* compiled from: BasicController */
class SendEventQueue extends ThreadedEventQueue {
    private BasicController controller;

    public SendEventQueue(BasicController c) {
        this.controller = c;
    }

    public void processEvent(ControllerEvent evt) {
        this.controller.dispatchEvent(evt);
    }
}
