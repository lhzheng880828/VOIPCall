package org.jivesoftware.smackx.bytestreams.ibb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.packet.XMPPError.Condition;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smack.util.SyncPacketSend;
import org.jivesoftware.smackx.bytestreams.BytestreamSession;
import org.jivesoftware.smackx.bytestreams.ibb.packet.Close;
import org.jivesoftware.smackx.bytestreams.ibb.packet.Data;
import org.jivesoftware.smackx.bytestreams.ibb.packet.DataPacketExtension;
import org.jivesoftware.smackx.bytestreams.ibb.packet.Open;

public class InBandBytestreamSession implements BytestreamSession {
    /* access modifiers changed from: private|final */
    public final Open byteStreamRequest;
    private boolean closeBothStreamsEnabled = false;
    /* access modifiers changed from: private|final */
    public final Connection connection;
    private IBBInputStream inputStream;
    private boolean isClosed = false;
    private IBBOutputStream outputStream;
    /* access modifiers changed from: private */
    public String remoteJID;

    private class IBBDataPacketFilter implements PacketFilter {
        private IBBDataPacketFilter() {
        }

        public boolean accept(Packet packet) {
            if (!packet.getFrom().equalsIgnoreCase(InBandBytestreamSession.this.remoteJID)) {
                return false;
            }
            PacketExtension packetExtension = packet.getExtension("data", InBandBytestreamManager.NAMESPACE);
            if (packetExtension != null && (packetExtension instanceof DataPacketExtension) && ((DataPacketExtension) packetExtension).getSessionID().equals(InBandBytestreamSession.this.byteStreamRequest.getSessionID())) {
                return true;
            }
            return false;
        }
    }

    private abstract class IBBInputStream extends InputStream {
        private byte[] buffer;
        private int bufferPointer = -1;
        private boolean closeInvoked = false;
        private final PacketListener dataPacketListener = getDataPacketListener();
        protected final BlockingQueue<DataPacketExtension> dataQueue = new LinkedBlockingQueue();
        /* access modifiers changed from: private */
        public boolean isClosed = false;
        /* access modifiers changed from: private */
        public int readTimeout = 0;
        private long seq = -1;

        public abstract PacketFilter getDataPacketFilter();

        public abstract PacketListener getDataPacketListener();

        public IBBInputStream() {
            InBandBytestreamSession.this.connection.addPacketListener(this.dataPacketListener, getDataPacketFilter());
        }

        public synchronized int read() throws IOException {
            int i = -1;
            synchronized (this) {
                checkClosed();
                if ((this.bufferPointer != -1 && this.bufferPointer < this.buffer.length) || loadBuffer()) {
                    byte[] bArr = this.buffer;
                    int i2 = this.bufferPointer;
                    this.bufferPointer = i2 + 1;
                    i = bArr[i2];
                }
            }
            return i;
        }

        public synchronized int read(byte[] b, int off, int len) throws IOException {
            int i = -1;
            synchronized (this) {
                if (b == null) {
                    throw new NullPointerException();
                }
                if (off >= 0) {
                    if (off <= b.length && len >= 0 && off + len <= b.length && off + len >= 0) {
                        if (len == 0) {
                            i = 0;
                        } else {
                            checkClosed();
                            if ((this.bufferPointer != -1 && this.bufferPointer < this.buffer.length) || loadBuffer()) {
                                int bytesAvailable = this.buffer.length - this.bufferPointer;
                                if (len > bytesAvailable) {
                                    len = bytesAvailable;
                                }
                                System.arraycopy(this.buffer, this.bufferPointer, b, off, len);
                                this.bufferPointer += len;
                                i = len;
                            }
                        }
                    }
                }
                throw new IndexOutOfBoundsException();
            }
            return i;
        }

        public synchronized int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        private synchronized boolean loadBuffer() throws IOException {
            boolean z;
            DataPacketExtension data = null;
            try {
                if (this.readTimeout == 0) {
                    while (data == null) {
                        if (this.isClosed && this.dataQueue.isEmpty()) {
                            z = false;
                            break;
                        }
                        data = (DataPacketExtension) this.dataQueue.poll(1000, TimeUnit.MILLISECONDS);
                    }
                } else {
                    data = (DataPacketExtension) this.dataQueue.poll((long) this.readTimeout, TimeUnit.MILLISECONDS);
                    if (data == null) {
                        throw new SocketTimeoutException();
                    }
                }
                if (this.seq == 65535) {
                    this.seq = -1;
                }
                long seq = data.getSeq();
                if (seq - 1 != this.seq) {
                    InBandBytestreamSession.this.close();
                    throw new IOException("Packets out of sequence");
                }
                this.seq = seq;
                this.buffer = data.getDecodedData();
                this.bufferPointer = 0;
                z = true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                z = false;
            }
            return z;
        }

