package org.jitsi.impl.neomedia.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;
import javax.media.protocol.PushBufferStream;
import net.sf.fmj.media.util.MediaThread;
import org.jitsi.impl.neomedia.jmfext.media.renderer.AbstractRenderer;
import org.jitsi.util.Logger;

public class PushBufferDataSourceAdapter extends PushBufferDataSourceDelegate<PullBufferDataSource> {
    private static final boolean STRICT_STOP = false;
    private static final Logger logger = Logger.getLogger(PushBufferDataSourceAdapter.class);
    private boolean started = false;
    private final List<PushBufferStreamAdapter> streams = new ArrayList();

    private static class PushBufferStreamAdapter implements PushBufferStream {
        private final Buffer buffer = new Buffer();
        private boolean bufferIsWritten = false;
        /* access modifiers changed from: private */
        public boolean started = false;
        public final PullBufferStream stream;
        private IOException streamReadException;
        /* access modifiers changed from: private */
        public Thread streamReadThread;
        /* access modifiers changed from: private|final */
        public final Object streamReadThreadSyncRoot = new Object();
        private BufferTransferHandler transferHandler;

        public PushBufferStreamAdapter(PullBufferStream stream) {
            if (stream == null) {
                throw new NullPointerException("stream");
            }
            this.stream = stream;
        }

        /* access modifiers changed from: 0000 */
        public void close() {
            stop();
        }

        public boolean endOfStream() {
            return this.stream.endOfStream();
        }

        public ContentDescriptor getContentDescriptor() {
            return this.stream.getContentDescriptor();
        }

        public long getContentLength() {
            return this.stream.getContentLength();
        }

        public Object getControl(String controlType) {
            return this.stream.getControl(controlType);
        }

        public Object[] getControls() {
            return this.stream.getControls();
        }

        public Format getFormat() {
            return this.stream.getFormat();
        }

        public void read(Buffer buffer) throws IOException {
            synchronized (this.buffer) {
                if (this.streamReadException != null) {
                    IOException ie = new IOException();
                    ie.initCause(this.streamReadException);
                    this.streamReadException = null;
                    throw ie;
                }
                if (this.bufferIsWritten) {
                    buffer.copy(this.buffer);
                    this.bufferIsWritten = false;
                } else {
                    buffer.setLength(0);
                }
            }
        }

        /* access modifiers changed from: private */
        public void runInStreamReadThread() {
            boolean bufferIsWritten;
            boolean yield = true;
            synchronized (this.buffer) {
                try {
                    boolean z;
                    this.stream.read(this.buffer);
                    if (this.buffer.isDiscard()) {
                        z = false;
                    } else {
                        z = true;
                    }
                    this.bufferIsWritten = z;
                    this.streamReadException = null;
                } catch (IOException ie) {
                    this.bufferIsWritten = false;
                    this.streamReadException = ie;
                }
                bufferIsWritten = this.bufferIsWritten;
                if (bufferIsWritten || this.streamReadException == null) {
                    yield = false;
                }
            }
            if (bufferIsWritten) {
                BufferTransferHandler transferHandler = this.transferHandler;
                if (transferHandler != null) {
                    transferHandler.transferData(this);
                }
            } else if (yield) {
                Thread.yield();
            }
        }

        public void setTransferHandler(BufferTransferHandler transferHandler) {
            if (this.transferHandler != transferHandler) {
                this.transferHandler = transferHandler;
            }
        }

