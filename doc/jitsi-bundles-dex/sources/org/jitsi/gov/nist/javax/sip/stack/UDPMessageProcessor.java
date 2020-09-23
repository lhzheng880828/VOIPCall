package org.jitsi.gov.nist.javax.sip.stack;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.jitsi.gov.nist.core.CommonLogger;
import org.jitsi.gov.nist.core.HostPort;
import org.jitsi.gov.nist.core.InternalErrorHandler;
import org.jitsi.gov.nist.core.StackLogger;
import org.jitsi.gov.nist.core.ThreadAuditor.ThreadHandle;
import org.jitsi.gov.nist.javax.sip.SipStackImpl;

public class UDPMessageProcessor extends MessageProcessor {
    private static final int HIGHWAT = 5000;
    private static final int LOWAT = 2500;
    private static StackLogger logger = CommonLogger.getLogger(UDPMessageProcessor.class);
    BlockingQueueDispatchAuditor congestionAuditor;
    protected boolean isRunning;
    private int maxMessageSize = SipStackImpl.MAX_DATAGRAM_SIZE.intValue();
    protected LinkedList messageChannels;
    protected BlockingQueue<DatagramQueuedMessageDispatch> messageQueue;
    private int port;
    protected DatagramSocket sock;
    protected int threadPoolSize;

    protected UDPMessageProcessor(InetAddress ipAddress, SIPTransactionStack sipStack, int port) throws IOException {
        super(ipAddress, port, "udp", sipStack);
        this.sipStack = sipStack;
        if (sipStack.getMaxMessageSize() < SipStackImpl.MAX_DATAGRAM_SIZE.intValue() && sipStack.getMaxMessageSize() > 0) {
            this.maxMessageSize = sipStack.getMaxMessageSize();
        }
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Max Message size is " + this.maxMessageSize);
        }
        this.messageQueue = new LinkedBlockingQueue();
        if (sipStack.stackCongenstionControlTimeout > 0) {
            this.congestionAuditor = new BlockingQueueDispatchAuditor(this.messageQueue);
            this.congestionAuditor.setTimeout(sipStack.stackCongenstionControlTimeout);
            this.congestionAuditor.start(2000);
        }
        this.port = port;
        try {
            this.sock = sipStack.getNetworkLayer().createDatagramSocket(port, ipAddress);
            this.sock.setReceiveBufferSize(sipStack.getReceiveUdpBufferSize());
            this.sock.setSendBufferSize(sipStack.getSendUdpBufferSize());
            if (sipStack.getThreadAuditor().isEnabled()) {
                this.sock.setSoTimeout((int) sipStack.getThreadAuditor().getPingIntervalInMillisecs());
            }
            if (ipAddress.getHostAddress().equals("0.0.0.0") || ipAddress.getHostAddress().equals("::0")) {
                super.setIpAddress(this.sock.getLocalAddress());
            }
        } catch (SocketException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    public int getPort() {
        return this.port;
    }

    public void start() throws IOException {
        this.isRunning = true;
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.setName("UDPMessageProcessorThread");
        thread.setPriority(10);
        thread.start();
    }

    public void run() {
        this.messageChannels = new LinkedList();
        if (this.sipStack.threadPoolSize != -1) {
            for (int i = 0; i < this.sipStack.threadPoolSize; i++) {
                this.messageChannels.add(new UDPMessageChannel(this.sipStack, this, ((SipStackImpl) this.sipStack).getStackName() + "-UDPMessageChannelThread-" + i));
            }
        }
        ThreadHandle threadHandle = this.sipStack.getThreadAuditor().addCurrentThread();
        while (this.isRunning) {
            try {
                threadHandle.ping();
                int bufsize = this.maxMessageSize;
                DatagramPacket packet = new DatagramPacket(new byte[bufsize], bufsize);
                this.sock.receive(packet);
                if (this.sipStack.threadPoolSize != -1) {
                    this.messageQueue.offer(new DatagramQueuedMessageDispatch(packet, System.currentTimeMillis()));
                } else {
                    UDPMessageChannel uDPMessageChannel = new UDPMessageChannel(this.sipStack, this, packet);
                }
            } catch (SocketTimeoutException e) {
            } catch (SocketException e2) {
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("UDPMessageProcessor: Stopping");
                }
                this.isRunning = false;
            } catch (IOException ex) {
                this.isRunning = false;
                ex.printStackTrace();
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("UDPMessageProcessor: Got an IO Exception");
                }
            } catch (Exception ex2) {
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("UDPMessageProcessor: Unexpected Exception - quitting");
                }
                InternalErrorHandler.handleException(ex2);
                return;
            }
        }
    }

    public void stop() {
        this.isRunning = false;
        this.sock.close();
        Iterator i$ = this.messageChannels.iterator();
        while (i$.hasNext()) {
            ((MessageChannel) i$.next()).close();
        }
    }

    public String getTransport() {
        return "udp";
    }

    public SIPTransactionStack getSIPStack() {
        return this.sipStack;
    }

    public MessageChannel createMessageChannel(HostPort targetHostPort) throws UnknownHostException {
        return new UDPMessageChannel(targetHostPort.getInetAddress(), targetHostPort.getPort(), this.sipStack, this);
    }

    public MessageChannel createMessageChannel(InetAddress host, int port) throws IOException {
        return new UDPMessageChannel(host, port, this.sipStack, this);
    }

    public int getDefaultTargetPort() {
        return 5060;
    }

    public boolean isSecure() {
        return false;
    }

    public int getMaximumMessageSize() {
        return this.sipStack.getReceiveUdpBufferSize();
    }

    public boolean inUse() {
        return !this.messageQueue.isEmpty();
    }
}
