package org.jitsi.impl.neomedia;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.media.control.PacketQueueControl;
import javax.media.format.VideoFormat;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.rtp.GlobalReceptionStats;
import javax.media.rtp.ReceiveStream;
import net.sf.fmj.media.rtp.RTCPFeedback;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.impl.neomedia.device.MediaDeviceSession;
import org.jitsi.impl.neomedia.device.VideoMediaDeviceSession;
import org.jitsi.impl.neomedia.portaudio.Pa;
import org.jitsi.service.neomedia.MediaStreamStats;
import org.jitsi.service.neomedia.MediaStreamTarget;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.control.FECDecoderControl;
import org.jitsi.service.neomedia.format.MediaFormat;
import org.jitsi.util.Logger;

public class MediaStreamStatsImpl implements MediaStreamStats {
    private static final Logger logger = Logger.getLogger(MediaStreamStatsImpl.class);
    private double[] jitterRTPTimestampUnits = new double[]{Pa.LATENCY_UNSPECIFIED, Pa.LATENCY_UNSPECIFIED};
    private final MediaStreamImpl mediaStreamImpl;
    private long[] nbByte = new long[]{0, 0};
    private long nbDiscarded = 0;
    private long nbFec = 0;
    private long[] nbLost = new long[]{0, 0};
    private long[] nbPackets = new long[]{0, 0};
    private double percentDiscarded = Pa.LATENCY_UNSPECIFIED;
    private double[] percentLoss = new double[]{Pa.LATENCY_UNSPECIFIED, Pa.LATENCY_UNSPECIFIED};
    private double[] rateKiloBitPerSec = new double[]{Pa.LATENCY_UNSPECIFIED, Pa.LATENCY_UNSPECIFIED};
    private long rttMs = -1;
    private long updateTimeMs = System.currentTimeMillis();
    private long uploadFeedbackNbPackets = 0;

    private enum StreamDirection {
        DOWNLOAD,
        UPLOAD
    }

    public MediaStreamStatsImpl(MediaStreamImpl mediaStreamImpl) {
        this.mediaStreamImpl = mediaStreamImpl;
    }

    public void updateStats() {
        long currentTimeMs = System.currentTimeMillis();
        updateStreamDirectionStats(StreamDirection.DOWNLOAD, currentTimeMs);
        updateStreamDirectionStats(StreamDirection.UPLOAD, currentTimeMs);
        this.updateTimeMs = currentTimeMs;
    }

    private void updateStreamDirectionStats(StreamDirection streamDirection, long currentTimeMs) {
        int streamDirectionIndex = streamDirection.ordinal();
        long newNbRecv = getNbPDU(streamDirection);
        long newNbByte = getNbBytes(streamDirection);
        long nbSteps = newNbRecv - this.nbPackets[streamDirectionIndex];
        if (nbSteps == 0) {
            nbSteps = (currentTimeMs - this.updateTimeMs) / 20;
        }
        if (streamDirection == StreamDirection.DOWNLOAD) {
            long newNbLost = getDownloadNbPDULost() - this.nbLost[streamDirectionIndex];
            updateNbLoss(streamDirection, newNbLost, nbSteps + newNbLost);
            long newNbDiscarded = getNbDiscarded() - this.nbDiscarded;
            updateNbDiscarded(newNbDiscarded, nbSteps + newNbDiscarded);
        }
        long j = nbSteps;
        this.rateKiloBitPerSec[streamDirectionIndex] = computeEWMA(j, this.rateKiloBitPerSec[streamDirectionIndex], computeRateKiloBitPerSec(newNbByte - this.nbByte[streamDirectionIndex], currentTimeMs - this.updateTimeMs));
        this.nbPackets[streamDirectionIndex] = newNbRecv;
        this.nbByte[streamDirectionIndex] = newNbByte;
        updateNbFec();
    }

    public String getLocalIPAddress() {
        InetSocketAddress mediaStreamLocalDataAddress = this.mediaStreamImpl.getLocalDataAddress();
        return mediaStreamLocalDataAddress == null ? null : mediaStreamLocalDataAddress.getAddress().getHostAddress();
    }

    public int getLocalPort() {
        InetSocketAddress mediaStreamLocalDataAddress = this.mediaStreamImpl.getLocalDataAddress();
        return mediaStreamLocalDataAddress == null ? -1 : mediaStreamLocalDataAddress.getPort();
    }

    public String getRemoteIPAddress() {
        MediaStreamTarget mediaStreamTarget = this.mediaStreamImpl.getTarget();
        return mediaStreamTarget == null ? null : mediaStreamTarget.getDataAddress().getAddress().getHostAddress();
    }

