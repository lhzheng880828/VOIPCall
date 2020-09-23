package net.sf.fmj.media.rtp;

import com.lti.utils.UnsignedUtils;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Vector;
import javax.media.rtp.RTPConnector;
import javax.media.rtp.RTPPushDataSource;
import javax.media.rtp.SessionAddress;
import net.sf.fmj.media.rtp.util.BadFormatException;
import net.sf.fmj.media.rtp.util.Packet;
import net.sf.fmj.media.rtp.util.PacketFilter;
import net.sf.fmj.media.rtp.util.RTPPacketReceiver;
import net.sf.fmj.media.rtp.util.UDPPacketReceiver;

public class RTCPRawReceiver extends PacketFilter {
    public DatagramSocket socket;
    private OverallStats stats = null;
    private StreamSynch streamSynch;

    public RTCPRawReceiver(DatagramSocket sock, OverallStats stats, StreamSynch streamSynch) {
        setSource(new UDPPacketReceiver(sock, 1000));
        this.stats = stats;
        this.streamSynch = streamSynch;
    }

    public RTCPRawReceiver(int localPort, String localAddress, OverallStats stats, StreamSynch streamSynch) throws UnknownHostException, IOException, SocketException {
        this.streamSynch = streamSynch;
        this.stats = stats;
        UDPPacketReceiver recv = new UDPPacketReceiver(localPort, localAddress, -1, null, 1000, null);
        setSource(recv);
        this.socket = recv.getSocket();
    }

