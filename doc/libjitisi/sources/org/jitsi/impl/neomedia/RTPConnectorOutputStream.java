package org.jitsi.impl.neomedia;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.LockSupport;
import javax.media.rtp.OutputDataStream;
import net.sf.fmj.ejmf.toolkit.util.TimeSource;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.packetlogging.PacketLoggingService;
import org.jitsi.service.packetlogging.PacketLoggingService.ProtocolName;
import org.jitsi.util.Logger;

public abstract class RTPConnectorOutputStream implements OutputDataStream {
    public static final int MAX_PACKETS_PER_MILLIS_POLICY_PACKET_QUEUE_CAPACITY = 256;
    private static final Logger logger = Logger.getLogger(RTPConnectorOutputStream.class);
    private MaxPacketsPerMillisPolicy maxPacketsPerMillisPolicy;
    private long numberOfPackets = 0;
    private final LinkedBlockingQueue<RawPacket[]> rawPacketArrayPool = new LinkedBlockingQueue();
    private final LinkedBlockingQueue<RawPacket> rawPacketPool = new LinkedBlockingQueue();
    protected final List<InetSocketAddress> targets = new LinkedList();

    private class MaxPacketsPerMillisPolicy {
        private boolean closed = false;
        private int maxPackets = -1;
        private long millisStartTime = 0;
        private final ArrayBlockingQueue<RawPacket> packetQueue = new ArrayBlockingQueue(256);
        private long packetsSentInMillis = 0;
        private long perNanos = -1;
        private Thread sendThread;

        public MaxPacketsPerMillisPolicy(int maxPackets, long perMillis) {
            setMaxPacketsPerMillis(maxPackets, perMillis);
            synchronized (this) {
                if (this.sendThread == null) {
                    this.sendThread = new Thread(getClass().getName(), RTPConnectorOutputStream.this) {
                        public void run() {
                            MaxPacketsPerMillisPolicy.this.runInSendThread();
                        }
                    };
                    this.sendThread.setDaemon(true);
                    this.sendThread.start();
                }
            }
        }

        /* access modifiers changed from: declared_synchronized */
        public synchronized void close() {
            if (!this.closed) {
                this.closed = true;
                this.packetQueue.offer(new RawPacket());
            }
        }

        /* access modifiers changed from: private */
        public void runInSendThread() {
            while (!this.closed) {
                try {
                    try {
                        RawPacket packet = (RawPacket) this.packetQueue.take();
                        if (this.closed) {
                            break;
                        }
                        long time = System.nanoTime();
                        long millisRemainingTime = time - this.millisStartTime;
                        if (this.perNanos < 1 || millisRemainingTime >= this.perNanos) {
                            this.millisStartTime = time;
                            this.packetsSentInMillis = 0;
                        } else if (this.maxPackets > 0 && this.packetsSentInMillis >= ((long) this.maxPackets)) {
                            while (true) {
                                millisRemainingTime = System.nanoTime() - this.millisStartTime;
                                if (millisRemainingTime >= this.perNanos) {
                                    break;
                                }
                                LockSupport.parkNanos(millisRemainingTime);
                            }
                            this.millisStartTime = System.nanoTime();
                            this.packetsSentInMillis = 0;
                        }
                        RTPConnectorOutputStream.this.send(packet);
                        this.packetsSentInMillis++;
                    } catch (InterruptedException e) {
                    }
                } catch (Throwable th) {
                    this.packetQueue.clear();
                    synchronized (this.packetQueue) {
                        if (Thread.currentThread().equals(this.sendThread)) {
                            this.sendThread = null;
                        }
                    }
                }
            }
            this.packetQueue.clear();
            synchronized (this.packetQueue) {
                if (Thread.currentThread().equals(this.sendThread)) {
                    this.sendThread = null;
                }
            }
        }

        public void setMaxPacketsPerMillis(int maxPackets, long perMillis) {
            if (maxPackets < 1) {
                this.maxPackets = -1;
                this.perNanos = -1;
            } else if (perMillis < 1) {
                throw new IllegalArgumentException("perMillis");
            } else {
                this.maxPackets = maxPackets;
                this.perNanos = TimeSource.MICROS_PER_SEC * perMillis;
            }
        }

