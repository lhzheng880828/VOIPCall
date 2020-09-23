package org.dhcp4java;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DHCPCoreServer implements Runnable {
    private static final int BOUNDED_QUEUE_SIZE = 20;
    private static final Properties DEF_PROPS = new Properties();
    protected static final int PACKET_SIZE = 1500;
    public static final String SERVER_ADDRESS = "serverAddress";
    private static final String SERVER_ADDRESS_DEFAULT = "127.0.0.1:67";
    public static final String SERVER_THREADS = "serverThreads";
    private static final String SERVER_THREADS_DEFAULT = "2";
    public static final String SERVER_THREADS_KEEPALIVE = "serverThreadsKeepalive";
    private static final String SERVER_THREADS_KEEPALIVE_DEFAULT = "10000";
    public static final String SERVER_THREADS_MAX = "serverThreadsMax";
    private static final String SERVER_THREADS_MAX_DEFAULT = "4";
    private static final Logger logger = Logger.getLogger(DHCPCoreServer.class.getName().toLowerCase());
    protected Properties properties;
    private DatagramSocket serverSocket;
    protected DHCPServlet servlet;
    private InetSocketAddress sockAddress = null;
    private boolean stopped = false;
    protected ThreadPoolExecutor threadPool;
    protected Properties userProps;

    private static class ServerThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        final String namePrefix = ("DHCPCoreServer-" + poolNumber.getAndIncrement() + "-thread-");
        final AtomicInteger threadNumber = new AtomicInteger(1);

        ServerThreadFactory() {
        }

        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, this.namePrefix + this.threadNumber.getAndIncrement());
        }
    }

    static {
        DEF_PROPS.put(SERVER_ADDRESS, SERVER_ADDRESS_DEFAULT);
        DEF_PROPS.put(SERVER_THREADS, SERVER_THREADS_DEFAULT);
        DEF_PROPS.put(SERVER_THREADS_MAX, SERVER_THREADS_MAX_DEFAULT);
        DEF_PROPS.put(SERVER_THREADS_KEEPALIVE, SERVER_THREADS_KEEPALIVE_DEFAULT);
    }

    private DHCPCoreServer(DHCPServlet servlet, Properties userProps) {
        this.servlet = servlet;
        this.userProps = userProps;
    }

    public static DHCPCoreServer initServer(DHCPServlet servlet, Properties userProps) throws DHCPServerInitException {
        if (servlet == null) {
            throw new IllegalArgumentException("servlet must not be null");
        }
        DHCPCoreServer server = new DHCPCoreServer(servlet, userProps);
        server.init();
        return server;
    }

    /* access modifiers changed from: protected */
    public void init() throws DHCPServerInitException {
        if (this.serverSocket != null) {
            throw new IllegalStateException("Server already initialized");
        }
        try {
            this.properties = new Properties(DEF_PROPS);
            InputStream propFileStream = getClass().getResourceAsStream("/DHCPd.properties");
            if (propFileStream != null) {
                this.properties.load(propFileStream);
            } else {
                logger.severe("Could not load /DHCPd.properties");
            }
            if (this.userProps != null) {
                this.properties.putAll(this.userProps);
            }
            this.sockAddress = getInetSocketAddress(this.properties);
            if (this.sockAddress == null) {
                throw new DHCPServerInitException("Cannot find which SockAddress to open");
            }
            this.serverSocket = new DatagramSocket(null);
            this.serverSocket.setBroadcast(true);
            this.serverSocket.bind(this.sockAddress);
            this.threadPool = new ThreadPoolExecutor(Integer.valueOf(this.properties.getProperty(SERVER_THREADS)).intValue(), Integer.valueOf(this.properties.getProperty(SERVER_THREADS_MAX)).intValue(), (long) Integer.valueOf(this.properties.getProperty(SERVER_THREADS_KEEPALIVE)).intValue(), TimeUnit.MILLISECONDS, new ArrayBlockingQueue(BOUNDED_QUEUE_SIZE), new ServerThreadFactory());
            this.threadPool.prestartAllCoreThreads();
            this.servlet.setServer(this);
            this.servlet.init(this.properties);
        } catch (DHCPServerInitException e) {
            throw e;
        } catch (Exception e2) {
            this.serverSocket = null;
            logger.log(Level.SEVERE, "Cannot open socket", e2);
            throw new DHCPServerInitException("Unable to init server", e2);
        }
    }

    /* access modifiers changed from: protected */
    public void dispatch() {
        try {
            DatagramPacket requestDatagram = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
            logger.finer("Waiting for packet");
            this.serverSocket.receive(requestDatagram);
            if (logger.isLoggable(Level.FINER)) {
                StringBuilder sbuf = new StringBuilder("Received packet from ");
                DHCPPacket.appendHostAddress(sbuf, requestDatagram.getAddress());
                sbuf.append('(').append(requestDatagram.getPort()).append(')');
                logger.finer(sbuf.toString());
            }
            this.threadPool.execute(new DHCPServletDispatcher(this, this.servlet, requestDatagram));
        } catch (IOException e) {
            logger.log(Level.FINE, "IOException", e);
        }
    }

    /* access modifiers changed from: protected */
    public void sendResponse(DatagramPacket responseDatagram) {
        if (responseDatagram != null) {
            try {
                this.serverSocket.send(responseDatagram);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "IOException", e);
            }
        }
    }

    /* access modifiers changed from: protected */
    public InetSocketAddress getInetSocketAddress(Properties props) {
        if (props == null) {
            throw new IllegalArgumentException("null props not allowed");
        }
        String serverAddress = props.getProperty(SERVER_ADDRESS);
        if (serverAddress != null) {
            return parseSocketAddress(serverAddress);
        }
        throw new IllegalStateException("Cannot load SERVER_ADDRESS property");
    }

    public static InetSocketAddress parseSocketAddress(String address) {
        if (address == null) {
            throw new IllegalArgumentException("Null address not allowed");
        }
        int index = address.indexOf(58);
        if (index > 0) {
            return new InetSocketAddress(address.substring(0, index), Integer.parseInt(address.substring(index + 1, address.length())));
        }
        throw new IllegalArgumentException("semicolon missing for port number");
    }

    public void run() {
        if (this.serverSocket == null) {
            throw new IllegalStateException("Listening socket is not open - terminating");
        }
        while (!this.stopped) {
            try {
                dispatch();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Unexpected Exception", e);
            }
        }
    }

    public void stopServer() {
        this.stopped = true;
        this.serverSocket.close();
    }

    public InetSocketAddress getSockAddress() {
        return this.sockAddress;
    }
}
