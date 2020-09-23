package org.jitsi.gov.nist.javax.sip.stack;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import org.jitsi.gov.nist.core.CommonLogger;
import org.jitsi.gov.nist.core.HostPort;
import org.jitsi.gov.nist.core.StackLogger;
import org.jitsi.javax.sip.ListeningPoint;

public class TCPMessageProcessor extends MessageProcessor {
    private static StackLogger logger = CommonLogger.getLogger(TCPMessageProcessor.class);
    private ArrayList<TCPMessageChannel> incomingTcpMessageChannels = new ArrayList();
    private boolean isRunning;
    protected int nConnections;
    private ServerSocket sock;
    private Hashtable tcpMessageChannels = new Hashtable();
    protected int useCount;

    protected TCPMessageProcessor(InetAddress ipAddress, SIPTransactionStack sipStack, int port) {
        super(ipAddress, port, "tcp", sipStack);
        this.sipStack = sipStack;
    }

    public void start() throws IOException {
        Thread thread = new Thread(this);
        thread.setName("TCPMessageProcessorThread");
        thread.setPriority(10);
        thread.setDaemon(true);
        this.sock = this.sipStack.getNetworkLayer().createServerSocket(getPort(), 0, getIpAddress());
        if (getIpAddress().getHostAddress().equals("0.0.0.0") || getIpAddress().getHostAddress().equals("::0")) {
            super.setIpAddress(this.sock.getInetAddress());
        }
        this.isRunning = true;
        thread.start();
    }

