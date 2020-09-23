package org.dhcp4java;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jitsi.gov.nist.core.Separators;

public class DHCPOption implements Serializable {
    static final /* synthetic */ boolean $assertionsDisabled = (!DHCPOption.class.desiredAssertionStatus() ? true : $assertionsDisabled);
    static final Map<Byte, OptionFormat> _DHO_FORMATS = new LinkedHashMap();
    private static final Object[] _OPTION_FORMATS = new Object[]{Byte.valueOf((byte) 1), OptionFormat.INET, Byte.valueOf((byte) 2), OptionFormat.INT, Byte.valueOf((byte) 3), OptionFormat.INETS, Byte.valueOf((byte) 4), OptionFormat.INETS, Byte.valueOf((byte) 5), OptionFormat.INETS, Byte.valueOf((byte) 6), OptionFormat.INETS, Byte.valueOf((byte) 7), OptionFormat.INETS, Byte.valueOf((byte) 8), OptionFormat.INETS, Byte.valueOf((byte) 9), OptionFormat.INETS, Byte.valueOf((byte) 10), OptionFormat.INETS, Byte.valueOf((byte) 11), OptionFormat.INETS, Byte.valueOf((byte) 12), OptionFormat.STRING, Byte.valueOf((byte) 13), OptionFormat.SHORT, Byte.valueOf(DHCPConstants.DHO_MERIT_DUMP), OptionFormat.STRING, Byte.valueOf(DHCPConstants.DHO_DOMAIN_NAME), OptionFormat.STRING, Byte.valueOf(DHCPConstants.DHO_SWAP_SERVER), OptionFormat.INET, Byte.valueOf(DHCPConstants.DHO_ROOT_PATH), OptionFormat.STRING, Byte.valueOf(DHCPConstants.DHO_EXTENSIONS_PATH), OptionFormat.STRING, Byte.valueOf(DHCPConstants.DHO_IP_FORWARDING), OptionFormat.BYTE, Byte.valueOf(DHCPConstants.DHO_NON_LOCAL_SOURCE_ROUTING), OptionFormat.BYTE, Byte.valueOf(DHCPConstants.DHO_POLICY_FILTER), OptionFormat.INETS, Byte.valueOf(DHCPConstants.DHO_MAX_DGRAM_REASSEMBLY), OptionFormat.SHORT, Byte.valueOf(DHCPConstants.DHO_DEFAULT_IP_TTL), OptionFormat.BYTE, Byte.valueOf((byte) 24), OptionFormat.INT, Byte.valueOf(DHCPConstants.DHO_PATH_MTU_PLATEAU_TABLE), OptionFormat.SHORTS, Byte.valueOf(DHCPConstants.DHO_INTERFACE_MTU), OptionFormat.SHORT, Byte.valueOf(DHCPConstants.DHO_ALL_SUBNETS_LOCAL), OptionFormat.BYTE, Byte.valueOf(DHCPConstants.DHO_BROADCAST_ADDRESS), OptionFormat.INET, Byte.valueOf(DHCPConstants.DHO_PERFORM_MASK_DISCOVERY), OptionFormat.BYTE, Byte.valueOf(DHCPConstants.DHO_MASK_SUPPLIER), OptionFormat.BYTE, Byte.valueOf(DHCPConstants.DHO_ROUTER_DISCOVERY), OptionFormat.BYTE, Byte.valueOf(DHCPConstants.DHO_ROUTER_SOLICITATION_ADDRESS), OptionFormat.INET, Byte.valueOf(DHCPConstants.DHO_STATIC_ROUTES), OptionFormat.INETS, Byte.valueOf(DHCPConstants.DHO_TRAILER_ENCAPSULATION), OptionFormat.BYTE, Byte.valueOf(DHCPConstants.DHO_ARP_CACHE_TIMEOUT), OptionFormat.INT, Byte.valueOf(DHCPConstants.DHO_IEEE802_3_ENCAPSULATION), OptionFormat.BYTE, Byte.valueOf(DHCPConstants.DHO_DEFAULT_TCP_TTL), OptionFormat.BYTE, Byte.valueOf(DHCPConstants.DHO_TCP_KEEPALIVE_INTERVAL), OptionFormat.INT, Byte.valueOf(DHCPConstants.DHO_TCP_KEEPALIVE_GARBAGE), OptionFormat.BYTE, Byte.valueOf(DHCPConstants.DHO_NIS_SERVERS), OptionFormat.INETS, Byte.valueOf(DHCPConstants.DHO_NTP_SERVERS), OptionFormat.INETS, Byte.valueOf(DHCPConstants.DHO_NETBIOS_NAME_SERVERS), OptionFormat.INETS, Byte.valueOf(DHCPConstants.DHO_NETBIOS_DD_SERVER), OptionFormat.INETS, Byte.valueOf(DHCPConstants.DHO_NETBIOS_NODE_TYPE), OptionFormat.BYTE, Byte.valueOf(DHCPConstants.DHO_NETBIOS_SCOPE), OptionFormat.STRING, Byte.valueOf(DHCPConstants.DHO_FONT_SERVERS), OptionFormat.INETS, Byte.valueOf(DHCPConstants.DHO_X_DISPLAY_MANAGER), OptionFormat.INETS, Byte.valueOf(DHCPConstants.DHO_DHCP_REQUESTED_ADDRESS), OptionFormat.INET, Byte.valueOf(DHCPConstants.DHO_DHCP_LEASE_TIME), OptionFormat.INT, Byte.valueOf(DHCPConstants.DHO_DHCP_OPTION_OVERLOAD), OptionFormat.BYTE, Byte.valueOf(DHCPConstants.DHO_DHCP_MESSAGE_TYPE), OptionFormat.BYTE, Byte.valueOf(DHCPConstants.DHO_DHCP_SERVER_IDENTIFIER), OptionFormat.INET, Byte.valueOf(DHCPConstants.DHO_DHCP_PARAMETER_REQUEST_LIST), OptionFormat.BYTES, Byte.valueOf(DHCPConstants.DHO_DHCP_MESSAGE), OptionFormat.STRING, Byte.valueOf(DHCPConstants.DHO_DHCP_MAX_MESSAGE_SIZE), OptionFormat.SHORT, Byte.valueOf(DHCPConstants.DHO_DHCP_RENEWAL_TIME), OptionFormat.INT, Byte.valueOf(DHCPConstants.DHO_DHCP_REBINDING_TIME), OptionFormat.INT, Byte.valueOf(DHCPConstants.DHO_VENDOR_CLASS_IDENTIFIER), OptionFormat.STRING, Byte.valueOf(DHCPConstants.DHO_NWIP_DOMAIN_NAME), OptionFormat.STRING, Byte.valueOf(DHCPConstants.DHO_NISPLUS_DOMAIN), OptionFormat.STRING, Byte.valueOf(DHCPConstants.DHO_NISPLUS_SERVER), OptionFormat.STRING, Byte.valueOf(DHCPConstants.DHO_TFTP_SERVER), OptionFormat.STRING, Byte.valueOf(DHCPConstants.DHO_BOOTFILE), OptionFormat.STRING, Byte.valueOf(DHCPConstants.DHO_MOBILE_IP_HOME_AGENT), OptionFormat.INETS, Byte.valueOf(DHCPConstants.DHO_SMTP_SERVER), OptionFormat.INETS, Byte.valueOf(DHCPConstants.DHO_POP3_SERVER), OptionFormat.INETS, Byte.valueOf(DHCPConstants.DHO_NNTP_SERVER), OptionFormat.INETS, Byte.valueOf(DHCPConstants.DHO_WWW_SERVER), OptionFormat.INETS, Byte.valueOf(DHCPConstants.DHO_FINGER_SERVER), OptionFormat.INETS, Byte.valueOf(DHCPConstants.DHO_IRC_SERVER), OptionFormat.INETS, Byte.valueOf(DHCPConstants.DHO_STREETTALK_SERVER), OptionFormat.INETS, Byte.valueOf(DHCPConstants.DHO_STDA_SERVER), OptionFormat.INETS, Byte.valueOf(DHCPConstants.DHO_NDS_SERVERS), OptionFormat.INETS, Byte.valueOf(DHCPConstants.DHO_NDS_TREE_NAME), OptionFormat.STRING, Byte.valueOf(DHCPConstants.DHO_NDS_CONTEXT), OptionFormat.STRING, Byte.valueOf(DHCPConstants.DHO_CLIENT_LAST_TRANSACTION_TIME), OptionFormat.INT, Byte.valueOf(DHCPConstants.DHO_ASSOCIATED_IP), OptionFormat.INETS, Byte.valueOf(DHCPConstants.DHO_USER_AUTHENTICATION_PROTOCOL), OptionFormat.STRING, Byte.valueOf(DHCPConstants.DHO_AUTO_CONFIGURE), OptionFormat.BYTE, Byte.valueOf(DHCPConstants.DHO_NAME_SERVICE_SEARCH), OptionFormat.SHORTS, Byte.valueOf(DHCPConstants.DHO_SUBNET_SELECTION), OptionFormat.INET, Byte.valueOf(DHCPConstants.DHO_DOMAIN_SEARCH), OptionFormat.STRING};
    private static final Logger logger = Logger.getLogger(DHCPOption.class.getName().toLowerCase());
    private static final long serialVersionUID = 2;
    private final byte code;
    private final boolean mirror;
    private final byte[] value;

