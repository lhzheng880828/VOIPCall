package net.sf.fmj.media;

import com.lti.utils.synchronization.CloseableThread;
import com.lti.utils.synchronization.ProducerConsumerQueue;
import com.lti.utils.synchronization.SynchronizedObjectHolder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.PushBufferStream;
import net.sf.fmj.utility.LoggerSingleton;

public class AsyncBufferTransferHandlerNotifier {
    private static final Logger logger = LoggerSingleton.logger;
    private NotifyTransferHandlerThread notifyTransferHandlerThread;
    private final PushBufferStream stream;
    private final SynchronizedObjectHolder<BufferTransferHandler> transferHandlerHolder = new SynchronizedObjectHolder();

    class NotifyTransferHandlerThread extends CloseableThread {
        private final ProducerConsumerQueue<Boolean> q = new ProducerConsumerQueue();

        public NotifyTransferHandlerThread(String threadName) {
            super(threadName);
            setDaemon(true);
        }

        public void notifyTransferHandlerAsync() throws InterruptedException {
            this.q.put(Boolean.TRUE);
        }

        public void run() {
            while (!isClosing() && this.q.get() != null) {
                try {
                    AsyncBufferTransferHandlerNotifier.this.notifyTransferHandlerSync();
                } catch (InterruptedException e) {
                    setClosed();
                    return;
                } catch (Throwable th) {
                    setClosed();
                    throw th;
                }
            }
            setClosed();
        }
    }

    public AsyncBufferTransferHandlerNotifier(PushBufferStream stream) {
        this.stream = stream;
    }

    public void dispose() {
        if (this.notifyTransferHandlerThread != null) {
            this.notifyTransferHandlerThread.close();
            try {
                this.notifyTransferHandlerThread.waitUntilClosed();
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "" + e, e);
            } finally {
                this.notifyTransferHandlerThread = null;
            }
        }
    }

    public void disposeAsync() {
        if (this.notifyTransferHandlerThread != null) {
            this.notifyTransferHandlerThread.close();
            this.notifyTransferHandlerThread = null;
        }
    }

    public void notifyTransferHandlerAsync() throws InterruptedException {
        if (this.notifyTransferHandlerThread == null) {
            this.notifyTransferHandlerThread = new NotifyTransferHandlerThread("NotifyTransferHandlerThread for " + this.stream);
            this.notifyTransferHandlerThread.start();
        }
        this.notifyTransferHandlerThread.notifyTransferHandlerAsync();
    }

    public void notifyTransferHandlerSync() {
        BufferTransferHandler handler = (BufferTransferHandler) this.transferHandlerHolder.getObject();
        if (handler != null) {
            handler.transferData(this.stream);
        }
    }

    public void setTransferHandler(BufferTransferHandler transferHandler) {
        this.transferHandlerHolder.setObject(transferHandler);
    }
}
