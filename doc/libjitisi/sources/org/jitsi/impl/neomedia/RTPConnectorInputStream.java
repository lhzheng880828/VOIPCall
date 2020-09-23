package org.jitsi.impl.neomedia;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PushSourceStream;
import javax.media.protocol.SourceTransferHandler;
import org.ice4j.socket.DatagramPacketFilter;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.packetlogging.PacketLoggingService;
import org.jitsi.service.packetlogging.PacketLoggingService.ProtocolName;
import org.jitsi.util.Logger;
import org.jitsi.util.OSUtils;

public abstract class RTPConnectorInputStream implements PushSourceStream, Runnable {
    private static final Object[] EMPTY_CONTROLS = new Object[0];
    public static final int PACKET_RECEIVE_BUFFER_LENGTH = 4096;
    private static final Logger logger = Logger.getLogger(RTPConnectorInputStream.class);
    private final byte[] buffer = new byte[4096];
    protected boolean closed;
    private DatagramPacketFilter[] datagramPacketFilters;
    protected boolean ioError = false;
    private RawPacket pkt;
    private final Object pktSyncRoot = new Object();
    private final Queue<RawPacket[]> rawPacketArrayPool = new LinkedBlockingQueue();
    private final Queue<RawPacket> rawPacketPool = new LinkedBlockingQueue();
    protected Thread receiverThread = null;
    private SourceTransferHandler transferHandler;

    public abstract void doLogPacket(DatagramPacket datagramPacket);

    public abstract void receivePacket(DatagramPacket datagramPacket) throws IOException;

    public RTPConnectorInputStream() {
        addDatagramPacketFilter(new DatagramPacketFilter() {
            private long numberOfPackets = 0;

            public boolean accept(DatagramPacket p) {
                this.numberOfPackets++;
                if (RTPConnectorOutputStream.logPacket(this.numberOfPackets)) {
                    PacketLoggingService packetLogging = LibJitsi.getPacketLoggingService();
                    if (packetLogging != null && packetLogging.isLoggingEnabled(ProtocolName.RTP)) {
                        RTPConnectorInputStream.this.doLogPacket(p);
                    }
                }
                return true;
            }
        });
    }

    public synchronized void close() {
    }

    /* access modifiers changed from: protected */
    public RawPacket[] createRawPacket(DatagramPacket datagramPacket) {
        RawPacket[] pkts = (RawPacket[]) this.rawPacketArrayPool.poll();
        if (pkts == null) {
            pkts = new RawPacket[1];
        }
        RawPacket pkt = (RawPacket) this.rawPacketPool.poll();
        if (pkt == null) {
            pkt = new RawPacket();
        }
        pkt.setBuffer(datagramPacket.getData());
        pkt.setLength(datagramPacket.getLength());
        pkt.setOffset(datagramPacket.getOffset());
        pkts[0] = pkt;
        return pkts;
    }

    public boolean endOfStream() {
        return false;
    }

    public ContentDescriptor getContentDescriptor() {
        return null;
    }

    public long getContentLength() {
        return -1;
    }

    public Object getControl(String controlType) {
        return null;
    }

    public Object[] getControls() {
        return EMPTY_CONTROLS;
    }

    public int getMinimumTransferSize() {
        return 2048;
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
        if (buffer == null) {
            throw new NullPointerException("buffer");
        } else if (this.ioError) {
            return -1;
        } else {
            RawPacket pkt;
            synchronized (this.pktSyncRoot) {
                pkt = this.pkt;
                this.pkt = null;
            }
            if (pkt == null) {
                return 0;
            }
            boolean poolPkt = true;
            try {
                int pktLength = pkt.getLength();
                if (length < pktLength) {
                    throw new IOException("Input buffer not big enough for " + pktLength);
                } else if (pkt.getBuffer() == null) {
                    throw new NullPointerException("pkt.buffer null, pkt.length " + pktLength + ", pkt.offset " + pkt.getOffset());
                } else {
                    System.arraycopy(pkt.getBuffer(), pkt.getOffset(), buffer, offset, pktLength);
                    if (1 == null) {
                        synchronized (this.pktSyncRoot) {
                            if (this.pkt == null) {
                                this.pkt = pkt;
                            } else {
                                poolPkt = true;
                            }
                        }
                    }
                    if (!poolPkt) {
                        return pktLength;
                    }
                    pkt.setBuffer(null);
                    pkt.setLength(0);
                    pkt.setOffset(0);
                    this.rawPacketPool.offer(pkt);
                    return pktLength;
                }
            } catch (Throwable th) {
                if (1 == null) {
                    synchronized (this.pktSyncRoot) {
                        if (this.pkt == null) {
                            this.pkt = pkt;
                        } else {
                            poolPkt = true;
                        }
                    }
                }
                if (poolPkt) {
                    pkt.setBuffer(null);
                    pkt.setLength(0);
                    pkt.setOffset(0);
                    this.rawPacketPool.offer(pkt);
                }
            }
        }
    }

