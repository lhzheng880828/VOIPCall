package org.jitsi.service.neomedia;

import org.jitsi.impl.neomedia.AbstractRTPConnector;
import org.jitsi.service.neomedia.event.SrtpListener;

public interface SrtpControl {
    public static final String RTP_SAVP = "RTP/SAVP";
    public static final String RTP_SAVPF = "RTP/SAVPF";

    public interface TransformEngine extends org.jitsi.impl.neomedia.transform.TransformEngine {
        void cleanup();
    }

    void cleanup();

    boolean getSecureCommunicationStatus();

    SrtpControlType getSrtpControlType();

    SrtpListener getSrtpListener();

    TransformEngine getTransformEngine();

    boolean requiresSecureSignalingTransport();

    void setConnector(AbstractRTPConnector abstractRTPConnector);

    void setMasterSession(boolean z);

    void setMultistream(SrtpControl srtpControl);

    void setSrtpListener(SrtpListener srtpListener);

    void start(MediaType mediaType);
}
