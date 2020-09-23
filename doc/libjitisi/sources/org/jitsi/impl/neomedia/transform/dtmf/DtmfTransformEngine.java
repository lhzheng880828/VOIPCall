package org.jitsi.impl.neomedia.transform.dtmf;

import java.util.Vector;
import org.jitsi.impl.neomedia.AudioMediaStreamImpl;
import org.jitsi.impl.neomedia.RawPacket;
import org.jitsi.impl.neomedia.portaudio.Pa;
import org.jitsi.impl.neomedia.transform.PacketTransformer;
import org.jitsi.impl.neomedia.transform.SinglePacketTransformer;
import org.jitsi.impl.neomedia.transform.TransformEngine;
import org.jitsi.service.neomedia.DTMFRtpTone;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.codec.Constants;
import org.jitsi.service.neomedia.format.MediaFormat;

public class DtmfTransformEngine extends SinglePacketTransformer implements TransformEngine {
    /* access modifiers changed from: private|static|final */
    public static final DTMFRtpTone[] supportedTones = new DTMFRtpTone[]{DTMFRtpTone.DTMF_0, DTMFRtpTone.DTMF_1, DTMFRtpTone.DTMF_2, DTMFRtpTone.DTMF_3, DTMFRtpTone.DTMF_4, DTMFRtpTone.DTMF_5, DTMFRtpTone.DTMF_6, DTMFRtpTone.DTMF_7, DTMFRtpTone.DTMF_8, DTMFRtpTone.DTMF_9, DTMFRtpTone.DTMF_A, DTMFRtpTone.DTMF_B, DTMFRtpTone.DTMF_C, DTMFRtpTone.DTMF_D, DTMFRtpTone.DTMF_SHARP, DTMFRtpTone.DTMF_STAR};
    private int currentDuration = 0;
    private int currentSpacingDuration = -1;
    private long currentTimestamp = 0;
    private Vector<DTMFRtpTone> currentTone = new Vector(1, 1);
    private DTMFDispatcher dtmfDispatcher = null;
    private boolean lastMinimalDuration = false;
    private int maximalToneDuration;
    /* access modifiers changed from: private|final */
    public final AudioMediaStreamImpl mediaStream;
    private int minimalToneDuration;
    private int nbToneToStop = 0;
    private int remainingsEndPackets = 0;
    private Object startStopToneMutex = new Object();
    private ToneTransmissionState toneTransmissionState = ToneTransmissionState.IDLE;
    private int volume;

    private class DTMFDispatcher implements Runnable {
        private boolean isRunning;
        private DTMFRtpTone lastReceivedTone;
        private DTMFRtpTone lastReportedTone;
        private boolean toEnd;

        private DTMFDispatcher() {
            this.isRunning = false;
            this.lastReceivedTone = null;
            this.lastReportedTone = null;
            this.toEnd = false;
        }