        /* access modifiers changed from: 0000 */
        public void start() {
            synchronized (this.streamReadThreadSyncRoot) {
                this.started = true;
                if (this.streamReadThread == null) {
                    this.streamReadThread = new Thread(getClass().getName() + ".streamReadThread") {
                        /* JADX WARNING: Missing block: B:37:?, code skipped:
            org.jitsi.impl.neomedia.protocol.PushBufferDataSourceAdapter.PushBufferStreamAdapter.access$400(r4.this$0);
     */
                        public void run() {
                            /*
                            r4 = this;
                            r0 = org.jitsi.impl.neomedia.protocol.PushBufferDataSourceAdapter.PushBufferStreamAdapter.this;	 Catch:{ all -> 0x004c }
                            r0 = r0.stream;	 Catch:{ all -> 0x004c }
                            org.jitsi.impl.neomedia.protocol.PushBufferDataSourceAdapter.setStreamReadThreadPriority(r0);	 Catch:{ all -> 0x004c }
                        L_0x0007:
                            r0 = org.jitsi.impl.neomedia.protocol.PushBufferDataSourceAdapter.PushBufferStreamAdapter.this;	 Catch:{ all -> 0x004c }
                            r1 = r0.streamReadThreadSyncRoot;	 Catch:{ all -> 0x004c }
                            monitor-enter(r1);	 Catch:{ all -> 0x004c }
                            r0 = org.jitsi.impl.neomedia.protocol.PushBufferDataSourceAdapter.PushBufferStreamAdapter.this;	 Catch:{ all -> 0x0049 }
                            r0 = r0.started;	 Catch:{ all -> 0x0049 }
                            if (r0 != 0) goto L_0x003b;
                        L_0x0016:
                            monitor-exit(r1);	 Catch:{ all -> 0x0049 }
                        L_0x0017:
                            r0 = org.jitsi.impl.neomedia.protocol.PushBufferDataSourceAdapter.PushBufferStreamAdapter.this;
                            r1 = r0.streamReadThreadSyncRoot;
                            monitor-enter(r1);
                            r0 = org.jitsi.impl.neomedia.protocol.PushBufferDataSourceAdapter.PushBufferStreamAdapter.this;	 Catch:{ all -> 0x0078 }
                            r0 = r0.streamReadThread;	 Catch:{ all -> 0x0078 }
                            r2 = java.lang.Thread.currentThread();	 Catch:{ all -> 0x0078 }
                            if (r0 != r2) goto L_0x0039;
                        L_0x002a:
                            r0 = org.jitsi.impl.neomedia.protocol.PushBufferDataSourceAdapter.PushBufferStreamAdapter.this;	 Catch:{ all -> 0x0078 }
                            r2 = 0;
                            r0.streamReadThread = r2;	 Catch:{ all -> 0x0078 }
                            r0 = org.jitsi.impl.neomedia.protocol.PushBufferDataSourceAdapter.PushBufferStreamAdapter.this;	 Catch:{ all -> 0x0078 }
                            r0 = r0.streamReadThreadSyncRoot;	 Catch:{ all -> 0x0078 }
                            r0.notifyAll();	 Catch:{ all -> 0x0078 }
                        L_0x0039:
                            monitor-exit(r1);	 Catch:{ all -> 0x0078 }
                            return;
                        L_0x003b:
                            r0 = org.jitsi.impl.neomedia.protocol.PushBufferDataSourceAdapter.PushBufferStreamAdapter.this;	 Catch:{ all -> 0x0049 }
                            r0 = r0.streamReadThread;	 Catch:{ all -> 0x0049 }
                            r2 = java.lang.Thread.currentThread();	 Catch:{ all -> 0x0049 }
                            if (r0 == r2) goto L_0x0071;
                        L_0x0047:
                            monitor-exit(r1);	 Catch:{ all -> 0x0049 }
                            goto L_0x0017;
                        L_0x0049:
                            r0 = move-exception;
                            monitor-exit(r1);	 Catch:{ all -> 0x0049 }
                            throw r0;	 Catch:{ all -> 0x004c }
                        L_0x004c:
                            r0 = move-exception;
                            r1 = org.jitsi.impl.neomedia.protocol.PushBufferDataSourceAdapter.PushBufferStreamAdapter.this;
                            r1 = r1.streamReadThreadSyncRoot;
                            monitor-enter(r1);
                            r2 = org.jitsi.impl.neomedia.protocol.PushBufferDataSourceAdapter.PushBufferStreamAdapter.this;	 Catch:{ all -> 0x007b }
                            r2 = r2.streamReadThread;	 Catch:{ all -> 0x007b }
                            r3 = java.lang.Thread.currentThread();	 Catch:{ all -> 0x007b }
                            if (r2 != r3) goto L_0x006f;
                        L_0x0060:
                            r2 = org.jitsi.impl.neomedia.protocol.PushBufferDataSourceAdapter.PushBufferStreamAdapter.this;	 Catch:{ all -> 0x007b }
                            r3 = 0;
                            r2.streamReadThread = r3;	 Catch:{ all -> 0x007b }
                            r2 = org.jitsi.impl.neomedia.protocol.PushBufferDataSourceAdapter.PushBufferStreamAdapter.this;	 Catch:{ all -> 0x007b }
                            r2 = r2.streamReadThreadSyncRoot;	 Catch:{ all -> 0x007b }
                            r2.notifyAll();	 Catch:{ all -> 0x007b }
                        L_0x006f:
                            monitor-exit(r1);	 Catch:{ all -> 0x007b }
                            throw r0;
                        L_0x0071:
                            monitor-exit(r1);	 Catch:{ all -> 0x0049 }
                            r0 = org.jitsi.impl.neomedia.protocol.PushBufferDataSourceAdapter.PushBufferStreamAdapter.this;	 Catch:{ all -> 0x004c }
                            r0.runInStreamReadThread();	 Catch:{ all -> 0x004c }
                            goto L_0x0007;
                        L_0x0078:
                            r0 = move-exception;
                            monitor-exit(r1);	 Catch:{ all -> 0x0078 }
                            throw r0;
                        L_0x007b:
                            r0 = move-exception;
                            monitor-exit(r1);	 Catch:{ all -> 0x007b }
                            throw r0;
                            */
                            throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.protocol.PushBufferDataSourceAdapter$PushBufferStreamAdapter$AnonymousClass1.run():void");
                        }
                    };
                    this.streamReadThread.setDaemon(true);
                    this.streamReadThread.start();
                }
            }
        }

