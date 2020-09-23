package net.java.sip.communicator.impl.protocol.jabber;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.ColibriConferenceIQ.Channel;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.CandidatePacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.CandidateType;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.IceUdpTransportPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.RawUdpTransportPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.RtpDescriptionPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.jinglesdp.JingleUtils;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import org.jitsi.service.neomedia.MediaStreamTarget;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.StreamConnector;
import org.jivesoftware.smack.packet.PacketExtension;

public class RawUdpTransportManager extends TransportManagerJabberImpl {
    private List<ContentPacketExtension> local;
    private final List<Iterable<ContentPacketExtension>> remotes = new LinkedList();

    public RawUdpTransportManager(CallPeerJabberImpl callPeer) {
        super(callPeer);
    }

    /* access modifiers changed from: protected */
    public PacketExtension createTransport(String media) throws OperationFailedException {
        MediaType mediaType = MediaType.parseString(media);
        return createTransport(mediaType, getStreamConnector(mediaType));
    }

    private RawUdpTransportPacketExtension createTransport(MediaType mediaType, StreamConnector connector) {
        RawUdpTransportPacketExtension ourTransport = new RawUdpTransportPacketExtension();
        int generation = getCurrentGeneration();
        CandidatePacketExtension rtpCand = new CandidatePacketExtension();
        rtpCand.setComponent(1);
        rtpCand.setGeneration(generation);
        rtpCand.setID(getNextID());
        rtpCand.setType(CandidateType.host);
        DatagramSocket dataSocket = connector.getDataSocket();
        rtpCand.setIP(dataSocket.getLocalAddress().getHostAddress());
        rtpCand.setPort(dataSocket.getLocalPort());
        ourTransport.addCandidate(rtpCand);
        CandidatePacketExtension rtcpCand = new CandidatePacketExtension();
        rtcpCand.setComponent(2);
        rtcpCand.setGeneration(generation);
        rtcpCand.setID(getNextID());
        rtcpCand.setType(CandidateType.host);
        DatagramSocket controlSocket = connector.getControlSocket();
        rtcpCand.setIP(controlSocket.getLocalAddress().getHostAddress());
        rtcpCand.setPort(controlSocket.getLocalPort());
        ourTransport.addCandidate(rtcpCand);
        return ourTransport;
    }

    /* access modifiers changed from: protected */
    public PacketExtension createTransportPacketExtension() {
        return new RawUdpTransportPacketExtension();
    }

    public MediaStreamTarget getStreamTarget(MediaType mediaType) {
        Channel channel = getColibriChannel(mediaType, true);
        MediaStreamTarget streamTarget = null;
        if (channel == null) {
            String media = mediaType.toString();
            for (Iterable<ContentPacketExtension> remote : this.remotes) {
                for (ContentPacketExtension content : remote) {
                    if (media.equals(((RtpDescriptionPacketExtension) content.getFirstChildOfType(RtpDescriptionPacketExtension.class)).getMedia())) {
                        streamTarget = JingleUtils.extractDefaultTarget(content);
                        break;
                    }
                }
            }
            return streamTarget;
        }
        IceUdpTransportPacketExtension transport = channel.getTransport();
        if (transport != null) {
            streamTarget = JingleUtils.extractDefaultTarget(transport);
        }
        if (streamTarget != null) {
            return streamTarget;
        }
        String host = channel.getHost();
        if (host == null) {
            return streamTarget;
        }
        return new MediaStreamTarget(new InetSocketAddress(host, channel.getRTPPort()), new InetSocketAddress(host, channel.getRTCPPort()));
    }

    public String getXmlNamespace() {
        return "urn:xmpp:jingle:transports:raw-udp:1";
    }

    public void removeContent(String name) {
        if (this.local != null) {
            removeContent(this.local, name);
        }
        removeRemoteContent(name);
    }

    private void removeRemoteContent(String name) {
        Iterator<Iterable<ContentPacketExtension>> remoteIter = this.remotes.iterator();
        while (remoteIter.hasNext()) {
            Iterable<ContentPacketExtension> remote = (Iterable) remoteIter.next();
            if (!(removeContent(remote, name) == null || remote.iterator().hasNext())) {
                remoteIter.remove();
            }
        }
    }

    /* access modifiers changed from: protected */
    public PacketExtension startCandidateHarvest(ContentPacketExtension theirContent, ContentPacketExtension ourContent, TransportInfoSender transportInfoSender, String media) throws OperationFailedException {
        return createTransportForStartCandidateHarvest(media);
    }

    public void startCandidateHarvest(List<ContentPacketExtension> theirOffer, List<ContentPacketExtension> ourAnswer, TransportInfoSender transportInfoSender) throws OperationFailedException {
        this.local = ourAnswer;
        super.startCandidateHarvest(theirOffer, ourAnswer, transportInfoSender);
    }

    public boolean startConnectivityEstablishment(Iterable<ContentPacketExtension> remote) {
        if (!(remote == null || this.remotes.contains(remote))) {
            for (ContentPacketExtension content : remote) {
                removeRemoteContent(content.getName());
            }
            this.remotes.add(remote);
        }
        return super.startConnectivityEstablishment((Iterable) remote);
    }

    public List<ContentPacketExtension> wrapupCandidateHarvest() {
        return this.local;
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
