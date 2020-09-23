package net.sf.fmj.media.datasink;

import java.util.Enumeration;
import java.util.Vector;
import javax.media.DataSink;
import javax.media.datasink.DataSinkErrorEvent;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.EndOfStreamEvent;

public abstract class BasicDataSink implements DataSink {
    protected Vector listeners = new Vector(1);

    public void addDataSinkListener(DataSinkListener dsl) {
        if (dsl != null && !this.listeners.contains(dsl)) {
            this.listeners.addElement(dsl);
        }
    }

    /* access modifiers changed from: protected */
    public void removeAllListeners() {
        this.listeners.removeAllElements();
    }

    public void removeDataSinkListener(DataSinkListener dsl) {
        if (dsl != null) {
            this.listeners.removeElement(dsl);
        }
    }

    /* access modifiers changed from: protected|final */
    public final void sendDataSinkErrorEvent(String reason) {
        sendEvent(new DataSinkErrorEvent(this, reason));
    }

    /* access modifiers changed from: protected|final */
    public final void sendEndofStreamEvent() {
        sendEvent(new EndOfStreamEvent(this));
    }

    /* access modifiers changed from: protected */
    public void sendEvent(DataSinkEvent event) {
        if (!this.listeners.isEmpty()) {
            synchronized (this.listeners) {
                Enumeration list = this.listeners.elements();
                while (list.hasMoreElements()) {
                    ((DataSinkListener) list.nextElement()).dataSinkUpdate(event);
                }
            }
        }
    }
}
