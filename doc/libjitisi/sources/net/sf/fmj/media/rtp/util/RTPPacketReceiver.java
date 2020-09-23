package net.sf.fmj.media.rtp.util;

import java.io.IOException;
import javax.media.Buffer;
import javax.media.protocol.PushSourceStream;
import javax.media.protocol.SourceTransferHandler;
import javax.media.rtp.RTPPushDataSource;
import net.sf.fmj.media.CircularBuffer;

public class RTPPacketReceiver implements PacketSource, SourceTransferHandler {
    CircularBuffer bufQue = new CircularBuffer(2);
    boolean closed = false;
    boolean dataRead = false;
    RTPPushDataSource rtpsource = null;

    public RTPPacketReceiver(PushSourceStream pss) {
        pss.setTransferHandler(this);
    }

    public RTPPacketReceiver(RTPPushDataSource rtpsource) {
        this.rtpsource = rtpsource;
        rtpsource.getOutputStream().setTransferHandler(this);
    }

    public void closeSource() {
        synchronized (this.bufQue) {
            this.closed = true;
            this.bufQue.notifyAll();
        }
    }

    public Packet receiveFrom() throws IOException {
        Buffer buf;
        byte[] data;
        int length;
        synchronized (this.bufQue) {
            if (this.dataRead) {
                this.bufQue.readReport();
                this.bufQue.notify();
            }
            while (!this.bufQue.canRead() && !this.closed) {
                try {
                    this.bufQue.wait(1000);
                } catch (InterruptedException e) {
                }
            }
            if (this.closed) {
                buf = null;
                this.dataRead = false;
            } else {
                buf = this.bufQue.read();
                this.dataRead = true;
            }
        }
        if (buf != null) {
            data = (byte[]) buf.getData();
        } else {
            data = new byte[1];
        }
        UDPPacket p = new UDPPacket();
        p.receiptTime = System.currentTimeMillis();
        p.data = data;
        p.offset = 0;
        if (buf != null) {
            length = buf.getLength();
        } else {
            length = 0;
        }
        p.length = length;
        return p;
    }

    public String sourceString() {
        return "RTPPacketReceiver for " + this.rtpsource;
    }

    /* JADX WARNING: Missing block: B:18:0x0027, code skipped:
            r3 = r10.getMinimumTransferSize();
            r1 = (byte[]) r0.getData();
            r2 = 0;
     */
    /* JADX WARNING: Missing block: B:19:0x0035, code skipped:
            if (r1 == null) goto L_0x003a;
     */
    /* JADX WARNING: Missing block: B:21:0x0038, code skipped:
            if (r1.length >= r3) goto L_0x003f;
     */
    /* JADX WARNING: Missing block: B:22:0x003a, code skipped:
            r1 = new byte[r3];
            r0.setData(r1);
     */
    /* JADX WARNING: Missing block: B:25:?, code skipped:
            r2 = r10.read(r1, 0, r3);
     */
    public void transferData(javax.media.protocol.PushSourceStream r10) {
        /*
        r9 = this;
        r8 = 0;
        r5 = r9.bufQue;
        monitor-enter(r5);
    L_0x0004:
        r4 = r9.bufQue;	 Catch:{ all -> 0x005c }
        r4 = r4.canWrite();	 Catch:{ all -> 0x005c }
        if (r4 != 0) goto L_0x001a;
    L_0x000c:
        r4 = r9.closed;	 Catch:{ all -> 0x005c }
        if (r4 != 0) goto L_0x001a;
    L_0x0010:
        r4 = r9.bufQue;	 Catch:{ InterruptedException -> 0x0018 }
        r6 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        r4.wait(r6);	 Catch:{ InterruptedException -> 0x0018 }
        goto L_0x0004;
    L_0x0018:
        r4 = move-exception;
        goto L_0x0004;
    L_0x001a:
        r4 = r9.closed;	 Catch:{ all -> 0x005c }
        if (r4 == 0) goto L_0x0020;
    L_0x001e:
        monitor-exit(r5);	 Catch:{ all -> 0x005c }
    L_0x001f:
        return;
    L_0x0020:
        r4 = r9.bufQue;	 Catch:{ all -> 0x005c }
        r0 = r4.getEmptyBuffer();	 Catch:{ all -> 0x005c }
        monitor-exit(r5);	 Catch:{ all -> 0x005c }
        r3 = r10.getMinimumTransferSize();
        r4 = r0.getData();
        r4 = (byte[]) r4;
        r1 = r4;
        r1 = (byte[]) r1;
        r2 = 0;
        if (r1 == 0) goto L_0x003a;
    L_0x0037:
        r4 = r1.length;
        if (r4 >= r3) goto L_0x003f;
    L_0x003a:
        r1 = new byte[r3];
        r0.setData(r1);
    L_0x003f:
        r4 = 0;
        r2 = r10.read(r1, r4, r3);	 Catch:{ IOException -> 0x005f }
    L_0x0044:
        r0.setLength(r2);
        r0.setOffset(r8);
        r5 = r9.bufQue;
        monitor-enter(r5);
        r4 = r9.bufQue;	 Catch:{ all -> 0x0059 }
        r4.writeReport();	 Catch:{ all -> 0x0059 }
        r4 = r9.bufQue;	 Catch:{ all -> 0x0059 }
        r4.notify();	 Catch:{ all -> 0x0059 }
        monitor-exit(r5);	 Catch:{ all -> 0x0059 }
        goto L_0x001f;
    L_0x0059:
        r4 = move-exception;
        monitor-exit(r5);	 Catch:{ all -> 0x0059 }
        throw r4;
    L_0x005c:
        r4 = move-exception;
        monitor-exit(r5);	 Catch:{ all -> 0x005c }
        throw r4;
    L_0x005f:
        r4 = move-exception;
        goto L_0x0044;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.media.rtp.util.RTPPacketReceiver.transferData(javax.media.protocol.PushSourceStream):void");
    }
}
