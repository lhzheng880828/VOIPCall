package net.java.sip.communicator.impl.packetlogging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.java.sip.communicator.util.Logger;
import org.dhcp4java.DHCPConstants;
import org.jitsi.service.fileaccess.FileCategory;
import org.jitsi.service.packetlogging.PacketLoggingConfiguration;
import org.jitsi.service.packetlogging.PacketLoggingService;
import org.jitsi.service.packetlogging.PacketLoggingService.ProtocolName;
import org.jitsi.service.packetlogging.PacketLoggingService.TransportName;
import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager;

public class PacketLoggingServiceImpl implements PacketLoggingService {
    private static final byte[] fakeEthernetHeader = new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 8, (byte) 0};
    private static final byte[] ip6HeaderTemplate = new byte[]{(byte) 96, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, DHCPConstants.DHO_ROOT_PATH, DHCPConstants.DHO_NISPLUS_DOMAIN, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
    private static final byte[] ipHeaderTemplate = new byte[]{DHCPConstants.DHO_SMTP_SERVER, (byte) 0, (byte) 3, DHCPConstants.DHO_WWW_SERVER, (byte) -55, DHCPConstants.DHO_NON_LOCAL_SOURCE_ROUTING, (byte) 0, (byte) 0, DHCPConstants.DHO_DHCP_MESSAGE_TYPE, DHCPConstants.DHO_ROOT_PATH, (byte) 0, (byte) 0, (byte) -43, (byte) -64, DHCPConstants.DHO_DHCP_REBINDING_TIME, DHCPConstants.DHO_STREETTALK_SERVER, (byte) -64, (byte) -88, (byte) 0, DHCPConstants.DHO_DHCP_OPTION_OVERLOAD};
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(PacketLoggingServiceImpl.class);
    private static final byte[] tcpHeaderTemplate = new byte[]{(byte) -73, (byte) 97, DHCPConstants.DHO_IP_FORWARDING, (byte) -60, (byte) 79, DHCPConstants.DHO_ROUTER_SOLICITATION_ADDRESS, DHCPConstants.DHO_DHCP_PARAMETER_REQUEST_LIST, DHCPConstants.DHO_DHCP_REBINDING_TIME, DHCPConstants.DHO_ROOT_PATH, DHCPConstants.DHO_PERFORM_MASK_DISCOVERY, (byte) -68, (byte) 84, Byte.MIN_VALUE, (byte) 24, (byte) 0, DHCPConstants.DHO_NETBIOS_NODE_TYPE, (byte) -84, (byte) 120, (byte) 0, (byte) 0, (byte) 1, (byte) 1, (byte) 8, (byte) 10, (byte) 0, (byte) 6, (byte) -44, DHCPConstants.DHO_WWW_SERVER, (byte) 110, (byte) -52, DHCPConstants.DHO_SUBNET_SELECTION, (byte) -67};
    private static final byte[] udpHeaderTemplate = new byte[]{DHCPConstants.DHO_IP_FORWARDING, (byte) -60, DHCPConstants.DHO_IP_FORWARDING, (byte) -60, (byte) 3, DHCPConstants.DHO_DHCP_OPTION_OVERLOAD, (byte) 0, (byte) 0};
    private long dstCount = 1;
    private File[] files;
    private FileOutputStream outputStream = null;
    private PacketLoggingConfiguration packetLoggingConfiguration = null;
    private SaverThread saverThread = new SaverThread(this, null);
    private long srcCount = 1;
    private Object tcpCounterLock = new Object();
    private long written = 0;

    /* renamed from: net.java.sip.communicator.impl.packetlogging.PacketLoggingServiceImpl$1 */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$org$jitsi$service$packetlogging$PacketLoggingService$ProtocolName = new int[ProtocolName.values().length];

        static {
            try {
                $SwitchMap$org$jitsi$service$packetlogging$PacketLoggingService$ProtocolName[ProtocolName.SIP.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$org$jitsi$service$packetlogging$PacketLoggingService$ProtocolName[ProtocolName.JABBER.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$org$jitsi$service$packetlogging$PacketLoggingService$ProtocolName[ProtocolName.RTP.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$org$jitsi$service$packetlogging$PacketLoggingService$ProtocolName[ProtocolName.ICE4J.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    private static class Packet {
        byte[] destinationAddress;
        int destinationPort;
        byte[] packetContent;
        int packetLength;
        int packetOffset;
        ProtocolName protocol;
        boolean sender;
        byte[] sourceAddress;
        int sourcePort;
        TransportName transport;

        /* synthetic */ Packet(ProtocolName x0, byte[] x1, int x2, byte[] x3, int x4, TransportName x5, boolean x6, byte[] x7, int x8, int x9, AnonymousClass1 x10) {
            this(x0, x1, x2, x3, x4, x5, x6, x7, x8, x9);
        }

        private Packet(ProtocolName protocol, byte[] sourceAddress, int sourcePort, byte[] destinationAddress, int destinationPort, TransportName transport, boolean sender, byte[] packetContent, int packetOffset, int packetLength) {
            this.protocol = protocol;
            this.sourceAddress = sourceAddress;
            this.sourcePort = sourcePort;
            this.destinationAddress = destinationAddress;
            this.destinationPort = destinationPort;
            this.transport = transport;
            this.sender = sender;
            this.packetContent = packetContent;
            this.packetOffset = packetOffset;
            this.packetLength = packetLength;
        }
    }

    private class SaverThread extends Thread {
        private final List<Packet> pktsToSave;
        private boolean stopped;

        private SaverThread() {
            this.stopped = true;
            this.pktsToSave = new ArrayList();
        }

        /* synthetic */ SaverThread(PacketLoggingServiceImpl x0, AnonymousClass1 x1) {
            this();
        }

        public void run() {
            this.stopped = false;
            while (!this.stopped) {
                synchronized (this) {
                    if (this.pktsToSave.isEmpty()) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                        }
                    } else {
                        Packet pktToSave = (Packet) this.pktsToSave.remove(0);
                        if (pktToSave != null) {
                            try {
                                PacketLoggingServiceImpl.this.savePacket(pktToSave);
                            } catch (Throwable t) {
                                if (t instanceof ThreadDeath) {
                                    ThreadDeath t2 = (ThreadDeath) t;
                                } else {
                                    PacketLoggingServiceImpl.logger.error("Error writing packet to file", t);
                                }
                            }
                        } else {
                            continue;
                        }
                    }
                }
            }
        }

        public synchronized void stopRunning() {
            this.stopped = true;
            notifyAll();
        }

        public synchronized void queuePacket(Packet packet) {
            this.pktsToSave.add(packet);
            notifyAll();
        }
    }

    public void start() {
        this.saverThread.start();
    }

    private void getFileNames() throws Exception {
        int fileCount = getConfiguration().getLogfileCount();
        this.files = new File[fileCount];
        for (int i = 0; i < fileCount; i++) {
            this.files[i] = PacketLoggingActivator.getFileAccessService().getPrivatePersistentFile(new File("log", "jitsi" + i + ".pcap").toString(), FileCategory.LOG);
        }
    }

    private void rotateFiles() throws IOException {
        if (this.outputStream != null) {
            this.outputStream.flush();
            this.outputStream.close();
        }
        for (int i = getConfiguration().getLogfileCount() - 2; i >= 0; i--) {
            File f1 = this.files[i];
            File f2 = this.files[i + 1];
            if (f1.exists()) {
                if (f2.exists()) {
                    f2.delete();
                }
                f1.renameTo(f2);
            }
        }
        this.outputStream = new FileOutputStream(this.files[0]);
        this.written = 0;
        createGlobalHeader();
    }

    public void stop() {
        this.saverThread.stopRunning();
        if (this.outputStream != null) {
            try {
                this.outputStream.flush();
                this.outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                this.outputStream = null;
            }
        }
    }

    private void createGlobalHeader() throws IOException {
        this.outputStream.write(212);
        this.outputStream.write(195);
        this.outputStream.write(178);
        this.outputStream.write(161);
        this.outputStream.write(2);
        this.outputStream.write(0);
        this.outputStream.write(4);
        this.outputStream.write(0);
        this.outputStream.write(0);
        this.outputStream.write(0);
        this.outputStream.write(0);
        this.outputStream.write(0);
        this.outputStream.write(0);
        this.outputStream.write(0);
        this.outputStream.write(0);
        this.outputStream.write(0);
        this.outputStream.write(255);
        this.outputStream.write(255);
        this.outputStream.write(0);
        this.outputStream.write(0);
        this.outputStream.write(1);
        this.outputStream.write(0);
        this.outputStream.write(0);
        this.outputStream.write(0);
    }

    public boolean isLoggingEnabled() {
        return getConfiguration().isGlobalLoggingEnabled();
    }

    public boolean isLoggingEnabled(ProtocolName protocol) {
        PacketLoggingConfiguration cfg = getConfiguration();
        if (!cfg.isGlobalLoggingEnabled()) {
            return false;
        }
        switch (AnonymousClass1.$SwitchMap$org$jitsi$service$packetlogging$PacketLoggingService$ProtocolName[protocol.ordinal()]) {
            case 1:
                return cfg.isSipLoggingEnabled();
            case 2:
                return cfg.isJabberLoggingEnabled();
            case 3:
                return cfg.isRTPLoggingEnabled();
            case 4:
                return cfg.isIce4JLoggingEnabled();
            default:
                return false;
        }
    }

    public void logPacket(ProtocolName protocol, byte[] sourceAddress, int sourcePort, byte[] destinationAddress, int destinationPort, TransportName transport, boolean sender, byte[] packetContent) {
        logPacket(protocol, sourceAddress, sourcePort, destinationAddress, destinationPort, transport, sender, packetContent, 0, packetContent.length);
    }

    public void logPacket(ProtocolName protocol, byte[] sourceAddress, int sourcePort, byte[] destinationAddress, int destinationPort, TransportName transport, boolean sender, byte[] packetContent, int packetOffset, int packetLength) {
        this.saverThread.queuePacket(new Packet(protocol, sourceAddress, sourcePort, destinationAddress, destinationPort, transport, sender, packetContent, packetOffset, packetLength, null));
    }

    public PacketLoggingConfiguration getConfiguration() {
        if (this.packetLoggingConfiguration == null) {
            this.packetLoggingConfiguration = new PacketLoggingConfigurationImpl();
        }
        return this.packetLoggingConfiguration;
    }

    /* access modifiers changed from: private */
    public void savePacket(Packet packet) throws Exception {
        byte[] ipHeader;
        byte[] transportHeader;
        short len;
        boolean isIPv4 = packet.sourceAddress.length == 4 || packet.destinationAddress.length == 4;
        if (isIPv4) {
            ipHeader = new byte[ipHeaderTemplate.length];
            System.arraycopy(ipHeaderTemplate, 0, ipHeader, 0, ipHeader.length);
            System.arraycopy(packet.sourceAddress, 0, ipHeader, 12, 4);
            System.arraycopy(packet.destinationAddress, 0, ipHeader, 16, 4);
        } else {
            ipHeader = new byte[ip6HeaderTemplate.length];
            System.arraycopy(ip6HeaderTemplate, 0, ipHeader, 0, ipHeader.length);
            System.arraycopy(packet.sourceAddress, 0, ipHeader, 8, 16);
            System.arraycopy(packet.destinationAddress, 0, ipHeader, 24, 16);
        }
        if (packet.transport == TransportName.UDP) {
            Object udpHeader = new byte[udpHeaderTemplate.length];
            transportHeader = udpHeader;
            System.arraycopy(udpHeaderTemplate, 0, udpHeader, 0, udpHeader.length);
            writeShort(packet.sourcePort, udpHeader, 0);
            writeShort(packet.destinationPort, udpHeader, 2);
            len = (short) (packet.packetLength + udpHeader.length);
            writeShort(len, udpHeader, 4);
        } else {
            Object transportHeader2 = new byte[tcpHeaderTemplate.length];
            System.arraycopy(tcpHeaderTemplate, 0, transportHeader2, 0, transportHeader2.length);
            writeShort(packet.sourcePort, transportHeader2, 0);
            writeShort(packet.destinationPort, transportHeader2, 2);
            len = (short) (packet.packetLength + transportHeader2.length);
            long seqnum;
            long acknum;
            if (packet.sender) {
                synchronized (this.tcpCounterLock) {
                    seqnum = this.srcCount;
                    this.srcCount += (long) packet.packetLength;
                    acknum = this.dstCount;
                }
                intToBytes((int) (-1 & seqnum), transportHeader2, 4);
                intToBytes((int) (-1 & acknum), transportHeader2, 8);
            } else {
                synchronized (this.tcpCounterLock) {
                    seqnum = this.dstCount;
                    this.dstCount += (long) packet.packetLength;
                    acknum = this.srcCount;
                }
                intToBytes((int) (-1 & seqnum), transportHeader2, 4);
                intToBytes((int) (-1 & acknum), transportHeader2, 8);
            }
        }
        if (isIPv4) {
            writeShort((short) (ipHeader.length + len), ipHeader, 2);
            if (packet.transport == TransportName.UDP) {
                ipHeader[9] = DHCPConstants.DHO_ROOT_PATH;
            } else {
                ipHeader[9] = (byte) 6;
            }
            int chk2 = computeChecksum(ipHeader);
            ipHeader[10] = (byte) (chk2 >> 8);
            ipHeader[11] = (byte) (chk2 & 255);
        } else {
            writeShort(len, ipHeader, 4);
            if (packet.transport == TransportName.UDP) {
                ipHeader[6] = DHCPConstants.DHO_ROOT_PATH;
            } else {
                ipHeader[6] = (byte) 6;
            }
        }
        long current = System.currentTimeMillis();
        int tsSec = (int) (current / 1000);
        int tsUsec = (int) ((current % 1000) * 1000);
        int inclLen = packet.packetLength + ((fakeEthernetHeader.length + ipHeader.length) + transportHeader2.length);
        int origLen = inclLen;
        synchronized (this) {
            if (this.outputStream == null) {
                getFileNames();
                rotateFiles();
            }
            long limit = getConfiguration().getLimit();
            if (limit > 0 && this.written > limit) {
                rotateFiles();
            }
            addInt(tsSec);
            addInt(tsUsec);
            addInt(inclLen);
            addInt(origLen);
            this.outputStream.write(fakeEthernetHeader);
            this.outputStream.write(ipHeader);
            this.outputStream.write(transportHeader2);
            this.outputStream.write(packet.packetContent, packet.packetOffset, packet.packetLength);
            this.outputStream.flush();
            this.written += (long) (inclLen + 16);
        }
    }

    private void addInt(int d) throws IOException {
        this.outputStream.write(d & 255);
        this.outputStream.write((65280 & d) >> 8);
        this.outputStream.write((16711680 & d) >> 16);
        this.outputStream.write((-16777216 & d) >> 24);
    }

    private static final void intToBytes(int address, byte[] data, int offset) {
        data[offset] = (byte) ((address >>> 24) & 255);
        data[offset + 1] = (byte) ((address >>> 16) & 255);
        data[offset + 2] = (byte) ((address >>> 8) & 255);
        data[offset + 3] = (byte) (address & 255);
    }

    private static void writeShort(int value, byte[] data, int offset) {
        data[offset] = (byte) (value >> 8);
        data[offset + 1] = (byte) value;
    }

    private int computeChecksum(byte[] data) {
        int total = 0;
        int i = 0;
        while (i < data.length - (data.length % 2)) {
            int i2 = i + 1;
            i = i2 + 1;
            total += ((data[i] & 255) << 8) | (data[i2] & 255);
        }
        if (i < data.length) {
            total += (data[i] & 255) << 8;
        }
        while ((-65536 & total) != 0) {
            total = (total & InBandBytestreamManager.MAXIMUM_BLOCK_SIZE) + (total >>> 16);
        }
        return (total ^ -1) & InBandBytestreamManager.MAXIMUM_BLOCK_SIZE;
    }
}
