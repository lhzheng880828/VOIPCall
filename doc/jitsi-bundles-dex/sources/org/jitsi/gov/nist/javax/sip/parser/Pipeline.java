package org.jitsi.gov.nist.javax.sip.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import org.jitsi.gov.nist.core.InternalErrorHandler;
import org.jitsi.gov.nist.javax.sip.stack.SIPStackTimerTask;
import org.jitsi.gov.nist.javax.sip.stack.timers.SipTimer;

public class Pipeline extends InputStream {
    private LinkedList buffList = new LinkedList();
    private Buffer currentBuffer;
    private boolean isClosed;
    private SIPStackTimerTask myTimerTask;
    private InputStream pipe;
    private int readTimeout;
    private SipTimer timer;

    class Buffer {
        byte[] bytes;
        int length;
        int ptr = 0;

        public Buffer(byte[] bytes, int length) {
            this.length = length;
            this.bytes = bytes;
        }

        public int getNextByte() {
            byte[] bArr = this.bytes;
            int i = this.ptr;
            this.ptr = i + 1;
            return bArr[i] & 255;
        }
    }

    class MyTimer extends SIPStackTimerTask {
        private boolean isCancelled;
        Pipeline pipeline;

        protected MyTimer(Pipeline pipeline) {
            this.pipeline = pipeline;
        }

        public void runTask() {
            if (this.isCancelled) {
                this.pipeline = null;
                return;
            }
            try {
                this.pipeline.close();
            } catch (IOException ex) {
                InternalErrorHandler.handleException(ex);
            }
        }

        public void cleanUpBeforeCancel() {
            this.isCancelled = true;
            this.pipeline = null;
            super.cleanUpBeforeCancel();
        }
    }

    public void startTimer() {
        if (this.readTimeout != -1) {
            this.myTimerTask = new MyTimer(this);
            this.timer.schedule(this.myTimerTask, (long) this.readTimeout);
        }
    }

    public void stopTimer() {
        if (this.readTimeout != -1 && this.myTimerTask != null) {
            this.timer.cancel(this.myTimerTask);
        }
    }

    public Pipeline(InputStream pipe, int readTimeout, SipTimer timer) {
        this.timer = timer;
        this.pipe = pipe;
        this.readTimeout = readTimeout;
    }

    public void write(byte[] bytes, int start, int length) throws IOException {
        if (this.isClosed) {
            throw new IOException("Closed!!");
        }
        Buffer buff = new Buffer(bytes, length);
        buff.ptr = start;
        synchronized (this.buffList) {
            this.buffList.add(buff);
            this.buffList.notifyAll();
        }
    }

    public void write(byte[] bytes) throws IOException {
        if (this.isClosed) {
            throw new IOException("Closed!!");
        }
        Buffer buff = new Buffer(bytes, bytes.length);
        synchronized (this.buffList) {
            this.buffList.add(buff);
            this.buffList.notifyAll();
        }
    }

    public void close() throws IOException {
        this.isClosed = true;
        synchronized (this.buffList) {
            this.buffList.notifyAll();
        }
        this.pipe.close();
    }

    public int read() throws IOException {
        int retval = -1;
        synchronized (this.buffList) {
            if (this.currentBuffer != null && this.currentBuffer.ptr < this.currentBuffer.length) {
                retval = this.currentBuffer.getNextByte();
                if (this.currentBuffer.ptr == this.currentBuffer.length) {
                    this.currentBuffer = null;
                }
            } else if (this.isClosed && this.buffList.isEmpty()) {
            } else {
                while (this.buffList.isEmpty()) {
                    try {
                        this.buffList.wait();
                        if (this.buffList.isEmpty() && this.isClosed) {
                            break;
                        }
                    } catch (InterruptedException ex) {
                        throw new IOException(ex.getMessage());
                    } catch (NoSuchElementException ex2) {
                        ex2.printStackTrace();
                        throw new IOException(ex2.getMessage());
                    }
                }
                this.currentBuffer = (Buffer) this.buffList.removeFirst();
                retval = this.currentBuffer.getNextByte();
                if (this.currentBuffer.ptr == this.currentBuffer.length) {
                    this.currentBuffer = null;
                }
            }
        }
        return retval;
    }
}
