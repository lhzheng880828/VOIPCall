package org.jitsi.service.neomedia.event;

public interface RTCPFeedbackListener {
    void rtcpFeedbackReceived(RTCPFeedbackEvent rTCPFeedbackEvent);
}
