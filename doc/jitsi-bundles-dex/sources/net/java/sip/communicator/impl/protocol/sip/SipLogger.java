package net.java.sip.communicator.impl.protocol.sip;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Properties;
import net.java.sip.communicator.util.Logger;
import org.dhcp4java.DHCPConstants;
import org.jitsi.gov.nist.core.ServerLogger;
import org.jitsi.gov.nist.core.StackLogger;
import org.jitsi.gov.nist.javax.sip.SipStackImpl;
import org.jitsi.gov.nist.javax.sip.message.SIPMessage;
import org.jitsi.gov.nist.javax.sip.message.SIPRequest;
import org.jitsi.javax.sip.ListeningPoint;
import org.jitsi.javax.sip.SipStack;
import org.jitsi.service.packetlogging.PacketLoggingService;
import org.jitsi.service.packetlogging.PacketLoggingService.ProtocolName;
import org.jitsi.service.packetlogging.PacketLoggingService.TransportName;

public class SipLogger implements StackLogger, ServerLogger {
    private static final Logger logger = Logger.getLogger(SipLogger.class);
    private SipStack sipStack;

    public void logStackTrace() {
        if (logger.isTraceEnabled()) {
            logger.trace("JAIN-SIP stack trace", new Throwable());
        }
    }

    public void logStackTrace(int traceLevel) {
        if (logger.isTraceEnabled()) {
            logger.trace("JAIN-SIP stack trace", new Throwable());
        }
    }

    public int getLineCount() {
        return 0;
    }

    public void logException(Throwable ex) {
        logger.warn("Exception in the JAIN-SIP stack: " + ex.getMessage());
        if (logger.isInfoEnabled()) {
            logger.info("JAIN-SIP exception stack trace is", ex);
        }
    }

    public void logDebug(String message) {
        if (logger.isDebugEnabled()) {
            logger.debug("Debug output from the JAIN-SIP stack: " + message);
        }
    }

    public void logFatalError(String message) {
        if (logger.isTraceEnabled()) {
            logger.trace("Fatal error from the JAIN-SIP stack: " + message);
        }
    }

    public void logError(String message) {
        logger.error("Error from the JAIN-SIP stack: " + message);
    }

    public boolean isLoggingEnabled() {
        return true;
    }

    public boolean isLoggingEnabled(int logLevel) {
        if (logLevel == 32) {
            return logger.isDebugEnabled();
        }
        if (logLevel == 16 || logLevel != 0) {
            return true;
        }
        return false;
    }

    public void logError(String message, Exception ex) {
        logger.error("Error from the JAIN-SIP stack: " + message, ex);
    }

    public void logWarning(String string) {
        logger.warn("Warning from the JAIN-SIP stack" + string);
    }

    public void logInfo(String string) {
        if (logger.isInfoEnabled()) {
            logger.info("Info from the JAIN-SIP stack: " + string);
        }
    }

    public void disableLogging() {
    }

    public void enableLogging() {
    }

    public void setBuildTimeStamp(String buildTimeStamp) {
        if (logger.isTraceEnabled()) {
            logger.trace("JAIN-SIP RI build " + buildTimeStamp);
        }
    }

    public void setStackProperties(Properties stackProperties) {
    }

    public void closeLogFile() {
    }

    public void logMessage(SIPMessage message, String from, String to, boolean sender, long time) {
        logMessage(message, from, to, null, sender, time);
    }

    public void logMessage(SIPMessage message, String from, String to, String status, boolean sender, long time) {
        try {
            logPacket(message, sender);
        } catch (Throwable e) {
            logger.error("Error logging packet", e);
        }
    }