    enum OptionFormat {
        INET,
        INETS,
        INT,
        SHORT,
        SHORTS,
        BYTE,
        BYTES,
        STRING
    }

    static {
        for (int i = 0; i < _OPTION_FORMATS.length / 2; i++) {
            _DHO_FORMATS.put((Byte) _OPTION_FORMATS[i * 2], (OptionFormat) _OPTION_FORMATS[(i * 2) + 1]);
        }
    }

    public DHCPOption(byte code, byte[] value, boolean mirror) {
        if (code == (byte) 0) {
            throw new IllegalArgumentException("code=0 is not allowed (reserved for padding");
        } else if (code == (byte) -1) {
            throw new IllegalArgumentException("code=-1 is not allowed (reserved for End Of Options)");
        } else {
            this.code = code;
            this.value = value != null ? (byte[]) value.clone() : null;
            this.mirror = mirror;
        }
    }

    public DHCPOption(byte code, byte[] value) {
        this(code, value, $assertionsDisabled);
    }

    public byte getCode() {
        return this.code;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DHCPOption)) {
            return $assertionsDisabled;
        }
        DHCPOption opt = (DHCPOption) o;
        return (opt.code == this.code && opt.mirror == this.mirror && Arrays.equals(opt.value, this.value)) ? true : $assertionsDisabled;
    }

    public int hashCode() {
        return (this.mirror ? Integer.MIN_VALUE : 0) ^ (Arrays.hashCode(this.value) ^ this.code);
    }

    public byte[] getValue() {
        return this.value == null ? null : (byte[]) this.value.clone();
    }

    public byte[] getValueFast() {
        return this.value;
    }

    public boolean isMirror() {
        return this.mirror;
    }

    public static final boolean isOptionAsByte(byte code) {
        return OptionFormat.BYTE.equals(_DHO_FORMATS.get(Byte.valueOf(code)));
    }

    public static DHCPOption newOptionAsByte(byte code, byte val) {
        if (isOptionAsByte(code)) {
            return new DHCPOption(code, byte2Bytes(val));
        }
        throw new IllegalArgumentException("DHCP option type (" + code + ") is not byte");
    }

    public byte getValueAsByte() throws IllegalArgumentException {
        if (!isOptionAsByte(this.code)) {
            throw new IllegalArgumentException("DHCP option type (" + this.code + ") is not byte");
        } else if (this.value == null) {
            throw new IllegalStateException("value is null");
        } else if (this.value.length == 1) {
            return this.value[0];
        } else {
            throw new DHCPBadPacketException("option " + this.code + " is wrong size:" + this.value.length + " should be 1");
        }
    }

    public static final boolean isOptionAsShort(byte code) {
        return OptionFormat.SHORT.equals(_DHO_FORMATS.get(Byte.valueOf(code)));
    }

    public short getValueAsShort() throws IllegalArgumentException {
        if (!isOptionAsShort(this.code)) {
            throw new IllegalArgumentException("DHCP option type (" + this.code + ") is not short");
        } else if (this.value == null) {
            throw new IllegalStateException("value is null");
        } else if (this.value.length == 2) {
            return (short) (((this.value[0] & 255) << 8) | (this.value[1] & 255));
        } else {
            throw new DHCPBadPacketException("option " + this.code + " is wrong size:" + this.value.length + " should be 2");
        }
    }

    public static final boolean isOptionAsInt(byte code) {
        return OptionFormat.INT.equals(_DHO_FORMATS.get(Byte.valueOf(code)));
    }

    public int getValueAsInt() throws IllegalArgumentException {
        if (!isOptionAsInt(this.code)) {
            throw new IllegalArgumentException("DHCP option type (" + this.code + ") is not int");
        } else if (this.value == null) {
            throw new IllegalStateException("value is null");
        } else if (this.value.length == 4) {
            return ((((this.value[0] & 255) << 24) | ((this.value[1] & 255) << 16)) | ((this.value[2] & 255) << 8)) | (this.value[3] & 255);
        } else {
            throw new DHCPBadPacketException("option " + this.code + " is wrong size:" + this.value.length + " should be 4");
        }
    }

    public Integer getValueAsNum() throws IllegalArgumentException {
        if (this.value == null) {
            return null;
        }
        if (this.value.length == 1) {
            return Integer.valueOf(this.value[0] & 255);
        }
        if (this.value.length == 2) {
            return Integer.valueOf(((this.value[0] & 255) << 8) | (this.value[1] & 255));
        }
        if (this.value.length == 4) {
            return Integer.valueOf(((((this.value[0] & 255) << 24) | ((this.value[1] & 255) << 16)) | ((this.value[2] & 255) << 8)) | (this.value[3] & 255));
        }
        return null;
    }

    public static final boolean isOptionAsInetAddr(byte code) {
        return OptionFormat.INET.equals(_DHO_FORMATS.get(Byte.valueOf(code)));
    }

    public InetAddress getValueAsInetAddr() throws IllegalArgumentException {
        if (!isOptionAsInetAddr(this.code)) {
            throw new IllegalArgumentException("DHCP option type (" + this.code + ") is not InetAddr");
        } else if (this.value == null) {
            throw new IllegalStateException("value is null");
        } else if (this.value.length != 4) {
            throw new DHCPBadPacketException("option " + this.code + " is wrong size:" + this.value.length + " should be 4");
        } else {
            try {
                return InetAddress.getByAddress(this.value);
            } catch (UnknownHostException e) {
                logger.log(Level.SEVERE, "Unexpected UnknownHostException", e);
                return null;
            }
        }
    }

    public static final boolean isOptionAsString(byte code) {
        return OptionFormat.STRING.equals(_DHO_FORMATS.get(Byte.valueOf(code)));
    }

    public String getValueAsString() throws IllegalArgumentException {
        if (!isOptionAsString(this.code)) {
            throw new IllegalArgumentException("DHCP option type (" + this.code + ") is not String");
        } else if (this.value != null) {
            return DHCPPacket.bytesToString(this.value);
        } else {
            throw new IllegalStateException("value is null");
        }
    }

    public static final boolean isOptionAsShorts(byte code) {
        return OptionFormat.SHORTS.equals(_DHO_FORMATS.get(Byte.valueOf(code)));
    }

    public short[] getValueAsShorts() throws IllegalArgumentException {
        if (!isOptionAsShorts(this.code)) {
            throw new IllegalArgumentException("DHCP option type (" + this.code + ") is not short[]");
        } else if (this.value == null) {
            throw new IllegalStateException("value is null");
        } else if (this.value.length % 2 != 0) {
            throw new DHCPBadPacketException("option " + this.code + " is wrong size:" + this.value.length + " should be 2*X");
        } else {
            short[] shorts = new short[(this.value.length / 2)];
            int i = 0;
            for (int a = 0; a < this.value.length; a += 2) {
                shorts[i] = (short) (((this.value[a] & 255) << 8) | (this.value[a + 1] & 255));
                i++;
            }
            return shorts;
        }
    }

    public static final boolean isOptionAsInetAddrs(byte code) {
        return OptionFormat.INETS.equals(_DHO_FORMATS.get(Byte.valueOf(code)));
    }

    public InetAddress[] getValueAsInetAddrs() throws IllegalArgumentException {
        if (!isOptionAsInetAddrs(this.code)) {
            throw new IllegalArgumentException("DHCP option type (" + this.code + ") is not InetAddr[]");
        } else if (this.value == null) {
            throw new IllegalStateException("value is null");
        } else if (this.value.length % 4 != 0) {
            throw new DHCPBadPacketException("option " + this.code + " is wrong size:" + this.value.length + " should be 4*X");
        } else {
            try {
                byte[] addr = new byte[4];
                InetAddress[] inetAddressArr = new InetAddress[(this.value.length / 4)];
                int i = 0;
                for (int a = 0; a < this.value.length; a += 4) {
                    addr[0] = this.value[a];
                    addr[1] = this.value[a + 1];
                    addr[2] = this.value[a + 2];
                    addr[3] = this.value[a + 3];
                    inetAddressArr[i] = InetAddress.getByAddress(addr);
                    i++;
                }
                return inetAddressArr;
            } catch (UnknownHostException e) {
                logger.log(Level.SEVERE, "Unexpected UnknownHostException", e);
                return null;
            }
        }
    }

    public static final boolean isOptionAsBytes(byte code) {
        return OptionFormat.BYTES.equals(_DHO_FORMATS.get(Byte.valueOf(code)));
    }

    public byte[] getValueAsBytes() throws IllegalArgumentException {
        if (!isOptionAsBytes(this.code)) {
            throw new IllegalArgumentException("DHCP option type (" + this.code + ") is not bytes");
        } else if (this.value != null) {
            return getValue();
        } else {
            throw new IllegalStateException("value is null");
        }
    }

    public static DHCPOption newOptionAsShort(byte code, short val) {
        if (isOptionAsShort(code)) {
            return new DHCPOption(code, short2Bytes(val));
        }
        throw new IllegalArgumentException("DHCP option type (" + code + ") is not short");
    }

    public static DHCPOption newOptionAsShorts(byte code, short[] arr) {
        if (isOptionAsShorts(code)) {
            byte[] buf = null;
            if (arr != null) {
                buf = new byte[(arr.length * 2)];
                for (int i = 0; i < arr.length; i++) {
                    short val = arr[i];
                    buf[i * 2] = (byte) ((65280 & val) >>> 8);
                    buf[(i * 2) + 1] = (byte) (val & 255);
                }
            }
            return new DHCPOption(code, buf);
        }
        throw new IllegalArgumentException("DHCP option type (" + code + ") is not shorts");
    }

    public static DHCPOption newOptionAsInt(byte code, int val) {
        if (isOptionAsInt(code)) {
            return new DHCPOption(code, int2Bytes(val));
        }
        throw new IllegalArgumentException("DHCP option type (" + code + ") is not int");
    }

    public static DHCPOption newOptionAsInetAddress(byte code, InetAddress val) {
        if (isOptionAsInetAddr(code) || isOptionAsInetAddrs(code)) {
            return new DHCPOption(code, inetAddress2Bytes(val));
        }
        throw new IllegalArgumentException("DHCP option type (" + code + ") is not InetAddress");
    }

    public static DHCPOption newOptionAsInetAddresses(byte code, InetAddress[] val) {
        if (isOptionAsInetAddrs(code)) {
            return new DHCPOption(code, inetAddresses2Bytes(val));
        }
        throw new IllegalArgumentException("DHCP option type (" + code + ") is not InetAddresses");
    }

    public static DHCPOption newOptionAsString(byte code, String val) {
        if (isOptionAsString(code)) {
            return new DHCPOption(code, DHCPPacket.stringToBytes(val));
        }
        throw new IllegalArgumentException("DHCP option type (" + code + ") is not string");
    }

    public DHCPOption applyOption(DHCPPacket request) {
        if (request == null) {
            throw new NullPointerException("request is null");
        } else if (!this.mirror) {
            return this;
        } else {
            DHCPOption res = request.getOption(getCode());
            return res != null ? res : this;
        }
    }

    public void append(StringBuilder buffer) {
        if (DHCPConstants._DHO_NAMES.containsKey(Byte.valueOf(this.code))) {
            buffer.append((String) DHCPConstants._DHO_NAMES.get(Byte.valueOf(this.code)));
        }
        buffer.append('(').append(unsignedByte(this.code)).append(")=");
        if (this.mirror) {
            buffer.append("<mirror>");
        }
        if (this.value == null) {
            buffer.append("<null>");
        } else if (this.code == DHCPConstants.DHO_DHCP_MESSAGE_TYPE) {
            Byte cmd = Byte.valueOf(getValueAsByte());
            if (DHCPConstants._DHCP_CODES.containsKey(cmd)) {
                buffer.append((String) DHCPConstants._DHCP_CODES.get(cmd));
            } else {
                buffer.append(cmd);
            }
        } else if (this.code == DHCPConstants.DHO_USER_CLASS) {
            buffer.append(userClassToString(this.value));
        } else if (this.code == DHCPConstants.DHO_DHCP_AGENT_OPTIONS) {
            buffer.append(agentOptionsToString(this.value));
        } else if (_DHO_FORMATS.containsKey(Byte.valueOf(this.code))) {
            try {
                switch ((OptionFormat) _DHO_FORMATS.get(Byte.valueOf(this.code))) {
                    case INET:
                        DHCPPacket.appendHostAddress(buffer, getValueAsInetAddr());
                        return;
                    case INETS:
                        for (InetAddress addr : getValueAsInetAddrs()) {
                            DHCPPacket.appendHostAddress(buffer, addr);
                            buffer.append(' ');
                        }
                        return;
                    case INT:
                        buffer.append(getValueAsInt());
                        return;
                    case SHORT:
                        buffer.append(getValueAsShort());
                        return;
                    case SHORTS:
                        for (short aShort : getValueAsShorts()) {
                            buffer.append(aShort).append(' ');
                        }
                        return;
                    case BYTE:
                        buffer.append(getValueAsByte());
                        return;
                    case STRING:
                        buffer.append('\"').append(getValueAsString()).append('\"');
                        return;
                    case BYTES:
                        if (this.value != null) {
                            for (byte aValue : this.value) {
                                buffer.append(unsignedByte(aValue)).append(' ');
                            }
                            return;
                        }
                        return;
                    default:
                        buffer.append("0x");
                        DHCPPacket.appendHex(buffer, this.value);
                        return;
                }
            } catch (IllegalArgumentException e) {
                buffer.append("0x");
                DHCPPacket.appendHex(buffer, this.value);
            }
            buffer.append("0x");
            DHCPPacket.appendHex(buffer, this.value);
        } else {
            buffer.append("0x");
            DHCPPacket.appendHex(buffer, this.value);
        }
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        append(s);
        return s.toString();
    }

    private static int unsignedByte(byte b) {
        return b & 255;
    }

    public static byte[] byte2Bytes(byte val) {
        return new byte[]{val};
    }

    public static byte[] short2Bytes(short val) {
        return new byte[]{(byte) ((65280 & val) >>> 8), (byte) (val & 255)};
    }

    public static byte[] int2Bytes(int val) {
        return new byte[]{(byte) ((-16777216 & val) >>> 24), (byte) ((16711680 & val) >>> 16), (byte) ((65280 & val) >>> 8), (byte) (val & 255)};
    }

    public static byte[] inetAddress2Bytes(InetAddress val) {
        if (val == null) {
            return null;
        }
        if (val instanceof Inet4Address) {
            return val.getAddress();
        }
        throw new IllegalArgumentException("Adress must be of subclass Inet4Address");
    }

    public static byte[] inetAddresses2Bytes(InetAddress[] val) {
        if (val == null) {
            return null;
        }
        byte[] buf = new byte[(val.length * 4)];
        int i = 0;
        while (i < val.length) {
            InetAddress addr = val[i];
            if (addr instanceof Inet4Address) {
                System.arraycopy(addr.getAddress(), 0, buf, i * 4, 4);
                i++;
            } else {
                throw new IllegalArgumentException("Adress must be of subclass Inet4Address");
            }
        }
        return buf;
    }

    public static List<String> userClassToList(byte[] buf) {
        if (buf == null) {
            return null;
        }
        List<String> list = new LinkedList();
        int i = 0;
        while (i < buf.length) {
            int i2 = i + 1;
            int size = unsignedByte(buf[i]);
            int instock = buf.length - i2;
            if (size > instock) {
                size = instock;
            }
            list.add(DHCPPacket.bytesToString(buf, i2, size));
            i = i2 + size;
        }
        return list;
    }

    public static String userClassToString(byte[] buf) {
        if (buf == null) {
            return null;
        }
        Iterator it = userClassToList(buf).iterator();
        StringBuffer s = new StringBuffer();
        while (it.hasNext()) {
            s.append('\"').append((String) it.next()).append('\"');
            if (it.hasNext()) {
                s.append(',');
            }
        }
        return s.toString();
    }

    public static byte[] stringListToUserClass(List<String> list) {
        if (list == null) {
            return null;
        }
        ByteArrayOutputStream buf = new ByteArrayOutputStream(32);
        DataOutputStream out = new DataOutputStream(buf);
        try {
            for (String s : list) {
                byte[] bytes = DHCPPacket.stringToBytes(s);
                int size = bytes.length;
                if (size > 255) {
                    size = 255;
                }
                out.writeByte(size);
                out.write(bytes, 0, size);
            }
            return buf.toByteArray();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unexpected IOException", e);
            return buf.toByteArray();
        }
    }

    public static String agentOptionsToString(byte[] buf) {
        if (buf == null) {
            return null;
        }
        Map<Byte, String> map = agentOptionsToMap(buf);
        StringBuffer s = new StringBuffer();
        for (Entry<Byte, String> entry : map.entrySet()) {
            s.append('{').append(unsignedByte(((Byte) entry.getKey()).byteValue())).append("}\"");
            s.append((String) entry.getValue()).append('\"');
            s.append(',');
        }
        if (s.length() > 0) {
            s.setLength(s.length() - 1);
        }
        return s.toString();
    }

    public static byte[] agentOptionToRaw(Map<Byte, String> map) {
        if (map == null) {
            return null;
        }
        ByteArrayOutputStream buf = new ByteArrayOutputStream(64);
        DataOutputStream out = new DataOutputStream(buf);
        try {
            for (Entry<Byte, String> entry : map.entrySet()) {
                byte[] bufTemp = DHCPPacket.stringToBytes((String) entry.getValue());
                int size = bufTemp.length;
                if (!$assertionsDisabled && size < 0) {
                    throw new AssertionError();
                } else if (size > 255) {
                    throw new IllegalArgumentException("Value size is greater then 255 bytes");
                } else {
                    out.writeByte(((Byte) entry.getKey()).byteValue());
                    out.writeByte(size);
                    out.write(bufTemp, 0, size);
                }
            }
            return buf.toByteArray();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unexpected IOException", e);
            return buf.toByteArray();
        }
    }

    public static final Map<Byte, String> agentOptionsToMap(byte[] buf) {
        if (buf == null) {
            return null;
        }
        Map<Byte, String> map = new LinkedHashMap();
        int i = 0;
        while (i < buf.length && buf.length - i >= 2) {
            int i2 = i + 1;
            Byte key = Byte.valueOf(buf[i]);
            i = i2 + 1;
            int size = unsignedByte(buf[i2]);
            int instock = buf.length - i;
            if (size > instock) {
                size = instock;
            }
            map.put(key, DHCPPacket.bytesToString(buf, i, size));
            i += size;
        }
        return map;
    }

    public static Class getOptionFormat(byte code) {
        OptionFormat format = (OptionFormat) _DHO_FORMATS.get(Byte.valueOf(code));
        if (format == null) {
            return null;
        }
        switch (format) {
            case INET:
                return InetAddress.class;
            case INETS:
                return InetAddress[].class;
            case INT:
                return Integer.TYPE;
            case SHORT:
                return Short.TYPE;
            case SHORTS:
                return short[].class;
            case BYTE:
                return Byte.TYPE;
            case STRING:
                return String.class;
            case BYTES:
                return byte[].class;
            default:
                return null;
        }
    }

    public static Class string2Class(String className) {
        if ("InetAddress".equals(className)) {
            return InetAddress.class;
        }
        if ("inet".equals(className)) {
            return InetAddress.class;
        }
        if ("InetAddress[]".equals(className)) {
            return InetAddress[].class;
        }
        if ("inets".equals(className)) {
            return InetAddress[].class;
        }
        if ("int".equals(className)) {
            return Integer.TYPE;
        }
        if ("short".equals(className)) {
            return Short.TYPE;
        }
        if ("short[]".equals(className)) {
            return short[].class;
        }
        if ("shorts".equals(className)) {
            return short[].class;
        }
        if ("byte".equals(className)) {
            return Byte.TYPE;
        }
        if ("byte[]".equals(className)) {
            return byte[].class;
        }
        if ("bytes".equals(className)) {
            return byte[].class;
        }
        if ("String".equals(className)) {
            return String.class;
        }
        if ("string".equals(className)) {
            return String.class;
        }
        return null;
    }

    public static DHCPOption parseNewOption(byte code, Class format, String value) {
        DHCPOption dHCPOption = null;
        if (format == null || value == null) {
            throw new NullPointerException();
        } else if (Short.TYPE.equals(format)) {
            return newOptionAsShort(code, (short) Integer.parseInt(value));
        } else {
            String[] listVal;
            int i;
            if (short[].class.equals(format)) {
                listVal = value.split(Separators.SP);
                short[] listShort = new short[listVal.length];
                for (i = 0; i < listVal.length; i++) {
                    listShort[i] = (short) Integer.parseInt(listVal[i]);
                }
                return newOptionAsShorts(code, listShort);
            } else if (Integer.TYPE.equals(format)) {
                return newOptionAsInt(code, Integer.parseInt(value));
            } else {
                if (String.class.equals(format)) {
                    return newOptionAsString(code, value);
                }
                if (Byte.TYPE.equals(format)) {
                    return newOptionAsByte(code, (byte) Integer.parseInt(value));
                }
                if (byte[].class.equals(format)) {
                    listVal = value.replace(Separators.DOT, Separators.SP).split(Separators.SP);
                    byte[] listBytes = new byte[listVal.length];
                    for (i = 0; i < listVal.length; i++) {
                        listBytes[i] = (byte) Integer.parseInt(listVal[i]);
                    }
                    return new DHCPOption(code, listBytes);
                } else if (InetAddress.class.equals(format)) {
                    try {
                        return newOptionAsInetAddress(code, InetAddress.getByName(value));
                    } catch (UnknownHostException e) {
                        logger.log(Level.SEVERE, "Invalid address:" + value, e);
                        return dHCPOption;
                    }
                } else if (!InetAddress[].class.equals(format)) {
                    return dHCPOption;
                } else {
                    listVal = value.split(Separators.SP);
                    InetAddress[] listInet = new InetAddress[listVal.length];
                    i = 0;
                    while (i < listVal.length) {
                        try {
                            listInet[i] = InetAddress.getByName(listVal[i]);
                            i++;
                        } catch (UnknownHostException e2) {
                            logger.log(Level.SEVERE, "Invalid address", e2);
                            return dHCPOption;
                        }
                    }
                    return newOptionAsInetAddresses(code, listInet);
                }
            }
        }
    }

    public static void main(String[] args) {
        String all = "";
        String inet1 = "";
        String inets = "";
        String int1 = "";
        String short1 = "";
        String shorts = "";
        String byte1 = "";
        String bytes = "";
        String string1 = "";
        for (Byte codeByte : DHCPConstants._DHO_NAMES.keySet()) {
            byte code = codeByte.byteValue();
            String s = "";
            if (!(code == (byte) 0 || code == (byte) -1)) {
                s = " * " + ((String) DHCPConstants._DHO_NAMES.get(codeByte)) + '(' + (code & 255) + ")\n";
            }
            all = all + s;
            if (_DHO_FORMATS.containsKey(codeByte)) {
                switch ((OptionFormat) _DHO_FORMATS.get(codeByte)) {
                    case INET:
                        inet1 = inet1 + s;
                        break;
                    case INETS:
                        inets = inets + s;
                        break;
                    case INT:
                        int1 = int1 + s;
                        break;
                    case SHORT:
                        short1 = short1 + s;
                        break;
                    case SHORTS:
                        shorts = shorts + s;
                        break;
                    case BYTE:
                        byte1 = byte1 + s;
                        break;
                    case STRING:
                        string1 = string1 + s;
                        break;
                    case BYTES:
                        bytes = bytes + s;
                        break;
                    default:
                        break;
                }
            }
        }
        System.out.println("---All codes---");
        System.out.println(all);
        System.out.println("---INET---");
        System.out.println(inet1);
        System.out.println("---INETS---");
        System.out.println(inets);
        System.out.println("---INT---");
        System.out.println(int1);
        System.out.println("---SHORT---");
        System.out.println(short1);
        System.out.println("---SHORTS---");
        System.out.println(shorts);
        System.out.println("---BYTE---");
        System.out.println(byte1);
        System.out.println("---BYTES---");
        System.out.println(bytes);
        System.out.println("---STRING---");
        System.out.println(string1);
    }
}