    public void run() {
        ThreadDeath t;
        DatagramPacket p = new DatagramPacket(this.buffer, 0, 4096);
        while (!this.closed) {
            try {
                boolean accept;
                int i;
                if (OSUtils.IS_ANDROID) {
                    p.setLength(4096);
                }
                receivePacket(p);
                DatagramPacketFilter[] datagramPacketFilters = getDatagramPacketFilters();
                if (datagramPacketFilters == null) {
                    accept = true;
                } else {
                    accept = true;
                    i = 0;
                    while (i < datagramPacketFilters.length) {
                        try {
                            if (!datagramPacketFilters[i].accept(p)) {
                                accept = false;
                                break;
                            }
                            i++;
                        } catch (Throwable t2) {
                            if (t2 instanceof ThreadDeath) {
                                t = (ThreadDeath) t2;
                            }
                        }
                    }
                }
                if (accept) {
                    RawPacket[] pkts = createRawPacket(p);
                    for (i = 0; i < pkts.length; i++) {
                        RawPacket pkt = pkts[i];
                        pkts[i] = null;
                        if (pkt != null) {
                            if (pkt.isInvalid()) {
                                pkt.setBuffer(null);
                                pkt.setLength(0);
                                pkt.setOffset(0);
                                this.rawPacketPool.offer(pkt);
                            } else {
                                RawPacket oldPkt;
                                synchronized (this.pktSyncRoot) {
                                    oldPkt = this.pkt;
                                    this.pkt = pkt;
                                }
                                if (oldPkt != null) {
                                    oldPkt.setBuffer(null);
                                    oldPkt.setLength(0);
                                    oldPkt.setOffset(0);
                                    this.rawPacketPool.offer(pkt);
                                }
                                if (!(this.transferHandler == null || this.closed)) {
                                    try {
                                        this.transferHandler.transferData(this);
                                    } catch (Throwable t22) {
                                        if (t22 instanceof ThreadDeath) {
                                            t = (ThreadDeath) t22;
                                        } else {
                                            logger.warn("An RTP packet may have not been fully handled.", t22);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    this.rawPacketArrayPool.offer(pkts);
                }
            } catch (IOException e) {
                this.ioError = true;
                return;
            }
        }
    }

    public void setTransferHandler(SourceTransferHandler transferHandler) {
        if (!this.closed) {
            this.transferHandler = transferHandler;
        }
    }

    public void setPriority(int priority) {
    }

    public synchronized DatagramPacketFilter[] getDatagramPacketFilters() {
        return this.datagramPacketFilters;
    }

    public synchronized void addDatagramPacketFilter(DatagramPacketFilter datagramPacketFilter) {
        if (datagramPacketFilter == null) {
            throw new NullPointerException("datagramPacketFilter");
        } else if (this.datagramPacketFilters == null) {
            this.datagramPacketFilters = new DatagramPacketFilter[]{datagramPacketFilter};
        } else {
            for (Object equals : this.datagramPacketFilters) {
                if (datagramPacketFilter.equals(equals)) {
                    break;
                }
            }
            DatagramPacketFilter[] newDatagramPacketFilters = new DatagramPacketFilter[(length + 1)];
            System.arraycopy(this.datagramPacketFilters, 0, newDatagramPacketFilters, 0, length);
            newDatagramPacketFilters[length] = datagramPacketFilter;
            this.datagramPacketFilters = newDatagramPacketFilters;
        }
    }
}
