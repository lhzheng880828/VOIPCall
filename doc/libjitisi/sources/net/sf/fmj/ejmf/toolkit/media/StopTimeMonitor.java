package net.sf.fmj.ejmf.toolkit.media;

import com.lti.utils.synchronization.CloseableThread;
import javax.media.Clock;
import javax.media.ClockStoppedException;
import javax.media.Controller;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DeallocateEvent;
import javax.media.MediaTimeSetEvent;
import javax.media.RateChangeEvent;
import javax.media.StartEvent;
import javax.media.StopEvent;
import javax.media.StopTimeChangeEvent;
import javax.media.Time;
import net.sf.fmj.ejmf.toolkit.util.TimeSource;

public class StopTimeMonitor extends CloseableThread implements ControllerListener {
    private AbstractController controller;
    private boolean wokenUp;

    public StopTimeMonitor(AbstractController controller, String threadName) {
        setName(threadName);
        this.controller = controller;
        controller.addControllerListener(this);
        setDaemon(true);
    }

    public synchronized void controllerUpdate(ControllerEvent e) {
        if ((e instanceof StopTimeChangeEvent) || (e instanceof RateChangeEvent) || (e instanceof MediaTimeSetEvent) || (e instanceof StartEvent) || ((e instanceof StopEvent) && !(e instanceof DeallocateEvent))) {
            this.wokenUp = true;
            notifyAll();
        }
    }

    private long getWaitTime(Time stopTime) throws ClockStoppedException {
        return (this.controller.mapToTimeBase(stopTime).getNanoseconds() - this.controller.getTimeBase().getNanoseconds()) / TimeSource.MICROS_PER_SEC;
    }

    private synchronized void monitorStopTime() throws InterruptedException {
        while (!isClosing()) {
            Time stopTime;
            while (true) {
                if (this.controller.getState() == Controller.Started) {
                    stopTime = this.controller.getStopTime();
                    if (stopTime != Clock.RESET) {
                        break;
                    }
                }
                wait();
            }
            this.wokenUp = false;
            try {
                long waittime = getWaitTime(stopTime);
                if (waittime > 0) {
                    wait(waittime);
                }
                if (!this.wokenUp) {
                    this.controller.stopAtTime();
                    this.controller.setStopTime(Clock.RESET);
                }
            } catch (ClockStoppedException e) {
            }
        }
    }

    public void run() {
        try {
            monitorStopTime();
        } catch (InterruptedException e) {
        }
        setClosed();
    }
}