    public RTCPRawReceiver(RTPConnector rtpConnector, OverallStats stats, StreamSynch streamSynch) {
        this.streamSynch = streamSynch;
        try {
            setSource(new RTPPacketReceiver(rtpConnector.getControlInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.stats = stats;
    }

    public RTCPRawReceiver(RTPPushDataSource networkdatasource, OverallStats stats, StreamSynch streamSynch) {
        this.streamSynch = streamSynch;
        setSource(new RTPPacketReceiver(networkdatasource));
        this.stats = stats;
    }

    public RTCPRawReceiver(SessionAddress localAddress, SessionAddress remoteAddress, OverallStats stats, StreamSynch streamSynch, DatagramSocket controlSocket) throws UnknownHostException, IOException, SocketException {
        this.streamSynch = streamSynch;
        this.stats = stats;
        UDPPacketReceiver recv = new UDPPacketReceiver(localAddress.getControlPort(), localAddress.getControlHostAddress(), remoteAddress.getControlPort(), remoteAddress.getControlHostAddress(), 1000, controlSocket);
        setSource(recv);
        this.socket = recv.getSocket();
    }

    public void close() {
        if (this.socket != null) {
            this.socket.close();
        }
        if (getSource() instanceof RTPPacketReceiver) {
            getSource().closeSource();
        }
    }

    public String filtername() {
        return "RTCP Raw Receiver";
    }

    public Packet handlePacket(Packet p) {
        this.stats.update(0, 1);
        this.stats.update(11, 1);
        this.stats.update(1, p.length);
        try {
            return parse(p);
        } catch (BadFormatException e) {
            this.stats.update(13, 1);
            return null;
        }
    }

    public Packet handlePacket(Packet p, int i) {
        return null;
    }

    public Packet handlePacket(Packet p, SessionAddress a) {
        return null;
    }

    public Packet handlePacket(Packet p, SessionAddress a, boolean control) {
        return null;
    }

    public RTCPPacket parse(Packet packet) throws BadFormatException {
        RTCPPacket base = new RTCPCompoundPacket(packet);
        Vector<RTCPPacket> vector = new Vector(2);
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(base.data, base.offset, base.length));
        int offset = 0;
        while (offset < base.length) {
            try {
                int firstbyte = dataInputStream.readUnsignedByte();
                if ((firstbyte & 192) != 128) {
                    throw new BadFormatException();
                }
                int type = dataInputStream.readUnsignedByte();
                int length = (dataInputStream.readUnsignedShort() + 1) << 2;
                int padlen = 0;
                if (offset + length > base.length) {
                    throw new BadFormatException();
                }
                RTCPPacket p;
                if (offset + length == base.length) {
                    if ((firstbyte & 32) != 0) {
                        padlen = base.data[(base.offset + base.length) - 1] & UnsignedUtils.MAX_UBYTE;
                        if (padlen == 0) {
                            throw new BadFormatException();
                        }
                    }
                } else if ((firstbyte & 32) != 0) {
                    throw new BadFormatException();
                }
                int inlength = length - padlen;
                firstbyte &= 31;
                RTCPPacket rTCPSRPacket;
                int i;
                RTCPReportBlock report;
                long val;
                switch (type) {
                    case 200:
                        this.stats.update(12, 1);
                        if (inlength == (firstbyte * 24) + 28) {
                            rTCPSRPacket = new RTCPSRPacket(base);
                            p = rTCPSRPacket;
                            rTCPSRPacket.ssrc = dataInputStream.readInt();
                            rTCPSRPacket.ntptimestampmsw = ((long) dataInputStream.readInt()) & 4294967295L;
                            rTCPSRPacket.ntptimestamplsw = ((long) dataInputStream.readInt()) & 4294967295L;
                            rTCPSRPacket.rtptimestamp = ((long) dataInputStream.readInt()) & 4294967295L;
                            rTCPSRPacket.packetcount = ((long) dataInputStream.readInt()) & 4294967295L;
                            rTCPSRPacket.octetcount = ((long) dataInputStream.readInt()) & 4294967295L;
                            rTCPSRPacket.reports = new RTCPReportBlock[firstbyte];
                            this.streamSynch.update(rTCPSRPacket.ssrc, rTCPSRPacket.rtptimestamp, rTCPSRPacket.ntptimestampmsw, rTCPSRPacket.ntptimestamplsw);
                            for (i = 0; i < rTCPSRPacket.reports.length; i++) {
                                report = new RTCPReportBlock();
                                rTCPSRPacket.reports[i] = report;
                                report.ssrc = dataInputStream.readInt();
                                val = ((long) dataInputStream.readInt()) & 4294967295L;
                                report.fractionlost = (int) (val >> 24);
                                report.packetslost = (int) (16777215 & val);
                                report.lastseq = ((long) dataInputStream.readInt()) & 4294967295L;
                                report.jitter = dataInputStream.readInt();
                                report.lsr = ((long) dataInputStream.readInt()) & 4294967295L;
                                report.dlsr = ((long) dataInputStream.readInt()) & 4294967295L;
                            }
                            break;
                        }
                        this.stats.update(18, 1);
                        System.out.println("bad format.");
                        throw new BadFormatException();
                    case RTCPPacket.RR /*201*/:
                        if (inlength == (firstbyte * 24) + 8) {
                            rTCPSRPacket = new RTCPRRPacket(base);
                            p = rTCPSRPacket;
                            rTCPSRPacket.ssrc = dataInputStream.readInt();
                            rTCPSRPacket.reports = new RTCPReportBlock[firstbyte];
                            for (i = 0; i < rTCPSRPacket.reports.length; i++) {
                                report = new RTCPReportBlock();
                                rTCPSRPacket.reports[i] = report;
                                report.ssrc = dataInputStream.readInt();
                                val = ((long) dataInputStream.readInt()) & 4294967295L;
                                report.fractionlost = (int) (val >> 24);
                                report.packetslost = (int) (16777215 & val);
                                report.lastseq = ((long) dataInputStream.readInt()) & 4294967295L;
                                report.jitter = dataInputStream.readInt();
                                report.lsr = ((long) dataInputStream.readInt()) & 4294967295L;
                                report.dlsr = ((long) dataInputStream.readInt()) & 4294967295L;
                            }
                            break;
                        }
                        this.stats.update(15, 1);
                        throw new BadFormatException();
                    case RTCPPacket.SDES /*202*/:
                        rTCPSRPacket = new RTCPSDESPacket(base);
                        p = rTCPSRPacket;
                        rTCPSRPacket.sdes = new RTCPSDES[firstbyte];
                        int sdesoff = 4;
                        i = 0;
                        while (i < rTCPSRPacket.sdes.length) {
                            RTCPSDES chunk = new RTCPSDES();
                            rTCPSRPacket.sdes[i] = chunk;
                            chunk.ssrc = dataInputStream.readInt();
                            sdesoff += 5;
                            Vector<RTCPSDESItem> items = new Vector();
                            boolean gotcname = false;
                            while (true) {
                                int j = dataInputStream.readUnsignedByte();
                                if (j != 0) {
                                    if (j < 1 || j > 8) {
                                        this.stats.update(16, 1);
                                    } else {
                                        if (j == 1) {
                                            gotcname = true;
                                        }
                                        RTCPSDESItem item = new RTCPSDESItem();
                                        items.addElement(item);
                                        item.type = j;
                                        int sdeslen = dataInputStream.readUnsignedByte();
                                        item.data = new byte[sdeslen];
                                        dataInputStream.readFully(item.data);
                                        sdesoff += sdeslen + 2;
                                    }
                                } else if (gotcname) {
                                    chunk.items = new RTCPSDESItem[items.size()];
                                    items.copyInto(chunk.items);
                                    if ((sdesoff & 3) != 0) {
                                        dataInputStream.skip((long) (4 - (sdesoff & 3)));
                                        sdesoff = (sdesoff + 3) & -4;
                                    }
                                    i++;
                                } else {
                                    this.stats.update(16, 1);
                                    throw new BadFormatException();
                                }
                            }
                            this.stats.update(16, 1);
                            throw new BadFormatException();
                        }
                        if (inlength == sdesoff) {
                            break;
                        }
                        this.stats.update(16, 1);
                        throw new BadFormatException();
                        break;
                    case RTCPPacket.BYE /*203*/:
                        int reasonlen;
                        RTCPPacket byep = new RTCPBYEPacket(base);
                        p = byep;
                        byep.ssrc = new int[firstbyte];
                        for (i = 0; i < byep.ssrc.length; i++) {
                            byep.ssrc[i] = dataInputStream.readInt();
                        }
                        if (inlength > (firstbyte * 4) + 4) {
                            reasonlen = dataInputStream.readUnsignedByte();
                            byep.reason = new byte[reasonlen];
                            reasonlen++;
                        } else {
                            reasonlen = 0;
                            byep.reason = new byte[0];
                        }
                        reasonlen = (reasonlen + 3) & -4;
                        if (inlength == ((firstbyte * 4) + 4) + reasonlen) {
                            dataInputStream.readFully(byep.reason);
                            dataInputStream.skip((long) (reasonlen - byep.reason.length));
                            break;
                        }
                        this.stats.update(17, 1);
                        throw new BadFormatException();
                    case RTCPPacket.APP /*204*/:
                        if (inlength >= 12) {
                            RTCPPacket appp = new RTCPAPPPacket(base);
                            p = appp;
                            appp.ssrc = dataInputStream.readInt();
                            appp.name = dataInputStream.readInt();
                            appp.subtype = firstbyte;
                            appp.data = new byte[(inlength - 12)];
                            dataInputStream.readFully(appp.data);
                            dataInputStream.skip((long) ((inlength - 12) - appp.data.length));
                            break;
                        }
                        throw new BadFormatException();
                    default:
                        this.stats.update(14, 1);
                        throw new BadFormatException();
                }
                p.offset = offset;
                p.length = length;
                vector.addElement(p);
                dataInputStream.skipBytes(padlen);
                offset += length;
            } catch (EOFException e) {
                throw new BadFormatException("Unexpected end of RTCP packet");
            } catch (IOException e2) {
                throw new IllegalArgumentException("Impossible Exception");
            }
        }
        base.packets = new RTCPPacket[vector.size()];
        vector.copyInto(base.packets);
        return base;
    }
}
