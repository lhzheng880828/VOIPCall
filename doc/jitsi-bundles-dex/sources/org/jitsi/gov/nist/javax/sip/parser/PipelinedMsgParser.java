package org.jitsi.gov.nist.javax.sip.parser;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.jitsi.gov.nist.core.CommonLogger;
import org.jitsi.gov.nist.core.InternalErrorHandler;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.core.StackLogger;
import org.jitsi.gov.nist.javax.sip.header.ContentLength;
import org.jitsi.gov.nist.javax.sip.message.SIPMessage;
import org.jitsi.gov.nist.javax.sip.stack.BlockingQueueDispatchAuditor;
import org.jitsi.gov.nist.javax.sip.stack.QueuedMessageDispatchBase;
import org.jitsi.gov.nist.javax.sip.stack.SIPEventInterceptor;
import org.jitsi.gov.nist.javax.sip.stack.SIPTransactionStack;
import org.jitsi.javax.sip.message.Message;

public final class PipelinedMsgParser implements Runnable {
    /* access modifiers changed from: private|static */
    public static StackLogger logger = CommonLogger.getLogger(PipelinedMsgParser.class);
    private static ExecutorService postParseExecutor = null;
    public static BlockingQueue<Runnable> staticQueue;
    public static BlockingQueueDispatchAuditor staticQueueAuditor;
    private static int uid = 0;
    private int maxMessageSize;
    /* access modifiers changed from: private */
    public ConcurrentHashMap<String, CallIDOrderingStructure> messagesOrderingMap;
    private Thread mythread;
    private Pipeline rawInputStream;
    protected SIPMessageListener sipMessageListener;
    /* access modifiers changed from: private */
    public SIPTransactionStack sipStack;
    private int sizeCounter;
    private MessageParser smp;

    class CallIDOrderingStructure {
        private Queue<SIPMessage> messagesForCallID = new ConcurrentLinkedQueue();
        private Semaphore semaphore = new Semaphore(1, true);

        public Semaphore getSemaphore() {
            return this.semaphore;
        }

        public Queue<SIPMessage> getMessagesForCallID() {
            return this.messagesForCallID;
        }
    }

    public class Dispatch implements Runnable, QueuedMessageDispatchBase {
        CallIDOrderingStructure callIDOrderingStructure;
        String callId;
        long time = System.currentTimeMillis();

        public Dispatch(CallIDOrderingStructure callIDOrderingStructure, String callId) {
            this.callIDOrderingStructure = callIDOrderingStructure;
            this.callId = callId;
        }

