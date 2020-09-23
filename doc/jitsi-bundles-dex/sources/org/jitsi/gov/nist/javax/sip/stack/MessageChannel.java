package org.jitsi.gov.nist.javax.sip.stack;

import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import org.jitsi.gov.nist.core.CommonLogger;
import org.jitsi.gov.nist.core.Host;
import org.jitsi.gov.nist.core.HostPort;
import org.jitsi.gov.nist.core.InternalErrorHandler;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.core.StackLogger;
import org.jitsi.gov.nist.javax.sip.address.AddressImpl;
import org.jitsi.gov.nist.javax.sip.header.ContentLength;
import org.jitsi.gov.nist.javax.sip.header.ContentType;
import org.jitsi.gov.nist.javax.sip.header.Via;
import org.jitsi.gov.nist.javax.sip.message.MessageFactoryImpl;
import org.jitsi.gov.nist.javax.sip.message.SIPMessage;
import org.jitsi.gov.nist.javax.sip.message.SIPRequest;
import org.jitsi.gov.nist.javax.sip.message.SIPResponse;
import org.jitsi.javax.sip.address.Hop;
import org.jitsi.javax.sip.header.ContactHeader;
import org.jitsi.javax.sip.header.ServerHeader;

public abstract class MessageChannel {
    /* access modifiers changed from: private|static */
    public static StackLogger logger = CommonLogger.getLogger(MessageChannel.class);
    private SIPClientTransaction encapsulatedClientTransaction;
    protected transient MessageProcessor messageProcessor;
    protected int useCount;

    public abstract void close();

    public abstract String getKey();

    public abstract String getPeerAddress();

    public abstract InetAddress getPeerInetAddress();

    public abstract InetAddress getPeerPacketSourceAddress();

    public abstract int getPeerPacketSourcePort();

    public abstract int getPeerPort();

    public abstract String getPeerProtocol();

    public abstract SIPTransactionStack getSIPStack();

    public abstract String getTransport();

    public abstract String getViaHost();

    public abstract int getViaPort();

    public abstract boolean isReliable();

    public abstract boolean isSecure();

    public abstract void sendMessage(SIPMessage sIPMessage) throws IOException;

    public abstract void sendMessage(byte[] bArr, InetAddress inetAddress, int i, boolean z) throws IOException;

    /* access modifiers changed from: protected */
    public void uncache() {
    }

    public String getHost() {
        return getMessageProcessor().getIpAddress().getHostAddress();
    }

    public int getPort() {
        if (this.messageProcessor != null) {
            return this.messageProcessor.getPort();
        }
        return -1;
    }

