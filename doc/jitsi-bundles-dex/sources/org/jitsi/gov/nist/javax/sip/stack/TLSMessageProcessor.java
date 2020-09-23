package org.jitsi.gov.nist.javax.sip.stack;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import javax.net.ssl.SSLServerSocket;
import org.jitsi.gov.nist.core.CommonLogger;
import org.jitsi.gov.nist.core.HostPort;
import org.jitsi.gov.nist.core.StackLogger;
import org.jitsi.gov.nist.javax.sip.SipStackImpl;
import org.jitsi.gov.nist.javax.sip.address.ParameterNames;
import org.jitsi.javax.sip.ListeningPoint;

public class TLSMessageProcessor extends MessageProcessor {
    private static StackLogger logger = CommonLogger.getLogger(TLSMessageProcessor.class);
    private ArrayList<TLSMessageChannel> incomingTlsMessageChannels;
    private boolean isRunning;
    protected int nConnections;
    private ServerSocket sock;
    private Hashtable<String, TLSMessageChannel> tlsMessageChannels;
    protected int useCount = 0;

    protected TLSMessageProcessor(InetAddress ipAddress, SIPTransactionStack sipStack, int port) {
        super(ipAddress, port, ParameterNames.TLS, sipStack);
        this.sipStack = sipStack;
        this.tlsMessageChannels = new Hashtable();
        this.incomingTlsMessageChannels = new ArrayList();
    }

