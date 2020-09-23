package org.jivesoftware.smack.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.dhcp4java.DHCPConstants;

public class Base64 {
    public static final int DECODE = 0;
    public static final int DONT_BREAK_LINES = 8;
    public static final int ENCODE = 1;
    private static final byte EQUALS_SIGN = (byte) 61;
    private static final byte EQUALS_SIGN_ENC = (byte) -1;
    public static final int GZIP = 2;
    private static final int MAX_LINE_LENGTH = 76;
    private static final byte NEW_LINE = (byte) 10;
    public static final int NO_OPTIONS = 0;
    public static final int ORDERED = 32;
    private static final String PREFERRED_ENCODING = "UTF-8";
    public static final int URL_SAFE = 16;
    private static final byte WHITE_SPACE_ENC = (byte) -5;
    private static final byte[] _ORDERED_ALPHABET = new byte[]{DHCPConstants.DHO_NETBIOS_DD_SERVER, DHCPConstants.DHO_FONT_SERVERS, DHCPConstants.DHO_X_DISPLAY_MANAGER, DHCPConstants.DHO_DHCP_REQUESTED_ADDRESS, DHCPConstants.DHO_DHCP_LEASE_TIME, DHCPConstants.DHO_DHCP_OPTION_OVERLOAD, DHCPConstants.DHO_DHCP_MESSAGE_TYPE, DHCPConstants.DHO_DHCP_SERVER_IDENTIFIER, DHCPConstants.DHO_DHCP_PARAMETER_REQUEST_LIST, DHCPConstants.DHO_DHCP_MESSAGE, DHCPConstants.DHO_DHCP_MAX_MESSAGE_SIZE, DHCPConstants.DHO_NISPLUS_SERVER, DHCPConstants.DHO_TFTP_SERVER, DHCPConstants.DHO_BOOTFILE, DHCPConstants.DHO_MOBILE_IP_HOME_AGENT, DHCPConstants.DHO_SMTP_SERVER, DHCPConstants.DHO_POP3_SERVER, DHCPConstants.DHO_NNTP_SERVER, DHCPConstants.DHO_WWW_SERVER, DHCPConstants.DHO_FINGER_SERVER, DHCPConstants.DHO_IRC_SERVER, DHCPConstants.DHO_STREETTALK_SERVER, DHCPConstants.DHO_STDA_SERVER, DHCPConstants.DHO_USER_CLASS, (byte) 78, (byte) 79, (byte) 80, DHCPConstants.DHO_FQDN, DHCPConstants.DHO_DHCP_AGENT_OPTIONS, (byte) 83, (byte) 84, DHCPConstants.DHO_NDS_SERVERS, DHCPConstants.DHO_NDS_TREE_NAME, DHCPConstants.DHO_NDS_CONTEXT, (byte) 88, (byte) 89, (byte) 90, (byte) 95, (byte) 97, DHCPConstants.DHO_USER_AUTHENTICATION_PROTOCOL, (byte) 99, (byte) 100, (byte) 101, (byte) 102, (byte) 103, (byte) 104, (byte) 105, (byte) 106, (byte) 107, (byte) 108, (byte) 109, (byte) 110, (byte) 111, (byte) 112, (byte) 113, (byte) 114, (byte) 115, DHCPConstants.DHO_AUTO_CONFIGURE, DHCPConstants.DHO_NAME_SERVICE_SEARCH, DHCPConstants.DHO_SUBNET_SELECTION, DHCPConstants.DHO_DOMAIN_SEARCH, (byte) 120, DHCPConstants.DHO_CLASSLESS_ROUTE, (byte) 122};
    private static final byte[] _ORDERED_DECODABET = new byte[]{(byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, WHITE_SPACE_ENC, WHITE_SPACE_ENC, (byte) -9, (byte) -9, WHITE_SPACE_ENC, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, WHITE_SPACE_ENC, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) 0, (byte) -9, (byte) -9, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8, (byte) 9, (byte) 10, (byte) -9, (byte) -9, (byte) -9, (byte) -1, (byte) -9, (byte) -9, (byte) -9, (byte) 11, (byte) 12, (byte) 13, DHCPConstants.DHO_MERIT_DUMP, DHCPConstants.DHO_DOMAIN_NAME, DHCPConstants.DHO_SWAP_SERVER, DHCPConstants.DHO_ROOT_PATH, DHCPConstants.DHO_EXTENSIONS_PATH, DHCPConstants.DHO_IP_FORWARDING, DHCPConstants.DHO_NON_LOCAL_SOURCE_ROUTING, DHCPConstants.DHO_POLICY_FILTER, DHCPConstants.DHO_MAX_DGRAM_REASSEMBLY, DHCPConstants.DHO_DEFAULT_IP_TTL, (byte) 24, DHCPConstants.DHO_PATH_MTU_PLATEAU_TABLE, DHCPConstants.DHO_INTERFACE_MTU, DHCPConstants.DHO_ALL_SUBNETS_LOCAL, DHCPConstants.DHO_BROADCAST_ADDRESS, DHCPConstants.DHO_PERFORM_MASK_DISCOVERY, DHCPConstants.DHO_MASK_SUPPLIER, DHCPConstants.DHO_ROUTER_DISCOVERY, DHCPConstants.DHO_ROUTER_SOLICITATION_ADDRESS, DHCPConstants.DHO_STATIC_ROUTES, DHCPConstants.DHO_TRAILER_ENCAPSULATION, DHCPConstants.DHO_ARP_CACHE_TIMEOUT, DHCPConstants.DHO_IEEE802_3_ENCAPSULATION, (byte) -9, (byte) -9, (byte) -9, (byte) -9, DHCPConstants.DHO_DEFAULT_TCP_TTL, (byte) -9, DHCPConstants.DHO_TCP_KEEPALIVE_INTERVAL, DHCPConstants.DHO_TCP_KEEPALIVE_GARBAGE, (byte) 40, DHCPConstants.DHO_NIS_SERVERS, DHCPConstants.DHO_NTP_SERVERS, DHCPConstants.DHO_VENDOR_ENCAPSULATED_OPTIONS, DHCPConstants.DHO_NETBIOS_NAME_SERVERS, DHCPConstants.DHO_NETBIOS_DD_SERVER, DHCPConstants.DHO_NETBIOS_NODE_TYPE, DHCPConstants.DHO_NETBIOS_SCOPE, DHCPConstants.DHO_FONT_SERVERS, DHCPConstants.DHO_X_DISPLAY_MANAGER, DHCPConstants.DHO_DHCP_REQUESTED_ADDRESS, DHCPConstants.DHO_DHCP_LEASE_TIME, DHCPConstants.DHO_DHCP_OPTION_OVERLOAD, DHCPConstants.DHO_DHCP_MESSAGE_TYPE, DHCPConstants.DHO_DHCP_SERVER_IDENTIFIER, DHCPConstants.DHO_DHCP_PARAMETER_REQUEST_LIST, DHCPConstants.DHO_DHCP_MESSAGE, DHCPConstants.DHO_DHCP_MAX_MESSAGE_SIZE, DHCPConstants.DHO_DHCP_RENEWAL_TIME, DHCPConstants.DHO_DHCP_REBINDING_TIME, DHCPConstants.DHO_VENDOR_CLASS_IDENTIFIER, (byte) 61, DHCPConstants.DHO_NWIP_DOMAIN_NAME, DHCPConstants.DHO_NWIP_SUBOPTIONS, (byte) -9, (byte) -9, (byte) -9, (byte) -9};
    private static final byte[] _STANDARD_ALPHABET = new byte[]{DHCPConstants.DHO_NISPLUS_SERVER, DHCPConstants.DHO_TFTP_SERVER, DHCPConstants.DHO_BOOTFILE, DHCPConstants.DHO_MOBILE_IP_HOME_AGENT, DHCPConstants.DHO_SMTP_SERVER, DHCPConstants.DHO_POP3_SERVER, DHCPConstants.DHO_NNTP_SERVER, DHCPConstants.DHO_WWW_SERVER, DHCPConstants.DHO_FINGER_SERVER, DHCPConstants.DHO_IRC_SERVER, DHCPConstants.DHO_STREETTALK_SERVER, DHCPConstants.DHO_STDA_SERVER, DHCPConstants.DHO_USER_CLASS, (byte) 78, (byte) 79, (byte) 80, DHCPConstants.DHO_FQDN, DHCPConstants.DHO_DHCP_AGENT_OPTIONS, (byte) 83, (byte) 84, DHCPConstants.DHO_NDS_SERVERS, DHCPConstants.DHO_NDS_TREE_NAME, DHCPConstants.DHO_NDS_CONTEXT, (byte) 88, (byte) 89, (byte) 90, (byte) 97, DHCPConstants.DHO_USER_AUTHENTICATION_PROTOCOL, (byte) 99, (byte) 100, (byte) 101, (byte) 102, (byte) 103, (byte) 104, (byte) 105, (byte) 106, (byte) 107, (byte) 108, (byte) 109, (byte) 110, (byte) 111, (byte) 112, (byte) 113, (byte) 114, (byte) 115, DHCPConstants.DHO_AUTO_CONFIGURE, DHCPConstants.DHO_NAME_SERVICE_SEARCH, DHCPConstants.DHO_SUBNET_SELECTION, DHCPConstants.DHO_DOMAIN_SEARCH, (byte) 120, DHCPConstants.DHO_CLASSLESS_ROUTE, (byte) 122, DHCPConstants.DHO_FONT_SERVERS, DHCPConstants.DHO_X_DISPLAY_MANAGER, DHCPConstants.DHO_DHCP_REQUESTED_ADDRESS, DHCPConstants.DHO_DHCP_LEASE_TIME, DHCPConstants.DHO_DHCP_OPTION_OVERLOAD, DHCPConstants.DHO_DHCP_MESSAGE_TYPE, DHCPConstants.DHO_DHCP_SERVER_IDENTIFIER, DHCPConstants.DHO_DHCP_PARAMETER_REQUEST_LIST, DHCPConstants.DHO_DHCP_MESSAGE, DHCPConstants.DHO_DHCP_MAX_MESSAGE_SIZE, DHCPConstants.DHO_VENDOR_ENCAPSULATED_OPTIONS, DHCPConstants.DHO_NETBIOS_SCOPE};
    private static final byte[] _STANDARD_DECODABET = new byte[]{(byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, WHITE_SPACE_ENC, WHITE_SPACE_ENC, (byte) -9, (byte) -9, WHITE_SPACE_ENC, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, WHITE_SPACE_ENC, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, DHCPConstants.DHO_NWIP_DOMAIN_NAME, (byte) -9, (byte) -9, (byte) -9, DHCPConstants.DHO_NWIP_SUBOPTIONS, DHCPConstants.DHO_DHCP_OPTION_OVERLOAD, DHCPConstants.DHO_DHCP_MESSAGE_TYPE, DHCPConstants.DHO_DHCP_SERVER_IDENTIFIER, DHCPConstants.DHO_DHCP_PARAMETER_REQUEST_LIST, DHCPConstants.DHO_DHCP_MESSAGE, DHCPConstants.DHO_DHCP_MAX_MESSAGE_SIZE, DHCPConstants.DHO_DHCP_RENEWAL_TIME, DHCPConstants.DHO_DHCP_REBINDING_TIME, DHCPConstants.DHO_VENDOR_CLASS_IDENTIFIER, (byte) 61, (byte) -9, (byte) -9, (byte) -9, (byte) -1, (byte) -9, (byte) -9, (byte) -9, (byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8, (byte) 9, (byte) 10, (byte) 11, (byte) 12, (byte) 13, DHCPConstants.DHO_MERIT_DUMP, DHCPConstants.DHO_DOMAIN_NAME, DHCPConstants.DHO_SWAP_SERVER, DHCPConstants.DHO_ROOT_PATH, DHCPConstants.DHO_EXTENSIONS_PATH, DHCPConstants.DHO_IP_FORWARDING, DHCPConstants.DHO_NON_LOCAL_SOURCE_ROUTING, DHCPConstants.DHO_POLICY_FILTER, DHCPConstants.DHO_MAX_DGRAM_REASSEMBLY, DHCPConstants.DHO_DEFAULT_IP_TTL, (byte) 24, DHCPConstants.DHO_PATH_MTU_PLATEAU_TABLE, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, DHCPConstants.DHO_INTERFACE_MTU, DHCPConstants.DHO_ALL_SUBNETS_LOCAL, DHCPConstants.DHO_BROADCAST_ADDRESS, DHCPConstants.DHO_PERFORM_MASK_DISCOVERY, DHCPConstants.DHO_MASK_SUPPLIER, DHCPConstants.DHO_ROUTER_DISCOVERY, DHCPConstants.DHO_ROUTER_SOLICITATION_ADDRESS, DHCPConstants.DHO_STATIC_ROUTES, DHCPConstants.DHO_TRAILER_ENCAPSULATION, DHCPConstants.DHO_ARP_CACHE_TIMEOUT, DHCPConstants.DHO_IEEE802_3_ENCAPSULATION, DHCPConstants.DHO_DEFAULT_TCP_TTL, DHCPConstants.DHO_TCP_KEEPALIVE_INTERVAL, DHCPConstants.DHO_TCP_KEEPALIVE_GARBAGE, (byte) 40, DHCPConstants.DHO_NIS_SERVERS, DHCPConstants.DHO_NTP_SERVERS, DHCPConstants.DHO_VENDOR_ENCAPSULATED_OPTIONS, DHCPConstants.DHO_NETBIOS_NAME_SERVERS, DHCPConstants.DHO_NETBIOS_DD_SERVER, DHCPConstants.DHO_NETBIOS_NODE_TYPE, DHCPConstants.DHO_NETBIOS_SCOPE, DHCPConstants.DHO_FONT_SERVERS, DHCPConstants.DHO_X_DISPLAY_MANAGER, DHCPConstants.DHO_DHCP_REQUESTED_ADDRESS, DHCPConstants.DHO_DHCP_LEASE_TIME, (byte) -9, (byte) -9, (byte) -9, (byte) -9};
    private static final byte[] _URL_SAFE_ALPHABET = new byte[]{DHCPConstants.DHO_NISPLUS_SERVER, DHCPConstants.DHO_TFTP_SERVER, DHCPConstants.DHO_BOOTFILE, DHCPConstants.DHO_MOBILE_IP_HOME_AGENT, DHCPConstants.DHO_SMTP_SERVER, DHCPConstants.DHO_POP3_SERVER, DHCPConstants.DHO_NNTP_SERVER, DHCPConstants.DHO_WWW_SERVER, DHCPConstants.DHO_FINGER_SERVER, DHCPConstants.DHO_IRC_SERVER, DHCPConstants.DHO_STREETTALK_SERVER, DHCPConstants.DHO_STDA_SERVER, DHCPConstants.DHO_USER_CLASS, (byte) 78, (byte) 79, (byte) 80, DHCPConstants.DHO_FQDN, DHCPConstants.DHO_DHCP_AGENT_OPTIONS, (byte) 83, (byte) 84, DHCPConstants.DHO_NDS_SERVERS, DHCPConstants.DHO_NDS_TREE_NAME, DHCPConstants.DHO_NDS_CONTEXT, (byte) 88, (byte) 89, (byte) 90, (byte) 97, DHCPConstants.DHO_USER_AUTHENTICATION_PROTOCOL, (byte) 99, (byte) 100, (byte) 101, (byte) 102, (byte) 103, (byte) 104, (byte) 105, (byte) 106, (byte) 107, (byte) 108, (byte) 109, (byte) 110, (byte) 111, (byte) 112, (byte) 113, (byte) 114, (byte) 115, DHCPConstants.DHO_AUTO_CONFIGURE, DHCPConstants.DHO_NAME_SERVICE_SEARCH, DHCPConstants.DHO_SUBNET_SELECTION, DHCPConstants.DHO_DOMAIN_SEARCH, (byte) 120, DHCPConstants.DHO_CLASSLESS_ROUTE, (byte) 122, DHCPConstants.DHO_FONT_SERVERS, DHCPConstants.DHO_X_DISPLAY_MANAGER, DHCPConstants.DHO_DHCP_REQUESTED_ADDRESS, DHCPConstants.DHO_DHCP_LEASE_TIME, DHCPConstants.DHO_DHCP_OPTION_OVERLOAD, DHCPConstants.DHO_DHCP_MESSAGE_TYPE, DHCPConstants.DHO_DHCP_SERVER_IDENTIFIER, DHCPConstants.DHO_DHCP_PARAMETER_REQUEST_LIST, DHCPConstants.DHO_DHCP_MESSAGE, DHCPConstants.DHO_DHCP_MAX_MESSAGE_SIZE, DHCPConstants.DHO_NETBIOS_DD_SERVER, (byte) 95};
    private static final byte[] _URL_SAFE_DECODABET = new byte[]{(byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, WHITE_SPACE_ENC, WHITE_SPACE_ENC, (byte) -9, (byte) -9, WHITE_SPACE_ENC, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, WHITE_SPACE_ENC, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, (byte) -9, DHCPConstants.DHO_NWIP_DOMAIN_NAME, (byte) -9, (byte) -9, DHCPConstants.DHO_DHCP_OPTION_OVERLOAD, DHCPConstants.DHO_DHCP_MESSAGE_TYPE, DHCPConstants.DHO_DHCP_SERVER_IDENTIFIER, DHCPConstants.DHO_DHCP_PARAMETER_REQUEST_LIST, DHCPConstants.DHO_DHCP_MESSAGE, DHCPConstants.DHO_DHCP_MAX_MESSAGE_SIZE, DHCPConstants.DHO_DHCP_RENEWAL_TIME, DHCPConstants.DHO_DHCP_REBINDING_TIME, DHCPConstants.DHO_VENDOR_CLASS_IDENTIFIER, (byte) 61, (byte) -9, (byte) -9, (byte) -9, (byte) -1, (byte) -9, (byte) -9, (byte) -9, (byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8, (byte) 9, (byte) 10, (byte) 11, (byte) 12, (byte) 13, DHCPConstants.DHO_MERIT_DUMP, DHCPConstants.DHO_DOMAIN_NAME, DHCPConstants.DHO_SWAP_SERVER, DHCPConstants.DHO_ROOT_PATH, DHCPConstants.DHO_EXTENSIONS_PATH, DHCPConstants.DHO_IP_FORWARDING, DHCPConstants.DHO_NON_LOCAL_SOURCE_ROUTING, DHCPConstants.DHO_POLICY_FILTER, DHCPConstants.DHO_MAX_DGRAM_REASSEMBLY, DHCPConstants.DHO_DEFAULT_IP_TTL, (byte) 24, DHCPConstants.DHO_PATH_MTU_PLATEAU_TABLE, (byte) -9, (byte) -9, (byte) -9, (byte) -9, DHCPConstants.DHO_NWIP_SUBOPTIONS, (byte) -9, DHCPConstants.DHO_INTERFACE_MTU, DHCPConstants.DHO_ALL_SUBNETS_LOCAL, DHCPConstants.DHO_BROADCAST_ADDRESS, DHCPConstants.DHO_PERFORM_MASK_DISCOVERY, DHCPConstants.DHO_MASK_SUPPLIER, DHCPConstants.DHO_ROUTER_DISCOVERY, DHCPConstants.DHO_ROUTER_SOLICITATION_ADDRESS, DHCPConstants.DHO_STATIC_ROUTES, DHCPConstants.DHO_TRAILER_ENCAPSULATION, DHCPConstants.DHO_ARP_CACHE_TIMEOUT, DHCPConstants.DHO_IEEE802_3_ENCAPSULATION, DHCPConstants.DHO_DEFAULT_TCP_TTL, DHCPConstants.DHO_TCP_KEEPALIVE_INTERVAL, DHCPConstants.DHO_TCP_KEEPALIVE_GARBAGE, (byte) 40, DHCPConstants.DHO_NIS_SERVERS, DHCPConstants.DHO_NTP_SERVERS, DHCPConstants.DHO_VENDOR_ENCAPSULATED_OPTIONS, DHCPConstants.DHO_NETBIOS_NAME_SERVERS, DHCPConstants.DHO_NETBIOS_DD_SERVER, DHCPConstants.DHO_NETBIOS_NODE_TYPE, DHCPConstants.DHO_NETBIOS_SCOPE, DHCPConstants.DHO_FONT_SERVERS, DHCPConstants.DHO_X_DISPLAY_MANAGER, DHCPConstants.DHO_DHCP_REQUESTED_ADDRESS, DHCPConstants.DHO_DHCP_LEASE_TIME, (byte) -9, (byte) -9, (byte) -9, (byte) -9};

    public static class InputStream extends FilterInputStream {
        private byte[] alphabet;
        private boolean breakLines;
        private byte[] buffer;
        private int bufferLength;
        private byte[] decodabet;
        private boolean encode;
        private int lineLength;
        private int numSigBytes;
        private int options;
        private int position;

        public InputStream(java.io.InputStream in) {
            this(in, 0);
        }

        public InputStream(java.io.InputStream in, int options) {
            boolean z = true;
            super(in);
            this.breakLines = (options & 8) != 8;
            if ((options & 1) != 1) {
                z = false;
            }
            this.encode = z;
            this.bufferLength = this.encode ? 4 : 3;
            this.buffer = new byte[this.bufferLength];
            this.position = -1;
            this.lineLength = 0;
            this.options = options;
            this.alphabet = Base64.getAlphabet(options);
            this.decodabet = Base64.getDecodabet(options);
        }

        public int read() throws IOException {
            int b;
            if (this.position < 0) {
                int i;
                if (this.encode) {
                    byte[] b3 = new byte[3];
                    int numBinaryBytes = 0;
                    for (i = 0; i < 3; i++) {
                        try {
                            b = this.in.read();
                            if (b >= 0) {
                                b3[i] = (byte) b;
                                numBinaryBytes++;
                            }
                        } catch (IOException e) {
                            if (i == 0) {
                                throw e;
                            }
                        }
                    }
                    if (numBinaryBytes <= 0) {
                        return -1;
                    }
                    Base64.encode3to4(b3, 0, numBinaryBytes, this.buffer, 0, this.options);
                    this.position = 0;
                    this.numSigBytes = 4;
                } else {
                    byte[] b4 = new byte[4];
                    i = 0;
                    while (i < 4) {
                        do {
                            b = this.in.read();
                            if (b < 0) {
                                break;
                            }
                        } while (this.decodabet[b & 127] <= Base64.WHITE_SPACE_ENC);
                        if (b < 0) {
                            break;
                        }
                        b4[i] = (byte) b;
                        i++;
                    }
                    if (i == 4) {
                        this.numSigBytes = Base64.decode4to3(b4, 0, this.buffer, 0, this.options);
                        this.position = 0;
                    } else if (i == 0) {
                        return -1;
                    } else {
                        throw new IOException("Improperly padded Base64 input.");
                    }
                }
            }
            if (this.position < 0) {
                throw new IOException("Error in Base64 code reading stream.");
            } else if (this.position >= this.numSigBytes) {
                return -1;
            } else {
                if (this.encode && this.breakLines && this.lineLength >= Base64.MAX_LINE_LENGTH) {
                    this.lineLength = 0;
                    return 10;
                }
                this.lineLength++;
                byte[] bArr = this.buffer;
                int i2 = this.position;
                this.position = i2 + 1;
                b = bArr[i2];
                if (this.position >= this.bufferLength) {
                    this.position = -1;
                }
                return b & 255;
            }
        }

        public int read(byte[] dest, int off, int len) throws IOException {
            int i = 0;
            while (i < len) {
                int b = read();
                if (b >= 0) {
                    dest[off + i] = (byte) b;
                    i++;
                } else if (i == 0) {
                    return -1;
                } else {
                    return i;
                }
            }
            return i;
        }
    }

    public static class OutputStream extends FilterOutputStream {
        private byte[] alphabet;
        private byte[] b4;
        private boolean breakLines;
        private byte[] buffer;
        private int bufferLength;
        private byte[] decodabet;
        private boolean encode;
        private int lineLength;
        private int options;
        private int position;
        private boolean suspendEncoding;

        public OutputStream(java.io.OutputStream out) {
            this(out, 1);
        }

        public OutputStream(java.io.OutputStream out, int options) {
            int i;
            boolean z = true;
            super(out);
            this.breakLines = (options & 8) != 8;
            if ((options & 1) != 1) {
                z = false;
            }
            this.encode = z;
            if (this.encode) {
                i = 3;
            } else {
                i = 4;
            }
            this.bufferLength = i;
            this.buffer = new byte[this.bufferLength];
            this.position = 0;
            this.lineLength = 0;
            this.suspendEncoding = false;
            this.b4 = new byte[4];
            this.options = options;
            this.alphabet = Base64.getAlphabet(options);
            this.decodabet = Base64.getDecodabet(options);
        }

        public void write(int theByte) throws IOException {
            byte[] bArr;
            int i;
            if (this.suspendEncoding) {
                this.out.write(theByte);
            } else if (this.encode) {
                bArr = this.buffer;
                i = this.position;
                this.position = i + 1;
                bArr[i] = (byte) theByte;
                if (this.position >= this.bufferLength) {
                    this.out.write(Base64.encode3to4(this.b4, this.buffer, this.bufferLength, this.options));
                    this.lineLength += 4;
                    if (this.breakLines && this.lineLength >= Base64.MAX_LINE_LENGTH) {
                        this.out.write(10);
                        this.lineLength = 0;
                    }
                    this.position = 0;
                }
            } else if (this.decodabet[theByte & 127] > Base64.WHITE_SPACE_ENC) {
                bArr = this.buffer;
                i = this.position;
                this.position = i + 1;
                bArr[i] = (byte) theByte;
                if (this.position >= this.bufferLength) {
                    this.out.write(this.b4, 0, Base64.decode4to3(this.buffer, 0, this.b4, 0, this.options));
                    this.position = 0;
                }
            } else if (this.decodabet[theByte & 127] != Base64.WHITE_SPACE_ENC) {
                throw new IOException("Invalid character in Base64 data.");
            }
        }

        public void write(byte[] theBytes, int off, int len) throws IOException {
            if (this.suspendEncoding) {
                this.out.write(theBytes, off, len);
                return;
            }
            for (int i = 0; i < len; i++) {
                write(theBytes[off + i]);
            }
        }

        public void flushBase64() throws IOException {
            if (this.position <= 0) {
                return;
            }
            if (this.encode) {
                this.out.write(Base64.encode3to4(this.b4, this.buffer, this.position, this.options));
                this.position = 0;
                return;
            }
            throw new IOException("Base64 input not properly padded.");
        }

        public void close() throws IOException {
            flushBase64();
            super.close();
            this.buffer = null;
            this.out = null;
        }

        public void suspendEncoding() throws IOException {
            flushBase64();
            this.suspendEncoding = true;
        }

        public void resumeEncoding() {
            this.suspendEncoding = false;
        }
    }

    /* access modifiers changed from: private|static|final */
    public static final byte[] getAlphabet(int options) {
        if ((options & 16) == 16) {
            return _URL_SAFE_ALPHABET;
        }
        if ((options & 32) == 32) {
            return _ORDERED_ALPHABET;
        }
        return _STANDARD_ALPHABET;
    }

    /* access modifiers changed from: private|static|final */
    public static final byte[] getDecodabet(int options) {
        if ((options & 16) == 16) {
            return _URL_SAFE_DECODABET;
        }
        if ((options & 32) == 32) {
            return _ORDERED_DECODABET;
        }
        return _STANDARD_DECODABET;
    }

    private Base64() {
    }

    public static final void main(String[] args) {
        if (args.length < 3) {
            usage("Not enough arguments.");
            return;
        }
        String flag = args[0];
        String infile = args[1];
        String outfile = args[2];
        if (flag.equals("-e")) {
            encodeFileToFile(infile, outfile);
        } else if (flag.equals("-d")) {
            decodeFileToFile(infile, outfile);
        } else {
            usage("Unknown flag: " + flag);
        }
    }

    private static final void usage(String msg) {
        System.err.println(msg);
        System.err.println("Usage: java Base64 -e|-d inputfile outputfile");
    }

    /* access modifiers changed from: private|static */
    public static byte[] encode3to4(byte[] b4, byte[] threeBytes, int numSigBytes, int options) {
        encode3to4(threeBytes, 0, numSigBytes, b4, 0, options);
        return b4;
    }

    /* access modifiers changed from: private|static */
    public static byte[] encode3to4(byte[] source, int srcOffset, int numSigBytes, byte[] destination, int destOffset, int options) {
        int i;
        int i2 = 0;
        byte[] ALPHABET = getAlphabet(options);
        if (numSigBytes > 0) {
            i = (source[srcOffset] << 24) >>> 8;
        } else {
            i = 0;
        }
        int i3 = (numSigBytes > 1 ? (source[srcOffset + 1] << 24) >>> 16 : 0) | i;
        if (numSigBytes > 2) {
            i2 = (source[srcOffset + 2] << 24) >>> 24;
        }
        int inBuff = i3 | i2;
        switch (numSigBytes) {
            case 1:
                destination[destOffset] = ALPHABET[inBuff >>> 18];
                destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 63];
                destination[destOffset + 2] = (byte) 61;
                destination[destOffset + 3] = (byte) 61;
                break;
            case 2:
                destination[destOffset] = ALPHABET[inBuff >>> 18];
                destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 63];
                destination[destOffset + 2] = ALPHABET[(inBuff >>> 6) & 63];
                destination[destOffset + 3] = (byte) 61;
                break;
            case 3:
                destination[destOffset] = ALPHABET[inBuff >>> 18];
                destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 63];
                destination[destOffset + 2] = ALPHABET[(inBuff >>> 6) & 63];
                destination[destOffset + 3] = ALPHABET[inBuff & 63];
                break;
        }
        return destination;
    }

    public static String encodeObject(Serializable serializableObject) {
        return encodeObject(serializableObject, 0);
    }

    public static String encodeObject(Serializable serializableObject, int options) {
        IOException e;
        Throwable th;
        ByteArrayOutputStream baos = null;
        java.io.OutputStream b64os = null;
        ObjectOutputStream oos = null;
        GZIPOutputStream gzos = null;
        int gzip = options & 2;
        int dontBreakLines = options & 8;
        try {
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            try {
                java.io.OutputStream b64os2 = new OutputStream(baos2, options | 1);
                if (gzip == 2) {
                    try {
                        GZIPOutputStream gzos2 = new GZIPOutputStream(b64os2);
                        try {
                            gzos = gzos2;
                            oos = new ObjectOutputStream(gzos2);
                        } catch (IOException e2) {
                            e = e2;
                            gzos = gzos2;
                            b64os = b64os2;
                            baos = baos2;
                            try {
                                e.printStackTrace();
                                try {
                                    oos.close();
                                } catch (Exception e3) {
                                }
                                try {
                                    gzos.close();
                                } catch (Exception e4) {
                                }
                                try {
                                    b64os.close();
                                } catch (Exception e5) {
                                }
                                try {
                                    baos.close();
                                    return null;
                                } catch (Exception e6) {
                                    return null;
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                try {
                                    oos.close();
                                } catch (Exception e7) {
                                }
                                try {
                                    gzos.close();
                                } catch (Exception e8) {
                                }
                                try {
                                    b64os.close();
                                } catch (Exception e9) {
                                }
                                try {
                                    baos.close();
                                } catch (Exception e10) {
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            gzos = gzos2;
                            b64os = b64os2;
                            baos = baos2;
                            oos.close();
                            gzos.close();
                            b64os.close();
                            baos.close();
                            throw th;
                        }
                    } catch (IOException e11) {
                        e = e11;
                        b64os = b64os2;
                        baos = baos2;
                        e.printStackTrace();
                        oos.close();
                        gzos.close();
                        b64os.close();
                        baos.close();
                        return null;
                    } catch (Throwable th4) {
                        th = th4;
                        b64os = b64os2;
                        baos = baos2;
                        oos.close();
                        gzos.close();
                        b64os.close();
                        baos.close();
                        throw th;
                    }
                }
                oos = new ObjectOutputStream(b64os2);
                oos.writeObject(serializableObject);
                try {
                    oos.close();
                } catch (Exception e12) {
                }
                try {
                    gzos.close();
                } catch (Exception e13) {
                }
                try {
                    b64os2.close();
                } catch (Exception e14) {
                }
                try {
                    baos2.close();
                } catch (Exception e15) {
                }
                try {
                    b64os = b64os2;
                    baos = baos2;
                    return new String(baos2.toByteArray(), PREFERRED_ENCODING);
                } catch (UnsupportedEncodingException e16) {
                    b64os = b64os2;
                    baos = baos2;
                    return new String(baos2.toByteArray());
                }
            } catch (IOException e17) {
                e = e17;
                baos = baos2;
                e.printStackTrace();
                oos.close();
                gzos.close();
                b64os.close();
                baos.close();
                return null;
            } catch (Throwable th5) {
                th = th5;
                baos = baos2;
                oos.close();
                gzos.close();
                b64os.close();
                baos.close();
                throw th;
            }
        } catch (IOException e18) {
            e = e18;
            e.printStackTrace();
            oos.close();
            gzos.close();
            b64os.close();
            baos.close();
            return null;
        }
    }

    public static String encodeBytes(byte[] source) {
        return encodeBytes(source, 0, source.length, 0);
    }

    public static String encodeBytes(byte[] source, int options) {
        return encodeBytes(source, 0, source.length, options);
    }

    public static String encodeBytes(byte[] source, int off, int len) {
        return encodeBytes(source, off, len, 0);
    }

    public static String encodeBytes(byte[] source, int off, int len, int options) {
        IOException e;
        Throwable th;
        int dontBreakLines = options & 8;
        if ((options & 2) == 2) {
            ByteArrayOutputStream baos = null;
            GZIPOutputStream gzos = null;
            OutputStream b64os = null;
            try {
                OutputStream b64os2;
                GZIPOutputStream gZIPOutputStream;
                ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
                try {
                    b64os2 = new OutputStream(baos2, options | 1);
                    try {
                        gZIPOutputStream = new GZIPOutputStream(b64os2);
                    } catch (IOException e2) {
                        e = e2;
                        b64os = b64os2;
                        baos = baos2;
                        try {
                            e.printStackTrace();
                            try {
                                gzos.close();
                            } catch (Exception e3) {
                            }
                            try {
                                b64os.close();
                            } catch (Exception e4) {
                            }
                            try {
                                baos.close();
                                return null;
                            } catch (Exception e5) {
                                return null;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            try {
                                gzos.close();
                            } catch (Exception e6) {
                            }
                            try {
                                b64os.close();
                            } catch (Exception e7) {
                            }
                            try {
                                baos.close();
                            } catch (Exception e8) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        b64os = b64os2;
                        baos = baos2;
                        gzos.close();
                        b64os.close();
                        baos.close();
                        throw th;
                    }
                } catch (IOException e9) {
                    e = e9;
                    baos = baos2;
                    e.printStackTrace();
                    gzos.close();
                    b64os.close();
                    baos.close();
                    return null;
                } catch (Throwable th4) {
                    th = th4;
                    baos = baos2;
                    gzos.close();
                    b64os.close();
                    baos.close();
                    throw th;
                }
                try {
                    gZIPOutputStream.write(source, off, len);
                    gZIPOutputStream.close();
                    try {
                        gZIPOutputStream.close();
                    } catch (Exception e10) {
                    }
                    try {
                        b64os2.close();
                    } catch (Exception e11) {
                    }
                    try {
                        baos2.close();
                    } catch (Exception e12) {
                    }
                    try {
                        return new String(baos2.toByteArray(), PREFERRED_ENCODING);
                    } catch (UnsupportedEncodingException e13) {
                        return new String(baos2.toByteArray());
                    }
                } catch (IOException e14) {
                    e = e14;
                    b64os = b64os2;
                    gzos = gZIPOutputStream;
                    baos = baos2;
                    e.printStackTrace();
                    gzos.close();
                    b64os.close();
                    baos.close();
                    return null;
                } catch (Throwable th5) {
                    th = th5;
                    b64os = b64os2;
                    gzos = gZIPOutputStream;
                    baos = baos2;
                    gzos.close();
                    b64os.close();
                    baos.close();
                    throw th;
                }
            } catch (IOException e15) {
                e = e15;
                e.printStackTrace();
                gzos.close();
                b64os.close();
                baos.close();
                return null;
            }
        }
        int i;
        boolean breakLines = dontBreakLines == 0;
        int len43 = (len * 4) / 3;
        int i2 = len43 + (len % 3 > 0 ? 4 : 0);
        if (breakLines) {
            i = len43 / MAX_LINE_LENGTH;
        } else {
            i = 0;
        }
        byte[] outBuff = new byte[(i + i2)];
        int d = 0;
        e = null;
        int len2 = len - 2;
        int lineLength = 0;
        while (d < len2) {
            int e16;
            encode3to4(source, d + off, 3, outBuff, e, options);
            lineLength += 4;
            if (breakLines && lineLength == MAX_LINE_LENGTH) {
                outBuff[e + 4] = (byte) 10;
                e16 = e + 1;
                lineLength = 0;
            }
            d += 3;
            e = e16 + 4;
        }
        if (d < len) {
            encode3to4(source, d + off, len - d, outBuff, e, options);
            e += 4;
        }
        try {
            return new String(outBuff, 0, e, PREFERRED_ENCODING);
        } catch (UnsupportedEncodingException e17) {
            return new String(outBuff, 0, e);
        }
    }

    /* access modifiers changed from: private|static */
    public static int decode4to3(byte[] source, int srcOffset, byte[] destination, int destOffset, int options) {
        byte[] DECODABET = getDecodabet(options);
        int outBuff;
        if (source[srcOffset + 2] == (byte) 61) {
            destination[destOffset] = (byte) ((((DECODABET[source[srcOffset]] & 255) << 18) | ((DECODABET[source[srcOffset + 1]] & 255) << 12)) >>> 16);
            return 1;
        } else if (source[srcOffset + 3] == (byte) 61) {
            outBuff = (((DECODABET[source[srcOffset]] & 255) << 18) | ((DECODABET[source[srcOffset + 1]] & 255) << 12)) | ((DECODABET[source[srcOffset + 2]] & 255) << 6);
            destination[destOffset] = (byte) (outBuff >>> 16);
            destination[destOffset + 1] = (byte) (outBuff >>> 8);
            return 2;
        } else {
            try {
                outBuff = ((((DECODABET[source[srcOffset]] & 255) << 18) | ((DECODABET[source[srcOffset + 1]] & 255) << 12)) | ((DECODABET[source[srcOffset + 2]] & 255) << 6)) | (DECODABET[source[srcOffset + 3]] & 255);
                destination[destOffset] = (byte) (outBuff >> 16);
                destination[destOffset + 1] = (byte) (outBuff >> 8);
                destination[destOffset + 2] = (byte) outBuff;
                return 3;
            } catch (Exception e) {
                System.out.println("" + source[srcOffset] + ": " + DECODABET[source[srcOffset]]);
                System.out.println("" + source[srcOffset + 1] + ": " + DECODABET[source[srcOffset + 1]]);
                System.out.println("" + source[srcOffset + 2] + ": " + DECODABET[source[srcOffset + 2]]);
                System.out.println("" + source[srcOffset + 3] + ": " + DECODABET[source[srcOffset + 3]]);
                return -1;
            }
        }
    }

    public static byte[] decode(byte[] source, int off, int len, int options) {
        byte[] DECODABET = getDecodabet(options);
        byte[] outBuff = new byte[((len * 3) / 4)];
        int outBuffPosn = 0;
        byte[] b4 = new byte[4];
        int b4Posn = 0;
        int i = off;
        while (true) {
            int b4Posn2 = b4Posn;
            if (i >= off + len) {
                b4Posn = b4Posn2;
                break;
            }
            byte sbiCrop = (byte) (source[i] & 127);
            byte sbiDecode = DECODABET[sbiCrop];
            if (sbiDecode >= WHITE_SPACE_ENC) {
                if (sbiDecode >= (byte) -1) {
                    b4Posn = b4Posn2 + 1;
                    b4[b4Posn2] = sbiCrop;
                    if (b4Posn > 3) {
                        outBuffPosn += decode4to3(b4, 0, outBuff, outBuffPosn, options);
                        b4Posn = 0;
                        if (sbiCrop == (byte) 61) {
                            break;
                        }
                    } else {
                        continue;
                    }
                } else {
                    b4Posn = b4Posn2;
                }
                i++;
            } else {
                System.err.println("Bad Base64 input character at " + i + ": " + source[i] + "(decimal)");
                b4Posn = b4Posn2;
                return null;
            }
        }
        byte[] out = new byte[outBuffPosn];
        System.arraycopy(outBuff, 0, out, 0, outBuffPosn);
        return out;
    }

    public static byte[] decode(String s) {
        return decode(s, 0);
    }

    public static byte[] decode(String s, int options) {
        byte[] bytes;
        Throwable th;
        try {
            bytes = s.getBytes(PREFERRED_ENCODING);
        } catch (UnsupportedEncodingException e) {
            bytes = s.getBytes();
        }
        bytes = decode(bytes, 0, bytes.length, options);
        if (bytes != null && bytes.length >= 4 && 35615 == ((bytes[0] & 255) | ((bytes[1] << 8) & 65280))) {
            ByteArrayInputStream bais = null;
            GZIPInputStream gzis = null;
            ByteArrayOutputStream baos = null;
            byte[] buffer = new byte[2048];
            try {
                ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
                try {
                    ByteArrayInputStream bais2 = new ByteArrayInputStream(bytes);
                    try {
                        GZIPInputStream gzis2 = new GZIPInputStream(bais2);
                        while (true) {
                            try {
                                int length = gzis2.read(buffer);
                                if (length < 0) {
                                    break;
                                }
                                baos2.write(buffer, 0, length);
                            } catch (IOException e2) {
                                baos = baos2;
                                gzis = gzis2;
                                bais = bais2;
                            } catch (Throwable th2) {
                                th = th2;
                                baos = baos2;
                                gzis = gzis2;
                                bais = bais2;
                                try {
                                    baos.close();
                                } catch (Exception e3) {
                                }
                                try {
                                    gzis.close();
                                } catch (Exception e4) {
                                }
                                try {
                                    bais.close();
                                } catch (Exception e5) {
                                }
                                throw th;
                            }
                        }
                        bytes = baos2.toByteArray();
                        try {
                            baos2.close();
                        } catch (Exception e6) {
                        }
                        try {
                            gzis2.close();
                        } catch (Exception e7) {
                        }
                        try {
                            bais2.close();
                        } catch (Exception e8) {
                        }
                    } catch (IOException e9) {
                        baos = baos2;
                        bais = bais2;
                        try {
                            baos.close();
                        } catch (Exception e10) {
                        }
                        try {
                            gzis.close();
                        } catch (Exception e11) {
                        }
                        try {
                            bais.close();
                        } catch (Exception e12) {
                        }
                        return bytes;
                    } catch (Throwable th3) {
                        th = th3;
                        baos = baos2;
                        bais = bais2;
                        baos.close();
                        gzis.close();
                        bais.close();
                        throw th;
                    }
                } catch (IOException e13) {
                    baos = baos2;
                    baos.close();
                    gzis.close();
                    bais.close();
                    return bytes;
                } catch (Throwable th4) {
                    th = th4;
                    baos = baos2;
                    baos.close();
                    gzis.close();
                    bais.close();
                    throw th;
                }
            } catch (IOException e14) {
                baos.close();
                gzis.close();
                bais.close();
                return bytes;
            } catch (Throwable th5) {
                th = th5;
                baos.close();
                gzis.close();
                bais.close();
                throw th;
            }
        }
        return bytes;
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:25:0x0031=Splitter:B:25:0x0031, B:16:0x0023=Splitter:B:16:0x0023} */
    public static java.lang.Object decodeToObject(java.lang.String r9) {
        /*
        r4 = decode(r9);
        r0 = 0;
        r5 = 0;
        r3 = 0;
        r1 = new java.io.ByteArrayInputStream;	 Catch:{ IOException -> 0x0022, ClassNotFoundException -> 0x0030 }
        r1.<init>(r4);	 Catch:{ IOException -> 0x0022, ClassNotFoundException -> 0x0030 }
        r6 = new java.io.ObjectInputStream;	 Catch:{ IOException -> 0x005e, ClassNotFoundException -> 0x0057, all -> 0x0050 }
        r6.<init>(r1);	 Catch:{ IOException -> 0x005e, ClassNotFoundException -> 0x0057, all -> 0x0050 }
        r3 = r6.readObject();	 Catch:{ IOException -> 0x0061, ClassNotFoundException -> 0x005a, all -> 0x0053 }
        r1.close();	 Catch:{ Exception -> 0x0046 }
    L_0x0018:
        r6.close();	 Catch:{ Exception -> 0x001e }
        r5 = r6;
        r0 = r1;
    L_0x001d:
        return r3;
    L_0x001e:
        r7 = move-exception;
        r5 = r6;
        r0 = r1;
        goto L_0x001d;
    L_0x0022:
        r2 = move-exception;
    L_0x0023:
        r2.printStackTrace();	 Catch:{ all -> 0x003e }
        r3 = 0;
        r0.close();	 Catch:{ Exception -> 0x0048 }
    L_0x002a:
        r5.close();	 Catch:{ Exception -> 0x002e }
        goto L_0x001d;
    L_0x002e:
        r7 = move-exception;
        goto L_0x001d;
    L_0x0030:
        r2 = move-exception;
    L_0x0031:
        r2.printStackTrace();	 Catch:{ all -> 0x003e }
        r3 = 0;
        r0.close();	 Catch:{ Exception -> 0x004a }
    L_0x0038:
        r5.close();	 Catch:{ Exception -> 0x003c }
        goto L_0x001d;
    L_0x003c:
        r7 = move-exception;
        goto L_0x001d;
    L_0x003e:
        r7 = move-exception;
    L_0x003f:
        r0.close();	 Catch:{ Exception -> 0x004c }
    L_0x0042:
        r5.close();	 Catch:{ Exception -> 0x004e }
    L_0x0045:
        throw r7;
    L_0x0046:
        r7 = move-exception;
        goto L_0x0018;
    L_0x0048:
        r7 = move-exception;
        goto L_0x002a;
    L_0x004a:
        r7 = move-exception;
        goto L_0x0038;
    L_0x004c:
        r8 = move-exception;
        goto L_0x0042;
    L_0x004e:
        r8 = move-exception;
        goto L_0x0045;
    L_0x0050:
        r7 = move-exception;
        r0 = r1;
        goto L_0x003f;
    L_0x0053:
        r7 = move-exception;
        r5 = r6;
        r0 = r1;
        goto L_0x003f;
    L_0x0057:
        r2 = move-exception;
        r0 = r1;
        goto L_0x0031;
    L_0x005a:
        r2 = move-exception;
        r5 = r6;
        r0 = r1;
        goto L_0x0031;
    L_0x005e:
        r2 = move-exception;
        r0 = r1;
        goto L_0x0023;
    L_0x0061:
        r2 = move-exception;
        r5 = r6;
        r0 = r1;
        goto L_0x0023;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jivesoftware.smack.util.Base64.decodeToObject(java.lang.String):java.lang.Object");
    }

    public static boolean encodeToFile(byte[] dataToEncode, String filename) {
        boolean success;
        Throwable th;
        OutputStream bos = null;
        try {
            OutputStream bos2 = new OutputStream(new FileOutputStream(filename), 1);
            try {
                bos2.write(dataToEncode);
                success = true;
                try {
                    bos2.close();
                    bos = bos2;
                } catch (Exception e) {
                    bos = bos2;
                }
            } catch (IOException e2) {
                bos = bos2;
                success = false;
                try {
                    bos.close();
                } catch (Exception e3) {
                }
                return success;
            } catch (Throwable th2) {
                th = th2;
                bos = bos2;
                try {
                    bos.close();
                } catch (Exception e4) {
                }
                throw th;
            }
        } catch (IOException e5) {
            success = false;
            bos.close();
            return success;
        } catch (Throwable th3) {
            th = th3;
            bos.close();
            throw th;
        }
        return success;
    }

    public static boolean decodeToFile(String dataToDecode, String filename) {
        boolean success;
        Throwable th;
        OutputStream bos = null;
        try {
            OutputStream bos2 = new OutputStream(new FileOutputStream(filename), 0);
            try {
                bos2.write(dataToDecode.getBytes(PREFERRED_ENCODING));
                success = true;
                try {
                    bos2.close();
                    bos = bos2;
                } catch (Exception e) {
                    bos = bos2;
                }
            } catch (IOException e2) {
                bos = bos2;
                success = false;
                try {
                    bos.close();
                } catch (Exception e3) {
                }
                return success;
            } catch (Throwable th2) {
                th = th2;
                bos = bos2;
                try {
                    bos.close();
                } catch (Exception e4) {
                }
                throw th;
            }
        } catch (IOException e5) {
            success = false;
            bos.close();
            return success;
        } catch (Throwable th3) {
            th = th3;
            bos.close();
            throw th;
        }
        return success;
    }

    public static byte[] decodeFromFile(String filename) {
        Throwable th;
        byte[] decodedData = null;
        InputStream bis = null;
        try {
            File file = new File(filename);
            int length = 0;
            if (file.length() > 2147483647L) {
                System.err.println("File is too big for this convenience method (" + file.length() + " bytes).");
                try {
                    bis.close();
                    return null;
                } catch (Exception e) {
                    return null;
                }
            }
            byte[] buffer = new byte[((int) file.length())];
            InputStream bis2 = new InputStream(new BufferedInputStream(new FileInputStream(file)), 0);
            while (true) {
                try {
                    int numBytes = bis2.read(buffer, length, 4096);
                    if (numBytes < 0) {
                        break;
                    }
                    length += numBytes;
                } catch (IOException e2) {
                    bis = bis2;
                    try {
                        System.err.println("Error decoding from file " + filename);
                        try {
                            bis.close();
                        } catch (Exception e3) {
                        }
                        return decodedData;
                    } catch (Throwable th2) {
                        th = th2;
                        try {
                            bis.close();
                        } catch (Exception e4) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    bis = bis2;
                    bis.close();
                    throw th;
                }
            }
            decodedData = new byte[length];
            System.arraycopy(buffer, 0, decodedData, 0, length);
            try {
                bis2.close();
                bis = bis2;
            } catch (Exception e5) {
                bis = bis2;
            }
            return decodedData;
        } catch (IOException e6) {
            System.err.println("Error decoding from file " + filename);
            bis.close();
            return decodedData;
        }
    }

    public static String encodeFromFile(String filename) {
        Throwable th;
        InputStream bis = null;
        try {
            File file = new File(filename);
            byte[] buffer = new byte[Math.max((int) (((double) file.length()) * 1.4d), 40)];
            int length = 0;
            InputStream bis2 = new InputStream(new BufferedInputStream(new FileInputStream(file)), 1);
            while (true) {
                try {
                    int numBytes = bis2.read(buffer, length, 4096);
                    if (numBytes >= 0) {
                        length += numBytes;
                    } else {
                        String encodedData = new String(buffer, 0, length, PREFERRED_ENCODING);
                        try {
                            bis2.close();
                            bis = bis2;
                            return encodedData;
                        } catch (Exception e) {
                            bis = bis2;
                            return encodedData;
                        }
                    }
                } catch (IOException e2) {
                    bis = bis2;
                    try {
                        System.err.println("Error encoding from file " + filename);
                        try {
                            bis.close();
                            return null;
                        } catch (Exception e3) {
                            return null;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        try {
                            bis.close();
                        } catch (Exception e4) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    bis = bis2;
                    bis.close();
                    throw th;
                }
            }
        } catch (IOException e5) {
            System.err.println("Error encoding from file " + filename);
            bis.close();
            return null;
        }
    }

    public static void encodeFileToFile(String infile, String outfile) {
        IOException ex;
        Throwable th;
        String encoded = encodeFromFile(infile);
        java.io.OutputStream out = null;
        try {
            java.io.OutputStream out2 = new BufferedOutputStream(new FileOutputStream(outfile));
            try {
                out2.write(encoded.getBytes("US-ASCII"));
                try {
                    out2.close();
                    out = out2;
                } catch (Exception e) {
                    out = out2;
                }
            } catch (IOException e2) {
                ex = e2;
                out = out2;
                try {
                    ex.printStackTrace();
                    try {
                        out.close();
                    } catch (Exception e3) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    try {
                        out.close();
                    } catch (Exception e4) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                out = out2;
                out.close();
                throw th;
            }
        } catch (IOException e5) {
            ex = e5;
            ex.printStackTrace();
            out.close();
        }
    }

    public static void decodeFileToFile(String infile, String outfile) {
        IOException ex;
        Throwable th;
        byte[] decoded = decodeFromFile(infile);
        java.io.OutputStream out = null;
        try {
            java.io.OutputStream out2 = new BufferedOutputStream(new FileOutputStream(outfile));
            try {
                out2.write(decoded);
                try {
                    out2.close();
                    out = out2;
                } catch (Exception e) {
                    out = out2;
                }
            } catch (IOException e2) {
                ex = e2;
                out = out2;
                try {
                    ex.printStackTrace();
                    try {
                        out.close();
                    } catch (Exception e3) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    try {
                        out.close();
                    } catch (Exception e4) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                out = out2;
                out.close();
                throw th;
            }
        } catch (IOException e5) {
            ex = e5;
            ex.printStackTrace();
            out.close();
        }
    }
}
