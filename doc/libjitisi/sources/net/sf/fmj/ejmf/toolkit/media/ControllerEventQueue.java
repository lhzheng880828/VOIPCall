package net.sf.fmj.ejmf.toolkit.media;

import com.lti.utils.synchronization.CloseableThread;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import net.sf.fmj.utility.LoggerSingleton;

public class ControllerEventQueue extends CloseableThread {
    private static final Logger logger = LoggerSingleton.logger;
    Vector eventQueue = new Vector();
    Vector listeners;

    public ControllerEventQueue(Vector listeners, String threadName) {
        setName(threadName);
        this.listeners = listeners;
        setDaemon(true);
    }

    private void dispatchEvent(ControllerEvent event) {
        Vector l;
        synchronized (this.listeners) {
            l = (Vector) this.listeners.clone();
        }
        for (int i = 0; i < l.size(); i++) {
            ControllerListener o = l.elementAt(i);
            if (o instanceof ControllerListener) {
                try {
                    o.controllerUpdate(event);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Exception occurred during event dispatching:" + e, e);
                }
            }
        }
    }

    private void monitorEvents() throws InterruptedException {
        while (!isClosing()) {
            Vector v;
            synchronized (this) {
                while (this.eventQueue.size() == 0) {
                    wait();
                }
                v = (Vector) this.eventQueue.clone();
                this.eventQueue.removeAllElements();
            }
            for (int i = 0; i < v.size(); i++) {
                dispatchEvent((ControllerEvent) v.elementAt(i));
            }
        }
    }

    public synchronized void postEvent(ControllerEvent event) {
        this.eventQueue.addElement(event);
        notify();
    }

    public void run() {
        try {
            monitorEvents();
        } catch (InterruptedException e) {
        }
        setClosed();
    }
}