        public void run() {
            Semaphore semaphore = this.callIDOrderingStructure.getSemaphore();
            Queue<SIPMessage> messagesForCallID = this.callIDOrderingStructure.getMessagesForCallID();
            if (PipelinedMsgParser.this.sipStack.sipEventInterceptor != null) {
                PipelinedMsgParser.this.sipStack.sipEventInterceptor.beforeMessage((Message) messagesForCallID.peek());
            }
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                PipelinedMsgParser.logger.logError("Semaphore acquisition for callId " + this.callId + " interrupted", e);
            }
            SIPMessage message = (SIPMessage) messagesForCallID.poll();
            if (PipelinedMsgParser.logger.isLoggingEnabled(32)) {
                PipelinedMsgParser.logger.logDebug("semaphore acquired for message " + message);
            }
            SIPEventInterceptor sIPEventInterceptor;
            try {
                PipelinedMsgParser.this.sipMessageListener.processMessage(message);
                if (this.callIDOrderingStructure.getMessagesForCallID().size() <= 0) {
                    PipelinedMsgParser.this.messagesOrderingMap.remove(this.callId);
                    if (PipelinedMsgParser.logger.isLoggingEnabled(32)) {
                        PipelinedMsgParser.logger.logDebug("CallIDOrderingStructure removed for message " + this.callId);
                    }
                }
                if (PipelinedMsgParser.logger.isLoggingEnabled(32)) {
                    PipelinedMsgParser.logger.logDebug("releasing semaphore for message " + message);
                }
                semaphore.release();
                if (PipelinedMsgParser.this.messagesOrderingMap.isEmpty()) {
                    synchronized (PipelinedMsgParser.this.messagesOrderingMap) {
                        PipelinedMsgParser.this.messagesOrderingMap.notify();
                    }
                }
                if (PipelinedMsgParser.this.sipStack.sipEventInterceptor != null) {
                    sIPEventInterceptor = PipelinedMsgParser.this.sipStack.sipEventInterceptor;
                    sIPEventInterceptor.afterMessage(message);
                }
            } catch (Exception e2) {
                PipelinedMsgParser.logger.logError("Error occured processing message", e2);
                if (this.callIDOrderingStructure.getMessagesForCallID().size() <= 0) {
                    PipelinedMsgParser.this.messagesOrderingMap.remove(this.callId);
                    if (PipelinedMsgParser.logger.isLoggingEnabled(32)) {
                        PipelinedMsgParser.logger.logDebug("CallIDOrderingStructure removed for message " + this.callId);
                    }
                }
                if (PipelinedMsgParser.logger.isLoggingEnabled(32)) {
                    PipelinedMsgParser.logger.logDebug("releasing semaphore for message " + message);
                }
                semaphore.release();
                if (PipelinedMsgParser.this.messagesOrderingMap.isEmpty()) {
                    synchronized (PipelinedMsgParser.this.messagesOrderingMap) {
                        PipelinedMsgParser.this.messagesOrderingMap.notify();
                    }
                }
                if (PipelinedMsgParser.this.sipStack.sipEventInterceptor != null) {
                    sIPEventInterceptor = PipelinedMsgParser.this.sipStack.sipEventInterceptor;
                }
            } catch (Throwable th) {
                if (this.callIDOrderingStructure.getMessagesForCallID().size() <= 0) {
                    PipelinedMsgParser.this.messagesOrderingMap.remove(this.callId);
                    if (PipelinedMsgParser.logger.isLoggingEnabled(32)) {
                        PipelinedMsgParser.logger.logDebug("CallIDOrderingStructure removed for message " + this.callId);
                    }
                }
                if (PipelinedMsgParser.logger.isLoggingEnabled(32)) {
                    PipelinedMsgParser.logger.logDebug("releasing semaphore for message " + message);
                }
                semaphore.release();
                if (PipelinedMsgParser.this.messagesOrderingMap.isEmpty()) {
                    synchronized (PipelinedMsgParser.this.messagesOrderingMap) {
                        PipelinedMsgParser.this.messagesOrderingMap.notify();
                    }
                }
                if (PipelinedMsgParser.this.sipStack.sipEventInterceptor != null) {
                    PipelinedMsgParser.this.sipStack.sipEventInterceptor.afterMessage(message);
                }
            }
        }

