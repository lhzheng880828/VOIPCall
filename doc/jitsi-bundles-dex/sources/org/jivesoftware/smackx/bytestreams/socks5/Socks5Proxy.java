package org.jivesoftware.smackx.bytestreams.socks5;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager;

public class Socks5Proxy {
    private static Socks5Proxy socks5Server;
    /* access modifiers changed from: private|final */
    public final List<String> allowedConnections = Collections.synchronizedList(new LinkedList());
    /* access modifiers changed from: private|final */
    public final Map<String, Socket> connectionMap = new ConcurrentHashMap();
    private final Set<String> localAddresses = Collections.synchronizedSet(new LinkedHashSet());
    private Socks5ServerProcess serverProcess = new Socks5ServerProcess();
    /* access modifiers changed from: private */
    public ServerSocket serverSocket;
    private Thread serverThread;

    private class Socks5ServerProcess implements Runnable {
        private Socks5ServerProcess() {
        }

        public void run() {
            while (true) {
                Socket socket = null;
                try {
                    if (!Socks5Proxy.this.serverSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
                        establishConnection(Socks5Proxy.this.serverSocket.accept());
                    } else {
                        return;
                    }
                } catch (SocketException e) {
                } catch (Exception e2) {
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e3) {
                        }
                    }
                }
            }
        }

        private void establishConnection(Socket socket) throws XMPPException, IOException {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            if (in.read() != 5) {
                throw new XMPPException("Only SOCKS5 supported");
            }
            byte[] auth = new byte[in.read()];
            in.readFully(auth);
            byte[] authMethodSelectionResponse = new byte[2];
            authMethodSelectionResponse[0] = (byte) 5;
            boolean noAuthMethodFound = false;
            for (byte b : auth) {
                if (b == (byte) 0) {
                    noAuthMethodFound = true;
                    break;
                }
            }
            if (noAuthMethodFound) {
                authMethodSelectionResponse[1] = (byte) 0;
                out.write(authMethodSelectionResponse);
                out.flush();
                byte[] connectionRequest = Socks5Utils.receiveSocks5Message(in);
                String responseDigest = new String(connectionRequest, 5, connectionRequest[4]);
                if (Socks5Proxy.this.allowedConnections.contains(responseDigest)) {
                    connectionRequest[1] = (byte) 0;
                    out.write(connectionRequest);
                    out.flush();
                    Socks5Proxy.this.connectionMap.put(responseDigest, socket);
                    return;
                }
                connectionRequest[1] = (byte) 5;
                out.write(connectionRequest);
                out.flush();
                throw new XMPPException("Connection is not allowed");
            }
            authMethodSelectionResponse[1] = (byte) -1;
            out.write(authMethodSelectionResponse);
            out.flush();
            throw new XMPPException("Authentication method not supported");
        }
    }

    private Socks5Proxy() {
        try {
            this.localAddresses.add(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
        }
    }

    public static synchronized Socks5Proxy getSocks5Proxy() {
        Socks5Proxy socks5Proxy;
        synchronized (Socks5Proxy.class) {
            if (socks5Server == null) {
                socks5Server = new Socks5Proxy();
            }
            if (SmackConfiguration.isLocalSocks5ProxyEnabled()) {
                socks5Server.start();
            }
            socks5Proxy = socks5Server;
        }
        return socks5Proxy;
    }

    public synchronized void start() {
        if (!isRunning()) {
            try {
                if (SmackConfiguration.getLocalSocks5ProxyPort() < 0) {
                    int port = Math.abs(SmackConfiguration.getLocalSocks5ProxyPort());
                    int i = 0;
                    while (i < InBandBytestreamManager.MAXIMUM_BLOCK_SIZE - port) {
                        try {
                            this.serverSocket = new ServerSocket(port + i);
                            break;
                        } catch (IOException e) {
                            i++;
                        }
                    }
                } else {
                    this.serverSocket = new ServerSocket(SmackConfiguration.getLocalSocks5ProxyPort());
                }
                if (this.serverSocket != null) {
                    this.serverThread = new Thread(this.serverProcess);
                    this.serverThread.start();
                }
            } catch (IOException e2) {
                System.err.println("couldn't setup local SOCKS5 proxy on port " + SmackConfiguration.getLocalSocks5ProxyPort() + ": " + e2.getMessage());
            }
        }
        return;
    }

    public synchronized void stop() {
        if (isRunning()) {
            try {
                this.serverSocket.close();
            } catch (IOException e) {
            }
            if (this.serverThread != null && this.serverThread.isAlive()) {
                try {
                    this.serverThread.interrupt();
                    this.serverThread.join();
                } catch (InterruptedException e2) {
                }
            }
            this.serverThread = null;
            this.serverSocket = null;
        }
    }

    public void addLocalAddress(String address) {
        if (address == null) {
            throw new IllegalArgumentException("address may not be null");
        }
        this.localAddresses.add(address);
    }

    public void removeLocalAddress(String address) {
        this.localAddresses.remove(address);
    }

    public List<String> getLocalAddresses() {
        return Collections.unmodifiableList(new ArrayList(this.localAddresses));
    }

    public void replaceLocalAddresses(List<String> addresses) {
        if (addresses == null) {
            throw new IllegalArgumentException("list must not be null");
        }
        this.localAddresses.clear();
        this.localAddresses.addAll(addresses);
    }

    public int getPort() {
        if (isRunning()) {
            return this.serverSocket.getLocalPort();
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public Socket getSocket(String digest) {
        return (Socket) this.connectionMap.get(digest);
    }

    /* access modifiers changed from: protected */
    public void addTransfer(String digest) {
        this.allowedConnections.add(digest);
    }

    /* access modifiers changed from: protected */
    public void removeTransfer(String digest) {
        this.allowedConnections.remove(digest);
        this.connectionMap.remove(digest);
    }

    public boolean isRunning() {
        return this.serverSocket != null;
    }
}
