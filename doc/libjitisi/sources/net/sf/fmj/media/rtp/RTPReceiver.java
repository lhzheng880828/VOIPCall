package net.sf.fmj.media.rtp;

import com.lti.utils.UnsignedUtils;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.media.protocol.PushBufferStream;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.event.ActiveReceiveStreamEvent;
import javax.media.rtp.event.NewReceiveStreamEvent;
import javax.media.rtp.event.RTPEvent;
import javax.media.rtp.event.RemotePayloadChangeEvent;
import net.sf.fmj.media.Log;
import net.sf.fmj.media.protocol.rtp.DataSource;
import net.sf.fmj.media.rtp.util.Packet;
import net.sf.fmj.media.rtp.util.PacketFilter;
import net.sf.fmj.media.rtp.util.PacketSource;
import net.sf.fmj.media.rtp.util.RTPPacket;
import net.sf.fmj.media.rtp.util.SSRCTable;
import net.sf.fmj.media.rtp.util.UDPPacket;
import org.jitsi.impl.neomedia.portaudio.Pa;

public class RTPReceiver extends PacketFilter {
    static final int MAX_DROPOUT = 3000;
    static final int MAX_MISORDER = 100;
    static final int MIN_SEQUENTIAL = 2;
    static final int SEQ_MOD = 65536;
    final SSRCCache cache;
    private final String content;
    public final String controlstr;
    private int errorPayload;
    private boolean initBC;
    int lastseqnum;
    private boolean mismatchprinted;
    final SSRCTable probationList;
    private boolean rtcpstarted;
    final RTPDemultiplexer rtpdemultiplexer;
    private boolean setpriority;

    public RTPReceiver(SSRCCache ssrccache, RTPDemultiplexer rtpdemultiplexer1) {
        this.mismatchprinted = false;
        this.initBC = false;
        this.lastseqnum = -1;
        this.rtcpstarted = false;
        this.setpriority = false;
        this.content = "";
        this.probationList = new SSRCTable();
        this.controlstr = "javax.media.rtp.RTPControl";
        this.errorPayload = -1;
        this.cache = ssrccache;
        this.rtpdemultiplexer = rtpdemultiplexer1;
        setConsumer(null);
    }

    public RTPReceiver(SSRCCache ssrccache, RTPDemultiplexer rtpdemultiplexer1, DatagramSocket datagramsocket) {
        this(ssrccache, rtpdemultiplexer1, new RTPRawReceiver(datagramsocket, ssrccache.sm.defaultstats));
    }

    public RTPReceiver(SSRCCache ssrccache, RTPDemultiplexer rtpdemultiplexer1, int i, String s) throws UnknownHostException, IOException {
        this(ssrccache, rtpdemultiplexer1, new RTPRawReceiver(i & -2, s, ssrccache.sm.defaultstats));
    }

    public RTPReceiver(SSRCCache ssrccache, RTPDemultiplexer rtpdemultiplexer1, PacketSource packetsource) {
        this(ssrccache, rtpdemultiplexer1);
        setSource(packetsource);
    }

    public String filtername() {
        return "RTP Packet Receiver";
    }

    public Packet handlePacket(Packet packet) {
        return handlePacket((RTPPacket) packet);
    }

    public Packet handlePacket(Packet packet, int i) {
        return null;
    }

    public Packet handlePacket(Packet packet, SessionAddress sessionaddress) {
        return null;
    }

    public Packet handlePacket(Packet packet, SessionAddress sessionaddress, boolean flag) {
        return null;
    }

