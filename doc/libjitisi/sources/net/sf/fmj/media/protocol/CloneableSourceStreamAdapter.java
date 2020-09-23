package net.sf.fmj.media.protocol;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.VideoFormat;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferStream;
import javax.media.protocol.PullSourceStream;
import javax.media.protocol.PushBufferStream;
import javax.media.protocol.PushSourceStream;
import javax.media.protocol.SourceStream;
import javax.media.protocol.SourceTransferHandler;
import net.sf.fmj.media.util.MediaThread;

public class CloneableSourceStreamAdapter {
    SourceStream adapter = null;
    SourceStream master;
    protected int numTracks = 0;
    Vector slaves = new Vector();
    protected Format[] trackFormats;

    class SourceStreamAdapter implements SourceStream {
        SourceStreamAdapter() {
        }

        public boolean endOfStream() {
            return CloneableSourceStreamAdapter.this.master.endOfStream();
        }

        public ContentDescriptor getContentDescriptor() {
            return CloneableSourceStreamAdapter.this.master.getContentDescriptor();
        }

        public long getContentLength() {
            return CloneableSourceStreamAdapter.this.master.getContentLength();
        }

        public Object getControl(String controlType) {
            return CloneableSourceStreamAdapter.this.master.getControl(controlType);
        }

        public Object[] getControls() {
            return CloneableSourceStreamAdapter.this.master.getControls();
        }
    }

    class PullBufferStreamAdapter extends SourceStreamAdapter implements PullBufferStream {
        PullBufferStreamAdapter() {
            super();
        }

        public Format getFormat() {
            return ((PullBufferStream) CloneableSourceStreamAdapter.this.master).getFormat();
        }

        public void read(Buffer buffer) throws IOException {
            CloneableSourceStreamAdapter.this.copyAndRead(buffer);
        }

        public boolean willReadBlock() {
            return ((PullBufferStream) CloneableSourceStreamAdapter.this.master).willReadBlock();
        }
    }

    class PullSourceStreamAdapter extends SourceStreamAdapter implements PullSourceStream {
        PullSourceStreamAdapter() {
            super();
        }

        public int read(byte[] buffer, int offset, int length) throws IOException {
            return CloneableSourceStreamAdapter.this.copyAndRead(buffer, offset, length);
        }

        public boolean willReadBlock() {
            return ((PullSourceStream) CloneableSourceStreamAdapter.this.master).willReadBlock();
        }
    }

    class PushBufferStreamAdapter extends SourceStreamAdapter implements PushBufferStream, BufferTransferHandler {
        BufferTransferHandler handler;

        PushBufferStreamAdapter() {
            super();
        }

        public Format getFormat() {
            return ((PushBufferStream) CloneableSourceStreamAdapter.this.master).getFormat();
        }

        public void read(Buffer buffer) throws IOException {
            CloneableSourceStreamAdapter.this.copyAndRead(buffer);
        }

        public void setTransferHandler(BufferTransferHandler transferHandler) {
            this.handler = transferHandler;
            ((PushBufferStream) CloneableSourceStreamAdapter.this.master).setTransferHandler(this);
        }

        public void transferData(PushBufferStream stream) {
            if (this.handler != null) {
                this.handler.transferData(this);
            }
        }
    }

    abstract class PushStreamSlave extends SourceStreamAdapter implements SourceStreamSlave, Runnable {
        boolean connected = false;
        MediaThread notifyingThread;

        PushStreamSlave() {
            super();
        }

        public synchronized void connect() {
            if (!this.connected) {
                this.connected = true;
                this.notifyingThread = new MediaThread((Runnable) this);
                if (this.notifyingThread != null) {
                    if (this instanceof PushBufferStream) {
                        if (((PushBufferStream) this).getFormat() instanceof VideoFormat) {
                            this.notifyingThread.useVideoPriority();
                        } else {
                            this.notifyingThread.useAudioPriority();
                        }
                    }
                    this.notifyingThread.start();
                }
            }
        }

        public synchronized void disconnect() {
            this.connected = false;
            notifyAll();
        }
    }

    class PushBufferStreamSlave extends PushStreamSlave implements PushBufferStream, Runnable {
        private Buffer b;
        BufferTransferHandler handler;

        PushBufferStreamSlave() {
            super();
        }

        public Format getFormat() {
            if (CloneableSourceStreamAdapter.this.master instanceof PullBufferStream) {
                return ((PullBufferStream) CloneableSourceStreamAdapter.this.master).getFormat();
            }
            if (CloneableSourceStreamAdapter.this.master instanceof PushBufferStream) {
                return ((PushBufferStream) CloneableSourceStreamAdapter.this.master).getFormat();
            }
            return null;
        }

        /* access modifiers changed from: 0000 */
        public BufferTransferHandler getTransferHandler() {
            return this.handler;
        }

        public synchronized void read(Buffer buffer) throws IOException {
            while (this.b == null && this.connected) {
                try {
                    wait(50);
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
            }
            if (this.connected) {
                buffer.copy(this.b);
                this.b = null;
            } else {
                throw new IOException("DataSource is not connected");
            }
        }

        public void run() {
            while (!endOfStream() && this.connected) {
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                if (this.connected && this.handler != null) {
                    this.handler.transferData(this);
                }
            }
        }

        /* access modifiers changed from: declared_synchronized */
        public synchronized void setBuffer(Buffer b) {
            this.b = b;
            notifyAll();
        }

        public void setTransferHandler(BufferTransferHandler transferHandler) {
            this.handler = transferHandler;
        }
    }