        private void checkClosed() throws IOException {
            if ((this.isClosed && this.dataQueue.isEmpty()) || this.closeInvoked) {
                this.dataQueue.clear();
                throw new IOException("Stream is closed");
            }
        }

        public boolean markSupported() {
            return false;
        }

        public void close() throws IOException {
            if (!this.isClosed) {
                this.closeInvoked = true;
                InBandBytestreamSession.this.closeByLocal(true);
            }
        }

        /* access modifiers changed from: private */
        public void closeInternal() {
            if (!this.isClosed) {
                this.isClosed = true;
            }
        }

        /* access modifiers changed from: private */
        public void cleanup() {
            InBandBytestreamSession.this.connection.removePacketListener(this.dataPacketListener);
        }
    }

    private abstract class IBBOutputStream extends OutputStream {
        protected final byte[] buffer;
        protected int bufferPointer = 0;
        protected boolean isClosed = false;
        protected long seq = 0;

        public abstract void writeToXML(DataPacketExtension dataPacketExtension) throws IOException;

        public IBBOutputStream() {
            this.buffer = new byte[((InBandBytestreamSession.this.byteStreamRequest.getBlockSize() / 4) * 3)];
        }

        public synchronized void write(int b) throws IOException {
            if (this.isClosed) {
                throw new IOException("Stream is closed");
            }
            if (this.bufferPointer >= this.buffer.length) {
                flushBuffer();
            }
            byte[] bArr = this.buffer;
            int i = this.bufferPointer;
            this.bufferPointer = i + 1;
            bArr[i] = (byte) b;
        }

        public synchronized void write(byte[] b, int off, int len) throws IOException {
            if (b == null) {
                throw new NullPointerException();
            }
            if (off >= 0) {
                if (off <= b.length && len >= 0 && off + len <= b.length && off + len >= 0) {
                    if (len != 0) {
                        if (this.isClosed) {
                            throw new IOException("Stream is closed");
                        } else if (len >= this.buffer.length) {
                            writeOut(b, off, this.buffer.length);
                            write(b, this.buffer.length + off, len - this.buffer.length);
                        } else {
                            writeOut(b, off, len);
                        }
                    }
                }
            }
            throw new IndexOutOfBoundsException();
        }

        public synchronized void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }

        private synchronized void writeOut(byte[] b, int off, int len) throws IOException {
            if (this.isClosed) {
                throw new IOException("Stream is closed");
            }
            int available = 0;
            if (len > this.buffer.length - this.bufferPointer) {
                available = this.buffer.length - this.bufferPointer;
                System.arraycopy(b, off, this.buffer, this.bufferPointer, available);
                this.bufferPointer += available;
                flushBuffer();
            }
            System.arraycopy(b, off + available, this.buffer, this.bufferPointer, len - available);
            this.bufferPointer += len - available;
        }

        public synchronized void flush() throws IOException {
            if (this.isClosed) {
                throw new IOException("Stream is closed");
            }
            flushBuffer();
        }

        private synchronized void flushBuffer() throws IOException {
            if (this.bufferPointer != 0) {
                long j;
                writeToXML(new DataPacketExtension(InBandBytestreamSession.this.byteStreamRequest.getSessionID(), this.seq, StringUtils.encodeBase64(this.buffer, 0, this.bufferPointer, false)));
                this.bufferPointer = 0;
                if (this.seq + 1 == 65535) {
                    j = 0;
                } else {
                    j = this.seq + 1;
                }
                this.seq = j;
            }
        }

        public void close() throws IOException {
            if (!this.isClosed) {
                InBandBytestreamSession.this.closeByLocal(false);
            }
        }

