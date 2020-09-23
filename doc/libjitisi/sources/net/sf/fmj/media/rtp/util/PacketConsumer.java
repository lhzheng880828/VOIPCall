package net.sf.fmj.media.rtp.util;

import java.io.IOException;

public interface PacketConsumer {
    void closeConsumer();

    String consumerString();

    void sendTo(Packet packet) throws IOException;
}