    /* JADX WARNING: Missing block: B:18:?, code skipped:
            r1 = r7.sock.accept();
     */
    /* JADX WARNING: Missing block: B:19:0x0033, code skipped:
            if (logger.isLoggingEnabled(32) == false) goto L_0x003c;
     */
    /* JADX WARNING: Missing block: B:20:0x0035, code skipped:
            logger.logDebug("Accepting new connection!");
     */
    /* JADX WARNING: Missing block: B:21:0x003c, code skipped:
            r7.incomingTcpMessageChannels.add(new org.jitsi.gov.nist.javax.sip.stack.TCPMessageChannel(r1, r7.sipStack, r7, "TCPMessageChannelThread-" + r7.nConnections));
     */
    public void run() {
        /*
        r7 = this;
    L_0x0000:
        r2 = r7.isRunning;
        if (r2 == 0) goto L_0x001c;
    L_0x0004:
        monitor-enter(r7);	 Catch:{ SocketException -> 0x005e, IOException -> 0x0066, Exception -> 0x0075 }
    L_0x0005:
        r2 = r7.sipStack;	 Catch:{ all -> 0x0063 }
        r2 = r2.maxConnections;	 Catch:{ all -> 0x0063 }
        r3 = -1;
        if (r2 == r3) goto L_0x001e;
    L_0x000c:
        r2 = r7.nConnections;	 Catch:{ all -> 0x0063 }
        r3 = r7.sipStack;	 Catch:{ all -> 0x0063 }
        r3 = r3.maxConnections;	 Catch:{ all -> 0x0063 }
        if (r2 < r3) goto L_0x001e;
    L_0x0014:
        r7.wait();	 Catch:{ InterruptedException -> 0x001d }
        r2 = r7.isRunning;	 Catch:{ InterruptedException -> 0x001d }
        if (r2 != 0) goto L_0x0005;
    L_0x001b:
        monitor-exit(r7);	 Catch:{ all -> 0x0063 }
    L_0x001c:
        return;
    L_0x001d:
        r0 = move-exception;
    L_0x001e:
        r2 = r7.nConnections;	 Catch:{ all -> 0x0063 }
        r2 = r2 + 1;
        r7.nConnections = r2;	 Catch:{ all -> 0x0063 }
        monitor-exit(r7);	 Catch:{ all -> 0x0063 }
        r2 = r7.sock;	 Catch:{ SocketException -> 0x005e, IOException -> 0x0066, Exception -> 0x0075 }
        r1 = r2.accept();	 Catch:{ SocketException -> 0x005e, IOException -> 0x0066, Exception -> 0x0075 }
        r2 = logger;	 Catch:{ SocketException -> 0x005e, IOException -> 0x0066, Exception -> 0x0075 }
        r3 = 32;
        r2 = r2.isLoggingEnabled(r3);	 Catch:{ SocketException -> 0x005e, IOException -> 0x0066, Exception -> 0x0075 }
        if (r2 == 0) goto L_0x003c;
    L_0x0035:
        r2 = logger;	 Catch:{ SocketException -> 0x005e, IOException -> 0x0066, Exception -> 0x0075 }
        r3 = "Accepting new connection!";
        r2.logDebug(r3);	 Catch:{ SocketException -> 0x005e, IOException -> 0x0066, Exception -> 0x0075 }
    L_0x003c:
        r2 = r7.incomingTcpMessageChannels;	 Catch:{ SocketException -> 0x005e, IOException -> 0x0066, Exception -> 0x0075 }
        r3 = new org.jitsi.gov.nist.javax.sip.stack.TCPMessageChannel;	 Catch:{ SocketException -> 0x005e, IOException -> 0x0066, Exception -> 0x0075 }
        r4 = r7.sipStack;	 Catch:{ SocketException -> 0x005e, IOException -> 0x0066, Exception -> 0x0075 }
        r5 = new java.lang.StringBuilder;	 Catch:{ SocketException -> 0x005e, IOException -> 0x0066, Exception -> 0x0075 }
        r5.<init>();	 Catch:{ SocketException -> 0x005e, IOException -> 0x0066, Exception -> 0x0075 }
        r6 = "TCPMessageChannelThread-";
        r5 = r5.append(r6);	 Catch:{ SocketException -> 0x005e, IOException -> 0x0066, Exception -> 0x0075 }
        r6 = r7.nConnections;	 Catch:{ SocketException -> 0x005e, IOException -> 0x0066, Exception -> 0x0075 }
        r5 = r5.append(r6);	 Catch:{ SocketException -> 0x005e, IOException -> 0x0066, Exception -> 0x0075 }
        r5 = r5.toString();	 Catch:{ SocketException -> 0x005e, IOException -> 0x0066, Exception -> 0x0075 }
        r3.m1602init(r1, r4, r7, r5);	 Catch:{ SocketException -> 0x005e, IOException -> 0x0066, Exception -> 0x0075 }
        r2.add(r3);	 Catch:{ SocketException -> 0x005e, IOException -> 0x0066, Exception -> 0x0075 }
        goto L_0x0000;
    L_0x005e:
        r0 = move-exception;
        r2 = 0;
        r7.isRunning = r2;
        goto L_0x0000;
    L_0x0063:
        r2 = move-exception;
        monitor-exit(r7);	 Catch:{ all -> 0x0063 }
        throw r2;	 Catch:{ SocketException -> 0x005e, IOException -> 0x0066, Exception -> 0x0075 }
    L_0x0066:
        r0 = move-exception;
        r2 = logger;
        r2 = r2.isLoggingEnabled();
        if (r2 == 0) goto L_0x0000;
    L_0x006f:
        r2 = logger;
        r2.logException(r0);
        goto L_0x0000;
    L_0x0075:
        r0 = move-exception;
        org.jitsi.gov.nist.core.InternalErrorHandler.handleException(r0);
        goto L_0x0000;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.gov.nist.javax.sip.stack.TCPMessageProcessor.run():void");
    }

    public String getTransport() {
        return "tcp";
    }

    public SIPTransactionStack getSIPStack() {
        return this.sipStack;
    }

    public synchronized void stop() {
        this.isRunning = false;
        try {
            this.sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (TCPMessageChannel next : this.tcpMessageChannels.values()) {
            next.close();
        }
        Iterator incomingMCIterator = this.incomingTcpMessageChannels.iterator();
        while (incomingMCIterator.hasNext()) {
            ((TCPMessageChannel) incomingMCIterator.next()).close();
        }
        notify();
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void remove(TCPMessageChannel tcpMessageChannel) {
        String key = tcpMessageChannel.getKey();
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug(Thread.currentThread() + " removing " + key);
        }
        if (this.tcpMessageChannels.get(key) == tcpMessageChannel) {
            this.tcpMessageChannels.remove(key);
        }
        this.incomingTcpMessageChannels.remove(tcpMessageChannel);
    }

    public synchronized MessageChannel createMessageChannel(HostPort targetHostPort) throws IOException {
        MessageChannel messageChannel;
        String key = MessageChannel.getKey(targetHostPort, ListeningPoint.TCP);
        if (this.tcpMessageChannels.get(key) != null) {
            messageChannel = (TCPMessageChannel) this.tcpMessageChannels.get(key);
        } else {
            MessageChannel retval = new TCPMessageChannel(targetHostPort.getInetAddress(), targetHostPort.getPort(), this.sipStack, this);
            this.tcpMessageChannels.put(key, retval);
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
    public synchronized void cacheMessageChannel(TCPMessageChannel messageChannel) {
        String key = messageChannel.getKey();
        TCPMessageChannel currentChannel = (TCPMessageChannel) this.tcpMessageChannels.get(key);
        if (currentChannel != null) {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("Closing " + key);
            }
            currentChannel.close();
        }
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Caching " + key);
        }
        this.tcpMessageChannels.put(key, messageChannel);
    }

    public synchronized MessageChannel createMessageChannel(InetAddress host, int port) throws IOException {
        MessageChannel messageChannel;
        try {
            String key = MessageChannel.getKey(host, port, ListeningPoint.TCP);
            if (this.tcpMessageChannels.get(key) != null) {
                messageChannel = (TCPMessageChannel) this.tcpMessageChannels.get(key);
            } else {
                MessageChannel retval = new TCPMessageChannel(host, port, this.sipStack, this);
                this.tcpMessageChannels.put(key, retval);
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
        return 5060;
    }

    public boolean isSecure() {
        return false;
    }
}