    public int getRemotePort() {
        MediaStreamTarget mediaStreamTarget = this.mediaStreamImpl.getTarget();
        return mediaStreamTarget == null ? -1 : mediaStreamTarget.getDataAddress().getPort();
    }

    public String getEncoding() {
        MediaFormat format = this.mediaStreamImpl.getFormat();
        return format == null ? null : format.getEncoding();
    }

    public String getEncodingClockRate() {
        MediaFormat format = this.mediaStreamImpl.getFormat();
        return format == null ? null : format.getRealUsedClockRateString();
    }

    private VideoFormat getUploadVideoFormat() {
        MediaDeviceSession deviceSession = this.mediaStreamImpl.getDeviceSession();
        return deviceSession instanceof VideoMediaDeviceSession ? ((VideoMediaDeviceSession) deviceSession).getSentVideoFormat() : null;
    }

    private VideoFormat getDownloadVideoFormat() {
        MediaDeviceSession deviceSession = this.mediaStreamImpl.getDeviceSession();
        return deviceSession instanceof VideoMediaDeviceSession ? ((VideoMediaDeviceSession) deviceSession).getReceivedVideoFormat() : null;
    }

    public Dimension getUploadVideoSize() {
        VideoFormat format = getUploadVideoFormat();
        return format == null ? null : format.getSize();
    }

    public Dimension getDownloadVideoSize() {
        VideoFormat format = getDownloadVideoFormat();
        return format == null ? null : format.getSize();
    }

    public double getDownloadPercentLoss() {
        return this.percentLoss[StreamDirection.DOWNLOAD.ordinal()];
    }

    public double getPercentDiscarded() {
        return this.percentDiscarded;
    }

    public double getUploadPercentLoss() {
        return this.percentLoss[StreamDirection.UPLOAD.ordinal()];
    }

    public double getDownloadRateKiloBitPerSec() {
        return this.rateKiloBitPerSec[StreamDirection.DOWNLOAD.ordinal()];
    }

    public double getUploadRateKiloBitPerSec() {
        return this.rateKiloBitPerSec[StreamDirection.UPLOAD.ordinal()];
    }

    public double getDownloadJitterMs() {
        return getJitterMs(StreamDirection.DOWNLOAD);
    }

    public double getUploadJitterMs() {
        return getJitterMs(StreamDirection.UPLOAD);
    }

    private double getJitterMs(StreamDirection streamDirection) {
        double clockRate;
        MediaFormat format = this.mediaStreamImpl.getFormat();
        if (format == null) {
            if (MediaType.VIDEO.equals(this.mediaStreamImpl.getMediaType())) {
                clockRate = 90000.0d;
            } else {
                clockRate = -1.0d;
            }
        } else {
            clockRate = format.getClockRate();
        }
        if (clockRate <= Pa.LATENCY_UNSPECIFIED) {
            return -1.0d;
        }
        return (this.jitterRTPTimestampUnits[streamDirection.ordinal()] / clockRate) * 1000.0d;
    }

    private void updateJitterRTPTimestampUnits(RTCPFeedback feedback, StreamDirection streamDirection) {
        this.jitterRTPTimestampUnits[streamDirection.ordinal()] = (double) feedback.getJitter();
    }

    public void updateNewSentFeedback(RTCPFeedback feedback) {
        updateJitterRTPTimestampUnits(feedback, StreamDirection.DOWNLOAD);
    }

    public void updateNewReceivedFeedback(RTCPFeedback feedback) {
        StreamDirection streamDirection = StreamDirection.UPLOAD;
        updateJitterRTPTimestampUnits(feedback, streamDirection);
        long uploadNewNbRecv = feedback.getXtndSeqNum();
        updateNbLoss(streamDirection, feedback.getNumLost() - this.nbLost[streamDirection.ordinal()], uploadNewNbRecv - this.uploadFeedbackNbPackets);
        this.uploadFeedbackNbPackets = uploadNewNbRecv;
        this.rttMs = computeRTTInMs(feedback);
    }

    private void updateNbLoss(StreamDirection streamDirection, long newNbLost, long nbSteps) {
        int streamDirectionIndex = streamDirection.ordinal();
        double newPercentLoss = computePercentLoss(nbSteps, newNbLost);
        this.percentLoss[streamDirectionIndex] = computeEWMA(nbSteps, this.percentLoss[streamDirectionIndex], newPercentLoss);
        long[] jArr = this.nbLost;
        jArr[streamDirectionIndex] = jArr[streamDirectionIndex] + newNbLost;
    }

