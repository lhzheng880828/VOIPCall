package org.jitsi.impl.neomedia;

import com.lti.utils.UnsignedUtils;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.media.Format;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushSourceStream;
import javax.media.protocol.SourceTransferHandler;
import javax.media.rtp.GlobalReceptionStats;
import javax.media.rtp.GlobalTransmissionStats;
import javax.media.rtp.OutputDataStream;
import javax.media.rtp.Participant;
import javax.media.rtp.RTPConnector;
import javax.media.rtp.RTPManager;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.RemoteListener;
import javax.media.rtp.SendStream;
import javax.media.rtp.SendStreamListener;
import javax.media.rtp.SessionListener;
import javax.media.rtp.TransmissionStats;
import javax.media.rtp.event.ReceiveStreamEvent;
import javax.media.rtp.rtcp.SenderReport;
import javax.media.rtp.rtcp.SourceDescription;
import net.sf.fmj.media.rtp.RTCPPacket;
import net.sf.fmj.media.rtp.RTPSessionMgr;
import org.jitsi.impl.neomedia.protocol.FakePushBufferDataSource;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.neomedia.RTPTranslator;
import org.jitsi.service.neomedia.event.RTCPFeedbackEvent;
import org.jitsi.util.Logger;

public class RTPTranslatorImpl implements ReceiveStreamListener, RTPTranslator {
    private static final boolean CREATE_FAKE_SEND_STREAM_IF_NECESSARY = false;
    /* access modifiers changed from: private|static|final */
    public static final int[] EMPTY_INT_ARRAY = new int[0];
    /* access modifiers changed from: private|static|final */
    public static final String REMOVE_RTP_HEADER_EXTENSIONS_PROPERTY_NAME = (RTPTranslatorImpl.class.getName() + ".removeRTPHeaderExtensions");
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(RTPTranslatorImpl.class);
    private RTPConnectorImpl connector;
    private SendStream fakeSendStream;
    private final RTPManager manager = RTPManager.newInstance();
    private final List<SendStreamDesc> sendStreams = new LinkedList();
    private final List<StreamRTPManagerDesc> streamRTPManagers = new ArrayList();

    private static class OutputDataStreamDesc {
        public RTPConnectorDesc connectorDesc;
        public OutputDataStream stream;

        public OutputDataStreamDesc(RTPConnectorDesc connectorDesc, OutputDataStream stream) {
            this.connectorDesc = connectorDesc;
            this.stream = stream;
        }
    }

    private static class OutputDataStreamImpl implements OutputDataStream, Runnable {
        private static final int WRITE_QUEUE_CAPACITY = 256;
        private boolean closed;
        private final boolean data;
        private final boolean removeRTPHeaderExtensions;
        private final List<OutputDataStreamDesc> streams = new ArrayList();
        private final RTPTranslatorBuffer[] writeQueue = new RTPTranslatorBuffer[256];
        private int writeQueueHead;
        private int writeQueueLength;
        private Thread writeThread;

        public OutputDataStreamImpl(boolean data) {
            this.data = data;
            ConfigurationService cfg = LibJitsi.getConfigurationService();
            boolean removeRTPHeaderExtensions = false;
            if (cfg != null) {
                removeRTPHeaderExtensions = cfg.getBoolean(RTPTranslatorImpl.REMOVE_RTP_HEADER_EXTENSIONS_PROPERTY_NAME, false);
            }
            this.removeRTPHeaderExtensions = removeRTPHeaderExtensions;
        }

        public synchronized void addStream(RTPConnectorDesc connectorDesc, OutputDataStream stream) {
            for (OutputDataStreamDesc streamDesc : this.streams) {
                if (streamDesc.connectorDesc == connectorDesc && streamDesc.stream == stream) {
                    break;
                }
            }
            this.streams.add(new OutputDataStreamDesc(connectorDesc, stream));
        }

        public synchronized void close() {
            this.closed = true;
            this.writeThread = null;
            notify();
        }

        private synchronized void createWriteThread() {
            this.writeThread = new Thread(this, getClass().getName());
            this.writeThread.setDaemon(true);
            this.writeThread.start();
        }

        private synchronized int doWrite(byte[] buffer, int offset, int length, Format format, StreamRTPManagerDesc exclusion) {
            int written;
            boolean removeRTPHeaderExtensions = this.removeRTPHeaderExtensions;
            written = 0;
            int streamCount = this.streams.size();
            for (int streamIndex = 0; streamIndex < streamCount; streamIndex++) {
                OutputDataStreamDesc streamDesc = (OutputDataStreamDesc) this.streams.get(streamIndex);
                StreamRTPManagerDesc streamRTPManagerDesc = streamDesc.connectorDesc.streamRTPManagerDesc;
                if (streamRTPManagerDesc != exclusion) {
                    boolean write;
                    if (this.data) {
                        if (removeRTPHeaderExtensions) {
                            removeRTPHeaderExtensions = false;
                            length = removeRTPHeaderExtensions(buffer, offset, length);
                        }
                        write = willWriteData(streamRTPManagerDesc, buffer, offset, length, format, exclusion);
                    } else {
                        write = willWriteControl(streamRTPManagerDesc, buffer, offset, length, format, exclusion);
                    }
                    if (write) {
                        int streamWritten = streamDesc.stream.write(buffer, offset, length);
                        if (written < streamWritten) {
                            written = streamWritten;
                        }
                    }
                }
            }
            return written;
        }

        private int removeRTPHeaderExtensions(byte[] buf, int off, int len) {
            if (len < 12) {
                return len;
            }
            byte b0 = buf[off];
            if (((b0 & 192) >>> 6) != 2) {
                return len;
            }
            if (!((b0 & 16) == 16)) {
                return len;
            }
            int xBegin = (off + 12) + ((b0 & 15) * 4);
            int end = off + len;
            if (xBegin + 4 >= end) {
                return len;
            }
            int xLen = 4 + (RTPTranslatorImpl.readUnsignedShort(buf, xBegin + 2) * 4);
            int xEnd = xBegin + xLen;
            if (xEnd > end) {
                return len;
            }
            int dst = xBegin;
            int src = xEnd;
            while (src < end) {
                int dst2 = dst + 1;
                int src2 = src + 1;
                buf[dst] = buf[src];
                dst = dst2;
                src = src2;
            }
            len -= xLen;
            buf[off] = (byte) (b0 & 239);
            return len;
        }

        public synchronized void removeStreams(RTPConnectorDesc connectorDesc) {
            Iterator<OutputDataStreamDesc> streamIter = this.streams.iterator();
            while (streamIter.hasNext()) {
                if (((OutputDataStreamDesc) streamIter.next()).connectorDesc == connectorDesc) {
                    streamIter.remove();
                }
            }
        }