        public void write(RawPacket packet) {
            while (true) {
                try {
                    this.packetQueue.put(packet);
                    break;
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public abstract void doLogPacket(RawPacket rawPacket, InetSocketAddress inetSocketAddress);

    public abstract boolean isSocketValid();

    public abstract void sendToTarget(RawPacket rawPacket, InetSocketAddress inetSocketAddress) throws IOException;

    public void addTarget(InetAddress remoteAddr, int remotePort) {
        InetSocketAddress target = new InetSocketAddress(remoteAddr, remotePort);
        if (!this.targets.contains(target)) {
            this.targets.add(target);
        }
    }

    public void close() {
        if (this.maxPacketsPerMillisPolicy != null) {
            this.maxPacketsPerMillisPolicy.close();
            this.maxPacketsPerMillisPolicy = null;
        }
        removeTargets();
    }

    /* access modifiers changed from: protected */
    public RawPacket[] createRawPacket(byte[] buffer, int offset, int length) {
        byte[] pktBuffer;
        RawPacket[] pkts = (RawPacket[]) this.rawPacketArrayPool.poll();
        if (pkts == null) {
            pkts = new RawPacket[1];
        }
        RawPacket pkt = (RawPacket) this.rawPacketPool.poll();
        if (pkt == null) {
            pktBuffer = new byte[length];
            pkt = new RawPacket();
        } else {
            pktBuffer = pkt.getBuffer();
        }
        if (pktBuffer.length < length) {
            pktBuffer = new byte[length];
        }
        pkt.setBuffer(pktBuffer);
        pkt.setLength(length);
        pkt.setOffset(0);
        System.arraycopy(buffer, offset, pktBuffer, 0, length);
        pkts[0] = pkt;
        return pkts;
    }

    public boolean removeTarget(InetAddress remoteAddr, int remotePort) {
        Iterator<InetSocketAddress> targetIter = this.targets.iterator();
        while (targetIter.hasNext()) {
            InetSocketAddress target = (InetSocketAddress) targetIter.next();
            if (target.getAddress().equals(remoteAddr) && target.getPort() == remotePort) {
                targetIter.remove();
                return true;
            }
        }
        return false;
    }

    public void removeTargets() {
        this.targets.clear();
    }

    static boolean logPacket(long numOfPacket) {
        return numOfPacket == 1 || numOfPacket == 300 || numOfPacket == 500 || numOfPacket == 1000 || numOfPacket % 5000 == 0;
    }

    /* access modifiers changed from: private */
    public boolean send(RawPacket packet) {
        if (isSocketValid()) {
            this.numberOfPackets++;
            for (InetSocketAddress target : this.targets) {
                try {
                    sendToTarget(packet, target);
                    if (logPacket(this.numberOfPackets)) {
                        PacketLoggingService packetLogging = LibJitsi.getPacketLoggingService();
                        if (packetLogging != null && packetLogging.isLoggingEnabled(ProtocolName.RTP)) {
                            doLogPacket(packet, target);
                        }
                    }
                } catch (IOException e) {
                    this.rawPacketPool.offer(packet);
                    return false;
                }
            }
            this.rawPacketPool.offer(packet);
            return true;
        }
        this.rawPacketPool.offer(packet);
        return false;
    }

    public void setMaxPacketsPerMillis(int maxPackets, long perMillis) {
        if (this.maxPacketsPerMillisPolicy != null) {
            this.maxPacketsPerMillisPolicy.setMaxPacketsPerMillis(maxPackets, perMillis);
        } else if (maxPackets <= 0) {
        } else {
            if (perMillis < 1) {
                throw new IllegalArgumentException("perMillis");
            }
            this.maxPacketsPerMillisPolicy = new MaxPacketsPerMillisPolicy(maxPackets, perMillis);
        }
    }

    public int write(byte[] buffer, int offset, int length) {
        if (logger.isDebugEnabled() && this.targets.isEmpty()) {
            logger.debug("Write called without targets!", new Throwable());
        }
        RawPacket[] pkts = createRawPacket(buffer, offset, length);
        boolean fail = false;
        for (int i = 0; i < pkts.length; i++) {
            RawPacket pkt = pkts[i];
            pkts[i] = null;
            if (!(pkt == null || fail)) {
                if (this.maxPacketsPerMillisPolicy != null) {
                    this.maxPacketsPerMillisPolicy.write(pkt);
                } else if (!send(pkt)) {
                    fail = true;
                }
            }
            if (pkt != null && fail) {
                this.rawPacketPool.offer(pkt);
            }
        }
        this.rawPacketArrayPool.offer(pkts);
        return fail ? -1 : length;
    }

    public void setPriority(int priority) {
    }
}
