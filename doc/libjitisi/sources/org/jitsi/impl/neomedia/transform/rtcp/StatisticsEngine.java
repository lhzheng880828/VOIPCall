package org.jitsi.impl.neomedia.transform.rtcp;

import java.util.List;
import net.sf.fmj.media.rtp.RTCPFeedback;
import net.sf.fmj.media.rtp.RTCPHeader;
import net.sf.fmj.media.rtp.RTCPSenderReport;
import org.jitsi.impl.neomedia.MediaStreamImpl;
import org.jitsi.impl.neomedia.RawPacket;
import org.jitsi.impl.neomedia.transform.PacketTransformer;
import org.jitsi.impl.neomedia.transform.SinglePacketTransformer;
import org.jitsi.impl.neomedia.transform.TransformEngine;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.util.Logger;

public class StatisticsEngine extends SinglePacketTransformer implements TransformEngine {
    public static final String RTP_STAT_PREFIX = "rtpstat:";
    private static final Logger logger = Logger.getLogger(StatisticsEngine.class);
    private long lost = 0;
    private long maxInterArrivalJitter = 0;
    private final MediaStreamImpl mediaStream;
    private long minInterArrivalJitter = -1;
    private long numberOfSenderReports = 0;

    private static boolean isRTCP(RawPacket pkt) {
        int len = pkt.getLength();
        if (len < 4) {
            return false;
        }
        byte[] buf = pkt.getBuffer();
        int off = pkt.getOffset();
        if (((buf[off] & 192) >>> 6) != 2 || (buf[off + 2] << 8) + (buf[off + 3] << 0) > len) {
            return false;
        }
        return true;
    }

    public StatisticsEngine(MediaStreamImpl stream) {
        this.mediaStream = stream;
    }

    public void close() {
    }

    public long getLost() {
        return this.lost;
    }

    public long getMaxInterArrivalJitter() {
        return this.maxInterArrivalJitter;
    }

    public long getMinInterArrivalJitter() {
        return this.minInterArrivalJitter;
    }

    public PacketTransformer getRTCPTransformer() {
        return this;
    }

    public PacketTransformer getRTPTransformer() {
        return null;
    }

    public RawPacket reverseTransform(RawPacket pkt) {
        try {
            int length = pkt.getLength();
            if (length != 0) {
                byte[] data = pkt.getBuffer();
                int offset = pkt.getOffset();
                if (new RTCPHeader(data, offset, length).getPacketType() == (short) 200) {
                    List<?> feedbackReports = new RTCPSenderReport(data, offset, length).getFeedbackReports();
                    if (feedbackReports.size() != 0) {
                        this.mediaStream.getMediaStreamStats().updateNewReceivedFeedback((RTCPFeedback) feedbackReports.get(0));
                    }
                }
            }
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                ThreadDeath t2 = (ThreadDeath) t;
            } else {
                logger.error("Failed to analyze an incoming RTCP packet for the purposes of statistics.", t);
            }
        }
        return pkt;
    }

    public RawPacket transform(RawPacket pkt) {
        try {
            if (isRTCP(pkt)) {
                this.numberOfSenderReports++;
                byte[] data = pkt.getBuffer();
                int offset = pkt.getOffset();
                int length = pkt.getLength();
                if (new RTCPHeader(data, offset, length).getPacketType() == (short) 200) {
                    RTCPSenderReport senderReport = new RTCPSenderReport(data, offset, length);
                    List<?> feedbackReports = senderReport.getFeedbackReports();
                    if (feedbackReports.size() != 0) {
                        RTCPFeedback feedback = (RTCPFeedback) feedbackReports.get(0);
                        this.mediaStream.getMediaStreamStats().updateNewSentFeedback(feedback);
                        if (logger.isInfoEnabled()) {
                            long jitter = feedback.getJitter();
                            if (jitter < getMinInterArrivalJitter() || getMinInterArrivalJitter() == -1) {
                                this.minInterArrivalJitter = jitter;
                            }
                            if (getMaxInterArrivalJitter() < jitter) {
                                this.maxInterArrivalJitter = jitter;
                            }
                            this.lost = feedback.getNumLost();
                            if (this.numberOfSenderReports % 4 == 1) {
                                StringBuilder buff = new StringBuilder(RTP_STAT_PREFIX);
                                MediaType mediaType = this.mediaStream.getMediaType();
                                buff.append("Sending a report for ").append(mediaType == null ? "" : mediaType.toString()).append(" stream SSRC:").append(feedback.getSSRC()).append(" [packet count:").append(senderReport.getSenderPacketCount()).append(", bytes:").append(senderReport.getSenderByteCount()).append(", interarrival jitter:").append(jitter).append(", lost packets:").append(feedback.getNumLost()).append(", time since previous report:").append((int) (((double) feedback.getDLSR()) / 65.536d)).append("ms ]");
                                logger.info(buff);
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                ThreadDeath t2 = (ThreadDeath) t;
            } else {
                logger.error("Failed to analyze an outgoing RTCP packet for the purposes of statistics.", t);
            }
        }
        return pkt;
    }
}
