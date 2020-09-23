package org.jitsi.impl.neomedia.transform.dtls;

import com.sun.media.format.WavAudioFormat;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.media.rtp.OutputDataStream;
import org.jitsi.bouncycastle.crypto.tls.DatagramTransport;
import org.jitsi.bouncycastle.crypto.tls.TlsUtils;
import org.jitsi.impl.neomedia.AbstractRTPConnector;
import org.jitsi.impl.neomedia.RawPacket;
import org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.WASAPI;
import org.jitsi.util.Logger;

public class DatagramTransportImpl implements DatagramTransport {
    private static final Logger logger = Logger.getLogger(DatagramTransportImpl.class);
    private final int componentID;
    private AbstractRTPConnector connector;
    private final Queue<RawPacket> rawPacketPool = new LinkedBlockingQueue();
    private final ArrayBlockingQueue<RawPacket> receiveQ;
    private final int receiveQCapacity;
    private byte[] sendBuf;
    private int sendBufLength;
    private final Object sendBufSyncRoot = new Object();

    public DatagramTransportImpl(int componentID) {
        switch (componentID) {
            case 1:
            case 2:
                this.componentID = componentID;
                this.receiveQCapacity = 256;
                this.receiveQ = new ArrayBlockingQueue(this.receiveQCapacity);
                return;
            default:
                throw new IllegalArgumentException("componentID");
        }
    }

    private AbstractRTPConnector assertNotClosed(boolean breakOutOfDTLSReliableHandshakeReceiveMessage) throws IOException {
        AbstractRTPConnector connector = this.connector;
        if (connector != null) {
            return connector;
        }
        String msg = getClass().getName() + " is closed!";
        IOException ioe = new IOException(msg);
        logger.error(msg, ioe);
        if (breakOutOfDTLSReliableHandshakeReceiveMessage) {
            breakOutOfDTLSReliableHandshakeReceiveMessage(ioe);
        }
        throw ioe;
    }

    private void breakOutOfDTLSReliableHandshakeReceiveMessage(Throwable cause) {
        for (StackTraceElement stackTraceElement : cause.getStackTrace()) {
            if ("org.jitsi.bouncycastle.crypto.tls.DTLSReliableHandshake".equals(stackTraceElement.getClassName()) && "receiveMessage".equals(stackTraceElement.getMethodName())) {
                throw new IllegalStateException(cause);
            }
        }
    }

    public void close() throws IOException {
        setConnector(null);
    }

    private void doSend(byte[] buf, int off, int len) throws IOException {
        OutputDataStream outputStream;
        flush();
        AbstractRTPConnector connector = assertNotClosed(false);
        switch (this.componentID) {
            case 1:
                outputStream = connector.getDataOutputStream();
                break;
            case 2:
                outputStream = connector.getControlOutputStream();
                break;
            default:
                String msg = "componentID";
                IllegalStateException ise = new IllegalStateException(msg);
                logger.error(msg, ise);
                throw ise;
        }
        outputStream.write(buf, off, len);
    }

    private void flush() throws IOException {
        byte[] buf;
        int len;
        assertNotClosed(false);
        synchronized (this.sendBufSyncRoot) {
            if (this.sendBuf == null || this.sendBufLength == 0) {
                buf = null;
                len = 0;
            } else {
                buf = this.sendBuf;
                this.sendBuf = null;
                len = this.sendBufLength;
                this.sendBufLength = 0;
            }
        }
        if (buf != null) {
            doSend(buf, 0, len);
            synchronized (this.sendBufSyncRoot) {
                if (this.sendBuf == null) {
                    this.sendBuf = buf;
                }
            }
        }
    }

    public int getReceiveLimit() throws IOException {
        AbstractRTPConnector connector = this.connector;
        int receiveLimit = connector == null ? -1 : connector.getReceiveBufferSize();
        if (receiveLimit <= 0) {
            return 4096;
        }
        return receiveLimit;
    }

