package org.jivesoftware.smackx.bytestreams.socks5;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import org.jivesoftware.smackx.bytestreams.BytestreamSession;

public class Socks5BytestreamSession implements BytestreamSession {
    private final boolean isDirect;
    private final Socket socket;

    protected Socks5BytestreamSession(Socket socket, boolean isDirect) {
        this.socket = socket;
        this.isDirect = isDirect;
    }

    public boolean isDirect() {
        return this.isDirect;
    }

    public boolean isMediated() {
        return !this.isDirect;
    }

    public InputStream getInputStream() throws IOException {
        return this.socket.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return this.socket.getOutputStream();
    }

    public int getReadTimeout() throws IOException {
        try {
            return this.socket.getSoTimeout();
        } catch (SocketException e) {
            throw new IOException("Error on underlying Socket");
        }
    }

    public void setReadTimeout(int timeout) throws IOException {
        try {
            this.socket.setSoTimeout(timeout);
        } catch (SocketException e) {
            throw new IOException("Error on underlying Socket");
        }
    }

    public void close() throws IOException {
        this.socket.close();
    }
}
