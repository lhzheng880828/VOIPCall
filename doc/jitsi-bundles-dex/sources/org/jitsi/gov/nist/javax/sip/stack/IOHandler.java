package org.jitsi.gov.nist.javax.sip.stack;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSocket;
import org.jitsi.gov.nist.core.CommonLogger;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.core.StackLogger;
import org.jitsi.gov.nist.javax.sip.SipStackImpl;

public class IOHandler {
    private static final String TCP = "tcp";
    private static final String TLS = "tls";
    private static StackLogger logger = CommonLogger.getLogger(IOHandler.class);
    private SipStackImpl sipStack;
    private final ConcurrentHashMap<String, Semaphore> socketCreationMap = new ConcurrentHashMap();
    private final ConcurrentHashMap<String, Socket> socketTable = new ConcurrentHashMap();

    protected static String makeKey(InetAddress addr, int port) {
        return addr.getHostAddress() + Separators.COLON + port;
    }

    protected static String makeKey(String addr, int port) {
        return addr + Separators.COLON + port;
    }

    protected IOHandler(SIPTransactionStack sipStack) {
        this.sipStack = (SipStackImpl) sipStack;
    }

    /* access modifiers changed from: protected */
    public void putSocket(String key, Socket sock) {
        this.socketTable.put(key, sock);
    }

    /* access modifiers changed from: protected */
    public Socket getSocket(String key) {
        return (Socket) this.socketTable.get(key);
    }

    /* access modifiers changed from: protected */
    public void removeSocket(String key) {
        this.socketTable.remove(key);
        this.socketCreationMap.remove(key);
    }

    private void writeChunks(OutputStream outputStream, byte[] bytes, int length) throws IOException {
        synchronized (outputStream) {
            int p = 0;
            while (p < length) {
                outputStream.write(bytes, p, p + 8192 < length ? 8192 : length - p);
                p += 8192;
            }
        }
        outputStream.flush();
    }

    public SocketAddress getLocalAddressForTcpDst(InetAddress dst, int dstPort, InetAddress localAddress, int localPort) throws IOException {
        String key = makeKey(dst, dstPort);
        Socket clientSock = getSocket(key);
        if (clientSock == null) {
            clientSock = this.sipStack.getNetworkLayer().createSocket(dst, dstPort, localAddress, localPort);
            putSocket(key, clientSock);
        }
        return clientSock.getLocalSocketAddress();
    }

