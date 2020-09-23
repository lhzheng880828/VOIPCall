package net.sf.fmj.media.rtp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.UnknownHostException;
import net.sf.fmj.media.rtp.util.Packet;
import net.sf.fmj.media.rtp.util.PacketConsumer;
import net.sf.fmj.media.rtp.util.PacketForwarder;
import net.sf.fmj.media.rtp.util.PacketSource;

public class RTCPReceiver implements PacketConsumer {
    private static final int RR = 2;
    private static final int SR = 1;
    SSRCCache cache;
    private boolean rtcpstarted;
    private boolean sentrecvstrmap;
    private int type;

    public RTCPReceiver(SSRCCache ssrccache) {
        this.rtcpstarted = false;
        this.sentrecvstrmap = false;
        this.type = 0;
        this.cache = ssrccache;
        SSRCInfo ssrcinfo = ssrccache.lookup(ssrccache.ourssrc.ssrc);
    }

    public RTCPReceiver(SSRCCache ssrccache, DatagramSocket datagramsocket, StreamSynch streamsynch) {
        this(ssrccache, new RTCPRawReceiver(datagramsocket, ssrccache.sm.defaultstats, streamsynch));
    }

    public RTCPReceiver(SSRCCache ssrccache, int i, String s, StreamSynch streamsynch) throws UnknownHostException, IOException {
        this(ssrccache, new RTCPRawReceiver(i | 1, s, ssrccache.sm.defaultstats, streamsynch));
    }

    public RTCPReceiver(SSRCCache ssrccache, PacketSource packetsource) {
        this(ssrccache);
        new PacketForwarder(packetsource, this).startPF();
    }

    public void closeConsumer() {
    }

    public String consumerString() {
        return "RTCP Packet Receiver/Collector";
    }

    public void sendTo(Packet packet) {
        sendTo((RTCPPacket) packet);
    }

