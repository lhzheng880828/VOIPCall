package org.jivesoftware.smackx.bytestreams.socks5;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.bytestreams.socks5.packet.Bytestream.StreamHost;

class Socks5Client {
    protected String digest;
    protected StreamHost streamHost;

    public Socks5Client(StreamHost streamHost, String digest) {
        this.streamHost = streamHost;
        this.digest = digest;
    }

    public Socket getSocket(int timeout) throws IOException, XMPPException, InterruptedException, TimeoutException {
        FutureTask<Socket> futureTask = new FutureTask(new Callable<Socket>() {
            public Socket call() throws Exception {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(Socks5Client.this.streamHost.getAddress(), Socks5Client.this.streamHost.getPort()));
                if (Socks5Client.this.establish(socket)) {
                    return socket;
                }
                socket.close();
                throw new XMPPException("establishing connection to SOCKS5 proxy failed");
            }
        });
        new Thread(futureTask).start();
        try {
            return (Socket) futureTask.get((long) timeout, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause != null) {
                if (cause instanceof IOException) {
                    throw ((IOException) cause);
                } else if (cause instanceof XMPPException) {
                    throw ((XMPPException) cause);
                }
            }
            throw new IOException("Error while connection to SOCKS5 proxy");
        }
    }

    /* access modifiers changed from: protected */
    public boolean establish(Socket socket) throws IOException {
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.write(new byte[]{(byte) 5, (byte) 1, (byte) 0});
        out.flush();
        byte[] response = new byte[2];
        in.readFully(response);
        if (response[0] != (byte) 5 || response[1] != (byte) 0) {
            return false;
        }
        byte[] connectionRequest = createSocks5ConnectRequest();
        out.write(connectionRequest);
        out.flush();
        try {
            byte[] connectionResponse = Socks5Utils.receiveSocks5Message(in);
            connectionRequest[1] = (byte) 0;
            return Arrays.equals(connectionRequest, connectionResponse);
        } catch (XMPPException e) {
            return false;
        }
    }

    private byte[] createSocks5ConnectRequest() {
        byte[] addr = this.digest.getBytes();
        byte[] data = new byte[(addr.length + 7)];
        data[0] = (byte) 5;
        data[1] = (byte) 1;
        data[2] = (byte) 0;
        data[3] = (byte) 3;
        data[4] = (byte) addr.length;
        System.arraycopy(addr, 0, data, 5, addr.length);
        data[data.length - 2] = (byte) 0;
        data[data.length - 1] = (byte) 0;
        return data;
    }
}