        public void run() {
            this.isRunning = true;
            while (this.isRunning) {
                DTMFRtpTone temp;
                synchronized (this) {
                    if (this.lastReceivedTone == null) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                        }
                    }
                    temp = this.lastReceivedTone;
                    this.lastReceivedTone = null;
                }
                if (temp != null && (((this.lastReportedTone == null && !this.toEnd) || (this.lastReportedTone != null && this.toEnd)) && DtmfTransformEngine.this.mediaStream != null)) {
                    DtmfTransformEngine.this.mediaStream.fireDTMFEvent(temp, this.toEnd);
                    if (this.toEnd) {
                        this.lastReportedTone = null;
                    } else {
                        this.lastReportedTone = temp;
                    }
                    this.toEnd = false;
                }
            }
        }

        public void addTonePacket(DtmfRawPacket p) {
            synchronized (this) {
                this.lastReceivedTone = getToneFromPacket(p);
                this.toEnd = p.isEnd();
                notifyAll();
            }
        }

        public void stop() {
            synchronized (this) {
                this.lastReceivedTone = null;
                this.isRunning = false;
                notifyAll();
            }
        }

        private DTMFRtpTone getToneFromPacket(DtmfRawPacket p) {
            for (DTMFRtpTone t : DtmfTransformEngine.supportedTones) {
                if (t.getCode() == p.getCode()) {
                    return t;
                }
            }
            return null;
        }
    }

    private enum ToneTransmissionState {
        IDLE,
        SENDING,
        END_REQUESTED,
        END_SEQUENCE_INITIATED
    }

    public DtmfTransformEngine(AudioMediaStreamImpl stream) {
        this.mediaStream = stream;
    }

    public void close() {
    }

    private int getCurrentSpacingDuration() {
        if (this.currentSpacingDuration == -1) {
            double clockRate;
            MediaFormat format = this.mediaStream.getFormat();
            if (format == null) {
                if (MediaType.VIDEO.equals(this.mediaStream.getMediaType())) {
                    clockRate = 90000.0d;
                } else {
                    clockRate = -1.0d;
                }
            } else {
                clockRate = format.getClockRate();
            }
            if (clockRate > Pa.LATENCY_UNSPECIFIED) {
                this.currentSpacingDuration = ((int) clockRate) / 50;
            }
        }
        return this.currentSpacingDuration;
    }

    public PacketTransformer getRTCPTransformer() {
        return null;
    }

    public PacketTransformer getRTPTransformer() {
        return this;
    }

    public RawPacket reverseTransform(RawPacket pkt) {
        if (this.mediaStream.getDynamicRTPPayloadType(Constants.TELEPHONE_EVENT) != pkt.getPayloadType()) {
            return pkt;
        }
        DtmfRawPacket p = new DtmfRawPacket(pkt);
        if (this.dtmfDispatcher == null) {
            this.dtmfDispatcher = new DTMFDispatcher();
            new Thread(this.dtmfDispatcher).start();
        }
        this.dtmfDispatcher.addTonePacket(p);
        return null;
    }

    public RawPacket transform(RawPacket pkt) {
        if (this.currentTone.isEmpty()) {
            return pkt;
        }
        byte toneCode = ((DTMFRtpTone) this.currentTone.firstElement()).getCode();
        byte currentDtmfPayload = this.mediaStream.getDynamicRTPPayloadType(Constants.TELEPHONE_EVENT);
        if (currentDtmfPayload == (byte) -1) {
            throw new IllegalStateException("Can't send DTMF when no payload type has been negotiated for DTMF events.");
        }
        RawPacket dtmfPkt = new DtmfRawPacket(pkt.getBuffer(), pkt.getOffset(), pkt.getLength(), currentDtmfPayload);
        long audioPacketTimestamp = dtmfPkt.getTimestamp();
        boolean pktEnd = false;
        boolean pktMarker = false;
        int pktDuration = 0;
        checkIfCurrentToneMustBeStopped();
        if (this.toneTransmissionState == ToneTransmissionState.IDLE) {
            this.lastMinimalDuration = false;
            this.currentDuration = 0;
            this.currentDuration += getCurrentSpacingDuration();
            pktDuration = this.currentDuration;
            pktMarker = true;
            this.currentTimestamp = audioPacketTimestamp;
            synchronized (this.startStopToneMutex) {
                this.toneTransmissionState = ToneTransmissionState.SENDING;
            }
        } else if (this.toneTransmissionState == ToneTransmissionState.SENDING || (this.toneTransmissionState == ToneTransmissionState.END_REQUESTED && !this.lastMinimalDuration)) {
            this.currentDuration += getCurrentSpacingDuration();
            pktDuration = this.currentDuration;
            if (this.currentDuration > this.minimalToneDuration) {
                this.lastMinimalDuration = true;
            }
            if (this.maximalToneDuration != -1 && this.currentDuration > this.maximalToneDuration) {
                this.toneTransmissionState = ToneTransmissionState.END_REQUESTED;
            }
            if (this.currentDuration > 65535) {
                pktDuration = 65535;
                this.currentDuration = 0;
                this.currentTimestamp = audioPacketTimestamp;
            }
        } else if (this.toneTransmissionState == ToneTransmissionState.END_REQUESTED) {
            this.currentDuration += getCurrentSpacingDuration();
            pktDuration = this.currentDuration;
            pktEnd = true;
            this.remainingsEndPackets = 2;
            synchronized (this.startStopToneMutex) {
                this.toneTransmissionState = ToneTransmissionState.END_SEQUENCE_INITIATED;
            }
        } else if (this.toneTransmissionState == ToneTransmissionState.END_SEQUENCE_INITIATED) {
            pktEnd = true;
            pktDuration = this.currentDuration;
            this.remainingsEndPackets--;
            if (this.remainingsEndPackets == 0) {
                synchronized (this.startStopToneMutex) {
                    this.toneTransmissionState = ToneTransmissionState.IDLE;
                    this.currentTone.remove(0);
                }
            }
        }
        dtmfPkt.init(toneCode, pktEnd, pktMarker, pktDuration, this.currentTimestamp, this.volume);
        pkt = dtmfPkt;
        RawPacket rawPacket = pkt;
        return pkt;
    }

    public void startSending(DTMFRtpTone tone, int minimalToneDuration, int maximalToneDuration, int volume) {
        synchronized (this.startStopToneMutex) {
            stopSendingDTMF();
            this.currentTone.add(tone);
        }
        this.minimalToneDuration = minimalToneDuration * 8;
        this.maximalToneDuration = maximalToneDuration * 8;
        if (maximalToneDuration == -1) {
            this.maximalToneDuration = -1;
        } else if (this.maximalToneDuration < this.minimalToneDuration) {
            this.maximalToneDuration = this.minimalToneDuration;
        }
        if (volume > 0) {
            this.volume = volume;
        } else {
            this.volume = 0;
        }
    }

    public void stopSendingDTMF() {
        synchronized (this.startStopToneMutex) {
            int stoppingTone = (this.toneTransmissionState == ToneTransmissionState.END_REQUESTED || this.toneTransmissionState == ToneTransmissionState.END_SEQUENCE_INITIATED) ? 1 : 0;
            if (this.currentTone.size() > this.nbToneToStop + stoppingTone) {
                this.nbToneToStop++;
            }
        }
    }

    public void stop() {
        if (this.dtmfDispatcher != null) {
            this.dtmfDispatcher.stop();
        }
    }

    private void checkIfCurrentToneMustBeStopped() {
        synchronized (this.startStopToneMutex) {
            if (this.nbToneToStop > 0 && this.toneTransmissionState == ToneTransmissionState.SENDING) {
                this.nbToneToStop--;
                this.toneTransmissionState = ToneTransmissionState.END_REQUESTED;
            }
        }
    }
}