    class PushSourceStreamAdapter extends SourceStreamAdapter implements PushSourceStream, SourceTransferHandler {
        SourceTransferHandler handler;

        PushSourceStreamAdapter() {
            super();
        }

        public int getMinimumTransferSize() {
            return ((PushSourceStream) CloneableSourceStreamAdapter.this.master).getMinimumTransferSize();
        }

        public int read(byte[] buffer, int offset, int length) throws IOException {
            return CloneableSourceStreamAdapter.this.copyAndRead(buffer, offset, length);
        }

        public void setTransferHandler(SourceTransferHandler transferHandler) {
            this.handler = transferHandler;
            ((PushSourceStream) CloneableSourceStreamAdapter.this.master).setTransferHandler(this);
        }

        public void transferData(PushSourceStream stream) {
            if (this.handler != null) {
                this.handler.transferData(this);
            }
        }
    }

    class PushSourceStreamSlave extends PushStreamSlave implements PushSourceStream, Runnable {
        private byte[] buffer;
        SourceTransferHandler handler;

        PushSourceStreamSlave() {
            super();
        }

        public int getMinimumTransferSize() {
            return CloneableSourceStreamAdapter.this.master instanceof PushSourceStream ? ((PushSourceStream) CloneableSourceStreamAdapter.this.master).getMinimumTransferSize() : 0;
        }

        /* access modifiers changed from: 0000 */
        public SourceTransferHandler getTransferHandler() {
            return this.handler;
        }

        public synchronized int read(byte[] buffer, int offset, int length) throws IOException {
            int copyLength;
            if (length + offset > buffer.length) {
                throw new IOException("buffer is too small");
            }
            while (this.buffer == null && this.connected) {
                try {
                    wait(50);
                } catch (InterruptedException e) {
                    System.out.println("Exception: " + e);
                }
            }
            if (this.connected) {
                if (length > this.buffer.length) {
                    copyLength = this.buffer.length;
                } else {
                    copyLength = length;
                }
                System.arraycopy(this.buffer, 0, buffer, offset, copyLength);
                this.buffer = null;
            } else {
                throw new IOException("DataSource is not connected");
            }
            return copyLength;
        }

        public void run() {
            while (!endOfStream() && this.connected) {
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                if (this.connected && this.handler != null) {
                    this.handler.transferData(this);
                }
            }
        }

        /* access modifiers changed from: declared_synchronized */
        public synchronized void setBuffer(byte[] buffer) {
            this.buffer = buffer;
            notifyAll();
        }

        public void setTransferHandler(SourceTransferHandler transferHandler) {
            this.handler = transferHandler;
        }
    }

    CloneableSourceStreamAdapter(SourceStream master) {
        this.master = master;
        if (master instanceof PullSourceStream) {
            this.adapter = new PullSourceStreamAdapter();
        }
        if (master instanceof PullBufferStream) {
            this.adapter = new PullBufferStreamAdapter();
        }
        if (master instanceof PushSourceStream) {
            this.adapter = new PushSourceStreamAdapter();
        }
        if (master instanceof PushBufferStream) {
            this.adapter = new PushBufferStreamAdapter();
        }
    }

    /* access modifiers changed from: 0000 */
    public void copyAndRead(Buffer b) throws IOException {
        if (this.master instanceof PullBufferStream) {
            ((PullBufferStream) this.master).read(b);
        } else if (this.master instanceof PushBufferStream) {
            ((PushBufferStream) this.master).read(b);
        }
        Enumeration e = this.slaves.elements();
        while (e.hasMoreElements()) {
            ((PushBufferStreamSlave) e.nextElement()).setBuffer((Buffer) b.clone());
            Thread.yield();
        }
    }

    /* access modifiers changed from: 0000 */
    public int copyAndRead(byte[] buffer, int offset, int length) throws IOException {
        int totalRead = 0;
        if (this.master instanceof PullSourceStream) {
            totalRead = ((PullSourceStream) this.master).read(buffer, offset, length);
        } else if (this.master instanceof PushSourceStream) {
            totalRead = ((PushSourceStream) this.master).read(buffer, offset, length);
        }
        Enumeration e = this.slaves.elements();
        while (e.hasMoreElements()) {
            Object stream = e.nextElement();
            byte[] copyBuffer = new byte[totalRead];
            System.arraycopy(buffer, offset, copyBuffer, 0, totalRead);
            ((PushSourceStreamSlave) stream).setBuffer(copyBuffer);
        }
        return totalRead;
    }

    /* access modifiers changed from: 0000 */
    public SourceStream createSlave() {
        SourceStream slave = null;
        if ((this.master instanceof PullSourceStream) || (this.master instanceof PushSourceStream)) {
            slave = new PushSourceStreamSlave();
        } else if ((this.master instanceof PullBufferStream) || (this.master instanceof PushBufferStream)) {
            slave = new PushBufferStreamSlave();
        }
        this.slaves.addElement(slave);
        return slave;
    }

    /* access modifiers changed from: 0000 */
    public SourceStream getAdapter() {
        return this.adapter;
    }
}
