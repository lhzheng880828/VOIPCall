package net.sf.fmj.media.codec;

import com.lti.utils.UnsignedUtils;
import com.lti.utils.synchronization.CloseableThread;
import com.lti.utils.synchronization.ProducerConsumerQueue;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import javax.media.Buffer;

public class InputStreamReader extends InputStream {
    private final int bufferSize;
    private final ProducerConsumerQueue emptyQueue = new ProducerConsumerQueue();
    private final ProducerConsumerQueue fullQueue = new ProducerConsumerQueue();
    private final InputStream is;
    private Buffer readBuffer;
    private IOException readException;
    private ReaderThread readerThread;
    private boolean readerThreadStarted;

    private static class ReaderThread extends CloseableThread {
        private final int bufferSize;
        private final ProducerConsumerQueue emptyQueue;
        private final ProducerConsumerQueue fullQueue;
        private final InputStream is;

        public ReaderThread(ProducerConsumerQueue emptyQueue, ProducerConsumerQueue fullQueue, InputStream is, int bufferSize) {
            this.emptyQueue = emptyQueue;
            this.fullQueue = fullQueue;
            this.is = is;
            this.bufferSize = bufferSize;
        }

        /* JADX WARNING: Removed duplicated region for block: B:17:0x004e A:{ExcHandler: all (r3_0 'th' java.lang.Throwable), Splitter:B:0:0x0000} */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing block: B:17:0x004e, code skipped:
            r3 = move-exception;
     */
        /* JADX WARNING: Missing block: B:18:0x004f, code skipped:
            setClosed();
     */
        /* JADX WARNING: Missing block: B:19:0x0052, code skipped:
            throw r3;
     */
        public void run() {
            /*
            r7 = this;
        L_0x0000:
            r3 = r7.isClosing();	 Catch:{ InterruptedException -> 0x0037, IOException -> 0x0040, all -> 0x004e }
            if (r3 != 0) goto L_0x004a;
        L_0x0006:
            r3 = r7.emptyQueue;	 Catch:{ InterruptedException -> 0x0037, IOException -> 0x0040, all -> 0x004e }
            r0 = r3.get();	 Catch:{ InterruptedException -> 0x0037, IOException -> 0x0040, all -> 0x004e }
            r0 = (javax.media.Buffer) r0;	 Catch:{ InterruptedException -> 0x0037, IOException -> 0x0040, all -> 0x004e }
            r3 = 0;
            r0.setEOM(r3);	 Catch:{ InterruptedException -> 0x0037, IOException -> 0x0040, all -> 0x004e }
            r3 = 0;
            r0.setLength(r3);	 Catch:{ InterruptedException -> 0x0037, IOException -> 0x0040, all -> 0x004e }
            r3 = 0;
            r0.setOffset(r3);	 Catch:{ InterruptedException -> 0x0037, IOException -> 0x0040, all -> 0x004e }
            r4 = r7.is;	 Catch:{ InterruptedException -> 0x0037, IOException -> 0x0040, all -> 0x004e }
            r3 = r0.getData();	 Catch:{ InterruptedException -> 0x0037, IOException -> 0x0040, all -> 0x004e }
            r3 = (byte[]) r3;	 Catch:{ InterruptedException -> 0x0037, IOException -> 0x0040, all -> 0x004e }
            r3 = (byte[]) r3;	 Catch:{ InterruptedException -> 0x0037, IOException -> 0x0040, all -> 0x004e }
            r5 = 0;
            r6 = r7.bufferSize;	 Catch:{ InterruptedException -> 0x0037, IOException -> 0x0040, all -> 0x004e }
            r2 = r4.read(r3, r5, r6);	 Catch:{ InterruptedException -> 0x0037, IOException -> 0x0040, all -> 0x004e }
            if (r2 >= 0) goto L_0x003c;
        L_0x002d:
            r3 = 1;
            r0.setEOM(r3);	 Catch:{ InterruptedException -> 0x0037, IOException -> 0x0040, all -> 0x004e }
        L_0x0031:
            r3 = r7.fullQueue;	 Catch:{ InterruptedException -> 0x0037, IOException -> 0x0040, all -> 0x004e }
            r3.put(r0);	 Catch:{ InterruptedException -> 0x0037, IOException -> 0x0040, all -> 0x004e }
            goto L_0x0000;
        L_0x0037:
            r3 = move-exception;
            r7.setClosed();
        L_0x003b:
            return;
        L_0x003c:
            r0.setLength(r2);	 Catch:{ InterruptedException -> 0x0037, IOException -> 0x0040, all -> 0x004e }
            goto L_0x0031;
        L_0x0040:
            r1 = move-exception;
            r3 = r7.fullQueue;	 Catch:{ InterruptedException -> 0x0053, all -> 0x004e }
            r3.put(r1);	 Catch:{ InterruptedException -> 0x0053, all -> 0x004e }
        L_0x0046:
            r7.setClosed();
            goto L_0x003b;
        L_0x004a:
            r7.setClosed();
            goto L_0x003b;
        L_0x004e:
            r3 = move-exception;
            r7.setClosed();
            throw r3;
        L_0x0053:
            r3 = move-exception;
            goto L_0x0046;
            */
            throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.media.codec.InputStreamReader$ReaderThread.run():void");
        }
    }