    /* JADX WARNING: Removed duplicated region for block: B:196:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:128:0x054b  */
    public void sendTo(net.sf.fmj.media.rtp.RTCPPacket r53) {
        /*
        r52 = this;
        r46 = 0;
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.sm;
        r22 = r3.isUnicast();
        if (r22 == 0) goto L_0x0056;
    L_0x000e:
        r0 = r52;
        r3 = r0.rtcpstarted;
        if (r3 != 0) goto L_0x0078;
    L_0x0014:
        r0 = r52;
        r3 = r0.cache;
        r4 = r3.sm;
        r0 = r53;
        r3 = r0.base;
        r3 = (net.sf.fmj.media.rtp.util.UDPPacket) r3;
        r3 = r3.remoteAddress;
        r4.startRTCPReports(r3);
        r3 = 1;
        r0 = r52;
        r0.rtcpstarted = r3;
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.sm;
        r3 = r3.controladdress;
        r15 = r3.getAddress();
        r3 = 3;
        r3 = r15[r3];
        r0 = r3 & 255;
        r24 = r0;
        r0 = r24;
        r3 = r0 & 255;
        r4 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        if (r3 != r4) goto L_0x005e;
    L_0x0045:
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.sm;
        r0 = r52;
        r4 = r0.cache;
        r4 = r4.sm;
        r4 = r4.controladdress;
        r3.addUnicastAddr(r4);
    L_0x0056:
        r0 = r53;
        r3 = r0.type;
        switch(r3) {
            case -1: goto L_0x009e;
            case 200: goto L_0x00dc;
            case 201: goto L_0x028b;
            case 202: goto L_0x03d2;
            case 203: goto L_0x04eb;
            case 204: goto L_0x0659;
            default: goto L_0x005d;
        };
    L_0x005d:
        return;
    L_0x005e:
        r26 = 0;
        r23 = 1;
        r26 = java.net.InetAddress.getLocalHost();	 Catch:{ UnknownHostException -> 0x0074 }
    L_0x0066:
        if (r23 == 0) goto L_0x0056;
    L_0x0068:
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.sm;
        r0 = r26;
        r3.addUnicastAddr(r0);
        goto L_0x0056;
    L_0x0074:
        r51 = move-exception;
        r23 = 0;
        goto L_0x0066;
    L_0x0078:
        r0 = r52;
        r3 = r0.cache;
        r4 = r3.sm;
        r0 = r53;
        r3 = r0.base;
        r3 = (net.sf.fmj.media.rtp.util.UDPPacket) r3;
        r3 = r3.remoteAddress;
        r3 = r4.isSenderDefaultAddr(r3);
        if (r3 != 0) goto L_0x0056;
    L_0x008c:
        r0 = r52;
        r3 = r0.cache;
        r4 = r3.sm;
        r0 = r53;
        r3 = r0.base;
        r3 = (net.sf.fmj.media.rtp.util.UDPPacket) r3;
        r3 = r3.remoteAddress;
        r4.addUnicastAddr(r3);
        goto L_0x0056;
    L_0x009e:
        r39 = r53;
        r39 = (net.sf.fmj.media.rtp.RTCPCompoundPacket) r39;
        r0 = r52;
        r3 = r0.cache;
        r0 = r39;
        r4 = r0.length;
        r3.updateavgrtcpsize(r4);
        r27 = 0;
    L_0x00af:
        r0 = r39;
        r3 = r0.packets;
        r3 = r3.length;
        r0 = r27;
        if (r0 >= r3) goto L_0x00c6;
    L_0x00b8:
        r0 = r39;
        r3 = r0.packets;
        r3 = r3[r27];
        r0 = r52;
        r0.sendTo(r3);
        r27 = r27 + 1;
        goto L_0x00af;
    L_0x00c6:
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.sm;
        r3 = r3.cleaner;
        if (r3 == 0) goto L_0x005d;
    L_0x00d0:
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.sm;
        r3 = r3.cleaner;
        r3.setClean();
        goto L_0x005d;
    L_0x00dc:
        r43 = r53;
        r43 = (net.sf.fmj.media.rtp.RTCPSRPacket) r43;
        r3 = 1;
        r0 = r52;
        r0.type = r3;
        r0 = r53;
        r3 = r0.base;
        r3 = r3 instanceof net.sf.fmj.media.rtp.util.UDPPacket;
        if (r3 == 0) goto L_0x01d4;
    L_0x00ed:
        r0 = r52;
        r4 = r0.cache;
        r0 = r43;
        r5 = r0.ssrc;
        r0 = r53;
        r3 = r0.base;
        r3 = (net.sf.fmj.media.rtp.util.UDPPacket) r3;
        r6 = r3.remoteAddress;
        r0 = r53;
        r3 = r0.base;
        r3 = (net.sf.fmj.media.rtp.util.UDPPacket) r3;
        r3 = r3.remotePort;
        r9 = 1;
        r46 = r4.get(r5, r6, r3, r9);
    L_0x010a:
        if (r46 == 0) goto L_0x005d;
    L_0x010c:
        r3 = 1;
        r0 = r46;
        r0.setAlive(r3);
        r0 = r43;
        r4 = r0.ntptimestampmsw;
        r3 = 32;
        r4 = r4 << r3;
        r0 = r43;
        r10 = r0.ntptimestamplsw;
        r4 = r4 + r10;
        r0 = r46;
        r0.lastSRntptimestamp = r4;
        r0 = r43;
        r4 = r0.rtptimestamp;
        r0 = r46;
        r0.lastSRrtptimestamp = r4;
        r0 = r43;
        r4 = r0.receiptTime;
        r0 = r46;
        r0.lastSRreceiptTime = r4;
        r0 = r43;
        r4 = r0.receiptTime;
        r0 = r46;
        r0.lastRTCPreceiptTime = r4;
        r0 = r43;
        r4 = r0.receiptTime;
        r0 = r46;
        r0.lastHeardFrom = r4;
        r0 = r46;
        r3 = r0.quiet;
        if (r3 == 0) goto L_0x0175;
    L_0x0148:
        r3 = 0;
        r0 = r46;
        r0.quiet = r3;
        r16 = 0;
        r0 = r46;
        r3 = r0 instanceof javax.media.rtp.ReceiveStream;
        if (r3 == 0) goto L_0x01e5;
    L_0x0155:
        r16 = new javax.media.rtp.event.ActiveReceiveStreamEvent;
        r0 = r52;
        r3 = r0.cache;
        r4 = r3.sm;
        r0 = r46;
        r5 = r0.sourceInfo;
        r3 = r46;
        r3 = (javax.media.rtp.ReceiveStream) r3;
        r0 = r16;
        r0.m346init(r4, r5, r3);
    L_0x016a:
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.eventhandler;
        r0 = r16;
        r3.postEvent(r0);
    L_0x0175:
        r0 = r43;
        r4 = r0.packetcount;
        r0 = r46;
        r0.lastSRpacketcount = r4;
        r0 = r43;
        r4 = r0.octetcount;
        r0 = r46;
        r0.lastSRoctetcount = r4;
        r29 = 0;
    L_0x0187:
        r0 = r43;
        r3 = r0.reports;
        r3 = r3.length;
        r0 = r29;
        if (r0 >= r3) goto L_0x0209;
    L_0x0190:
        r0 = r43;
        r3 = r0.reports;
        r3 = r3[r29];
        r0 = r43;
        r4 = r0.receiptTime;
        r3.receiptTime = r4;
        r0 = r43;
        r3 = r0.reports;
        r3 = r3[r29];
        r0 = r3.ssrc;
        r31 = r0;
        r0 = r46;
        r3 = r0.reports;
        r0 = r31;
        r3 = r3.get(r0);
        r3 = (net.sf.fmj.media.rtp.RTCPReportBlock[]) r3;
        r20 = r3;
        r20 = (net.sf.fmj.media.rtp.RTCPReportBlock[]) r20;
        if (r20 != 0) goto L_0x01f9;
    L_0x01b8:
        r3 = 2;
        r0 = new net.sf.fmj.media.rtp.RTCPReportBlock[r3];
        r20 = r0;
        r3 = 0;
        r0 = r43;
        r4 = r0.reports;
        r4 = r4[r29];
        r20[r3] = r4;
        r0 = r46;
        r3 = r0.reports;
        r0 = r31;
        r1 = r20;
        r3.put(r0, r1);
    L_0x01d1:
        r29 = r29 + 1;
        goto L_0x0187;
    L_0x01d4:
        r0 = r52;
        r3 = r0.cache;
        r0 = r43;
        r4 = r0.ssrc;
        r5 = 0;
        r6 = 0;
        r9 = 1;
        r46 = r3.get(r4, r5, r6, r9);
        goto L_0x010a;
    L_0x01e5:
        r16 = new javax.media.rtp.event.ActiveReceiveStreamEvent;
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.sm;
        r0 = r46;
        r4 = r0.sourceInfo;
        r5 = 0;
        r0 = r16;
        r0.m346init(r3, r4, r5);
        goto L_0x016a;
    L_0x01f9:
        r3 = 1;
        r4 = 0;
        r4 = r20[r4];
        r20[r3] = r4;
        r3 = 0;
        r0 = r43;
        r4 = r0.reports;
        r4 = r4[r29];
        r20[r3] = r4;
        goto L_0x01d1;
    L_0x0209:
        r0 = r46;
        r3 = r0.probation;
        if (r3 > 0) goto L_0x005d;
    L_0x020f:
        r0 = r46;
        r3 = r0.newpartsent;
        if (r3 != 0) goto L_0x023c;
    L_0x0215:
        r0 = r46;
        r3 = r0.sourceInfo;
        if (r3 == 0) goto L_0x023c;
    L_0x021b:
        r33 = new javax.media.rtp.event.NewParticipantEvent;
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.sm;
        r0 = r46;
        r4 = r0.sourceInfo;
        r0 = r33;
        r0.m357init(r3, r4);
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.eventhandler;
        r0 = r33;
        r3.postEvent(r0);
        r3 = 1;
        r0 = r46;
        r0.newpartsent = r3;
    L_0x023c:
        r0 = r46;
        r3 = r0.recvstrmap;
        if (r3 != 0) goto L_0x026d;
    L_0x0242:
        r0 = r46;
        r3 = r0.sourceInfo;
        if (r3 == 0) goto L_0x026d;
    L_0x0248:
        r3 = 1;
        r0 = r46;
        r0.recvstrmap = r3;
        r49 = new javax.media.rtp.event.StreamMappedEvent;
        r0 = r52;
        r3 = r0.cache;
        r4 = r3.sm;
        r3 = r46;
        r3 = (javax.media.rtp.ReceiveStream) r3;
        r0 = r46;
        r5 = r0.sourceInfo;
        r0 = r49;
        r0.m366init(r4, r3, r5);
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.eventhandler;
        r0 = r49;
        r3.postEvent(r0);
    L_0x026d:
        r45 = new javax.media.rtp.event.SenderReportEvent;
        r0 = r52;
        r3 = r0.cache;
        r4 = r3.sm;
        r3 = r46;
        r3 = (javax.media.rtp.rtcp.SenderReport) r3;
        r0 = r45;
        r0.m364init(r4, r3);
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.eventhandler;
        r0 = r45;
        r3.postEvent(r0);
        goto L_0x005d;
    L_0x028b:
        r40 = r53;
        r40 = (net.sf.fmj.media.rtp.RTCPRRPacket) r40;
        r3 = 2;
        r0 = r52;
        r0.type = r3;
        r0 = r53;
        r3 = r0.base;
        r3 = r3 instanceof net.sf.fmj.media.rtp.util.UDPPacket;
        if (r3 == 0) goto L_0x0353;
    L_0x029c:
        r0 = r52;
        r4 = r0.cache;
        r0 = r40;
        r5 = r0.ssrc;
        r0 = r53;
        r3 = r0.base;
        r3 = (net.sf.fmj.media.rtp.util.UDPPacket) r3;
        r6 = r3.remoteAddress;
        r0 = r53;
        r3 = r0.base;
        r3 = (net.sf.fmj.media.rtp.util.UDPPacket) r3;
        r3 = r3.remotePort;
        r9 = 2;
        r46 = r4.get(r5, r6, r3, r9);
    L_0x02b9:
        if (r46 == 0) goto L_0x005d;
    L_0x02bb:
        r3 = 1;
        r0 = r46;
        r0.setAlive(r3);
        r0 = r40;
        r4 = r0.receiptTime;
        r0 = r46;
        r0.lastRTCPreceiptTime = r4;
        r0 = r40;
        r4 = r0.receiptTime;
        r0 = r46;
        r0.lastHeardFrom = r4;
        r0 = r46;
        r3 = r0.quiet;
        if (r3 == 0) goto L_0x0304;
    L_0x02d7:
        r3 = 0;
        r0 = r46;
        r0.quiet = r3;
        r17 = 0;
        r0 = r46;
        r3 = r0 instanceof javax.media.rtp.ReceiveStream;
        if (r3 == 0) goto L_0x0364;
    L_0x02e4:
        r17 = new javax.media.rtp.event.ActiveReceiveStreamEvent;
        r0 = r52;
        r3 = r0.cache;
        r4 = r3.sm;
        r0 = r46;
        r5 = r0.sourceInfo;
        r3 = r46;
        r3 = (javax.media.rtp.ReceiveStream) r3;
        r0 = r17;
        r0.m346init(r4, r5, r3);
    L_0x02f9:
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.eventhandler;
        r0 = r17;
        r3.postEvent(r0);
    L_0x0304:
        r25 = 0;
    L_0x0306:
        r0 = r40;
        r3 = r0.reports;
        r3 = r3.length;
        r0 = r25;
        if (r0 >= r3) goto L_0x0387;
    L_0x030f:
        r0 = r40;
        r3 = r0.reports;
        r3 = r3[r25];
        r0 = r40;
        r4 = r0.receiptTime;
        r3.receiptTime = r4;
        r0 = r40;
        r3 = r0.reports;
        r3 = r3[r25];
        r0 = r3.ssrc;
        r28 = r0;
        r0 = r46;
        r3 = r0.reports;
        r0 = r28;
        r3 = r3.get(r0);
        r3 = (net.sf.fmj.media.rtp.RTCPReportBlock[]) r3;
        r21 = r3;
        r21 = (net.sf.fmj.media.rtp.RTCPReportBlock[]) r21;
        if (r21 != 0) goto L_0x0377;
    L_0x0337:
        r3 = 2;
        r0 = new net.sf.fmj.media.rtp.RTCPReportBlock[r3];
        r21 = r0;
        r3 = 0;
        r0 = r40;
        r4 = r0.reports;
        r4 = r4[r25];
        r21[r3] = r4;
        r0 = r46;
        r3 = r0.reports;
        r0 = r28;
        r1 = r21;
        r3.put(r0, r1);
    L_0x0350:
        r25 = r25 + 1;
        goto L_0x0306;
    L_0x0353:
        r0 = r52;
        r3 = r0.cache;
        r0 = r40;
        r4 = r0.ssrc;
        r5 = 0;
        r6 = 0;
        r9 = 2;
        r46 = r3.get(r4, r5, r6, r9);
        goto L_0x02b9;
    L_0x0364:
        r17 = new javax.media.rtp.event.ActiveReceiveStreamEvent;
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.sm;
        r0 = r46;
        r4 = r0.sourceInfo;
        r5 = 0;
        r0 = r17;
        r0.m346init(r3, r4, r5);
        goto L_0x02f9;
    L_0x0377:
        r3 = 1;
        r4 = 0;
        r4 = r21[r4];
        r21[r3] = r4;
        r3 = 0;
        r0 = r40;
        r4 = r0.reports;
        r4 = r4[r25];
        r21[r3] = r4;
        goto L_0x0350;
    L_0x0387:
        r0 = r46;
        r3 = r0.newpartsent;
        if (r3 != 0) goto L_0x03b4;
    L_0x038d:
        r0 = r46;
        r3 = r0.sourceInfo;
        if (r3 == 0) goto L_0x03b4;
    L_0x0393:
        r34 = new javax.media.rtp.event.NewParticipantEvent;
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.sm;
        r0 = r46;
        r4 = r0.sourceInfo;
        r0 = r34;
        r0.m357init(r3, r4);
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.eventhandler;
        r0 = r34;
        r3.postEvent(r0);
        r3 = 1;
        r0 = r46;
        r0.newpartsent = r3;
    L_0x03b4:
        r36 = new javax.media.rtp.event.ReceiverReportEvent;
        r0 = r52;
        r3 = r0.cache;
        r4 = r3.sm;
        r3 = r46;
        r3 = (javax.media.rtp.rtcp.ReceiverReport) r3;
        r0 = r36;
        r0.m361init(r4, r3);
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.eventhandler;
        r0 = r36;
        r3.postEvent(r0);
        goto L_0x005d;
    L_0x03d2:
        r42 = r53;
        r42 = (net.sf.fmj.media.rtp.RTCPSDESPacket) r42;
        r30 = 0;
    L_0x03d8:
        r0 = r42;
        r3 = r0.sdes;
        r3 = r3.length;
        r0 = r30;
        if (r0 >= r3) goto L_0x0441;
    L_0x03e1:
        r0 = r42;
        r3 = r0.sdes;
        r41 = r3[r30];
        r0 = r52;
        r3 = r0.type;
        r4 = 1;
        if (r3 != r4) goto L_0x0413;
    L_0x03ee:
        r0 = r53;
        r3 = r0.base;
        r3 = r3 instanceof net.sf.fmj.media.rtp.util.UDPPacket;
        if (r3 == 0) goto L_0x04b0;
    L_0x03f6:
        r0 = r52;
        r4 = r0.cache;
        r0 = r41;
        r5 = r0.ssrc;
        r0 = r53;
        r3 = r0.base;
        r3 = (net.sf.fmj.media.rtp.util.UDPPacket) r3;
        r6 = r3.remoteAddress;
        r0 = r53;
        r3 = r0.base;
        r3 = (net.sf.fmj.media.rtp.util.UDPPacket) r3;
        r3 = r3.remotePort;
        r9 = 1;
        r46 = r4.get(r5, r6, r3, r9);
    L_0x0413:
        r0 = r52;
        r3 = r0.type;
        r4 = 2;
        if (r3 != r4) goto L_0x043f;
    L_0x041a:
        r0 = r53;
        r3 = r0.base;
        r3 = r3 instanceof net.sf.fmj.media.rtp.util.UDPPacket;
        if (r3 == 0) goto L_0x04c1;
    L_0x0422:
        r0 = r52;
        r4 = r0.cache;
        r0 = r41;
        r5 = r0.ssrc;
        r0 = r53;
        r3 = r0.base;
        r3 = (net.sf.fmj.media.rtp.util.UDPPacket) r3;
        r6 = r3.remoteAddress;
        r0 = r53;
        r3 = r0.base;
        r3 = (net.sf.fmj.media.rtp.util.UDPPacket) r3;
        r3 = r3.remotePort;
        r9 = 2;
        r46 = r4.get(r5, r6, r3, r9);
    L_0x043f:
        if (r46 != 0) goto L_0x04d2;
    L_0x0441:
        if (r46 == 0) goto L_0x0470;
    L_0x0443:
        r0 = r46;
        r3 = r0.newpartsent;
        if (r3 != 0) goto L_0x0470;
    L_0x0449:
        r0 = r46;
        r3 = r0.sourceInfo;
        if (r3 == 0) goto L_0x0470;
    L_0x044f:
        r35 = new javax.media.rtp.event.NewParticipantEvent;
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.sm;
        r0 = r46;
        r4 = r0.sourceInfo;
        r0 = r35;
        r0.m357init(r3, r4);
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.eventhandler;
        r0 = r35;
        r3.postEvent(r0);
        r3 = 1;
        r0 = r46;
        r0.newpartsent = r3;
    L_0x0470:
        if (r46 == 0) goto L_0x04a9;
    L_0x0472:
        r0 = r46;
        r3 = r0.recvstrmap;
        if (r3 != 0) goto L_0x04a9;
    L_0x0478:
        r0 = r46;
        r3 = r0.sourceInfo;
        if (r3 == 0) goto L_0x04a9;
    L_0x047e:
        r0 = r46;
        r3 = r0 instanceof net.sf.fmj.media.rtp.RecvSSRCInfo;
        if (r3 == 0) goto L_0x04a9;
    L_0x0484:
        r3 = 1;
        r0 = r46;
        r0.recvstrmap = r3;
        r50 = new javax.media.rtp.event.StreamMappedEvent;
        r0 = r52;
        r3 = r0.cache;
        r4 = r3.sm;
        r3 = r46;
        r3 = (javax.media.rtp.ReceiveStream) r3;
        r0 = r46;
        r5 = r0.sourceInfo;
        r0 = r50;
        r0.m366init(r4, r3, r5);
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.eventhandler;
        r0 = r50;
        r3.postEvent(r0);
    L_0x04a9:
        r3 = 0;
        r0 = r52;
        r0.type = r3;
        goto L_0x005d;
    L_0x04b0:
        r0 = r52;
        r3 = r0.cache;
        r0 = r41;
        r4 = r0.ssrc;
        r5 = 0;
        r6 = 0;
        r9 = 1;
        r46 = r3.get(r4, r5, r6, r9);
        goto L_0x0413;
    L_0x04c1:
        r0 = r52;
        r3 = r0.cache;
        r0 = r41;
        r4 = r0.ssrc;
        r5 = 0;
        r6 = 0;
        r9 = 2;
        r46 = r3.get(r4, r5, r6, r9);
        goto L_0x043f;
    L_0x04d2:
        r3 = 1;
        r0 = r46;
        r0.setAlive(r3);
        r0 = r42;
        r4 = r0.receiptTime;
        r0 = r46;
        r0.lastHeardFrom = r4;
        r0 = r46;
        r1 = r41;
        r0.addSDESInfo(r1);
        r30 = r30 + 1;
        goto L_0x03d8;
    L_0x04eb:
        r38 = r53;
        r38 = (net.sf.fmj.media.rtp.RTCPBYEPacket) r38;
        r0 = r53;
        r3 = r0.base;
        r3 = r3 instanceof net.sf.fmj.media.rtp.util.UDPPacket;
        if (r3 == 0) goto L_0x05f9;
    L_0x04f7:
        r0 = r52;
        r4 = r0.cache;
        r0 = r38;
        r3 = r0.ssrc;
        r5 = 0;
        r5 = r3[r5];
        r0 = r53;
        r3 = r0.base;
        r3 = (net.sf.fmj.media.rtp.util.UDPPacket) r3;
        r6 = r3.remoteAddress;
        r0 = r53;
        r3 = r0.base;
        r3 = (net.sf.fmj.media.rtp.util.UDPPacket) r3;
        r3 = r3.remotePort;
        r47 = r4.get(r5, r6, r3);
    L_0x0516:
        r32 = 0;
    L_0x0518:
        r0 = r38;
        r3 = r0.ssrc;
        r3 = r3.length;
        r0 = r32;
        if (r0 >= r3) goto L_0x0549;
    L_0x0521:
        r0 = r53;
        r3 = r0.base;
        r3 = r3 instanceof net.sf.fmj.media.rtp.util.UDPPacket;
        if (r3 == 0) goto L_0x060c;
    L_0x0529:
        r0 = r52;
        r4 = r0.cache;
        r0 = r38;
        r3 = r0.ssrc;
        r5 = r3[r32];
        r0 = r53;
        r3 = r0.base;
        r3 = (net.sf.fmj.media.rtp.util.UDPPacket) r3;
        r6 = r3.remoteAddress;
        r0 = r53;
        r3 = r0.base;
        r3 = (net.sf.fmj.media.rtp.util.UDPPacket) r3;
        r3 = r3.remotePort;
        r47 = r4.get(r5, r6, r3);
    L_0x0547:
        if (r47 != 0) goto L_0x061e;
    L_0x0549:
        if (r47 == 0) goto L_0x005d;
    L_0x054b:
        r0 = r47;
        r3 = r0.quiet;
        if (r3 == 0) goto L_0x057e;
    L_0x0551:
        r3 = 0;
        r0 = r47;
        r0.quiet = r3;
        r18 = 0;
        r0 = r47;
        r3 = r0 instanceof javax.media.rtp.ReceiveStream;
        if (r3 == 0) goto L_0x0645;
    L_0x055e:
        r18 = new javax.media.rtp.event.ActiveReceiveStreamEvent;
        r0 = r52;
        r3 = r0.cache;
        r4 = r3.sm;
        r0 = r47;
        r5 = r0.sourceInfo;
        r3 = r47;
        r3 = (javax.media.rtp.ReceiveStream) r3;
        r0 = r18;
        r0.m346init(r4, r5, r3);
    L_0x0573:
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.eventhandler;
        r0 = r18;
        r3.postEvent(r0);
    L_0x057e:
        r3 = new java.lang.String;
        r0 = r38;
        r4 = r0.reason;
        r3.<init>(r4);
        r0 = r47;
        r0.byereason = r3;
        r0 = r47;
        r3 = r0.byeReceived;
        if (r3 != 0) goto L_0x005d;
    L_0x0591:
        r7 = 0;
        r0 = r47;
        r0 = r0.sourceInfo;
        r44 = r0;
        if (r44 == 0) goto L_0x05a1;
    L_0x059a:
        r3 = r44.getStreamCount();
        if (r3 != 0) goto L_0x05a1;
    L_0x05a0:
        r7 = 1;
    L_0x05a1:
        r2 = 0;
        r0 = r47;
        r3 = r0 instanceof net.sf.fmj.media.rtp.RecvSSRCInfo;
        if (r3 == 0) goto L_0x05c4;
    L_0x05a8:
        r2 = new javax.media.rtp.event.ByeEvent;
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.sm;
        r0 = r47;
        r4 = r0.sourceInfo;
        r5 = r47;
        r5 = (javax.media.rtp.ReceiveStream) r5;
        r6 = new java.lang.String;
        r0 = r38;
        r9 = r0.reason;
        r6.<init>(r9);
        r2.m351init(r3, r4, r5, r6, r7);
    L_0x05c4:
        r0 = r47;
        r3 = r0 instanceof net.sf.fmj.media.rtp.PassiveSSRCInfo;
        if (r3 == 0) goto L_0x05e3;
    L_0x05ca:
        r2 = new javax.media.rtp.event.ByeEvent;
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.sm;
        r0 = r47;
        r4 = r0.sourceInfo;
        r5 = 0;
        r6 = new java.lang.String;
        r0 = r38;
        r9 = r0.reason;
        r6.<init>(r9);
        r2.m351init(r3, r4, r5, r6, r7);
    L_0x05e3:
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.eventhandler;
        r3.postEvent(r2);
        r0 = r52;
        r3 = r0.cache;
        r0 = r47;
        r4 = r0.ssrc;
        r3.remove(r4);
        goto L_0x005d;
    L_0x05f9:
        r0 = r52;
        r3 = r0.cache;
        r0 = r38;
        r4 = r0.ssrc;
        r5 = 0;
        r4 = r4[r5];
        r5 = 0;
        r6 = 0;
        r47 = r3.get(r4, r5, r6);
        goto L_0x0516;
    L_0x060c:
        r0 = r52;
        r3 = r0.cache;
        r0 = r38;
        r4 = r0.ssrc;
        r4 = r4[r32];
        r5 = 0;
        r6 = 0;
        r47 = r3.get(r4, r5, r6);
        goto L_0x0547;
    L_0x061e:
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.byestate;
        if (r3 != 0) goto L_0x0641;
    L_0x0626:
        r3 = 0;
        r0 = r47;
        r0.setAlive(r3);
        r3 = 1;
        r0 = r47;
        r0.byeReceived = r3;
        r0 = r53;
        r4 = r0.receiptTime;
        r0 = r47;
        r0.byeTime = r4;
        r0 = r38;
        r4 = r0.receiptTime;
        r0 = r47;
        r0.lastHeardFrom = r4;
    L_0x0641:
        r32 = r32 + 1;
        goto L_0x0518;
    L_0x0645:
        r18 = new javax.media.rtp.event.ActiveReceiveStreamEvent;
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.sm;
        r0 = r47;
        r4 = r0.sourceInfo;
        r5 = 0;
        r0 = r18;
        r0.m346init(r3, r4, r5);
        goto L_0x0573;
    L_0x0659:
        r37 = r53;
        r37 = (net.sf.fmj.media.rtp.RTCPAPPPacket) r37;
        r0 = r53;
        r3 = r0.base;
        r3 = r3 instanceof net.sf.fmj.media.rtp.util.UDPPacket;
        if (r3 == 0) goto L_0x070b;
    L_0x0665:
        r0 = r52;
        r4 = r0.cache;
        r0 = r37;
        r5 = r0.ssrc;
        r0 = r53;
        r3 = r0.base;
        r3 = (net.sf.fmj.media.rtp.util.UDPPacket) r3;
        r6 = r3.remoteAddress;
        r0 = r53;
        r3 = r0.base;
        r3 = (net.sf.fmj.media.rtp.util.UDPPacket) r3;
        r3 = r3.remotePort;
        r48 = r4.get(r5, r6, r3);
    L_0x0681:
        if (r48 == 0) goto L_0x005d;
    L_0x0683:
        r0 = r37;
        r4 = r0.receiptTime;
        r0 = r48;
        r0.lastHeardFrom = r4;
        r0 = r48;
        r3 = r0.quiet;
        if (r3 == 0) goto L_0x06be;
    L_0x0691:
        r3 = 0;
        r0 = r48;
        r0.quiet = r3;
        r19 = 0;
        r0 = r48;
        r3 = r0 instanceof javax.media.rtp.ReceiveStream;
        if (r3 == 0) goto L_0x071b;
    L_0x069e:
        r19 = new javax.media.rtp.event.ActiveReceiveStreamEvent;
        r0 = r52;
        r3 = r0.cache;
        r4 = r3.sm;
        r0 = r48;
        r5 = r0.sourceInfo;
        r3 = r48;
        r3 = (javax.media.rtp.ReceiveStream) r3;
        r0 = r19;
        r0.m346init(r4, r5, r3);
    L_0x06b3:
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.eventhandler;
        r0 = r19;
        r3.postEvent(r0);
    L_0x06be:
        r8 = 0;
        r0 = r48;
        r3 = r0 instanceof net.sf.fmj.media.rtp.RecvSSRCInfo;
        if (r3 == 0) goto L_0x06e1;
    L_0x06c5:
        r8 = new javax.media.rtp.event.ApplicationEvent;
        r0 = r52;
        r3 = r0.cache;
        r9 = r3.sm;
        r0 = r48;
        r10 = r0.sourceInfo;
        r11 = r48;
        r11 = (javax.media.rtp.ReceiveStream) r11;
        r0 = r37;
        r12 = r0.subtype;
        r13 = 0;
        r0 = r37;
        r14 = r0.data;
        r8.m349init(r9, r10, r11, r12, r13, r14);
    L_0x06e1:
        r0 = r48;
        r3 = r0 instanceof net.sf.fmj.media.rtp.PassiveSSRCInfo;
        if (r3 == 0) goto L_0x0700;
    L_0x06e7:
        r8 = new javax.media.rtp.event.ApplicationEvent;
        r0 = r52;
        r3 = r0.cache;
        r9 = r3.sm;
        r0 = r48;
        r10 = r0.sourceInfo;
        r11 = 0;
        r0 = r37;
        r12 = r0.subtype;
        r13 = 0;
        r0 = r37;
        r14 = r0.data;
        r8.m349init(r9, r10, r11, r12, r13, r14);
    L_0x0700:
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.eventhandler;
        r3.postEvent(r8);
        goto L_0x005d;
    L_0x070b:
        r0 = r52;
        r3 = r0.cache;
        r0 = r37;
        r4 = r0.ssrc;
        r5 = 0;
        r6 = 0;
        r48 = r3.get(r4, r5, r6);
        goto L_0x0681;
    L_0x071b:
        r19 = new javax.media.rtp.event.ActiveReceiveStreamEvent;
        r0 = r52;
        r3 = r0.cache;
        r3 = r3.sm;
        r0 = r48;
        r4 = r0.sourceInfo;
        r5 = 0;
        r0 = r19;
        r0.m346init(r3, r4, r5);
        goto L_0x06b3;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.media.rtp.RTCPReceiver.sendTo(net.sf.fmj.media.rtp.RTCPPacket):void");
    }
}