        public long getReceptionTime() {
            return this.time;
        }
    }

    public static class NamedThreadFactory implements ThreadFactory {
        static long threadNumber = 0;

        public Thread newThread(Runnable arg0) {
            Thread thread = new Thread(arg0);
            StringBuilder append = new StringBuilder().append("SIP-TCP-Core-PipelineThreadpool-");
            long j = threadNumber;
            threadNumber = 1 + j;
            thread.setName(append.append(j % 999999999).toString());
            return thread;
        }
    }

    protected PipelinedMsgParser() {
        this.smp = null;
        this.messagesOrderingMap = new ConcurrentHashMap();
    }

    private static synchronized int getNewUid() {
        int i;
        synchronized (PipelinedMsgParser.class) {
            i = uid;
            uid = i + 1;
        }
        return i;
    }

    public PipelinedMsgParser(SIPTransactionStack sipStack, SIPMessageListener sipMessageListener, Pipeline in, boolean debug, int maxMessageSize) {
        this();
        this.sipStack = sipStack;
        this.smp = sipStack.getMessageParserFactory().createMessageParser(sipStack);
        this.sipMessageListener = sipMessageListener;
        this.rawInputStream = in;
        this.maxMessageSize = maxMessageSize;
        this.mythread = new Thread(this);
        this.mythread.setName("PipelineThread-" + getNewUid());
    }

    public PipelinedMsgParser(SIPTransactionStack sipStack, SIPMessageListener mhandler, Pipeline in, int maxMsgSize) {
        this(sipStack, mhandler, in, false, maxMsgSize);
    }

    public PipelinedMsgParser(SIPTransactionStack sipStack, Pipeline in) {
        this(sipStack, null, in, false, 0);
    }

    public void processInput() {
        this.mythread.start();
    }

    /* access modifiers changed from: protected */
    public Object clone() {
        PipelinedMsgParser p = new PipelinedMsgParser();
        p.rawInputStream = this.rawInputStream;
        p.sipMessageListener = this.sipMessageListener;
        new Thread(p).setName("PipelineThread");
        return p;
    }

    public void setMessageListener(SIPMessageListener mlistener) {
        this.sipMessageListener = mlistener;
    }

    private String readLine(InputStream inputStream) throws IOException {
        int counter = 0;
        int bufferSize = 1024;
        byte[] lineBuffer = new byte[bufferSize];
        while (true) {
            int i = inputStream.read();
            if (i == -1) {
                throw new IOException("End of stream");
            }
            char ch = (char) (i & 255);
            if (this.maxMessageSize > 0) {
                this.sizeCounter--;
                if (this.sizeCounter <= 0) {
                    throw new IOException("Max size exceeded!");
                }
            }
            if (ch != 13) {
                int counter2 = counter + 1;
                lineBuffer[counter] = (byte) (i & 255);
                counter = counter2;
            }
            if (ch == 10) {
                return new String(lineBuffer, 0, counter, "UTF-8");
            }
            if (counter == bufferSize) {
                byte[] tempBuffer = new byte[(bufferSize + 1024)];
                System.arraycopy(lineBuffer, 0, tempBuffer, 0, bufferSize);
                bufferSize += 1024;
                lineBuffer = tempBuffer;
            }
        }
    }

    public void run() {
        Exception e;
        Pipeline inputStream = this.rawInputStream;
        StackLogger stackLogger = logger;
        while (true) {
            try {
                String line1;
                String line2;
                this.sizeCounter = this.maxMessageSize;
                StringBuilder inputBuffer = new StringBuilder();
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("Starting to parse.");
                }
                while (true) {
                    line1 = readLine(inputStream);
                    if (!line1.equals(Separators.RETURN)) {
                        break;
                    } else if (logger.isLoggingEnabled(32)) {
                        logger.logDebug("Discarding blank line");
                    }
                }
                inputBuffer.append(line1);
                this.rawInputStream.startTimer();
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("Reading Input stream.");
                }
                do {
                    try {
                        line2 = readLine(inputStream);
                        inputBuffer.append(line2);
                    } catch (IOException e2) {
                        if (postParseExecutor != null) {
                            synchronized (this.messagesOrderingMap) {
                                try {
                                    this.messagesOrderingMap.wait();
                                } catch (InterruptedException e3) {
                                }
                            }
                        }
                        this.rawInputStream.stopTimer();
                        if (logger.isLoggingEnabled(32)) {
                            logger.logStackTrace(32);
                        }
                        try {
                            cleanMessageOrderingMap();
                            try {
                                inputStream.close();
                                return;
                            } catch (IOException e4) {
                                e = e4;
                            }
                        } catch (IOException e5) {
                            e = e5;
                            InternalErrorHandler.handleException(e);
                            return;
                        }
                    }
                } while (!line2.trim().equals(""));
                this.rawInputStream.stopTimer();
                inputBuffer.append(line2);
                try {
                    if (stackLogger.isLoggingEnabled(32)) {
                        stackLogger.logDebug("About to parse : " + inputBuffer.toString());
                    }
                    SIPMessage sipMessage = this.smp.parseSIPMessage(inputBuffer.toString().getBytes(), false, false, this.sipMessageListener);
                    if (sipMessage == null) {
                        this.rawInputStream.stopTimer();
                    } else {
                        int contentLength;
                        if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("Completed parsing message");
                        }
                        ContentLength cl = (ContentLength) sipMessage.getContentLength();
                        if (cl != null) {
                            contentLength = cl.getContentLength();
                        } else {
                            contentLength = 0;
                        }
                        if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("Content length = " + contentLength);
                        }
                        if (contentLength == 0) {
                            sipMessage.removeContent();
                        } else if (this.maxMessageSize == 0 || contentLength < this.sizeCounter) {
                            byte[] message_body = new byte[contentLength];
                            int nread = 0;
                            while (nread < contentLength) {
                                this.rawInputStream.startTimer();
                                try {
                                    int readlength = inputStream.read(message_body, nread, contentLength - nread);
                                    if (readlength <= 0) {
                                        this.rawInputStream.stopTimer();
                                        break;
                                    } else {
                                        nread += readlength;
                                        this.rawInputStream.stopTimer();
                                    }
                                } catch (IOException ex) {
                                    stackLogger.logError("Exception Reading Content", ex);
                                    this.rawInputStream.stopTimer();
                                } catch (Throwable th) {
                                    this.rawInputStream.stopTimer();
                                }
                            }
                            sipMessage.setMessageContent(message_body);
                        }
                        if (this.sipMessageListener != null) {
                            try {
                                if (postParseExecutor == null) {
                                    if (this.sipStack.sipEventInterceptor != null) {
                                        this.sipStack.sipEventInterceptor.beforeMessage(sipMessage);
                                    }
                                    this.sipMessageListener.processMessage(sipMessage);
                                    if (this.sipStack.sipEventInterceptor != null) {
                                        this.sipStack.sipEventInterceptor.afterMessage(sipMessage);
                                    }
                                } else {
                                    String callId = sipMessage.getCallId().getCallId();
                                    CallIDOrderingStructure orderingStructure = (CallIDOrderingStructure) this.messagesOrderingMap.get(callId);
                                    if (orderingStructure == null) {
                                        CallIDOrderingStructure newCallIDOrderingStructure = new CallIDOrderingStructure();
                                        orderingStructure = (CallIDOrderingStructure) this.messagesOrderingMap.putIfAbsent(callId, newCallIDOrderingStructure);
                                        if (orderingStructure == null) {
                                            orderingStructure = newCallIDOrderingStructure;
                                            if (stackLogger.isLoggingEnabled(32)) {
                                                stackLogger.logDebug("new CallIDOrderingStructure added for message " + sipMessage);
                                            }
                                        }
                                    }
                                    CallIDOrderingStructure callIDOrderingStructure = orderingStructure;
                                    callIDOrderingStructure.getMessagesForCallID().offer(sipMessage);
                                    postParseExecutor.execute(new Dispatch(callIDOrderingStructure, callId));
                                }
                            } catch (Exception e6) {
                                try {
                                    cleanMessageOrderingMap();
                                    try {
                                        inputStream.close();
                                        return;
                                    } catch (IOException e7) {
                                        e = e7;
                                    }
                                } catch (IOException e8) {
                                    e = e8;
                                    InternalErrorHandler.handleException(e);
                                    return;
                                }
                            }
                        }
                    }
                } catch (ParseException ex2) {
                    stackLogger.logError("Detected a parse error", ex2);
                }
            } catch (IOException e9) {
                if (postParseExecutor != null) {
                    synchronized (this.messagesOrderingMap) {
                        try {
                            this.messagesOrderingMap.wait();
                        } catch (InterruptedException e10) {
                        }
                    }
                }
                if (logger.isLoggingEnabled(32)) {
                    logger.logStackTrace(32);
                }
                this.rawInputStream.stopTimer();
                try {
                    cleanMessageOrderingMap();
                    inputStream.close();
                    return;
                } catch (IOException e11) {
                    e = e11;
                }
            } catch (Throwable th2) {
                try {
                    cleanMessageOrderingMap();
                    inputStream.close();
                } catch (IOException e12) {
                    InternalErrorHandler.handleException(e12);
                }
            }
        }
    }

    public static void setPostParseExcutorSize(int threads, int queueTimeout) {
        if (postParseExecutor != null) {
            postParseExecutor.shutdownNow();
        }
        if (staticQueueAuditor != null) {
            try {
                staticQueueAuditor.stop();
            } catch (Exception e) {
            }
        }
        if (threads <= 0) {
            postParseExecutor = null;
            return;
        }
        staticQueue = new LinkedBlockingQueue();
        postParseExecutor = new ThreadPoolExecutor(threads, threads, 0, TimeUnit.SECONDS, staticQueue, new NamedThreadFactory());
        staticQueueAuditor = new BlockingQueueDispatchAuditor(staticQueue);
        staticQueueAuditor.setTimeout(queueTimeout);
        staticQueueAuditor.start(2000);
    }

    public void close() {
        try {
            this.rawInputStream.close();
        } catch (IOException e) {
        }
        if (postParseExecutor != null) {
            postParseExecutor.shutdown();
            postParseExecutor = null;
        }
        cleanMessageOrderingMap();
    }

    private void cleanMessageOrderingMap() {
        for (CallIDOrderingStructure callIDOrderingStructure : this.messagesOrderingMap.values()) {
            callIDOrderingStructure.getSemaphore().release();
            callIDOrderingStructure.getMessagesForCallID().clear();
        }
        this.messagesOrderingMap.clear();
    }
}
