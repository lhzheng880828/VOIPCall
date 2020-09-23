package net.sf.fmj.media.rtp;

import java.net.InetAddress;
import java.util.Random;
import net.sf.fmj.media.Log;
import net.sf.fmj.media.rtp.util.RTPMediaThread;

public class RTCPReporter implements Runnable {
    SSRCCache cache;
    boolean closed = false;
    String cname;
    InetAddress host;
    Random myrand;
    RTPMediaThread reportthread;
    boolean restart = false;
    RTCPTransmitter transmit;

    public RTCPReporter(SSRCCache cache, RTCPTransmitter t) {
        this.cache = cache;
        setTransmitter(t);
        this.reportthread = new RTPMediaThread(this, "RTCP Reporter");
        this.reportthread.useControlPriority();
        this.reportthread.setDaemon(true);
        this.reportthread.start();
    }

    public void close(String reason) {
        synchronized (this.reportthread) {
            this.closed = true;
            this.reportthread.notify();
        }
        releasessrc(reason);
        this.transmit.close();
    }

    public void releasessrc(String reason) {
        this.transmit.bye(reason);
        this.transmit.ssrcInfo.setOurs(false);
        this.transmit.ssrcInfo = null;
    }

    public void run() {
        if (this.restart) {
            this.restart = false;
        }
        while (true) {
            double delay = this.cache.calcReportInterval(this.cache.ourssrc.sender, false);
            synchronized (this.reportthread) {
                try {
                    this.reportthread.wait((long) delay);
                } catch (InterruptedException e) {
                    Log.dumpStack(e);
                }
            }
            if (!this.closed) {
                if (this.restart) {
                    this.restart = false;
                } else {
                    this.transmit.report();
                }
            } else {
                return;
            }
        }
    }

    public void setTransmitter(RTCPTransmitter t) {
        this.transmit = t;
    }
}