    private static double computePercentLoss(long nbLostAndRecv, long nbLost) {
        if (nbLostAndRecv == 0) {
            return Pa.LATENCY_UNSPECIFIED;
        }
        return (100.0d * ((double) nbLost)) / ((double) nbLostAndRecv);
    }

    private static double computeRateKiloBitPerSec(long nbByteRecv, long callNbTimeMsSpent) {
        if (nbByteRecv == 0) {
            return Pa.LATENCY_UNSPECIFIED;
        }
        return ((((double) nbByteRecv) * 8.0d) / 1000.0d) / (((double) callNbTimeMsSpent) / 1000.0d);
    }

    private static double computeEWMA(long nbStepSinceLastUpdate, double lastValue, double newValue) {
        double EWMACoeff = 0.01d * ((double) nbStepSinceLastUpdate);
        if (EWMACoeff > 1.0d) {
            EWMACoeff = 1.0d;
        }
        return ((1.0d - EWMACoeff) * lastValue) + (newValue * EWMACoeff);
    }

    private long getNbPDU(StreamDirection streamDirection) {
        StreamRTPManager rtpManager = this.mediaStreamImpl.queryRTPManager();
        if (rtpManager == null) {
            return 0;
        }
        switch (streamDirection) {
            case UPLOAD:
                return (long) rtpManager.getGlobalTransmissionStats().getRTPSent();
            case DOWNLOAD:
                GlobalReceptionStats globalReceptionStats = rtpManager.getGlobalReceptionStats();
                return (long) (globalReceptionStats.getPacketsRecd() - globalReceptionStats.getRTCPRecd());
            default:
                return 0;
        }
    }

    private long getDownloadNbPDULost() {
        MediaDeviceSession devSession = this.mediaStreamImpl.getDeviceSession();
        int nbLost = 0;
        if (devSession != null) {
            for (ReceiveStream receiveStream : devSession.getReceiveStreams()) {
                nbLost += receiveStream.getSourceReceptionStats().getPDUlost();
            }
        }
        return (long) nbLost;
    }

    public long getNbDiscarded() {
        int nbDiscarded = 0;
        for (PacketQueueControl pqc : getPacketQueueControls()) {
            nbDiscarded = pqc.getDiscarded();
        }
        return (long) nbDiscarded;
    }

    public int getNbDiscardedShrink() {
        int nbDiscardedShrink = 0;
        for (PacketQueueControl pqc : getPacketQueueControls()) {
            nbDiscardedShrink = pqc.getDiscardedShrink();
        }
        return nbDiscardedShrink;
    }

    public int getNbDiscardedFull() {
        int nbDiscardedFull = 0;
        for (PacketQueueControl pqc : getPacketQueueControls()) {
            nbDiscardedFull = pqc.getDiscardedFull();
        }
        return nbDiscardedFull;
    }

    public int getNbDiscardedLate() {
        int nbDiscardedLate = 0;
        for (PacketQueueControl pqc : getPacketQueueControls()) {
            nbDiscardedLate = pqc.getDiscardedLate();
        }
        return nbDiscardedLate;
    }

    public int getNbDiscardedReset() {
        int nbDiscardedReset = 0;
        for (PacketQueueControl pqc : getPacketQueueControls()) {
            nbDiscardedReset = pqc.getDiscardedReset();
        }
        return nbDiscardedReset;
    }

    private long getNbBytes(StreamDirection streamDirection) {
        StreamRTPManager rtpManager = this.mediaStreamImpl.queryRTPManager();
        if (rtpManager == null) {
            return 0;
        }
        switch (streamDirection) {
            case UPLOAD:
                return (long) rtpManager.getGlobalTransmissionStats().getBytesSent();
            case DOWNLOAD:
                return (long) rtpManager.getGlobalReceptionStats().getBytesRecd();
            default:
                return 0;
        }
    }

