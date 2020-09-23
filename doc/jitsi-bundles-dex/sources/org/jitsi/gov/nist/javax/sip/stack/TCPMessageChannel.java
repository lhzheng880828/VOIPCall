package org.jitsi.gov.nist.javax.sip.stack;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.text.ParseException;
import org.jitsi.gov.nist.core.CommonLogger;
import org.jitsi.gov.nist.core.StackLogger;
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
import org.jitsi.gov.nist.javax.sip.parser.PipelinedMsgParser;
import org.jitsi.gov.nist.javax.sip.parser.SIPMessageListener;
import org.jitsi.javax.sip.ListeningPoint;

public class TCPMessageChannel extends MessageChannel implements SIPMessageListener, Runnable, RawMessageChannel {
    /* access modifiers changed from: private|static */
    public static StackLogger logger = CommonLogger.getLogger(TCPMessageChannel.class);
    protected boolean isCached;
    protected boolean isRunning = true;
    protected String key;
    protected String myAddress;
    protected InputStream myClientInputStream;
    protected OutputStream myClientOutputStream;
    private PipelinedMsgParser myParser;
    protected int myPort;
    /* access modifiers changed from: private */
    public Socket mySock;
    private Thread mythread;
    protected InetAddress peerAddress;
    protected int peerPort;
    protected String peerProtocol;
    protected SIPTransactionStack sipStack;
    private TCPMessageProcessor tcpMessageProcessor;

    protected TCPMessageChannel(SIPTransactionStack sipStack) {
        this.sipStack = sipStack;
    }

