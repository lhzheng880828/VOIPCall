package org.jitsi.service.neomedia;

import net.sf.fmj.media.rtp.RTCPFeedback;
import org.jitsi.android.util.java.awt.Dimension;

public interface MediaStreamStats {
    double getDownloadJitterMs();

    double getDownloadPercentLoss();

    double getDownloadRateKiloBitPerSec();

    Dimension getDownloadVideoSize();

    String getEncoding();

    String getEncodingClockRate();

    int getJitterBufferDelayMs();

    int getJitterBufferDelayPackets();

    String getLocalIPAddress();

    int getLocalPort();

    long getNbDiscarded();

    int getNbDiscardedFull();

    int getNbDiscardedLate();

    int getNbDiscardedReset();

    int getNbDiscardedShrink();

    long getNbFec();

    int getPacketQueueCountPackets();

    int getPacketQueueSize();

    double getPercentDiscarded();

    String getRemoteIPAddress();

    int getRemotePort();

    long getRttMs();

    double getUploadJitterMs();

    double getUploadPercentLoss();

    double getUploadRateKiloBitPerSec();

    Dimension getUploadVideoSize();

    boolean isAdaptiveBufferEnabled();

    void updateNewReceivedFeedback(RTCPFeedback rTCPFeedback);

    void updateNewSentFeedback(RTCPFeedback rTCPFeedback);

    void updateStats();
}