        /* access modifiers changed from: 0000 */
        public void stop() {
            synchronized (this.streamReadThreadSyncRoot) {
                this.started = false;
                this.streamReadThread = null;
            }
        }
    }

    public PushBufferDataSourceAdapter(PullBufferDataSource dataSource) {
        super(dataSource);
    }

    public void disconnect() {
        synchronized (this.streams) {
            Iterator<PushBufferStreamAdapter> streamIter = this.streams.iterator();
            while (streamIter.hasNext()) {
                PushBufferStreamAdapter stream = (PushBufferStreamAdapter) streamIter.next();
                streamIter.remove();
                stream.close();
            }
        }
        super.disconnect();
    }

    public PushBufferStream[] getStreams() {
        PushBufferStream[] pushBufferStreamArr;
        synchronized (this.streams) {
            int dataSourceStreamCount;
            int dataSourceStreamIndex;
            PullBufferStream[] dataSourceStreams = ((PullBufferDataSource) this.dataSource).getStreams();
            if (dataSourceStreams != null) {
                dataSourceStreams = (PullBufferStream[]) dataSourceStreams.clone();
                dataSourceStreamCount = dataSourceStreams.length;
            } else {
                dataSourceStreamCount = 0;
            }
            Iterator<PushBufferStreamAdapter> streamIter = this.streams.iterator();
            while (streamIter.hasNext()) {
                PushBufferStreamAdapter streamAdapter = (PushBufferStreamAdapter) streamIter.next();
                PullBufferStream stream = streamAdapter.stream;
                boolean removeStream = true;
                for (dataSourceStreamIndex = 0; dataSourceStreamIndex < dataSourceStreamCount; dataSourceStreamIndex++) {
                    if (stream == dataSourceStreams[dataSourceStreamIndex]) {
                        removeStream = false;
                        dataSourceStreams[dataSourceStreamIndex] = null;
                        break;
                    }
                }
                if (removeStream) {
                    streamIter.remove();
                    streamAdapter.close();
                }
            }
            for (dataSourceStreamIndex = 0; dataSourceStreamIndex < dataSourceStreamCount; dataSourceStreamIndex++) {
                PullBufferStream dataSourceStream = dataSourceStreams[dataSourceStreamIndex];
                if (dataSourceStream != null) {
                    PushBufferStreamAdapter stream2 = new PushBufferStreamAdapter(dataSourceStream);
                    this.streams.add(stream2);
                    if (this.started) {
                        stream2.start();
                    }
                }
            }
            pushBufferStreamArr = (PushBufferStream[]) this.streams.toArray(EMPTY_STREAMS);
        }
        return pushBufferStreamArr;
    }

    /* access modifiers changed from: private|static */
    public static void setStreamReadThreadPriority(PullBufferStream stream) {
        try {
            int threadPriority;
            Format format = stream.getFormat();
            if (format instanceof AudioFormat) {
                threadPriority = MediaThread.getAudioPriority();
            } else if (format instanceof VideoFormat) {
                threadPriority = MediaThread.getVideoPriority();
            } else {
                return;
            }
            AbstractRenderer.useThreadPriority(threadPriority);
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                ThreadDeath t2 = (ThreadDeath) t;
            } else {
                logger.warn("Failed to set the priority of streamReadThread");
            }
        }
    }

    public void start() throws IOException {
        super.start();
        synchronized (this.streams) {
            this.started = true;
            for (PushBufferStreamAdapter stream : this.streams) {
                stream.start();
            }
        }
    }

    public void stop() throws IOException {
        synchronized (this.streams) {
            this.started = false;
            for (PushBufferStreamAdapter stream : this.streams) {
                stream.stop();
            }
        }
        super.stop();
    }
}