    public Packet handlePacket(RTPPacket rtppacket) {
        if (rtppacket.payloadType == 13) {
            return rtppacket;
        }
        if (rtppacket.payloadType == 126) {
            return null;
        }
        SSRCInfo ssrcinfo = null;
        if (rtppacket.base instanceof UDPPacket) {
            InetAddress inetaddress = ((UDPPacket) rtppacket.base).remoteAddress;
            if (this.cache.sm.bindtome && !this.cache.sm.isBroadcast(this.cache.sm.dataaddress)) {
                if (!inetaddress.equals(this.cache.sm.dataaddress)) {
                    return null;
                }
            }
        }
        if (null == null) {
            if (rtppacket.base instanceof UDPPacket) {
                ssrcinfo = this.cache.get(rtppacket.ssrc, ((UDPPacket) rtppacket.base).remoteAddress, ((UDPPacket) rtppacket.base).remotePort, 1);
            } else {
                ssrcinfo = this.cache.get(rtppacket.ssrc, null, 0, 1);
            }
        }
        if (ssrcinfo == null) {
            return null;
        }
        for (int i = 0; i < rtppacket.csrc.length; i++) {
            SSRCInfo ssrcinfo1;
            if (rtppacket.base instanceof UDPPacket) {
                ssrcinfo1 = this.cache.get(rtppacket.csrc[i], ((UDPPacket) rtppacket.base).remoteAddress, ((UDPPacket) rtppacket.base).remotePort, 1);
            } else {
                ssrcinfo1 = this.cache.get(rtppacket.csrc[i], null, 0, 1);
            }
            if (ssrcinfo1 != null) {
                ssrcinfo1.lastHeardFrom = rtppacket.receiptTime;
            }
        }
        if (ssrcinfo.lastPayloadType != -1 && ssrcinfo.lastPayloadType == rtppacket.payloadType && this.mismatchprinted) {
            return null;
        }
        if (!ssrcinfo.sender) {
            ssrcinfo.initsource(rtppacket.seqnum);
            ssrcinfo.payloadType = rtppacket.payloadType;
        }
        int diff = rtppacket.seqnum - ssrcinfo.maxseq;
        if (ssrcinfo.maxseq + 1 != rtppacket.seqnum && diff > 0) {
            ssrcinfo.stats.update(0, diff - 1);
        }
        if (diff > -100 && diff < 0) {
            ssrcinfo.stats.update(0, -1);
        }
        if (ssrcinfo.wrapped) {
            ssrcinfo.wrapped = false;
        }
        boolean flag = false;
        if (ssrcinfo.probation > 0) {
            if (rtppacket.seqnum == ssrcinfo.maxseq + 1) {
                ssrcinfo.probation--;
                ssrcinfo.maxseq = rtppacket.seqnum;
                if (ssrcinfo.probation == 0) {
                    flag = true;
                }
            } else {
                ssrcinfo.probation = 1;
                ssrcinfo.maxseq = rtppacket.seqnum;
                ssrcinfo.stats.update(2);
            }
        } else if (diff < MAX_DROPOUT) {
            if (rtppacket.seqnum < ssrcinfo.baseseq && diff < -32767) {
                ssrcinfo.cycles += 65536;
                ssrcinfo.wrapped = true;
            }
            ssrcinfo.maxseq = rtppacket.seqnum;
        } else if (diff <= 65436) {
            ssrcinfo.stats.update(3);
            if (rtppacket.seqnum == ssrcinfo.lastbadseq) {
                ssrcinfo.initsource(rtppacket.seqnum);
            } else {
                ssrcinfo.lastbadseq = (rtppacket.seqnum + 1) & 65535;
            }
        } else {
            ssrcinfo.stats.update(4);
        }
        if (this.cache.sm.isUnicast()) {
            if (this.rtcpstarted) {
                if (!this.cache.sm.isSenderDefaultAddr(((UDPPacket) rtppacket.base).remoteAddress)) {
                    this.cache.sm.addUnicastAddr(((UDPPacket) rtppacket.base).remoteAddress);
                }
            } else {
                this.cache.sm.startRTCPReports(((UDPPacket) rtppacket.base).remoteAddress);
                this.rtcpstarted = true;
                if (((this.cache.sm.controladdress.getAddress()[3] & UnsignedUtils.MAX_UBYTE) & UnsignedUtils.MAX_UBYTE) == 255) {
                    this.cache.sm.addUnicastAddr(this.cache.sm.controladdress);
                } else {
                    InetAddress inetaddress1 = null;
                    boolean flag2 = true;
                    try {
                        inetaddress1 = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        flag2 = false;
                    }
                    if (flag2) {
                        this.cache.sm.addUnicastAddr(inetaddress1);
                    }
                }
            }
        }
        ssrcinfo.received++;
        ssrcinfo.stats.update(1);
        if (ssrcinfo.probation > 0) {
            this.probationList.put(ssrcinfo.ssrc, rtppacket.clone());
            return null;
        }
        ssrcinfo.maxseq = rtppacket.seqnum;
        if (!(ssrcinfo.lastPayloadType == -1 || ssrcinfo.lastPayloadType == rtppacket.payloadType)) {
            ssrcinfo.currentformat = null;
            if (ssrcinfo.dsource != null) {
                RTPControlImpl rtpcontrolimpl = (RTPControlImpl) ssrcinfo.dsource.getControl(this.controlstr);
                if (rtpcontrolimpl != null) {
                    rtpcontrolimpl.currentformat = null;
                    rtpcontrolimpl.payload = -1;
                }
            }
            if (ssrcinfo.dsource != null) {
                try {
                    Log.warning("Stopping stream because of payload type mismatch: expecting pt=" + ssrcinfo.lastPayloadType + ", got pt=" + rtppacket.payloadType);
                    ssrcinfo.dsource.stop();
                } catch (IOException ioexception) {
                    System.err.println("Stopping DataSource after PCE " + ioexception.getMessage());
                }
            }
            ssrcinfo.lastPayloadType = rtppacket.payloadType;
            this.cache.eventhandler.postEvent(new RemotePayloadChangeEvent(this.cache.sm, (ReceiveStream) ssrcinfo, ssrcinfo.lastPayloadType, rtppacket.payloadType));
        }
        if (ssrcinfo.currentformat == null) {
            ssrcinfo.currentformat = this.cache.sm.formatinfo.get(rtppacket.payloadType);
            if (ssrcinfo.currentformat == null) {
                if (this.errorPayload == rtppacket.payloadType) {
                    return rtppacket;
                }
                Log.error("No format has been registered for RTP Payload type " + rtppacket.payloadType);
                this.errorPayload = rtppacket.payloadType;
                return rtppacket;
            } else if (ssrcinfo.dstream != null) {
                ssrcinfo.dstream.setFormat(ssrcinfo.currentformat);
            }
        }
        if (ssrcinfo.currentformat == null) {
            System.err.println("No Format for PT= " + rtppacket.payloadType);
            return rtppacket;
        }
        if (ssrcinfo.dsource != null) {
            RTPControlImpl rtpcontrolimpl1 = (RTPControlImpl) ssrcinfo.dsource.getControl(this.controlstr);
            if (rtpcontrolimpl1 != null) {
                rtpcontrolimpl1.currentformat = this.cache.sm.formatinfo.get(rtppacket.payloadType);
            }
        }
        if (!this.initBC) {
            ((BufferControlImpl) this.cache.sm.buffercontrol).initBufferControl(ssrcinfo.currentformat);
            this.initBC = true;
        }
        if (!ssrcinfo.streamconnect) {
            DataSource datasource = (DataSource) this.cache.sm.dslist.get(ssrcinfo.ssrc);
            if (datasource == null) {
                DataSource datasource1 = this.cache.sm.getDataSource(null);
                if (datasource1 == null) {
                    datasource = this.cache.sm.createNewDS(null);
                    this.cache.sm.setDefaultDSassigned(ssrcinfo.ssrc);
                } else if (this.cache.sm.isDefaultDSassigned()) {
                    datasource = this.cache.sm.createNewDS(ssrcinfo.ssrc);
                } else {
                    datasource = datasource1;
                    this.cache.sm.setDefaultDSassigned(ssrcinfo.ssrc);
                }
            }
            PushBufferStream[] apushbufferstream = datasource.getStreams();
            ssrcinfo.dsource = datasource;
            ssrcinfo.dstream = (RTPSourceStream) apushbufferstream[0];
            ssrcinfo.dstream.setContentDescriptor(this.content);
            ssrcinfo.dstream.setFormat(ssrcinfo.currentformat);
            RTPControlImpl rtpcontrolimpl2 = (RTPControlImpl) ssrcinfo.dsource.getControl(this.controlstr);
            if (rtpcontrolimpl2 != null) {
                rtpcontrolimpl2.currentformat = this.cache.sm.formatinfo.get(rtppacket.payloadType);
                rtpcontrolimpl2.stream = ssrcinfo;
            }
            ssrcinfo.streamconnect = true;
        }
        if (ssrcinfo.dsource != null) {
            ssrcinfo.active = true;
        }
        if (!ssrcinfo.newrecvstream) {
            RTPEvent newReceiveStreamEvent = new NewReceiveStreamEvent(this.cache.sm, (ReceiveStream) ssrcinfo);
            ssrcinfo.newrecvstream = true;
            this.cache.eventhandler.postEvent(newReceiveStreamEvent);
        }
        if (ssrcinfo.lastRTPReceiptTime != 0 && ssrcinfo.lastPayloadType == rtppacket.payloadType) {
            double d = (double) (((((long) this.cache.clockrate[ssrcinfo.payloadType]) * (rtppacket.receiptTime - ssrcinfo.lastRTPReceiptTime)) / 1000) - (rtppacket.timestamp - ssrcinfo.lasttimestamp));
            if (d < Pa.LATENCY_UNSPECIFIED) {
                d = -d;
            }
            ssrcinfo.jitter += 0.0625d * (d - ssrcinfo.jitter);
        }
        ssrcinfo.lastRTPReceiptTime = rtppacket.receiptTime;
        ssrcinfo.lasttimestamp = rtppacket.timestamp;
        ssrcinfo.payloadType = rtppacket.payloadType;
        ssrcinfo.lastPayloadType = rtppacket.payloadType;
        ssrcinfo.bytesreceived += rtppacket.payloadlength;
        ssrcinfo.lastHeardFrom = rtppacket.receiptTime;
        if (ssrcinfo.quiet) {
            ActiveReceiveStreamEvent activereceivestreamevent;
            ssrcinfo.quiet = false;
            if (ssrcinfo instanceof ReceiveStream) {
                activereceivestreamevent = new ActiveReceiveStreamEvent(this.cache.sm, ssrcinfo.sourceInfo, (ReceiveStream) ssrcinfo);
            } else {
                activereceivestreamevent = new ActiveReceiveStreamEvent(this.cache.sm, ssrcinfo.sourceInfo, null);
            }
            this.cache.eventhandler.postEvent(activereceivestreamevent);
        }
        SourceRTPPacket sourceRTPPacket = new SourceRTPPacket(rtppacket, ssrcinfo);
        if (ssrcinfo.dsource == null) {
            return rtppacket;
        }
        if (this.mismatchprinted) {
            this.mismatchprinted = false;
        }
        if (flag) {
            RTPPacket rtppacket1 = (RTPPacket) this.probationList.remove(ssrcinfo.ssrc);
            if (rtppacket1 != null) {
                this.rtpdemultiplexer.demuxpayload(new SourceRTPPacket(rtppacket1, ssrcinfo));
            }
        }
        this.rtpdemultiplexer.demuxpayload(sourceRTPPacket);
        return rtppacket;
    }
}
