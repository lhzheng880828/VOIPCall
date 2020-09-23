package net.java.sip.communicator.impl.protocol.jabber;

import net.java.sip.communicator.impl.protocol.jabber.AbstractCallJabberGTalkImpl;
import net.java.sip.communicator.impl.protocol.jabber.AbstractCallPeerMediaHandlerJabberGTalkImpl;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.OperationSetPresence;
import net.java.sip.communicator.service.protocol.media.MediaAwareCallPeer;
import net.java.sip.communicator.util.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smackx.packet.DiscoverInfo;

public abstract class AbstractCallPeerJabberGTalkImpl<T extends AbstractCallJabberGTalkImpl<?>, U extends AbstractCallPeerMediaHandlerJabberGTalkImpl<?>, V extends IQ> extends MediaAwareCallPeer<T, U, ProtocolProviderServiceJabberImpl> {
    private static final Logger logger = Logger.getLogger(AbstractCallPeerJabberGTalkImpl.class);
    private DiscoverInfo discoverInfo;
    protected boolean initiator = false;
    protected String peerJID;
    protected V sessionInitIQ;

    public abstract String getSID();

    protected AbstractCallPeerJabberGTalkImpl(String peerAddress, T owningCall) {
        super(owningCall);
        this.peerJID = peerAddress;
    }

    public String getAddress() {
        return this.peerJID;
    }

    public Contact getContact() {
        OperationSetPresence presence = (OperationSetPresence) ((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getOperationSet(OperationSetPresence.class);
        return presence == null ? null : presence.findContactByID(getAddress());
    }

    public DiscoverInfo getDiscoveryInfo() {
        return this.discoverInfo;
    }

    public String getDisplayName() {
        if (getCall() != null) {
            Contact contact = getContact();
            if (contact != null) {
                return contact.getDisplayName();
            }
        }
        return this.peerJID;
    }

    public String getURI() {
        return "xmpp:" + this.peerJID;
    }

    public boolean isInitiator() {
        return this.initiator;
    }

    /* access modifiers changed from: protected */
    public void retrieveDiscoveryInfo(String calleeURI) {
        try {
            DiscoverInfo discoveryInfo = ((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getDiscoveryManager().discoverInfo(calleeURI);
            if (discoveryInfo != null) {
                setDiscoveryInfo(discoveryInfo);
            }
        } catch (XMPPException xmppex) {
            logger.warn("Could not retrieve info for " + calleeURI, xmppex);
        }
    }

    public void setAddress(String address) {
        if (!this.peerJID.equals(address)) {
            String oldAddress = getAddress();
            this.peerJID = address;
            fireCallPeerChangeEvent("CallPeerAddressChange", oldAddress, address);
        }
    }

    public void setDiscoveryInfo(DiscoverInfo discoverInfo) {
        this.discoverInfo = discoverInfo;
    }

    public String getSessInitID() {
        return this.sessionInitIQ != null ? this.sessionInitIQ.getPacketID() : null;
    }
}