        /* JADX WARNING: Missing block: B:68:?, code skipped:
            doWrite(r1, 0, r3, r4, r5);
     */
        /* JADX WARNING: Missing block: B:70:?, code skipped:
            monitor-enter(r12);
     */
        /* JADX WARNING: Missing block: B:72:?, code skipped:
            r9 = r12.writeQueue[r10];
     */
        /* JADX WARNING: Missing block: B:73:0x00bb, code skipped:
            if (r9 == null) goto L_0x00c3;
     */
        /* JADX WARNING: Missing block: B:75:0x00bf, code skipped:
            if (r9.data != null) goto L_0x00c3;
     */
        /* JADX WARNING: Missing block: B:76:0x00c1, code skipped:
            r9.data = r1;
     */
        /* JADX WARNING: Missing block: B:77:0x00c3, code skipped:
            monitor-exit(r12);
     */
        public void run() {
            /*
            r12 = this;
        L_0x0000:
            monitor-enter(r12);	 Catch:{ Throwable -> 0x004a }
            r0 = r12.closed;	 Catch:{ all -> 0x0047 }
            if (r0 != 0) goto L_0x0011;
        L_0x0005:
            r0 = java.lang.Thread.currentThread();	 Catch:{ all -> 0x0047 }
            r2 = r12.writeThread;	 Catch:{ all -> 0x0047 }
            r0 = r0.equals(r2);	 Catch:{ all -> 0x0047 }
            if (r0 != 0) goto L_0x0033;
        L_0x0011:
            monitor-exit(r12);	 Catch:{ all -> 0x0047 }
            monitor-enter(r12);
            r0 = java.lang.Thread.currentThread();	 Catch:{ all -> 0x00dc }
            r2 = r12.writeThread;	 Catch:{ all -> 0x00dc }
            r0 = r0.equals(r2);	 Catch:{ all -> 0x00dc }
            if (r0 == 0) goto L_0x0022;
        L_0x001f:
            r0 = 0;
            r12.writeThread = r0;	 Catch:{ all -> 0x00dc }
        L_0x0022:
            r0 = r12.closed;	 Catch:{ all -> 0x00dc }
            if (r0 != 0) goto L_0x0031;
        L_0x0026:
            r0 = r12.writeThread;	 Catch:{ all -> 0x00dc }
            if (r0 != 0) goto L_0x0031;
        L_0x002a:
            r0 = r12.writeQueueLength;	 Catch:{ all -> 0x00dc }
            if (r0 <= 0) goto L_0x0031;
        L_0x002e:
            r12.createWriteThread();	 Catch:{ all -> 0x00dc }
        L_0x0031:
            monitor-exit(r12);	 Catch:{ all -> 0x00dc }
        L_0x0032:
            return;
        L_0x0033:
            r0 = r12.writeQueueLength;	 Catch:{ all -> 0x0047 }
            r2 = 1;
            if (r0 >= r2) goto L_0x0080;
        L_0x0038:
            r7 = 0;
            r12.wait();	 Catch:{ InterruptedException -> 0x007d }
        L_0x003c:
            if (r7 == 0) goto L_0x0045;
        L_0x003e:
            r0 = java.lang.Thread.currentThread();	 Catch:{ all -> 0x0047 }
            r0.interrupt();	 Catch:{ all -> 0x0047 }
        L_0x0045:
            monitor-exit(r12);	 Catch:{ all -> 0x0047 }
            goto L_0x0000;
        L_0x0047:
            r0 = move-exception;
            monitor-exit(r12);	 Catch:{ all -> 0x0047 }
            throw r0;	 Catch:{ Throwable -> 0x004a }
        L_0x004a:
            r8 = move-exception;
            r0 = org.jitsi.impl.neomedia.RTPTranslatorImpl.logger;	 Catch:{ all -> 0x005b }
            r2 = "Failed to translate RTP packet";
            r0.error(r2, r8);	 Catch:{ all -> 0x005b }
            r0 = r8 instanceof java.lang.ThreadDeath;	 Catch:{ all -> 0x005b }
            if (r0 == 0) goto L_0x00df;
        L_0x0058:
            r8 = (java.lang.ThreadDeath) r8;	 Catch:{ all -> 0x005b }
            throw r8;	 Catch:{ all -> 0x005b }
        L_0x005b:
            r0 = move-exception;
            monitor-enter(r12);
            r2 = java.lang.Thread.currentThread();	 Catch:{ all -> 0x0104 }
            r11 = r12.writeThread;	 Catch:{ all -> 0x0104 }
            r2 = r2.equals(r11);	 Catch:{ all -> 0x0104 }
            if (r2 == 0) goto L_0x006c;
        L_0x0069:
            r2 = 0;
            r12.writeThread = r2;	 Catch:{ all -> 0x0104 }
        L_0x006c:
            r2 = r12.closed;	 Catch:{ all -> 0x0104 }
            if (r2 != 0) goto L_0x007b;
        L_0x0070:
            r2 = r12.writeThread;	 Catch:{ all -> 0x0104 }
            if (r2 != 0) goto L_0x007b;
        L_0x0074:
            r2 = r12.writeQueueLength;	 Catch:{ all -> 0x0104 }
            if (r2 <= 0) goto L_0x007b;
        L_0x0078:
            r12.createWriteThread();	 Catch:{ all -> 0x0104 }
        L_0x007b:
            monitor-exit(r12);	 Catch:{ all -> 0x0104 }
            throw r0;
        L_0x007d:
            r6 = move-exception;
            r7 = 1;
            goto L_0x003c;
        L_0x0080:
            r10 = r12.writeQueueHead;	 Catch:{ all -> 0x0047 }
            r0 = r12.writeQueue;	 Catch:{ all -> 0x0047 }
            r9 = r0[r10];	 Catch:{ all -> 0x0047 }
            r1 = r9.data;	 Catch:{ all -> 0x0047 }
            r0 = 0;
            r9.data = r0;	 Catch:{ all -> 0x0047 }
            r5 = r9.exclusion;	 Catch:{ all -> 0x0047 }
            r0 = 0;
            r9.exclusion = r0;	 Catch:{ all -> 0x0047 }
            r4 = r9.format;	 Catch:{ all -> 0x0047 }
            r0 = 0;
            r9.format = r0;	 Catch:{ all -> 0x0047 }
            r3 = r9.length;	 Catch:{ all -> 0x0047 }
            r0 = 0;
            r9.length = r0;	 Catch:{ all -> 0x0047 }
            r0 = r12.writeQueueHead;	 Catch:{ all -> 0x0047 }
            r0 = r0 + 1;
            r12.writeQueueHead = r0;	 Catch:{ all -> 0x0047 }
            r0 = r12.writeQueueHead;	 Catch:{ all -> 0x0047 }
            r2 = r12.writeQueue;	 Catch:{ all -> 0x0047 }
            r2 = r2.length;	 Catch:{ all -> 0x0047 }
            if (r0 < r2) goto L_0x00aa;
        L_0x00a7:
            r0 = 0;
            r12.writeQueueHead = r0;	 Catch:{ all -> 0x0047 }
        L_0x00aa:
            r0 = r12.writeQueueLength;	 Catch:{ all -> 0x0047 }
            r0 = r0 + -1;
            r12.writeQueueLength = r0;	 Catch:{ all -> 0x0047 }
            monitor-exit(r12);	 Catch:{ all -> 0x0047 }
            r2 = 0;
            r0 = r12;
            r0.doWrite(r1, r2, r3, r4, r5);	 Catch:{ all -> 0x00c9 }
            monitor-enter(r12);	 Catch:{ Throwable -> 0x004a }
            r0 = r12.writeQueue;	 Catch:{ all -> 0x00c6 }
            r9 = r0[r10];	 Catch:{ all -> 0x00c6 }
            if (r9 == 0) goto L_0x00c3;
        L_0x00bd:
            r0 = r9.data;	 Catch:{ all -> 0x00c6 }
            if (r0 != 0) goto L_0x00c3;
        L_0x00c1:
            r9.data = r1;	 Catch:{ all -> 0x00c6 }
        L_0x00c3:
            monitor-exit(r12);	 Catch:{ all -> 0x00c6 }
            goto L_0x0000;
        L_0x00c6:
            r0 = move-exception;
            monitor-exit(r12);	 Catch:{ all -> 0x00c6 }
            throw r0;	 Catch:{ Throwable -> 0x004a }
        L_0x00c9:
            r0 = move-exception;
            monitor-enter(r12);	 Catch:{ Throwable -> 0x004a }
            r2 = r12.writeQueue;	 Catch:{ all -> 0x00d9 }
            r9 = r2[r10];	 Catch:{ all -> 0x00d9 }
            if (r9 == 0) goto L_0x00d7;
        L_0x00d1:
            r2 = r9.data;	 Catch:{ all -> 0x00d9 }
            if (r2 != 0) goto L_0x00d7;
        L_0x00d5:
            r9.data = r1;	 Catch:{ all -> 0x00d9 }
        L_0x00d7:
            monitor-exit(r12);	 Catch:{ all -> 0x00d9 }
            throw r0;	 Catch:{ Throwable -> 0x004a }
        L_0x00d9:
            r0 = move-exception;
            monitor-exit(r12);	 Catch:{ all -> 0x00d9 }
            throw r0;	 Catch:{ Throwable -> 0x004a }
        L_0x00dc:
            r0 = move-exception;
            monitor-exit(r12);	 Catch:{ all -> 0x00dc }
            throw r0;
        L_0x00df:
            monitor-enter(r12);
            r0 = java.lang.Thread.currentThread();	 Catch:{ all -> 0x0101 }
            r2 = r12.writeThread;	 Catch:{ all -> 0x0101 }
            r0 = r0.equals(r2);	 Catch:{ all -> 0x0101 }
            if (r0 == 0) goto L_0x00ef;
        L_0x00ec:
            r0 = 0;
            r12.writeThread = r0;	 Catch:{ all -> 0x0101 }
        L_0x00ef:
            r0 = r12.closed;	 Catch:{ all -> 0x0101 }
            if (r0 != 0) goto L_0x00fe;
        L_0x00f3:
            r0 = r12.writeThread;	 Catch:{ all -> 0x0101 }
            if (r0 != 0) goto L_0x00fe;
        L_0x00f7:
            r0 = r12.writeQueueLength;	 Catch:{ all -> 0x0101 }
            if (r0 <= 0) goto L_0x00fe;
        L_0x00fb:
            r12.createWriteThread();	 Catch:{ all -> 0x0101 }
        L_0x00fe:
            monitor-exit(r12);	 Catch:{ all -> 0x0101 }
            goto L_0x0032;
        L_0x0101:
            r0 = move-exception;
            monitor-exit(r12);	 Catch:{ all -> 0x0101 }
            throw r0;
        L_0x0104:
            r0 = move-exception;
            monitor-exit(r12);	 Catch:{ all -> 0x0104 }
            throw r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.RTPTranslatorImpl$OutputDataStreamImpl.run():void");
        }

        private boolean willWriteControl(StreamRTPManagerDesc destination, byte[] buffer, int offset, int length, Format format, StreamRTPManagerDesc exclusion) {
            boolean write = true;
            if (length >= 12) {
                byte b0 = buffer[offset];
                if (((b0 & 192) >>> 6) == 2) {
                    int pt = buffer[offset + 1] & UnsignedUtils.MAX_UBYTE;
                    if (pt == RTCPFeedbackEvent.PT_TL || pt == RTCPFeedbackEvent.PT_PS) {
                        if ((RTPTranslatorImpl.readUnsignedShort(buffer, offset + 2) + 1) * 4 <= length) {
                            int ssrcOfMediaSource = RTPTranslatorImpl.readInt(buffer, offset + 8);
                            if (!destination.containsReceiveSSRC(ssrcOfMediaSource)) {
                                write = false;
                            } else if (RTPTranslatorImpl.logger.isTraceEnabled()) {
                                RTPTranslatorImpl.logger.trace(getClass().getName() + ".willWriteControl: FMT " + (b0 & 31) + ", PT " + pt + ", SSRC of packet sender " + Long.toString(((long) RTPTranslatorImpl.readInt(buffer, offset + 4)) & 4294967295L) + ", SSRC of media source " + Long.toString(((long) ssrcOfMediaSource) & 4294967295L));
                            }
                        }
                    }
                }
            }
            if (write && RTPTranslatorImpl.logger.isTraceEnabled()) {
                RTPTranslatorImpl.logRTCP(this, "doWrite", buffer, offset, length);
            }
            return write;
        }

        private boolean willWriteData(StreamRTPManagerDesc destination, byte[] buffer, int offset, int length, Format format, StreamRTPManagerDesc exclusion) {
            if (!destination.streamRTPManager.getMediaStream().getDirection().allowsSending()) {
                return false;
            }
            if (format != null && length > 0) {
                Integer payloadType = destination.getPayloadType(format);
                if (payloadType == null && exclusion != null) {
                    payloadType = exclusion.getPayloadType(format);
                }
                if (payloadType != null) {
                    int payloadTypeByteIndex = offset + 1;
                    buffer[payloadTypeByteIndex] = (byte) ((buffer[payloadTypeByteIndex] & 128) | (payloadType.intValue() & 127));
                }
            }
            return true;
        }

        public int write(byte[] buffer, int offset, int length) {
            return doWrite(buffer, offset, length, null, null);
        }

        public synchronized void write(byte[] buffer, int offset, int length, Format format, StreamRTPManagerDesc exclusion) {
            if (!this.closed) {
                int writeIndex;
                if (this.writeQueueLength < this.writeQueue.length) {
                    writeIndex = (this.writeQueueHead + this.writeQueueLength) % this.writeQueue.length;
                } else {
                    writeIndex = this.writeQueueHead;
                    this.writeQueueHead++;
                    if (this.writeQueueHead >= this.writeQueue.length) {
                        this.writeQueueHead = 0;
                    }
                    this.writeQueueLength--;
                    RTPTranslatorImpl.logger.warn("Will not translate RTP packet.");
                }
                RTPTranslatorBuffer write = this.writeQueue[writeIndex];
                if (write == null) {
                    RTPTranslatorBuffer[] rTPTranslatorBufferArr = this.writeQueue;
                    write = new RTPTranslatorBuffer();
                    rTPTranslatorBufferArr[writeIndex] = write;
                }
                byte[] data = write.data;
                if (data == null || data.length < length) {
                    data = new byte[length];
                    write.data = data;
                }
                System.arraycopy(buffer, offset, data, 0, length);
                write.exclusion = exclusion;
                write.format = format;
                write.length = length;
                this.writeQueueLength++;
                if (this.writeThread == null) {
                    createWriteThread();
                } else {
                    notify();
                }
            }
        }
    }

    private static class PushSourceStreamDesc {
        public final RTPConnectorDesc connectorDesc;
        public final boolean data;
        public final PushSourceStream stream;

        public PushSourceStreamDesc(RTPConnectorDesc connectorDesc, PushSourceStream stream, boolean data) {
            this.connectorDesc = connectorDesc;
            this.stream = stream;
            this.data = data;
        }
    }

    private class PushSourceStreamImpl implements PushSourceStream, Runnable, SourceTransferHandler {
        private boolean closed = false;
        private final boolean data;
        private boolean read = false;
        private final Queue<SourcePacket> readQ;
        private final int readQCapacity;
        private final Queue<SourcePacket> sourcePacketPool = new LinkedBlockingQueue();
        private final List<PushSourceStreamDesc> streams = new LinkedList();
        private Thread transferDataThread;
        private SourceTransferHandler transferHandler;

        public PushSourceStreamImpl(boolean data) {
            this.data = data;
            this.readQCapacity = 256;
            this.readQ = new ArrayBlockingQueue(this.readQCapacity);
            this.transferDataThread = new Thread(this, getClass().getName());
            this.transferDataThread.setDaemon(true);
            this.transferDataThread.start();
        }

        public synchronized void addStream(RTPConnectorDesc connectorDesc, PushSourceStream stream) {
            for (PushSourceStreamDesc streamDesc : this.streams) {
                if (streamDesc.connectorDesc == connectorDesc && streamDesc.stream == stream) {
                    break;
                }
            }
            this.streams.add(new PushSourceStreamDesc(connectorDesc, stream, this.data));
            stream.setTransferHandler(this);
        }

        public void close() {
            this.closed = true;
            this.sourcePacketPool.clear();
        }

        public boolean endOfStream() {
            return false;
        }

        public ContentDescriptor getContentDescriptor() {
            return null;
        }

        public long getContentLength() {
            return -1;
        }

        public Object getControl(String controlType) {
            return null;
        }

        public Object[] getControls() {
            return null;
        }

        public synchronized int getMinimumTransferSize() {
            int minimumTransferSize;
            minimumTransferSize = 0;
            for (PushSourceStreamDesc streamDesc : this.streams) {
                int streamMinimumTransferSize = streamDesc.stream.getMinimumTransferSize();
                if (minimumTransferSize < streamMinimumTransferSize) {
                    minimumTransferSize = streamMinimumTransferSize;
                }
            }
            return minimumTransferSize;
        }

        /* JADX WARNING: Missing block: B:21:0x0056, code skipped:
            java.lang.System.arraycopy(r6.getBuffer(), r6.getOffset(), r9, r10, r7);
            r1 = r6.streamDesc;
            r5 = r7;
            r6.streamDesc = null;
            r8.sourcePacketPool.offer(r6);
     */
        /* JADX WARNING: Missing block: B:22:0x006c, code skipped:
            if (r5 <= 0) goto L_?;
     */
        /* JADX WARNING: Missing block: B:26:?, code skipped:
            return r5;
     */
        /* JADX WARNING: Missing block: B:27:?, code skipped:
            return org.jitsi.impl.neomedia.RTPTranslatorImpl.access$500(r8.this$0, r1, r9, r10, r11, r5);
     */
        public int read(byte[] r9, int r10, int r11) throws java.io.IOException {
            /*
            r8 = this;
            r0 = r8.closed;
            if (r0 == 0) goto L_0x0006;
        L_0x0004:
            r5 = -1;
        L_0x0005:
            return r5;
        L_0x0006:
            r2 = r8.readQ;
            monitor-enter(r2);
            r0 = r8.readQ;	 Catch:{ all -> 0x0016 }
            r6 = r0.peek();	 Catch:{ all -> 0x0016 }
            r6 = (org.jitsi.impl.neomedia.RTPTranslatorImpl.SourcePacket) r6;	 Catch:{ all -> 0x0016 }
            if (r6 != 0) goto L_0x0019;
        L_0x0013:
            r5 = 0;
            monitor-exit(r2);	 Catch:{ all -> 0x0016 }
            goto L_0x0005;
        L_0x0016:
            r0 = move-exception;
            monitor-exit(r2);	 Catch:{ all -> 0x0016 }
            throw r0;
        L_0x0019:
            r7 = r6.getLength();	 Catch:{ all -> 0x0016 }
            if (r11 >= r7) goto L_0x0048;
        L_0x001f:
            r0 = new java.io.IOException;	 Catch:{ all -> 0x0016 }
            r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0016 }
            r3.<init>();	 Catch:{ all -> 0x0016 }
            r4 = "Length ";
            r3 = r3.append(r4);	 Catch:{ all -> 0x0016 }
            r3 = r3.append(r11);	 Catch:{ all -> 0x0016 }
            r4 = " is insuffient. Must be at least ";
            r3 = r3.append(r4);	 Catch:{ all -> 0x0016 }
            r3 = r3.append(r7);	 Catch:{ all -> 0x0016 }
            r4 = ".";
            r3 = r3.append(r4);	 Catch:{ all -> 0x0016 }
            r3 = r3.toString();	 Catch:{ all -> 0x0016 }
            r0.<init>(r3);	 Catch:{ all -> 0x0016 }
            throw r0;	 Catch:{ all -> 0x0016 }
        L_0x0048:
            r0 = r8.readQ;	 Catch:{ all -> 0x0016 }
            r0.remove();	 Catch:{ all -> 0x0016 }
            r0 = 1;
            r8.read = r0;	 Catch:{ all -> 0x0016 }
            r0 = r8.readQ;	 Catch:{ all -> 0x0016 }
            r0.notifyAll();	 Catch:{ all -> 0x0016 }
            monitor-exit(r2);	 Catch:{ all -> 0x0016 }
            r0 = r6.getBuffer();
            r2 = r6.getOffset();
            java.lang.System.arraycopy(r0, r2, r9, r10, r7);
            r1 = r6.streamDesc;
            r5 = r7;
            r0 = 0;
            r6.streamDesc = r0;
            r0 = r8.sourcePacketPool;
            r0.offer(r6);
            if (r5 <= 0) goto L_0x0005;
        L_0x006e:
            r0 = org.jitsi.impl.neomedia.RTPTranslatorImpl.this;
            r2 = r9;
            r3 = r10;
            r4 = r11;
            r5 = r0.read(r1, r2, r3, r4, r5);
            goto L_0x0005;
            */
            throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.RTPTranslatorImpl$PushSourceStreamImpl.read(byte[], int, int):int");
        }

        public synchronized void removeStreams(RTPConnectorDesc connectorDesc) {
            Iterator<PushSourceStreamDesc> streamIter = this.streams.iterator();
            while (streamIter.hasNext()) {
                PushSourceStreamDesc streamDesc = (PushSourceStreamDesc) streamIter.next();
                if (streamDesc.connectorDesc == connectorDesc) {
                    streamDesc.stream.setTransferHandler(null);
                    streamIter.remove();
                }
            }
        }

        public void run() {
            while (!this.closed) {
                try {
                    SourceTransferHandler transferHandler = this.transferHandler;
                    synchronized (this.readQ) {
                        if (this.readQ.isEmpty() || transferHandler == null) {
                            try {
                                this.readQ.wait(100);
                            } catch (InterruptedException e) {
                            }
                        } else {
                            transferHandler.transferData(this);
                        }
                    }
                } catch (Throwable th) {
                    if (Thread.currentThread().equals(this.transferDataThread)) {
                        this.transferDataThread = null;
                    }
                }
            }
            if (Thread.currentThread().equals(this.transferDataThread)) {
                this.transferDataThread = null;
            }
        }

        public synchronized void setTransferHandler(SourceTransferHandler transferHandler) {
            if (this.transferHandler != transferHandler) {
                this.transferHandler = transferHandler;
                for (PushSourceStreamDesc streamDesc : this.streams) {
                    streamDesc.stream.setTransferHandler(this);
                }
            }
        }

        public void transferData(PushSourceStream stream) {
            boolean yield = true;
            if (!this.closed) {
                PushSourceStreamDesc streamDesc = null;
                synchronized (this) {
                    for (PushSourceStreamDesc aStreamDesc : this.streams) {
                        if (aStreamDesc.stream == stream) {
                            streamDesc = aStreamDesc;
                            break;
                        }
                    }
                }
                if (streamDesc != null) {
                    byte[] buf;
                    int len = stream.getMinimumTransferSize();
                    if (len < 1) {
                        len = 2048;
                    }
                    SourcePacket pkt = (SourcePacket) this.sourcePacketPool.poll();
                    if (pkt == null || pkt.getBuffer().length < len) {
                        buf = new byte[len];
                        pkt = new SourcePacket(buf, 0, len);
                    } else {
                        buf = pkt.getBuffer();
                        len = buf.length;
                    }
                    int read = 0;
                    int readQSize;
                    try {
                        read = stream.read(buf, 0, len);
                        if (read > 0) {
                            pkt.setLength(read);
                            pkt.setOffset(0);
                            pkt.streamDesc = streamDesc;
                            synchronized (this.readQ) {
                                readQSize = this.readQ.size();
                                if (readQSize < 1) {
                                    yield = false;
                                } else if (readQSize >= this.readQCapacity) {
                                    yield = true;
                                } else if (this.read) {
                                    yield = false;
                                }
                                if (yield) {
                                    this.readQ.notifyAll();
                                }
                            }
                            if (yield) {
                                Thread.yield();
                            }
                            synchronized (this.readQ) {
                                if (this.readQ.size() >= this.readQCapacity) {
                                    this.readQ.remove();
                                    RTPTranslatorImpl.logger.warn("Discarded an RTP packet because the read queue is full.");
                                }
                                if (this.readQ.offer(pkt)) {
                                }
                                this.readQ.notifyAll();
                            }
                            return;
                        }
                        pkt.streamDesc = null;
                        this.sourcePacketPool.offer(pkt);
                    } catch (IOException ioe) {
                        RTPTranslatorImpl.logger.error("Failed to read from an RTP stream!", ioe);
                        if (read > 0) {
                            pkt.setLength(read);
                            pkt.setOffset(0);
                            pkt.streamDesc = streamDesc;
                            synchronized (this.readQ) {
                                readQSize = this.readQ.size();
                                if (readQSize < 1) {
                                    yield = false;
                                } else if (readQSize >= this.readQCapacity) {
                                    yield = true;
                                } else if (this.read) {
                                    yield = false;
                                }
                                if (yield) {
                                    this.readQ.notifyAll();
                                }
                                if (yield) {
                                    Thread.yield();
                                }
                                synchronized (this.readQ) {
                                    if (this.readQ.size() >= this.readQCapacity) {
                                        this.readQ.remove();
                                        RTPTranslatorImpl.logger.warn("Discarded an RTP packet because the read queue is full.");
                                    }
                                    if (this.readQ.offer(pkt)) {
                                    }
                                    this.readQ.notifyAll();
                                    return;
                                }
                            }
                        }
                        pkt.streamDesc = null;
                        this.sourcePacketPool.offer(pkt);
                    } catch (Throwable th) {
                        if (read > 0) {
                            pkt.setLength(read);
                            pkt.setOffset(0);
                            pkt.streamDesc = streamDesc;
                            synchronized (this.readQ) {
                                readQSize = this.readQ.size();
                                if (readQSize < 1) {
                                    yield = false;
                                } else if (readQSize >= this.readQCapacity) {
                                    yield = true;
                                } else if (this.read) {
                                    yield = false;
                                }
                                if (yield) {
                                    this.readQ.notifyAll();
                                }
                                if (yield) {
                                    Thread.yield();
                                }
                                synchronized (this.readQ) {
                                    if (this.readQ.size() >= this.readQCapacity) {
                                        this.readQ.remove();
                                        RTPTranslatorImpl.logger.warn("Discarded an RTP packet because the read queue is full.");
                                    }
                                    if (this.readQ.offer(pkt)) {
                                    }
                                    this.readQ.notifyAll();
                                }
                            }
                        } else {
                            pkt.streamDesc = null;
                            this.sourcePacketPool.offer(pkt);
                        }
                    }
                }
            }
        }
    }

