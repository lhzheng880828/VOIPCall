package net.sf.fmj.media.rtp;

import javax.media.Buffer;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.PushBufferStream;
import javax.media.rtp.event.LocalPayloadChangeEvent;
import net.sf.fmj.ejmf.toolkit.util.TimeSource;
import net.sf.fmj.media.rtp.util.RTPMediaThread;

public class RTPSinkStream implements BufferTransferHandler {
    static int LEEWAY = 5;
    static int THRESHOLD = 80;
    static AudioFormat mpegAudio = new AudioFormat(AudioFormat.MPEG_RTP);
    static VideoFormat mpegVideo = new VideoFormat(VideoFormat.MPEG_RTP);
    long audioPT = 0;
    boolean bufSizeSet = false;
    Buffer current = new Buffer();
    SendSSRCInfo info = null;
    boolean mpegBFrame = false;
    boolean mpegPFrame = false;
    int rate;
    RTPRawSender sender = null;
    long startPT = -1;
    Object startReq = new Integer(0);
    long startTime = 0;
    boolean started = false;
    private RTPMediaThread thread = null;
    RTPTransmitter transmitter = null;

    /* access modifiers changed from: protected */
    public void close() {
        stop();
    }

    /* access modifiers changed from: protected */
    public void setSSRCInfo(SendSSRCInfo info) {
        this.info = info;
    }

    /* access modifiers changed from: protected */
    public void setTransmitter(RTPTransmitter t) {
        this.transmitter = t;
        if (this.transmitter != null) {
            this.sender = this.transmitter.getSender();
        }
    }

    public void start() {
        if (!this.started) {
            this.started = true;
            synchronized (this.startReq) {
                this.startReq.notifyAll();
            }
        }
    }

    public void startStream() {
    }

    public void stop() {
        this.started = false;
        this.startPT = -1;
        synchronized (this.startReq) {
            this.startReq.notifyAll();
        }
    }

    public void transferData(PushBufferStream stream) {
        try {
            synchronized (this.startReq) {
                while (!this.started) {
                    this.startPT = -1;
                    this.startReq.wait();
                }
            }
            stream.read(this.current);
            if (!this.current.getFormat().matches(this.info.myformat)) {
                int payload = this.transmitter.cache.sm.formatinfo.getPayload(this.current.getFormat());
                if (payload != -1) {
                    this.transmitter.cache.eventhandler.postEvent(new LocalPayloadChangeEvent(this.transmitter.cache.sm, this.info, this.info.payloadType, payload));
                    this.info.payloadType = payload;
                    this.info.myformat = this.current.getFormat();
                } else {
                    return;
                }
            }
            if (this.info.myformat instanceof VideoFormat) {
                transmitVideo();
            } else if (this.info.myformat instanceof AudioFormat) {
                transmitAudio();
            }
        } catch (Exception e) {
        }
    }

    private void transmitAudio() {
        long j = 0;
        if (this.current.isEOM() || this.current.isDiscard()) {
            this.startPT = -1;
            return;
        }
        if (this.startPT == -1) {
            this.startTime = System.currentTimeMillis();
            if (this.current.getTimeStamp() > 0) {
                j = this.current.getTimeStamp() / TimeSource.MICROS_PER_SEC;
            }
            this.startPT = j;
            this.audioPT = this.startPT;
        }
        if ((this.current.getFlags() & 96) == 0) {
            if (mpegAudio.matches(this.current.getFormat())) {
                this.audioPT = this.current.getTimeStamp() / TimeSource.MICROS_PER_SEC;
            } else {
                this.audioPT = (((AudioFormat) this.info.myformat).computeDuration((long) this.current.getLength()) / TimeSource.MICROS_PER_SEC) + this.audioPT;
            }
            waitForPT(this.startTime, this.startPT, this.audioPT);
        }
        this.transmitter.TransmitPacket(this.current, this.info);
    }

    private void transmitVideo() {
        if (this.current.isEOM() || this.current.isDiscard()) {
            this.startPT = -1;
            this.mpegBFrame = false;
            this.mpegPFrame = false;
            return;
        }
        if (this.startPT == -1) {
            this.startTime = System.currentTimeMillis();
            this.startPT = this.current.getTimeStamp() / TimeSource.MICROS_PER_SEC;
        }
        if (this.current.getTimeStamp() > 0 && (this.current.getFlags() & 96) == 0 && (this.current.getFlags() & 2048) != 0) {
            if (mpegVideo.matches(this.info.myformat)) {
                int ptype = ((byte[]) this.current.getData())[this.current.getOffset() + 2] & 7;
                if (ptype > 2) {
                    this.mpegBFrame = true;
                } else if (ptype == 2) {
                    this.mpegPFrame = true;
                }
                if (ptype > 2 || ((ptype == 2 && !this.mpegBFrame) || (ptype == 1 && (this.mpegBFrame | this.mpegPFrame) == 0))) {
                    waitForPT(this.startTime, this.startPT, this.current.getTimeStamp() / TimeSource.MICROS_PER_SEC);
                }
            } else {
                waitForPT(this.startTime, this.startPT, this.current.getTimeStamp() / TimeSource.MICROS_PER_SEC);
            }
        }
        this.transmitter.TransmitPacket(this.current, this.info);
    }

    private void waitForPT(long start, long startPT, long pt) {
        long j = pt - startPT;
        long currentTimeMillis = System.currentTimeMillis();
        while (true) {
            long delay = j - (currentTimeMillis - start);
            if (delay > ((long) LEEWAY)) {
                if (delay > ((long) THRESHOLD)) {
                    delay = (long) THRESHOLD;
                }
                try {
                    Thread.currentThread();
                    Thread.sleep(delay);
                    j = pt - startPT;
                    currentTimeMillis = System.currentTimeMillis();
                } catch (Exception e) {
                    return;
                }
            }
            return;
        }
    }
}
