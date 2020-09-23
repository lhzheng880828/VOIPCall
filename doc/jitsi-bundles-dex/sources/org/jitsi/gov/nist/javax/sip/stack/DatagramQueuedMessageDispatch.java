package org.jitsi.gov.nist.javax.sip.stack;

import java.net.DatagramPacket;

public class DatagramQueuedMessageDispatch implements QueuedMessageDispatchBase {
    public DatagramPacket packet;
    long time;

    public DatagramQueuedMessageDispatch(DatagramPacket packet, long time) {
        this.time = time;
        this.packet = packet;
    }

    public long getReceptionTime() {
        return this.time;
    }

    public void run() {
    }
}
