package net.sf.fmj.media.rtp.util;

import java.io.IOException;
import javax.media.rtp.OutputDataStream;
import javax.media.rtp.RTPConnector;
import javax.media.rtp.RTPPushDataSource;

public class RTPPacketSender implements PacketConsumer {
    RTPConnector connector = null;
    RTPPushDataSource dest = null;
    OutputDataStream outstream = null;

    public RTPPacketSender(OutputDataStream os) {
        this.outstream = os;
    }

    public RTPPacketSender(RTPConnector connector) throws IOException {
        this.connector = connector;
        this.outstream = connector.getDataOutputStream();
    }

    public RTPPacketSender(RTPPushDataSource dest) {
        this.dest = dest;
        this.outstream = dest.getInputStream();
    }

    public void closeConsumer() {
    }

    public String consumerString() {
        return "RTPPacketSender for " + this.dest;
    }

    public RTPConnector getConnector() {
        return this.connector;
    }

    public void sendTo(Packet p) throws IOException {
        if (this.outstream == null) {
            throw new IOException();
        }
        this.outstream.write(p.data, 0, p.length);
    }
}
