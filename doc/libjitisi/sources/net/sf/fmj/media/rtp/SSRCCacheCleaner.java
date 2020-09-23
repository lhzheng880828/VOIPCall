package net.sf.fmj.media.rtp;

import java.util.Enumeration;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.event.ByeEvent;
import javax.media.rtp.event.InactiveReceiveStreamEvent;
import javax.media.rtp.event.TimeoutEvent;
import net.sf.fmj.media.rtp.util.RTPMediaThread;

public class SSRCCacheCleaner implements Runnable {
    private static final int DEATHTIME = 1800000;
    private static final int TIMEOUT_MULTIPLIER = 5;
    private SSRCCache cache;
    private boolean killed = false;
    private StreamSynch streamSynch;
    private RTPMediaThread thread;
    boolean timeToClean = false;

    public SSRCCacheCleaner(SSRCCache cache, StreamSynch streamSynch) {
        this.cache = cache;
        this.streamSynch = streamSynch;
        this.thread = new RTPMediaThread(this, "SSRC Cache Cleaner");
        this.thread.useControlPriority();
        this.thread.setDaemon(true);
        this.thread.start();
    }

    public synchronized void cleannow() {
        long time = System.currentTimeMillis();
        if (this.cache.ourssrc != null) {
            double reportInterval = this.cache.calcReportInterval(this.cache.ourssrc.sender, true);
            Enumeration elements = this.cache.cache.elements();
            while (elements.hasMoreElements()) {
                SSRCInfo info = (SSRCInfo) elements.nextElement();
                if (!info.ours) {
                    boolean byepart;
                    RTPSourceInfo sourceInfo;
                    if (info.byeReceived) {
                        if (time - info.byeTime < 1000) {
                            try {
                                Thread.sleep((1000 - time) + info.byeTime);
                            } catch (InterruptedException e) {
                            }
                            time = System.currentTimeMillis();
                        }
                        info.byeTime = 0;
                        info.byeReceived = false;
                        this.cache.remove(info.ssrc);
                        this.streamSynch.remove(info.ssrc);
                        byepart = false;
                        sourceInfo = info.sourceInfo;
                        if (sourceInfo != null && sourceInfo.getStreamCount() == 0) {
                            byepart = true;
                        }
                        ByeEvent evtbye = null;
                        if (info instanceof RecvSSRCInfo) {
                            evtbye = new ByeEvent(this.cache.sm, info.sourceInfo, (ReceiveStream) info, info.byereason, byepart);
                        }
                        if (info instanceof PassiveSSRCInfo) {
                            evtbye = new ByeEvent(this.cache.sm, info.sourceInfo, null, info.byereason, byepart);
                        }
                        this.cache.eventhandler.postEvent(evtbye);
                    } else if (((double) info.lastHeardFrom) + reportInterval <= ((double) time)) {
                        InactiveReceiveStreamEvent event = null;
                        if (!info.inactivesent) {
                            boolean laststream = false;
                            RTPSourceInfo si = info.sourceInfo;
                            if (si != null && si.getStreamCount() == 1) {
                                laststream = true;
                            }
                            if (info instanceof ReceiveStream) {
                                event = new InactiveReceiveStreamEvent(this.cache.sm, info.sourceInfo, (ReceiveStream) info, laststream);
                            } else {
                                reportInterval *= 5.0d;
                                if (((double) info.lastHeardFrom) + reportInterval <= ((double) time)) {
                                    event = new InactiveReceiveStreamEvent(this.cache.sm, info.sourceInfo, null, laststream);
                                }
                            }
                            if (event != null) {
                                this.cache.eventhandler.postEvent(event);
                                info.quiet = true;
                                info.inactivesent = true;
                                info.setAlive(false);
                            }
                        } else if (info.lastHeardFrom + 5000 <= time) {
                            TimeoutEvent evt;
                            this.cache.remove(info.ssrc);
                            byepart = false;
                            sourceInfo = info.sourceInfo;
                            if (sourceInfo != null && sourceInfo.getStreamCount() == 0) {
                                byepart = true;
                            }
                            if (info instanceof ReceiveStream) {
                                evt = new TimeoutEvent(this.cache.sm, info.sourceInfo, (ReceiveStream) info, byepart);
                            } else {
                                evt = new TimeoutEvent(this.cache.sm, info.sourceInfo, null, byepart);
                            }
                            this.cache.eventhandler.postEvent(evt);
                        }
                    }
                }
            }
        }
    }

    public synchronized void run() {
        while (true) {
            try {
                if (this.timeToClean || this.killed) {
                    if (this.killed) {
                        break;
                    }
                    cleannow();
                    this.timeToClean = false;
                } else {
                    wait();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return;
    }

    public synchronized void setClean() {
        this.timeToClean = true;
        notifyAll();
    }

    public synchronized void stop() {
        this.killed = true;
        notifyAll();
    }
}
