package org.dhcp4java;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

public final class DHCPResponseFactory {
    private static final Logger logger = Logger.getLogger(DHCPResponseFactory.class.getName().toLowerCase());

    private DHCPResponseFactory() {
        throw new UnsupportedOperationException();
    }

    public static final DHCPPacket makeDHCPOffer(DHCPPacket request, InetAddress offeredAddress, int leaseTime, InetAddress serverIdentifier, String message, DHCPOption[] options) {
        if (request == null) {
            throw new NullPointerException("request is null");
        } else if (request.isDhcp()) {
            Byte requestMessageType = request.getDHCPMessageType();
            if (requestMessageType == null) {
                throw new DHCPBadPacketException("request has no message type");
            } else if (requestMessageType.byteValue() != (byte) 1) {
                throw new DHCPBadPacketException("request is not DHCPDISCOVER");
            } else if (offeredAddress == null) {
                throw new IllegalArgumentException("offeredAddress must not be null");
            } else if (offeredAddress instanceof Inet4Address) {
                DHCPPacket resp = new DHCPPacket();
                resp.setOp((byte) 2);
                resp.setHtype(request.getHtype());
                resp.setHlen(request.getHlen());
                resp.setXid(request.getXid());
                resp.setFlags(request.getFlags());
                resp.setYiaddr(offeredAddress);
                resp.setGiaddrRaw(request.getGiaddrRaw());
                resp.setChaddr(request.getChaddr());
                resp.setDHCPMessageType((byte) 2);
                resp.setOptionAsInt(DHCPConstants.DHO_DHCP_LEASE_TIME, leaseTime);
                resp.setOptionAsInetAddress((byte) DHCPConstants.DHO_DHCP_SERVER_IDENTIFIER, serverIdentifier);
                resp.setOptionAsString(DHCPConstants.DHO_DHCP_MESSAGE, message);
                if (options != null) {
                    for (DHCPOption opt : options) {
                        resp.setOption(opt.applyOption(request));
                    }
                }
                resp.setAddrPort(getDefaultSocketAddress(request, (byte) 2));
                return resp;
            } else {
                throw new IllegalArgumentException("offeredAddress must be IPv4");
            }
        } else {
            throw new DHCPBadPacketException("request is BOOTP");
        }
    }

    public static final DHCPPacket makeDHCPAck(DHCPPacket request, InetAddress offeredAddress, int leaseTime, InetAddress serverIdentifier, String message, DHCPOption[] options) {
        if (request == null) {
            throw new NullPointerException("request is null");
        } else if (request.isDhcp()) {
            Byte requestMessageType = request.getDHCPMessageType();
            if (requestMessageType == null) {
                throw new DHCPBadPacketException("request has no message type");
            } else if (requestMessageType.byteValue() != (byte) 3 && requestMessageType.byteValue() != (byte) 8) {
                throw new DHCPBadPacketException("request is not DHCPREQUEST/DHCPINFORM");
            } else if (offeredAddress == null) {
                throw new IllegalArgumentException("offeredAddress must not be null");
            } else if (offeredAddress instanceof Inet4Address) {
                DHCPPacket resp = new DHCPPacket();
                resp.setOp((byte) 2);
                resp.setHtype(request.getHtype());
                resp.setHlen(request.getHlen());
                resp.setXid(request.getXid());
                resp.setFlags(request.getFlags());
                resp.setCiaddrRaw(request.getCiaddrRaw());
                if (requestMessageType.byteValue() != (byte) 8) {
                    resp.setYiaddr(offeredAddress);
                }
                resp.setGiaddrRaw(request.getGiaddrRaw());
                resp.setChaddr(request.getChaddr());
                resp.setDHCPMessageType((byte) 5);
                if (requestMessageType.byteValue() == (byte) 3) {
                    resp.setOptionAsInt(DHCPConstants.DHO_DHCP_LEASE_TIME, leaseTime);
                }
                resp.setOptionAsInetAddress((byte) DHCPConstants.DHO_DHCP_SERVER_IDENTIFIER, serverIdentifier);
                resp.setOptionAsString(DHCPConstants.DHO_DHCP_MESSAGE, message);
                if (options != null) {
                    for (DHCPOption opt : options) {
                        resp.setOption(opt.applyOption(request));
                    }
                }
                resp.setAddrPort(getDefaultSocketAddress(request, (byte) 5));
                return resp;
            } else {
                throw new IllegalArgumentException("offeredAddress must be IPv4");
            }
        } else {
            throw new DHCPBadPacketException("request is BOOTP");
        }
    }

    public static final DHCPPacket makeDHCPNak(DHCPPacket request, InetAddress serverIdentifier, String message) {
        if (request == null) {
            throw new NullPointerException("request is null");
        } else if (request.isDhcp()) {
            Byte requestMessageType = request.getDHCPMessageType();
            if (requestMessageType == null) {
                throw new DHCPBadPacketException("request has no message type");
            } else if (requestMessageType.byteValue() != (byte) 3) {
                throw new DHCPBadPacketException("request is not DHCPREQUEST");
            } else {
                DHCPPacket resp = new DHCPPacket();
                resp.setOp((byte) 2);
                resp.setHtype(request.getHtype());
                resp.setHlen(request.getHlen());
                resp.setXid(request.getXid());
                resp.setFlags(request.getFlags());
                resp.setGiaddrRaw(request.getGiaddrRaw());
                resp.setChaddr(request.getChaddr());
                resp.setDHCPMessageType((byte) 6);
                resp.setOptionAsInetAddress((byte) DHCPConstants.DHO_DHCP_SERVER_IDENTIFIER, serverIdentifier);
                resp.setOptionAsString(DHCPConstants.DHO_DHCP_MESSAGE, message);
                resp.setAddrPort(getDefaultSocketAddress(request, (byte) 6));
                return resp;
            }
        } else {
            throw new DHCPBadPacketException("request is BOOTP");
        }
    }

    public static InetSocketAddress getDefaultSocketAddress(DHCPPacket request, byte responseType) {
        if (request == null) {
            throw new IllegalArgumentException("request is null");
        }
        InetAddress giaddr = request.getGiaddr();
        InetAddress ciaddr = request.getCiaddr();
        switch (responseType) {
            case (byte) 2:
            case (byte) 5:
                if (!DHCPConstants.INADDR_ANY.equals(giaddr)) {
                    return new InetSocketAddress(giaddr, 67);
                }
                if (DHCPConstants.INADDR_ANY.equals(ciaddr)) {
                    return new InetSocketAddress(DHCPConstants.INADDR_BROADCAST, 68);
                }
                return new InetSocketAddress(ciaddr, 68);
            case (byte) 6:
                if (DHCPConstants.INADDR_ANY.equals(giaddr)) {
                    return new InetSocketAddress(DHCPConstants.INADDR_BROADCAST, 68);
                }
                return new InetSocketAddress(giaddr, 67);
            default:
                throw new IllegalArgumentException("responseType not valid");
        }
    }
}