    public InputStreamReader(InputStream is, int bufferSize) {
        this.is = is;
        this.bufferSize = bufferSize;
        int i = 0;
        while (i < 2) {
            Buffer b = new Buffer();
            b.setData(new byte[bufferSize]);
            try {
                this.emptyQueue.put(b);
                i++;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        this.readerThread = new ReaderThread(this.emptyQueue, this.fullQueue, is, bufferSize);
        this.readerThread.setName("ReaderThread for " + is);
        this.readerThread.setDaemon(true);
    }

    public int available() throws IOException {
        if (this.readException != null) {
            throw this.readException;
        } else if (this.readBuffer == null || this.readBuffer.getLength() <= 0) {
            return 0;
        } else {
            return this.readBuffer.getLength();
        }
    }

    public void close() throws IOException {
        super.close();
        if (this.readerThread != null) {
            this.readerThread.close();
            this.readerThread = null;
        }
    }

    public int read() throws IOException {
        byte[] ba = new byte[1];
        if (read(ba, 0, 1) == -1) {
            return -1;
        }
        return ba[0] & UnsignedUtils.MAX_UBYTE;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        try {
            if (this.readBuffer == null && this.readException == null) {
                Object o = this.fullQueue.get();
                if (o instanceof IOException) {
                    this.readException = (IOException) o;
                } else {
                    this.readBuffer = (Buffer) o;
                }
            }
            if (this.readException != null) {
                throw this.readException;
            } else if (this.readBuffer.isEOM()) {
                return -1;
            } else {
                int lenToCopy;
                byte[] readBufferData = (byte[]) this.readBuffer.getData();
                if (this.readBuffer.getLength() < len) {
                    lenToCopy = this.readBuffer.getLength();
                } else {
                    lenToCopy = len;
                }
                System.arraycopy(readBufferData, this.readBuffer.getOffset(), b, off, lenToCopy);
                this.readBuffer.setOffset(this.readBuffer.getOffset() + lenToCopy);
                this.readBuffer.setLength(this.readBuffer.getLength() - lenToCopy);
                if (this.readBuffer.getLength() != 0) {
                    return lenToCopy;
                }
                this.emptyQueue.put(this.readBuffer);
                this.readBuffer = null;
                return lenToCopy;
            }
        } catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
    }

    public void startReaderThread() {
        if (!this.readerThreadStarted) {
            this.readerThread.start();
            this.readerThreadStarted = true;
        }
    }
}