    private void logPacket(SIPMessage message, boolean sender) {
        try {
            PacketLoggingService packetLogging = SipActivator.getPacketLogging();
            if (packetLogging != null && packetLogging.isLoggingEnabled(ProtocolName.SIP) && message.getTopmostVia() != null) {
                int srcPort;
                byte[] srcAddr;
                int dstPort;
                byte[] dstAddr;
                String transport = message.getTopmostVia().getTransport();
                boolean isTransportUDP = transport.equalsIgnoreCase(ListeningPoint.UDP);
                if (sender) {
                    if (isTransportUDP) {
                        srcPort = message.getLocalPort();
                        if (message.getLocalAddress() != null) {
                            srcAddr = message.getLocalAddress().getAddress();
                        } else if (message.getRemoteAddress() != null) {
                            srcAddr = new byte[message.getRemoteAddress().getAddress().length];
                        } else {
                            srcAddr = new byte[4];
                        }
                    } else {
                        InetSocketAddress localAddress = getLocalAddressForDestination(message.getRemoteAddress(), message.getRemotePort(), message.getLocalAddress(), transport);
                        srcPort = localAddress.getPort();
                        srcAddr = localAddress.getAddress().getAddress();
                    }
                    dstPort = message.getRemotePort();
                    if (message.getRemoteAddress() != null) {
                        dstAddr = message.getRemoteAddress().getAddress();
                    } else {
                        dstAddr = new byte[srcAddr.length];
                    }
                } else {
                    if (isTransportUDP) {
                        dstPort = message.getLocalPort();
                        if (message.getLocalAddress() != null) {
                            dstAddr = message.getLocalAddress().getAddress();
                        } else if (message.getRemoteAddress() != null) {
                            dstAddr = new byte[message.getRemoteAddress().getAddress().length];
                        } else {
                            dstAddr = new byte[4];
                        }
                    } else {
                        InetSocketAddress dstAddress = getLocalAddressForDestination(message.getRemoteAddress(), message.getRemotePort(), message.getLocalAddress(), transport);
                        dstPort = dstAddress.getPort();
                        dstAddr = dstAddress.getAddress().getAddress();
                    }
                    srcPort = message.getRemotePort();
                    if (message.getRemoteAddress() != null) {
                        srcAddr = message.getRemoteAddress().getAddress();
                    } else {
                        srcAddr = new byte[dstAddr.length];
                    }
                }
                byte[] msg = null;
                if (message instanceof SIPRequest) {
                    SIPRequest req = (SIPRequest) message;
                    if (req.getMethod().equals("MESSAGE") && message.getContentTypeHeader() != null && message.getContentTypeHeader().getContentType().equalsIgnoreCase("text")) {
                        int len = req.getContentLength().getContentLength();
                        if (len > 0) {
                            SIPRequest newReq = (SIPRequest) req.clone();
                            byte[] newContent = new byte[len];
                            Arrays.fill(newContent, DHCPConstants.DHO_NETBIOS_NODE_TYPE);
                            newReq.setMessageContent(newContent);
                            msg = newReq.toString().getBytes("UTF-8");
                        }
                    }
                }
                if (msg == null) {
                    msg = message.toString().getBytes("UTF-8");
                }
                packetLogging.logPacket(ProtocolName.SIP, srcAddr, srcPort, dstAddr, dstPort, isTransportUDP ? TransportName.UDP : TransportName.TCP, sender, msg);
            }
        } catch (Throwable e) {
            logger.error("Cannot obtain message body", e);
        }
    }

    public void logMessage(SIPMessage message, String from, String to, String status, boolean sender) {
        if (logger.isInfoEnabled()) {
            String msgHeader;
            if (sender) {
                msgHeader = "JAIN-SIP sent a message from=\"";
            } else {
                msgHeader = "JAIN-SIP received a message from=\"";
            }
            if (logger.isInfoEnabled()) {
                logger.info(msgHeader + from + "\" to=\"" + to + "\" (status: " + status + "):\n" + message);
            }
        }
    }

    public void logException(Exception exception) {
        logger.warn("the following exception occured in JAIN-SIP: " + exception, exception);
    }

    public void setSipStack(SipStack sipStack) {
        this.sipStack = sipStack;
    }

    public String getLoggerName() {
        return "SIP Communicator JAIN SIP logger.";
    }

    public void logTrace(String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(message);
        }
    }

    public InetSocketAddress getLocalAddressForDestination(InetAddress dst, int dstPort, InetAddress localAddress, String transport) throws IOException {
        if (ListeningPoint.TLS.equalsIgnoreCase(transport)) {
            return (InetSocketAddress) ((SipStackImpl) this.sipStack).getLocalAddressForTlsDst(dst, dstPort, localAddress);
        }
        return (InetSocketAddress) ((SipStackImpl) this.sipStack).getLocalAddressForTcpDst(dst, dstPort, localAddress, 0);
    }
}
