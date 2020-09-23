package org.jitsi.impl.neomedia.codec.video.h264;

import com.lti.utils.UnsignedUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.VideoFormat;
import org.jitsi.impl.neomedia.codec.AbstractCodec2;
import org.jitsi.service.neomedia.codec.Constants;
import org.jitsi.service.neomedia.control.KeyFrameControl;
import org.jitsi.util.Logger;

public class DePacketizer extends AbstractCodec2 {
    public static final byte[] NAL_PREFIX = new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 1};
    private static final boolean OUTPUT_INCOMPLETE_NAL_UNITS = true;
    private static final long TIME_BETWEEN_REQUEST_KEY_FRAME = 500;
    private static final long TIME_FROM_KEY_FRAME_TO_REQUEST_KEY_FRAME = 13000;
    private static final int UNSPECIFIED_NAL_UNIT_TYPE = 0;
    private static final Logger logger = Logger.getLogger(DePacketizer.class);
    private boolean fuaStartedAndNotEnded = false;
    private KeyFrameControl keyFrameControl;
    private long lastKeyFrameTime = -1;
    private long lastRequestKeyFrameTime = -1;
    private long lastSequenceNumber = -1;
    private int nal_unit_type;
    private final int outputPaddingSize = 8;
    private boolean requestKeyFrame = false;
    /* access modifiers changed from: private */
    public Thread requestKeyFrameThread;

    public DePacketizer() {
        super("H264 DePacketizer", VideoFormat.class, new VideoFormat[]{new VideoFormat(Constants.H264)});
        List<Format> inputFormats = new ArrayList();
        inputFormats.add(new VideoFormat(Constants.H264_RTP));
        Collections.addAll(inputFormats, Packetizer.SUPPORTED_OUTPUT_FORMATS);
        this.inputFormats = (Format[]) inputFormats.toArray(EMPTY_FORMATS);
    }

    private int dePacketizeFUA(byte[] in, int inOffset, int inLength, Buffer outBuffer) {
        int octet;
        byte fu_indicator = in[inOffset];
        inOffset++;
        inLength--;
        byte fu_header = in[inOffset];
        inOffset++;
        inLength--;
        int nal_unit_type = fu_header & 31;
        this.nal_unit_type = nal_unit_type;
        boolean start_bit = (fu_header & 128) != 0;
        boolean end_bit = (fu_header & 64) != 0;
        int outOffset = outBuffer.getOffset();
        int newOutLength = inLength;
        if (start_bit) {
            if (end_bit) {
                outBuffer.setDiscard(true);
                return 0;
            }
            this.fuaStartedAndNotEnded = true;
            newOutLength += NAL_PREFIX.length + 1;
            octet = (fu_indicator & 224) | nal_unit_type;
        } else if (this.fuaStartedAndNotEnded) {
            int outLength = outBuffer.getLength();
            outOffset += outLength;
            newOutLength += outLength;
            octet = 0;
        } else {
            outBuffer.setDiscard(true);
            return 0;
        }
        byte[] out = AbstractCodec2.validateByteArraySize(outBuffer, (outBuffer.getOffset() + newOutLength) + 8, true);
        if (start_bit) {
            System.arraycopy(NAL_PREFIX, 0, out, outOffset, NAL_PREFIX.length);
            outOffset += NAL_PREFIX.length;
            out[outOffset] = (byte) (octet & UnsignedUtils.MAX_UBYTE);
            outOffset++;
        }
        System.arraycopy(in, inOffset, out, outOffset, inLength);
        padOutput(out, outOffset + inLength);
        outBuffer.setLength(newOutLength);
        if (!end_bit) {
            return 4;
        }
        this.fuaStartedAndNotEnded = false;
        return 0;
    }

    private int dePacketizeSingleNALUnitPacket(int nal_unit_type, byte[] in, int inOffset, int inLength, Buffer outBuffer) {
        this.nal_unit_type = nal_unit_type;
        int outOffset = outBuffer.getOffset();
        int newOutLength = NAL_PREFIX.length + inLength;
        byte[] out = AbstractCodec2.validateByteArraySize(outBuffer, (outOffset + newOutLength) + 8, true);
        System.arraycopy(NAL_PREFIX, 0, out, outOffset, NAL_PREFIX.length);
        outOffset += NAL_PREFIX.length;
        System.arraycopy(in, inOffset, out, outOffset, inLength);
        padOutput(out, outOffset + inLength);
        outBuffer.setLength(newOutLength);
        return 0;
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void doClose() {
        this.requestKeyFrameThread = null;
        notifyAll();
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void doOpen() throws ResourceUnavailableException {
        this.fuaStartedAndNotEnded = false;
        this.lastKeyFrameTime = -1;
        this.lastRequestKeyFrameTime = -1;
        this.lastSequenceNumber = -1;
        this.nal_unit_type = 0;
        this.requestKeyFrame = false;
        this.requestKeyFrameThread = null;
    }

    /* access modifiers changed from: protected */
    public int doProcess(Buffer inBuffer, Buffer outBuffer) {
        int ret;
        long sequenceNumber = inBuffer.getSequenceNumber();
        boolean requestKeyFrame = this.lastKeyFrameTime == -1;
        if (!(this.lastSequenceNumber == -1 || sequenceNumber - this.lastSequenceNumber == 1)) {
            if (logger.isTraceEnabled()) {
                logger.trace("Dropped RTP packets upto sequenceNumber " + this.lastSequenceNumber + " and continuing with sequenceNumber " + sequenceNumber);
            }
            requestKeyFrame = true;
            ret = reset(outBuffer);
            if ((ret & 4) == 0) {
                setRequestKeyFrame(true);
                return ret;
            }
        }
        this.lastSequenceNumber = sequenceNumber;
        byte[] in = (byte[]) inBuffer.getData();
        int inOffset = inBuffer.getOffset();
        int nal_unit_type = in[inOffset] & 31;
        if (nal_unit_type >= 1 && nal_unit_type <= 23) {
            this.fuaStartedAndNotEnded = false;
            ret = dePacketizeSingleNALUnitPacket(nal_unit_type, in, inOffset, inBuffer.getLength(), outBuffer);
        } else if (nal_unit_type == 28) {
            ret = dePacketizeFUA(in, inOffset, inBuffer.getLength(), outBuffer);
            if (outBuffer.isDiscard()) {
                this.fuaStartedAndNotEnded = false;
            }
        } else {
            logger.warn("Dropping NAL unit of unsupported type " + nal_unit_type);
            this.nal_unit_type = nal_unit_type;
            this.fuaStartedAndNotEnded = false;
            outBuffer.setDiscard(true);
            ret = 0;
        }
        outBuffer.setSequenceNumber(sequenceNumber);
        if ((inBuffer.getFlags() & 2048) != 0) {
            outBuffer.setFlags(outBuffer.getFlags() | 2048);
        }
        switch (this.nal_unit_type) {
            case 5:
                this.lastKeyFrameTime = System.currentTimeMillis();
                break;
            case 7:
            case 8:
                break;
        }
        requestKeyFrame = false;
        setRequestKeyFrame(requestKeyFrame);
        int i = ret;
        return ret;
    }

    private void padOutput(byte[] out, int outOffset) {
        Arrays.fill(out, outOffset, outOffset + 8, (byte) 0);
    }

    public synchronized boolean requestKeyFrame(boolean urgent) {
        this.lastKeyFrameTime = -1;
        setRequestKeyFrame(true);
        return true;
    }

    private int reset(Buffer outBuffer) {
        if (this.fuaStartedAndNotEnded && outBuffer.getLength() >= (NAL_PREFIX.length + 1) + 1) {
            Object outData = outBuffer.getData();
            if (outData instanceof byte[]) {
                byte[] out = (byte[]) outData;
                int octetIndex = outBuffer.getOffset() + NAL_PREFIX.length;
                out[octetIndex] = (byte) (out[octetIndex] | 128);
                this.fuaStartedAndNotEnded = false;
                return 2;
            }
        }
        this.fuaStartedAndNotEnded = false;
        outBuffer.setLength(0);
        return 4;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Missing block: B:31:0x004b, code skipped:
            r3 = r18.keyFrameControl;
     */
    /* JADX WARNING: Missing block: B:32:0x004f, code skipped:
            if (r3 == null) goto L_0x006d;
     */
    /* JADX WARNING: Missing block: B:33:0x0051, code skipped:
            r5 = r3.getKeyFrameRequesters();
     */
    /* JADX WARNING: Missing block: B:34:0x0055, code skipped:
            if (r5 == null) goto L_0x006d;
     */
    /* JADX WARNING: Missing block: B:35:0x0057, code skipped:
            r2 = r5.iterator();
     */
    /* JADX WARNING: Missing block: B:37:0x005f, code skipped:
            if (r2.hasNext() == false) goto L_0x006d;
     */
    /* JADX WARNING: Missing block: B:41:0x006b, code skipped:
            if (((org.jitsi.service.neomedia.control.KeyFrameControl.KeyFrameRequester) r2.next()).requestKeyFrame() == false) goto L_0x005b;
     */
    /* JADX WARNING: Missing block: B:42:0x006d, code skipped:
            r18.lastRequestKeyFrameTime = java.lang.System.currentTimeMillis();
     */
    public void runInRequestKeyFrameThread() {
        /*
        r18 = this;
    L_0x0000:
        monitor-enter(r18);
        r0 = r18;
        r14 = r0.requestKeyFrameThread;	 Catch:{ all -> 0x003e }
        r15 = java.lang.Thread.currentThread();	 Catch:{ all -> 0x003e }
        if (r14 == r15) goto L_0x000d;
    L_0x000b:
        monitor-exit(r18);	 Catch:{ all -> 0x003e }
        return;
    L_0x000d:
        r10 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x003e }
        r0 = r18;
        r14 = r0.requestKeyFrame;	 Catch:{ all -> 0x003e }
        if (r14 == 0) goto L_0x0047;
    L_0x0017:
        r0 = r18;
        r14 = r0.lastKeyFrameTime;	 Catch:{ all -> 0x003e }
        r16 = 13000; // 0x32c8 float:1.8217E-41 double:6.423E-320;
        r6 = r14 + r16;
        r14 = (r10 > r6 ? 1 : (r10 == r6 ? 0 : -1));
        if (r14 < 0) goto L_0x0044;
    L_0x0023:
        r0 = r18;
        r14 = r0.lastRequestKeyFrameTime;	 Catch:{ all -> 0x003e }
        r16 = 500; // 0x1f4 float:7.0E-43 double:2.47E-321;
        r8 = r14 + r16;
        r14 = (r10 > r8 ? 1 : (r10 == r8 ? 0 : -1));
        if (r14 < 0) goto L_0x0041;
    L_0x002f:
        r12 = -1;
    L_0x0031:
        r14 = 0;
        r14 = (r12 > r14 ? 1 : (r12 == r14 ? 0 : -1));
        if (r14 < 0) goto L_0x004a;
    L_0x0037:
        r0 = r18;
        r0.wait(r12);	 Catch:{ InterruptedException -> 0x0076 }
    L_0x003c:
        monitor-exit(r18);	 Catch:{ all -> 0x003e }
        goto L_0x0000;
    L_0x003e:
        r14 = move-exception;
        monitor-exit(r18);	 Catch:{ all -> 0x003e }
        throw r14;
    L_0x0041:
        r12 = r8 - r10;
        goto L_0x0031;
    L_0x0044:
        r12 = r6 - r10;
        goto L_0x0031;
    L_0x0047:
        r12 = 0;
        goto L_0x0031;
    L_0x004a:
        monitor-exit(r18);	 Catch:{ all -> 0x003e }
        r0 = r18;
        r3 = r0.keyFrameControl;
        if (r3 == 0) goto L_0x006d;
    L_0x0051:
        r5 = r3.getKeyFrameRequesters();
        if (r5 == 0) goto L_0x006d;
    L_0x0057:
        r2 = r5.iterator();
    L_0x005b:
        r14 = r2.hasNext();
        if (r14 == 0) goto L_0x006d;
    L_0x0061:
        r4 = r2.next();
        r4 = (org.jitsi.service.neomedia.control.KeyFrameControl.KeyFrameRequester) r4;
        r14 = r4.requestKeyFrame();	 Catch:{ Exception -> 0x0078 }
        if (r14 == 0) goto L_0x005b;
    L_0x006d:
        r14 = java.lang.System.currentTimeMillis();
        r0 = r18;
        r0.lastRequestKeyFrameTime = r14;
        goto L_0x0000;
    L_0x0076:
        r14 = move-exception;
        goto L_0x003c;
    L_0x0078:
        r14 = move-exception;
        goto L_0x005b;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.codec.video.h264.DePacketizer.runInRequestKeyFrameThread():void");
    }

    public void setKeyFrameControl(KeyFrameControl keyFrameControl) {
        this.keyFrameControl = keyFrameControl;
    }

    private synchronized void setRequestKeyFrame(boolean requestKeyFrame) {
        if (this.requestKeyFrame != requestKeyFrame) {
            this.requestKeyFrame = requestKeyFrame;
            if (this.requestKeyFrame && this.requestKeyFrameThread == null) {
                this.requestKeyFrameThread = new Thread() {
                    public void run() {
                        try {
                            DePacketizer.this.runInRequestKeyFrameThread();
                            synchronized (DePacketizer.this) {
                                if (DePacketizer.this.requestKeyFrameThread == Thread.currentThread()) {
                                    DePacketizer.this.requestKeyFrameThread = null;
                                }
                            }
                        } catch (Throwable th) {
                            synchronized (DePacketizer.this) {
                                if (DePacketizer.this.requestKeyFrameThread == Thread.currentThread()) {
                                    DePacketizer.this.requestKeyFrameThread = null;
                                }
                            }
                        }
                    }
                };
                this.requestKeyFrameThread.start();
            }
            notifyAll();
        }
    }
}
