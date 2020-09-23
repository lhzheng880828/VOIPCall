package org.jitsi.service.neomedia.event;

import java.util.EventObject;

public class RTCPFeedbackEvent extends EventObject {
    public static final int FMT_FIR = 4;
    public static final int FMT_PLI = 1;
    public static final int PT_PS = 206;
    public static final int PT_TL = 205;
    private static final long serialVersionUID = 0;
    private final int feedbackMessageType;
    private final int payloadType;

    public RTCPFeedbackEvent(Object src, int feedbackMessageType, int payloadType) {
        super(src);
        this.feedbackMessageType = feedbackMessageType;
        this.payloadType = payloadType;
    }

    public int getFeedbackMessageType() {
        return this.feedbackMessageType;
    }

    public int getPayloadType() {
        return this.payloadType;
    }
}
