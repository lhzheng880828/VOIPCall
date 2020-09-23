package net.java.sip.communicator.impl.protocol.jabber;

import java.util.Collection;
import java.util.HashSet;
import net.java.sip.communicator.util.Logger;
import org.ice4j.Transport;
import org.ice4j.TransportAddress;
import org.ice4j.ice.Component;
import org.ice4j.ice.LocalCandidate;
import org.ice4j.ice.harvest.CandidateHarvester;
import org.jivesoftware.smack.XMPPConnection;
import org.xmpp.jnodes.smack.JingleChannelIQ;
import org.xmpp.jnodes.smack.SmackServiceNode;
import org.xmpp.jnodes.smack.TrackerEntry;

public class JingleNodesHarvester extends CandidateHarvester {
    private static final Logger logger = Logger.getLogger(JingleNodesHarvester.class.getName());
    private TransportAddress localAddressSecond = null;
    private TransportAddress relayedAddressSecond = null;
    private SmackServiceNode serviceNode = null;

    public JingleNodesHarvester(SmackServiceNode serviceNode) {
        this.serviceNode = serviceNode;
    }

    public synchronized Collection<LocalCandidate> harvest(Component component) {
        Collection<LocalCandidate> candidates;
        logger.info("harvest Jingle Nodes");
        candidates = new HashSet();
        if (this.localAddressSecond == null || this.relayedAddressSecond == null) {
            XMPPConnection conn = this.serviceNode.getConnection();
            JingleChannelIQ ciq = null;
            if (this.serviceNode != null) {
                TrackerEntry preferred = this.serviceNode.getPreferedRelay();
                if (preferred != null) {
                    ciq = SmackServiceNode.getChannel(conn, preferred.getJid());
                }
            }
            if (ciq != null && ciq.getRemoteport() > 0) {
                String ip = ciq.getHost();
                int port = ciq.getRemoteport();
                if (logger.isInfoEnabled()) {
                    logger.info("JN relay: " + ip + " remote port:" + port + " local port: " + ciq.getLocalport());
                }
                LocalCandidate local = createJingleNodesCandidate(new TransportAddress(ip, port, Transport.UDP), component, new TransportAddress(ip, ciq.getLocalport(), Transport.UDP));
                this.relayedAddressSecond = new TransportAddress(ip, port + 1, Transport.UDP);
                this.localAddressSecond = new TransportAddress(ip, ciq.getLocalport() + 1, Transport.UDP);
                candidates.add(local);
                component.addLocalCandidate(local);
            }
        } else {
            LocalCandidate candidate = createJingleNodesCandidate(this.relayedAddressSecond, component, this.localAddressSecond);
            candidates.add(candidate);
            component.addLocalCandidate(candidate);
            this.localAddressSecond = null;
            this.relayedAddressSecond = null;
        }
        return candidates;
    }

    /* access modifiers changed from: protected */
    public JingleNodesCandidate createJingleNodesCandidate(TransportAddress transportAddress, Component component, TransportAddress localEndPoint) {
        Throwable e;
        JingleNodesCandidate cand = null;
        try {
            JingleNodesCandidate cand2 = new JingleNodesCandidate(transportAddress, component, localEndPoint);
            try {
                cand2.getStunStack().addSocket(cand2.getStunSocket(null));
                return cand2;
            } catch (Throwable th) {
                e = th;
                cand = cand2;
                logger.debug("Exception occurred when creating JingleNodesCandidate: " + e);
                return cand;
            }
        } catch (Throwable th2) {
            e = th2;
            logger.debug("Exception occurred when creating JingleNodesCandidate: " + e);
            return cand;
        }
    }
}
