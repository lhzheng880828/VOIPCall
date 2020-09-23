package net.sf.fmj.media.rtp;

import javax.media.Buffer;

interface JitterBufferBehaviour {
    void dropPkt();

    boolean isAdaptive();

    boolean preAdd(Buffer buffer, RTPRawReceiver rTPRawReceiver);

    void read(Buffer buffer);

    void reset();

    boolean willReadBlock();
}
