package org.dhcp4java;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DHCPConstants {
    public static final int BOOTP_REPLY_PORT = 68;
    public static final int BOOTP_REQUEST_PORT = 67;
    public static final byte BOOTREPLY = (byte) 2;
    public static final byte BOOTREQUEST = (byte) 1;
    public static final byte DHCPACK = (byte) 5;
    public static final byte DHCPDECLINE = (byte) 4;
    public static final byte DHCPDISCOVER = (byte) 1;
    public static final byte DHCPFORCERENEW = (byte) 9;
    public static final byte DHCPINFORM = (byte) 8;
    public static final byte DHCPLEASEACTIVE = (byte) 13;
    public static final byte DHCPLEASEQUERY = (byte) 10;
    public static final byte DHCPLEASEUNASSIGNED = (byte) 11;
    public static final byte DHCPLEASEUNKNOWN = (byte) 12;
    public static final byte DHCPNAK = (byte) 6;
    public static final byte DHCPOFFER = (byte) 2;
    public static final byte DHCPRELEASE = (byte) 7;
    public static final byte DHCPREQUEST = (byte) 3;
    public static final byte DHO_ALL_SUBNETS_LOCAL = (byte) 27;
    public static final byte DHO_ARP_CACHE_TIMEOUT = (byte) 35;
    public static final byte DHO_ASSOCIATED_IP = (byte) 92;
    public static final byte DHO_AUTO_CONFIGURE = (byte) 116;
    public static final byte DHO_BOOTFILE = (byte) 67;
    public static final byte DHO_BOOT_SIZE = (byte) 13;
    public static final byte DHO_BROADCAST_ADDRESS = (byte) 28;
    public static final byte DHO_CLASSLESS_ROUTE = (byte) 121;
    public static final byte DHO_CLIENT_LAST_TRANSACTION_TIME = (byte) 91;
    public static final byte DHO_COOKIE_SERVERS = (byte) 8;
    public static final byte DHO_DEFAULT_IP_TTL = (byte) 23;
    public static final byte DHO_DEFAULT_TCP_TTL = (byte) 37;
    public static final byte DHO_DHCP_AGENT_OPTIONS = (byte) 82;
    public static final byte DHO_DHCP_CLIENT_IDENTIFIER = (byte) 61;
    public static final byte DHO_DHCP_LEASE_TIME = (byte) 51;
    public static final byte DHO_DHCP_MAX_MESSAGE_SIZE = (byte) 57;
    public static final byte DHO_DHCP_MESSAGE = (byte) 56;
    public static final byte DHO_DHCP_MESSAGE_TYPE = (byte) 53;
    public static final byte DHO_DHCP_OPTION_OVERLOAD = (byte) 52;
    public static final byte DHO_DHCP_PARAMETER_REQUEST_LIST = (byte) 55;
    public static final byte DHO_DHCP_REBINDING_TIME = (byte) 59;
    public static final byte DHO_DHCP_RENEWAL_TIME = (byte) 58;
    public static final byte DHO_DHCP_REQUESTED_ADDRESS = (byte) 50;
    public static final byte DHO_DHCP_SERVER_IDENTIFIER = (byte) 54;
    public static final byte DHO_DOMAIN_NAME = (byte) 15;
    public static final byte DHO_DOMAIN_NAME_SERVERS = (byte) 6;
    public static final byte DHO_DOMAIN_SEARCH = (byte) 119;
    public static final byte DHO_END = (byte) -1;
    public static final byte DHO_EXTENSIONS_PATH = (byte) 18;
    public static final byte DHO_FINGER_SERVER = (byte) 73;
    public static final byte DHO_FONT_SERVERS = (byte) 48;
    public static final byte DHO_FQDN = (byte) 81;
    public static final byte DHO_HOST_NAME = (byte) 12;
    public static final byte DHO_IEEE802_3_ENCAPSULATION = (byte) 36;
    public static final byte DHO_IMPRESS_SERVERS = (byte) 10;
    public static final byte DHO_INTERFACE_MTU = (byte) 26;
    public static final byte DHO_IP_FORWARDING = (byte) 19;
    public static final byte DHO_IRC_SERVER = (byte) 74;
    public static final byte DHO_LOG_SERVERS = (byte) 7;
    public static final byte DHO_LPR_SERVERS = (byte) 9;
    public static final byte DHO_MASK_SUPPLIER = (byte) 30;
    public static final byte DHO_MAX_DGRAM_REASSEMBLY = (byte) 22;
    public static final byte DHO_MERIT_DUMP = (byte) 14;
    public static final byte DHO_MOBILE_IP_HOME_AGENT = (byte) 68;
    public static final byte DHO_NAME_SERVERS = (byte) 5;
    public static final byte DHO_NAME_SERVICE_SEARCH = (byte) 117;
    public static final byte DHO_NDS_CONTEXT = (byte) 87;
    public static final byte DHO_NDS_SERVERS = (byte) 85;
    public static final byte DHO_NDS_TREE_NAME = (byte) 86;
    public static final byte DHO_NETBIOS_DD_SERVER = (byte) 45;
    public static final byte DHO_NETBIOS_NAME_SERVERS = (byte) 44;
    public static final byte DHO_NETBIOS_NODE_TYPE = (byte) 46;
    public static final byte DHO_NETBIOS_SCOPE = (byte) 47;
    public static final byte DHO_NISPLUS_DOMAIN = (byte) 64;
    public static final byte DHO_NISPLUS_SERVER = (byte) 65;
    public static final byte DHO_NIS_SERVERS = (byte) 41;
    public static final byte DHO_NNTP_SERVER = (byte) 71;
    public static final byte DHO_NON_LOCAL_SOURCE_ROUTING = (byte) 20;
    public static final byte DHO_NTP_SERVERS = (byte) 42;
    public static final byte DHO_NWIP_DOMAIN_NAME = (byte) 62;
    public static final byte DHO_NWIP_SUBOPTIONS = (byte) 63;
    public static final byte DHO_PAD = (byte) 0;
    public static final byte DHO_PATH_MTU_AGING_TIMEOUT = (byte) 24;
    public static final byte DHO_PATH_MTU_PLATEAU_TABLE = (byte) 25;
    public static final byte DHO_PERFORM_MASK_DISCOVERY = (byte) 29;
    public static final byte DHO_POLICY_FILTER = (byte) 21;
    public static final byte DHO_POP3_SERVER = (byte) 70;
    public static final byte DHO_RESOURCE_LOCATION_SERVERS = (byte) 11;
    public static final byte DHO_ROOT_PATH = (byte) 17;
    public static final byte DHO_ROUTERS = (byte) 3;
    public static final byte DHO_ROUTER_DISCOVERY = (byte) 31;
    public static final byte DHO_ROUTER_SOLICITATION_ADDRESS = (byte) 32;
    public static final byte DHO_SMTP_SERVER = (byte) 69;
    public static final byte DHO_STATIC_ROUTES = (byte) 33;
    public static final byte DHO_STDA_SERVER = (byte) 76;
    public static final byte DHO_STREETTALK_SERVER = (byte) 75;
    public static final byte DHO_SUBNET_MASK = (byte) 1;
    public static final byte DHO_SUBNET_SELECTION = (byte) 118;
    public static final byte DHO_SWAP_SERVER = (byte) 16;
    public static final byte DHO_TCP_KEEPALIVE_GARBAGE = (byte) 39;
    public static final byte DHO_TCP_KEEPALIVE_INTERVAL = (byte) 38;
    public static final byte DHO_TFTP_SERVER = (byte) 66;
    public static final byte DHO_TIME_OFFSET = (byte) 2;
    public static final byte DHO_TIME_SERVERS = (byte) 4;
    public static final byte DHO_TRAILER_ENCAPSULATION = (byte) 34;
    public static final byte DHO_USER_AUTHENTICATION_PROTOCOL = (byte) 98;
    public static final byte DHO_USER_CLASS = (byte) 77;
    public static final byte DHO_VENDOR_CLASS_IDENTIFIER = (byte) 60;
    public static final byte DHO_VENDOR_ENCAPSULATED_OPTIONS = (byte) 43;
    public static final byte DHO_WWW_SERVER = (byte) 72;
    public static final byte DHO_X_DISPLAY_MANAGER = (byte) 49;
    public static final byte HTYPE_ETHER = (byte) 1;
    public static final byte HTYPE_FDDI = (byte) 8;
    public static final byte HTYPE_IEEE1394 = (byte) 24;
    public static final byte HTYPE_IEEE802 = (byte) 6;
    public static final InetAddress INADDR_ANY = getInaddrAny();
    public static final InetAddress INADDR_BROADCAST = getInaddrBroadcast();
    static final int _BOOTP_ABSOLUTE_MIN_LEN = 236;
    static final int _BOOTP_VEND_SIZE = 64;
    static final Map<Byte, String> _BOOT_NAMES;
    static final Map<Byte, String> _DHCP_CODES;
    static final int _DHCP_DEFAULT_MAX_LEN = 576;
    static final int _DHCP_MAX_MTU = 1500;
    static final int _DHCP_MIN_LEN = 548;
    static final int _DHCP_UDP_OVERHEAD = 42;
    static final Map<Byte, String> _DHO_NAMES;
    static final Map<String, Byte> _DHO_NAMES_REV;
    static final Map<Byte, String> _HTYPE_NAMES;
    static final int _MAGIC_COOKIE = 1669485411;

    private DHCPConstants() {
        throw new UnsupportedOperationException();
    }

    static {
        Map<Byte, String> bootNames = new LinkedHashMap();
        Map<Byte, String> htypeNames = new LinkedHashMap();
        Map<Byte, String> dhcpCodes = new LinkedHashMap();
        Map<Byte, String> dhoNames = new LinkedHashMap();
        Map<String, Byte> dhoNamesRev = new LinkedHashMap();
        try {
            for (Field field : DHCPConstants.class.getDeclaredFields()) {
                int mod = field.getModifiers();
                String name = field.getName();
                if (Modifier.isFinal(mod) && Modifier.isPublic(mod) && Modifier.isStatic(mod) && field.getType().equals(Byte.TYPE)) {
                    byte code = field.getByte(null);
                    if (name.startsWith("BOOT")) {
                        bootNames.put(Byte.valueOf(code), name);
                    } else if (name.startsWith("HTYPE_")) {
                        htypeNames.put(Byte.valueOf(code), name);
                    } else if (name.startsWith("DHCP")) {
                        dhcpCodes.put(Byte.valueOf(code), name);
                    } else if (name.startsWith("DHO_")) {
                        dhoNames.put(Byte.valueOf(code), name);
                        dhoNamesRev.put(name, Byte.valueOf(code));
                    }
                }
            }
            _BOOT_NAMES = Collections.unmodifiableMap(bootNames);
            _HTYPE_NAMES = Collections.unmodifiableMap(htypeNames);
            _DHCP_CODES = Collections.unmodifiableMap(dhcpCodes);
            _DHO_NAMES = Collections.unmodifiableMap(dhoNames);
            _DHO_NAMES_REV = Collections.unmodifiableMap(dhoNamesRev);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Fatal error while parsing internal fields");
        }
    }

    private static final InetAddress getInaddrAny() {
        try {
            return InetAddress.getByAddress(new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0});
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Unable to generate INADDR_ANY");
        }
    }

    private static final InetAddress getInaddrBroadcast() {
        try {
            return InetAddress.getByAddress(new byte[]{(byte) -1, (byte) -1, (byte) -1, (byte) -1});
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Unable to generate INADDR_BROADCAST");
        }
    }

    public static final Map<Byte, String> getBootNamesMap() {
        return _BOOT_NAMES;
    }

    public static final Map<Byte, String> getHtypesMap() {
        return _HTYPE_NAMES;
    }

    public static final Map<Byte, String> getDhcpCodesMap() {
        return _DHCP_CODES;
    }

    public static final Map<Byte, String> getDhoNamesMap() {
        return _DHO_NAMES;
    }

    public static final Map<String, Byte> getDhoNamesReverseMap() {
        return _DHO_NAMES_REV;
    }

    public static final Byte getDhoNamesReverse(String name) {
        if (name != null) {
            return (Byte) _DHO_NAMES_REV.get(name);
        }
        throw new NullPointerException();
    }

    public static final String getDhoName(byte code) {
        return (String) _DHO_NAMES.get(Byte.valueOf(code));
    }
}
