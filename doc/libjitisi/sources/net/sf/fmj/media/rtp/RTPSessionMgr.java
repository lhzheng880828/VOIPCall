package net.sf.fmj.media.rtp;

import com.lti.utils.UnsignedUtils;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.CachingControl;
import javax.media.Format;
import javax.media.control.BufferControl;
import javax.media.format.AudioFormat;
import javax.media.format.UnsupportedFormatException;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.rtp.EncryptionInfo;
import javax.media.rtp.GlobalReceptionStats;
import javax.media.rtp.GlobalTransmissionStats;
import javax.media.rtp.InvalidSessionAddressException;
import javax.media.rtp.LocalParticipant;
import javax.media.rtp.Participant;
import javax.media.rtp.RTPConnector;
import javax.media.rtp.RTPControl;
import javax.media.rtp.RTPManager;
import javax.media.rtp.RTPPushDataSource;
import javax.media.rtp.RTPSocket;
import javax.media.rtp.RTPStream;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.RemoteListener;
import javax.media.rtp.RemoteParticipant;
import javax.media.rtp.SSRCInUseException;
import javax.media.rtp.SendStream;
import javax.media.rtp.SendStreamListener;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.SessionListener;
import javax.media.rtp.SessionManager;
import javax.media.rtp.SessionManagerException;
import javax.media.rtp.event.NewSendStreamEvent;
import javax.media.rtp.event.StreamClosedEvent;
import javax.media.rtp.rtcp.SourceDescription;
import net.sf.fmj.media.protocol.rtp.DataSource;
import net.sf.fmj.media.rtp.util.PacketForwarder;
import net.sf.fmj.media.rtp.util.RTPPacketSender;
import net.sf.fmj.media.rtp.util.SSRCTable;
import net.sf.fmj.media.rtp.util.UDPPacketSender;
import org.jitsi.impl.neomedia.portaudio.Pa;

public class RTPSessionMgr extends RTPManager implements SessionManager {
    private static final int GET_ALL_PARTICIPANTS = -1;
    private static final String SOURCE_DESC_EMAIL = "fmj-devel@lists.sourceforge.net";
    private static final String SOURCE_DESC_TOOL = "FMJ RTP Player";
    static Vector addedList = new Vector();
    private static final Logger logger = Logger.getLogger(RTPSessionMgr.class.getName());
    static FormatInfo supportedList = null;
    private final int MAX_PORT;
    boolean bds;
    boolean bindtome;
    BufferControl buffercontrol;
    private SSRCCache cache;
    SSRCCacheCleaner cleaner;
    private DatagramSocket controlSocket;
    InetAddress controladdress;
    int controlport;
    private DatagramSocket dataSocket;
    InetAddress dataaddress;
    int dataport;
    long defaultSSRC;
    Format defaultformat;
    DataSource defaultsource;
    int defaultsourceid;
    OverallStats defaultstats;
    PushBufferStream defaultstream;
    SSRCTable dslist;
    boolean encryption;
    FormatInfo formatinfo;
    private boolean initialized;
    private SessionAddress localAddress;
    InetAddress localControlAddress;
    int localControlPort;
    InetAddress localDataAddress;
    int localDataPort;
    private SessionAddress localReceiverAddress;
    SessionAddress localSenderAddress;
    boolean multi_unicast;
    private boolean newRtpInterface;
    private boolean nonparticipating;
    private boolean nosockets;
    private OverallStats overallStats;
    private boolean participating;
    Vector peerlist;
    Hashtable peerrtcplist;
    Hashtable peerrtplist;
    private SessionAddress remoteAddress;
    private Vector remoteAddresses;
    protected Vector remotelistener;
    private PacketForwarder rtcpForwarder;
    private RTCPRawReceiver rtcpRawReceiver;
    private RTCPTransmitter rtcpTransmitter;
    RTPPushDataSource rtcpsource;
    private RTPConnector rtpConnector;
    private RTPDemultiplexer rtpDemultiplexer;
    private PacketForwarder rtpForwarder;
    private RTPRawReceiver rtpRawReceiver;
    RTPTransmitter rtpTransmitter;
    RTPPacketSender rtpsender;
    RTPPushDataSource rtpsource;
    RTCPRawSender sender;
    int sendercount;
    Vector sendstreamlist;
    protected Vector sendstreamlistener;
    protected Vector sessionlistener;
    private boolean started;
    private boolean startedparticipating;
    StreamSynch streamSynch;
    protected Vector streamlistener;
    OverallTransStats transstats;
    int ttl;
    private UDPPacketSender udpPacketSender;
    UDPPacketSender udpsender;
    private boolean unicast;

    public static boolean formatSupported(Format format) {
        if (supportedList == null) {
            supportedList = new FormatInfo();
        }
        if (supportedList.getPayload(format) != -1) {
            return true;
        }
        for (int i = 0; i < addedList.size(); i++) {
            if (((Format) addedList.elementAt(i)).matches(format)) {
                return true;
            }
        }
        return false;
    }

    public RTPSessionMgr() {
        this.bindtome = false;
        this.cache = null;
        this.ttl = 0;
        this.sendercount = 0;
        this.localDataAddress = null;
        this.localDataPort = 0;
        this.localControlAddress = null;
        this.localControlPort = 0;
        this.dataaddress = null;
        this.controladdress = null;
        this.dataport = 0;
        this.controlport = 0;
        this.rtpsource = null;
        this.rtcpsource = null;
        this.defaultSSRC = 0;
        this.localSenderAddress = null;
        this.localReceiverAddress = null;
        this.udpsender = null;
        this.rtpsender = null;
        this.sender = null;
        this.cleaner = null;
        this.unicast = false;
        this.startedparticipating = false;
        this.nonparticipating = false;
        this.nosockets = false;
        this.started = false;
        this.initialized = false;
        this.sessionlistener = null;
        this.remotelistener = null;
        this.streamlistener = null;
        this.sendstreamlistener = null;
        this.encryption = false;
        this.dslist = null;
        this.streamSynch = null;
        this.formatinfo = null;
        this.defaultsource = null;
        this.defaultstream = null;
        this.defaultformat = null;
        this.buffercontrol = null;
        this.defaultstats = null;
        this.transstats = null;
        this.defaultsourceid = 0;
        this.sendstreamlist = null;
        this.rtpTransmitter = null;
        this.bds = false;
        this.peerlist = null;
        this.multi_unicast = false;
        this.peerrtplist = null;
        this.peerrtcplist = null;
        this.newRtpInterface = false;
        this.remoteAddress = null;
        this.localAddress = null;
        this.rtcpRawReceiver = null;
        this.rtpRawReceiver = null;
        this.rtpForwarder = null;
        this.rtcpForwarder = null;
        this.rtpDemultiplexer = null;
        this.overallStats = null;
        this.participating = false;
        this.udpPacketSender = null;
        this.remoteAddresses = null;
        this.rtcpTransmitter = null;
        this.rtpConnector = null;
        this.dataSocket = null;
        this.controlSocket = null;
        this.MAX_PORT = 65535;
        this.bindtome = false;
        this.localDataAddress = null;
        this.localDataPort = 0;
        this.localControlAddress = null;
        this.localControlPort = 0;
        this.dataaddress = null;
        this.controladdress = null;
        this.dataport = 0;
        this.controlport = 0;
        this.rtpsource = null;
        this.rtcpsource = null;
        this.defaultSSRC = 0;
        this.udpsender = null;
        this.rtpsender = null;
        this.sender = null;
        this.cleaner = null;
        this.unicast = false;
        this.startedparticipating = false;
        this.nonparticipating = false;
        this.nosockets = false;
        this.started = false;
        this.initialized = false;
        this.sessionlistener = new Vector();
        this.remotelistener = new Vector();
        this.streamlistener = new Vector();
        this.sendstreamlistener = new Vector();
        this.encryption = false;
        this.dslist = new SSRCTable();
        this.formatinfo = null;
        this.defaultsource = null;
        this.defaultstream = null;
        this.defaultformat = null;
        this.buffercontrol = null;
        this.defaultstats = null;
        this.transstats = null;
        this.defaultsourceid = 0;
        this.sendstreamlist = new Vector(1);
        this.rtpTransmitter = null;
        this.bds = false;
        this.peerlist = new Vector();
        this.multi_unicast = false;
        this.peerrtplist = new Hashtable(5);
        this.peerrtcplist = new Hashtable(5);
        this.newRtpInterface = false;
        this.formatinfo = new FormatInfo();
        this.buffercontrol = new BufferControlImpl();
        this.defaultstats = new OverallStats();
        this.transstats = new OverallTransStats();
        this.streamSynch = new StreamSynch();
    }

    public RTPSessionMgr(DataSource datasource) throws IOException {
        this.bindtome = false;
        this.cache = null;
        this.ttl = 0;
        this.sendercount = 0;
        this.localDataAddress = null;
        this.localDataPort = 0;
        this.localControlAddress = null;
        this.localControlPort = 0;
        this.dataaddress = null;
        this.controladdress = null;
        this.dataport = 0;
        this.controlport = 0;
        this.rtpsource = null;
        this.rtcpsource = null;
        this.defaultSSRC = 0;
        this.localSenderAddress = null;
        this.localReceiverAddress = null;
        this.udpsender = null;
        this.rtpsender = null;
        this.sender = null;
        this.cleaner = null;
        this.unicast = false;
        this.startedparticipating = false;
        this.nonparticipating = false;
        this.nosockets = false;
        this.started = false;
        this.initialized = false;
        this.sessionlistener = null;
        this.remotelistener = null;
        this.streamlistener = null;
        this.sendstreamlistener = null;
        this.encryption = false;
        this.dslist = null;
        this.streamSynch = null;
        this.formatinfo = null;
        this.defaultsource = null;
        this.defaultstream = null;
        this.defaultformat = null;
        this.buffercontrol = null;
        this.defaultstats = null;
        this.transstats = null;
        this.defaultsourceid = 0;
        this.sendstreamlist = null;
        this.rtpTransmitter = null;
        this.bds = false;
        this.peerlist = null;
        this.multi_unicast = false;
        this.peerrtplist = null;
        this.peerrtcplist = null;
        this.newRtpInterface = false;
        this.remoteAddress = null;
        this.localAddress = null;
        this.rtcpRawReceiver = null;
        this.rtpRawReceiver = null;
        this.rtpForwarder = null;
        this.rtcpForwarder = null;
        this.rtpDemultiplexer = null;
        this.overallStats = null;
        this.participating = false;
        this.udpPacketSender = null;
        this.remoteAddresses = null;
        this.rtcpTransmitter = null;
        this.rtpConnector = null;
        this.dataSocket = null;
        this.controlSocket = null;
        this.MAX_PORT = 65535;
        this.bindtome = false;
        this.localDataAddress = null;
        this.localDataPort = 0;
        this.localControlAddress = null;
        this.localControlPort = 0;
        this.dataaddress = null;
        this.controladdress = null;
        this.dataport = 0;
        this.controlport = 0;
        this.rtpsource = null;
        this.rtcpsource = null;
        this.defaultSSRC = 0;
        this.udpsender = null;
        this.rtpsender = null;
        this.sender = null;
        this.cleaner = null;
        this.unicast = false;
        this.startedparticipating = false;
        this.nonparticipating = false;
        this.nosockets = false;
        this.started = false;
        this.initialized = false;
        this.sessionlistener = new Vector();
        this.remotelistener = new Vector();
        this.streamlistener = new Vector();
        this.sendstreamlistener = new Vector();
        this.encryption = false;
        this.dslist = new SSRCTable();
        this.formatinfo = null;
        this.defaultsource = null;
        this.defaultstream = null;
        this.defaultformat = null;
        this.buffercontrol = null;
        this.defaultstats = null;
        this.transstats = null;
        this.defaultsourceid = 0;
        this.sendstreamlist = new Vector(1);
        this.rtpTransmitter = null;
        this.bds = false;
        this.peerlist = new Vector();
        this.multi_unicast = false;
        this.peerrtplist = new Hashtable(5);
        this.peerrtcplist = new Hashtable(5);
        this.newRtpInterface = false;
        this.formatinfo = new FormatInfo();
        this.buffercontrol = new BufferControlImpl();
        this.defaultstats = new OverallStats();
        this.transstats = new OverallTransStats();
        UpdateEncodings(datasource);
        try {
            RTPMediaLocator rtpmedialocator = new RTPMediaLocator(datasource.getLocator().toString());
            createNewDS(rtpmedialocator).setControl((RTPControl) datasource.getControl("javax.media.rtp.RTPControl"));
            String s = rtpmedialocator.getSessionAddress();
            this.dataport = rtpmedialocator.getSessionPort();
            this.controlport = this.dataport + 1;
            this.ttl = rtpmedialocator.getTTL();
            try {
                this.dataaddress = InetAddress.getByName(s);
            } catch (Throwable throwable1) {
                logger.log(Level.WARNING, "error retrieving address " + s + " by name" + throwable1.getMessage(), throwable1);
            }
            this.controladdress = this.dataaddress;
            try {
                initSession(new SessionAddress(), setSDES(), 0.05d, 0.25d);
            } catch (SessionManagerException sessionmanagerexception) {
                throw new IOException("SessionManager exception " + sessionmanagerexception.getMessage());
            }
        } catch (MalformedURLException malformedurlexception) {
            throw new IOException("RTP URL is Malformed " + malformedurlexception.getMessage());
        }
    }