    public void sendMessage(SIPMessage sipMessage, Hop hop) throws IOException {
        long time = System.currentTimeMillis();
        InetAddress hopAddr = InetAddress.getByName(hop.getHost());
        try {
            for (MessageProcessor messageProcessor : getSIPStack().getMessageProcessors()) {
                if (messageProcessor.getIpAddress().equals(hopAddr) && messageProcessor.getPort() == hop.getPort() && messageProcessor.getTransport().equalsIgnoreCase(hop.getTransport())) {
                    MessageChannel messageChannel = messageProcessor.createMessageChannel(hopAddr, hop.getPort());
                    if (messageChannel instanceof RawMessageChannel) {
                        final RawMessageChannel channel = (RawMessageChannel) messageChannel;
                        final SIPMessage sIPMessage = sipMessage;
                        getSIPStack().getSelfRoutingThreadpoolExecutor().execute(new Runnable() {
                            public void run() {
                                try {
                                    channel.processMessage((SIPMessage) sIPMessage.clone());
                                } catch (Exception ex) {
                                    if (MessageChannel.logger.isLoggingEnabled(4)) {
                                        MessageChannel.logger.logError("Error self routing message cause by: ", ex);
                                    }
                                }
                            }
                        });
                        if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("Self routing message");
                        }
                        if (logger.isLoggingEnabled(16)) {
                            logMessage(sipMessage, hopAddr, hop.getPort(), time);
                            return;
                        }
                        return;
                    }
                }
            }
            sendMessage(sipMessage.encodeAsBytes(getTransport()), hopAddr, hop.getPort(), sipMessage instanceof SIPRequest);
            sipMessage.setRemoteAddress(hopAddr);
            sipMessage.setRemotePort(hop.getPort());
            sipMessage.setLocalPort(getPort());
            sipMessage.setLocalAddress(getMessageProcessor().getIpAddress());
            if (logger.isLoggingEnabled(16)) {
                logMessage(sipMessage, hopAddr, hop.getPort(), time);
            }
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception ex) {
            if (logger.isLoggingEnabled(4)) {
                logger.logError("Error self routing message cause by: ", ex);
            }
            throw new IOException("Error self routing message");
        } catch (Throwable th) {
            Throwable th2 = th;
            if (logger.isLoggingEnabled(16)) {
                logMessage(sipMessage, hopAddr, hop.getPort(), time);
            }
        }
    }

    public void sendMessage(SIPMessage sipMessage, InetAddress receiverAddress, int receiverPort) throws IOException {
        long time = System.currentTimeMillis();
        sendMessage(sipMessage.encodeAsBytes(getTransport()), receiverAddress, receiverPort, sipMessage instanceof SIPRequest);
        sipMessage.setRemoteAddress(receiverAddress);
        sipMessage.setRemotePort(receiverPort);
        sipMessage.setLocalPort(getPort());
        sipMessage.setLocalAddress(getMessageProcessor().getIpAddress());
        logMessage(sipMessage, receiverAddress, receiverPort, time);
    }

    public String getRawIpSourceAddress() {
        String rawIpSourceAddress = null;
        try {
            return InetAddress.getByName(getPeerAddress()).getHostAddress();
        } catch (Exception ex) {
            InternalErrorHandler.handleException(ex);
            return rawIpSourceAddress;
        }
    }

    public static String getKey(InetAddress inetAddr, int port, String transport) {
        return (transport + Separators.COLON + inetAddr.getHostAddress() + Separators.COLON + port).toLowerCase();
    }

    public static String getKey(HostPort hostPort, String transport) {
        return (transport + Separators.COLON + hostPort.getHost().getHostname() + Separators.COLON + hostPort.getPort()).toLowerCase();
    }

    public HostPort getHostPort() {
        HostPort retval = new HostPort();
        retval.setHost(new Host(getHost()));
        retval.setPort(getPort());
        return retval;
    }

    public HostPort getPeerHostPort() {
        HostPort retval = new HostPort();
        retval.setHost(new Host(getPeerAddress()));
        retval.setPort(getPeerPort());
        return retval;
    }

    public Via getViaHeader() {
        Via channelViaHeader = new Via();
        try {
            channelViaHeader.setTransport(getTransport());
        } catch (ParseException e) {
        }
        channelViaHeader.setSentBy(getHostPort());
        return channelViaHeader;
    }

    public HostPort getViaHostPort() {
        HostPort retval = new HostPort();
        retval.setHost(new Host(getViaHost()));
        retval.setPort(getViaPort());
        return retval;
    }

    public void logMessage(SIPMessage sipMessage, InetAddress address, int port, long time) {
        if (logger.isLoggingEnabled(16)) {
            if (port == -1) {
                port = 5060;
            }
            getSIPStack().serverLogger.logMessage(sipMessage, getHost() + Separators.COLON + getPort(), address.getHostAddress().toString() + Separators.COLON + port, true, time);
        }
    }

    public void logResponse(SIPResponse sipResponse, long receptionTime, String status) {
        int peerport = getPeerPort();
        if (peerport == 0 && sipResponse.getContactHeaders() != null) {
            peerport = ((AddressImpl) ((ContactHeader) sipResponse.getContactHeaders().getFirst()).getAddress()).getPort();
        }
        getSIPStack().serverLogger.logMessage(sipResponse, getPeerAddress().toString() + Separators.COLON + peerport, getHost() + Separators.COLON + getPort(), status, false, receptionTime);
    }

    /* access modifiers changed from: protected|final */
    public final String createBadReqRes(String badReq, ParseException pe) {
        StringBuilder buf = new StringBuilder(512);
        buf.append("SIP/2.0 400 Bad Request (" + pe.getLocalizedMessage() + ')');
        if (!copyViaHeaders(badReq, buf) || !copyHeader("CSeq", badReq, buf) || !copyHeader("Call-ID", badReq, buf) || !copyHeader("From", badReq, buf) || !copyHeader("To", badReq, buf)) {
            return null;
        }
        int toStart = buf.indexOf("To");
        if (toStart != -1 && buf.indexOf("tag", toStart) == -1) {
            buf.append(";tag=badreq");
        }
        ServerHeader s = MessageFactoryImpl.getDefaultServerHeader();
        if (s != null) {
            buf.append(Separators.NEWLINE + s.toString());
        }
        int clength = badReq.length();
        if (!(this instanceof UDPMessageChannel) || (((buf.length() + clength) + "Content-Type".length()) + ": message/sipfrag\r\n".length()) + "Content-Length".length() < 1300) {
            buf.append(Separators.NEWLINE + new ContentType("message", "sipfrag").toString());
            buf.append(Separators.NEWLINE + new ContentLength(clength).toString());
            buf.append("\r\n\r\n" + badReq);
        } else {
            buf.append(Separators.NEWLINE + new ContentLength(0).toString());
        }
        return buf.toString();
    }

    private static final boolean copyHeader(String name, String fromReq, StringBuilder buf) {
        int start = fromReq.indexOf(name);
        if (start != -1) {
            int end = fromReq.indexOf(Separators.NEWLINE, start);
            if (end != -1) {
                buf.append(fromReq.subSequence(start - 2, end));
                return true;
            }
        }
        return false;
    }

    private static final boolean copyViaHeaders(String fromReq, StringBuilder buf) {
        int start = fromReq.indexOf("Via");
        boolean found = false;
        while (start != -1) {
            int end = fromReq.indexOf(Separators.NEWLINE, start);
            if (end == -1) {
                return false;
            }
            buf.append(fromReq.subSequence(start - 2, end));
            found = true;
            start = fromReq.indexOf("Via", end);
        }
        return found;
    }

    public MessageProcessor getMessageProcessor() {
        return this.messageProcessor;
    }

    public SIPClientTransaction getEncapsulatedClientTransaction() {
        return this.encapsulatedClientTransaction;
    }

    public void setEncapsulatedClientTransaction(SIPClientTransaction transaction) {
        this.encapsulatedClientTransaction = transaction;
    }
}