    private static class RTPConnectorDesc {
        public final RTPConnector connector;
        public final StreamRTPManagerDesc streamRTPManagerDesc;

        public RTPConnectorDesc(StreamRTPManagerDesc streamRTPManagerDesc, RTPConnector connector) {
            this.streamRTPManagerDesc = streamRTPManagerDesc;
            this.connector = connector;
        }
    }

    private class RTPConnectorImpl implements RTPConnector {
        private final List<RTPConnectorDesc> connectors;
        private PushSourceStreamImpl controlInputStream;
        private OutputDataStreamImpl controlOutputStream;
        private PushSourceStreamImpl dataInputStream;
        private OutputDataStreamImpl dataOutputStream;

        private RTPConnectorImpl() {
            this.connectors = new LinkedList();
        }

        public synchronized void addConnector(RTPConnectorDesc connector) {
            if (!this.connectors.contains(connector)) {
                this.connectors.add(connector);
                if (this.controlInputStream != null) {
                    try {
                        PushSourceStream controlInputStream = connector.connector.getControlInputStream();
                        if (controlInputStream != null) {
                            this.controlInputStream.addStream(connector, controlInputStream);
                        }
                    } catch (IOException ioe) {
                        throw new UndeclaredThrowableException(ioe);
                    } catch (IOException ioe2) {
                        throw new UndeclaredThrowableException(ioe2);
                    } catch (IOException ioe22) {
                        throw new UndeclaredThrowableException(ioe22);
                    } catch (IOException ioe222) {
                        throw new UndeclaredThrowableException(ioe222);
                    }
                }
                if (this.controlOutputStream != null) {
                    OutputDataStream controlOutputStream = connector.connector.getControlOutputStream();
                    if (controlOutputStream != null) {
                        this.controlOutputStream.addStream(connector, controlOutputStream);
                    }
                }
                if (this.dataInputStream != null) {
                    PushSourceStream dataInputStream = connector.connector.getDataInputStream();
                    if (dataInputStream != null) {
                        this.dataInputStream.addStream(connector, dataInputStream);
                    }
                }
                if (this.dataOutputStream != null) {
                    OutputDataStream dataOutputStream = connector.connector.getDataOutputStream();
                    if (dataOutputStream != null) {
                        this.dataOutputStream.addStream(connector, dataOutputStream);
                    }
                }
            }
        }