    public RTPSessionMgr(RTPPushDataSource rtppushdatasource) {
        this.bindtome = false;
        this.cache = null;
        this.ttl = 0;
        this.sendercount = 0;
        this.localDataAddress = null;
        this.localDataPort = 0;
        this.localControlAddress = null;
        this.localControlPort = 0;
        this.dataaddress = null;
        this.controladdress = null;
        this.dataport = 0;
        this.controlport = 0;
        this.rtpsource = null;
        this.rtcpsource = null;
        this.defaultSSRC = 0;
        this.localSenderAddress = null;
        this.localReceiverAddress = null;
        this.udpsender = null;
        this.rtpsender = null;
        this.sender = null;
        this.cleaner = null;
        this.unicast = false;
        this.startedparticipating = false;
        this.nonparticipating = false;
        this.nosockets = false;
        this.started = false;
        this.initialized = false;
        this.sessionlistener = null;
        this.remotelistener = null;
        this.streamlistener = null;
        this.sendstreamlistener = null;
        this.encryption = false;
        this.dslist = null;
        this.streamSynch = null;
        this.formatinfo = null;
        this.defaultsource = null;
        this.defaultstream = null;
        this.defaultformat = null;
        this.buffercontrol = null;
        this.defaultstats = null;
        this.transstats = null;
        this.defaultsourceid = 0;
        this.sendstreamlist = null;
        this.rtpTransmitter = null;
        this.bds = false;
        this.peerlist = null;
        this.multi_unicast = false;
        this.peerrtplist = null;
        this.peerrtcplist = null;
        this.newRtpInterface = false;
        this.remoteAddress = null;
        this.localAddress = null;
        this.rtcpRawReceiver = null;
        this.rtpRawReceiver = null;
        this.rtpForwarder = null;
        this.rtcpForwarder = null;
        this.rtpDemultiplexer = null;
        this.overallStats = null;
        this.participating = false;
        this.udpPacketSender = null;
        this.remoteAddresses = null;
        this.rtcpTransmitter = null;
        this.rtpConnector = null;
        this.dataSocket = null;
        this.controlSocket = null;
        this.MAX_PORT = 65535;
        this.bindtome = false;
        this.localDataAddress = null;
        this.localDataPort = 0;
        this.localControlAddress = null;
        this.localControlPort = 0;
        this.dataaddress = null;
        this.controladdress = null;
        this.dataport = 0;
        this.controlport = 0;
        this.rtpsource = null;
        this.rtcpsource = null;
        this.defaultSSRC = 0;
        this.udpsender = null;
        this.rtpsender = null;
        this.sender = null;
        this.cleaner = null;
        this.unicast = false;
        this.startedparticipating = false;
        this.nonparticipating = false;
        this.nosockets = false;
        this.started = false;
        this.initialized = false;
        this.sessionlistener = new Vector();
        this.remotelistener = new Vector();
        this.streamlistener = new Vector();
        this.sendstreamlistener = new Vector();
        this.encryption = false;
        this.dslist = new SSRCTable();
        this.formatinfo = null;
        this.defaultsource = null;
        this.defaultstream = null;
        this.defaultformat = null;
        this.buffercontrol = null;
        this.defaultstats = null;
        this.transstats = null;
        this.defaultsourceid = 0;
        this.sendstreamlist = new Vector(1);
        this.rtpTransmitter = null;
        this.bds = false;
        this.peerlist = new Vector();
        this.multi_unicast = false;
        this.peerrtplist = new Hashtable(5);
        this.peerrtcplist = new Hashtable(5);
        this.newRtpInterface = false;
        this.nosockets = true;
        this.rtpsource = rtppushdatasource;
        if (this.rtpsource instanceof RTPSocket) {
            this.rtcpsource = ((RTPSocket) this.rtpsource).getControlChannel();
        }
        this.formatinfo = new FormatInfo();
        this.buffercontrol = new BufferControlImpl();
        this.defaultstats = new OverallStats();
        this.transstats = new OverallTransStats();
        DataSource datasource = createNewDS((RTPMediaLocator) null);
        UpdateEncodings(rtppushdatasource);
        datasource.setControl((RTPControl) rtppushdatasource.getControl(RTPControl.class.getName()));
        initSession(setSDES(), 0.05d, 0.25d);
        startSession(this.rtpsource, this.rtcpsource, null);
    }

    public void addFormat(Format format, int i) {
        if (this.formatinfo != null) {
            this.formatinfo.add(i, format);
        }
        if (format != null) {
            addedList.addElement(format);
        }
    }

    public void addMRL(RTPMediaLocator rtpmedialocator) {
        int i = (int) rtpmedialocator.getSSRC();
        if (i != 0 && ((DataSource) this.dslist.get(i)) == null) {
            DataSource datasource1 = createNewDS(rtpmedialocator);
        }
    }

