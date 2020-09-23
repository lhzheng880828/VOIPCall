package net.java.sip.communicator.impl.netaddr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.java.sip.communicator.service.netaddr.event.ChangeEvent;
import net.java.sip.communicator.service.netaddr.event.NetworkConfigurationChangeListener;
import net.java.sip.communicator.util.Logger;

public class NetworkEventDispatcher implements Runnable {
    private static Logger logger = Logger.getLogger(NetworkEventDispatcher.class);
    private Thread dispatcherThread = null;
    private Map<ChangeEvent, Integer> eventsToDispatch = new LinkedHashMap();
    private final List<NetworkConfigurationChangeListener> listeners = new ArrayList();
    private boolean stopped = true;

    /* access modifiers changed from: 0000 */
    public void addNetworkConfigurationChangeListener(NetworkConfigurationChangeListener listener) {
        synchronized (this.listeners) {
            if (!this.listeners.contains(listener)) {
                this.listeners.add(listener);
                if (this.dispatcherThread == null) {
                    this.dispatcherThread = new Thread(this);
                    this.dispatcherThread.start();
                }
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void removeNetworkConfigurationChangeListener(NetworkConfigurationChangeListener listener) {
        synchronized (this.listeners) {
            this.listeners.remove(listener);
        }
    }

    /* access modifiers changed from: protected */
    public void fireChangeEvent(ChangeEvent evt) {
        fireChangeEvent(evt, 0);
    }

    /* access modifiers changed from: protected */
    public void fireChangeEvent(ChangeEvent evt, int wait) {
        synchronized (this.eventsToDispatch) {
            this.eventsToDispatch.put(evt, Integer.valueOf(wait));
            this.eventsToDispatch.notifyAll();
            if (this.dispatcherThread == null && this.listeners.size() > 0) {
                this.dispatcherThread = new Thread(this);
                this.dispatcherThread.start();
            }
        }
    }

    static void fireChangeEvent(ChangeEvent evt, NetworkConfigurationChangeListener listener) {
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("firing event to " + listener + " evt=" + evt);
            }
            listener.configurationChanged(evt);
        } catch (Throwable e) {
            logger.warn("Error delivering event:" + evt + ", to:" + listener, e);
        }
    }

    public void run() {
        try {
            this.stopped = false;
            while (!this.stopped) {
                Entry<ChangeEvent, Integer> eventToProcess = null;
                synchronized (this.eventsToDispatch) {
                    if (this.eventsToDispatch.size() == 0) {
                        try {
                            this.eventsToDispatch.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                    if (this.listeners.size() == 0) {
                    } else {
                        List<NetworkConfigurationChangeListener> listenersCopy = new ArrayList(this.listeners);
                        Iterator<Entry<ChangeEvent, Integer>> iter = this.eventsToDispatch.entrySet().iterator();
                        if (iter.hasNext()) {
                            eventToProcess = (Entry) iter.next();
                            iter.remove();
                        }
                        if (!(eventToProcess == null || listenersCopy == null)) {
                            if (((Integer) eventToProcess.getValue()).intValue() > 0) {
                                synchronized (this) {
                                    try {
                                        wait((long) ((Integer) eventToProcess.getValue()).intValue());
                                    } catch (Throwable th) {
                                    }
                                }
                            }
                            for (int i = 0; i < listenersCopy.size(); i++) {
                                fireChangeEvent((ChangeEvent) eventToProcess.getKey(), (NetworkConfigurationChangeListener) listenersCopy.get(i));
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            logger.error("Error dispatching thread ended unexpectedly", t);
        }
    }

    public void stop() {
        synchronized (this.eventsToDispatch) {
            this.stopped = true;
            this.eventsToDispatch.notifyAll();
            this.dispatcherThread = null;
        }
    }

    public boolean isRunning() {
        return !this.stopped;
    }
}
