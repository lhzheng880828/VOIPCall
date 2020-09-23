package net.sf.fmj.media;

import java.util.ArrayList;
import java.util.List;
import javax.media.DataSink;
import javax.media.MediaLocator;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkListener;

public abstract class AbstractDataSink implements DataSink {
    private final List<DataSinkListener> listeners = new ArrayList();
    protected MediaLocator outputLocator;

    public void addDataSinkListener(DataSinkListener listener) {
        synchronized (this.listeners) {
            this.listeners.add(listener);
        }
    }

    public MediaLocator getOutputLocator() {
        return this.outputLocator;
    }

    /* access modifiers changed from: protected */
    public void notifyDataSinkListeners(DataSinkEvent event) {
        synchronized (this.listeners) {
        }
        for (DataSinkListener listener : (DataSinkListener[]) this.listeners.toArray(new DataSinkListener[this.listeners.size()])) {
            listener.dataSinkUpdate(event);
        }
    }

    public void removeDataSinkListener(DataSinkListener listener) {
        synchronized (this.listeners) {
            this.listeners.remove(listener);
        }
    }

    public void setOutputLocator(MediaLocator output) {
        this.outputLocator = output;
    }
}