    public int getSendLimit() throws IOException {
        AbstractRTPConnector connector = this.connector;
        int sendLimit = connector == null ? -1 : connector.getSendBufferSize();
        if (sendLimit <= 0) {
            return 1037;
        }
        return sendLimit;
    }

    /* access modifiers changed from: 0000 */
    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    public void queueReceive(byte[] r8, int r9, int r10) {
        /*
        r7 = this;
        if (r10 <= 0) goto L_0x0048;
    L_0x0002:
        r5 = r7.receiveQ;
        monitor-enter(r5);
        r4 = 0;
        r7.assertNotClosed(r4);	 Catch:{ IOException -> 0x0049 }
        r4 = r7.rawPacketPool;	 Catch:{ all -> 0x0050 }
        r2 = r4.poll();	 Catch:{ all -> 0x0050 }
        r2 = (org.jitsi.impl.neomedia.RawPacket) r2;	 Catch:{ all -> 0x0050 }
        if (r2 == 0) goto L_0x001a;
    L_0x0013:
        r3 = r2.getBuffer();	 Catch:{ all -> 0x0050 }
        r4 = r3.length;	 Catch:{ all -> 0x0050 }
        if (r4 >= r10) goto L_0x0053;
    L_0x001a:
        r3 = new byte[r10];	 Catch:{ all -> 0x0050 }
        r2 = new org.jitsi.impl.neomedia.RawPacket;	 Catch:{ all -> 0x0050 }
        r4 = 0;
        r2.m1852init(r3, r4, r10);	 Catch:{ all -> 0x0050 }
    L_0x0022:
        r4 = 0;
        java.lang.System.arraycopy(r8, r9, r3, r4, r10);	 Catch:{ all -> 0x0050 }
        r4 = r7.receiveQ;	 Catch:{ all -> 0x0050 }
        r4 = r4.size();	 Catch:{ all -> 0x0050 }
        r6 = r7.receiveQCapacity;	 Catch:{ all -> 0x0050 }
        if (r4 != r6) goto L_0x003d;
    L_0x0030:
        r4 = r7.receiveQ;	 Catch:{ all -> 0x0050 }
        r1 = r4.remove();	 Catch:{ all -> 0x0050 }
        r1 = (org.jitsi.impl.neomedia.RawPacket) r1;	 Catch:{ all -> 0x0050 }
        r4 = r7.rawPacketPool;	 Catch:{ all -> 0x0050 }
        r4.offer(r1);	 Catch:{ all -> 0x0050 }
    L_0x003d:
        r4 = r7.receiveQ;	 Catch:{ all -> 0x0050 }
        r4.add(r2);	 Catch:{ all -> 0x0050 }
        r4 = r7.receiveQ;	 Catch:{ all -> 0x0050 }
        r4.notifyAll();	 Catch:{ all -> 0x0050 }
        monitor-exit(r5);	 Catch:{ all -> 0x0050 }
    L_0x0048:
        return;
    L_0x0049:
        r0 = move-exception;
        r4 = new java.lang.IllegalStateException;	 Catch:{ all -> 0x0050 }
        r4.<init>(r0);	 Catch:{ all -> 0x0050 }
        throw r4;	 Catch:{ all -> 0x0050 }
    L_0x0050:
        r4 = move-exception;
        monitor-exit(r5);	 Catch:{ all -> 0x0050 }
        throw r4;
    L_0x0053:
        r3 = r2.getBuffer();	 Catch:{ all -> 0x0050 }
        r2.setLength(r10);	 Catch:{ all -> 0x0050 }
        r4 = 0;
        r2.setOffset(r4);	 Catch:{ all -> 0x0050 }
        goto L_0x0022;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.transform.dtls.DatagramTransportImpl.queueReceive(byte[], int, int):void");
    }

    public int receive(byte[] buf, int off, int len, int waitMillis) throws IOException {
        long enterTime = System.currentTimeMillis();
        int received = -1;
        boolean interrupted = false;
        while (received < len) {
            long timeout;
            if (waitMillis > 0) {
                timeout = (((long) waitMillis) - System.currentTimeMillis()) + enterTime;
                if (timeout == 0) {
                    timeout = -1;
                }
            } else {
                timeout = (long) waitMillis;
            }
            synchronized (this.receiveQ) {
                assertNotClosed(true);
                RawPacket pkt = (RawPacket) this.receiveQ.peek();
                if (pkt != null) {
                    if (received < 0) {
                        received = 0;
                    }
                    int toReceive = len - received;
                    boolean toReceiveIsPositive = toReceive > 0;
                    if (toReceiveIsPositive) {
                        int pktLength = pkt.getLength();
                        int pktOffset = pkt.getOffset();
                        if (toReceive > pktLength) {
                            toReceive = pktLength;
                            toReceiveIsPositive = toReceive > 0;
                        }
                        if (toReceiveIsPositive) {
                            System.arraycopy(pkt.getBuffer(), pktOffset, buf, off + received, toReceive);
                            received += toReceive;
                        }
                        if (toReceive == pktLength) {
                            this.receiveQ.remove();
                            this.rawPacketPool.offer(pkt);
                        } else {
                            pkt.setLength(pktLength - toReceive);
                            pkt.setOffset(pktOffset + toReceive);
                        }
                        if (toReceiveIsPositive) {
                        }
                    }
                }
                if (this.receiveQ.isEmpty()) {
                    if (timeout >= 0) {
                        try {
                            this.receiveQ.wait(timeout);
                        } catch (InterruptedException e) {
                            interrupted = true;
                        }
                    }
                }
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
        return received;
    }

    public void send(byte[] buf, int off, int len) throws IOException {
        assertNotClosed(false);
        if (len >= 13) {
            boolean endOfFlight = false;
            switch (TlsUtils.readUint8(buf, off)) {
                case (short) 20:
                    break;
                case WavAudioFormat.WAVE_FORMAT_DIGIFIX /*22*/:
                    switch (TlsUtils.readUint8(buf, off + 11)) {
                        case (short) 2:
                        case (short) 4:
                        case (short) 11:
                        case (short) 12:
                        case (short) 13:
                        case (short) 15:
                        case (short) 16:
                        case WASAPI.CLSCTX_ALL /*23*/:
                            endOfFlight = false;
                            break;
                        default:
                            endOfFlight = true;
                            break;
                    }
                default:
                    doSend(buf, off, len);
                    return;
            }
            synchronized (this.sendBufSyncRoot) {
                int newSendBufLength = this.sendBufLength + len;
                int sendLimit = getSendLimit();
                if (newSendBufLength <= sendLimit) {
                    if (this.sendBuf == null) {
                        this.sendBuf = new byte[sendLimit];
                        this.sendBufLength = 0;
                    } else if (this.sendBuf.length < sendLimit) {
                        byte[] oldSendBuf = this.sendBuf;
                        this.sendBuf = new byte[sendLimit];
                        System.arraycopy(oldSendBuf, 0, this.sendBuf, 0, Math.min(this.sendBufLength, this.sendBuf.length));
                    }
                    System.arraycopy(buf, off, this.sendBuf, this.sendBufLength, len);
                    this.sendBufLength = newSendBufLength;
                    if (endOfFlight) {
                        flush();
                    }
                } else if (endOfFlight) {
                    doSend(buf, off, len);
                } else {
                    flush();
                    send(buf, off, len);
                }
            }
            return;
        }
        doSend(buf, off, len);
    }

    /* access modifiers changed from: 0000 */
    public void setConnector(AbstractRTPConnector connector) {
        synchronized (this.receiveQ) {
            this.connector = connector;
            this.receiveQ.notifyAll();
        }
    }
}
