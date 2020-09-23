package org.jitsi.impl.neomedia.portaudio;

import java.nio.ByteBuffer;

public interface PortAudioStreamCallback {
    public static final int RESULT_ABORT = 2;
    public static final int RESULT_COMPLETE = 1;
    public static final int RESULT_CONTINUE = 0;

    int callback(ByteBuffer byteBuffer, ByteBuffer byteBuffer2);

    void finishedCallback();
}