        public synchronized void close() {
            if (this.controlInputStream != null) {
                this.controlInputStream.close();
                this.controlInputStream = null;
            }
            if (this.controlOutputStream != null) {
                this.controlOutputStream.close();
                this.controlOutputStream = null;
            }
            if (this.dataInputStream != null) {
                this.dataInputStream.close();
                this.dataInputStream = null;
            }
            if (this.dataOutputStream != null) {
                this.dataOutputStream.close();
                this.dataOutputStream = null;
            }
            for (RTPConnectorDesc connectorDesc : this.connectors) {
                connectorDesc.connector.close();
            }
        }

        public synchronized PushSourceStream getControlInputStream() throws IOException {
            if (this.controlInputStream == null) {
                this.controlInputStream = new PushSourceStreamImpl(false);
                for (RTPConnectorDesc connectorDesc : this.connectors) {
                    PushSourceStream controlInputStream = connectorDesc.connector.getControlInputStream();
                    if (controlInputStream != null) {
                        this.controlInputStream.addStream(connectorDesc, controlInputStream);
                    }
                }
            }
            return this.controlInputStream;
        }

        public synchronized OutputDataStreamImpl getControlOutputStream() throws IOException {
            if (this.controlOutputStream == null) {
                this.controlOutputStream = new OutputDataStreamImpl(false);
                for (RTPConnectorDesc connectorDesc : this.connectors) {
                    OutputDataStream controlOutputStream = connectorDesc.connector.getControlOutputStream();
                    if (controlOutputStream != null) {
                        this.controlOutputStream.addStream(connectorDesc, controlOutputStream);
                    }
                }
            }
            return this.controlOutputStream;
        }

