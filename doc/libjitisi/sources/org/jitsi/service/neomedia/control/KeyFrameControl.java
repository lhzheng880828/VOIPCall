package org.jitsi.service.neomedia.control;

import java.util.List;

public interface KeyFrameControl {

    public interface KeyFrameRequestee {
        boolean keyFrameRequest();
    }

    public interface KeyFrameRequester {
        public static final String DEFAULT_PREFERRED = "rtcp";
        public static final String PREFERRED_PNAME = "net.java.sip.communicator.impl.neomedia.codec.video.h264.preferredKeyFrameRequester";
        public static final String RTCP = "rtcp";
        public static final String SIGNALING = "signaling";

        boolean requestKeyFrame();
    }

    void addKeyFrameRequestee(int i, KeyFrameRequestee keyFrameRequestee);

    void addKeyFrameRequester(int i, KeyFrameRequester keyFrameRequester);

    List<KeyFrameRequestee> getKeyFrameRequestees();

    List<KeyFrameRequester> getKeyFrameRequesters();

    boolean keyFrameRequest();

    boolean removeKeyFrameRequestee(KeyFrameRequestee keyFrameRequestee);

    boolean removeKeyFrameRequester(KeyFrameRequester keyFrameRequester);

    boolean requestKeyFrame(boolean z);
}
