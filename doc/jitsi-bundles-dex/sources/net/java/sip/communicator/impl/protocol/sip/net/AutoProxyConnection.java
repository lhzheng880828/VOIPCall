package net.java.sip.communicator.impl.protocol.sip.net;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import net.java.sip.communicator.impl.protocol.sip.SipAccountIDImpl;
import net.java.sip.communicator.service.dns.DnssecException;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.NetworkUtils;
import net.java.sip.communicator.util.SRVRecord;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.javax.sip.ListeningPoint;

public class AutoProxyConnection extends ProxyConnection {
    private static final Logger logger = Logger.getLogger(AutoProxyConnection.class);
    private static final String[] transports = new String[]{ListeningPoint.TLS, ListeningPoint.TCP, ListeningPoint.UDP};
    private String address;
    private final String defaultTransport;
    private boolean hadSrvResults;
    private int naptrIndex;
    private String[][] naptrRecords;
    private LocalNetworkUtils nu = new LocalNetworkUtils();
    private int socketAddressIndex;
    private InetSocketAddress[] socketAddresses;
    private SRVRecord[] srvRecords;
    private int srvRecordsIndex;
    private int srvTransportIndex;
    private State state;

    protected static class LocalNetworkUtils {
        protected LocalNetworkUtils() {
        }

        public InetAddress getInetAddress(String address) throws UnknownHostException {
            return NetworkUtils.getInetAddress(address);
        }

        public String[][] getNAPTRRecords(String address) throws ParseException, DnssecException {
            return NetworkUtils.getNAPTRRecords(address);
        }

        public SRVRecord[] getSRVRecords(String service, String proto, String address) throws ParseException, DnssecException {
            return NetworkUtils.getSRVRecords(service, proto, address);
        }

        public InetSocketAddress[] getAandAAAARecords(String target, int port) throws ParseException, DnssecException {
            return NetworkUtils.getAandAAAARecords(target, port);
        }

        public boolean isValidIPAddress(String address) {
            return NetworkUtils.isValidIPAddress(address);
        }

        public SRVRecord[] getSRVRecords(String domain) throws ParseException, DnssecException {
            return NetworkUtils.getSRVRecords(domain);
        }
    }

    private enum State {
        New,
        Naptr,
        NaptrSrv,
        NaptrSrvHosts,
        NaptrSrvHostIPs,
        Srv,
        SrvHosts,
        SrvHostIPs,
        Hosts,
        IP
    }

    public AutoProxyConnection(SipAccountIDImpl account, String defaultTransport) {
        super(account);
        this.defaultTransport = defaultTransport;
        reset();
    }

    public AutoProxyConnection(SipAccountIDImpl account, String address, String defaultTransport) {
        super(account);
        this.defaultTransport = defaultTransport;
        reset();
        this.address = address;
        if (this.nu.isValidIPAddress(this.address)) {
            this.state = State.IP;
        }
    }

    /* access modifiers changed from: protected */
    public void setNetworkUtils(LocalNetworkUtils nu) {
        this.nu = nu;
    }

    /* access modifiers changed from: protected */
    public boolean getNextAddressFromDns() throws DnssecException {
        try {
            return getNextAddressInternal();
        } catch (ParseException ex) {
            logger.error("Unable to get DNS data for <" + this.address + "> in state" + this.state, ex);
            return false;
        }
    }

