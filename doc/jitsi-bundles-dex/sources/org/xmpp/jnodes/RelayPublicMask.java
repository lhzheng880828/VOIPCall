package org.xmpp.jnodes;

import java.net.InetSocketAddress;
import org.xmpp.jnodes.nio.PublicIPResolver;

public class RelayPublicMask {
    private InetSocketAddress addressA;
    private InetSocketAddress addressA_;
    private InetSocketAddress addressB;
    private InetSocketAddress addressB_;
    private final RelayChannel channel;

    public RelayPublicMask(RelayChannel channel) {
        this.channel = channel;
    }

    public void discover(String stunServer, int port) {
        this.addressA = PublicIPResolver.getPublicAddress(this.channel.getChannelA(), stunServer, port);
        this.addressA_ = PublicIPResolver.getPublicAddress(this.channel.getChannelA_(), stunServer, port);
        this.addressB = PublicIPResolver.getPublicAddress(this.channel.getChannelB(), stunServer, port);
        this.addressB_ = PublicIPResolver.getPublicAddress(this.channel.getChannelB_(), stunServer, port);
    }

    public InetSocketAddress getAddressA() {
        return this.addressA;
    }

    public InetSocketAddress getAddressB() {
        return this.addressB;
    }

    public InetSocketAddress getAddressA_() {
        return this.addressA_;
    }

    public InetSocketAddress getAddressB_() {
        return this.addressB_;
    }
}
