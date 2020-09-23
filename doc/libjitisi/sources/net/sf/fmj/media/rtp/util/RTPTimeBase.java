package net.sf.fmj.media.rtp.util;

import java.util.Vector;
import javax.media.Time;
import javax.media.TimeBase;
import net.sf.fmj.media.Log;

public class RTPTimeBase implements TimeBase {
    static int SSRC_UNDEFINED = 0;
    static Vector timeBases = new Vector();
    String cname;
    RTPTimeReporter master = null;
    long offset = 0;
    boolean offsetUpdatable = true;
    long origin = 0;
    Vector reporters = new Vector();

    public static RTPTimeBase find(RTPTimeReporter rtptimereporter, String s) {
        Throwable th;
        synchronized (timeBases) {
            RTPTimeBase rtptimebase1;
            RTPTimeBase rtptimebase12;
            int i = 0;
            while (i < timeBases.size()) {
                try {
                    RTPTimeBase rtptimebase = (RTPTimeBase) timeBases.elementAt(i);
                    if (rtptimebase.cname != null && rtptimebase.cname.equals(s)) {
                        rtptimebase1 = rtptimebase;
                        break;
                    }
                    i++;
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            }
            rtptimebase1 = null;
            if (rtptimebase1 == null) {
                try {
                    Log.comment("Created RTP time base for session: " + s + "\n");
                    rtptimebase12 = new RTPTimeBase(s);
                    timeBases.addElement(rtptimebase12);
                } catch (Throwable th3) {
                    th = th3;
                    rtptimebase12 = rtptimebase1;
                    throw th;
                }
            }
            rtptimebase12 = rtptimebase1;
            if (rtptimereporter != null) {
                if (rtptimebase12.getMaster() == null) {
                    rtptimebase12.setMaster(rtptimereporter);
                }
                rtptimebase12.reporters.addElement(rtptimereporter);
            }
            RTPTimeBase rtptimebase2 = rtptimebase12;
            return rtptimebase2;
        }
    }

    public static RTPTimeBase getMapper(String s) {
        RTPTimeBase rtptimebase;
        synchronized (timeBases) {
            rtptimebase = find(null, s);
        }
        return rtptimebase;
    }

    public static RTPTimeBase getMapperUpdatable(String s) {
        synchronized (timeBases) {
            RTPTimeBase rtptimebase = find(null, s);
            if (rtptimebase.offsetUpdatable) {
                rtptimebase.offsetUpdatable = false;
                RTPTimeBase rtptimebase1 = rtptimebase;
                return rtptimebase1;
            }
            return null;
        }
    }

    public static void remove(RTPTimeReporter rtptimereporter, String s) {
        synchronized (timeBases) {
            int i = 0;
            while (i < timeBases.size()) {
                RTPTimeBase rtptimebase = (RTPTimeBase) timeBases.elementAt(i);
                if (rtptimebase.cname == null || s == null || !rtptimebase.cname.equals(s)) {
                    i++;
                } else {
                    rtptimebase.reporters.removeElement(rtptimereporter);
                    if (rtptimebase.reporters.size() == 0) {
                        rtptimebase.master = null;
                        timeBases.removeElement(rtptimebase);
                    } else {
                        synchronized (rtptimebase) {
                            if (rtptimebase.master == rtptimereporter) {
                                rtptimebase.setMaster((RTPTimeReporter) rtptimebase.reporters.elementAt(0));
                            }
                        }
                    }
                }
            }
        }
    }

    public static void returnMapperUpdatable(RTPTimeBase rtptimebase) {
        synchronized (timeBases) {
            rtptimebase.offsetUpdatable = true;
        }
    }

    RTPTimeBase(String s) {
        this.cname = s;
    }

    public synchronized RTPTimeReporter getMaster() {
        return this.master;
    }

    public synchronized long getNanoseconds() {
        return this.master == null ? 0 : this.master.getRTPTime();
    }

    public long getOffset() {
        return this.offset;
    }

    public long getOrigin() {
        return this.origin;
    }

    public Time getTime() {
        return new Time(getNanoseconds());
    }

    public synchronized void setMaster(RTPTimeReporter rtptimereporter) {
        this.master = rtptimereporter;
    }

    public synchronized void setOffset(long l) {
        this.offset = l;
    }

    public synchronized void setOrigin(long l) {
        this.origin = l;
    }
}
