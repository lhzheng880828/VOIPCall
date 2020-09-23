package net.sf.fmj.media.rtp.util;

import java.io.IOException;
import java.util.Vector;
import javax.media.rtp.SessionAddress;

public abstract class PacketFilter implements PacketSource, PacketConsumer {
    PacketConsumer consumer;
    public boolean control = false;
    public Vector destAddressList = null;
    public Vector peerlist = null;
    PacketSource source;

    public abstract Packet handlePacket(Packet packet);

    public abstract Packet handlePacket(Packet packet, int i);

    public abstract Packet handlePacket(Packet packet, SessionAddress sessionAddress);

    public void close() {
    }

    public void closeConsumer() {
        close();
        if (this.consumer != null) {
            this.consumer.closeConsumer();
        }
    }

    public void closeSource() {
        close();
        if (this.source != null) {
            this.source.closeSource();
        }
    }

    public String consumerString() {
        if (this.consumer == null) {
            return filtername();
        }
        return filtername() + " connected to " + this.consumer.consumerString();
    }

    public String filtername() {
        return getClass().getName();
    }

    public PacketConsumer getConsumer() {
        return this.consumer;
    }

    public Vector getDestList() {
        return null;
    }

    public PacketSource getSource() {
        return this.source;
    }

    public Packet receiveFrom() throws IOException {
        Packet rawp = this.source.receiveFrom();
        if (rawp != null) {
            return handlePacket(rawp);
        }
        return null;
    }

    public void sendTo(Packet p) throws IOException {
        Packet origpacket = p;
        int i;
        if (this.peerlist != null) {
            p = handlePacket(origpacket);
            for (i = 0; i < this.peerlist.size(); i++) {
                SessionAddress a = (SessionAddress) this.peerlist.elementAt(i);
                if (this.control) {
                    ((UDPPacket) p).remoteAddress = a.getControlAddress();
                    ((UDPPacket) p).remotePort = a.getControlPort();
                } else {
                    ((UDPPacket) p).remoteAddress = a.getDataAddress();
                    ((UDPPacket) p).remotePort = a.getDataPort();
                }
                if (!(p == null || this.consumer == null)) {
                    this.consumer.sendTo(p);
                }
            }
        } else if (this.destAddressList != null) {
            for (i = 0; i < this.destAddressList.size(); i++) {
                p = handlePacket(origpacket, (SessionAddress) this.destAddressList.elementAt(i));
                if (!(p == null || this.consumer == null)) {
                    this.consumer.sendTo(p);
                }
            }
        } else if (this.destAddressList == null) {
            p = handlePacket(p);
            if (p != null && this.consumer != null) {
                this.consumer.sendTo(p);
            }
        }
    }

    public void setConsumer(PacketConsumer c) {
        this.consumer = c;
    }

    public void setSource(PacketSource s) {
        this.source = s;
    }

    public String sourceString() {
        if (this.source == null) {
            return filtername();
        }
        return filtername() + " attached to " + this.source.sourceString();
    }
}
