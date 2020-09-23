package org.jitsi.gov.nist.javax.sip.stack;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.text.ParseException;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import org.jitsi.gov.nist.core.CommonLogger;
import org.jitsi.gov.nist.core.StackLogger;
import org.jitsi.gov.nist.javax.sip.address.ParameterNames;
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

public final class TLSMessageChannel extends MessageChannel implements SIPMessageListener, Runnable, RawMessageChannel {
    private static StackLogger logger = CommonLogger.getLogger(TLSMessageChannel.class);
    private HandshakeCompletedListener handshakeCompletedListener;
    protected boolean isCached;
    protected boolean isRunning = true;
    private String key;
    private String myAddress;
    private InputStream myClientInputStream;
    private PipelinedMsgParser myParser;
    private int myPort;
    private Socket mySock;
    private Thread mythread;
    private InetAddress peerAddress;
    private int peerPort;
    private String peerProtocol;
    private SIPTransactionStack sipStack;
    private TLSMessageProcessor tlsMessageProcessor;

    protected TLSMessageChannel(Socket sock, SIPTransactionStack sipStack, TLSMessageProcessor msgProcessor, String threadName) throws IOException {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("creating new TLSMessageChannel (incoming)");
            logger.logStackTrace();
        }
        this.mySock = (SSLSocket) sock;
        if (sock instanceof SSLSocket) {
            try {
                SSLSocket sslSock = (SSLSocket) sock;
                if (!(sipStack.getClientAuth() == ClientAuthType.Want || sipStack.getClientAuth() == ClientAuthType.Disabled)) {
                    sslSock.setNeedClientAuth(true);
                }
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("SSLServerSocket need client auth " + sslSock.getNeedClientAuth());
                }
                this.handshakeCompletedListener = new HandshakeCompletedListenerImpl(this);
                sslSock.addHandshakeCompletedListener(this.handshakeCompletedListener);
                sslSock.startHandshake();
            } catch (SSLHandshakeException ex) {
                throw new IOException(ex.getMessage());
            }
        }
        this.peerAddress = this.mySock.getInetAddress();
        this.myAddress = msgProcessor.getIpAddress().getHostAddress();
        this.myClientInputStream = this.mySock.getInputStream();
        this.mythread = new Thread(this);
        this.mythread.setDaemon(true);
        this.mythread.setName(threadName);
        this.sipStack = sipStack;
        this.tlsMessageProcessor = msgProcessor;
        this.myPort = this.tlsMessageProcessor.getPort();
        this.peerPort = this.mySock.getPort();
        this.messageProcessor = msgProcessor;
        this.mythread.start();
    }

    protected TLSMessageChannel(InetAddress inetAddr, int port, SIPTransactionStack sipStack, TLSMessageProcessor messageProcessor) throws IOException {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("creating new TLSMessageChannel (outgoing)");
            logger.logStackTrace();
        }
        this.peerAddress = inetAddr;
        this.peerPort = port;
        this.myPort = messageProcessor.getPort();
        this.peerProtocol = ListeningPoint.TLS;
        this.sipStack = sipStack;
        this.tlsMessageProcessor = messageProcessor;
        this.myAddress = messageProcessor.getIpAddress().getHostAddress();
        this.key = MessageChannel.getKey(this.peerAddress, this.peerPort, ListeningPoint.TLS);
        this.messageProcessor = messageProcessor;
    }

    public boolean isReliable() {
        return true;
    }

    public void close() {
        try {
            if (this.mySock != null) {
                this.mySock.close();
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
        return ParameterNames.TLS;
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

    private void sendMessage(byte[] msg, boolean retry) throws IOException {
        Socket sock = this.sipStack.ioHandler.sendBytes(getMessageProcessor().getIpAddress(), this.peerAddress, this.peerPort, this.peerProtocol, msg, retry, this);
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
            Thread thread = new Thread(this);
            thread.setDaemon(true);
            thread.setName("TLSMessageChannelThread");
            thread.start();
        }
    }

    public void sendMessage(SIPMessage sipMessage) throws IOException {
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
        Socket sock = this.sipStack.ioHandler.sendBytes(this.messageProcessor.getIpAddress(), receiverAddress, receiverPort, ListeningPoint.TLS, message, retry, this);
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
            Thread mythread = new Thread(this);
            mythread.setDaemon(true);
            mythread.setName("TLSMessageChannelThread");
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
            logger.logDebug("Encountered bad message \n" + message);
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

    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    public void processMessage(org.jitsi.gov.nist.javax.sip.message.SIPMessage r30) throws java.lang.Exception {
        /*
        r29 = this;
        r0 = r29;
        r5 = r0.peerAddress;	 Catch:{ all -> 0x01bd }
        r0 = r30;
        r0.setRemoteAddress(r5);	 Catch:{ all -> 0x01bd }
        r5 = r29.getPeerPort();	 Catch:{ all -> 0x01bd }
        r0 = r30;
        r0.setRemotePort(r5);	 Catch:{ all -> 0x01bd }
        r5 = r29.getMessageProcessor();	 Catch:{ all -> 0x01bd }
        r5 = r5.getIpAddress();	 Catch:{ all -> 0x01bd }
        r0 = r30;
        r0.setLocalAddress(r5);	 Catch:{ all -> 0x01bd }
        r5 = r29.getPort();	 Catch:{ all -> 0x01bd }
        r0 = r30;
        r0.setLocalPort(r5);	 Catch:{ all -> 0x01bd }
        r5 = r30.getFrom();	 Catch:{ all -> 0x01bd }
        if (r5 == 0) goto L_0x0046;
    L_0x002e:
        r5 = r30.getTo();	 Catch:{ all -> 0x01bd }
        if (r5 == 0) goto L_0x0046;
    L_0x0034:
        r5 = r30.getCallId();	 Catch:{ all -> 0x01bd }
        if (r5 == 0) goto L_0x0046;
    L_0x003a:
        r5 = r30.getCSeq();	 Catch:{ all -> 0x01bd }
        if (r5 == 0) goto L_0x0046;
    L_0x0040:
        r5 = r30.getViaHeaders();	 Catch:{ all -> 0x01bd }
        if (r5 != 0) goto L_0x0072;
    L_0x0046:
        r4 = r30.encode();	 Catch:{ all -> 0x01bd }
        r5 = logger;	 Catch:{ all -> 0x01bd }
        r5 = r5.isLoggingEnabled();	 Catch:{ all -> 0x01bd }
        if (r5 == 0) goto L_0x0071;
    L_0x0052:
        r5 = logger;	 Catch:{ all -> 0x01bd }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x01bd }
        r6.<init>();	 Catch:{ all -> 0x01bd }
        r7 = "bad message ";
        r6 = r6.append(r7);	 Catch:{ all -> 0x01bd }
        r6 = r6.append(r4);	 Catch:{ all -> 0x01bd }
        r6 = r6.toString();	 Catch:{ all -> 0x01bd }
        r5.logError(r6);	 Catch:{ all -> 0x01bd }
        r5 = logger;	 Catch:{ all -> 0x01bd }
        r6 = ">>> Dropped Bad Msg";
        r5.logError(r6);	 Catch:{ all -> 0x01bd }
    L_0x0071:
        return;
    L_0x0072:
        r28 = r30.getViaHeaders();	 Catch:{ all -> 0x01bd }
        r0 = r30;
        r5 = r0 instanceof org.jitsi.gov.nist.javax.sip.message.SIPRequest;	 Catch:{ all -> 0x01bd }
        if (r5 == 0) goto L_0x0111;
    L_0x007c:
        r26 = r28.getFirst();	 Catch:{ all -> 0x01bd }
        r26 = (org.jitsi.gov.nist.javax.sip.header.Via) r26;	 Catch:{ all -> 0x01bd }
        r0 = r29;
        r5 = r0.sipStack;	 Catch:{ all -> 0x01bd }
        r5 = r5.addressResolver;	 Catch:{ all -> 0x01bd }
        r6 = r26.getHop();	 Catch:{ all -> 0x01bd }
        r14 = r5.resolveAddress(r6);	 Catch:{ all -> 0x01bd }
        r5 = r26.getTransport();	 Catch:{ all -> 0x01bd }
        r0 = r29;
        r0.peerProtocol = r5;	 Catch:{ all -> 0x01bd }
        r0 = r29;
        r5 = r0.mySock;	 Catch:{ ParseException -> 0x01bf }
        r5 = r5.getInetAddress();	 Catch:{ ParseException -> 0x01bf }
        r0 = r29;
        r0.peerAddress = r5;	 Catch:{ ParseException -> 0x01bf }
        r5 = "rport";
        r0 = r26;
        r5 = r0.hasParameter(r5);	 Catch:{ ParseException -> 0x01bf }
        if (r5 != 0) goto L_0x00c0;
    L_0x00ae:
        r5 = r14.getHost();	 Catch:{ ParseException -> 0x01bf }
        r0 = r29;
        r6 = r0.peerAddress;	 Catch:{ ParseException -> 0x01bf }
        r6 = r6.getHostAddress();	 Catch:{ ParseException -> 0x01bf }
        r5 = r5.equals(r6);	 Catch:{ ParseException -> 0x01bf }
        if (r5 != 0) goto L_0x00cf;
    L_0x00c0:
        r5 = "received";
        r0 = r29;
        r6 = r0.peerAddress;	 Catch:{ ParseException -> 0x01bf }
        r6 = r6.getHostAddress();	 Catch:{ ParseException -> 0x01bf }
        r0 = r26;
        r0.setParameter(r5, r6);	 Catch:{ ParseException -> 0x01bf }
    L_0x00cf:
        r5 = "rport";
        r0 = r29;
        r6 = r0.peerPort;	 Catch:{ ParseException -> 0x01bf }
        r6 = java.lang.Integer.toString(r6);	 Catch:{ ParseException -> 0x01bf }
        r0 = r26;
        r0.setParameter(r5, r6);	 Catch:{ ParseException -> 0x01bf }
    L_0x00de:
        r0 = r29;
        r5 = r0.isCached;	 Catch:{ all -> 0x01bd }
        if (r5 != 0) goto L_0x0111;
    L_0x00e4:
        r0 = r29;
        r5 = r0.messageProcessor;	 Catch:{ all -> 0x01bd }
        r5 = (org.jitsi.gov.nist.javax.sip.stack.TLSMessageProcessor) r5;	 Catch:{ all -> 0x01bd }
        r0 = r29;
        r5.cacheMessageChannel(r0);	 Catch:{ all -> 0x01bd }
        r5 = 1;
        r0 = r29;
        r0.isCached = r5;	 Catch:{ all -> 0x01bd }
        r0 = r29;
        r5 = r0.mySock;	 Catch:{ all -> 0x01bd }
        r5 = r5.getInetAddress();	 Catch:{ all -> 0x01bd }
        r0 = r29;
        r6 = r0.peerPort;	 Catch:{ all -> 0x01bd }
        r15 = org.jitsi.gov.nist.javax.sip.stack.IOHandler.makeKey(r5, r6);	 Catch:{ all -> 0x01bd }
        r0 = r29;
        r5 = r0.sipStack;	 Catch:{ all -> 0x01bd }
        r5 = r5.ioHandler;	 Catch:{ all -> 0x01bd }
        r0 = r29;
        r6 = r0.mySock;	 Catch:{ all -> 0x01bd }
        r5.putSocket(r15, r6);	 Catch:{ all -> 0x01bd }
    L_0x0111:
        r10 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x01bd }
        r0 = r30;
        r5 = r0 instanceof org.jitsi.gov.nist.javax.sip.message.SIPRequest;	 Catch:{ all -> 0x01bd }
        if (r5 == 0) goto L_0x02d7;
    L_0x011b:
        r0 = r30;
        r0 = (org.jitsi.gov.nist.javax.sip.message.SIPRequest) r0;	 Catch:{ all -> 0x01bd }
        r20 = r0;
        r5 = logger;	 Catch:{ all -> 0x01bd }
        r6 = 32;
        r5 = r5.isLoggingEnabled(r6);	 Catch:{ all -> 0x01bd }
        if (r5 == 0) goto L_0x0132;
    L_0x012b:
        r5 = logger;	 Catch:{ all -> 0x01bd }
        r6 = "----Processing Message---";
        r5.logDebug(r6);	 Catch:{ all -> 0x01bd }
    L_0x0132:
        r5 = logger;	 Catch:{ all -> 0x01bd }
        r6 = 16;
        r5 = r5.isLoggingEnabled(r6);	 Catch:{ all -> 0x01bd }
        if (r5 == 0) goto L_0x017b;
    L_0x013c:
        r0 = r29;
        r5 = r0.sipStack;	 Catch:{ all -> 0x01bd }
        r5 = r5.serverLogger;	 Catch:{ all -> 0x01bd }
        r6 = r29.getPeerHostPort();	 Catch:{ all -> 0x01bd }
        r7 = r6.toString();	 Catch:{ all -> 0x01bd }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x01bd }
        r6.<init>();	 Catch:{ all -> 0x01bd }
        r0 = r29;
        r8 = r0.messageProcessor;	 Catch:{ all -> 0x01bd }
        r8 = r8.getIpAddress();	 Catch:{ all -> 0x01bd }
        r8 = r8.getHostAddress();	 Catch:{ all -> 0x01bd }
        r6 = r6.append(r8);	 Catch:{ all -> 0x01bd }
        r8 = ":";
        r6 = r6.append(r8);	 Catch:{ all -> 0x01bd }
        r0 = r29;
        r8 = r0.messageProcessor;	 Catch:{ all -> 0x01bd }
        r8 = r8.getPort();	 Catch:{ all -> 0x01bd }
        r6 = r6.append(r8);	 Catch:{ all -> 0x01bd }
        r8 = r6.toString();	 Catch:{ all -> 0x01bd }
        r9 = 0;
        r6 = r30;
        r5.logMessage(r6, r7, r8, r9, r10);	 Catch:{ all -> 0x01bd }
    L_0x017b:
        r0 = r29;
        r5 = r0.sipStack;	 Catch:{ all -> 0x01bd }
        r5 = r5.getMaxMessageSize();	 Catch:{ all -> 0x01bd }
        if (r5 <= 0) goto L_0x01ce;
    L_0x0185:
        r6 = r20.getSize();	 Catch:{ all -> 0x01bd }
        r5 = r20.getContentLength();	 Catch:{ all -> 0x01bd }
        if (r5 != 0) goto L_0x01c5;
    L_0x018f:
        r5 = 0;
    L_0x0190:
        r5 = r5 + r6;
        r0 = r29;
        r6 = r0.sipStack;	 Catch:{ all -> 0x01bd }
        r6 = r6.getMaxMessageSize();	 Catch:{ all -> 0x01bd }
        if (r5 <= r6) goto L_0x01ce;
    L_0x019b:
        r5 = 513; // 0x201 float:7.19E-43 double:2.535E-321;
        r0 = r20;
        r21 = r0.createResponse(r5);	 Catch:{ all -> 0x01bd }
        r5 = r29.getTransport();	 Catch:{ all -> 0x01bd }
        r0 = r21;
        r17 = r0.encodeAsBytes(r5);	 Catch:{ all -> 0x01bd }
        r5 = 0;
        r0 = r29;
        r1 = r17;
        r0.sendMessage(r1, r5);	 Catch:{ all -> 0x01bd }
        r5 = new java.lang.Exception;	 Catch:{ all -> 0x01bd }
        r6 = "Message size exceeded";
        r5.<init>(r6);	 Catch:{ all -> 0x01bd }
        throw r5;	 Catch:{ all -> 0x01bd }
    L_0x01bd:
        r5 = move-exception;
        throw r5;
    L_0x01bf:
        r13 = move-exception;
        org.jitsi.gov.nist.core.InternalErrorHandler.handleException(r13);	 Catch:{ all -> 0x01bd }
        goto L_0x00de;
    L_0x01c5:
        r5 = r20.getContentLength();	 Catch:{ all -> 0x01bd }
        r5 = r5.getContentLength();	 Catch:{ all -> 0x01bd }
        goto L_0x0190;
    L_0x01ce:
        r0 = r30;
        r0 = (org.jitsi.gov.nist.javax.sip.message.SIPRequest) r0;	 Catch:{ all -> 0x01bd }
        r5 = r0;
        r5 = r5.getRequestLine();	 Catch:{ all -> 0x01bd }
        r25 = r5.getSipVersion();	 Catch:{ all -> 0x01bd }
        r5 = "SIP/2.0";
        r0 = r25;
        r5 = r0.equals(r5);	 Catch:{ all -> 0x01bd }
        if (r5 != 0) goto L_0x021c;
    L_0x01e5:
        r30 = (org.jitsi.gov.nist.javax.sip.message.SIPRequest) r30;	 Catch:{ all -> 0x01bd }
        r5 = 505; // 0x1f9 float:7.08E-43 double:2.495E-321;
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x01bd }
        r6.<init>();	 Catch:{ all -> 0x01bd }
        r7 = "Bad SIP version ";
        r6 = r6.append(r7);	 Catch:{ all -> 0x01bd }
        r0 = r25;
        r6 = r6.append(r0);	 Catch:{ all -> 0x01bd }
        r6 = r6.toString();	 Catch:{ all -> 0x01bd }
        r0 = r30;
        r27 = r0.createResponse(r5, r6);	 Catch:{ all -> 0x01bd }
        r5 = r29.getTransport();	 Catch:{ all -> 0x01bd }
        r0 = r27;
        r5 = r0.encodeAsBytes(r5);	 Catch:{ all -> 0x01bd }
        r6 = 0;
        r0 = r29;
        r0.sendMessage(r5, r6);	 Catch:{ all -> 0x01bd }
        r5 = new java.lang.Exception;	 Catch:{ all -> 0x01bd }
        r6 = "Bad version ";
        r5.<init>(r6);	 Catch:{ all -> 0x01bd }
        throw r5;	 Catch:{ all -> 0x01bd }
    L_0x021c:
        r0 = r30;
        r0 = (org.jitsi.gov.nist.javax.sip.message.SIPRequest) r0;	 Catch:{ all -> 0x01bd }
        r5 = r0;
        r16 = r5.getMethod();	 Catch:{ all -> 0x01bd }
        r30 = (org.jitsi.gov.nist.javax.sip.message.SIPRequest) r30;	 Catch:{ all -> 0x01bd }
        r5 = r30.getCSeqHeader();	 Catch:{ all -> 0x01bd }
        r12 = r5.getMethod();	 Catch:{ all -> 0x01bd }
        r0 = r16;
        r5 = r0.equalsIgnoreCase(r12);	 Catch:{ all -> 0x01bd }
        if (r5 != 0) goto L_0x0259;
    L_0x0237:
        r5 = 400; // 0x190 float:5.6E-43 double:1.976E-321;
        r0 = r20;
        r21 = r0.createResponse(r5);	 Catch:{ all -> 0x01bd }
        r5 = r29.getTransport();	 Catch:{ all -> 0x01bd }
        r0 = r21;
        r17 = r0.encodeAsBytes(r5);	 Catch:{ all -> 0x01bd }
        r5 = 0;
        r0 = r29;
        r1 = r17;
        r0.sendMessage(r1, r5);	 Catch:{ all -> 0x01bd }
        r5 = new java.lang.Exception;	 Catch:{ all -> 0x01bd }
        r6 = "Bad CSeq method";
        r5.<init>(r6);	 Catch:{ all -> 0x01bd }
        throw r5;	 Catch:{ all -> 0x01bd }
    L_0x0259:
        r0 = r29;
        r5 = r0.sipStack;	 Catch:{ all -> 0x01bd }
        r0 = r20;
        r1 = r29;
        r22 = r5.newSIPServerRequest(r0, r1);	 Catch:{ all -> 0x01bd }
        if (r22 == 0) goto L_0x02a2;
    L_0x0267:
        r0 = r22;
        r1 = r20;
        r2 = r29;
        r0.processRequest(r1, r2);	 Catch:{ all -> 0x0289 }
        r0 = r22;
        r5 = r0 instanceof org.jitsi.gov.nist.javax.sip.stack.SIPTransaction;	 Catch:{ all -> 0x01bd }
        if (r5 == 0) goto L_0x0071;
    L_0x0276:
        r0 = r22;
        r0 = (org.jitsi.gov.nist.javax.sip.stack.SIPServerTransaction) r0;	 Catch:{ all -> 0x01bd }
        r24 = r0;
        r5 = r24.passToListener();	 Catch:{ all -> 0x01bd }
        if (r5 != 0) goto L_0x0071;
    L_0x0282:
        r22 = (org.jitsi.gov.nist.javax.sip.stack.SIPTransaction) r22;	 Catch:{ all -> 0x01bd }
        r22.releaseSem();	 Catch:{ all -> 0x01bd }
        goto L_0x0071;
    L_0x0289:
        r5 = move-exception;
        r0 = r22;
        r6 = r0 instanceof org.jitsi.gov.nist.javax.sip.stack.SIPTransaction;	 Catch:{ all -> 0x01bd }
        if (r6 == 0) goto L_0x02a1;
    L_0x0290:
        r0 = r22;
        r0 = (org.jitsi.gov.nist.javax.sip.stack.SIPServerTransaction) r0;	 Catch:{ all -> 0x01bd }
        r24 = r0;
        r6 = r24.passToListener();	 Catch:{ all -> 0x01bd }
        if (r6 != 0) goto L_0x02a1;
    L_0x029c:
        r22 = (org.jitsi.gov.nist.javax.sip.stack.SIPTransaction) r22;	 Catch:{ all -> 0x01bd }
        r22.releaseSem();	 Catch:{ all -> 0x01bd }
    L_0x02a1:
        throw r5;	 Catch:{ all -> 0x01bd }
    L_0x02a2:
        r5 = 503; // 0x1f7 float:7.05E-43 double:2.485E-321;
        r0 = r20;
        r18 = r0.createResponse(r5);	 Catch:{ all -> 0x01bd }
        r19 = new org.jitsi.gov.nist.javax.sip.header.RetryAfter;	 Catch:{ all -> 0x01bd }
        r19.m1213init();	 Catch:{ all -> 0x01bd }
        r6 = 4621819117588971520; // 0x4024000000000000 float:0.0 double:10.0;
        r8 = java.lang.Math.random();	 Catch:{ Exception -> 0x03df }
        r6 = r6 * r8;
        r5 = (int) r6;	 Catch:{ Exception -> 0x03df }
        r0 = r19;
        r0.setRetryAfter(r5);	 Catch:{ Exception -> 0x03df }
        r18.setHeader(r19);	 Catch:{ Exception -> 0x03df }
        r0 = r29;
        r1 = r18;
        r0.sendMessage(r1);	 Catch:{ Exception -> 0x03df }
    L_0x02c6:
        r5 = logger;	 Catch:{ all -> 0x01bd }
        r5 = r5.isLoggingEnabled();	 Catch:{ all -> 0x01bd }
        if (r5 == 0) goto L_0x0071;
    L_0x02ce:
        r5 = logger;	 Catch:{ all -> 0x01bd }
        r6 = "Dropping message -- could not acquire semaphore";
        r5.logWarning(r6);	 Catch:{ all -> 0x01bd }
        goto L_0x0071;
    L_0x02d7:
        r0 = r30;
        r0 = (org.jitsi.gov.nist.javax.sip.message.SIPResponse) r0;	 Catch:{ all -> 0x01bd }
        r21 = r0;
        r21.checkHeaders();	 Catch:{ ParseException -> 0x0313 }
        r0 = r29;
        r5 = r0.sipStack;	 Catch:{ all -> 0x01bd }
        r5 = r5.getMaxMessageSize();	 Catch:{ all -> 0x01bd }
        if (r5 <= 0) goto L_0x0341;
    L_0x02ea:
        r6 = r21.getSize();	 Catch:{ all -> 0x01bd }
        r5 = r21.getContentLength();	 Catch:{ all -> 0x01bd }
        if (r5 != 0) goto L_0x0338;
    L_0x02f4:
        r5 = 0;
    L_0x02f5:
        r5 = r5 + r6;
        r0 = r29;
        r6 = r0.sipStack;	 Catch:{ all -> 0x01bd }
        r6 = r6.getMaxMessageSize();	 Catch:{ all -> 0x01bd }
        if (r5 <= r6) goto L_0x0341;
    L_0x0300:
        r5 = logger;	 Catch:{ all -> 0x01bd }
        r6 = 32;
        r5 = r5.isLoggingEnabled(r6);	 Catch:{ all -> 0x01bd }
        if (r5 == 0) goto L_0x0071;
    L_0x030a:
        r5 = logger;	 Catch:{ all -> 0x01bd }
        r6 = "Message size exceeded";
        r5.logDebug(r6);	 Catch:{ all -> 0x01bd }
        goto L_0x0071;
    L_0x0313:
        r13 = move-exception;
        r5 = logger;	 Catch:{ all -> 0x01bd }
        r5 = r5.isLoggingEnabled();	 Catch:{ all -> 0x01bd }
        if (r5 == 0) goto L_0x0071;
    L_0x031c:
        r5 = logger;	 Catch:{ all -> 0x01bd }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x01bd }
        r6.<init>();	 Catch:{ all -> 0x01bd }
        r7 = "Dropping Badly formatted response message >>> ";
        r6 = r6.append(r7);	 Catch:{ all -> 0x01bd }
        r0 = r21;
        r6 = r6.append(r0);	 Catch:{ all -> 0x01bd }
        r6 = r6.toString();	 Catch:{ all -> 0x01bd }
        r5.logError(r6);	 Catch:{ all -> 0x01bd }
        goto L_0x0071;
    L_0x0338:
        r5 = r21.getContentLength();	 Catch:{ all -> 0x01bd }
        r5 = r5.getContentLength();	 Catch:{ all -> 0x01bd }
        goto L_0x02f5;
    L_0x0341:
        r0 = r29;
        r5 = r0.sipStack;	 Catch:{ all -> 0x01bd }
        r0 = r21;
        r1 = r29;
        r23 = r5.newSIPServerResponse(r0, r1);	 Catch:{ all -> 0x01bd }
        if (r23 == 0) goto L_0x03d6;
    L_0x034f:
        r0 = r23;
        r5 = r0 instanceof org.jitsi.gov.nist.javax.sip.stack.SIPClientTransaction;	 Catch:{ all -> 0x03bd }
        if (r5 == 0) goto L_0x039c;
    L_0x0355:
        r0 = r23;
        r0 = (org.jitsi.gov.nist.javax.sip.stack.SIPClientTransaction) r0;	 Catch:{ all -> 0x03bd }
        r5 = r0;
        r0 = r21;
        r5 = r5.checkFromTag(r0);	 Catch:{ all -> 0x03bd }
        if (r5 != 0) goto L_0x039c;
    L_0x0362:
        r5 = logger;	 Catch:{ all -> 0x03bd }
        r5 = r5.isLoggingEnabled();	 Catch:{ all -> 0x03bd }
        if (r5 == 0) goto L_0x0384;
    L_0x036a:
        r5 = logger;	 Catch:{ all -> 0x03bd }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x03bd }
        r6.<init>();	 Catch:{ all -> 0x03bd }
        r7 = "Dropping response message with invalid tag >>> ";
        r6 = r6.append(r7);	 Catch:{ all -> 0x03bd }
        r0 = r21;
        r6 = r6.append(r0);	 Catch:{ all -> 0x03bd }
        r6 = r6.toString();	 Catch:{ all -> 0x03bd }
        r5.logError(r6);	 Catch:{ all -> 0x03bd }
    L_0x0384:
        r0 = r23;
        r5 = r0 instanceof org.jitsi.gov.nist.javax.sip.stack.SIPTransaction;	 Catch:{ all -> 0x01bd }
        if (r5 == 0) goto L_0x0071;
    L_0x038a:
        r0 = r23;
        r0 = (org.jitsi.gov.nist.javax.sip.stack.SIPTransaction) r0;	 Catch:{ all -> 0x01bd }
        r5 = r0;
        r5 = r5.passToListener();	 Catch:{ all -> 0x01bd }
        if (r5 != 0) goto L_0x0071;
    L_0x0395:
        r23 = (org.jitsi.gov.nist.javax.sip.stack.SIPTransaction) r23;	 Catch:{ all -> 0x01bd }
        r23.releaseSem();	 Catch:{ all -> 0x01bd }
        goto L_0x0071;
    L_0x039c:
        r0 = r23;
        r1 = r21;
        r2 = r29;
        r0.processResponse(r1, r2);	 Catch:{ all -> 0x03bd }
        r0 = r23;
        r5 = r0 instanceof org.jitsi.gov.nist.javax.sip.stack.SIPTransaction;	 Catch:{ all -> 0x01bd }
        if (r5 == 0) goto L_0x0071;
    L_0x03ab:
        r0 = r23;
        r0 = (org.jitsi.gov.nist.javax.sip.stack.SIPTransaction) r0;	 Catch:{ all -> 0x01bd }
        r5 = r0;
        r5 = r5.passToListener();	 Catch:{ all -> 0x01bd }
        if (r5 != 0) goto L_0x0071;
    L_0x03b6:
        r23 = (org.jitsi.gov.nist.javax.sip.stack.SIPTransaction) r23;	 Catch:{ all -> 0x01bd }
        r23.releaseSem();	 Catch:{ all -> 0x01bd }
        goto L_0x0071;
    L_0x03bd:
        r5 = move-exception;
        r6 = r5;
        r0 = r23;
        r5 = r0 instanceof org.jitsi.gov.nist.javax.sip.stack.SIPTransaction;	 Catch:{ all -> 0x01bd }
        if (r5 == 0) goto L_0x03d5;
    L_0x03c5:
        r0 = r23;
        r0 = (org.jitsi.gov.nist.javax.sip.stack.SIPTransaction) r0;	 Catch:{ all -> 0x01bd }
        r5 = r0;
        r5 = r5.passToListener();	 Catch:{ all -> 0x01bd }
        if (r5 != 0) goto L_0x03d5;
    L_0x03d0:
        r23 = (org.jitsi.gov.nist.javax.sip.stack.SIPTransaction) r23;	 Catch:{ all -> 0x01bd }
        r23.releaseSem();	 Catch:{ all -> 0x01bd }
    L_0x03d5:
        throw r6;	 Catch:{ all -> 0x01bd }
    L_0x03d6:
        r5 = logger;	 Catch:{ all -> 0x01bd }
        r6 = "Could not get semaphore... dropping response";
        r5.logWarning(r6);	 Catch:{ all -> 0x01bd }
        goto L_0x0071;
    L_0x03df:
        r5 = move-exception;
        goto L_0x02c6;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.gov.nist.javax.sip.stack.TLSMessageChannel.processMessage(org.jitsi.gov.nist.javax.sip.message.SIPMessage):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:64:0x0118 A:{ExcHandler: Exception (r1_0 'ex' java.lang.Exception), Splitter:B:13:0x0062} */
    /* JADX WARNING: Missing block: B:64:0x0118, code skipped:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:66:?, code skipped:
            org.jitsi.gov.nist.core.InternalErrorHandler.handleException(r1);
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
        r5 = r10.tlsMessageProcessor;
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
        r6 = r10.tlsMessageProcessor;	 Catch:{ IOException -> 0x0097, Exception -> 0x0118 }
        monitor-enter(r6);	 Catch:{ IOException -> 0x0097, Exception -> 0x0118 }
        r5 = r10.tlsMessageProcessor;	 Catch:{ all -> 0x0094 }
        r7 = r5.nConnections;	 Catch:{ all -> 0x0094 }
        r7 = r7 + -1;
        r5.nConnections = r7;	 Catch:{ all -> 0x0094 }
        r5 = r10.tlsMessageProcessor;	 Catch:{ all -> 0x0094 }
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
        r5 = r10.tlsMessageProcessor;
        r5.remove(r10);
        r5 = r10.tlsMessageProcessor;
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
        r5 = r5.getBytes(r6);	 Catch:{ Exception -> 0x0136 }
        r2.write(r5);	 Catch:{ Exception -> 0x0136 }
    L_0x00aa:
        r5 = logger;	 Catch:{ Exception -> 0x0134 }
        r6 = 32;
        r5 = r5.isLoggingEnabled(r6);	 Catch:{ Exception -> 0x0134 }
        if (r5 == 0) goto L_0x00cc;
    L_0x00b4:
        r5 = logger;	 Catch:{ Exception -> 0x0134 }
        r6 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0134 }
        r6.<init>();	 Catch:{ Exception -> 0x0134 }
        r7 = "IOException  closing sock ";
        r6 = r6.append(r7);	 Catch:{ Exception -> 0x0134 }
        r6 = r6.append(r1);	 Catch:{ Exception -> 0x0134 }
        r6 = r6.toString();	 Catch:{ Exception -> 0x0134 }
        r5.logDebug(r6);	 Catch:{ Exception -> 0x0134 }
    L_0x00cc:
        r5 = r10.sipStack;	 Catch:{ IOException -> 0x0116 }
        r5 = r5.maxConnections;	 Catch:{ IOException -> 0x0116 }
        if (r5 == r9) goto L_0x00e3;
    L_0x00d2:
        r6 = r10.tlsMessageProcessor;	 Catch:{ IOException -> 0x0116 }
        monitor-enter(r6);	 Catch:{ IOException -> 0x0116 }
        r5 = r10.tlsMessageProcessor;	 Catch:{ all -> 0x0113 }
        r7 = r5.nConnections;	 Catch:{ all -> 0x0113 }
        r7 = r7 + -1;
        r5.nConnections = r7;	 Catch:{ all -> 0x0113 }
        r5 = r10.tlsMessageProcessor;	 Catch:{ all -> 0x0113 }
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
        r5 = r10.tlsMessageProcessor;
        r5.remove(r10);
        r5 = r10.tlsMessageProcessor;
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
        org.jitsi.gov.nist.core.InternalErrorHandler.handleException(r1);	 Catch:{ all -> 0x011e }
        goto L_0x0035;
    L_0x011e:
        r5 = move-exception;
        r10.isRunning = r8;
        r6 = r10.tlsMessageProcessor;
        r6.remove(r10);
        r6 = r10.tlsMessageProcessor;
        r7 = r6.useCount;
        r7 = r7 + -1;
        r6.useCount = r7;
        r6 = r10.myParser;
        r6.close();
        throw r5;
    L_0x0134:
        r5 = move-exception;
        goto L_0x0100;
    L_0x0136:
        r5 = move-exception;
        goto L_0x00aa;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.gov.nist.javax.sip.stack.TLSMessageChannel.run():void");
    }

    /* access modifiers changed from: protected */
    public void uncache() {
        if (this.isCached && !this.isRunning) {
            this.tlsMessageProcessor.remove(this);
        }
    }

    public boolean equals(Object other) {
        if (!getClass().equals(other.getClass())) {
            return false;
        }
        if (this.mySock == ((TLSMessageChannel) other).mySock) {
            return true;
        }
        return false;
    }

    public String getKey() {
        if (this.key != null) {
            return this.key;
        }
        this.key = MessageChannel.getKey(this.peerAddress, this.peerPort, ListeningPoint.TLS);
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
        return true;
    }

    public void setHandshakeCompletedListener(HandshakeCompletedListener handshakeCompletedListenerImpl) {
        this.handshakeCompletedListener = handshakeCompletedListenerImpl;
    }

    public HandshakeCompletedListenerImpl getHandshakeCompletedListener() {
        return (HandshakeCompletedListenerImpl) this.handshakeCompletedListener;
    }
}
