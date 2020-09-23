package net.sf.fmj.ejmf.toolkit.util;

import java.util.EventObject;

public class SourcedTimerEvent extends EventObject {
    private long time;

    public SourcedTimerEvent(Object src, long t) {
        super(src);
        this.time = t;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long t) {
        this.time = t;
    }
}
