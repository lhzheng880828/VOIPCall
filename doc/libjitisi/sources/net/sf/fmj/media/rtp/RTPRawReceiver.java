package net.sf.fmj.media.rtp;

import com.lti.utils.UnsignedUtils;
import com.sun.media.format.WavAudioFormat;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import javax.media.rtp.RTPConnector;
import javax.media.rtp.RTPPushDataSource;
import javax.media.rtp.SessionAddress;
import net.sf.fmj.media.Log;
import net.sf.fmj.media.rtp.util.BadFormatException;
import net.sf.fmj.media.rtp.util.Packet;
import net.sf.fmj.media.rtp.util.PacketFilter;
import net.sf.fmj.media.rtp.util.RTPPacket;
import net.sf.fmj.media.rtp.util.RTPPacketReceiver;
import net.sf.fmj.media.rtp.util.UDPPacketReceiver;

public class RTPRawReceiver extends PacketFilter {
    private boolean recvBufSizeSet = false;
    private RTPConnector rtpConnector = null;
    public DatagramSocket socket;
    private OverallStats stats = null;

    public RTPRawReceiver(DatagramSocket datagramsocket, OverallStats overallstats) {
        setSource(new UDPPacketReceiver(datagramsocket, 2000));
        this.stats = overallstats;
    }

    public RTPRawReceiver(int i, String s, OverallStats overallstats) throws UnknownHostException, IOException, SocketException {
        UDPPacketReceiver udppacketreceiver = new UDPPacketReceiver(i & -2, s, -1, null, 2000, null);
        setSource(udppacketreceiver);
        this.socket = udppacketreceiver.getSocket();
        this.stats = overallstats;
    }

    public RTPRawReceiver(RTPConnector rtpconnector, OverallStats overallstats) {
        try {
            setSource(new RTPPacketReceiver(rtpconnector.getDataInputStream()));
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
        }
        this.rtpConnector = rtpconnector;
        this.stats = overallstats;
    }

    public RTPRawReceiver(RTPPushDataSource rtppushdatasource, OverallStats overallstats) {
        setSource(new RTPPacketReceiver(rtppushdatasource));
        this.stats = overallstats;
    }

    public RTPRawReceiver(SessionAddress sessionaddress, SessionAddress sessionaddress1, OverallStats overallstats, DatagramSocket datagramsocket) throws UnknownHostException, IOException, SocketException {
        this.stats = overallstats;
        UDPPacketReceiver udppacketreceiver = new UDPPacketReceiver(sessionaddress.getDataPort(), sessionaddress.getDataHostAddress(), sessionaddress1.getDataPort(), sessionaddress1.getDataHostAddress(), 2000, datagramsocket);
        setSource(udppacketreceiver);
        this.socket = udppacketreceiver.getSocket();
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
        return "RTP Raw Packet Receiver";
    }

    public int getRecvBufSize() {
        try {
            return ((Integer) this.socket.getClass().getMethod("getReceiveBufferSize", new Class[0]).invoke(this.socket, new Object[0])).intValue();
        } catch (Exception e) {
            if (this.rtpConnector != null) {
                return this.rtpConnector.getReceiveBufferSize();
            }
            return -1;
        }
    }

    public Packet handlePacket(Packet packet) {
        this.stats.update(0, 1);
        this.stats.update(1, packet.length);
        try {
            RTPPacket rtppacket = parse(packet);
            if (this.recvBufSizeSet) {
                return rtppacket;
            }
            this.recvBufSizeSet = true;
            switch (rtppacket.payloadType) {
                case 14:
                case 26:
                case WavAudioFormat.WAVE_FORMAT_DSPGROUP_TRUESPEECH /*34*/:
                case 42:
                    setRecvBufSize(64000);
                    return rtppacket;
                case 31:
                    setRecvBufSize(128000);
                    return rtppacket;
                case 32:
                    setRecvBufSize(128000);
                    return rtppacket;
                default:
                    if (rtppacket.payloadType < 96 || rtppacket.payloadType > 127) {
                        return rtppacket;
                    }
                    setRecvBufSize(64000);
                    return rtppacket;
            }
        } catch (BadFormatException e) {
            this.stats.update(2, 1);
            return null;
        }
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

    public RTPPacket parse(Packet packet) throws BadFormatException {
        RTPPacket rtppacket = new RTPPacket(packet);
        DataInputStream datainputstream = new DataInputStream(new ByteArrayInputStream(rtppacket.data, rtppacket.offset, rtppacket.length));
        try {
            int i = datainputstream.readUnsignedByte();
            if ((i & 192) != 128) {
                throw new BadFormatException();
            }
            if ((i & 16) != 0) {
                rtppacket.extensionPresent = true;
            }
            int j = 0;
            if ((i & 32) != 0) {
                j = rtppacket.data[(rtppacket.offset + rtppacket.length) - 1] & UnsignedUtils.MAX_UBYTE;
            }
            i &= 15;
            rtppacket.payloadType = datainputstream.readUnsignedByte();
            rtppacket.marker = rtppacket.payloadType >> 7;
            rtppacket.payloadType &= 127;
            rtppacket.seqnum = datainputstream.readUnsignedShort();
            rtppacket.timestamp = ((long) datainputstream.readInt()) & 4294967295L;
            rtppacket.ssrc = datainputstream.readInt();
            rtppacket.csrc = new int[i];
            for (int i1 = 0; i1 < rtppacket.csrc.length; i1++) {
                rtppacket.csrc[i1] = datainputstream.readInt();
            }
            int k = 0 + ((rtppacket.csrc.length << 2) + 12);
            if (rtppacket.extensionPresent) {
                rtppacket.extensionType = datainputstream.readUnsignedShort();
                int l = datainputstream.readUnsignedShort() << 2;
                rtppacket.extension = new byte[l];
                datainputstream.readFully(rtppacket.extension);
                k += l + 4;
            }
            rtppacket.payloadlength = rtppacket.length - (k + j);
            if (rtppacket.payloadlength < 1) {
                throw new BadFormatException();
            }
            rtppacket.payloadoffset = rtppacket.offset + k;
            return rtppacket;
        } catch (EOFException e) {
            throw new BadFormatException("Unexpected end of RTP packet");
        } catch (IOException e2) {
            throw new IllegalArgumentException("Impossible Exception");
        }
    }

    public void setRecvBufSize(int i) {
        try {
            if (this.socket == null && this.rtpConnector != null) {
                this.rtpConnector.setReceiveBufferSize(i);
            }
        } catch (Exception exception) {
            Log.comment("Cannot set receive buffer size: " + exception);
        }
    }
}