    protected TCPMessageChannel(Socket sock, SIPTransactionStack sipStack, TCPMessageProcessor msgProcessor, String threadName) throws IOException {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("creating new TCPMessageChannel ");
            logger.logStackTrace();
        }
        this.mySock = sock;
        this.peerAddress = this.mySock.getInetAddress();
        this.myAddress = msgProcessor.getIpAddress().getHostAddress();
        this.myClientInputStream = this.mySock.getInputStream();
        this.myClientOutputStream = this.mySock.getOutputStream();
        this.mythread = new Thread(this);
        this.mythread.setDaemon(true);
        this.mythread.setName(threadName);
        this.sipStack = sipStack;
        this.peerPort = this.mySock.getPort();
        this.tcpMessageProcessor = msgProcessor;
        this.myPort = this.tcpMessageProcessor.getPort();
        this.messageProcessor = msgProcessor;
        this.mythread.start();
    }

    protected TCPMessageChannel(InetAddress inetAddr, int port, SIPTransactionStack sipStack, TCPMessageProcessor messageProcessor) throws IOException {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("creating new TCPMessageChannel ");
            logger.logStackTrace();
        }
        this.peerAddress = inetAddr;
        this.peerPort = port;
        this.myPort = messageProcessor.getPort();
        this.peerProtocol = ListeningPoint.TCP;
        this.sipStack = sipStack;
        this.tcpMessageProcessor = messageProcessor;
        this.myAddress = messageProcessor.getIpAddress().getHostAddress();
        this.key = MessageChannel.getKey(this.peerAddress, this.peerPort, ListeningPoint.TCP);
        this.messageProcessor = messageProcessor;
    }

    public boolean isReliable() {
        return true;
    }

    public void close() {
        try {
            if (this.mySock != null) {
                this.mySock.close();
                this.mySock = null;
            }
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("Closing message Channel " + this);
            }
        } catch (IOException ex) {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("Error closing socket " + ex);
            }
        }
    }

    public SIPTransactionStack getSIPStack() {
        return this.sipStack;
    }

    public String getTransport() {
        return ListeningPoint.TCP;
    }

    public String getPeerAddress() {
        if (this.peerAddress != null) {
            return this.peerAddress.getHostAddress();
        }
        return getHost();
    }

    /* access modifiers changed from: protected */
    public InetAddress getPeerInetAddress() {
        return this.peerAddress;
    }

    public String getPeerProtocol() {
        return this.peerProtocol;
    }

    private void sendMessage(byte[] msg, boolean isClient) throws IOException {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("sendMessage isClient  = " + isClient);
        }
        Socket sock = this.sipStack.ioHandler.sendBytes(this.messageProcessor.getIpAddress(), this.peerAddress, this.peerPort, this.peerProtocol, msg, isClient, this);
        if (sock != this.mySock && sock != null) {
            try {
                if (this.mySock != null) {
                    if (logger.isLoggingEnabled(32)) {
                        logger.logDebug("Closing socket");
                    }
                    this.mySock.close();
                }
            } catch (IOException e) {
            }
            this.mySock = sock;
            this.myClientInputStream = this.mySock.getInputStream();
            this.myClientOutputStream = this.mySock.getOutputStream();
            Thread thread = new Thread(this);
            thread.setDaemon(true);
            thread.setName("TCPMessageChannelThread");
            thread.start();
        }
    }

    public void sendMessage(final SIPMessage sipMessage) throws IOException {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("sendMessage:: " + sipMessage.getFirstLine() + " cseq method = " + sipMessage.getCSeq().getMethod());
        }
        for (MessageProcessor messageProcessor : getSIPStack().getMessageProcessors()) {
            if (messageProcessor.getIpAddress().getHostAddress().equals(getPeerAddress()) && messageProcessor.getPort() == getPeerPort() && messageProcessor.getTransport().equalsIgnoreCase(getPeerProtocol())) {
                getSIPStack().getSelfRoutingThreadpoolExecutor().execute(new Runnable() {
                    public void run() {
                        try {
                            TCPMessageChannel.this.processMessage((SIPMessage) sipMessage.clone());
                        } catch (Exception ex) {
                            if (TCPMessageChannel.logger.isLoggingEnabled(4)) {
                                TCPMessageChannel.logger.logError("Error self routing message cause by: ", ex);
                            }
                        }
                    }
                });
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("Self routing message");
                    return;
                }
                return;
            }
        }
        byte[] msg = sipMessage.encodeAsBytes(getTransport());
        long time = System.currentTimeMillis();
        sendMessage(msg, sipMessage instanceof SIPRequest);
        sipMessage.setRemoteAddress(this.peerAddress);
        sipMessage.setRemotePort(this.peerPort);
        sipMessage.setLocalAddress(getMessageProcessor().getIpAddress());
        sipMessage.setLocalPort(getPort());
        if (logger.isLoggingEnabled(16)) {
            logMessage(sipMessage, this.peerAddress, this.peerPort, time);
        }
    }

    public void sendMessage(byte[] message, InetAddress receiverAddress, int receiverPort, boolean retry) throws IOException {
        if (message == null || receiverAddress == null) {
            throw new IllegalArgumentException("Null argument");
        }
        Socket sock = this.sipStack.ioHandler.sendBytes(this.messageProcessor.getIpAddress(), receiverAddress, receiverPort, ListeningPoint.TCP, message, retry, this);
        if (sock != this.mySock && sock != null) {
            if (this.mySock != null) {
                this.sipStack.getTimer().schedule(new SIPStackTimerTask() {
                    public void cleanUpBeforeCancel() {
                        try {
                            TCPMessageChannel.logger.logDebug("closing socket");
                            TCPMessageChannel.this.mySock.close();
                        } catch (IOException e) {
                        }
                    }

                    public void runTask() {
                        try {
                            if (TCPMessageChannel.logger.isLoggingEnabled(32)) {
                                TCPMessageChannel.logger.logDebug("Closing socket");
                            }
                            TCPMessageChannel.this.mySock.close();
                        } catch (IOException e) {
                        }
                    }
                }, 8000);
            }
            this.mySock = sock;
            this.myClientInputStream = this.mySock.getInputStream();
            this.myClientOutputStream = this.mySock.getOutputStream();
            Thread mythread = new Thread(this);
            mythread.setDaemon(true);
            mythread.setName("TCPMessageChannelThread");
            mythread.start();
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
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Encountered Bad Message \n" + sipMessage.toString());
        }
        String msgString = sipMessage.toString();
        if (!(msgString.startsWith("SIP/") || msgString.startsWith("ACK "))) {
            String badReqRes = createBadReqRes(msgString, ex);
            if (badReqRes != null) {
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("Sending automatic 400 Bad Request:");
                    logger.logDebug(badReqRes);
                }
                try {
                    sendMessage(badReqRes.getBytes(), getPeerInetAddress(), getPeerPort(), false);
                } catch (IOException e) {
                    logger.logException(e);
                }
            } else if (logger.isLoggingEnabled(32)) {
                logger.logDebug("Could not formulate automatic 400 Bad Request");
            }
        }
        throw ex;
    }

    public void processMessage(SIPMessage sipMessage, InetAddress address) {
        this.peerAddress = address;
        try {
            processMessage(sipMessage);
        } catch (Exception e) {
            if (logger.isLoggingEnabled(4)) {
                logger.logError("ERROR processing self routing", e);
            }
        }
    }

    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    public void processMessage(org.jitsi.gov.nist.javax.sip.message.SIPMessage r29) throws java.lang.Exception {
        /*
        r28 = this;
        r5 = r29.getFrom();	 Catch:{ all -> 0x01c4 }
        if (r5 == 0) goto L_0x001e;
    L_0x0006:
        r5 = r29.getTo();	 Catch:{ all -> 0x01c4 }
        if (r5 == 0) goto L_0x001e;
    L_0x000c:
        r5 = r29.getCallId();	 Catch:{ all -> 0x01c4 }
        if (r5 == 0) goto L_0x001e;
    L_0x0012:
        r5 = r29.getCSeq();	 Catch:{ all -> 0x01c4 }
        if (r5 == 0) goto L_0x001e;
    L_0x0018:
        r5 = r29.getViaHeaders();	 Catch:{ all -> 0x01c4 }
        if (r5 != 0) goto L_0x0039;
    L_0x001e:
        r4 = r29.encode();	 Catch:{ all -> 0x01c4 }
        r5 = logger;	 Catch:{ all -> 0x01c4 }
        r6 = 32;
        r5 = r5.isLoggingEnabled(r6);	 Catch:{ all -> 0x01c4 }
        if (r5 == 0) goto L_0x0038;
    L_0x002c:
        r5 = logger;	 Catch:{ all -> 0x01c4 }
        r6 = ">>> Dropped Bad Msg";
        r5.logDebug(r6);	 Catch:{ all -> 0x01c4 }
        r5 = logger;	 Catch:{ all -> 0x01c4 }
        r5.logDebug(r4);	 Catch:{ all -> 0x01c4 }
    L_0x0038:
        return;
    L_0x0039:
        r0 = r28;
        r5 = r0.peerAddress;	 Catch:{ all -> 0x01c4 }
        r0 = r29;
        r0.setRemoteAddress(r5);	 Catch:{ all -> 0x01c4 }
        r5 = r28.getPeerPort();	 Catch:{ all -> 0x01c4 }
        r0 = r29;
        r0.setRemotePort(r5);	 Catch:{ all -> 0x01c4 }
        r5 = r28.getMessageProcessor();	 Catch:{ all -> 0x01c4 }
        r5 = r5.getIpAddress();	 Catch:{ all -> 0x01c4 }
        r0 = r29;
        r0.setLocalAddress(r5);	 Catch:{ all -> 0x01c4 }
        r5 = r28.getPort();	 Catch:{ all -> 0x01c4 }
        r0 = r29;
        r0.setLocalPort(r5);	 Catch:{ all -> 0x01c4 }
        r27 = r29.getViaHeaders();	 Catch:{ all -> 0x01c4 }
        r0 = r29;
        r5 = r0 instanceof org.jitsi.gov.nist.javax.sip.message.SIPRequest;	 Catch:{ all -> 0x01c4 }
        if (r5 == 0) goto L_0x0118;
    L_0x006b:
        r25 = r27.getFirst();	 Catch:{ all -> 0x01c4 }
        r25 = (org.jitsi.gov.nist.javax.sip.header.Via) r25;	 Catch:{ all -> 0x01c4 }
        r0 = r28;
        r5 = r0.sipStack;	 Catch:{ all -> 0x01c4 }
        r5 = r5.addressResolver;	 Catch:{ all -> 0x01c4 }
        r6 = r25.getHop();	 Catch:{ all -> 0x01c4 }
        r14 = r5.resolveAddress(r6);	 Catch:{ all -> 0x01c4 }
        r5 = r25.getTransport();	 Catch:{ all -> 0x01c4 }
        r0 = r28;
        r0.peerProtocol = r5;	 Catch:{ all -> 0x01c4 }
        r0 = r28;
        r5 = r0.mySock;	 Catch:{ ParseException -> 0x01c6 }
        if (r5 == 0) goto L_0x0099;
    L_0x008d:
        r0 = r28;
        r5 = r0.mySock;	 Catch:{ ParseException -> 0x01c6 }
        r5 = r5.getInetAddress();	 Catch:{ ParseException -> 0x01c6 }
        r0 = r28;
        r0.peerAddress = r5;	 Catch:{ ParseException -> 0x01c6 }
    L_0x0099:
        r5 = "rport";
        r0 = r25;
        r5 = r0.hasParameter(r5);	 Catch:{ ParseException -> 0x01c6 }
        if (r5 != 0) goto L_0x00b5;
    L_0x00a3:
        r5 = r14.getHost();	 Catch:{ ParseException -> 0x01c6 }
        r0 = r28;
        r6 = r0.peerAddress;	 Catch:{ ParseException -> 0x01c6 }
        r6 = r6.getHostAddress();	 Catch:{ ParseException -> 0x01c6 }
        r5 = r5.equals(r6);	 Catch:{ ParseException -> 0x01c6 }
        if (r5 != 0) goto L_0x00c4;
    L_0x00b5:
        r5 = "received";
        r0 = r28;
        r6 = r0.peerAddress;	 Catch:{ ParseException -> 0x01c6 }
        r6 = r6.getHostAddress();	 Catch:{ ParseException -> 0x01c6 }
        r0 = r25;
        r0.setParameter(r5, r6);	 Catch:{ ParseException -> 0x01c6 }
    L_0x00c4:
        r5 = "rport";
        r0 = r28;
        r6 = r0.peerPort;	 Catch:{ ParseException -> 0x01c6 }
        r6 = java.lang.Integer.toString(r6);	 Catch:{ ParseException -> 0x01c6 }
        r0 = r25;
        r0.setParameter(r5, r6);	 Catch:{ ParseException -> 0x01c6 }
    L_0x00d3:
        r0 = r28;
        r5 = r0.isCached;	 Catch:{ all -> 0x01c4 }
        if (r5 != 0) goto L_0x0118;
    L_0x00d9:
        r0 = r28;
        r5 = r0.mySock;	 Catch:{ all -> 0x01c4 }
        if (r5 == 0) goto L_0x0118;
    L_0x00df:
        r5 = 1;
        r0 = r28;
        r0.isCached = r5;	 Catch:{ all -> 0x01c4 }
        r0 = r28;
        r5 = r0.mySock;	 Catch:{ all -> 0x01c4 }
        r5 = r5.getRemoteSocketAddress();	 Catch:{ all -> 0x01c4 }
        r5 = (java.net.InetSocketAddress) r5;	 Catch:{ all -> 0x01c4 }
        r17 = r5.getPort();	 Catch:{ all -> 0x01c4 }
        r0 = r28;
        r5 = r0.mySock;	 Catch:{ all -> 0x01c4 }
        r5 = r5.getInetAddress();	 Catch:{ all -> 0x01c4 }
        r0 = r17;
        r15 = org.jitsi.gov.nist.javax.sip.stack.IOHandler.makeKey(r5, r0);	 Catch:{ all -> 0x01c4 }
        r0 = r28;
        r5 = r0.sipStack;	 Catch:{ all -> 0x01c4 }
        r5 = r5.ioHandler;	 Catch:{ all -> 0x01c4 }
        r0 = r28;
        r6 = r0.mySock;	 Catch:{ all -> 0x01c4 }
        r5.putSocket(r15, r6);	 Catch:{ all -> 0x01c4 }
        r0 = r28;
        r5 = r0.messageProcessor;	 Catch:{ all -> 0x01c4 }
        r5 = (org.jitsi.gov.nist.javax.sip.stack.TCPMessageProcessor) r5;	 Catch:{ all -> 0x01c4 }
        r0 = r28;
        r5.cacheMessageChannel(r0);	 Catch:{ all -> 0x01c4 }
    L_0x0118:
        r10 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x01c4 }
        r0 = r29;
        r5 = r0 instanceof org.jitsi.gov.nist.javax.sip.message.SIPRequest;	 Catch:{ all -> 0x01c4 }
        if (r5 == 0) goto L_0x02bc;
    L_0x0122:
        r0 = r29;
        r0 = (org.jitsi.gov.nist.javax.sip.message.SIPRequest) r0;	 Catch:{ all -> 0x01c4 }
        r19 = r0;
        r5 = logger;	 Catch:{ all -> 0x01c4 }
        r6 = 32;
        r5 = r5.isLoggingEnabled(r6);	 Catch:{ all -> 0x01c4 }
        if (r5 == 0) goto L_0x0139;
    L_0x0132:
        r5 = logger;	 Catch:{ all -> 0x01c4 }
        r6 = "----Processing Message---";
        r5.logDebug(r6);	 Catch:{ all -> 0x01c4 }
    L_0x0139:
        r5 = logger;	 Catch:{ all -> 0x01c4 }
        r6 = 16;
        r5 = r5.isLoggingEnabled(r6);	 Catch:{ all -> 0x01c4 }
        if (r5 == 0) goto L_0x0182;
    L_0x0143:
        r0 = r28;
        r5 = r0.sipStack;	 Catch:{ all -> 0x01c4 }
        r5 = r5.serverLogger;	 Catch:{ all -> 0x01c4 }
        r6 = r28.getPeerHostPort();	 Catch:{ all -> 0x01c4 }
        r7 = r6.toString();	 Catch:{ all -> 0x01c4 }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x01c4 }
        r6.<init>();	 Catch:{ all -> 0x01c4 }
        r8 = r28.getMessageProcessor();	 Catch:{ all -> 0x01c4 }
        r8 = r8.getIpAddress();	 Catch:{ all -> 0x01c4 }
        r8 = r8.getHostAddress();	 Catch:{ all -> 0x01c4 }
        r6 = r6.append(r8);	 Catch:{ all -> 0x01c4 }
        r8 = ":";
        r6 = r6.append(r8);	 Catch:{ all -> 0x01c4 }
        r8 = r28.getMessageProcessor();	 Catch:{ all -> 0x01c4 }
        r8 = r8.getPort();	 Catch:{ all -> 0x01c4 }
        r6 = r6.append(r8);	 Catch:{ all -> 0x01c4 }
        r8 = r6.toString();	 Catch:{ all -> 0x01c4 }
        r9 = 0;
        r6 = r29;
        r5.logMessage(r6, r7, r8, r9, r10);	 Catch:{ all -> 0x01c4 }
    L_0x0182:
        r0 = r28;
        r5 = r0.sipStack;	 Catch:{ all -> 0x01c4 }
        r5 = r5.getMaxMessageSize();	 Catch:{ all -> 0x01c4 }
        if (r5 <= 0) goto L_0x01d7;
    L_0x018c:
        r6 = r19.getSize();	 Catch:{ all -> 0x01c4 }
        r5 = r19.getContentLength();	 Catch:{ all -> 0x01c4 }
        if (r5 != 0) goto L_0x01ce;
    L_0x0196:
        r5 = 0;
    L_0x0197:
        r5 = r5 + r6;
        r0 = r28;
        r6 = r0.sipStack;	 Catch:{ all -> 0x01c4 }
        r6 = r6.getMaxMessageSize();	 Catch:{ all -> 0x01c4 }
        if (r5 <= r6) goto L_0x01d7;
    L_0x01a2:
        r5 = 513; // 0x201 float:7.19E-43 double:2.535E-321;
        r0 = r19;
        r20 = r0.createResponse(r5);	 Catch:{ all -> 0x01c4 }
        r5 = r28.getTransport();	 Catch:{ all -> 0x01c4 }
        r0 = r20;
        r18 = r0.encodeAsBytes(r5);	 Catch:{ all -> 0x01c4 }
        r5 = 0;
        r0 = r28;
        r1 = r18;
        r0.sendMessage(r1, r5);	 Catch:{ all -> 0x01c4 }
        r5 = new java.lang.Exception;	 Catch:{ all -> 0x01c4 }
        r6 = "Message size exceeded";
        r5.<init>(r6);	 Catch:{ all -> 0x01c4 }
        throw r5;	 Catch:{ all -> 0x01c4 }
    L_0x01c4:
        r5 = move-exception;
        throw r5;
    L_0x01c6:
        r13 = move-exception;
        r5 = logger;	 Catch:{ all -> 0x01c4 }
        org.jitsi.gov.nist.core.InternalErrorHandler.handleException(r13, r5);	 Catch:{ all -> 0x01c4 }
        goto L_0x00d3;
    L_0x01ce:
        r5 = r19.getContentLength();	 Catch:{ all -> 0x01c4 }
        r5 = r5.getContentLength();	 Catch:{ all -> 0x01c4 }
        goto L_0x0197;
    L_0x01d7:
        r0 = r29;
        r0 = (org.jitsi.gov.nist.javax.sip.message.SIPRequest) r0;	 Catch:{ all -> 0x01c4 }
        r5 = r0;
        r5 = r5.getRequestLine();	 Catch:{ all -> 0x01c4 }
        r24 = r5.getSipVersion();	 Catch:{ all -> 0x01c4 }
        r5 = "SIP/2.0";
        r0 = r24;
        r5 = r0.equals(r5);	 Catch:{ all -> 0x01c4 }
        if (r5 != 0) goto L_0x0225;
    L_0x01ee:
        r29 = (org.jitsi.gov.nist.javax.sip.message.SIPRequest) r29;	 Catch:{ all -> 0x01c4 }
        r5 = 505; // 0x1f9 float:7.08E-43 double:2.495E-321;
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x01c4 }
        r6.<init>();	 Catch:{ all -> 0x01c4 }
        r7 = "Bad version ";
        r6 = r6.append(r7);	 Catch:{ all -> 0x01c4 }
        r0 = r24;
        r6 = r6.append(r0);	 Catch:{ all -> 0x01c4 }
        r6 = r6.toString();	 Catch:{ all -> 0x01c4 }
        r0 = r29;
        r26 = r0.createResponse(r5, r6);	 Catch:{ all -> 0x01c4 }
        r5 = r28.getTransport();	 Catch:{ all -> 0x01c4 }
        r0 = r26;
        r5 = r0.encodeAsBytes(r5);	 Catch:{ all -> 0x01c4 }
        r6 = 0;
        r0 = r28;
        r0.sendMessage(r5, r6);	 Catch:{ all -> 0x01c4 }
        r5 = new java.lang.Exception;	 Catch:{ all -> 0x01c4 }
        r6 = "Bad sip version";
        r5.<init>(r6);	 Catch:{ all -> 0x01c4 }
        throw r5;	 Catch:{ all -> 0x01c4 }
    L_0x0225:
        r0 = r29;
        r0 = (org.jitsi.gov.nist.javax.sip.message.SIPRequest) r0;	 Catch:{ all -> 0x01c4 }
        r5 = r0;
        r16 = r5.getMethod();	 Catch:{ all -> 0x01c4 }
        r29 = (org.jitsi.gov.nist.javax.sip.message.SIPRequest) r29;	 Catch:{ all -> 0x01c4 }
        r5 = r29.getCSeqHeader();	 Catch:{ all -> 0x01c4 }
        r12 = r5.getMethod();	 Catch:{ all -> 0x01c4 }
        r0 = r16;
        r5 = r0.equalsIgnoreCase(r12);	 Catch:{ all -> 0x01c4 }
        if (r5 != 0) goto L_0x0262;
    L_0x0240:
        r5 = 400; // 0x190 float:5.6E-43 double:1.976E-321;
        r0 = r19;
        r20 = r0.createResponse(r5);	 Catch:{ all -> 0x01c4 }
        r5 = r28.getTransport();	 Catch:{ all -> 0x01c4 }
        r0 = r20;
        r18 = r0.encodeAsBytes(r5);	 Catch:{ all -> 0x01c4 }
        r5 = 0;
        r0 = r28;
        r1 = r18;
        r0.sendMessage(r1, r5);	 Catch:{ all -> 0x01c4 }
        r5 = new java.lang.Exception;	 Catch:{ all -> 0x01c4 }
        r6 = "Bad CSeq method";
        r5.<init>(r6);	 Catch:{ all -> 0x01c4 }
        throw r5;	 Catch:{ all -> 0x01c4 }
    L_0x0262:
        r0 = r28;
        r5 = r0.sipStack;	 Catch:{ all -> 0x01c4 }
        r0 = r19;
        r1 = r28;
        r21 = r5.newSIPServerRequest(r0, r1);	 Catch:{ all -> 0x01c4 }
        if (r21 == 0) goto L_0x02ab;
    L_0x0270:
        r0 = r21;
        r1 = r19;
        r2 = r28;
        r0.processRequest(r1, r2);	 Catch:{ all -> 0x0292 }
        r0 = r21;
        r5 = r0 instanceof org.jitsi.gov.nist.javax.sip.stack.SIPTransaction;	 Catch:{ all -> 0x01c4 }
        if (r5 == 0) goto L_0x0038;
    L_0x027f:
        r0 = r21;
        r0 = (org.jitsi.gov.nist.javax.sip.stack.SIPServerTransaction) r0;	 Catch:{ all -> 0x01c4 }
        r23 = r0;
        r5 = r23.passToListener();	 Catch:{ all -> 0x01c4 }
        if (r5 != 0) goto L_0x0038;
    L_0x028b:
        r21 = (org.jitsi.gov.nist.javax.sip.stack.SIPTransaction) r21;	 Catch:{ all -> 0x01c4 }
        r21.releaseSem();	 Catch:{ all -> 0x01c4 }
        goto L_0x0038;
    L_0x0292:
        r5 = move-exception;
        r0 = r21;
        r6 = r0 instanceof org.jitsi.gov.nist.javax.sip.stack.SIPTransaction;	 Catch:{ all -> 0x01c4 }
        if (r6 == 0) goto L_0x02aa;
    L_0x0299:
        r0 = r21;
        r0 = (org.jitsi.gov.nist.javax.sip.stack.SIPServerTransaction) r0;	 Catch:{ all -> 0x01c4 }
        r23 = r0;
        r6 = r23.passToListener();	 Catch:{ all -> 0x01c4 }
        if (r6 != 0) goto L_0x02aa;
    L_0x02a5:
        r21 = (org.jitsi.gov.nist.javax.sip.stack.SIPTransaction) r21;	 Catch:{ all -> 0x01c4 }
        r21.releaseSem();	 Catch:{ all -> 0x01c4 }
    L_0x02aa:
        throw r5;	 Catch:{ all -> 0x01c4 }
    L_0x02ab:
        r5 = logger;	 Catch:{ all -> 0x01c4 }
        r5 = r5.isLoggingEnabled();	 Catch:{ all -> 0x01c4 }
        if (r5 == 0) goto L_0x0038;
    L_0x02b3:
        r5 = logger;	 Catch:{ all -> 0x01c4 }
        r6 = "Dropping request -- could not acquire semaphore in 10 sec";
        r5.logWarning(r6);	 Catch:{ all -> 0x01c4 }
        goto L_0x0038;
    L_0x02bc:
        r0 = r29;
        r0 = (org.jitsi.gov.nist.javax.sip.message.SIPResponse) r0;	 Catch:{ all -> 0x01c4 }
        r20 = r0;
        r20.checkHeaders();	 Catch:{ ParseException -> 0x02f8 }
        r0 = r28;
        r5 = r0.sipStack;	 Catch:{ all -> 0x01c4 }
        r5 = r5.getMaxMessageSize();	 Catch:{ all -> 0x01c4 }
        if (r5 <= 0) goto L_0x0326;
    L_0x02cf:
        r6 = r20.getSize();	 Catch:{ all -> 0x01c4 }
        r5 = r20.getContentLength();	 Catch:{ all -> 0x01c4 }
        if (r5 != 0) goto L_0x031d;
    L_0x02d9:
        r5 = 0;
    L_0x02da:
        r5 = r5 + r6;
        r0 = r28;
        r6 = r0.sipStack;	 Catch:{ all -> 0x01c4 }
        r6 = r6.getMaxMessageSize();	 Catch:{ all -> 0x01c4 }
        if (r5 <= r6) goto L_0x0326;
    L_0x02e5:
        r5 = logger;	 Catch:{ all -> 0x01c4 }
        r6 = 32;
        r5 = r5.isLoggingEnabled(r6);	 Catch:{ all -> 0x01c4 }
        if (r5 == 0) goto L_0x0038;
    L_0x02ef:
        r5 = logger;	 Catch:{ all -> 0x01c4 }
        r6 = "Message size exceeded";
        r5.logDebug(r6);	 Catch:{ all -> 0x01c4 }
        goto L_0x0038;
    L_0x02f8:
        r13 = move-exception;
        r5 = logger;	 Catch:{ all -> 0x01c4 }
        r5 = r5.isLoggingEnabled();	 Catch:{ all -> 0x01c4 }
        if (r5 == 0) goto L_0x0038;
    L_0x0301:
        r5 = logger;	 Catch:{ all -> 0x01c4 }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x01c4 }
        r6.<init>();	 Catch:{ all -> 0x01c4 }
        r7 = "Dropping Badly formatted response message >>> ";
        r6 = r6.append(r7);	 Catch:{ all -> 0x01c4 }
        r0 = r20;
        r6 = r6.append(r0);	 Catch:{ all -> 0x01c4 }
        r6 = r6.toString();	 Catch:{ all -> 0x01c4 }
        r5.logError(r6);	 Catch:{ all -> 0x01c4 }
        goto L_0x0038;
    L_0x031d:
        r5 = r20.getContentLength();	 Catch:{ all -> 0x01c4 }
        r5 = r5.getContentLength();	 Catch:{ all -> 0x01c4 }
        goto L_0x02da;
    L_0x0326:
        r0 = r28;
        r5 = r0.sipStack;	 Catch:{ all -> 0x01c4 }
        r0 = r20;
        r1 = r28;
        r22 = r5.newSIPServerResponse(r0, r1);	 Catch:{ all -> 0x01c4 }
        if (r22 == 0) goto L_0x03bb;
    L_0x0334:
        r0 = r22;
        r5 = r0 instanceof org.jitsi.gov.nist.javax.sip.stack.SIPClientTransaction;	 Catch:{ all -> 0x03a2 }
        if (r5 == 0) goto L_0x0381;
    L_0x033a:
        r0 = r22;
        r0 = (org.jitsi.gov.nist.javax.sip.stack.SIPClientTransaction) r0;	 Catch:{ all -> 0x03a2 }
        r5 = r0;
        r0 = r20;
        r5 = r5.checkFromTag(r0);	 Catch:{ all -> 0x03a2 }
        if (r5 != 0) goto L_0x0381;
    L_0x0347:
        r5 = logger;	 Catch:{ all -> 0x03a2 }
        r5 = r5.isLoggingEnabled();	 Catch:{ all -> 0x03a2 }
        if (r5 == 0) goto L_0x0369;
    L_0x034f:
        r5 = logger;	 Catch:{ all -> 0x03a2 }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x03a2 }
        r6.<init>();	 Catch:{ all -> 0x03a2 }
        r7 = "Dropping response message with invalid tag >>> ";
        r6 = r6.append(r7);	 Catch:{ all -> 0x03a2 }
        r0 = r20;
        r6 = r6.append(r0);	 Catch:{ all -> 0x03a2 }
        r6 = r6.toString();	 Catch:{ all -> 0x03a2 }
        r5.logError(r6);	 Catch:{ all -> 0x03a2 }
    L_0x0369:
        r0 = r22;
        r5 = r0 instanceof org.jitsi.gov.nist.javax.sip.stack.SIPTransaction;	 Catch:{ all -> 0x01c4 }
        if (r5 == 0) goto L_0x0038;
    L_0x036f:
        r0 = r22;
        r0 = (org.jitsi.gov.nist.javax.sip.stack.SIPTransaction) r0;	 Catch:{ all -> 0x01c4 }
        r5 = r0;
        r5 = r5.passToListener();	 Catch:{ all -> 0x01c4 }
        if (r5 != 0) goto L_0x0038;
    L_0x037a:
        r22 = (org.jitsi.gov.nist.javax.sip.stack.SIPTransaction) r22;	 Catch:{ all -> 0x01c4 }
        r22.releaseSem();	 Catch:{ all -> 0x01c4 }
        goto L_0x0038;
    L_0x0381:
        r0 = r22;
        r1 = r20;
        r2 = r28;
        r0.processResponse(r1, r2);	 Catch:{ all -> 0x03a2 }
        r0 = r22;
        r5 = r0 instanceof org.jitsi.gov.nist.javax.sip.stack.SIPTransaction;	 Catch:{ all -> 0x01c4 }
        if (r5 == 0) goto L_0x0038;
    L_0x0390:
        r0 = r22;
        r0 = (org.jitsi.gov.nist.javax.sip.stack.SIPTransaction) r0;	 Catch:{ all -> 0x01c4 }
        r5 = r0;
        r5 = r5.passToListener();	 Catch:{ all -> 0x01c4 }
        if (r5 != 0) goto L_0x0038;
    L_0x039b:
        r22 = (org.jitsi.gov.nist.javax.sip.stack.SIPTransaction) r22;	 Catch:{ all -> 0x01c4 }
        r22.releaseSem();	 Catch:{ all -> 0x01c4 }
        goto L_0x0038;
    L_0x03a2:
        r5 = move-exception;
        r6 = r5;
        r0 = r22;
        r5 = r0 instanceof org.jitsi.gov.nist.javax.sip.stack.SIPTransaction;	 Catch:{ all -> 0x01c4 }
        if (r5 == 0) goto L_0x03ba;
    L_0x03aa:
        r0 = r22;
        r0 = (org.jitsi.gov.nist.javax.sip.stack.SIPTransaction) r0;	 Catch:{ all -> 0x01c4 }
        r5 = r0;
        r5 = r5.passToListener();	 Catch:{ all -> 0x01c4 }
        if (r5 != 0) goto L_0x03ba;
    L_0x03b5:
        r22 = (org.jitsi.gov.nist.javax.sip.stack.SIPTransaction) r22;	 Catch:{ all -> 0x01c4 }
        r22.releaseSem();	 Catch:{ all -> 0x01c4 }
    L_0x03ba:
        throw r6;	 Catch:{ all -> 0x01c4 }
    L_0x03bb:
        r5 = logger;	 Catch:{ all -> 0x01c4 }
        r6 = "Application is blocked -- could not acquire semaphore -- dropping response";
        r5.logWarning(r6);	 Catch:{ all -> 0x01c4 }
        goto L_0x0038;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.gov.nist.javax.sip.stack.TCPMessageChannel.processMessage(org.jitsi.gov.nist.javax.sip.message.SIPMessage):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:64:0x0118 A:{ExcHandler: Exception (r1_0 'ex' java.lang.Exception), Splitter:B:13:0x0062} */
    /* JADX WARNING: Missing block: B:64:0x0118, code skipped:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:66:?, code skipped:
            org.jitsi.gov.nist.core.InternalErrorHandler.handleException(r1, logger);
     */
    public void run() {
        /*
        r10 = this;
        r9 = -1;
        r8 = 0;
        r2 = 0;
        r2 = new org.jitsi.gov.nist.javax.sip.parser.Pipeline;
        r5 = r10.myClientInputStream;
        r6 = r10.sipStack;
        r6 = r6.readTimeout;
        r7 = r10.sipStack;
        r7 = r7.getTimer();
        r2.m1391init(r5, r6, r7);
        r5 = new org.jitsi.gov.nist.javax.sip.parser.PipelinedMsgParser;
        r6 = r10.sipStack;
        r7 = r10.sipStack;
        r7 = r7.getMaxMessageSize();
        r5.m1399init(r6, r10, r2, r7);
        r10.myParser = r5;
        r5 = r10.myParser;
        r5.processInput();
        r0 = 4096; // 0x1000 float:5.74E-42 double:2.0237E-320;
        r5 = r10.tcpMessageProcessor;
        r6 = r5.useCount;
        r6 = r6 + 1;
        r5.useCount = r6;
        r5 = 1;
        r10.isRunning = r5;
    L_0x0035:
        r3 = new byte[r0];	 Catch:{ IOException -> 0x009e, Exception -> 0x0118 }
        r5 = r10.myClientInputStream;	 Catch:{ IOException -> 0x009e, Exception -> 0x0118 }
        r6 = 0;
        r4 = r5.read(r3, r6, r0);	 Catch:{ IOException -> 0x009e, Exception -> 0x0118 }
        if (r4 != r9) goto L_0x0099;
    L_0x0040:
        r5 = "\r\n\r\n";
        r6 = "UTF-8";
        r5 = r5.getBytes(r6);	 Catch:{ IOException -> 0x009e, Exception -> 0x0118 }
        r2.write(r5);	 Catch:{ IOException -> 0x009e, Exception -> 0x0118 }
        r5 = r10.sipStack;	 Catch:{ IOException -> 0x0097, Exception -> 0x0118 }
        r5 = r5.maxConnections;	 Catch:{ IOException -> 0x0097, Exception -> 0x0118 }
        if (r5 == r9) goto L_0x0062;
    L_0x0051:
        r6 = r10.tcpMessageProcessor;	 Catch:{ IOException -> 0x0097, Exception -> 0x0118 }
        monitor-enter(r6);	 Catch:{ IOException -> 0x0097, Exception -> 0x0118 }
        r5 = r10.tcpMessageProcessor;	 Catch:{ all -> 0x0094 }
        r7 = r5.nConnections;	 Catch:{ all -> 0x0094 }
        r7 = r7 + -1;
        r5.nConnections = r7;	 Catch:{ all -> 0x0094 }
        r5 = r10.tcpMessageProcessor;	 Catch:{ all -> 0x0094 }
        r5.notify();	 Catch:{ all -> 0x0094 }
        monitor-exit(r6);	 Catch:{ all -> 0x0094 }
    L_0x0062:
        r2.close();	 Catch:{ IOException -> 0x0097, Exception -> 0x0118 }
        r5 = r10.mySock;	 Catch:{ IOException -> 0x0097, Exception -> 0x0118 }
        if (r5 == 0) goto L_0x007f;
    L_0x0069:
        r5 = logger;	 Catch:{ IOException -> 0x0097, Exception -> 0x0118 }
        r6 = 32;
        r5 = r5.isLoggingEnabled(r6);	 Catch:{ IOException -> 0x0097, Exception -> 0x0118 }
        if (r5 == 0) goto L_0x007a;
    L_0x0073:
        r5 = logger;	 Catch:{ IOException -> 0x0097, Exception -> 0x0118 }
        r6 = "Closing socket";
        r5.logDebug(r6);	 Catch:{ IOException -> 0x0097, Exception -> 0x0118 }
    L_0x007a:
        r5 = r10.mySock;	 Catch:{ IOException -> 0x0097, Exception -> 0x0118 }
        r5.close();	 Catch:{ IOException -> 0x0097, Exception -> 0x0118 }
    L_0x007f:
        r10.isRunning = r8;
        r5 = r10.tcpMessageProcessor;
        r5.remove(r10);
        r5 = r10.tcpMessageProcessor;
        r6 = r5.useCount;
        r6 = r6 + -1;
        r5.useCount = r6;
        r5 = r10.myParser;
    L_0x0090:
        r5.close();
        return;
    L_0x0094:
        r5 = move-exception;
        monitor-exit(r6);	 Catch:{ all -> 0x0094 }
        throw r5;	 Catch:{ IOException -> 0x0097, Exception -> 0x0118 }
    L_0x0097:
        r5 = move-exception;
        goto L_0x007f;
    L_0x0099:
        r5 = 0;
        r2.write(r3, r5, r4);	 Catch:{ IOException -> 0x009e, Exception -> 0x0118 }
        goto L_0x0035;
    L_0x009e:
        r1 = move-exception;
        r5 = "\r\n\r\n";
        r6 = "UTF-8";
        r5 = r5.getBytes(r6);	 Catch:{ Exception -> 0x0138 }
        r2.write(r5);	 Catch:{ Exception -> 0x0138 }
    L_0x00aa:
        r5 = logger;	 Catch:{ Exception -> 0x0136 }
        r6 = 32;
        r5 = r5.isLoggingEnabled(r6);	 Catch:{ Exception -> 0x0136 }
        if (r5 == 0) goto L_0x00cc;
    L_0x00b4:
        r5 = logger;	 Catch:{ Exception -> 0x0136 }
        r6 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0136 }
        r6.<init>();	 Catch:{ Exception -> 0x0136 }
        r7 = "IOException  closing sock ";
        r6 = r6.append(r7);	 Catch:{ Exception -> 0x0136 }
        r6 = r6.append(r1);	 Catch:{ Exception -> 0x0136 }
        r6 = r6.toString();	 Catch:{ Exception -> 0x0136 }
        r5.logDebug(r6);	 Catch:{ Exception -> 0x0136 }
    L_0x00cc:
        r5 = r10.sipStack;	 Catch:{ IOException -> 0x0116 }
        r5 = r5.maxConnections;	 Catch:{ IOException -> 0x0116 }
        if (r5 == r9) goto L_0x00e3;
    L_0x00d2:
        r6 = r10.tcpMessageProcessor;	 Catch:{ IOException -> 0x0116 }
        monitor-enter(r6);	 Catch:{ IOException -> 0x0116 }
        r5 = r10.tcpMessageProcessor;	 Catch:{ all -> 0x0113 }
        r7 = r5.nConnections;	 Catch:{ all -> 0x0113 }
        r7 = r7 + -1;
        r5.nConnections = r7;	 Catch:{ all -> 0x0113 }
        r5 = r10.tcpMessageProcessor;	 Catch:{ all -> 0x0113 }
        r5.notify();	 Catch:{ all -> 0x0113 }
        monitor-exit(r6);	 Catch:{ all -> 0x0113 }
    L_0x00e3:
        r5 = r10.mySock;	 Catch:{ IOException -> 0x0116 }
        if (r5 == 0) goto L_0x00fd;
    L_0x00e7:
        r5 = logger;	 Catch:{ IOException -> 0x0116 }
        r6 = 32;
        r5 = r5.isLoggingEnabled(r6);	 Catch:{ IOException -> 0x0116 }
        if (r5 == 0) goto L_0x00f8;
    L_0x00f1:
        r5 = logger;	 Catch:{ IOException -> 0x0116 }
        r6 = "Closing socket";
        r5.logDebug(r6);	 Catch:{ IOException -> 0x0116 }
    L_0x00f8:
        r5 = r10.mySock;	 Catch:{ IOException -> 0x0116 }
        r5.close();	 Catch:{ IOException -> 0x0116 }
    L_0x00fd:
        r2.close();	 Catch:{ IOException -> 0x0116 }
    L_0x0100:
        r10.isRunning = r8;
        r5 = r10.tcpMessageProcessor;
        r5.remove(r10);
        r5 = r10.tcpMessageProcessor;
        r6 = r5.useCount;
        r6 = r6 + -1;
        r5.useCount = r6;
        r5 = r10.myParser;
        goto L_0x0090;
    L_0x0113:
        r5 = move-exception;
        monitor-exit(r6);	 Catch:{ all -> 0x0113 }
        throw r5;	 Catch:{ IOException -> 0x0116 }
    L_0x0116:
        r5 = move-exception;
        goto L_0x0100;
    L_0x0118:
        r1 = move-exception;
        r5 = logger;	 Catch:{ all -> 0x0120 }
        org.jitsi.gov.nist.core.InternalErrorHandler.handleException(r1, r5);	 Catch:{ all -> 0x0120 }
        goto L_0x0035;
    L_0x0120:
        r5 = move-exception;
        r10.isRunning = r8;
        r6 = r10.tcpMessageProcessor;
        r6.remove(r10);
        r6 = r10.tcpMessageProcessor;
        r7 = r6.useCount;
        r7 = r7 + -1;
        r6.useCount = r7;
        r6 = r10.myParser;
        r6.close();
        throw r5;
    L_0x0136:
        r5 = move-exception;
        goto L_0x0100;
    L_0x0138:
        r5 = move-exception;
        goto L_0x00aa;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.gov.nist.javax.sip.stack.TCPMessageChannel.run():void");
    }

    /* access modifiers changed from: protected */
    public void uncache() {
        if (this.isCached && !this.isRunning) {
            this.tcpMessageProcessor.remove(this);
        }
    }

    public boolean equals(Object other) {
        if (!getClass().equals(other.getClass())) {
            return false;
        }
        if (this.mySock == ((TCPMessageChannel) other).mySock) {
            return true;
        }
        return false;
    }

    public String getKey() {
        if (this.key != null) {
            return this.key;
        }
        this.key = MessageChannel.getKey(this.peerAddress, this.peerPort, ListeningPoint.TCP);
        return this.key;
    }

    public String getViaHost() {
        return this.myAddress;
    }

    public int getViaPort() {
        return this.myPort;
    }

    public int getPeerPort() {
        return this.peerPort;
    }

    public int getPeerPacketSourcePort() {
        return this.peerPort;
    }

    public InetAddress getPeerPacketSourceAddress() {
        return this.peerAddress;
    }

    public boolean isSecure() {
        return false;
    }
}