        public synchronized PushSourceStream getDataInputStream() throws IOException {
            if (this.dataInputStream == null) {
                this.dataInputStream = new PushSourceStreamImpl(true);
                for (RTPConnectorDesc connectorDesc : this.connectors) {
                    PushSourceStream dataInputStream = connectorDesc.connector.getDataInputStream();
                    if (dataInputStream != null) {
                        this.dataInputStream.addStream(connectorDesc, dataInputStream);
                    }
                }
            }
            return this.dataInputStream;
        }

        public synchronized OutputDataStreamImpl getDataOutputStream() throws IOException {
            if (this.dataOutputStream == null) {
                this.dataOutputStream = new OutputDataStreamImpl(true);
                for (RTPConnectorDesc connectorDesc : this.connectors) {
                    OutputDataStream dataOutputStream = connectorDesc.connector.getDataOutputStream();
                    if (dataOutputStream != null) {
                        this.dataOutputStream.addStream(connectorDesc, dataOutputStream);
                    }
                }
            }
            return this.dataOutputStream;
        }

        public int getReceiveBufferSize() {
            return -1;
        }

        public double getRTCPBandwidthFraction() {
            return -1.0d;
        }

        public double getRTCPSenderBandwidthFraction() {
            return -1.0d;
        }

        public int getSendBufferSize() {
            return -1;
        }

        public synchronized void removeConnector(RTPConnectorDesc connector) {
            if (this.connectors.contains(connector)) {
                if (this.controlInputStream != null) {
                    this.controlInputStream.removeStreams(connector);
                }
                if (this.controlOutputStream != null) {
                    this.controlOutputStream.removeStreams(connector);
                }
                if (this.dataInputStream != null) {
                    this.dataInputStream.removeStreams(connector);
                }
                if (this.dataOutputStream != null) {
                    this.dataOutputStream.removeStreams(connector);
                }
                this.connectors.remove(connector);
            }
        }

        public void setReceiveBufferSize(int receiveBufferSize) throws IOException {
        }

        public void setSendBufferSize(int sendBufferSize) throws IOException {
        }
    }

    private static class RTPTranslatorBuffer {
        public byte[] data;
        public StreamRTPManagerDesc exclusion;
        public Format format;
        public int length;

        private RTPTranslatorBuffer() {
        }
    }

    private class SendStreamDesc {
        public final DataSource dataSource;
        public final SendStream sendStream;
        private final List<SendStreamImpl> sendStreams = new LinkedList();
        private int started;
        public final int streamIndex;

        public SendStreamDesc(DataSource dataSource, int streamIndex, SendStream sendStream) {
            this.dataSource = dataSource;
            this.sendStream = sendStream;
            this.streamIndex = streamIndex;
        }

        /* access modifiers changed from: 0000 */
        public void close(SendStreamImpl sendStream) {
            boolean close = false;
            synchronized (this) {
                if (this.sendStreams.contains(sendStream)) {
                    this.sendStreams.remove(sendStream);
                    close = this.sendStreams.isEmpty();
                }
            }
            if (close) {
                RTPTranslatorImpl.this.closeSendStream(this);
            }
        }