    private long computeRTTInMs(RTCPFeedback feedback) {
        long currentTime = System.currentTimeMillis();
        long DLSR = feedback.getDLSR();
        long LSR = feedback.getLSR();
        if (DLSR == 0 || LSR == 0) {
            return -1;
        }
        long LSRs = LSR >> 16;
        long LSRms = ((65535 & LSR) * 1000) / 65535;
        long DLSRs = DLSR / 65535;
        long DLSRms = ((65535 & DLSR) * 1000) / 65535;
        long currentTimeS = (currentTime / 1000) & 65535;
        long currentTimeMs = currentTime % 1000;
        long rttS = (currentTimeS - DLSRs) - LSRs;
        long rttMs = (currentTimeMs - DLSRms) - LSRms;
        long computedRTTms = (1000 * rttS) + rttMs;
        if (computedRTTms <= 60000 || !logger.isInfoEnabled()) {
            return computedRTTms;
        }
        logger.info("RTT computation seems to be wrong (" + computedRTTms + "> 60 seconds):" + "\n\tcurrentTime: " + currentTime + " (" + Long.toHexString(currentTime) + ")" + "\n\tDLSR: " + DLSR + " (" + Long.toHexString(DLSR) + ")" + "\n\tLSR: " + LSR + " (" + Long.toHexString(LSR) + ")" + "\n\n\tcurrentTimeS: " + currentTimeS + " (" + Long.toHexString(currentTimeS) + ")" + "\n\tDLSRs: " + DLSRs + " (" + Long.toHexString(DLSRs) + ")" + "\n\tLSRs: " + LSRs + " (" + Long.toHexString(LSRs) + ")" + "\n\trttS: " + rttS + " (" + Long.toHexString(rttS) + ")" + "\n\n\tcurrentTimeMs: " + currentTimeMs + " (" + Long.toHexString(currentTimeMs) + ")" + "\n\tDLSRms: " + DLSRms + " (" + Long.toHexString(DLSRms) + ")" + "\n\tLSRms: " + LSRms + " (" + Long.toHexString(LSRms) + ")" + "\n\trttMs: " + rttMs + " (" + Long.toHexString(rttMs) + ")");
        return computedRTTms;
    }

    public long getRttMs() {
        return this.rttMs;
    }

    public long getNbFec() {
        return this.nbFec;
    }

    private void updateNbFec() {
        MediaDeviceSession devSession = this.mediaStreamImpl.getDeviceSession();
        int nbFec = 0;
        if (devSession != null) {
            for (ReceiveStream receiveStream : devSession.getReceiveStreams()) {
                for (FECDecoderControl fecDecoderControl : devSession.getDecoderControls(receiveStream, FECDecoderControl.class)) {
                    nbFec += fecDecoderControl.fecPacketsDecoded();
                }
            }
        }
        this.nbFec = (long) nbFec;
    }

    private void updateNbDiscarded(long newNbDiscarded, long nbSteps) {
        long j = nbSteps;
        this.percentDiscarded = computeEWMA(j, this.percentDiscarded, computePercentLoss(nbSteps, newNbDiscarded));
        this.nbDiscarded += newNbDiscarded;
    }

    public boolean isAdaptiveBufferEnabled() {
        for (PacketQueueControl pcq : getPacketQueueControls()) {
            if (pcq.isAdaptiveBufferEnabled()) {
                return true;
            }
        }
        return false;
    }

    public int getJitterBufferDelayPackets() {
        int delay = 0;
        for (PacketQueueControl pqc : getPacketQueueControls()) {
            if (pqc.getCurrentDelayPackets() > delay) {
                delay = pqc.getCurrentDelayPackets();
            }
        }
        return delay;
    }

    public int getJitterBufferDelayMs() {
        int delay = 0;
        for (PacketQueueControl pqc : getPacketQueueControls()) {
            if (pqc.getCurrentDelayMs() > delay) {
                delay = pqc.getCurrentDelayMs();
            }
        }
        return delay;
    }

    public int getPacketQueueSize() {
        Iterator i$ = getPacketQueueControls().iterator();
        if (i$.hasNext()) {
            return ((PacketQueueControl) i$.next()).getCurrentSizePackets();
        }
        return 0;
    }

    public int getPacketQueueCountPackets() {
        Iterator i$ = getPacketQueueControls().iterator();
        if (i$.hasNext()) {
            return ((PacketQueueControl) i$.next()).getCurrentPacketCount();
        }
        return 0;
    }

    private Set<PacketQueueControl> getPacketQueueControls() {
        Set<PacketQueueControl> set = new HashSet();
        if (this.mediaStreamImpl.isStarted()) {
            MediaDeviceSession devSession = this.mediaStreamImpl.getDeviceSession();
            if (devSession != null) {
                for (ReceiveStream receiveStream : devSession.getReceiveStreams()) {
                    DataSource ds = receiveStream.getDataSource();
                    if (ds instanceof net.sf.fmj.media.protocol.rtp.DataSource) {
                        for (PushBufferStream pbs : ((net.sf.fmj.media.protocol.rtp.DataSource) ds).getStreams()) {
                            PacketQueueControl pqc = (PacketQueueControl) pbs.getControl(PacketQueueControl.class.getName());
                            if (pqc != null) {
                                set.add(pqc);
                            }
                        }
                    }
                }
            }
        }
        return set;
    }
}
