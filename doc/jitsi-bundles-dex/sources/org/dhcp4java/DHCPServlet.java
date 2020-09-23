package org.dhcp4java;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DHCPServlet {
    private static final Logger logger = Logger.getLogger(DHCPServlet.class.getName().toLowerCase());
    protected DHCPCoreServer server = null;

    public void init(Properties props) {
    }

    public DatagramPacket serviceDatagram(DatagramPacket requestDatagram) {
        if (requestDatagram == null) {
            return null;
        }
        try {
            DHCPPacket request = DHCPPacket.getPacket(requestDatagram);
            if (request == null) {
                return null;
            }
            if (logger.isLoggable(Level.FINER)) {
                logger.finer(request.toString());
            }
            DHCPPacket response = service(request);
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("service() done");
            }
            if (response == null) {
                return null;
            }
            InetAddress address = response.getAddress();
            if (address == null) {
                logger.warning("Address needed in response");
                return null;
            }
            int port = response.getPort();
            byte[] responseBuf = response.serialize();
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("Buffer is " + responseBuf.length + " bytes long");
            }
            DatagramPacket responseDatagram = new DatagramPacket(responseBuf, responseBuf.length, address, port);
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("Sending back to" + address.getHostAddress() + '(' + port + ')');
            }
            postProcess(requestDatagram, responseDatagram);
            return responseDatagram;
        } catch (DHCPBadPacketException e) {
            logger.log(Level.INFO, "Invalid DHCP packet received", e);
            return null;
        } catch (Exception e2) {
            logger.log(Level.INFO, "Unexpected Exception", e2);
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public DHCPPacket service(DHCPPacket request) {
        if (request == null) {
            return null;
        }
        if (request.isDhcp()) {
            Byte dhcpMessageType = request.getDHCPMessageType();
            if (dhcpMessageType == null) {
                logger.info("no DHCP message type");
                return null;
            } else if (request.getOp() == (byte) 1) {
                switch (dhcpMessageType.byteValue()) {
                    case (byte) 1:
                        return doDiscover(request);
                    case (byte) 3:
                        return doRequest(request);
                    case (byte) 4:
                        return doDecline(request);
                    case (byte) 7:
                        return doRelease(request);
                    case (byte) 8:
                        return doInform(request);
                    default:
                        logger.info("Unsupported message type " + dhcpMessageType);
                        return null;
                }
            } else if (request.getOp() == (byte) 2) {
                logger.info("BOOTREPLY received from client");
                return null;
            } else {
                logger.warning("Unknown Op: " + request.getOp());
                return null;
            }
        }
        logger.info("BOOTP packet rejected");
        return null;
    }

    /* access modifiers changed from: protected */
    public DHCPPacket doDiscover(DHCPPacket request) {
        logger.fine("DISCOVER packet received");
        return null;
    }

    /* access modifiers changed from: protected */
    public DHCPPacket doRequest(DHCPPacket request) {
        logger.fine("REQUEST packet received");
        return null;
    }

    /* access modifiers changed from: protected */
    public DHCPPacket doInform(DHCPPacket request) {
        logger.fine("INFORM packet received");
        return null;
    }

    /* access modifiers changed from: protected */
    public DHCPPacket doDecline(DHCPPacket request) {
        logger.fine("DECLINE packet received");
        return null;
    }

    /* access modifiers changed from: protected */
    public DHCPPacket doRelease(DHCPPacket request) {
        logger.fine("RELEASE packet received");
        return null;
    }

    /* access modifiers changed from: protected */
    public void postProcess(DatagramPacket requestDatagram, DatagramPacket responseDatagram) {
    }

    public DHCPCoreServer getServer() {
        return this.server;
    }

    public void setServer(DHCPCoreServer server) {
        this.server = server;
    }
}