        public synchronized SendStreamImpl getSendStream(StreamRTPManager streamRTPManager, boolean create) {
            SendStreamImpl sendStream;
            for (SendStreamImpl sendStream2 : this.sendStreams) {
                if (sendStream2.streamRTPManager == streamRTPManager) {
                    break;
                }
            }
            if (create) {
                sendStream2 = new SendStreamImpl(streamRTPManager, this);
                this.sendStreams.add(sendStream2);
            } else {
                sendStream2 = null;
            }
            return sendStream2;
        }

        public synchronized int getSendStreamCount() {
            return this.sendStreams.size();
        }

        /* access modifiers changed from: declared_synchronized */
        public synchronized void start(SendStreamImpl sendStream) throws IOException {
            if (this.sendStreams.contains(sendStream)) {
                if (this.started < 1) {
                    this.sendStream.start();
                    this.started = 1;
                } else {
                    this.started++;
                }
            }
        }

        /* access modifiers changed from: declared_synchronized */
        public synchronized void stop(SendStreamImpl sendStream) throws IOException {
            if (this.sendStreams.contains(sendStream)) {
                if (this.started == 1) {
                    this.sendStream.stop();
                    this.started = 0;
                } else if (this.started > 1) {
                    this.started--;
                }
            }
        }
    }

    private static class SendStreamImpl implements SendStream {
        private boolean closed;
        public final SendStreamDesc sendStreamDesc;
        private boolean started;
        public final StreamRTPManager streamRTPManager;

        public SendStreamImpl(StreamRTPManager streamRTPManager, SendStreamDesc sendStreamDesc) {
            this.sendStreamDesc = sendStreamDesc;
            this.streamRTPManager = streamRTPManager;
        }

        public void close() {
            if (!this.closed) {
                try {
                    if (this.started) {
                        stop();
                    }
                    this.sendStreamDesc.close(this);
                    this.closed = true;
                } catch (IOException ioe) {
                    throw new UndeclaredThrowableException(ioe);
                } catch (Throwable th) {
                    this.sendStreamDesc.close(this);
                    this.closed = true;
                }
            }
        }

        public DataSource getDataSource() {
            return this.sendStreamDesc.sendStream.getDataSource();
        }

        public Participant getParticipant() {
            return this.sendStreamDesc.sendStream.getParticipant();
        }

        public SenderReport getSenderReport() {
            return this.sendStreamDesc.sendStream.getSenderReport();
        }

        public TransmissionStats getSourceTransmissionStats() {
            return this.sendStreamDesc.sendStream.getSourceTransmissionStats();
        }

        public long getSSRC() {
            return this.sendStreamDesc.sendStream.getSSRC();
        }

        public int setBitRate(int bitRate) {
            return 0;
        }

        public void setSourceDescription(SourceDescription[] sourceDescription) {
        }

        public void start() throws IOException {
            if (this.closed) {
                throw new IOException("Cannot start SendStream after it has been closed.");
            } else if (!this.started) {
                this.sendStreamDesc.start(this);
                this.started = true;
            }
        }

        public void stop() throws IOException {
            if (!this.closed && this.started) {
                this.sendStreamDesc.stop(this);
                this.started = false;
            }
        }
    }

    private static class SourcePacket extends RawPacket {
        public PushSourceStreamDesc streamDesc;

        public SourcePacket(byte[] buf, int off, int len) {
            super(buf, off, len);
        }
    }

    private static class StreamRTPManagerDesc {
        public RTPConnectorDesc connectorDesc;
        private final Map<Integer, Format> formats = new HashMap();
        private int[] receiveSSRCs = RTPTranslatorImpl.EMPTY_INT_ARRAY;
        private final List<ReceiveStreamListener> receiveStreamListeners = new LinkedList();
        public final StreamRTPManager streamRTPManager;

        public StreamRTPManagerDesc(StreamRTPManager streamRTPManager) {
            this.streamRTPManager = streamRTPManager;
        }

        public void addFormat(Format format, int payloadType) {
            synchronized (this.formats) {
                this.formats.put(Integer.valueOf(payloadType), format);
            }
        }

        public synchronized void addReceiveSSRC(int receiveSSRC) {
            if (!containsReceiveSSRC(receiveSSRC)) {
                int receiveSSRCCount = this.receiveSSRCs.length;
                int[] newReceiveSSRCs = new int[(receiveSSRCCount + 1)];
                System.arraycopy(this.receiveSSRCs, 0, newReceiveSSRCs, 0, receiveSSRCCount);
                newReceiveSSRCs[receiveSSRCCount] = receiveSSRC;
                this.receiveSSRCs = newReceiveSSRCs;
            }
        }

        public void addReceiveStreamListener(ReceiveStreamListener listener) {
            synchronized (this.receiveStreamListeners) {
                if (!this.receiveStreamListeners.contains(listener)) {
                    this.receiveStreamListeners.add(listener);
                }
            }
        }

        public synchronized boolean containsReceiveSSRC(int receiveSSRC) {
            boolean z;
            for (int i : this.receiveSSRCs) {
                if (i == receiveSSRC) {
                    z = true;
                    break;
                }
            }
            z = false;
            return z;
        }

        public Format getFormat(int payloadType) {
            Format format;
            synchronized (this.formats) {
                format = (Format) this.formats.get(Integer.valueOf(payloadType));
            }
            return format;
        }

        public Format[] getFormats() {
            Format[] formatArr;
            synchronized (this.formats) {
                Collection<Format> formats = this.formats.values();
                formatArr = (Format[]) formats.toArray(new Format[formats.size()]);
            }
            return formatArr;
        }

        public Integer getPayloadType(Format format) {
            synchronized (this.formats) {
                for (Entry<Integer, Format> entry : this.formats.entrySet()) {
                    Format entryFormat = (Format) entry.getValue();
                    if (!entryFormat.matches(format)) {
                        if (format.matches(entryFormat)) {
                        }
                    }
                    Integer num = (Integer) entry.getKey();
                    return num;
                }
                return null;
            }
        }

        public ReceiveStreamListener[] getReceiveStreamListeners() {
            ReceiveStreamListener[] receiveStreamListenerArr;
            synchronized (this.receiveStreamListeners) {
                receiveStreamListenerArr = (ReceiveStreamListener[]) this.receiveStreamListeners.toArray(new ReceiveStreamListener[this.receiveStreamListeners.size()]);
            }
            return receiveStreamListenerArr;
        }

        public void removeReceiveStreamListener(ReceiveStreamListener listener) {
            synchronized (this.receiveStreamListeners) {
                this.receiveStreamListeners.remove(listener);
            }
        }
    }

    public RTPTranslatorImpl() {
        this.manager.addReceiveStreamListener(this);
    }

    public synchronized void addFormat(StreamRTPManager streamRTPManager, Format format, int payloadType) {
        this.manager.addFormat(format, payloadType);
        getStreamRTPManagerDesc(streamRTPManager, true).addFormat(format, payloadType);
    }

    public synchronized void addReceiveStreamListener(StreamRTPManager streamRTPManager, ReceiveStreamListener listener) {
        getStreamRTPManagerDesc(streamRTPManager, true).addReceiveStreamListener(listener);
    }

    public void addRemoteListener(StreamRTPManager streamRTPManager, RemoteListener listener) {
        this.manager.addRemoteListener(listener);
    }

    public void addSendStreamListener(StreamRTPManager streamRTPManager, SendStreamListener listener) {
    }

    public void addSessionListener(StreamRTPManager streamRTPManager, SessionListener listener) {
    }

    private synchronized void closeFakeSendStreamIfNotNecessary() {
        try {
            if ((!this.sendStreams.isEmpty() || this.streamRTPManagers.size() < 2) && this.fakeSendStream != null) {
                this.fakeSendStream.close();
                this.fakeSendStream = null;
            }
        } catch (NullPointerException npe) {
            logger.error("Failed to close fake send stream", npe);
            this.fakeSendStream = null;
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                ThreadDeath t2 = (ThreadDeath) t;
            } else if (logger.isDebugEnabled()) {
                logger.debug("Failed to close the fake SendStream of this RTPTranslator.", t);
            }
        }
    }

    /* access modifiers changed from: private|declared_synchronized */
    public synchronized void closeSendStream(SendStreamDesc sendStreamDesc) {
        if (this.sendStreams.contains(sendStreamDesc) && sendStreamDesc.getSendStreamCount() < 1) {
            try {
                sendStreamDesc.sendStream.close();
            } catch (NullPointerException npe) {
                logger.error("Failed to close send stream", npe);
            }
            this.sendStreams.remove(sendStreamDesc);
        }
        return;
    }