    public void addPeer(SessionAddress sessionaddress) throws IOException, InvalidSessionAddressException {
        SocketException socketexception;
        Throwable th;
        SocketException socketexception1;
        int i = 0;
        while (i < this.peerlist.size()) {
            if (!((SessionAddress) this.peerlist.elementAt(i)).equals(sessionaddress)) {
                i++;
            } else {
                return;
            }
        }
        this.peerlist.addElement(sessionaddress);
        CheckRTPPorts(sessionaddress.getDataPort(), sessionaddress.getControlPort());
        RTCPRawReceiver rtcprawreceiver = null;
        InetAddress inetaddress = sessionaddress.getDataAddress();
        InetAddress inetaddress1 = sessionaddress.getControlAddress();
        int j = sessionaddress.getDataPort();
        int k = sessionaddress.getControlPort();
        CheckRTPAddress(inetaddress, inetaddress1);
        InetAddress inetaddress2 = null;
        try {
            inetaddress2 = InetAddress.getLocalHost();
        } catch (Throwable throwable1) {
            logger.log(Level.WARNING, "InitSession : UnknownHostExcpetion " + throwable1.getMessage(), throwable1);
        }
        if (!(inetaddress.isMulticastAddress() || inetaddress.equals(inetaddress2))) {
            if (!isBroadcast(inetaddress) || Win32()) {
                this.bindtome = true;
            } else {
                this.bindtome = false;
            }
        }
        if (!this.bindtome) {
            try {
                RTPRawReceiver rTPRawReceiver;
                RTPRawReceiver rtprawreceiver;
                RTCPRawReceiver rtcprawreceiver2 = new RTCPRawReceiver(k, inetaddress1.getHostAddress(), this.defaultstats, this.streamSynch);
                if (inetaddress != null) {
                    try {
                        rTPRawReceiver = new RTPRawReceiver(j, inetaddress.getHostAddress(), this.defaultstats);
                    } catch (SocketException e) {
                        socketexception = e;
                        rtcprawreceiver = rtcprawreceiver2;
                        try {
                            throw new IOException(socketexception.getMessage());
                        } catch (Throwable th2) {
                            th = th2;
                            logger.warning("could not create RTCP/RTP raw receivers");
                            rtcprawreceiver.closeSource();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        rtcprawreceiver = rtcprawreceiver2;
                        if (!(inetaddress == null || null != null || rtcprawreceiver == null)) {
                            logger.warning("could not create RTCP/RTP raw receivers");
                            rtcprawreceiver.closeSource();
                        }
                        throw th;
                    }
                }
                rtprawreceiver = null;
                if (!(inetaddress == null || rtprawreceiver != null || rtcprawreceiver2 == null)) {
                    logger.warning("could not create RTCP/RTP raw receivers");
                    rtcprawreceiver2.closeSource();
                }
                try {
                    RTPRawReceiver rtprawreceiver2;
                    rtcprawreceiver = new RTCPRawReceiver(k, inetaddress2.getHostAddress(), this.defaultstats, this.streamSynch);
                    if (inetaddress != null) {
                        try {
                            rTPRawReceiver = new RTPRawReceiver(j, inetaddress2.getHostAddress(), this.defaultstats);
                        } catch (SocketException e2) {
                            socketexception1 = e2;
                            try {
                                throw new IOException(socketexception1.getMessage());
                            } catch (Throwable th4) {
                                th = th4;
                                if (!(inetaddress == null || rtprawreceiver != null || rtcprawreceiver == null)) {
                                    logger.warning("could not create RTCP/RTP raw receivers");
                                    rtcprawreceiver.closeSource();
                                }
                                throw th;
                            }
                        }
                    }
                    rtprawreceiver2 = rtprawreceiver;
                    if (!(inetaddress == null || rtprawreceiver2 != null || rtcprawreceiver == null)) {
                        logger.warning("could not create RTCP/RTP raw receivers");
                        rtcprawreceiver.closeSource();
                    }
                    PacketForwarder packetforwarder = new PacketForwarder(rtcprawreceiver, new RTCPReceiver(this.cache));
                    PacketForwarder packetforwarder1 = null;
                    if (rtprawreceiver2 != null) {
                        packetforwarder1 = new PacketForwarder(rtprawreceiver2, new RTPReceiver(this.cache, this.rtpDemultiplexer));
                    }
                    packetforwarder.startPF("RTCP Forwarder for address" + inetaddress1.toString() + "port " + k);
                    if (packetforwarder1 != null) {
                        packetforwarder1.startPF("RTP Forwarder for address " + inetaddress.toString() + "port " + j);
                    }
                    this.peerrtplist.put(sessionaddress, packetforwarder1);
                    this.peerrtcplist.put(sessionaddress, packetforwarder);
                    if (this.cache.ourssrc != null) {
                        if (this.cache.ourssrc.reporter == null) {
                            this.controladdress = inetaddress1;
                            this.controlport = k;
                            this.cache.ourssrc.reporter = startParticipating(k, inetaddress.getHostAddress(), this.cache.ourssrc);
                        }
                        if (this.cache.ourssrc.reporter.transmit.sender.peerlist == null) {
                            this.cache.ourssrc.reporter.transmit.sender.peerlist = new Vector();
                        }
                    }
                    this.cache.ourssrc.reporter.transmit.sender.peerlist.addElement(sessionaddress);
                    if (this.cache != null) {
                        Enumeration elements = this.cache.cache.elements();
                        while (elements.hasMoreElements()) {
                            SSRCInfo ssrcinfo = (SSRCInfo) elements.nextElement();
                            if (ssrcinfo instanceof SendSSRCInfo) {
                                ssrcinfo.reporter.transmit.sender.control = true;
                                if (ssrcinfo.reporter.transmit.sender.peerlist == null) {
                                    ssrcinfo.reporter.transmit.sender.peerlist = new Vector();
                                    ssrcinfo.reporter.transmit.sender.peerlist.addElement(sessionaddress);
                                }
                            }
                        }
                    }
                    for (int l = 0; l < this.sendstreamlist.size(); l++) {
                        SSRCInfo sendssrcinfo = (SendSSRCInfo) this.sendstreamlist.elementAt(l);
                        if (sendssrcinfo.sinkstream.transmitter.sender.peerlist == null) {
                            sendssrcinfo.sinkstream.transmitter.sender.peerlist = new Vector();
                            sendssrcinfo.sinkstream.transmitter.sender.peerlist.addElement(sessionaddress);
                        }
                    }
                } catch (SocketException e3) {
                    socketexception1 = e3;
                    rtcprawreceiver = rtcprawreceiver2;
                    throw new IOException(socketexception1.getMessage());
                } catch (Throwable th5) {
                    th = th5;
                    rtcprawreceiver = rtcprawreceiver2;
                    logger.warning("could not create RTCP/RTP raw receivers");
                    rtcprawreceiver.closeSource();
                    throw th;
                }
            } catch (SocketException e4) {
                socketexception = e4;
                throw new IOException(socketexception.getMessage());
            }
        }
    }

    public void addReceiveStreamListener(ReceiveStreamListener receivestreamlistener) {
        if (!this.streamlistener.contains(receivestreamlistener)) {
            this.streamlistener.addElement(receivestreamlistener);
        }
    }

    public void addRemoteListener(RemoteListener remotelistener1) {
        if (!this.remotelistener.contains(remotelistener1)) {
            this.remotelistener.addElement(remotelistener1);
        }
    }

    /* access modifiers changed from: 0000 */
    public void addSendStream(SendStream sendstream) {
        this.sendstreamlist.addElement(sendstream);
    }

    public void addSendStreamListener(SendStreamListener sendstreamlistener1) {
        if (!this.sendstreamlistener.contains(sendstreamlistener1)) {
            this.sendstreamlistener.addElement(sendstreamlistener1);
        }
    }

    public void addSessionListener(SessionListener sessionlistener1) {
        if (!this.sessionlistener.contains(sessionlistener1)) {
            this.sessionlistener.addElement(sessionlistener1);
        }
    }

    public void addTarget(SessionAddress sessionaddress) throws IOException {
        this.remoteAddresses.addElement(sessionaddress);
        if (this.remoteAddresses.size() > 1) {
            setRemoteAddresses();
            return;
        }
        this.remoteAddress = sessionaddress;
        logger.finest("Added target: " + sessionaddress);
        try {
            this.rtcpRawReceiver = new RTCPRawReceiver(this.localAddress, sessionaddress, this.defaultstats, this.streamSynch, this.controlSocket);
            this.rtpRawReceiver = new RTPRawReceiver(this.localAddress, sessionaddress, this.defaultstats, this.dataSocket);
            this.rtpDemultiplexer = new RTPDemultiplexer(this.cache, this.rtpRawReceiver, this.streamSynch);
            this.rtcpForwarder = new PacketForwarder(this.rtcpRawReceiver, new RTCPReceiver(this.cache));
            if (this.rtpRawReceiver != null) {
                this.rtpForwarder = new PacketForwarder(this.rtpRawReceiver, new RTPReceiver(this.cache, this.rtpDemultiplexer));
            }
            this.rtcpForwarder.startPF("RTCP Forwarder for address" + sessionaddress.getControlHostAddress() + " port " + sessionaddress.getControlPort());
            if (this.rtpForwarder != null) {
                this.rtpForwarder.startPF("RTP Forwarder for address " + sessionaddress.getDataHostAddress() + " port " + sessionaddress.getDataPort());
            }
            this.cleaner = new SSRCCacheCleaner(this.cache, this.streamSynch);
            if (this.cache.ourssrc != null && this.participating) {
                this.cache.ourssrc.reporter = startParticipating(this.rtcpRawReceiver.socket);
            }
        } catch (SocketException socketexception) {
            throw new IOException(socketexception.getMessage());
        } catch (UnknownHostException unknownhostexception) {
            throw new IOException(unknownhostexception.getMessage());
        }
    }

    public void addUnicastAddr(InetAddress inetaddress) {
        if (this.sender != null) {
            this.sender.addDestAddr(inetaddress);
        }
    }

    private void CheckRTPAddress(InetAddress inetaddress, InetAddress inetaddress1) throws InvalidSessionAddressException {
        if (inetaddress == null && inetaddress1 == null) {
            throw new InvalidSessionAddressException("Data and control addresses are null");
        }
        if (inetaddress1 == null && inetaddress != null) {
            inetaddress1 = inetaddress;
        }
        if (inetaddress == null && inetaddress1 != null) {
            inetaddress = inetaddress1;
        }
    }

    private void CheckRTPPorts(int i, int j) throws InvalidSessionAddressException {
        if (i == 0 || i == -1) {
            i = j - 1;
        }
        if (j == 0 || j == -1) {
            j = i + 1;
        }
        if (i != 0 && i % 2 != 0) {
            throw new InvalidSessionAddressException("Data Port must be valid and even");
        } else if (j != 0 && j % 2 != 1) {
            throw new InvalidSessionAddressException("Control Port must be valid and odd");
        } else if (j != i + 1) {
            throw new InvalidSessionAddressException("Control Port must be one higher than the Data Port");
        }
    }

    public void closeSession() {
        if (this.dslist.isEmpty() || this.nosockets) {
            closeSession("DataSource disconnected");
        }
    }

    public void closeSession(String s) {
        stopParticipating(s, this.cache.ourssrc);
        if (this.defaultsource != null) {
            this.defaultsource.disconnect();
        }
        if (this.cache != null) {
            Enumeration elements = this.cache.cache.elements();
            while (elements.hasMoreElements()) {
                SSRCInfo ssrcinfo = (SSRCInfo) elements.nextElement();
                if (ssrcinfo.dstream != null) {
                    ssrcinfo.dstream.close();
                }
                if (ssrcinfo instanceof SendSSRCInfo) {
                    ((SendSSRCInfo) ssrcinfo).close();
                }
                stopParticipating(s, ssrcinfo);
            }
        }
        for (int i = 0; i < this.sendstreamlist.size(); i++) {
            removeSendStream((SendStream) this.sendstreamlist.elementAt(i));
        }
        if (this.rtpTransmitter != null) {
            this.rtpTransmitter.close();
        }
        if (this.rtcpForwarder != null) {
            RTCPRawReceiver rtcprawreceiver = (RTCPRawReceiver) this.rtcpForwarder.getSource();
            this.rtcpForwarder.close();
            if (rtcprawreceiver != null) {
                rtcprawreceiver.close();
            }
        }
        if (this.cleaner != null) {
            this.cleaner.stop();
        }
        if (this.cache != null) {
            this.cache.destroy();
        }
        if (this.rtpForwarder != null) {
            RTPRawReceiver rtprawreceiver = (RTPRawReceiver) this.rtpForwarder.getSource();
            this.rtpForwarder.close();
            if (rtprawreceiver != null) {
                rtprawreceiver.close();
            }
        }
        if (this.multi_unicast) {
            removeAllPeers();
        }
    }

    public DataSource createNewDS(int i) {
        DataSource datasource = new DataSource();
        datasource.setContentType(ContentDescriptor.RAW);
        try {
            datasource.connect();
        } catch (IOException ioexception) {
            logger.log(Level.WARNING, "Error connecting data source " + ioexception.getMessage(), ioexception);
        }
        ((BufferControlImpl) this.buffercontrol).addSourceStream(new RTPSourceStream(datasource));
        this.dslist.put(i, datasource);
        datasource.setSSRC(i);
        datasource.setMgr(this);
        return datasource;
    }

    public DataSource createNewDS(RTPMediaLocator rtpmedialocator) {
        DataSource datasource = new DataSource();
        datasource.setContentType(ContentDescriptor.RAW);
        try {
            datasource.connect();
        } catch (IOException ioexception) {
            logger.log(Level.WARNING, "IOException in createNewDS() " + ioexception.getMessage(), ioexception);
        }
        RTPSourceStream rtpsourcestream = new RTPSourceStream(datasource);
        ((BufferControlImpl) this.buffercontrol).addSourceStream(rtpsourcestream);
        if (rtpmedialocator == null || ((int) rtpmedialocator.getSSRC()) == 0) {
            this.defaultsource = datasource;
            this.defaultstream = rtpsourcestream;
        } else {
            this.dslist.put((int) rtpmedialocator.getSSRC(), datasource);
            datasource.setSSRC((int) rtpmedialocator.getSSRC());
            datasource.setMgr(this);
        }
        return datasource;
    }

    public SendStream createSendStream(int ssrc, javax.media.protocol.DataSource datasource, int j) throws UnsupportedFormatException, IOException, SSRCInUseException {
        if (this.sendercount != 0 && this.cache.lookup(ssrc) != null) {
            throw new SSRCInUseException("SSRC supplied is already in use");
        } else if (this.cache.rtcp_bw_fraction == Pa.LATENCY_UNSPECIFIED) {
            throw new IOException("Initialized with zero RTP/RTCP outgoing bandwidth. Cannot create a sending stream ");
        } else {
            PushBufferStream pushbufferstream = ((PushBufferDataSource) datasource).getStreams()[j];
            Format format = pushbufferstream.getFormat();
            int l = this.formatinfo.getPayload(format);
            if (l == -1) {
                throw new UnsupportedFormatException("Format of Stream not supported in RTP Session Manager", format);
            }
            SendSSRCInfo obj;
            if (this.sendercount == 0) {
                obj = new SendSSRCInfo(this.cache.ourssrc);
                obj.ours = true;
                this.cache.ourssrc = obj;
                this.cache.getMainCache().put(obj.ssrc, obj);
            } else {
                SSRCInfo obj2 = (SendSSRCInfo) this.cache.get(ssrc, this.dataaddress, this.dataport, 3);
                obj2.ours = true;
                if (this.nosockets) {
                    obj2.reporter = startParticipating(this.rtcpsource, obj2);
                } else {
                    obj2.reporter = startParticipating(this.controlport, this.controladdress.getHostAddress(), obj2);
                }
            }
            obj2.payloadType = l;
            obj2.sinkstream.setSSRCInfo(obj2);
            obj2.setFormat(format);
            if (format instanceof VideoFormat) {
                obj2.clockrate = 90000;
            } else if (format instanceof AudioFormat) {
                obj2.clockrate = (int) ((AudioFormat) format).getSampleRate();
            } else {
                throw new UnsupportedFormatException("Format not supported", format);
            }
            obj2.pds = datasource;
            pushbufferstream.setTransferHandler(obj2.sinkstream);
            if (this.multi_unicast) {
                if (this.peerlist.size() > 0) {
                    SessionAddress sessionaddress = (SessionAddress) this.peerlist.firstElement();
                    this.dataport = sessionaddress.getDataPort();
                    this.dataaddress = sessionaddress.getDataAddress();
                } else {
                    throw new IOException("At least one peer must be added");
                }
            }
            if (this.rtpTransmitter == null) {
                if (this.rtpConnector != null) {
                    this.rtpTransmitter = startDataTransmission(this.rtpConnector);
                } else if (this.nosockets) {
                    this.rtpTransmitter = startDataTransmission(this.rtpsource);
                } else {
                    if (this.newRtpInterface) {
                        this.dataport = this.remoteAddress.getDataPort();
                        this.dataaddress = this.remoteAddress.getDataAddress();
                    }
                    this.rtpTransmitter = startDataTransmission(this.dataport, this.dataaddress.getHostAddress());
                }
                if (this.rtpTransmitter == null) {
                    throw new IOException("Cannot create a transmitter");
                }
            }
            obj2.sinkstream.setTransmitter(this.rtpTransmitter);
            addSendStream(obj2);
            if (this.multi_unicast) {
                for (int i1 = 0; i1 < this.peerlist.size(); i1++) {
                    SessionAddress sessionaddress1 = (SessionAddress) this.peerlist.elementAt(i1);
                    if (obj2.sinkstream.transmitter.sender.peerlist == null) {
                        obj2.sinkstream.transmitter.sender.peerlist = new Vector();
                    }
                    obj2.sinkstream.transmitter.sender.peerlist.addElement(sessionaddress1);
                    if (this.cache != null) {
                        Enumeration elements = this.cache.cache.elements();
                        while (elements.hasMoreElements()) {
                            SSRCInfo ssrcinfo1 = (SSRCInfo) elements.nextElement();
                            if (ssrcinfo1 instanceof SendSSRCInfo) {
                                ssrcinfo1.reporter.transmit.sender.control = true;
                                if (ssrcinfo1.reporter.transmit.sender.peerlist == null) {
                                    ssrcinfo1.reporter.transmit.sender.peerlist = new Vector();
                                }
                                ssrcinfo1.reporter.transmit.sender.peerlist.addElement(sessionaddress1);
                            }
                        }
                    }
                }
            }
            obj2.sinkstream.startStream();
            this.cache.eventhandler.postEvent(new NewSendStreamEvent(this, obj2));
            return obj2;
        }
    }

    public SendStream createSendStream(javax.media.protocol.DataSource datasource, int i) throws IOException, UnsupportedFormatException {
        int ssrc;
        if (this.sendercount != 0 || this.cache.ourssrc == null) {
            do {
                ssrc = (int) generateSSRC(GenerateSSRCCause.CREATE_SEND_STREAM);
            } while (this.cache.lookup(ssrc) != null);
        } else {
            ssrc = this.cache.ourssrc.ssrc;
        }
        SendStream sendstream = null;
        try {
            sendstream = createSendStream(ssrc, datasource, i);
            if (this.newRtpInterface) {
                setRemoteAddresses();
            }
        } catch (SSRCInUseException e) {
        }
        return sendstream;
    }

    public void dispose() {
        if (this.rtpConnector != null) {
            this.rtpConnector.close();
            this.rtpConnector = null;
        }
        if (this.defaultsource != null) {
            this.defaultsource.disconnect();
        }
        if (this.cache != null) {
            Enumeration elements = this.cache.cache.elements();
            while (elements.hasMoreElements()) {
                SSRCInfo ssrcinfo = (SSRCInfo) elements.nextElement();
                if (ssrcinfo.dstream != null) {
                    ssrcinfo.dstream.close();
                }
                if (ssrcinfo instanceof SendSSRCInfo) {
                    ((SendSSRCInfo) ssrcinfo).close();
                }
                stopParticipating("dispose", ssrcinfo);
            }
        }
        for (int i = 0; i < this.sendstreamlist.size(); i++) {
            removeSendStream((SendStream) this.sendstreamlist.elementAt(i));
        }
        if (this.rtpTransmitter != null) {
            this.rtpTransmitter.close();
        }
        if (this.rtcpTransmitter != null) {
            this.rtcpTransmitter.close();
        }
        if (this.rtcpForwarder != null) {
            RTCPRawReceiver rtcprawreceiver = (RTCPRawReceiver) this.rtcpForwarder.getSource();
            this.rtcpForwarder.close();
            if (rtcprawreceiver != null) {
                rtcprawreceiver.close();
            }
        }
        if (this.cleaner != null) {
            this.cleaner.stop();
        }
        if (this.cache != null) {
            this.cache.destroy();
        }
        if (this.rtpForwarder != null) {
            RTPRawReceiver rtprawreceiver = (RTPRawReceiver) this.rtpForwarder.getSource();
            this.rtpForwarder.close();
            if (rtprawreceiver != null) {
                rtprawreceiver.close();
            }
        }
        if (this.dataSocket != null) {
            this.dataSocket.close();
        }
        if (this.controlSocket != null) {
            this.controlSocket.close();
        }
    }

    private int findLocalPorts() {
        boolean flag = false;
        int i = -1;
        while (!flag) {
            while (true) {
                i = (int) (65535.0d * Math.random());
                if (i % 2 != 0) {
                    i++;
                }
                if (i >= 1024 && i <= 65534) {
                    try {
                        break;
                    } catch (SocketException e) {
                        flag = false;
                    }
                }
            }
            new DatagramSocket(i).close();
            new DatagramSocket(i + 1).close();
            flag = true;
        }
        return i;
    }

    public String generateCNAME() {
        return SourceDescription.generateCNAME();
    }

    public long generateSSRC() {
        return (long) TrueRandom.nextInt();
    }

    /* access modifiers changed from: protected */
    public long generateSSRC(GenerateSSRCCause cause) {
        return generateSSRC();
    }

    public Vector getActiveParticipants() {
        Vector vector1 = new Vector();
        Enumeration enumeration = this.cache.getRTPSICache().getCacheTable().elements();
        while (enumeration.hasMoreElements()) {
            Participant participant = (Participant) enumeration.nextElement();
            if (!(participant != null && (participant instanceof LocalParticipant) && this.nonparticipating) && participant.getStreams().size() > 0) {
                vector1.addElement(participant);
            }
        }
        return vector1;
    }

    public Vector getAllParticipants() {
        Vector vector = new Vector();
        Enumeration enumeration = this.cache.getRTPSICache().getCacheTable().elements();
        while (enumeration.hasMoreElements()) {
            Participant participant = (Participant) enumeration.nextElement();
            if (!(participant == null || ((participant instanceof LocalParticipant) && this.nonparticipating))) {
                vector.addElement(participant);
            }
        }
        return vector;
    }

    public Object getControl(String s) {
        if (s.equals("javax.media.control.BufferControl")) {
            return this.buffercontrol;
        }
        return null;
    }

    public Object[] getControls() {
        return new Object[]{this.buffercontrol};
    }

    public DataSource getDataSource(RTPMediaLocator rtpmedialocator) {
        if (rtpmedialocator == null) {
            return this.defaultsource;
        }
        int i = (int) rtpmedialocator.getSSRC();
        if (i == 0) {
            return this.defaultsource;
        }
        return (DataSource) this.dslist.get(i);
    }

    public long getDefaultSSRC() {
        return this.defaultSSRC;
    }

    public Format getFormat(int i) {
        return this.formatinfo.get(i);
    }

    public GlobalReceptionStats getGlobalReceptionStats() {
        return this.defaultstats;
    }

    public GlobalTransmissionStats getGlobalTransmissionStats() {
        return this.transstats;
    }

    public LocalParticipant getLocalParticipant() {
        Enumeration enumeration = this.cache.getRTPSICache().getCacheTable().elements();
        while (enumeration.hasMoreElements()) {
            Participant participant = (Participant) enumeration.nextElement();
            if (participant != null && !this.nonparticipating && (participant instanceof LocalParticipant)) {
                return (LocalParticipant) participant;
            }
        }
        return null;
    }

    public SessionAddress getLocalReceiverAddress() {
        return this.localReceiverAddress;
    }

    public SessionAddress getLocalSessionAddress() {
        if (this.newRtpInterface) {
            return this.localAddress;
        }
        return new SessionAddress(this.localDataAddress, this.localDataPort, this.localControlAddress, this.localControlPort);
    }

    public long getLocalSSRC() {
        return (this.cache == null || this.cache.ourssrc == null) ? CachingControl.LENGTH_UNKNOWN : (long) this.cache.ourssrc.ssrc;
    }

    public int getMulticastScope() {
        return this.ttl;
    }

    public Vector getPassiveParticipants() {
        Vector vector1 = new Vector();
        Enumeration enumeration = this.cache.getRTPSICache().getCacheTable().elements();
        while (enumeration.hasMoreElements()) {
            Participant participant = (Participant) enumeration.nextElement();
            if (!(participant != null && (participant instanceof LocalParticipant) && this.nonparticipating) && participant.getStreams().size() == 0) {
                vector1.addElement(participant);
            }
        }
        return vector1;
    }

    public Vector getPeers() {
        return this.peerlist;
    }

    private String getProperty(String s) {
        String s1 = null;
        try {
            return System.getProperty(s);
        } catch (Throwable th) {
            return s1;
        }
    }

    public Vector getReceiveStreams() {
        Vector vector = new Vector();
        Vector vector1 = getAllParticipants();
        for (int i = 0; i < vector1.size(); i++) {
            Vector vector2 = ((Participant) vector1.elementAt(i)).getStreams();
            for (int j = 0; j < vector2.size(); j++) {
                RTPStream rtpstream = (RTPStream) vector2.elementAt(j);
                if (rtpstream instanceof ReceiveStream) {
                    vector.addElement(rtpstream);
                }
            }
        }
        vector.trimToSize();
        return vector;
    }

    public Vector getRemoteParticipants() {
        Vector vector = new Vector();
        Enumeration enumeration = this.cache.getRTPSICache().getCacheTable().elements();
        while (enumeration.hasMoreElements()) {
            Participant participant = (Participant) enumeration.nextElement();
            if (participant != null && (participant instanceof RemoteParticipant)) {
                vector.addElement(participant);
            }
        }
        return vector;
    }

    public SessionAddress getRemoteSessionAddress() {
        return this.remoteAddress;
    }

    public Vector getSendStreams() {
        return new Vector(this.sendstreamlist);
    }

    public SessionAddress getSessionAddress() {
        return new SessionAddress(this.dataaddress, this.dataport, this.controladdress, this.controlport);
    }

    public int getSSRC() {
        return 0;
    }

    /* access modifiers changed from: 0000 */
    public SSRCCache getSSRCCache() {
        return this.cache;
    }

    public SSRCInfo getSSRCInfo(int i) {
        return this.cache.lookup(i);
    }

    public RTPStream getStream(long l) {
        Vector vector = getAllParticipants();
        if (vector == null) {
            return null;
        }
        for (int i = 0; i < vector.size(); i++) {
            RTPStream rtpstream = ((RTPSourceInfo) vector.elementAt(i)).getSSRCStream(l);
            if (rtpstream != null) {
                return rtpstream;
            }
        }
        return null;
    }

    public void initialize(RTPConnector rtpconnector) {
        this.rtpConnector = rtpconnector;
        this.newRtpInterface = true;
        String s = SourceDescription.generateCNAME();
        SourceDescription[] asourcedescription = new SourceDescription[]{new SourceDescription(3, SOURCE_DESC_EMAIL, 1, false), new SourceDescription(1, s, 1, false), new SourceDescription(6, SOURCE_DESC_TOOL, 1, false)};
        int ssrc = (int) generateSSRC(GenerateSSRCCause.INITIALIZE);
        this.ttl = 1;
        this.participating = this.rtpConnector.getRTCPBandwidthFraction() != Pa.LATENCY_UNSPECIFIED;
        this.cache = new SSRCCache(this);
        this.cache.sessionbandwidth = 384000;
        this.formatinfo.setCache(this.cache);
        if (this.rtpConnector.getRTCPBandwidthFraction() > Pa.LATENCY_UNSPECIFIED) {
            this.cache.rtcp_bw_fraction = this.rtpConnector.getRTCPBandwidthFraction();
        } else {
            this.cache.rtcp_bw_fraction = 0.05d;
        }
        if (this.rtpConnector.getRTCPSenderBandwidthFraction() > Pa.LATENCY_UNSPECIFIED) {
            this.cache.rtcp_sender_bw_fraction = this.rtpConnector.getRTCPSenderBandwidthFraction();
        } else {
            this.cache.rtcp_sender_bw_fraction = 0.25d;
        }
        this.cache.ourssrc = this.cache.get(ssrc, null, 0, 2);
        this.cache.ourssrc.setAlive(true);
        if (isCNAME(asourcedescription)) {
            this.cache.ourssrc.setSourceDescription(asourcedescription);
        } else {
            this.cache.ourssrc.setSourceDescription(setCNAME(asourcedescription));
        }
        this.cache.ourssrc.ssrc = ssrc;
        this.cache.ourssrc.setOurs(true);
        this.initialized = true;
        this.rtpRawReceiver = new RTPRawReceiver(this.rtpConnector, this.defaultstats);
        this.rtcpRawReceiver = new RTCPRawReceiver(this.rtpConnector, this.defaultstats, this.streamSynch);
        this.rtpDemultiplexer = new RTPDemultiplexer(this.cache, this.rtpRawReceiver, this.streamSynch);
        this.rtpForwarder = new PacketForwarder(this.rtpRawReceiver, new RTPReceiver(this.cache, this.rtpDemultiplexer));
        if (this.rtpForwarder != null) {
            this.rtpForwarder.startPF("RTP Forwarder: " + this.rtpConnector);
        }
        this.rtcpForwarder = new PacketForwarder(this.rtcpRawReceiver, new RTCPReceiver(this.cache));
        if (this.rtcpForwarder != null) {
            this.rtcpForwarder.startPF("RTCP Forwarder: " + this.rtpConnector);
        }
        this.cleaner = new SSRCCacheCleaner(this.cache, this.streamSynch);
        if (this.participating && this.cache.ourssrc != null) {
            this.cache.ourssrc.reporter = startParticipating(this.rtpConnector, this.cache.ourssrc);
        }
    }

    public void initialize(SessionAddress sessionaddress) throws InvalidSessionAddressException {
        String s = SourceDescription.generateCNAME();
        SessionAddress[] asessionaddress = new SessionAddress[]{sessionaddress};
        initialize(asessionaddress, new SourceDescription[]{new SourceDescription(3, SOURCE_DESC_EMAIL, 1, false), new SourceDescription(1, s, 1, false), new SourceDescription(6, SOURCE_DESC_TOOL, 1, false)}, 0.05d, 0.25d, null);
    }

    public void initialize(SessionAddress[] asessionaddress, SourceDescription[] asourcedescription, double d, double d1, EncryptionInfo encryptioninfo) throws InvalidSessionAddressException {
        if (!this.initialized) {
            this.newRtpInterface = true;
            this.remoteAddresses = new Vector();
            int i = (int) generateSSRC(GenerateSSRCCause.INITIALIZE);
            this.ttl = 1;
            this.participating = d != Pa.LATENCY_UNSPECIFIED;
            if (asessionaddress.length == 0) {
                throw new InvalidSessionAddressException("At least one local address is required!");
            }
            this.localAddress = asessionaddress[0];
            if (this.localAddress == null) {
                throw new InvalidSessionAddressException("Invalid local address: null");
            }
            try {
                String s1;
                InetAddress inetaddress = this.localAddress.getDataAddress();
                if (inetaddress.getHostAddress().equals("0.0.0.0")) {
                    s1 = "0.0.0.0";
                } else {
                    s1 = inetaddress.getHostName();
                }
                InetAddress[] ainetaddress = InetAddress.getAllByName(s1);
                if (this.localAddress.getDataAddress() == null) {
                    this.localAddress.setDataHostAddress(inetaddress);
                }
                if (this.localAddress.getControlAddress() == null) {
                    this.localAddress.setControlHostAddress(inetaddress);
                }
                if (!this.localAddress.getDataAddress().isMulticastAddress()) {
                    boolean flag = true;
                    boolean flag1 = true;
                    try {
                        logger.fine("Looking for local data address: " + this.localAddress.getDataAddress() + " and control address" + this.localAddress.getControlAddress());
                        if (this.localAddress.getDataHostAddress().equals("0.0.0.0") || this.localAddress.getDataHostAddress().equals("::0")) {
                            flag = true;
                        }
                        if (this.localAddress.getControlHostAddress().equals("0.0.0.0") || this.localAddress.getControlHostAddress().equals("::0")) {
                            flag1 = true;
                        }
                        Enumeration intfs = NetworkInterface.getNetworkInterfaces();
                        while (intfs.hasMoreElements() && (!flag || !flag1)) {
                            Enumeration addrs = ((NetworkInterface) intfs.nextElement()).getInetAddresses();
                            while (addrs.hasMoreElements()) {
                                try {
                                    InetAddress addr = (InetAddress) addrs.nextElement();
                                    logger.fine("Testing iface address " + this.localAddress.getDataAddress());
                                    if (addr.equals(this.localAddress.getDataAddress())) {
                                        flag = true;
                                    }
                                    if (addr.equals(this.localAddress.getControlAddress())) {
                                        flag1 = true;
                                    }
                                } catch (Exception e) {
                                }
                            }
                        }
                    } catch (Exception exc) {
                        logger.log(Level.WARNING, "Error while enumerating local interfaces.", exc);
                    }
                    if (!flag) {
                        throw new InvalidSessionAddressException("Local Data Address " + "Does not belong to any of this hosts local interfaces");
                    } else if (flag1) {
                        if (this.localAddress.getDataPort() == -1) {
                            int k = findLocalPorts();
                            this.localAddress.setDataPort(k);
                            this.localAddress.setControlPort(k + 1);
                        }
                        if (!this.localAddress.getDataAddress().isMulticastAddress()) {
                            try {
                                this.dataSocket = new DatagramSocket(this.localAddress.getDataPort(), this.localAddress.getDataAddress());
                            } catch (SocketException e2) {
                                throw new InvalidSessionAddressException("Can't open local data port: " + this.localAddress.getDataPort());
                            }
                        }
                        if (!this.localAddress.getControlAddress().isMulticastAddress()) {
                            try {
                                this.controlSocket = new DatagramSocket(this.localAddress.getControlPort(), this.localAddress.getControlAddress());
                            } catch (SocketException e3) {
                                if (this.dataSocket != null) {
                                    this.dataSocket.close();
                                }
                                throw new InvalidSessionAddressException("Can't open local control port: " + this.localAddress.getControlPort());
                            }
                        }
                    } else {
                        throw new InvalidSessionAddressException("Local Control Address " + "Does not belong to any of this hosts local interfaces");
                    }
                } else if (this.localAddress.getControlAddress().isMulticastAddress()) {
                    this.ttl = this.localAddress.getTimeToLive();
                } else {
                    throw new InvalidSessionAddressException("Invalid multicast address");
                }
                this.cache = new SSRCCache(this);
                if (this.ttl <= 16) {
                    this.cache.sessionbandwidth = 384000;
                } else if (this.ttl <= 64) {
                    this.cache.sessionbandwidth = 128000;
                } else if (this.ttl <= 128) {
                    this.cache.sessionbandwidth = 16000;
                } else if (this.ttl <= 192) {
                    this.cache.sessionbandwidth = 6625;
                } else {
                    this.cache.sessionbandwidth = 4000;
                }
                this.formatinfo.setCache(this.cache);
                this.cache.rtcp_bw_fraction = d;
                this.cache.rtcp_sender_bw_fraction = d1;
                this.cache.ourssrc = this.cache.get(i, inetaddress, 0, 2);
                this.cache.ourssrc.setAlive(true);
                if (isCNAME(asourcedescription)) {
                    this.cache.ourssrc.setSourceDescription(asourcedescription);
                } else {
                    this.cache.ourssrc.setSourceDescription(setCNAME(asourcedescription));
                }
                this.cache.ourssrc.ssrc = i;
                this.cache.ourssrc.setOurs(true);
                this.initialized = true;
            } catch (Throwable throwable1) {
                logger.log(Level.WARNING, "Error during initialization: " + throwable1.getMessage(), throwable1);
            }
        }
    }

    public int initSession(SessionAddress sessionaddress, long l, SourceDescription[] asourcedescription, double d, double d1) throws InvalidSessionAddressException {
        if (this.initialized) {
            return -1;
        }
        if (d == Pa.LATENCY_UNSPECIFIED) {
            this.nonparticipating = true;
        }
        this.defaultSSRC = l;
        this.localDataAddress = sessionaddress.getDataAddress();
        this.localControlAddress = sessionaddress.getControlAddress();
        this.localDataPort = sessionaddress.getDataPort();
        this.localControlPort = sessionaddress.getControlPort();
        try {
            InetAddress inetaddress = this.localAddress.getDataAddress();
            InetAddress[] ainetaddress = InetAddress.getAllByName(inetaddress.getHostName());
            if (this.localDataAddress == null) {
                this.localDataAddress = inetaddress;
            }
            if (this.localControlAddress == null) {
                this.localControlAddress = inetaddress;
            }
            boolean flag = false;
            boolean flag1 = false;
            try {
                Enumeration intfs = NetworkInterface.getNetworkInterfaces();
                while (intfs.hasMoreElements()) {
                    Enumeration addrs = ((NetworkInterface) intfs.nextElement()).getInetAddresses();
                    while (addrs.hasMoreElements()) {
                        try {
                            InetAddress addr = (InetAddress) addrs.nextElement();
                            if (addr.equals(this.localAddress.getDataAddress())) {
                                flag = true;
                            }
                            if (addr.equals(this.localAddress.getControlAddress())) {
                                flag1 = true;
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            } catch (Exception exc) {
                logger.log(Level.SEVERE, "Error while enumerating interfaces", exc);
            }
            String s2 = "Does not belong to any of this hosts local interfaces";
            if (!flag) {
                throw new InvalidSessionAddressException("Local Data Address " + s2);
            } else if (flag1) {
                this.cache = new SSRCCache(this);
                this.formatinfo.setCache(this.cache);
                this.cache.rtcp_bw_fraction = d;
                this.cache.rtcp_sender_bw_fraction = d1;
                this.cache.ourssrc = this.cache.get((int) l, inetaddress, 0, 2);
                this.cache.ourssrc.setAlive(true);
                if (isCNAME(asourcedescription)) {
                    this.cache.ourssrc.setSourceDescription(asourcedescription);
                } else {
                    this.cache.ourssrc.setSourceDescription(setCNAME(asourcedescription));
                }
                this.cache.ourssrc.ssrc = (int) l;
                this.cache.ourssrc.setOurs(true);
                this.initialized = true;
                return 0;
            } else {
                throw new InvalidSessionAddressException("Local Control Address" + s2);
            }
        } catch (Throwable throwable1) {
            logger.log(Level.WARNING, "InitSession  RTPSessionMgr :" + throwable1.getMessage(), throwable1);
            return -1;
        }
    }

    public int initSession(SessionAddress sessionaddress, SourceDescription[] asourcedescription, double d, double d1) throws InvalidSessionAddressException {
        return initSession(sessionaddress, generateSSRC(GenerateSSRCCause.INIT_SESSION), asourcedescription, d, d1);
    }

    private int initSession(SourceDescription[] asourcedescription, double d, double d1) {
        if (this.initialized) {
            return -1;
        }
        if (d == Pa.LATENCY_UNSPECIFIED) {
            this.nonparticipating = true;
        }
        this.defaultSSRC = generateSSRC(GenerateSSRCCause.INIT_SESSION);
        this.cache = new SSRCCache(this);
        this.formatinfo.setCache(this.cache);
        this.cache.rtcp_bw_fraction = d;
        this.cache.rtcp_sender_bw_fraction = d1;
        try {
            InetAddress inetaddress = InetAddress.getLocalHost();
            this.cache.ourssrc = this.cache.get((int) this.defaultSSRC, null, 0, 2);
            this.cache.ourssrc.setAlive(true);
            if (isCNAME(asourcedescription)) {
                this.cache.ourssrc.setSourceDescription(asourcedescription);
            } else {
                this.cache.ourssrc.setSourceDescription(setCNAME(asourcedescription));
            }
            this.cache.ourssrc.ssrc = (int) this.defaultSSRC;
            this.cache.ourssrc.setOurs(true);
            this.initialized = true;
            return 0;
        } catch (Throwable throwable1) {
            logger.log(Level.WARNING, "InitSession UnknownHostExcpetion " + throwable1.getMessage(), throwable1);
            return -1;
        }
    }

    /* access modifiers changed from: 0000 */
    public boolean isBroadcast(InetAddress inetaddress) {
        try {
            byte[] abyte0 = InetAddress.getLocalHost().getAddress();
            int i = (((abyte0[3] & UnsignedUtils.MAX_UBYTE) | ((abyte0[2] << 8) & 65280)) | ((abyte0[1] << 16) & 16711680)) | ((abyte0[0] << 24) & -16777216);
            byte[] abyte1 = inetaddress.getAddress();
            if ((i | UnsignedUtils.MAX_UBYTE) == ((((abyte1[3] & UnsignedUtils.MAX_UBYTE) | ((abyte1[2] << 8) & 65280)) | ((abyte1[1] << 16) & 16711680)) | ((abyte1[0] << 24) & -16777216))) {
                return true;
            }
        } catch (UnknownHostException unknownhostexception) {
            logger.warning(unknownhostexception.getMessage());
        }
        return false;
    }

    private boolean isCNAME(SourceDescription[] asourcedescription) {
        boolean flag = false;
        boolean flag2;
        if (asourcedescription == null) {
            flag2 = false;
            return 0;
        }
        for (int j = 0; j < asourcedescription.length; j++) {
            try {
                int i = asourcedescription[j].getType();
                String s = asourcedescription[j].getDescription();
                if (i == 1 && s != null) {
                    flag = true;
                }
            } catch (Exception e) {
            }
        }
        flag2 = flag;
        return flag;
    }

    public boolean isDefaultDSassigned() {
        return this.bds;
    }

    public boolean IsNonParticipating() {
        return this.nonparticipating;
    }

    public boolean isSenderDefaultAddr(InetAddress inetaddress) {
        if (this.sender == null) {
            return false;
        }
        return this.sender.getRemoteAddr().equals(inetaddress);
    }

    /* access modifiers changed from: 0000 */
    public boolean isUnicast() {
        return this.unicast;
    }

    public void removeAllPeers() {
        for (int i = 0; i < this.peerlist.size(); i++) {
            removePeer((SessionAddress) this.peerlist.elementAt(i));
        }
    }

    public void removeDataSource(DataSource datasource) {
        if (datasource == this.defaultsource) {
            this.defaultsource = null;
            this.defaultstream = null;
            this.defaultsourceid = 0;
            this.bds = false;
        }
        this.dslist.removeObj(datasource);
    }

    public void removePeer(SessionAddress sessionaddress) {
        PacketForwarder packetforwarder = (PacketForwarder) this.peerrtplist.get(sessionaddress);
        PacketForwarder packetforwarder1 = (PacketForwarder) this.peerrtplist.get(sessionaddress);
        if (packetforwarder != null) {
            packetforwarder.close();
        }
        if (packetforwarder1 != null) {
            packetforwarder1.close();
        }
        for (int i = 0; i < this.peerlist.size(); i++) {
            if (((SessionAddress) this.peerlist.elementAt(i)).equals(sessionaddress)) {
                this.peerlist.removeElementAt(i);
            }
        }
    }

    public void removeReceiveStreamListener(ReceiveStreamListener receivestreamlistener) {
        this.streamlistener.removeElement(receivestreamlistener);
    }

    public void removeRemoteListener(RemoteListener remotelistener1) {
        this.remotelistener.removeElement(remotelistener1);
    }

    /* access modifiers changed from: 0000 */
    public void removeSendStream(SendStream sendstream) {
        this.sendstreamlist.removeElement(sendstream);
        SendSSRCInfo sendstreamAsSendSSRCInfo = (SendSSRCInfo) sendstream;
        if (sendstreamAsSendSSRCInfo.sinkstream != null) {
            sendstreamAsSendSSRCInfo.sinkstream.close();
            this.cache.eventhandler.postEvent(new StreamClosedEvent(this, sendstream));
            stopParticipating("Closed Stream", sendstreamAsSendSSRCInfo);
        }
        if (this.sendstreamlist.size() == 0 && this.cache.ourssrc != null) {
            SSRCInfo passivessrcinfo;
            if (this.cache.ourssrc.ssrc == sendstreamAsSendSSRCInfo.ssrc && sendstreamAsSendSSRCInfo.reporter == null) {
                long lNewSSRC;
                int iNewSSRC = 0;
                do {
                    lNewSSRC = generateSSRC(GenerateSSRCCause.REMOVE_SEND_STREAM);
                    if (lNewSSRC == CachingControl.LENGTH_UNKNOWN) {
                        break;
                    }
                    iNewSSRC = (int) lNewSSRC;
                } while (this.cache.lookup(iNewSSRC) != null);
                if (lNewSSRC == CachingControl.LENGTH_UNKNOWN) {
                    passivessrcinfo = new PassiveSSRCInfo(this.cache.ourssrc);
                } else {
                    passivessrcinfo = this.cache.get(iNewSSRC, null, 0, 2);
                    passivessrcinfo.setAlive(true);
                    SourceDescription[] asourcedescription = new SourceDescription[]{new SourceDescription(3, SOURCE_DESC_EMAIL, 1, false), new SourceDescription(1, generateCNAME(), 1, false), new SourceDescription(6, SOURCE_DESC_TOOL, 1, false)};
                    if (!isCNAME(asourcedescription)) {
                        asourcedescription = setCNAME(asourcedescription);
                    }
                    passivessrcinfo.setSourceDescription(asourcedescription);
                    passivessrcinfo.ssrc = iNewSSRC;
                }
            } else {
                passivessrcinfo = new PassiveSSRCInfo(this.cache.ourssrc);
            }
            passivessrcinfo.setOurs(true);
            this.cache.ourssrc = passivessrcinfo;
            this.cache.getMainCache().put(passivessrcinfo.ssrc, passivessrcinfo);
            if (this.rtpConnector != null) {
                this.cache.ourssrc.reporter = startParticipating(this.rtpConnector, this.cache.ourssrc);
            }
        }
    }

    public void removeSendStreamListener(SendStreamListener sendstreamlistener2) {
    }

    public void removeSessionListener(SessionListener sessionlistener1) {
        this.sessionlistener.removeElement(sessionlistener1);
    }

    public void removeTarget(SessionAddress sessionaddress, String s) {
        this.remoteAddresses.removeElement(sessionaddress);
        setRemoteAddresses();
        if (this.remoteAddresses.size() == 0 && this.cache != null) {
            stopParticipating(s, this.cache.ourssrc);
        }
    }

    public void removeTargets(String s) {
        if (this.cache != null) {
            stopParticipating(s, this.cache.ourssrc);
        }
        if (this.remoteAddresses != null) {
            this.remoteAddresses.removeAllElements();
        }
        setRemoteAddresses();
    }

    private SourceDescription[] setCNAME(SourceDescription[] asourcedescription) {
        boolean flag = false;
        if (asourcedescription == null) {
            asourcedescription = new SourceDescription[]{new SourceDescription(1, SourceDescription.generateCNAME(), 1, false)};
            return asourcedescription;
        }
        for (int j = 0; j < asourcedescription.length; j++) {
            int i = asourcedescription[j].getType();
            String s1 = asourcedescription[j].getDescription();
            if (i == 1 && s1 == null) {
                s1 = SourceDescription.generateCNAME();
                flag = true;
                break;
            }
        }
        SourceDescription[] sourceDescriptionArr;
        if (flag) {
            sourceDescriptionArr = asourcedescription;
            return asourcedescription;
        }
        SourceDescription[] asourcedescription1 = new SourceDescription[(asourcedescription.length + 1)];
        asourcedescription1[0] = new SourceDescription(1, SourceDescription.generateCNAME(), 1, false);
        int k = 1;
        for (int l = 0; l < asourcedescription.length; l++) {
            asourcedescription1[k] = new SourceDescription(asourcedescription[l].getType(), asourcedescription[l].getDescription(), 1, false);
            k++;
        }
        sourceDescriptionArr = asourcedescription;
        return asourcedescription1;
    }

    public void setDefaultDSassigned(int i) {
        this.bds = true;
        this.defaultsourceid = i;
        this.dslist.put(i, this.defaultsource);
        this.defaultsource.setSSRC(i);
        this.defaultsource.setMgr(this);
    }

    public void setMulticastScope(int i) {
        if (i < 1) {
            i = 1;
        }
        this.ttl = i;
        if (this.ttl <= 16) {
            this.cache.sessionbandwidth = 384000;
        } else if (this.ttl <= 64) {
            this.cache.sessionbandwidth = 128000;
        } else if (this.ttl <= 128) {
            this.cache.sessionbandwidth = 16000;
        } else if (this.ttl <= 192) {
            this.cache.sessionbandwidth = 6625;
        } else {
            this.cache.sessionbandwidth = 4000;
        }
        if (this.udpsender != null) {
            try {
                this.udpsender.setttl(this.ttl);
            } catch (IOException ioexception) {
                logger.log(Level.WARNING, "setMulticastScope Exception ", ioexception);
            }
        }
    }

    private void setRemoteAddresses() {
        if (this.rtpTransmitter != null) {
            this.rtpTransmitter.getSender().setDestAddresses(this.remoteAddresses);
        }
        if (this.rtcpTransmitter != null) {
            this.rtcpTransmitter.getSender().setDestAddresses(this.remoteAddresses);
        }
    }

    private SourceDescription[] setSDES() {
        return new SourceDescription[]{new SourceDescription(2, getProperty("user.name"), 1, false), new SourceDescription(1, SourceDescription.generateCNAME(), 1, false), new SourceDescription(6, SOURCE_DESC_TOOL, 1, false)};
    }

    /* access modifiers changed from: 0000 */
    public void setSessionBandwidth(int i) {
        this.cache.sessionbandwidth = i;
    }

    private RTPTransmitter startDataTransmission(int i, String s) throws IOException {
        if (this.localDataPort == -1) {
            this.udpsender = new UDPPacketSender(this.dataaddress, this.dataport);
        } else if (this.newRtpInterface) {
            this.udpsender = new UDPPacketSender(this.rtpRawReceiver.socket);
        } else {
            this.udpsender = new UDPPacketSender(this.localSenderAddress.getDataPort(), this.localSenderAddress.getDataAddress(), this.dataaddress, this.dataport);
        }
        if (this.ttl != 1) {
            this.udpsender.setttl(this.ttl);
        }
        return new RTPTransmitter(this.cache, new RTPRawSender(this.dataport, s, this.udpsender));
    }

    private RTPTransmitter startDataTransmission(RTPConnector rtpconnector) {
        try {
            this.rtpsender = new RTPPacketSender(rtpconnector);
            RTPRawSender rtprawsender = new RTPRawSender(this.rtpsender);
            try {
                RTPTransmitter rtptransmitter = new RTPTransmitter(this.cache, rtprawsender);
                return rtptransmitter;
            } catch (IOException e) {
                RTPRawSender rTPRawSender = rtprawsender;
                return null;
            }
        } catch (IOException e2) {
            return null;
        }
    }

    private RTPTransmitter startDataTransmission(RTPPushDataSource rtppushdatasource) {
        this.rtpsender = new RTPPacketSender(rtppushdatasource);
        return new RTPTransmitter(this.cache, new RTPRawSender(this.rtpsender));
    }

    private synchronized RTCPReporter startParticipating(DatagramSocket datagramsocket) throws IOException {
        RTCPReporter rtcpreporter;
        UDPPacketSender udppacketsender = new UDPPacketSender(datagramsocket);
        this.udpPacketSender = udppacketsender;
        if (this.ttl != 1) {
            udppacketsender.setttl(this.ttl);
        }
        this.rtcpTransmitter = new RTCPTransmitter(this.cache, new RTCPRawSender(this.remoteAddress.getControlPort(), this.remoteAddress.getControlAddress().getHostAddress(), udppacketsender));
        this.rtcpTransmitter.setSSRCInfo(this.cache.ourssrc);
        rtcpreporter = new RTCPReporter(this.cache, this.rtcpTransmitter);
        this.startedparticipating = true;
        return rtcpreporter;
    }

    private synchronized RTCPReporter startParticipating(int i, String s, SSRCInfo ssrcinfo) throws IOException {
        RTCPTransmitter rtcptransmitter;
        UDPPacketSender udppacketsender;
        this.startedparticipating = true;
        if (this.localControlPort == -1) {
            udppacketsender = new UDPPacketSender(this.controladdress, this.controlport);
            this.localControlPort = udppacketsender.getLocalPort();
            this.localControlAddress = udppacketsender.getLocalAddress();
        } else {
            udppacketsender = new UDPPacketSender(this.localControlPort, this.localControlAddress, this.controladdress, this.controlport);
        }
        if (this.ttl != 1) {
            udppacketsender.setttl(this.ttl);
        }
        rtcptransmitter = new RTCPTransmitter(this.cache, new RTCPRawSender(i, s, udppacketsender));
        rtcptransmitter.setSSRCInfo(ssrcinfo);
        return new RTCPReporter(this.cache, rtcptransmitter);
    }

    private synchronized RTCPReporter startParticipating(RTPConnector rtpconnector, SSRCInfo ssrcinfo) {
        RTCPTransmitter rtcptransmitter;
        this.startedparticipating = true;
        try {
            this.rtpsender = new RTPPacketSender(rtpconnector.getControlOutputStream());
        } catch (IOException ioexception) {
            logger.log(Level.WARNING, "error initializing rtp sender  " + ioexception.getMessage(), ioexception);
        }
        rtcptransmitter = new RTCPTransmitter(this.cache, new RTCPRawSender(this.rtpsender));
        rtcptransmitter.setSSRCInfo(ssrcinfo);
        return new RTCPReporter(this.cache, rtcptransmitter);
    }

    private synchronized RTCPReporter startParticipating(RTPPushDataSource rtppushdatasource, SSRCInfo ssrcinfo) {
        RTCPTransmitter rtcptransmitter;
        this.startedparticipating = true;
        this.rtpsender = new RTPPacketSender(rtppushdatasource);
        rtcptransmitter = new RTCPTransmitter(this.cache, new RTCPRawSender(this.rtpsender));
        rtcptransmitter.setSSRCInfo(ssrcinfo);
        return new RTCPReporter(this.cache, rtcptransmitter);
    }

    private synchronized RTCPReporter startParticipating(SessionAddress sessionaddress, SessionAddress sessionaddress1, SSRCInfo ssrcinfo, DatagramSocket datagramsocket) throws IOException {
        RTCPTransmitter rtcptransmitter;
        UDPPacketSender udppacketsender;
        this.localReceiverAddress = sessionaddress;
        this.startedparticipating = true;
        int i = sessionaddress1.getControlPort();
        InetAddress inetaddress = sessionaddress1.getControlAddress();
        int j = sessionaddress.getControlPort();
        InetAddress inetaddress1 = sessionaddress.getControlAddress();
        if (i == -1) {
            udppacketsender = new UDPPacketSender(inetaddress, i);
        } else if (i == j) {
            udppacketsender = new UDPPacketSender(datagramsocket);
        } else {
            udppacketsender = new UDPPacketSender(i, inetaddress, this.controladdress, this.controlport);
        }
        if (this.ttl != 1) {
            udppacketsender.setttl(this.ttl);
        }
        rtcptransmitter = new RTCPTransmitter(this.cache, new RTCPRawSender(this.controlport, this.controladdress.getHostAddress(), udppacketsender));
        rtcptransmitter.setSSRCInfo(ssrcinfo);
        return new RTCPReporter(this.cache, rtcptransmitter);
    }

    /* access modifiers changed from: 0000 */
    public void startRTCPReports(InetAddress inetaddress) {
        if (!this.nonparticipating && !this.startedparticipating) {
            try {
                if (this.cache.ourssrc != null) {
                    this.cache.ourssrc.reporter = startParticipating(this.controlport, inetaddress.getHostAddress(), this.cache.ourssrc);
                }
            } catch (IOException ioexception) {
                logger.log(Level.WARNING, "start rtcp reports  " + ioexception.getMessage(), ioexception);
            }
        }
    }

    public void startSession() throws IOException {
        try {
            startSession(new SessionAddress(this.dataaddress, this.dataport, this.controladdress, this.controlport), this.ttl, null);
        } catch (SessionManagerException sessionmanagerexception) {
            throw new IOException("SessionManager exception " + sessionmanagerexception.getMessage());
        }
    }

    public int startSession(int i, EncryptionInfo encryptioninfo) throws IOException {
        this.multi_unicast = true;
        if (i < 1) {
            i = 1;
        }
        this.ttl = i;
        if (this.ttl <= 16) {
            this.cache.sessionbandwidth = 384000;
        } else if (this.ttl <= 64) {
            this.cache.sessionbandwidth = 128000;
        } else if (this.ttl <= 128) {
            this.cache.sessionbandwidth = 16000;
        } else if (this.ttl <= 192) {
            this.cache.sessionbandwidth = 6625;
        } else {
            this.cache.sessionbandwidth = 4000;
        }
        this.cleaner = new SSRCCacheCleaner(this.cache, this.streamSynch);
        return 0;
    }

    private int startSession(RTPPushDataSource rtppushdatasource, RTPPushDataSource rtppushdatasource1, EncryptionInfo encryptioninfo) {
        if (!this.initialized || this.started) {
            return -1;
        }
        this.cache.sessionbandwidth = 384000;
        RTPRawReceiver rtprawreceiver = new RTPRawReceiver(rtppushdatasource, this.defaultstats);
        RTCPRawReceiver rtcprawreceiver = new RTCPRawReceiver(rtppushdatasource1, this.defaultstats, this.streamSynch);
        this.rtpDemultiplexer = new RTPDemultiplexer(this.cache, rtprawreceiver, this.streamSynch);
        this.rtpForwarder = new PacketForwarder(rtprawreceiver, new RTPReceiver(this.cache, this.rtpDemultiplexer));
        if (this.rtpForwarder != null) {
            this.rtpForwarder.startPF("RTP Forwarder " + rtppushdatasource);
        }
        this.rtcpForwarder = new PacketForwarder(rtcprawreceiver, new RTCPReceiver(this.cache));
        if (this.rtcpForwarder != null) {
            this.rtcpForwarder.startPF("RTCP Forwarder " + rtppushdatasource);
        }
        this.cleaner = new SSRCCacheCleaner(this.cache, this.streamSynch);
        if (!(this.nonparticipating || this.cache.ourssrc == null)) {
            this.cache.ourssrc.reporter = startParticipating(rtppushdatasource1, this.cache.ourssrc);
        }
        this.started = true;
        return 0;
    }

    public int startSession(SessionAddress sessionaddress, int i, EncryptionInfo encryptioninfo) throws IOException, InvalidSessionAddressException {
        SocketException socketexception1;
        Throwable th;
        SocketException socketexception;
        if (this.started) {
            return -1;
        }
        if (i < 1) {
            i = 1;
        }
        this.ttl = i;
        if (this.ttl <= 16) {
            this.cache.sessionbandwidth = 384000;
        } else if (this.ttl <= 64) {
            this.cache.sessionbandwidth = 128000;
        } else if (this.ttl <= 128) {
            this.cache.sessionbandwidth = 16000;
        } else if (this.ttl <= 192) {
            this.cache.sessionbandwidth = 6625;
        } else {
            this.cache.sessionbandwidth = 4000;
        }
        this.controlport = sessionaddress.getControlPort();
        this.dataport = sessionaddress.getDataPort();
        CheckRTPPorts(this.dataport, this.controlport);
        this.dataaddress = sessionaddress.getDataAddress();
        this.controladdress = sessionaddress.getControlAddress();
        CheckRTPAddress(this.dataaddress, this.controladdress);
        RTCPRawReceiver rtcprawreceiver = null;
        RTPRawReceiver rtprawreceiver = null;
        try {
            InetAddress inetaddress = InetAddress.getLocalHost();
            if (this.dataaddress.equals(inetaddress)) {
                this.unicast = true;
            }
            if (!(this.dataaddress.isMulticastAddress() || this.dataaddress.equals(inetaddress))) {
                boolean z = !isBroadcast(this.dataaddress) || Win32();
                this.bindtome = z;
            }
            if (this.bindtome) {
                return -1;
            }
            try {
                RTCPRawReceiver rtcprawreceiver2 = new RTCPRawReceiver(this.controlport, this.controladdress.getHostAddress(), this.defaultstats, this.streamSynch);
                try {
                    if (this.dataaddress != null) {
                        rtprawreceiver = new RTPRawReceiver(this.dataport, this.dataaddress.getHostAddress(), this.defaultstats);
                    }
                    if (!(this.dataaddress == null || rtprawreceiver != null || rtcprawreceiver2 == null)) {
                        logger.warning("could not create RTCP/RTP raw receivers");
                        rtcprawreceiver2.closeSource();
                    }
                    try {
                        rtcprawreceiver = new RTCPRawReceiver(this.controlport, inetaddress.getHostAddress(), this.defaultstats, this.streamSynch);
                        try {
                            if (this.dataaddress != null) {
                                rtprawreceiver = new RTPRawReceiver(this.dataport, inetaddress.getHostAddress(), this.defaultstats);
                            }
                            if (!(this.dataaddress == null || rtprawreceiver != null || rtcprawreceiver == null)) {
                                logger.warning("could not create RTCP/RTP raw receivers");
                                rtcprawreceiver.closeSource();
                            }
                            this.rtpDemultiplexer = new RTPDemultiplexer(this.cache, rtprawreceiver, this.streamSynch);
                            this.rtcpForwarder = new PacketForwarder(rtcprawreceiver, new RTCPReceiver(this.cache));
                            if (rtprawreceiver != null) {
                                this.rtpForwarder = new PacketForwarder(rtprawreceiver, new RTPReceiver(this.cache, this.rtpDemultiplexer));
                            }
                            this.rtcpForwarder.startPF("RTCP Forwarder for address" + this.controladdress.toString() + "port " + this.controlport);
                            if (this.rtpForwarder != null) {
                                this.rtpForwarder.startPF("RTP Forwarder for address " + this.dataaddress.toString() + "port " + this.dataport);
                            }
                            this.cleaner = new SSRCCacheCleaner(this.cache, this.streamSynch);
                            if (!(this.nonparticipating || this.unicast || this.cache.ourssrc == null)) {
                                this.cache.ourssrc.reporter = startParticipating(this.controlport, this.dataaddress.getHostAddress(), this.cache.ourssrc);
                            }
                            this.started = true;
                            return 0;
                        } catch (SocketException e) {
                            socketexception1 = e;
                            try {
                                throw new IOException(socketexception1.getMessage());
                            } catch (Throwable th2) {
                                th = th2;
                                logger.warning("could not create RTCP/RTP raw receivers");
                                rtcprawreceiver.closeSource();
                                throw th;
                            }
                        }
                    } catch (SocketException e2) {
                        socketexception1 = e2;
                        rtcprawreceiver = rtcprawreceiver2;
                        throw new IOException(socketexception1.getMessage());
                    } catch (Throwable th3) {
                        th = th3;
                        rtcprawreceiver = rtcprawreceiver2;
                        if (!(this.dataaddress == null || rtprawreceiver != null || rtcprawreceiver == null)) {
                            logger.warning("could not create RTCP/RTP raw receivers");
                            rtcprawreceiver.closeSource();
                        }
                        throw th;
                    }
                } catch (SocketException e3) {
                    socketexception = e3;
                    rtcprawreceiver = rtcprawreceiver2;
                    try {
                        throw new IOException(socketexception.getMessage());
                    } catch (Throwable th4) {
                        th = th4;
                        logger.warning("could not create RTCP/RTP raw receivers");
                        rtcprawreceiver.closeSource();
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    rtcprawreceiver = rtcprawreceiver2;
                    if (!(this.dataaddress == null || null != null || rtcprawreceiver == null)) {
                        logger.warning("could not create RTCP/RTP raw receivers");
                        rtcprawreceiver.closeSource();
                    }
                    throw th;
                }
            } catch (SocketException e4) {
                socketexception = e4;
                throw new IOException(socketexception.getMessage());
            }
        } catch (Throwable throwable1) {
            logger.log(Level.WARNING, "InitSession  RTPSessionMgr : " + throwable1.getMessage(), throwable1);
            return -1;
        }
    }

    public int startSession(SessionAddress sessionaddress, SessionAddress sessionaddress1, SessionAddress sessionaddress2, EncryptionInfo encryptioninfo) throws IOException, InvalidSessionAddressException {
        SocketException socketexception;
        Throwable th;
        if (this.started) {
            return -1;
        }
        this.localSenderAddress = sessionaddress1;
        this.cache.sessionbandwidth = 384000;
        this.controlport = sessionaddress.getControlPort();
        this.dataport = sessionaddress.getDataPort();
        CheckRTPPorts(this.dataport, this.controlport);
        this.dataaddress = sessionaddress.getDataAddress();
        this.controladdress = sessionaddress.getControlAddress();
        if (this.dataaddress.isMulticastAddress() || this.controladdress.isMulticastAddress() || isBroadcast(this.dataaddress) || isBroadcast(this.controladdress)) {
            throw new InvalidSessionAddressException("Local Address must be UNICAST IP addresses");
        }
        CheckRTPAddress(this.dataaddress, this.controladdress);
        RTCPRawReceiver rtcprawreceiver = null;
        RTPRawReceiver rtprawreceiver = null;
        try {
            InetAddress inetaddress = InetAddress.getLocalHost();
            try {
                RTCPRawReceiver rtcprawreceiver2 = new RTCPRawReceiver(this.controlport, this.controladdress.getHostAddress(), this.defaultstats, this.streamSynch);
                try {
                    if (this.dataaddress != null) {
                        rtprawreceiver = new RTPRawReceiver(this.dataport, this.dataaddress.getHostAddress(), this.defaultstats);
                    }
                    if (!(this.dataaddress == null || rtprawreceiver != null || rtcprawreceiver2 == null)) {
                        logger.warning("could not create RTCP/RTP raw receivers");
                        rtcprawreceiver2.closeSource();
                    }
                    this.rtpDemultiplexer = new RTPDemultiplexer(this.cache, rtprawreceiver, this.streamSynch);
                    this.rtcpForwarder = new PacketForwarder(rtcprawreceiver2, new RTCPReceiver(this.cache));
                    if (rtprawreceiver != null) {
                        this.rtpForwarder = new PacketForwarder(rtprawreceiver, new RTPReceiver(this.cache, this.rtpDemultiplexer));
                    }
                    this.rtcpForwarder.startPF("RTCP Forwarder for address" + this.controladdress.toString() + "port " + this.controlport);
                    if (this.rtpForwarder != null) {
                        this.rtpForwarder.startPF("RTP Forwarder for address " + this.dataaddress.toString() + "port " + this.dataport);
                    }
                    this.controlport = sessionaddress2.getControlPort();
                    this.dataport = sessionaddress2.getDataPort();
                    CheckRTPPorts(this.dataport, this.controlport);
                    this.dataaddress = sessionaddress2.getDataAddress();
                    this.controladdress = sessionaddress2.getControlAddress();
                    if (this.dataaddress.isMulticastAddress() || this.controladdress.isMulticastAddress() || isBroadcast(this.dataaddress) || isBroadcast(this.controladdress)) {
                        throw new InvalidSessionAddressException("Remote Address must be UNICAST IP addresses");
                    }
                    CheckRTPAddress(this.dataaddress, this.controladdress);
                    this.cleaner = new SSRCCacheCleaner(this.cache, this.streamSynch);
                    if (!(this.nonparticipating || this.unicast || this.cache.ourssrc == null)) {
                        this.cache.ourssrc.reporter = startParticipating(sessionaddress, sessionaddress1, this.cache.ourssrc, rtcprawreceiver2.socket);
                    }
                    this.started = true;
                    return 0;
                } catch (SocketException e) {
                    socketexception = e;
                    rtcprawreceiver = rtcprawreceiver2;
                    try {
                        throw new IOException(socketexception.getMessage());
                    } catch (Throwable th2) {
                        th = th2;
                        if (!(this.dataaddress == null || null != null || rtcprawreceiver == null)) {
                            logger.warning("could not create RTCP/RTP raw receivers");
                            rtcprawreceiver.closeSource();
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    rtcprawreceiver = rtcprawreceiver2;
                    logger.warning("could not create RTCP/RTP raw receivers");
                    rtcprawreceiver.closeSource();
                    throw th;
                }
            } catch (SocketException e2) {
                socketexception = e2;
                throw new IOException(socketexception.getMessage());
            }
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "InitSession : UnknownHostExcpetion " + throwable.getMessage(), throwable);
            return -1;
        }
    }

    private synchronized void stopParticipating(String s, SSRCInfo ssrcinfo) {
        if (ssrcinfo.reporter != null) {
            ssrcinfo.reporter.close(s);
            ssrcinfo.reporter = null;
        }
    }

    public String toString() {
        if (!this.newRtpInterface) {
            return "RTPSession Manager  \n\tSSRCCache  " + this.cache + "\n\tDataport  " + this.dataport + "\n\tControlport  " + this.controlport + "\n\tAddress  " + this.dataaddress + "\n\tRTPForwarder  " + this.rtpForwarder + "\n\tRTPDEmux  " + this.rtpDemultiplexer;
        }
        int i = 0;
        int j = 0;
        String s1 = "";
        if (this.localAddress != null) {
            i = this.localAddress.getControlPort();
            j = this.localAddress.getDataPort();
            s1 = this.localAddress.getDataHostAddress();
        }
        return "RTPManager \n\tSSRCCache  " + this.cache + "\n\tDataport  " + j + "\n\tControlport  " + i + "\n\tAddress  " + s1 + "\n\tRTPForwarder  " + this.rtpForwarder + "\n\tRTPDemux  " + this.rtpDemultiplexer;
    }

    public void UpdateEncodings(javax.media.protocol.DataSource datasource) {
        RTPControlImpl rtpcontrolimpl = (RTPControlImpl) datasource.getControl(RTPControl.class.getName());
        if (rtpcontrolimpl != null && rtpcontrolimpl.codeclist != null) {
            Enumeration enumeration = rtpcontrolimpl.codeclist.keys();
            while (enumeration.hasMoreElements()) {
                Integer integer = (Integer) enumeration.nextElement();
                this.formatinfo.add(integer.intValue(), (Format) rtpcontrolimpl.codeclist.get(integer));
            }
        }
    }

    private boolean Win32() {
        return System.getProperty("os.name").startsWith("Windows");
    }
}
