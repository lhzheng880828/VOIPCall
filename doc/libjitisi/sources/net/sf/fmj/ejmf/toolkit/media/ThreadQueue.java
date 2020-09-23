package net.sf.fmj.ejmf.toolkit.media;

import com.lti.utils.synchronization.CloseableThread;
import java.util.Vector;

public class ThreadQueue extends CloseableThread {
    private Vector queue = new Vector();
    private Thread running;

    public ThreadQueue(String threadName) {
        setName(threadName);
        setDaemon(true);
    }

    public synchronized void addThread(Thread t) {
        this.queue.addElement(t);
        notify();
    }

    public void run() {
        while (!isClosing()) {
            try {
                synchronized (this) {
                    while (this.queue.size() == 0) {
                        wait();
                    }
                    this.running = (Thread) this.queue.elementAt(0);
                    this.queue.removeElementAt(0);
                }
                this.running.start();
                this.running.join();
            } catch (InterruptedException e) {
            }
        }
        setClosed();
    }

    public synchronized void stopThreads() {
        if (this.running != null) {
            this.running.stop();
        }
        int n = this.queue.size();
        for (int i = 0; i < n; i++) {
            ((Thread) this.queue.elementAt(i)).stop();
        }
        this.queue.removeAllElements();
    }
}