    public SocketAddress getLocalAddressForTlsDst(InetAddress dst, int dstPort, InetAddress localAddress, TLSMessageChannel channel) throws IOException {
        String key = makeKey(dst, dstPort);
        Socket clientSock = getSocket(key);
        if (clientSock == null) {
            clientSock = this.sipStack.getNetworkLayer().createSSLSocket(dst, dstPort, localAddress);
            SSLSocket sslsock = (SSLSocket) clientSock;
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("inaddr = " + dst);
                logger.logDebug("port = " + dstPort);
            }
            HandshakeCompletedListener listner = new HandshakeCompletedListenerImpl(channel);
            channel.setHandshakeCompletedListener(listner);
            sslsock.addHandshakeCompletedListener(listner);
            sslsock.setEnabledProtocols(this.sipStack.getEnabledProtocols());
            sslsock.startHandshake();
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("Handshake passed");
            }
            try {
                this.sipStack.getTlsSecurityPolicy().enforceTlsPolicy(channel.getEncapsulatedClientTransaction());
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("TLS Security policy passed");
                }
                putSocket(key, clientSock);
            } catch (SecurityException ex) {
                throw new IOException(ex.getMessage());
            }
        }
        return clientSock.getLocalSocketAddress();
    }

    /* JADX WARNING: Removed duplicated region for block: B:122:0x0439 A:{ExcHandler: SSLHandshakeException (r11_1 'ex' javax.net.ssl.SSLHandshakeException), Splitter:B:95:0x0337} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:122:0x0439, code skipped:
            r11 = move-exception;
     */
    /* JADX WARNING: Missing block: B:125:?, code skipped:
            removeSocket(r13);
     */
    /* JADX WARNING: Missing block: B:126:0x043f, code skipped:
            throw r11;
     */
    /* JADX WARNING: Missing block: B:128:0x0441, code skipped:
            leaveIOCriticalSection(r13);
     */
    /* JADX WARNING: Missing block: B:132:0x0455, code skipped:
            r11 = move-exception;
     */
    /* JADX WARNING: Missing block: B:135:0x045c, code skipped:
            if (logger.isLoggingEnabled() != false) goto L_0x045e;
     */
    /* JADX WARNING: Missing block: B:136:0x045e, code skipped:
            logger.logException(r11);
     */
    /* JADX WARNING: Missing block: B:137:0x0463, code skipped:
            removeSocket(r13);
     */
    /* JADX WARNING: Missing block: B:139:?, code skipped:
            logger.logDebug("Closing socket");
            r9.close();
     */
    /* JADX WARNING: Missing block: B:140:0x0472, code skipped:
            r9 = null;
            r17 = r17 + 1;
     */
    /* JADX WARNING: Missing block: B:166:0x056e, code skipped:
            r12 = move-exception;
     */
    /* JADX WARNING: Missing block: B:169:0x0576, code skipped:
            if (logger.isLoggingEnabled(4) != false) goto L_0x0578;
     */
    /* JADX WARNING: Missing block: B:170:0x0578, code skipped:
            logger.logError("IOException occured  ", r12);
     */
    /* JADX WARNING: Missing block: B:172:0x0587, code skipped:
            if (logger.isLoggingEnabled(32) != false) goto L_0x0589;
     */
    /* JADX WARNING: Missing block: B:173:0x0589, code skipped:
            logger.logDebug("Removing and Closing socket");
     */
    /* JADX WARNING: Missing block: B:174:0x0590, code skipped:
            removeSocket(r13);
     */
    /* JADX WARNING: Missing block: B:176:?, code skipped:
            r9.close();
     */
    /* JADX WARNING: Missing block: B:179:?, code skipped:
            throw r12;
     */
    public java.net.Socket sendBytes(java.net.InetAddress r20, java.net.InetAddress r21, int r22, java.lang.String r23, byte[] r24, boolean r25, org.jitsi.gov.nist.javax.sip.stack.MessageChannel r26) throws java.io.IOException {
        /*
        r19 = this;
        r17 = 0;
        if (r25 == 0) goto L_0x0163;
    L_0x0004:
        r15 = 2;
    L_0x0005:
        r0 = r24;
        r6 = r0.length;
        r4 = logger;
        r5 = 32;
        r4 = r4.isLoggingEnabled(r5);
        if (r4 == 0) goto L_0x005c;
    L_0x0012:
        r4 = logger;
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r7 = "sendBytes ";
        r5 = r5.append(r7);
        r0 = r23;
        r5 = r5.append(r0);
        r7 = " inAddr ";
        r5 = r5.append(r7);
        r7 = r21.getHostAddress();
        r5 = r5.append(r7);
        r7 = " port = ";
        r5 = r5.append(r7);
        r0 = r22;
        r5 = r5.append(r0);
        r7 = " length = ";
        r5 = r5.append(r7);
        r5 = r5.append(r6);
        r7 = " isClient ";
        r5 = r5.append(r7);
        r0 = r25;
        r5 = r5.append(r0);
        r5 = r5.toString();
        r4.logDebug(r5);
    L_0x005c:
        r4 = logger;
        r5 = 16;
        r4 = r4.isLoggingEnabled(r5);
        if (r4 == 0) goto L_0x0077;
    L_0x0066:
        r0 = r19;
        r4 = r0.sipStack;
        r4 = r4.isLogStackTraceOnMessageSend();
        if (r4 == 0) goto L_0x0077;
    L_0x0070:
        r4 = logger;
        r5 = 16;
        r4.logStackTrace(r5);
    L_0x0077:
        r4 = "tcp";
        r0 = r23;
        r4 = r0.compareToIgnoreCase(r4);
        if (r4 != 0) goto L_0x0321;
    L_0x0081:
        r13 = makeKey(r21, r22);
        r9 = 0;
        r0 = r19;
        r0.enterIOCriticalSection(r13);
        r0 = r19;
        r9 = r0.getSocket(r13);	 Catch:{ IOException -> 0x01b7 }
    L_0x0091:
        r0 = r17;
        if (r0 >= r15) goto L_0x00f9;
    L_0x0095:
        if (r9 != 0) goto L_0x0166;
    L_0x0097:
        r4 = logger;	 Catch:{ IOException -> 0x01b7 }
        r5 = 32;
        r4 = r4.isLoggingEnabled(r5);	 Catch:{ IOException -> 0x01b7 }
        if (r4 == 0) goto L_0x00d5;
    L_0x00a1:
        r4 = logger;	 Catch:{ IOException -> 0x01b7 }
        r5 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x01b7 }
        r5.<init>();	 Catch:{ IOException -> 0x01b7 }
        r7 = "inaddr = ";
        r5 = r5.append(r7);	 Catch:{ IOException -> 0x01b7 }
        r0 = r21;
        r5 = r5.append(r0);	 Catch:{ IOException -> 0x01b7 }
        r5 = r5.toString();	 Catch:{ IOException -> 0x01b7 }
        r4.logDebug(r5);	 Catch:{ IOException -> 0x01b7 }
        r4 = logger;	 Catch:{ IOException -> 0x01b7 }
        r5 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x01b7 }
        r5.<init>();	 Catch:{ IOException -> 0x01b7 }
        r7 = "port = ";
        r5 = r5.append(r7);	 Catch:{ IOException -> 0x01b7 }
        r0 = r22;
        r5 = r5.append(r0);	 Catch:{ IOException -> 0x01b7 }
        r5 = r5.toString();	 Catch:{ IOException -> 0x01b7 }
        r4.logDebug(r5);	 Catch:{ IOException -> 0x01b7 }
    L_0x00d5:
        r0 = r19;
        r4 = r0.sipStack;	 Catch:{ IOException -> 0x01b7 }
        r4 = r4.getNetworkLayer();	 Catch:{ IOException -> 0x01b7 }
        r0 = r21;
        r1 = r22;
        r2 = r20;
        r9 = r4.createSocket(r0, r1, r2);	 Catch:{ IOException -> 0x01b7 }
        r16 = r9.getOutputStream();	 Catch:{ IOException -> 0x01b7 }
        r0 = r19;
        r1 = r16;
        r2 = r24;
        r0.writeChunks(r1, r2, r6);	 Catch:{ IOException -> 0x01b7 }
        r0 = r19;
        r0.putSocket(r13, r9);	 Catch:{ IOException -> 0x01b7 }
    L_0x00f9:
        r0 = r19;
        r0.leaveIOCriticalSection(r13);
        if (r9 != 0) goto L_0x02b0;
    L_0x0100:
        r4 = logger;
        r5 = 4;
        r4 = r4.isLoggingEnabled(r5);
        if (r4 == 0) goto L_0x013c;
    L_0x0109:
        r4 = logger;
        r0 = r19;
        r5 = r0.socketTable;
        r5 = r5.toString();
        r4.logError(r5);
        r4 = logger;
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r7 = "Could not connect to ";
        r5 = r5.append(r7);
        r0 = r21;
        r5 = r5.append(r0);
        r7 = ":";
        r5 = r5.append(r7);
        r0 = r22;
        r5 = r5.append(r0);
        r5 = r5.toString();
        r4.logError(r5);
    L_0x013c:
        r4 = new java.io.IOException;
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r7 = "Could not connect to ";
        r5 = r5.append(r7);
        r0 = r21;
        r5 = r5.append(r0);
        r7 = ":";
        r5 = r5.append(r7);
        r0 = r22;
        r5 = r5.append(r0);
        r5 = r5.toString();
        r4.<init>(r5);
        throw r4;
    L_0x0163:
        r15 = 1;
        goto L_0x0005;
    L_0x0166:
        r16 = r9.getOutputStream();	 Catch:{ IOException -> 0x0174 }
        r0 = r19;
        r1 = r16;
        r2 = r24;
        r0.writeChunks(r1, r2, r6);	 Catch:{ IOException -> 0x0174 }
        goto L_0x00f9;
    L_0x0174:
        r11 = move-exception;
        r4 = logger;	 Catch:{ IOException -> 0x01b7 }
        r5 = 4;
        r4 = r4.isLoggingEnabled(r5);	 Catch:{ IOException -> 0x01b7 }
        if (r4 == 0) goto L_0x0198;
    L_0x017e:
        r4 = logger;	 Catch:{ IOException -> 0x01b7 }
        r5 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x01b7 }
        r5.<init>();	 Catch:{ IOException -> 0x01b7 }
        r7 = "IOException occured retryCount ";
        r5 = r5.append(r7);	 Catch:{ IOException -> 0x01b7 }
        r0 = r17;
        r5 = r5.append(r0);	 Catch:{ IOException -> 0x01b7 }
        r5 = r5.toString();	 Catch:{ IOException -> 0x01b7 }
        r4.logInfo(r5);	 Catch:{ IOException -> 0x01b7 }
    L_0x0198:
        r4 = logger;	 Catch:{ IOException -> 0x01b7 }
        r5 = 32;
        r4 = r4.isLoggingEnabled(r5);	 Catch:{ IOException -> 0x01b7 }
        if (r4 == 0) goto L_0x01a9;
    L_0x01a2:
        r4 = logger;	 Catch:{ IOException -> 0x01b7 }
        r5 = "Removing and Closing socket";
        r4.logDebug(r5);	 Catch:{ IOException -> 0x01b7 }
    L_0x01a9:
        r0 = r19;
        r0.removeSocket(r13);	 Catch:{ IOException -> 0x01b7 }
        r9.close();	 Catch:{ Exception -> 0x05c3 }
    L_0x01b1:
        r9 = 0;
        r17 = r17 + 1;
        if (r25 != 0) goto L_0x0091;
    L_0x01b6:
        throw r11;	 Catch:{ IOException -> 0x01b7 }
    L_0x01b7:
        r11 = move-exception;
        r4 = logger;	 Catch:{ all -> 0x0312 }
        r5 = 4;
        r4 = r4.isLoggingEnabled(r5);	 Catch:{ all -> 0x0312 }
        if (r4 == 0) goto L_0x022b;
    L_0x01c1:
        r4 = logger;	 Catch:{ all -> 0x0312 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0312 }
        r5.<init>();	 Catch:{ all -> 0x0312 }
        r7 = "Problem sending: sendBytes ";
        r5 = r5.append(r7);	 Catch:{ all -> 0x0312 }
        r0 = r23;
        r5 = r5.append(r0);	 Catch:{ all -> 0x0312 }
        r7 = " inAddr ";
        r5 = r5.append(r7);	 Catch:{ all -> 0x0312 }
        r7 = r21.getHostAddress();	 Catch:{ all -> 0x0312 }
        r5 = r5.append(r7);	 Catch:{ all -> 0x0312 }
        r7 = " port = ";
        r5 = r5.append(r7);	 Catch:{ all -> 0x0312 }
        r0 = r22;
        r5 = r5.append(r0);	 Catch:{ all -> 0x0312 }
        r7 = " remoteHost ";
        r5 = r5.append(r7);	 Catch:{ all -> 0x0312 }
        r7 = r26.getPeerAddress();	 Catch:{ all -> 0x0312 }
        r5 = r5.append(r7);	 Catch:{ all -> 0x0312 }
        r7 = " remotePort ";
        r5 = r5.append(r7);	 Catch:{ all -> 0x0312 }
        r7 = r26.getPeerPort();	 Catch:{ all -> 0x0312 }
        r5 = r5.append(r7);	 Catch:{ all -> 0x0312 }
        r7 = " peerPacketPort ";
        r5 = r5.append(r7);	 Catch:{ all -> 0x0312 }
        r7 = r26.getPeerPacketSourcePort();	 Catch:{ all -> 0x0312 }
        r5 = r5.append(r7);	 Catch:{ all -> 0x0312 }
        r7 = " isClient ";
        r5 = r5.append(r7);	 Catch:{ all -> 0x0312 }
        r0 = r25;
        r5 = r5.append(r0);	 Catch:{ all -> 0x0312 }
        r5 = r5.toString();	 Catch:{ all -> 0x0312 }
        r4.logError(r5);	 Catch:{ all -> 0x0312 }
    L_0x022b:
        r0 = r19;
        r0.removeSocket(r13);	 Catch:{ all -> 0x0312 }
        if (r25 != 0) goto L_0x0319;
    L_0x0232:
        r4 = r26.getViaHost();	 Catch:{ all -> 0x0312 }
        r21 = java.net.InetAddress.getByName(r4);	 Catch:{ all -> 0x0312 }
        r22 = r26.getViaPort();	 Catch:{ all -> 0x0312 }
        r4 = -1;
        r0 = r22;
        if (r0 != r4) goto L_0x0245;
    L_0x0243:
        r22 = 5060; // 0x13c4 float:7.09E-42 double:2.5E-320;
    L_0x0245:
        r4 = r26.getViaPort();	 Catch:{ all -> 0x0312 }
        r0 = r21;
        r13 = makeKey(r0, r4);	 Catch:{ all -> 0x0312 }
        r0 = r19;
        r9 = r0.getSocket(r13);	 Catch:{ all -> 0x0312 }
        if (r9 != 0) goto L_0x02b1;
    L_0x0257:
        r4 = logger;	 Catch:{ all -> 0x0312 }
        r5 = 32;
        r4 = r4.isLoggingEnabled(r5);	 Catch:{ all -> 0x0312 }
        if (r4 == 0) goto L_0x0287;
    L_0x0261:
        r4 = logger;	 Catch:{ all -> 0x0312 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0312 }
        r5.<init>();	 Catch:{ all -> 0x0312 }
        r7 = "inaddr = ";
        r5 = r5.append(r7);	 Catch:{ all -> 0x0312 }
        r0 = r21;
        r5 = r5.append(r0);	 Catch:{ all -> 0x0312 }
        r7 = " port = ";
        r5 = r5.append(r7);	 Catch:{ all -> 0x0312 }
        r0 = r22;
        r5 = r5.append(r0);	 Catch:{ all -> 0x0312 }
        r5 = r5.toString();	 Catch:{ all -> 0x0312 }
        r4.logDebug(r5);	 Catch:{ all -> 0x0312 }
    L_0x0287:
        r0 = r19;
        r4 = r0.sipStack;	 Catch:{ all -> 0x0312 }
        r4 = r4.getNetworkLayer();	 Catch:{ all -> 0x0312 }
        r0 = r21;
        r1 = r22;
        r2 = r20;
        r9 = r4.createSocket(r0, r1, r2);	 Catch:{ all -> 0x0312 }
        r16 = r9.getOutputStream();	 Catch:{ all -> 0x0312 }
        r0 = r19;
        r1 = r16;
        r2 = r24;
        r0.writeChunks(r1, r2, r6);	 Catch:{ all -> 0x0312 }
        r0 = r19;
        r0.putSocket(r13, r9);	 Catch:{ all -> 0x0312 }
        r0 = r19;
        r0.leaveIOCriticalSection(r13);
    L_0x02b0:
        return r9;
    L_0x02b1:
        r4 = logger;	 Catch:{ all -> 0x0312 }
        r5 = 32;
        r4 = r4.isLoggingEnabled(r5);	 Catch:{ all -> 0x0312 }
        if (r4 == 0) goto L_0x02d3;
    L_0x02bb:
        r4 = logger;	 Catch:{ all -> 0x0312 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0312 }
        r5.<init>();	 Catch:{ all -> 0x0312 }
        r7 = "sending to ";
        r5 = r5.append(r7);	 Catch:{ all -> 0x0312 }
        r5 = r5.append(r13);	 Catch:{ all -> 0x0312 }
        r5 = r5.toString();	 Catch:{ all -> 0x0312 }
        r4.logDebug(r5);	 Catch:{ all -> 0x0312 }
    L_0x02d3:
        r16 = r9.getOutputStream();	 Catch:{ IOException -> 0x02e6 }
        r0 = r19;
        r1 = r16;
        r2 = r24;
        r0.writeChunks(r1, r2, r6);	 Catch:{ IOException -> 0x02e6 }
        r0 = r19;
        r0.leaveIOCriticalSection(r13);
        goto L_0x02b0;
    L_0x02e6:
        r12 = move-exception;
        r4 = logger;	 Catch:{ all -> 0x0312 }
        r5 = 4;
        r4 = r4.isLoggingEnabled(r5);	 Catch:{ all -> 0x0312 }
        if (r4 == 0) goto L_0x02f7;
    L_0x02f0:
        r4 = logger;	 Catch:{ all -> 0x0312 }
        r5 = "IOException occured  ";
        r4.logError(r5, r12);	 Catch:{ all -> 0x0312 }
    L_0x02f7:
        r4 = logger;	 Catch:{ all -> 0x0312 }
        r5 = 32;
        r4 = r4.isLoggingEnabled(r5);	 Catch:{ all -> 0x0312 }
        if (r4 == 0) goto L_0x0308;
    L_0x0301:
        r4 = logger;	 Catch:{ all -> 0x0312 }
        r5 = "Removing and Closing socket";
        r4.logDebug(r5);	 Catch:{ all -> 0x0312 }
    L_0x0308:
        r0 = r19;
        r0.removeSocket(r13);	 Catch:{ all -> 0x0312 }
        r9.close();	 Catch:{ Exception -> 0x05c6 }
    L_0x0310:
        r9 = 0;
        throw r12;	 Catch:{ all -> 0x0312 }
    L_0x0312:
        r4 = move-exception;
        r0 = r19;
        r0.leaveIOCriticalSection(r13);
        throw r4;
    L_0x0319:
        r4 = logger;	 Catch:{ all -> 0x0312 }
        r5 = "IOException occured at ";
        r4.logError(r5, r11);	 Catch:{ all -> 0x0312 }
        throw r11;	 Catch:{ all -> 0x0312 }
    L_0x0321:
        r4 = "tls";
        r0 = r23;
        r4 = r0.compareToIgnoreCase(r4);
        if (r4 != 0) goto L_0x059b;
    L_0x032b:
        r13 = makeKey(r21, r22);
        r9 = 0;
        r0 = r19;
        r0.enterIOCriticalSection(r13);
        r0 = r19;
        r9 = r0.getSocket(r13);	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
    L_0x033b:
        r0 = r17;
        if (r0 >= r15) goto L_0x0400;
    L_0x033f:
        if (r9 != 0) goto L_0x0447;
    L_0x0341:
        r0 = r19;
        r4 = r0.sipStack;	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r4 = r4.getNetworkLayer();	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r0 = r21;
        r1 = r22;
        r2 = r20;
        r9 = r4.createSSLSocket(r0, r1, r2);	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r0 = r9;
        r0 = (javax.net.ssl.SSLSocket) r0;	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r18 = r0;
        r4 = logger;	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r5 = 32;
        r4 = r4.isLoggingEnabled(r5);	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        if (r4 == 0) goto L_0x0396;
    L_0x0362:
        r4 = logger;	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r5 = new java.lang.StringBuilder;	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r5.<init>();	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r7 = "inaddr = ";
        r5 = r5.append(r7);	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r0 = r21;
        r5 = r5.append(r0);	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r5 = r5.toString();	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r4.logDebug(r5);	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r4 = logger;	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r5 = new java.lang.StringBuilder;	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r5.<init>();	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r7 = "port = ";
        r5 = r5.append(r7);	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r0 = r22;
        r5 = r5.append(r0);	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r5 = r5.toString();	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r4.logDebug(r5);	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
    L_0x0396:
        r14 = new org.jitsi.gov.nist.javax.sip.stack.HandshakeCompletedListenerImpl;	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r0 = r26;
        r0 = (org.jitsi.gov.nist.javax.sip.stack.TLSMessageChannel) r0;	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r4 = r0;
        r14.m1545init(r4);	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r0 = r26;
        r0 = (org.jitsi.gov.nist.javax.sip.stack.TLSMessageChannel) r0;	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r4 = r0;
        r4.setHandshakeCompletedListener(r14);	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r0 = r18;
        r0.addHandshakeCompletedListener(r14);	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r0 = r19;
        r4 = r0.sipStack;	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r4 = r4.getEnabledProtocols();	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r0 = r18;
        r0.setEnabledProtocols(r4);	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r18.startHandshake();	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r4 = logger;	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r5 = 32;
        r4 = r4.isLoggingEnabled(r5);	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        if (r4 == 0) goto L_0x03ce;
    L_0x03c7:
        r4 = logger;	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r5 = "Handshake passed";
        r4.logDebug(r5);	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
    L_0x03ce:
        r0 = r19;
        r4 = r0.sipStack;	 Catch:{ SecurityException -> 0x042e }
        r4 = r4.getTlsSecurityPolicy();	 Catch:{ SecurityException -> 0x042e }
        r5 = r26.getEncapsulatedClientTransaction();	 Catch:{ SecurityException -> 0x042e }
        r4.enforceTlsPolicy(r5);	 Catch:{ SecurityException -> 0x042e }
        r4 = logger;	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r5 = 32;
        r4 = r4.isLoggingEnabled(r5);	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        if (r4 == 0) goto L_0x03ee;
    L_0x03e7:
        r4 = logger;	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r5 = "TLS Security policy passed";
        r4.logDebug(r5);	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
    L_0x03ee:
        r16 = r9.getOutputStream();	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r0 = r19;
        r1 = r16;
        r2 = r24;
        r0.writeChunks(r1, r2, r6);	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r0 = r19;
        r0.putSocket(r13, r9);	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
    L_0x0400:
        r0 = r19;
        r0.leaveIOCriticalSection(r13);
        if (r9 != 0) goto L_0x02b0;
    L_0x0407:
        r4 = new java.io.IOException;
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r7 = "Could not connect to ";
        r5 = r5.append(r7);
        r0 = r21;
        r5 = r5.append(r0);
        r7 = ":";
        r5 = r5.append(r7);
        r0 = r22;
        r5 = r5.append(r0);
        r5 = r5.toString();
        r4.<init>(r5);
        throw r4;
    L_0x042e:
        r11 = move-exception;
        r4 = new java.io.IOException;	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r5 = r11.getMessage();	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r4.<init>(r5);	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        throw r4;	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
    L_0x0439:
        r11 = move-exception;
        r0 = r19;
        r0.removeSocket(r13);	 Catch:{ all -> 0x0440 }
        throw r11;	 Catch:{ all -> 0x0440 }
    L_0x0440:
        r4 = move-exception;
        r0 = r19;
        r0.leaveIOCriticalSection(r13);
        throw r4;
    L_0x0447:
        r16 = r9.getOutputStream();	 Catch:{ IOException -> 0x0455, SSLHandshakeException -> 0x0439 }
        r0 = r19;
        r1 = r16;
        r2 = r24;
        r0.writeChunks(r1, r2, r6);	 Catch:{ IOException -> 0x0455, SSLHandshakeException -> 0x0439 }
        goto L_0x0400;
    L_0x0455:
        r11 = move-exception;
        r4 = logger;	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r4 = r4.isLoggingEnabled();	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        if (r4 == 0) goto L_0x0463;
    L_0x045e:
        r4 = logger;	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r4.logException(r11);	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
    L_0x0463:
        r0 = r19;
        r0.removeSocket(r13);	 Catch:{ SSLHandshakeException -> 0x0439, IOException -> 0x0477 }
        r4 = logger;	 Catch:{ Exception -> 0x05cb }
        r5 = "Closing socket";
        r4.logDebug(r5);	 Catch:{ Exception -> 0x05cb }
        r9.close();	 Catch:{ Exception -> 0x05cb }
    L_0x0472:
        r9 = 0;
        r17 = r17 + 1;
        goto L_0x033b;
    L_0x0477:
        r11 = move-exception;
        r0 = r19;
        r0.removeSocket(r13);	 Catch:{ all -> 0x0440 }
        if (r25 != 0) goto L_0x059a;
    L_0x047f:
        r4 = r26.getViaHost();	 Catch:{ all -> 0x0440 }
        r21 = java.net.InetAddress.getByName(r4);	 Catch:{ all -> 0x0440 }
        r22 = r26.getViaPort();	 Catch:{ all -> 0x0440 }
        r4 = -1;
        r0 = r22;
        if (r0 != r4) goto L_0x0492;
    L_0x0490:
        r22 = 5060; // 0x13c4 float:7.09E-42 double:2.5E-320;
    L_0x0492:
        r4 = r26.getViaPort();	 Catch:{ all -> 0x0440 }
        r0 = r21;
        r13 = makeKey(r0, r4);	 Catch:{ all -> 0x0440 }
        r0 = r19;
        r9 = r0.getSocket(r13);	 Catch:{ all -> 0x0440 }
        if (r9 != 0) goto L_0x0538;
    L_0x04a4:
        r4 = logger;	 Catch:{ all -> 0x0440 }
        r5 = 32;
        r4 = r4.isLoggingEnabled(r5);	 Catch:{ all -> 0x0440 }
        if (r4 == 0) goto L_0x04d4;
    L_0x04ae:
        r4 = logger;	 Catch:{ all -> 0x0440 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0440 }
        r5.<init>();	 Catch:{ all -> 0x0440 }
        r7 = "inaddr = ";
        r5 = r5.append(r7);	 Catch:{ all -> 0x0440 }
        r0 = r21;
        r5 = r5.append(r0);	 Catch:{ all -> 0x0440 }
        r7 = " port = ";
        r5 = r5.append(r7);	 Catch:{ all -> 0x0440 }
        r0 = r22;
        r5 = r5.append(r0);	 Catch:{ all -> 0x0440 }
        r5 = r5.toString();	 Catch:{ all -> 0x0440 }
        r4.logDebug(r5);	 Catch:{ all -> 0x0440 }
    L_0x04d4:
        r0 = r19;
        r4 = r0.sipStack;	 Catch:{ all -> 0x0440 }
        r4 = r4.getNetworkLayer();	 Catch:{ all -> 0x0440 }
        r0 = r21;
        r1 = r22;
        r2 = r20;
        r18 = r4.createSSLSocket(r0, r1, r2);	 Catch:{ all -> 0x0440 }
        r16 = r18.getOutputStream();	 Catch:{ all -> 0x0440 }
        r14 = new org.jitsi.gov.nist.javax.sip.stack.HandshakeCompletedListenerImpl;	 Catch:{ all -> 0x0440 }
        r0 = r26;
        r0 = (org.jitsi.gov.nist.javax.sip.stack.TLSMessageChannel) r0;	 Catch:{ all -> 0x0440 }
        r4 = r0;
        r14.m1545init(r4);	 Catch:{ all -> 0x0440 }
        r26 = (org.jitsi.gov.nist.javax.sip.stack.TLSMessageChannel) r26;	 Catch:{ all -> 0x0440 }
        r0 = r26;
        r0.setHandshakeCompletedListener(r14);	 Catch:{ all -> 0x0440 }
        r0 = r18;
        r0.addHandshakeCompletedListener(r14);	 Catch:{ all -> 0x0440 }
        r0 = r19;
        r4 = r0.sipStack;	 Catch:{ all -> 0x0440 }
        r4 = r4.getEnabledProtocols();	 Catch:{ all -> 0x0440 }
        r0 = r18;
        r0.setEnabledProtocols(r4);	 Catch:{ all -> 0x0440 }
        r18.startHandshake();	 Catch:{ all -> 0x0440 }
        r4 = logger;	 Catch:{ all -> 0x0440 }
        r5 = 32;
        r4 = r4.isLoggingEnabled(r5);	 Catch:{ all -> 0x0440 }
        if (r4 == 0) goto L_0x0521;
    L_0x051a:
        r4 = logger;	 Catch:{ all -> 0x0440 }
        r5 = "Handshake passed";
        r4.logDebug(r5);	 Catch:{ all -> 0x0440 }
    L_0x0521:
        r0 = r19;
        r1 = r16;
        r2 = r24;
        r0.writeChunks(r1, r2, r6);	 Catch:{ all -> 0x0440 }
        r0 = r19;
        r0.putSocket(r13, r9);	 Catch:{ all -> 0x0440 }
        r0 = r19;
        r0.leaveIOCriticalSection(r13);
        r9 = r18;
        goto L_0x02b0;
    L_0x0538:
        r4 = logger;	 Catch:{ all -> 0x0440 }
        r5 = 32;
        r4 = r4.isLoggingEnabled(r5);	 Catch:{ all -> 0x0440 }
        if (r4 == 0) goto L_0x055a;
    L_0x0542:
        r4 = logger;	 Catch:{ all -> 0x0440 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0440 }
        r5.<init>();	 Catch:{ all -> 0x0440 }
        r7 = "sending to ";
        r5 = r5.append(r7);	 Catch:{ all -> 0x0440 }
        r5 = r5.append(r13);	 Catch:{ all -> 0x0440 }
        r5 = r5.toString();	 Catch:{ all -> 0x0440 }
        r4.logDebug(r5);	 Catch:{ all -> 0x0440 }
    L_0x055a:
        r16 = r9.getOutputStream();	 Catch:{ IOException -> 0x056e }
        r0 = r19;
        r1 = r16;
        r2 = r24;
        r0.writeChunks(r1, r2, r6);	 Catch:{ IOException -> 0x056e }
        r0 = r19;
        r0.leaveIOCriticalSection(r13);
        goto L_0x02b0;
    L_0x056e:
        r12 = move-exception;
        r4 = logger;	 Catch:{ all -> 0x0440 }
        r5 = 4;
        r4 = r4.isLoggingEnabled(r5);	 Catch:{ all -> 0x0440 }
        if (r4 == 0) goto L_0x057f;
    L_0x0578:
        r4 = logger;	 Catch:{ all -> 0x0440 }
        r5 = "IOException occured  ";
        r4.logError(r5, r12);	 Catch:{ all -> 0x0440 }
    L_0x057f:
        r4 = logger;	 Catch:{ all -> 0x0440 }
        r5 = 32;
        r4 = r4.isLoggingEnabled(r5);	 Catch:{ all -> 0x0440 }
        if (r4 == 0) goto L_0x0590;
    L_0x0589:
        r4 = logger;	 Catch:{ all -> 0x0440 }
        r5 = "Removing and Closing socket";
        r4.logDebug(r5);	 Catch:{ all -> 0x0440 }
    L_0x0590:
        r0 = r19;
        r0.removeSocket(r13);	 Catch:{ all -> 0x0440 }
        r9.close();	 Catch:{ Exception -> 0x05c9 }
    L_0x0598:
        r9 = 0;
        throw r12;	 Catch:{ all -> 0x0440 }
    L_0x059a:
        throw r11;	 Catch:{ all -> 0x0440 }
    L_0x059b:
        r0 = r19;
        r4 = r0.sipStack;
        r4 = r4.getNetworkLayer();
        r10 = r4.createDatagramSocket();
        r0 = r21;
        r1 = r22;
        r10.connect(r0, r1);
        r3 = new java.net.DatagramPacket;
        r5 = 0;
        r4 = r24;
        r7 = r21;
        r8 = r22;
        r3.<init>(r4, r5, r6, r7, r8);
        r10.send(r3);
        r10.close();
        r9 = 0;
        goto L_0x02b0;
    L_0x05c3:
        r4 = move-exception;
        goto L_0x01b1;
    L_0x05c6:
        r4 = move-exception;
        goto L_0x0310;
    L_0x05c9:
        r4 = move-exception;
        goto L_0x0598;
    L_0x05cb:
        r4 = move-exception;
        goto L_0x0472;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.gov.nist.javax.sip.stack.IOHandler.sendBytes(java.net.InetAddress, java.net.InetAddress, int, java.lang.String, byte[], boolean, org.jitsi.gov.nist.javax.sip.stack.MessageChannel):java.net.Socket");
    }

    private void leaveIOCriticalSection(String key) {
        Semaphore creationSemaphore = (Semaphore) this.socketCreationMap.get(key);
        if (creationSemaphore != null) {
            creationSemaphore.release();
        }
    }

    private void enterIOCriticalSection(String key) throws IOException {
        Semaphore creationSemaphore = (Semaphore) this.socketCreationMap.get(key);
        if (creationSemaphore == null) {
            Semaphore newCreationSemaphore = new Semaphore(1, true);
            creationSemaphore = (Semaphore) this.socketCreationMap.putIfAbsent(key, newCreationSemaphore);
            if (creationSemaphore == null) {
                creationSemaphore = newCreationSemaphore;
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("new Semaphore added for key " + key);
                }
            }
        }
        try {
            if (!creationSemaphore.tryAcquire(10, TimeUnit.SECONDS)) {
                throw new IOException("Could not acquire IO Semaphore'" + key + "' after 10 seconds -- giving up ");
            }
        } catch (InterruptedException e) {
            throw new IOException("exception in acquiring sem");
        }
    }

    public void closeAll() {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Closing " + this.socketTable.size() + " sockets from IOHandler");
        }
        Enumeration<Socket> values = this.socketTable.elements();
        while (values.hasMoreElements()) {
            try {
                ((Socket) values.nextElement()).close();
            } catch (IOException e) {
            }
        }
    }
}