    public void start() throws IOException {
        Thread thread = new Thread(this);
        thread.setName("TLSMessageProcessorThread");
        thread.setPriority(10);
        thread.setDaemon(true);
        this.sock = this.sipStack.getNetworkLayer().createSSLServerSocket(getPort(), 0, getIpAddress());
        if (this.sipStack.getClientAuth() == ClientAuthType.Want || this.sipStack.getClientAuth() == ClientAuthType.Default) {
            ((SSLServerSocket) this.sock).setWantClientAuth(true);
        } else {
            ((SSLServerSocket) this.sock).setWantClientAuth(false);
        }
        if (this.sipStack.getClientAuth() == ClientAuthType.Enabled) {
            ((SSLServerSocket) this.sock).setNeedClientAuth(true);
        } else {
            ((SSLServerSocket) this.sock).setNeedClientAuth(false);
        }
        ((SSLServerSocket) this.sock).setUseClientMode(false);
        ((SSLServerSocket) this.sock).setEnabledCipherSuites(((SipStackImpl) this.sipStack).getEnabledCipherSuites());
        if (this.sipStack.getClientAuth() == ClientAuthType.Want || this.sipStack.getClientAuth() == ClientAuthType.Default) {
            ((SSLServerSocket) this.sock).setWantClientAuth(true);
        } else {
            ((SSLServerSocket) this.sock).setWantClientAuth(false);
        }
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("SSLServerSocket want client auth " + ((SSLServerSocket) this.sock).getWantClientAuth());
            logger.logDebug("SSLServerSocket need client auth " + ((SSLServerSocket) this.sock).getNeedClientAuth());
        }
        this.isRunning = true;
        thread.start();
    }

    /* JADX WARNING: Missing block: B:20:0x002e, code skipped:
            if (logger.isLoggingEnabled(32) == false) goto L_0x0037;
     */
    /* JADX WARNING: Missing block: B:21:0x0030, code skipped:
            logger.logDebug(" waiting to accept new connection!");
     */
    /* JADX WARNING: Missing block: B:22:0x0037, code skipped:
            r1 = r8.sock.accept();
     */
    /* JADX WARNING: Missing block: B:23:0x0045, code skipped:
            if (logger.isLoggingEnabled(32) == false) goto L_0x004e;
     */
    /* JADX WARNING: Missing block: B:24:0x0047, code skipped:
            logger.logDebug("Accepting new connection!");
     */
    /* JADX WARNING: Missing block: B:25:0x004e, code skipped:
            r8.incomingTlsMessageChannels.add(new org.jitsi.gov.nist.javax.sip.stack.TLSMessageChannel(r1, r8.sipStack, r8, "TLSMessageChannelThread-" + r8.nConnections));
     */
    public void run() {
        /*
        r8 = this;
        r7 = 0;
    L_0x0001:
        r2 = r8.isRunning;
        if (r2 == 0) goto L_0x001d;
    L_0x0005:
        monitor-enter(r8);	 Catch:{ SocketException -> 0x0070, SSLException -> 0x0082, IOException -> 0x008d, Exception -> 0x0097 }
    L_0x0006:
        r2 = r8.sipStack;	 Catch:{ all -> 0x007f }
        r2 = r2.maxConnections;	 Catch:{ all -> 0x007f }
        r3 = -1;
        if (r2 == r3) goto L_0x001f;
    L_0x000d:
        r2 = r8.nConnections;	 Catch:{ all -> 0x007f }
        r3 = r8.sipStack;	 Catch:{ all -> 0x007f }
        r3 = r3.maxConnections;	 Catch:{ all -> 0x007f }
        if (r2 < r3) goto L_0x001f;
    L_0x0015:
        r8.wait();	 Catch:{ InterruptedException -> 0x001e }
        r2 = r8.isRunning;	 Catch:{ InterruptedException -> 0x001e }
        if (r2 != 0) goto L_0x0006;
    L_0x001c:
        monitor-exit(r8);	 Catch:{ all -> 0x007f }
    L_0x001d:
        return;
    L_0x001e:
        r0 = move-exception;
    L_0x001f:
        r2 = r8.nConnections;	 Catch:{ all -> 0x007f }
        r2 = r2 + 1;
        r8.nConnections = r2;	 Catch:{ all -> 0x007f }
        monitor-exit(r8);	 Catch:{ all -> 0x007f }
        r2 = logger;	 Catch:{ SocketException -> 0x0070, SSLException -> 0x0082, IOException -> 0x008d, Exception -> 0x0097 }
        r3 = 32;
        r2 = r2.isLoggingEnabled(r3);	 Catch:{ SocketException -> 0x0070, SSLException -> 0x0082, IOException -> 0x008d, Exception -> 0x0097 }
        if (r2 == 0) goto L_0x0037;
    L_0x0030:
        r2 = logger;	 Catch:{ SocketException -> 0x0070, SSLException -> 0x0082, IOException -> 0x008d, Exception -> 0x0097 }
        r3 = " waiting to accept new connection!";
        r2.logDebug(r3);	 Catch:{ SocketException -> 0x0070, SSLException -> 0x0082, IOException -> 0x008d, Exception -> 0x0097 }
    L_0x0037:
        r2 = r8.sock;	 Catch:{ SocketException -> 0x0070, SSLException -> 0x0082, IOException -> 0x008d, Exception -> 0x0097 }
        r1 = r2.accept();	 Catch:{ SocketException -> 0x0070, SSLException -> 0x0082, IOException -> 0x008d, Exception -> 0x0097 }
        r2 = logger;	 Catch:{ SocketException -> 0x0070, SSLException -> 0x0082, IOException -> 0x008d, Exception -> 0x0097 }
        r3 = 32;
        r2 = r2.isLoggingEnabled(r3);	 Catch:{ SocketException -> 0x0070, SSLException -> 0x0082, IOException -> 0x008d, Exception -> 0x0097 }
        if (r2 == 0) goto L_0x004e;
    L_0x0047:
        r2 = logger;	 Catch:{ SocketException -> 0x0070, SSLException -> 0x0082, IOException -> 0x008d, Exception -> 0x0097 }
        r3 = "Accepting new connection!";
        r2.logDebug(r3);	 Catch:{ SocketException -> 0x0070, SSLException -> 0x0082, IOException -> 0x008d, Exception -> 0x0097 }
    L_0x004e:
        r2 = r8.incomingTlsMessageChannels;	 Catch:{ SocketException -> 0x0070, SSLException -> 0x0082, IOException -> 0x008d, Exception -> 0x0097 }
        r3 = new org.jitsi.gov.nist.javax.sip.stack.TLSMessageChannel;	 Catch:{ SocketException -> 0x0070, SSLException -> 0x0082, IOException -> 0x008d, Exception -> 0x0097 }
        r4 = r8.sipStack;	 Catch:{ SocketException -> 0x0070, SSLException -> 0x0082, IOException -> 0x008d, Exception -> 0x0097 }
        r5 = new java.lang.StringBuilder;	 Catch:{ SocketException -> 0x0070, SSLException -> 0x0082, IOException -> 0x008d, Exception -> 0x0097 }
        r5.<init>();	 Catch:{ SocketException -> 0x0070, SSLException -> 0x0082, IOException -> 0x008d, Exception -> 0x0097 }
        r6 = "TLSMessageChannelThread-";
        r5 = r5.append(r6);	 Catch:{ SocketException -> 0x0070, SSLException -> 0x0082, IOException -> 0x008d, Exception -> 0x0097 }
        r6 = r8.nConnections;	 Catch:{ SocketException -> 0x0070, SSLException -> 0x0082, IOException -> 0x008d, Exception -> 0x0097 }
        r5 = r5.append(r6);	 Catch:{ SocketException -> 0x0070, SSLException -> 0x0082, IOException -> 0x008d, Exception -> 0x0097 }
        r5 = r5.toString();	 Catch:{ SocketException -> 0x0070, SSLException -> 0x0082, IOException -> 0x008d, Exception -> 0x0097 }
        r3.m1608init(r1, r4, r8, r5);	 Catch:{ SocketException -> 0x0070, SSLException -> 0x0082, IOException -> 0x008d, Exception -> 0x0097 }
        r2.add(r3);	 Catch:{ SocketException -> 0x0070, SSLException -> 0x0082, IOException -> 0x008d, Exception -> 0x0097 }
        goto L_0x0001;
    L_0x0070:
        r0 = move-exception;
        r2 = r8.isRunning;
        if (r2 == 0) goto L_0x0001;
    L_0x0075:
        r2 = logger;
        r3 = "Fatal - SocketException occured while Accepting connection";
        r2.logError(r3, r0);
        r8.isRunning = r7;
        goto L_0x001d;
    L_0x007f:
        r2 = move-exception;
        monitor-exit(r8);	 Catch:{ all -> 0x007f }
        throw r2;	 Catch:{ SocketException -> 0x0070, SSLException -> 0x0082, IOException -> 0x008d, Exception -> 0x0097 }
    L_0x0082:
        r0 = move-exception;
        r8.isRunning = r7;
        r2 = logger;
        r3 = "Fatal - SSSLException occured while Accepting connection";
        r2.logError(r3, r0);
        goto L_0x001d;
    L_0x008d:
        r0 = move-exception;
        r2 = logger;
        r3 = "Problem Accepting Connection";
        r2.logError(r3, r0);
        goto L_0x0001;
    L_0x0097:
        r0 = move-exception;
        r2 = logger;
        r3 = "Unexpected Exception!";
        r2.logError(r3, r0);
        goto L_0x0001;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.gov.nist.javax.sip.stack.TLSMessageProcessor.run():void");
    }

    public SIPTransactionStack getSIPStack() {
        return this.sipStack;
    }

    public synchronized void stop() {
        if (this.isRunning) {
            this.isRunning = false;
            try {
                this.sock.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (TLSMessageChannel next : this.tlsMessageChannels.values()) {
                next.close();
            }
            Iterator incomingMCIterator = this.incomingTlsMessageChannels.iterator();
            while (incomingMCIterator.hasNext()) {
                ((TLSMessageChannel) incomingMCIterator.next()).close();
            }
            notify();
        }
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void remove(TLSMessageChannel tlsMessageChannel) {
        String key = tlsMessageChannel.getKey();
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug(Thread.currentThread() + " removing " + key);
        }
        if (this.tlsMessageChannels.get(key) == tlsMessageChannel) {
            this.tlsMessageChannels.remove(key);
        }
        this.incomingTlsMessageChannels.remove(tlsMessageChannel);
    }

    public synchronized MessageChannel createMessageChannel(HostPort targetHostPort) throws IOException {
        MessageChannel messageChannel;
        String key = MessageChannel.getKey(targetHostPort, ListeningPoint.TLS);
        if (this.tlsMessageChannels.get(key) != null) {
            messageChannel = (TLSMessageChannel) this.tlsMessageChannels.get(key);
        } else {
            MessageChannel retval = new TLSMessageChannel(targetHostPort.getInetAddress(), targetHostPort.getPort(), this.sipStack, this);
            this.tlsMessageChannels.put(key, retval);
            retval.isCached = true;
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("key " + key);
                logger.logDebug("Creating " + retval);
            }
            messageChannel = retval;
        }
        return messageChannel;
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void cacheMessageChannel(TLSMessageChannel messageChannel) {
        String key = messageChannel.getKey();
        TLSMessageChannel currentChannel = (TLSMessageChannel) this.tlsMessageChannels.get(key);
        if (currentChannel != null) {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("Closing " + key);
            }
            currentChannel.close();
        }
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Caching " + key);
        }
        this.tlsMessageChannels.put(key, messageChannel);
    }

    public synchronized MessageChannel createMessageChannel(InetAddress host, int port) throws IOException {
        MessageChannel messageChannel;
        try {
            String key = MessageChannel.getKey(host, port, ListeningPoint.TLS);
            if (this.tlsMessageChannels.get(key) != null) {
                messageChannel = (TLSMessageChannel) this.tlsMessageChannels.get(key);
            } else {
                MessageChannel retval = new TLSMessageChannel(host, port, this.sipStack, this);
                this.tlsMessageChannels.put(key, retval);
                retval.isCached = true;
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("key " + key);
                    logger.logDebug("Creating " + retval);
                }
                messageChannel = retval;
            }
        } catch (UnknownHostException ex) {
            throw new IOException(ex.getMessage());
        }
        return messageChannel;
    }

    public int getMaximumMessageSize() {
        return Integer.MAX_VALUE;
    }

    public boolean inUse() {
        return this.useCount != 0;
    }

    public int getDefaultTargetPort() {
        return 5061;
    }

    public boolean isSecure() {
        return true;
    }
}