    private synchronized void createFakeSendStreamIfNecessary() {
        if (this.fakeSendStream == null && this.sendStreams.isEmpty() && this.streamRTPManagers.size() > 1) {
            Format supportedFormat = null;
            for (StreamRTPManagerDesc s : this.streamRTPManagers) {
                Format[] formats = s.getFormats();
                if (formats != null && formats.length > 0) {
                    for (Format f : formats) {
                        if (f != null) {
                            supportedFormat = f;
                            break;
                        }
                    }
                    if (supportedFormat != null) {
                        break;
                    }
                }
            }
            if (supportedFormat != null) {
                try {
                    this.fakeSendStream = this.manager.createSendStream(new FakePushBufferDataSource(supportedFormat), 0);
                } catch (Throwable t) {
                    if (t instanceof ThreadDeath) {
                        ThreadDeath t2 = (ThreadDeath) t;
                    } else {
                        logger.error("Failed to create a fake SendStream to ensure that this RTPTranslator is able to disperse RTP and RTCP received from remote peers even when the local peer is not generating media to be transmitted.", t);
                    }
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x003a  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0037  */
    /* JADX WARNING: Missing block: B:21:0x0039, code skipped:
            return r3 != null ? null : r3.getSendStream(r7, true);
     */
    public synchronized javax.media.rtp.SendStream createSendStream(org.jitsi.impl.neomedia.StreamRTPManager r7, javax.media.protocol.DataSource r8, int r9) throws java.io.IOException, javax.media.format.UnsupportedFormatException {
        /*
        r6 = this;
        monitor-enter(r6);
        r3 = 0;
        r5 = r6.sendStreams;	 Catch:{ all -> 0x0040 }
        r0 = r5.iterator();	 Catch:{ all -> 0x0040 }
    L_0x0008:
        r5 = r0.hasNext();	 Catch:{ all -> 0x0040 }
        if (r5 == 0) goto L_0x0048;
    L_0x000e:
        r1 = r0.next();	 Catch:{ all -> 0x0040 }
        r1 = (org.jitsi.impl.neomedia.RTPTranslatorImpl.SendStreamDesc) r1;	 Catch:{ all -> 0x0040 }
        r5 = r1.dataSource;	 Catch:{ all -> 0x0040 }
        if (r5 != r8) goto L_0x0008;
    L_0x0018:
        r5 = r1.streamIndex;	 Catch:{ all -> 0x0040 }
        if (r5 != r9) goto L_0x0008;
    L_0x001c:
        r3 = r1;
        r4 = r3;
    L_0x001e:
        if (r4 != 0) goto L_0x0046;
    L_0x0020:
        r5 = r6.manager;	 Catch:{ all -> 0x0043 }
        r2 = r5.createSendStream(r8, r9);	 Catch:{ all -> 0x0043 }
        if (r2 == 0) goto L_0x0046;
    L_0x0028:
        r3 = new org.jitsi.impl.neomedia.RTPTranslatorImpl$SendStreamDesc;	 Catch:{ all -> 0x0043 }
        r3.m1849init(r8, r9, r2);	 Catch:{ all -> 0x0043 }
        r5 = r6.sendStreams;	 Catch:{ all -> 0x0040 }
        r5.add(r3);	 Catch:{ all -> 0x0040 }
        r6.closeFakeSendStreamIfNotNecessary();	 Catch:{ all -> 0x0040 }
    L_0x0035:
        if (r3 != 0) goto L_0x003a;
    L_0x0037:
        r5 = 0;
    L_0x0038:
        monitor-exit(r6);
        return r5;
    L_0x003a:
        r5 = 1;
        r5 = r3.getSendStream(r7, r5);	 Catch:{ all -> 0x0040 }
        goto L_0x0038;
    L_0x0040:
        r5 = move-exception;
    L_0x0041:
        monitor-exit(r6);
        throw r5;
    L_0x0043:
        r5 = move-exception;
        r3 = r4;
        goto L_0x0041;
    L_0x0046:
        r3 = r4;
        goto L_0x0035;
    L_0x0048:
        r4 = r3;
        goto L_0x001e;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.RTPTranslatorImpl.createSendStream(org.jitsi.impl.neomedia.StreamRTPManager, javax.media.protocol.DataSource, int):javax.media.rtp.SendStream");
    }

    public synchronized void dispose() {
        this.manager.removeReceiveStreamListener(this);
        try {
            this.manager.dispose();
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                ThreadDeath t2 = (ThreadDeath) t;
            } else {
                logger.error("Failed to dispose of RTPManager", t);
            }
        }
    }

    public synchronized void dispose(StreamRTPManager streamRTPManager) {
        Iterator<StreamRTPManagerDesc> streamRTPManagerIter = this.streamRTPManagers.iterator();
        while (streamRTPManagerIter.hasNext()) {
            StreamRTPManagerDesc streamRTPManagerDesc = (StreamRTPManagerDesc) streamRTPManagerIter.next();
            if (streamRTPManagerDesc.streamRTPManager == streamRTPManager) {
                RTPConnectorDesc connectorDesc = streamRTPManagerDesc.connectorDesc;
                if (connectorDesc != null) {
                    if (this.connector != null) {
                        this.connector.removeConnector(connectorDesc);
                    }
                    connectorDesc.connector.close();
                    streamRTPManagerDesc.connectorDesc = null;
                }
                streamRTPManagerIter.remove();
                closeFakeSendStreamIfNotNecessary();
            }
        }
    }

    private synchronized StreamRTPManagerDesc findStreamRTPManagerDescByReceiveSSRC(int receiveSSRC, StreamRTPManagerDesc exclusion) {
        StreamRTPManagerDesc s;
        int count = this.streamRTPManagers.size();
        for (int i = 0; i < count; i++) {
            s = (StreamRTPManagerDesc) this.streamRTPManagers.get(i);
            if (s != exclusion && s.containsReceiveSSRC(receiveSSRC)) {
                break;
            }
        }
        s = null;
        return s;
    }

    public Object getControl(StreamRTPManager streamRTPManager, String controlType) {
        return this.manager.getControl(controlType);
    }

    public GlobalReceptionStats getGlobalReceptionStats(StreamRTPManager streamRTPManager) {
        return this.manager.getGlobalReceptionStats();
    }

    public GlobalTransmissionStats getGlobalTransmissionStats(StreamRTPManager streamRTPManager) {
        return this.manager.getGlobalTransmissionStats();
    }

    public long getLocalSSRC(StreamRTPManager streamRTPManager) {
        return ((RTPSessionMgr) this.manager).getLocalSSRC();
    }

    public synchronized Vector<ReceiveStream> getReceiveStreams(StreamRTPManager streamRTPManager) {
        Vector<ReceiveStream> receiveStreams;
        StreamRTPManagerDesc streamRTPManagerDesc = getStreamRTPManagerDesc(streamRTPManager, false);
        receiveStreams = null;
        if (streamRTPManagerDesc != null) {
            Vector<?> managerReceiveStreams = this.manager.getReceiveStreams();
            if (managerReceiveStreams != null) {
                receiveStreams = new Vector(managerReceiveStreams.size());
                Iterator i$ = managerReceiveStreams.iterator();
                while (i$.hasNext()) {
                    ReceiveStream receiveStream = (ReceiveStream) i$.next();
                    if (streamRTPManagerDesc.containsReceiveSSRC((int) receiveStream.getSSRC())) {
                        receiveStreams.add(receiveStream);
                    }
                }
            }
        }
        return receiveStreams;
    }

    public synchronized Vector<SendStream> getSendStreams(StreamRTPManager streamRTPManager) {
        Vector<SendStream> sendStreams;
        Vector<?> managerSendStreams = this.manager.getSendStreams();
        sendStreams = null;
        if (managerSendStreams != null) {
            sendStreams = new Vector(managerSendStreams.size());
            for (SendStreamDesc sendStreamDesc : this.sendStreams) {
                if (managerSendStreams.contains(sendStreamDesc.sendStream)) {
                    SendStream sendStream = sendStreamDesc.getSendStream(streamRTPManager, false);
                    if (sendStream != null) {
                        sendStreams.add(sendStream);
                    }
                }
            }
        }
        return sendStreams;
    }

    private synchronized StreamRTPManagerDesc getStreamRTPManagerDesc(StreamRTPManager streamRTPManager, boolean create) {
        StreamRTPManagerDesc s;
        for (StreamRTPManagerDesc s2 : this.streamRTPManagers) {
            if (s2.streamRTPManager == streamRTPManager) {
                break;
            }
        }
        if (create) {
            s2 = new StreamRTPManagerDesc(streamRTPManager);
            this.streamRTPManagers.add(s2);
        } else {
            s2 = null;
        }
        return s2;
    }

    public synchronized void initialize(StreamRTPManager streamRTPManager, RTPConnector connector) {
        if (this.connector == null) {
            this.connector = new RTPConnectorImpl();
            this.manager.initialize(this.connector);
        }
        StreamRTPManagerDesc streamRTPManagerDesc = getStreamRTPManagerDesc(streamRTPManager, true);
        RTPConnectorDesc connectorDesc = streamRTPManagerDesc.connectorDesc;
        if (connectorDesc == null || connectorDesc.connector != connector) {
            if (connectorDesc != null) {
                this.connector.removeConnector(connectorDesc);
            }
            connectorDesc = connector == null ? null : new RTPConnectorDesc(streamRTPManagerDesc, connector);
            streamRTPManagerDesc.connectorDesc = connectorDesc;
            if (connectorDesc != null) {
                this.connector.addConnector(connectorDesc);
            }
        }
    }

    /* access modifiers changed from: private|static */
    public static void logRTCP(Object obj, String methodName, byte[] buffer, int offset, int length) {
        if (length >= 8) {
            byte b0 = buffer[offset];
            if (((b0 & 192) >>> 6) == 2 && (buffer[offset + 1] & UnsignedUtils.MAX_UBYTE) == RTCPPacket.BYE) {
                if ((readUnsignedShort(buffer, offset + 2) + 1) * 4 <= length) {
                    int sc = b0 & 31;
                    int off = offset + 4;
                    int i = 0;
                    int end = offset + length;
                    while (i < sc && off + 4 <= end) {
                        logger.trace(obj.getClass().getName() + '.' + methodName + ": RTCP BYE SSRC/CSRC " + Long.toString(((long) readInt(buffer, off)) & 4294967295L));
                        i++;
                        off += 4;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private|declared_synchronized */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x006c  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0052  */
    public synchronized int read(org.jitsi.impl.neomedia.RTPTranslatorImpl.PushSourceStreamDesc r10, byte[] r11, int r12, int r13, int r14) throws java.io.IOException {
        /*
        r9 = this;
        monitor-enter(r9);
        r6 = r10.data;	 Catch:{ all -> 0x0059 }
        r1 = r10.connectorDesc;	 Catch:{ all -> 0x0059 }
        r5 = r1.streamRTPManagerDesc;	 Catch:{ all -> 0x0059 }
        r4 = 0;
        if (r6 == 0) goto L_0x005e;
    L_0x000a:
        r1 = r5.streamRTPManager;	 Catch:{ all -> 0x0059 }
        r1 = r1.getMediaStream();	 Catch:{ all -> 0x0059 }
        r1 = r1.getDirection();	 Catch:{ all -> 0x0059 }
        r1 = r1.allowsReceiving();	 Catch:{ all -> 0x0059 }
        if (r1 != 0) goto L_0x001c;
    L_0x001a:
        monitor-exit(r9);
        return r14;
    L_0x001c:
        r1 = 12;
        if (r13 < r1) goto L_0x0048;
    L_0x0020:
        r1 = r11[r12];	 Catch:{ all -> 0x0059 }
        r1 = r1 & 192;
        r1 = r1 >>> 6;
        r2 = 2;
        if (r1 != r2) goto L_0x0048;
    L_0x0029:
        r1 = r12 + 8;
        r8 = readInt(r11, r1);	 Catch:{ all -> 0x0059 }
        r1 = r5.containsReceiveSSRC(r8);	 Catch:{ all -> 0x0059 }
        if (r1 != 0) goto L_0x003e;
    L_0x0035:
        r1 = r9.findStreamRTPManagerDescByReceiveSSRC(r8, r5);	 Catch:{ all -> 0x0059 }
        if (r1 != 0) goto L_0x005c;
    L_0x003b:
        r5.addReceiveSSRC(r8);	 Catch:{ all -> 0x0059 }
    L_0x003e:
        r1 = r12 + 1;
        r1 = r11[r1];	 Catch:{ all -> 0x0059 }
        r7 = r1 & 127;
        r4 = r5.getFormat(r7);	 Catch:{ all -> 0x0059 }
    L_0x0048:
        if (r6 == 0) goto L_0x006c;
    L_0x004a:
        r1 = r9.connector;	 Catch:{ all -> 0x0059 }
        r0 = r1.getDataOutputStream();	 Catch:{ all -> 0x0059 }
    L_0x0050:
        if (r0 == 0) goto L_0x001a;
    L_0x0052:
        r1 = r11;
        r2 = r12;
        r3 = r14;
        r0.write(r1, r2, r3, r4, r5);	 Catch:{ all -> 0x0059 }
        goto L_0x001a;
    L_0x0059:
        r1 = move-exception;
        monitor-exit(r9);
        throw r1;
    L_0x005c:
        r14 = 0;
        goto L_0x001a;
    L_0x005e:
        r1 = logger;	 Catch:{ all -> 0x0059 }
        r1 = r1.isTraceEnabled();	 Catch:{ all -> 0x0059 }
        if (r1 == 0) goto L_0x0048;
    L_0x0066:
        r1 = "read";
        logRTCP(r9, r1, r11, r12, r14);	 Catch:{ all -> 0x0059 }
        goto L_0x0048;
    L_0x006c:
        r1 = r9.connector;	 Catch:{ all -> 0x0059 }
        r0 = r1.getControlOutputStream();	 Catch:{ all -> 0x0059 }
        goto L_0x0050;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.RTPTranslatorImpl.read(org.jitsi.impl.neomedia.RTPTranslatorImpl$PushSourceStreamDesc, byte[], int, int, int):int");
    }

    public static int readInt(byte[] buf, int off) {
        int off2 = off + 1;
        off = off2 + 1;
        return ((((buf[off] & UnsignedUtils.MAX_UBYTE) << 24) | ((buf[off2] & UnsignedUtils.MAX_UBYTE) << 16)) | ((buf[off] & UnsignedUtils.MAX_UBYTE) << 8)) | (buf[off + 1] & UnsignedUtils.MAX_UBYTE);
    }

    public static int readUnsignedShort(byte[] buf, int off) {
        return ((buf[off] & UnsignedUtils.MAX_UBYTE) << 8) | (buf[off + 1] & UnsignedUtils.MAX_UBYTE);
    }

    public synchronized void removeReceiveStreamListener(StreamRTPManager streamRTPManager, ReceiveStreamListener listener) {
        StreamRTPManagerDesc streamRTPManagerDesc = getStreamRTPManagerDesc(streamRTPManager, false);
        if (streamRTPManagerDesc != null) {
            streamRTPManagerDesc.removeReceiveStreamListener(listener);
        }
    }

    public void removeRemoteListener(StreamRTPManager streamRTPManager, RemoteListener listener) {
        this.manager.removeRemoteListener(listener);
    }

    public void removeSendStreamListener(StreamRTPManager streamRTPManager, SendStreamListener listener) {
    }

    public void removeSessionListener(StreamRTPManager streamRTPManager, SessionListener listener) {
    }

    public void update(ReceiveStreamEvent event) {
        if (event != null) {
            ReceiveStream receiveStream = event.getReceiveStream();
            if (receiveStream != null) {
                StreamRTPManagerDesc streamRTPManagerDesc = findStreamRTPManagerDescByReceiveSSRC((int) receiveStream.getSSRC(), null);
                if (streamRTPManagerDesc != null) {
                    for (ReceiveStreamListener listener : streamRTPManagerDesc.getReceiveStreamListeners()) {
                        listener.update(event);
                    }
                }
            }
        }
    }
}