        /* access modifiers changed from: protected */
        public void closeInternal(boolean flush) {
            if (!this.isClosed) {
                this.isClosed = true;
                if (flush) {
                    try {
                        flushBuffer();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    private class IQIBBInputStream extends IBBInputStream {
        private IQIBBInputStream() {
            super();
        }

        /* access modifiers changed from: protected */
        public PacketListener getDataPacketListener() {
            return new PacketListener() {
                private long lastSequence = -1;

                public void processPacket(Packet packet) {
                    DataPacketExtension data = (DataPacketExtension) packet.getExtension("data", InBandBytestreamManager.NAMESPACE);
                    if (data.getSeq() <= this.lastSequence) {
                        InBandBytestreamSession.this.connection.sendPacket(IQ.createErrorResponse((IQ) packet, new XMPPError(Condition.unexpected_request)));
                    } else if (data.getDecodedData() == null) {
                        InBandBytestreamSession.this.connection.sendPacket(IQ.createErrorResponse((IQ) packet, new XMPPError(Condition.bad_request)));
                    } else {
                        IQIBBInputStream.this.dataQueue.offer(data);
                        InBandBytestreamSession.this.connection.sendPacket(IQ.createResultIQ((IQ) packet));
                        this.lastSequence = data.getSeq();
                        if (this.lastSequence == 65535) {
                            this.lastSequence = -1;
                        }
                    }
                }
            };
        }

        /* access modifiers changed from: protected */
        public PacketFilter getDataPacketFilter() {
            return new AndFilter(new PacketTypeFilter(Data.class), new IBBDataPacketFilter());
        }
    }

    private class IQIBBOutputStream extends IBBOutputStream {
        private IQIBBOutputStream() {
            super();
        }

        /* access modifiers changed from: protected|declared_synchronized */
        public synchronized void writeToXML(DataPacketExtension data) throws IOException {
            IQ iq = new Data(data);
            iq.setTo(InBandBytestreamSession.this.remoteJID);
            try {
                SyncPacketSend.getReply(InBandBytestreamSession.this.connection, iq);
            } catch (XMPPException e) {
                if (!this.isClosed) {
                    InBandBytestreamSession.this.close();
                    throw new IOException("Error while sending Data: " + e.getMessage());
                }
            }
        }
    }

    private class MessageIBBInputStream extends IBBInputStream {
        private MessageIBBInputStream() {
            super();
        }

        /* access modifiers changed from: protected */
        public PacketListener getDataPacketListener() {
            return new PacketListener() {
                public void processPacket(Packet packet) {
                    DataPacketExtension data = (DataPacketExtension) packet.getExtension("data", InBandBytestreamManager.NAMESPACE);
                    if (data.getDecodedData() != null) {
                        MessageIBBInputStream.this.dataQueue.offer(data);
                    }
                }
            };
        }

        /* access modifiers changed from: protected */
        public PacketFilter getDataPacketFilter() {
            return new AndFilter(new PacketTypeFilter(Message.class), new IBBDataPacketFilter());
        }
    }

    private class MessageIBBOutputStream extends IBBOutputStream {
        private MessageIBBOutputStream() {
            super();
        }

        /* access modifiers changed from: protected|declared_synchronized */
        public synchronized void writeToXML(DataPacketExtension data) {
            Message message = new Message(InBandBytestreamSession.this.remoteJID);
            message.addExtension(data);
            InBandBytestreamSession.this.connection.sendPacket(message);
        }
    }

    protected InBandBytestreamSession(Connection connection, Open byteStreamRequest, String remoteJID) {
        this.connection = connection;
        this.byteStreamRequest = byteStreamRequest;
        this.remoteJID = remoteJID;
        switch (byteStreamRequest.getStanza()) {
            case IQ:
                this.inputStream = new IQIBBInputStream();
                this.outputStream = new IQIBBOutputStream();
                return;
            case MESSAGE:
                this.inputStream = new MessageIBBInputStream();
                this.outputStream = new MessageIBBOutputStream();
                return;
            default:
                return;
        }
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }

    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    public int getReadTimeout() {
        return this.inputStream.readTimeout;
    }

    public void setReadTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("Timeout must be >= 0");
        }
        this.inputStream.readTimeout = timeout;
    }

    public boolean isCloseBothStreamsEnabled() {
        return this.closeBothStreamsEnabled;
    }

    public void setCloseBothStreamsEnabled(boolean closeBothStreamsEnabled) {
        this.closeBothStreamsEnabled = closeBothStreamsEnabled;
    }

    public void close() throws IOException {
        closeByLocal(true);
        closeByLocal(false);
    }

    /* access modifiers changed from: protected */
    public void closeByPeer(Close closeRequest) {
        this.inputStream.closeInternal();
        this.inputStream.cleanup();
        this.outputStream.closeInternal(false);
        this.connection.sendPacket(IQ.createResultIQ(closeRequest));
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void closeByLocal(boolean in) throws IOException {
        if (!this.isClosed) {
            if (this.closeBothStreamsEnabled) {
                this.inputStream.closeInternal();
                this.outputStream.closeInternal(true);
            } else if (in) {
                this.inputStream.closeInternal();
            } else {
                this.outputStream.closeInternal(true);
            }
            if (this.inputStream.isClosed && this.outputStream.isClosed) {
                this.isClosed = true;
                Close close = new Close(this.byteStreamRequest.getSessionID());
                close.setTo(this.remoteJID);
                try {
                    SyncPacketSend.getReply(this.connection, close);
                    this.inputStream.cleanup();
                    InBandBytestreamManager.getByteStreamManager(this.connection).getSessions().remove(this);
                } catch (XMPPException e) {
                    throw new IOException("Error while closing stream: " + e.getMessage());
                }
            }
        }
    }
}
