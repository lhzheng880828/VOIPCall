package org.jitsi.gov.nist.javax.sip.stack;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.Hashtable;
import org.jitsi.gov.nist.core.CommonLogger;
import org.jitsi.gov.nist.core.InternalErrorHandler;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.core.StackLogger;
import org.jitsi.gov.nist.core.ThreadAuditor.ThreadHandle;
import org.jitsi.gov.nist.javax.sip.SIPConstants;
import org.jitsi.gov.nist.javax.sip.header.CSeq;
import org.jitsi.gov.nist.javax.sip.header.CallID;
import org.jitsi.gov.nist.javax.sip.header.ContentLength;
import org.jitsi.gov.nist.javax.sip.header.From;
import org.jitsi.gov.nist.javax.sip.header.RequestLine;
import org.jitsi.gov.nist.javax.sip.header.StatusLine;
import org.jitsi.gov.nist.javax.sip.header.To;
import org.jitsi.gov.nist.javax.sip.header.Via;
import org.jitsi.gov.nist.javax.sip.message.SIPMessage;
import org.jitsi.gov.nist.javax.sip.message.SIPRequest;
import org.jitsi.gov.nist.javax.sip.message.SIPResponse;
import org.jitsi.gov.nist.javax.sip.parser.MessageParser;
import org.jitsi.gov.nist.javax.sip.parser.ParseExceptionListener;
import org.jitsi.javax.sip.ListeningPoint;
import org.jitsi.javax.sip.address.Hop;
import org.jitsi.javax.sip.message.Response;

public class UDPMessageChannel extends MessageChannel implements ParseExceptionListener, Runnable, RawMessageChannel {
    /* access modifiers changed from: private|static */
    public static StackLogger logger = CommonLogger.getLogger(UDPMessageChannel.class);
    /* access modifiers changed from: private|static */
    public static Hashtable<String, PingBackTimerTask> pingBackRecord = new Hashtable();
    private DatagramPacket incomingPacket;
    private String myAddress;
    protected MessageParser myParser;
    protected int myPort;
    private Thread mythread = null;
    private InetAddress peerAddress;
    private InetAddress peerPacketSourceAddress;
    private int peerPacketSourcePort;
    private int peerPort;
    private String peerProtocol;
    private long receptionTime;
    protected SIPTransactionStack sipStack;

    class PingBackTimerTask extends SIPStackTimerTask {
        String ipAddress;
        int port;

        public PingBackTimerTask(String ipAddress, int port) {
            this.ipAddress = ipAddress;
            this.port = port;
        }

        public void runTask() {
            UDPMessageChannel.pingBackRecord.remove(this.ipAddress + Separators.COLON + this.port);
        }

        public int hashCode() {
            return (this.ipAddress + Separators.COLON + this.port).hashCode();
        }
    }

    protected UDPMessageChannel(SIPTransactionStack stack, UDPMessageProcessor messageProcessor, String threadName) {
        this.messageProcessor = messageProcessor;
        this.sipStack = stack;
        this.myParser = this.sipStack.getMessageParserFactory().createMessageParser(this.sipStack);
        this.mythread = new Thread(this);
        this.myAddress = messageProcessor.getIpAddress().getHostAddress();
        this.myPort = messageProcessor.getPort();
        this.mythread.setName(threadName);
        this.mythread.setDaemon(true);
        this.mythread.start();
    }

    protected UDPMessageChannel(SIPTransactionStack stack, UDPMessageProcessor messageProcessor, DatagramPacket packet) {
        this.incomingPacket = packet;
        this.messageProcessor = messageProcessor;
        this.sipStack = stack;
        this.myParser = this.sipStack.getMessageParserFactory().createMessageParser(this.sipStack);
        this.myAddress = messageProcessor.getIpAddress().getHostAddress();
        this.myPort = messageProcessor.getPort();
        this.mythread = new Thread(this);
        this.mythread.setDaemon(true);
        this.mythread.start();
    }

