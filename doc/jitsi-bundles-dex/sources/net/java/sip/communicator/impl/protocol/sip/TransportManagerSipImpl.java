package net.java.sip.communicator.impl.protocol.sip;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import net.java.sip.communicator.service.protocol.media.TransportManager;

public class TransportManagerSipImpl extends TransportManager<CallPeerSipImpl> {
    protected TransportManagerSipImpl(CallPeerSipImpl callPeer) {
        super(callPeer);
    }

    /* access modifiers changed from: protected */
    public InetAddress getIntendedDestination(CallPeerSipImpl peer) {
        return ((ProtocolProviderServiceSipImpl) peer.getProtocolProvider()).getIntendedDestination(peer.getPeerAddress()).getAddress();
    }

    public String getICECandidateExtendedType(String streamName) {
        return null;
    }

    public String getICEState() {
        return null;
    }

    public InetSocketAddress getICELocalHostAddress(String streamName) {
        return null;
    }

    public InetSocketAddress getICERemoteHostAddress(String streamName) {
        return null;
    }

    public InetSocketAddress getICELocalReflexiveAddress(String streamName) {
        return null;
    }

    public InetSocketAddress getICERemoteReflexiveAddress(String streamName) {
        return null;
    }

    public InetSocketAddress getICELocalRelayedAddress(String streamName) {
        return null;
    }

    public InetSocketAddress getICERemoteRelayedAddress(String streamName) {
        return null;
    }

    public long getTotalHarvestingTime() {
        return 0;
    }

    public long getHarvestingTime(String harvesterName) {
        return 0;
    }

    public int getNbHarvesting() {
        return 0;
    }

    public int getNbHarvesting(String harvesterName) {
        return 0;
    }
}