    /* JADX WARNING: Missing block: B:23:0x008d, code skipped:
            r8.naptrIndex++;
     */
    /* JADX WARNING: Missing block: B:25:0x0098, code skipped:
            if (r8.naptrIndex >= r8.naptrRecords.length) goto L_0x00f7;
     */
    /* JADX WARNING: Missing block: B:26:0x009a, code skipped:
            r8.srvRecords = r8.nu.getSRVRecords(r8.naptrRecords[r8.naptrIndex][2]);
     */
    /* JADX WARNING: Missing block: B:27:0x00ad, code skipped:
            if (r8.srvRecords == null) goto L_0x008d;
     */
    /* JADX WARNING: Missing block: B:29:0x00b2, code skipped:
            if (r8.srvRecords.length <= 0) goto L_0x008d;
     */
    /* JADX WARNING: Missing block: B:30:0x00b4, code skipped:
            r8.state = net.java.sip.communicator.impl.protocol.sip.net.AutoProxyConnection.State.NaptrSrvHosts;
     */
    /* JADX WARNING: Missing block: B:31:0x00c6, code skipped:
            if (org.jitsi.javax.sip.ListeningPoint.TLS.equalsIgnoreCase(r8.naptrRecords[r8.naptrIndex][1]) == false) goto L_0x00dd;
     */
    /* JADX WARNING: Missing block: B:32:0x00c8, code skipped:
            r8.transport = org.jitsi.javax.sip.ListeningPoint.TLS;
     */
    /* JADX WARNING: Missing block: B:33:0x00cc, code skipped:
            r8.srvRecordsIndex = 0;
     */
    /* JADX WARNING: Missing block: B:34:0x00d2, code skipped:
            if (getNextAddressFromDns() == false) goto L_0x008d;
     */
    /* JADX WARNING: Missing block: B:35:0x00d4, code skipped:
            r8.naptrIndex++;
     */
    /* JADX WARNING: Missing block: B:37:0x00eb, code skipped:
            if (org.jitsi.javax.sip.ListeningPoint.TCP.equalsIgnoreCase(r8.naptrRecords[r8.naptrIndex][1]) == false) goto L_0x00f2;
     */
    /* JADX WARNING: Missing block: B:38:0x00ed, code skipped:
            r8.transport = org.jitsi.javax.sip.ListeningPoint.TCP;
     */
    /* JADX WARNING: Missing block: B:39:0x00f2, code skipped:
            r8.transport = org.jitsi.javax.sip.ListeningPoint.UDP;
     */
    /* JADX WARNING: Missing block: B:41:0x00fa, code skipped:
            r8.srvRecordsIndex++;
     */
    /* JADX WARNING: Missing block: B:43:0x0105, code skipped:
            if (r8.srvRecordsIndex >= r8.srvRecords.length) goto L_0x0141;
     */
    /* JADX WARNING: Missing block: B:44:0x0107, code skipped:
            r8.socketAddresses = r8.nu.getAandAAAARecords(r8.srvRecords[r8.srvRecordsIndex].getTarget(), r8.srvRecords[r8.srvRecordsIndex].getPort());
     */
    /* JADX WARNING: Missing block: B:45:0x0125, code skipped:
            if (r8.socketAddresses == null) goto L_0x00fa;
     */
    /* JADX WARNING: Missing block: B:47:0x012a, code skipped:
            if (r8.socketAddresses.length <= 0) goto L_0x00fa;
     */
    /* JADX WARNING: Missing block: B:48:0x012c, code skipped:
            r8.state = net.java.sip.communicator.impl.protocol.sip.net.AutoProxyConnection.State.NaptrSrvHostIPs;
            r8.socketAddressIndex = 0;
     */
    /* JADX WARNING: Missing block: B:49:0x0136, code skipped:
            if (getNextAddressFromDns() == false) goto L_0x00fa;
     */
    /* JADX WARNING: Missing block: B:50:0x0138, code skipped:
            r8.srvRecordsIndex++;
     */
    /* JADX WARNING: Missing block: B:51:0x0141, code skipped:
            r8.state = net.java.sip.communicator.impl.protocol.sip.net.AutoProxyConnection.State.NaptrSrv;
     */
    /* JADX WARNING: Missing block: B:56:0x016d, code skipped:
            r8.srvTransportIndex++;
     */
    /* JADX WARNING: Missing block: B:58:0x0178, code skipped:
            if (r8.srvTransportIndex >= transports.length) goto L_0x01d2;
     */
    /* JADX WARNING: Missing block: B:59:0x017a, code skipped:
            r5 = r8.nu;
     */
    /* JADX WARNING: Missing block: B:60:0x0188, code skipped:
            if (org.jitsi.javax.sip.ListeningPoint.TLS.equals(transports[r8.srvTransportIndex]) == false) goto L_0x01cc;
     */
    /* JADX WARNING: Missing block: B:61:0x018a, code skipped:
            r1 = "sips";
     */
    /* JADX WARNING: Missing block: B:63:0x0198, code skipped:
            if (org.jitsi.javax.sip.ListeningPoint.UDP.equalsIgnoreCase(transports[r8.srvTransportIndex]) == false) goto L_0x01cf;
     */
    /* JADX WARNING: Missing block: B:64:0x019a, code skipped:
            r2 = org.jitsi.javax.sip.ListeningPoint.UDP;
     */
    /* JADX WARNING: Missing block: B:65:0x019c, code skipped:
            r8.srvRecords = r5.getSRVRecords(r1, r2, r8.address);
     */
    /* JADX WARNING: Missing block: B:66:0x01a6, code skipped:
            if (r8.srvRecords == null) goto L_0x016d;
     */
    /* JADX WARNING: Missing block: B:68:0x01ab, code skipped:
            if (r8.srvRecords.length <= 0) goto L_0x016d;
     */
    /* JADX WARNING: Missing block: B:69:0x01ad, code skipped:
            r8.hadSrvResults = true;
            r8.state = net.java.sip.communicator.impl.protocol.sip.net.AutoProxyConnection.State.SrvHosts;
            r8.srvRecordsIndex = 0;
            r8.transport = transports[r8.srvTransportIndex];
     */
    /* JADX WARNING: Missing block: B:70:0x01c1, code skipped:
            if (getNextAddressFromDns() == false) goto L_0x016d;
     */
    /* JADX WARNING: Missing block: B:71:0x01c3, code skipped:
            r8.srvTransportIndex++;
     */
    /* JADX WARNING: Missing block: B:72:0x01cc, code skipped:
            r1 = "sip";
     */
    /* JADX WARNING: Missing block: B:73:0x01cf, code skipped:
            r2 = org.jitsi.javax.sip.ListeningPoint.TCP;
     */
    /* JADX WARNING: Missing block: B:75:0x01d4, code skipped:
            if (r8.hadSrvResults != false) goto L_0x01e2;
     */
    /* JADX WARNING: Missing block: B:76:0x01d6, code skipped:
            r8.state = net.java.sip.communicator.impl.protocol.sip.net.AutoProxyConnection.State.Hosts;
            r8.socketAddressIndex = 0;
     */
    /* JADX WARNING: Missing block: B:133:?, code skipped:
            return true;
     */
    /* JADX WARNING: Missing block: B:134:?, code skipped:
            return false;
     */
    /* JADX WARNING: Missing block: B:135:?, code skipped:
            return true;
     */
    /* JADX WARNING: Missing block: B:136:?, code skipped:
            return getNextAddressFromDns();
     */
    /* JADX WARNING: Missing block: B:139:?, code skipped:
            return true;
     */
    /* JADX WARNING: Missing block: B:140:?, code skipped:
            return getNextAddressFromDns();
     */
    /* JADX WARNING: Missing block: B:141:?, code skipped:
            return false;
     */
    private boolean getNextAddressInternal() throws net.java.sip.communicator.service.dns.DnssecException, java.text.ParseException {
        /*
        r8 = this;
        r1 = 5060; // 0x13c4 float:7.09E-42 double:2.5E-320;
        r3 = 1;
        r4 = 0;
        r2 = net.java.sip.communicator.impl.protocol.sip.net.AutoProxyConnection.AnonymousClass1.$SwitchMap$net$java$sip$communicator$impl$protocol$sip$net$AutoProxyConnection$State;
        r5 = r8.state;
        r5 = r5.ordinal();
        r2 = r2[r5];
        switch(r2) {
            case 1: goto L_0x0013;
            case 2: goto L_0x001c;
            case 3: goto L_0x0066;
            case 4: goto L_0x0093;
            case 5: goto L_0x0100;
            case 6: goto L_0x014b;
            case 7: goto L_0x0173;
            case 8: goto L_0x01e5;
            case 9: goto L_0x0240;
            case 10: goto L_0x0262;
            default: goto L_0x0011;
        };
    L_0x0011:
        r1 = r4;
    L_0x0012:
        return r1;
    L_0x0013:
        r1 = net.java.sip.communicator.impl.protocol.sip.net.AutoProxyConnection.State.Naptr;
        r8.state = r1;
        r1 = r8.getNextAddressFromDns();
        goto L_0x0012;
    L_0x001c:
        r2 = r8.socketAddressIndex;
        if (r2 != 0) goto L_0x0064;
    L_0x0020:
        r2 = r8.socketAddressIndex;
        r2 = r2 + 1;
        r8.socketAddressIndex = r2;
        r2 = new java.net.InetSocketAddress;	 Catch:{ UnknownHostException -> 0x0047 }
        r5 = r8.nu;	 Catch:{ UnknownHostException -> 0x0047 }
        r6 = r8.address;	 Catch:{ UnknownHostException -> 0x0047 }
        r5 = r5.getInetAddress(r6);	 Catch:{ UnknownHostException -> 0x0047 }
        r6 = "TLS";
        r7 = r8.transport;	 Catch:{ UnknownHostException -> 0x0047 }
        r6 = r6.equalsIgnoreCase(r7);	 Catch:{ UnknownHostException -> 0x0047 }
        if (r6 == 0) goto L_0x003c;
    L_0x003a:
        r1 = 5061; // 0x13c5 float:7.092E-42 double:2.5005E-320;
    L_0x003c:
        r2.<init>(r5, r1);	 Catch:{ UnknownHostException -> 0x0047 }
        r8.socketAddress = r2;	 Catch:{ UnknownHostException -> 0x0047 }
        r1 = r8.defaultTransport;
        r8.transport = r1;
        r1 = r3;
        goto L_0x0012;
    L_0x0047:
        r0 = move-exception;
        r1 = logger;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "invalid IP address: ";
        r2 = r2.append(r3);
        r3 = r8.address;
        r2 = r2.append(r3);
        r2 = r2.toString();
        r1.error(r2, r0);
        r1 = r4;
        goto L_0x0012;
    L_0x0064:
        r1 = r4;
        goto L_0x0012;
    L_0x0066:
        r1 = r8.nu;
        r2 = r8.address;
        r1 = r1.getNAPTRRecords(r2);
        r8.naptrRecords = r1;
        r1 = r8.naptrRecords;
        if (r1 == 0) goto L_0x0084;
    L_0x0074:
        r1 = r8.naptrRecords;
        r1 = r1.length;
        if (r1 <= 0) goto L_0x0084;
    L_0x0079:
        r1 = net.java.sip.communicator.impl.protocol.sip.net.AutoProxyConnection.State.NaptrSrv;
        r8.state = r1;
        r8.naptrIndex = r4;
    L_0x007f:
        r1 = r8.getNextAddressFromDns();
        goto L_0x0012;
    L_0x0084:
        r8.hadSrvResults = r4;
        r1 = net.java.sip.communicator.impl.protocol.sip.net.AutoProxyConnection.State.Srv;
        r8.state = r1;
        r8.srvTransportIndex = r4;
        goto L_0x007f;
    L_0x008d:
        r1 = r8.naptrIndex;
        r1 = r1 + 1;
        r8.naptrIndex = r1;
    L_0x0093:
        r1 = r8.naptrIndex;
        r2 = r8.naptrRecords;
        r2 = r2.length;
        if (r1 >= r2) goto L_0x00f7;
    L_0x009a:
        r1 = r8.nu;
        r2 = r8.naptrRecords;
        r5 = r8.naptrIndex;
        r2 = r2[r5];
        r5 = 2;
        r2 = r2[r5];
        r1 = r1.getSRVRecords(r2);
        r8.srvRecords = r1;
        r1 = r8.srvRecords;
        if (r1 == 0) goto L_0x008d;
    L_0x00af:
        r1 = r8.srvRecords;
        r1 = r1.length;
        if (r1 <= 0) goto L_0x008d;
    L_0x00b4:
        r1 = net.java.sip.communicator.impl.protocol.sip.net.AutoProxyConnection.State.NaptrSrvHosts;
        r8.state = r1;
        r1 = "TLS";
        r2 = r8.naptrRecords;
        r5 = r8.naptrIndex;
        r2 = r2[r5];
        r2 = r2[r3];
        r1 = r1.equalsIgnoreCase(r2);
        if (r1 == 0) goto L_0x00dd;
    L_0x00c8:
        r1 = "TLS";
        r8.transport = r1;
    L_0x00cc:
        r8.srvRecordsIndex = r4;
        r1 = r8.getNextAddressFromDns();
        if (r1 == 0) goto L_0x008d;
    L_0x00d4:
        r1 = r8.naptrIndex;
        r1 = r1 + 1;
        r8.naptrIndex = r1;
        r1 = r3;
        goto L_0x0012;
    L_0x00dd:
        r1 = "TCP";
        r2 = r8.naptrRecords;
        r5 = r8.naptrIndex;
        r2 = r2[r5];
        r2 = r2[r3];
        r1 = r1.equalsIgnoreCase(r2);
        if (r1 == 0) goto L_0x00f2;
    L_0x00ed:
        r1 = "TCP";
        r8.transport = r1;
        goto L_0x00cc;
    L_0x00f2:
        r1 = "UDP";
        r8.transport = r1;
        goto L_0x00cc;
    L_0x00f7:
        r1 = r4;
        goto L_0x0012;
    L_0x00fa:
        r1 = r8.srvRecordsIndex;
        r1 = r1 + 1;
        r8.srvRecordsIndex = r1;
    L_0x0100:
        r1 = r8.srvRecordsIndex;
        r2 = r8.srvRecords;
        r2 = r2.length;
        if (r1 >= r2) goto L_0x0141;
    L_0x0107:
        r1 = r8.nu;
        r2 = r8.srvRecords;
        r5 = r8.srvRecordsIndex;
        r2 = r2[r5];
        r2 = r2.getTarget();
        r5 = r8.srvRecords;
        r6 = r8.srvRecordsIndex;
        r5 = r5[r6];
        r5 = r5.getPort();
        r1 = r1.getAandAAAARecords(r2, r5);
        r8.socketAddresses = r1;
        r1 = r8.socketAddresses;
        if (r1 == 0) goto L_0x00fa;
    L_0x0127:
        r1 = r8.socketAddresses;
        r1 = r1.length;
        if (r1 <= 0) goto L_0x00fa;
    L_0x012c:
        r1 = net.java.sip.communicator.impl.protocol.sip.net.AutoProxyConnection.State.NaptrSrvHostIPs;
        r8.state = r1;
        r8.socketAddressIndex = r4;
        r1 = r8.getNextAddressFromDns();
        if (r1 == 0) goto L_0x00fa;
    L_0x0138:
        r1 = r8.srvRecordsIndex;
        r1 = r1 + 1;
        r8.srvRecordsIndex = r1;
        r1 = r3;
        goto L_0x0012;
    L_0x0141:
        r1 = net.java.sip.communicator.impl.protocol.sip.net.AutoProxyConnection.State.NaptrSrv;
        r8.state = r1;
        r1 = r8.getNextAddressFromDns();
        goto L_0x0012;
    L_0x014b:
        r1 = r8.socketAddressIndex;
        r2 = r8.socketAddresses;
        r2 = r2.length;
        if (r1 < r2) goto L_0x015c;
    L_0x0152:
        r1 = net.java.sip.communicator.impl.protocol.sip.net.AutoProxyConnection.State.NaptrSrvHosts;
        r8.state = r1;
        r1 = r8.getNextAddressFromDns();
        goto L_0x0012;
    L_0x015c:
        r1 = r8.socketAddresses;
        r2 = r8.socketAddressIndex;
        r1 = r1[r2];
        r8.socketAddress = r1;
        r1 = r8.socketAddressIndex;
        r1 = r1 + 1;
        r8.socketAddressIndex = r1;
        r1 = r3;
        goto L_0x0012;
    L_0x016d:
        r1 = r8.srvTransportIndex;
        r1 = r1 + 1;
        r8.srvTransportIndex = r1;
    L_0x0173:
        r1 = r8.srvTransportIndex;
        r2 = transports;
        r2 = r2.length;
        if (r1 >= r2) goto L_0x01d2;
    L_0x017a:
        r5 = r8.nu;
        r1 = "TLS";
        r2 = transports;
        r6 = r8.srvTransportIndex;
        r2 = r2[r6];
        r1 = r1.equals(r2);
        if (r1 == 0) goto L_0x01cc;
    L_0x018a:
        r1 = "sips";
    L_0x018c:
        r2 = "UDP";
        r6 = transports;
        r7 = r8.srvTransportIndex;
        r6 = r6[r7];
        r2 = r2.equalsIgnoreCase(r6);
        if (r2 == 0) goto L_0x01cf;
    L_0x019a:
        r2 = "UDP";
    L_0x019c:
        r6 = r8.address;
        r1 = r5.getSRVRecords(r1, r2, r6);
        r8.srvRecords = r1;
        r1 = r8.srvRecords;
        if (r1 == 0) goto L_0x016d;
    L_0x01a8:
        r1 = r8.srvRecords;
        r1 = r1.length;
        if (r1 <= 0) goto L_0x016d;
    L_0x01ad:
        r8.hadSrvResults = r3;
        r1 = net.java.sip.communicator.impl.protocol.sip.net.AutoProxyConnection.State.SrvHosts;
        r8.state = r1;
        r8.srvRecordsIndex = r4;
        r1 = transports;
        r2 = r8.srvTransportIndex;
        r1 = r1[r2];
        r8.transport = r1;
        r1 = r8.getNextAddressFromDns();
        if (r1 == 0) goto L_0x016d;
    L_0x01c3:
        r1 = r8.srvTransportIndex;
        r1 = r1 + 1;
        r8.srvTransportIndex = r1;
        r1 = r3;
        goto L_0x0012;
    L_0x01cc:
        r1 = "sip";
        goto L_0x018c;
    L_0x01cf:
        r2 = "TCP";
        goto L_0x019c;
    L_0x01d2:
        r1 = r8.hadSrvResults;
        if (r1 != 0) goto L_0x01e2;
    L_0x01d6:
        r1 = net.java.sip.communicator.impl.protocol.sip.net.AutoProxyConnection.State.Hosts;
        r8.state = r1;
        r8.socketAddressIndex = r4;
        r1 = r8.getNextAddressFromDns();
        goto L_0x0012;
    L_0x01e2:
        r1 = r4;
        goto L_0x0012;
    L_0x01e5:
        r1 = r8.srvRecordsIndex;
        r2 = r8.srvRecords;
        r2 = r2.length;
        if (r1 < r2) goto L_0x01fc;
    L_0x01ec:
        r1 = net.java.sip.communicator.impl.protocol.sip.net.AutoProxyConnection.State.Srv;
        r8.state = r1;
        r1 = r8.getNextAddressFromDns();
        goto L_0x0012;
    L_0x01f6:
        r1 = r8.srvRecordsIndex;
        r1 = r1 + 1;
        r8.srvRecordsIndex = r1;
    L_0x01fc:
        r1 = r8.srvRecordsIndex;
        r2 = r8.srvRecords;
        r2 = r2.length;
        if (r1 >= r2) goto L_0x023d;
    L_0x0203:
        r1 = r8.nu;
        r2 = r8.srvRecords;
        r5 = r8.srvRecordsIndex;
        r2 = r2[r5];
        r2 = r2.getTarget();
        r5 = r8.srvRecords;
        r6 = r8.srvRecordsIndex;
        r5 = r5[r6];
        r5 = r5.getPort();
        r1 = r1.getAandAAAARecords(r2, r5);
        r8.socketAddresses = r1;
        r1 = r8.socketAddresses;
        if (r1 == 0) goto L_0x01f6;
    L_0x0223:
        r1 = r8.socketAddresses;
        r1 = r1.length;
        if (r1 <= 0) goto L_0x01f6;
    L_0x0228:
        r1 = net.java.sip.communicator.impl.protocol.sip.net.AutoProxyConnection.State.SrvHostIPs;
        r8.state = r1;
        r8.socketAddressIndex = r4;
        r1 = r8.getNextAddressFromDns();
        if (r1 == 0) goto L_0x01f6;
    L_0x0234:
        r1 = r8.srvRecordsIndex;
        r1 = r1 + 1;
        r8.srvRecordsIndex = r1;
        r1 = r3;
        goto L_0x0012;
    L_0x023d:
        r1 = r4;
        goto L_0x0012;
    L_0x0240:
        r1 = r8.socketAddressIndex;
        r2 = r8.socketAddresses;
        r2 = r2.length;
        if (r1 < r2) goto L_0x0251;
    L_0x0247:
        r1 = net.java.sip.communicator.impl.protocol.sip.net.AutoProxyConnection.State.SrvHosts;
        r8.state = r1;
        r1 = r8.getNextAddressFromDns();
        goto L_0x0012;
    L_0x0251:
        r1 = r8.socketAddresses;
        r2 = r8.socketAddressIndex;
        r1 = r1[r2];
        r8.socketAddress = r1;
        r1 = r8.socketAddressIndex;
        r1 = r1 + 1;
        r8.socketAddressIndex = r1;
        r1 = r3;
        goto L_0x0012;
    L_0x0262:
        r2 = r8.defaultTransport;
        r8.transport = r2;
        r2 = r8.socketAddresses;
        if (r2 != 0) goto L_0x0274;
    L_0x026a:
        r2 = r8.nu;
        r5 = r8.address;
        r1 = r2.getAandAAAARecords(r5, r1);
        r8.socketAddresses = r1;
    L_0x0274:
        r1 = r8.socketAddresses;
        if (r1 == 0) goto L_0x0293;
    L_0x0278:
        r1 = r8.socketAddresses;
        r1 = r1.length;
        if (r1 <= 0) goto L_0x0293;
    L_0x027d:
        r1 = r8.socketAddressIndex;
        r2 = r8.socketAddresses;
        r2 = r2.length;
        if (r1 >= r2) goto L_0x0293;
    L_0x0284:
        r1 = r8.socketAddresses;
        r2 = r8.socketAddressIndex;
        r4 = r2 + 1;
        r8.socketAddressIndex = r4;
        r1 = r1[r2];
        r8.socketAddress = r1;
        r1 = r3;
        goto L_0x0012;
    L_0x0293:
        r1 = r4;
        goto L_0x0012;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.protocol.sip.net.AutoProxyConnection.getNextAddressInternal():boolean");
    }

    public void reset() {
        super.reset();
        this.state = State.New;
        String userID = this.account.getAccountPropertyString("USER_ID");
        int domainIx = userID.indexOf(Separators.AT);
        if (domainIx > 0) {
            this.address = userID.substring(domainIx + 1);
        } else {
            this.address = this.account.getAccountPropertyString("SERVER_ADDRESS");
            if (this.address == null || this.address.trim().length() == 0) {
                return;
            }
        }
        if (this.nu.isValidIPAddress(this.address)) {
            this.state = State.IP;
            this.socketAddressIndex = 0;
        }
    }
}