    protected UDPMessageChannel(InetAddress targetAddr, int port, SIPTransactionStack sipStack, UDPMessageProcessor messageProcessor) {
        this.peerAddress = targetAddr;
        this.peerPort = port;
        this.peerProtocol = ListeningPoint.UDP;
        this.messageProcessor = messageProcessor;
        this.myAddress = messageProcessor.getIpAddress().getHostAddress();
        this.myPort = messageProcessor.getPort();
        this.sipStack = sipStack;
        this.myParser = sipStack.getMessageParserFactory().createMessageParser(sipStack);
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Creating message channel " + targetAddr.getHostAddress() + Separators.SLASH + port);
        }
    }

    public void run() {
        ThreadHandle threadHandle = null;
        UDPMessageProcessor udpMessageProcessor = this.messageProcessor;
        do {
            DatagramPacket packet = null;
            if (this.sipStack.threadPoolSize != -1) {
                if (threadHandle == null) {
                    threadHandle = this.sipStack.getThreadAuditor().addCurrentThread();
                }
                threadHandle.ping();
                try {
                    packet = ((DatagramQueuedMessageDispatch) udpMessageProcessor.messageQueue.take()).packet;
                } catch (InterruptedException e) {
                    if (!udpMessageProcessor.isRunning) {
                        return;
                    }
                }
                this.incomingPacket = packet;
            } else {
                packet = this.incomingPacket;
            }
            try {
                processIncomingDataPacket(packet);
            } catch (Exception e2) {
                logger.logError("Error while processing incoming UDP packet", e2);
            }
        } while (this.sipStack.threadPoolSize != -1);
    }

    private void processIncomingDataPacket(DatagramPacket packet) throws Exception {
        this.peerAddress = packet.getAddress();
        int packetLength = packet.getLength();
        Object msgBytes = new byte[packetLength];
        System.arraycopy(packet.getData(), 0, msgBytes, 0, packetLength);
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("UDPMessageChannel: processIncomingDataPacket : peerAddress = " + this.peerAddress.getHostAddress() + Separators.SLASH + packet.getPort() + " Length = " + packetLength);
        }
        try {
            this.receptionTime = System.currentTimeMillis();
            SIPMessage sipMessage = this.myParser.parseSIPMessage(msgBytes, true, false, this);
            if (sipMessage instanceof SIPRequest) {
                String sipVersion = ((SIPRequest) sipMessage).getRequestLine().getSipVersion();
                if (sipVersion.equals(SIPConstants.SIP_VERSION_STRING)) {
                    if (!((SIPRequest) sipMessage).getMethod().equalsIgnoreCase(((SIPRequest) sipMessage).getCSeqHeader().getMethod())) {
                        sendMessage(((SIPRequest) sipMessage).createResponse(Response.BAD_REQUEST).encodeAsBytes(getTransport()), this.peerAddress, packet.getPort(), ListeningPoint.UDP, false);
                        return;
                    }
                }
                sendMessage(((SIPRequest) sipMessage).createResponse(Response.VERSION_NOT_SUPPORTED, "Bad version " + sipVersion).toString().getBytes(), this.peerAddress, packet.getPort(), ListeningPoint.UDP, false);
                return;
            }
            if (sipMessage == null) {
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("Rejecting message !  + Null message parsed.");
                }
                String key = packet.getAddress().getHostAddress() + Separators.COLON + packet.getPort();
                if (pingBackRecord.get(key) != null || this.sipStack.getMinKeepAliveInterval() <= 0) {
                    logger.logDebug("Not sending ping back");
                    return;
                }
                byte[] retval = "\r\n\r\n".getBytes();
                DatagramPacket keepalive = new DatagramPacket(retval, 0, retval.length, packet.getAddress(), packet.getPort());
                SIPStackTimerTask pingBackTimerTask = new PingBackTimerTask(packet.getAddress().getHostAddress(), packet.getPort());
                pingBackRecord.put(key, pingBackTimerTask);
                this.sipStack.getTimer().schedule(pingBackTimerTask, this.sipStack.getMinKeepAliveInterval() * 1000);
                ((UDPMessageProcessor) this.messageProcessor).sock.send(keepalive);
                return;
            }
            Via topMostVia = sipMessage.getTopmostVia();
            if (sipMessage.getFrom() == null || sipMessage.getTo() == null || sipMessage.getCallId() == null || sipMessage.getCSeq() == null || topMostVia == null) {
                String badmsg = new String(msgBytes);
                if (logger.isLoggingEnabled()) {
                    logger.logError("bad message " + badmsg);
                    logger.logError(">>> Dropped Bad Msg From = " + sipMessage.getFrom() + "To = " + sipMessage.getTo() + "CallId = " + sipMessage.getCallId() + "CSeq = " + sipMessage.getCSeq() + "Via = " + sipMessage.getViaHeaders());
                    return;
                }
                return;
            }
            if (this.sipStack.sipEventInterceptor != null) {
                this.sipStack.sipEventInterceptor.beforeMessage(sipMessage);
            }
            if (sipMessage instanceof SIPRequest) {
                Hop hop = this.sipStack.addressResolver.resolveAddress(topMostVia.getHop());
                this.peerPort = hop.getPort();
                this.peerProtocol = topMostVia.getTransport();
                this.peerPacketSourceAddress = packet.getAddress();
                this.peerPacketSourcePort = packet.getPort();
                try {
                    this.peerAddress = packet.getAddress();
                    boolean hasRPort = topMostVia.hasParameter("rport");
                    if (hasRPort || !hop.getHost().equals(this.peerAddress.getHostAddress())) {
                        topMostVia.setParameter("received", this.peerAddress.getHostAddress());
                    }
                    if (hasRPort) {
                        topMostVia.setParameter("rport", Integer.toString(this.peerPacketSourcePort));
                    }
                } catch (ParseException ex1) {
                    InternalErrorHandler.handleException(ex1);
                }
            } else {
                this.peerPacketSourceAddress = packet.getAddress();
                this.peerPacketSourcePort = packet.getPort();
                this.peerAddress = packet.getAddress();
                this.peerPort = packet.getPort();
                this.peerProtocol = topMostVia.getTransport();
            }
            processMessage(sipMessage);
            if (this.sipStack.sipEventInterceptor != null) {
                this.sipStack.sipEventInterceptor.afterMessage(sipMessage);
            }
        } catch (ParseException ex) {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("Rejecting message !  " + new String(msgBytes));
                logger.logDebug("error message " + ex.getMessage());
                logger.logException(ex);
            }
            String str = new String(msgBytes, 0, packetLength);
            if (!str.startsWith("SIP/")) {
                if (!str.startsWith("ACK ")) {
                    String badReqRes = createBadReqRes(str, ex);
                    if (badReqRes != null) {
                        if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("Sending automatic 400 Bad Request:");
                            logger.logDebug(badReqRes);
                        }
                        try {
                            sendMessage(badReqRes.getBytes(), this.peerAddress, packet.getPort(), ListeningPoint.UDP, false);
                        } catch (IOException e) {
                            logger.logException(e);
                        }
                    } else if (logger.isLoggingEnabled(32)) {
                        logger.logDebug("Could not formulate automatic 400 Bad Request");
                    }
                }
            }
        }
    }

    public void processMessage(SIPMessage sipMessage) {
        sipMessage.setRemoteAddress(this.peerAddress);
        sipMessage.setRemotePort(getPeerPort());
        sipMessage.setLocalPort(getPort());
        sipMessage.setLocalAddress(getMessageProcessor().getIpAddress());
        if (sipMessage instanceof SIPRequest) {
            SIPRequest sipRequest = (SIPRequest) sipMessage;
            if (logger.isLoggingEnabled(16)) {
                this.sipStack.serverLogger.logMessage(sipMessage, getPeerHostPort().toString(), getHost() + Separators.COLON + this.myPort, false, this.receptionTime);
            }
            ServerRequestInterface sipServerRequest = this.sipStack.newSIPServerRequest(sipRequest, this);
            if (sipServerRequest != null) {
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("About to process " + sipRequest.getFirstLine() + Separators.SLASH + sipServerRequest);
                }
                try {
                    sipServerRequest.processRequest(sipRequest, this);
                    if ((sipServerRequest instanceof SIPTransaction) && !((SIPServerTransaction) sipServerRequest).passToListener()) {
                        ((SIPTransaction) sipServerRequest).releaseSem();
                    }
                    if (logger.isLoggingEnabled(32)) {
                        logger.logDebug("Done processing " + sipRequest.getFirstLine() + Separators.SLASH + sipServerRequest);
                        return;
                    }
                    return;
                } catch (Throwable th) {
                    if ((sipServerRequest instanceof SIPTransaction) && !((SIPServerTransaction) sipServerRequest).passToListener()) {
                        ((SIPTransaction) sipServerRequest).releaseSem();
                    }
                }
            } else if (logger.isLoggingEnabled()) {
                logger.logWarning("Null request interface returned -- dropping request");
                return;
            } else {
                return;
            }
        }
        SIPResponse sipResponse = (SIPResponse) sipMessage;
        try {
            sipResponse.checkHeaders();
            ServerResponseInterface sipServerResponse = this.sipStack.newSIPServerResponse(sipResponse, this);
            if (sipServerResponse != null) {
                try {
                    if (!(sipServerResponse instanceof SIPClientTransaction) || ((SIPClientTransaction) sipServerResponse).checkFromTag(sipResponse)) {
                        sipServerResponse.processResponse(sipResponse, this);
                        if ((sipServerResponse instanceof SIPTransaction) && !((SIPTransaction) sipServerResponse).passToListener()) {
                            SIPTransaction sipServerResponse2 = (SIPTransaction) sipServerResponse;
                        } else {
                            return;
                        }
                    }
                    if (logger.isLoggingEnabled()) {
                        logger.logError("Dropping response message with invalid tag >>> " + sipResponse);
                    }
                    if ((sipServerResponse instanceof SIPTransaction) && !((SIPTransaction) sipServerResponse).passToListener()) {
                        sipServerResponse = (SIPTransaction) sipServerResponse;
                    } else {
                        return;
                    }
                    sipServerResponse.releaseSem();
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    if ((sipServerResponse instanceof SIPTransaction) && !((SIPTransaction) sipServerResponse).passToListener()) {
                        ((SIPTransaction) sipServerResponse).releaseSem();
                    }
                }
            } else if (logger.isLoggingEnabled(32)) {
                logger.logDebug("null sipServerResponse!");
            }
        } catch (ParseException e) {
            if (logger.isLoggingEnabled()) {
                logger.logError("Dropping Badly formatted response message >>> " + sipResponse);
            }
        }
    }

    public void handleException(ParseException ex, SIPMessage sipMessage, Class hdrClass, String header, String message) throws ParseException {
        if (logger.isLoggingEnabled()) {
            logger.logException(ex);
        }
        if (hdrClass == null || !(hdrClass.equals(From.class) || hdrClass.equals(To.class) || hdrClass.equals(CSeq.class) || hdrClass.equals(Via.class) || hdrClass.equals(CallID.class) || hdrClass.equals(ContentLength.class) || hdrClass.equals(RequestLine.class) || hdrClass.equals(StatusLine.class))) {
            sipMessage.addUnparsed(header);
            return;
        }
        if (logger.isLoggingEnabled()) {
            logger.logError("BAD MESSAGE!");
            logger.logError(message);
        }
        throw ex;
    }

    public void sendMessage(SIPMessage sipMessage) throws IOException {
        if (logger.isLoggingEnabled(16) && this.sipStack.isLogStackTraceOnMessageSend()) {
            if (!(sipMessage instanceof SIPRequest) || ((SIPRequest) sipMessage).getRequestLine() == null) {
                logger.logStackTrace(16);
            } else {
                logger.logStackTrace(16);
            }
        }
        long time = System.currentTimeMillis();
        try {
            InetAddress inetAddress;
            int i;
            StackLogger stackLogger;
            String str;
            for (MessageProcessor messageProcessor : this.sipStack.getMessageProcessors()) {
                if (messageProcessor.getIpAddress().equals(this.peerAddress) && messageProcessor.getPort() == this.peerPort && messageProcessor.getTransport().equalsIgnoreCase(this.peerProtocol)) {
                    MessageChannel messageChannel = messageProcessor.createMessageChannel(this.peerAddress, this.peerPort);
                    if (messageChannel instanceof RawMessageChannel) {
                        final RawMessageChannel channel = (RawMessageChannel) messageChannel;
                        final SIPMessage sIPMessage = sipMessage;
                        getSIPStack().getSelfRoutingThreadpoolExecutor().execute(new Runnable() {
                            public void run() {
                                try {
                                    channel.processMessage((SIPMessage) sIPMessage.clone());
                                } catch (Exception ex) {
                                    if (UDPMessageChannel.logger.isLoggingEnabled(4)) {
                                        UDPMessageChannel.logger.logError("Error self routing message cause by: ", ex);
                                    }
                                }
                            }
                        });
                        if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("Self routing message");
                        }
                        if (logger.isLoggingEnabled(16) && !sipMessage.isNullRequest()) {
                            inetAddress = this.peerAddress;
                            i = this.peerPort;
                            logMessage(sipMessage, inetAddress, i, time);
                        } else if (logger.isLoggingEnabled(32)) {
                            stackLogger = logger;
                            str = "Sent EMPTY Message";
                            stackLogger.logDebug(str);
                        } else {
                            return;
                        }
                    }
                }
            }
            sendMessage(sipMessage.encodeAsBytes(getTransport()), this.peerAddress, this.peerPort, this.peerProtocol, sipMessage instanceof SIPRequest);
            sipMessage.setRemoteAddress(this.peerAddress);
            sipMessage.setRemotePort(this.peerPort);
            sipMessage.setLocalPort(getPort());
            sipMessage.setLocalAddress(getMessageProcessor().getIpAddress());
            if (logger.isLoggingEnabled(16) && !sipMessage.isNullRequest()) {
                inetAddress = this.peerAddress;
                i = this.peerPort;
                logMessage(sipMessage, inetAddress, i, time);
            } else if (logger.isLoggingEnabled(32)) {
                stackLogger = logger;
                str = "Sent EMPTY Message";
                stackLogger.logDebug(str);
            }
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex2) {
            logger.logError("An exception occured while sending message", ex2);
            throw new IOException("An exception occured while sending message");
        } catch (Throwable th) {
            if (logger.isLoggingEnabled(16) && !sipMessage.isNullRequest()) {
                logMessage(sipMessage, this.peerAddress, this.peerPort, time);
            } else if (logger.isLoggingEnabled(32)) {
                logger.logDebug("Sent EMPTY Message");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void sendMessage(byte[] msg, InetAddress peerAddress, int peerPort, boolean reConnect) throws IOException {
        if (logger.isLoggingEnabled(16) && this.sipStack.isLogStackTraceOnMessageSend()) {
            logger.logStackTrace(16);
        }
        if (peerPort == -1) {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug(getClass().getName() + ":sendMessage: Dropping reply!");
            }
            throw new IOException("Receiver port not set ");
        }
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("sendMessage " + peerAddress.getHostAddress() + Separators.SLASH + peerPort + Separators.RETURN + "messageSize =  " + msg.length + " message = " + new String(msg));
            logger.logDebug("*******************\n");
        }
        DatagramPacket reply = new DatagramPacket(msg, msg.length, peerAddress, peerPort);
        boolean created = false;
        try {
            DatagramSocket sock;
            if (this.sipStack.udpFlag) {
                sock = ((UDPMessageProcessor) this.messageProcessor).sock;
            } else {
                sock = new DatagramSocket();
                created = true;
            }
            sock.send(reply);
            if (created) {
                sock.close();
            }
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex2) {
            InternalErrorHandler.handleException(ex2);
        }
    }

    /* access modifiers changed from: protected */
    public void sendMessage(byte[] msg, InetAddress peerAddress, int peerPort, String peerProtocol, boolean retry) throws IOException {
        if (peerPort == -1) {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug(getClass().getName() + ":sendMessage: Dropping reply!");
            }
            throw new IOException("Receiver port not set ");
        }
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug(":sendMessage " + peerAddress.getHostAddress() + Separators.SLASH + peerPort + Separators.RETURN + " messageSize = " + msg.length);
        }
        if (peerProtocol.compareToIgnoreCase(ListeningPoint.UDP) == 0) {
            DatagramPacket reply = new DatagramPacket(msg, msg.length, peerAddress, peerPort);
            try {
                DatagramSocket sock;
                if (this.sipStack.udpFlag) {
                    sock = ((UDPMessageProcessor) this.messageProcessor).sock;
                } else {
                    sock = this.sipStack.getNetworkLayer().createDatagramSocket();
                }
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("sendMessage " + peerAddress.getHostAddress() + Separators.SLASH + peerPort + Separators.RETURN + new String(msg));
                }
                sock.send(reply);
                if (!this.sipStack.udpFlag) {
                    sock.close();
                    return;
                }
                return;
            } catch (IOException ex) {
                throw ex;
            } catch (Exception ex2) {
                InternalErrorHandler.handleException(ex2);
                return;
            }
        }
        OutputStream myOutputStream = this.sipStack.ioHandler.sendBytes(this.messageProcessor.getIpAddress(), peerAddress, peerPort, "tcp", msg, retry, this).getOutputStream();
        myOutputStream.write(msg, 0, msg.length);
        myOutputStream.flush();
    }

    public SIPTransactionStack getSIPStack() {
        return this.sipStack;
    }

    public String getTransport() {
        return "udp";
    }

    public String getHost() {
        return this.messageProcessor.getIpAddress().getHostAddress();
    }

    public int getPort() {
        return ((UDPMessageProcessor) this.messageProcessor).getPort();
    }

    public String getPeerName() {
        return this.peerAddress.getHostName();
    }

    public String getPeerAddress() {
        return this.peerAddress.getHostAddress();
    }

    /* access modifiers changed from: protected */
    public InetAddress getPeerInetAddress() {
        return this.peerAddress;
    }

    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!getClass().equals(other.getClass())) {
            return false;
        }
        return getKey().equals(((UDPMessageChannel) other).getKey());
    }

    public String getKey() {
        return MessageChannel.getKey(this.peerAddress, this.peerPort, ListeningPoint.UDP);
    }

    public int getPeerPacketSourcePort() {
        return this.peerPacketSourcePort;
    }

    public InetAddress getPeerPacketSourceAddress() {
        return this.peerPacketSourceAddress;
    }

    public String getViaHost() {
        return this.myAddress;
    }

    public int getViaPort() {
        return this.myPort;
    }

    public boolean isReliable() {
        return false;
    }

    public boolean isSecure() {
        return false;
    }

    public int getPeerPort() {
        return this.peerPort;
    }

    public String getPeerProtocol() {
        return this.peerProtocol;
    }

    public void close() {
        if (this.mythread != null) {
            this.mythread.interrupt();
            this.mythread = null;
        }
    }
}
