package net.java.sip.communicator.impl.protocol.jabber;

import net.java.sip.communicator.service.protocol.AuthorizationRequest;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetExtendedAuthorizations;
import net.java.sip.communicator.service.protocol.OperationSetExtendedAuthorizations.SubscriptionStatus;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.packet.RosterPacket.ItemStatus;
import org.jivesoftware.smack.packet.RosterPacket.ItemType;

public class OperationSetExtendedAuthorizationsJabberImpl implements OperationSetExtendedAuthorizations {
    private OperationSetPersistentPresenceJabberImpl opSetPersPresence = null;
    private ProtocolProviderServiceJabberImpl parentProvider;

    OperationSetExtendedAuthorizationsJabberImpl(ProtocolProviderServiceJabberImpl provider, OperationSetPersistentPresenceJabberImpl opSetPersPresence) {
        this.opSetPersPresence = opSetPersPresence;
        this.parentProvider = provider;
    }

    public void explicitAuthorize(Contact contact) throws OperationFailedException {
        this.opSetPersPresence.assertConnected();
        if (contact instanceof ContactJabberImpl) {
            Presence responsePacket = new Presence(Type.subscribed);
            responsePacket.setTo(contact.getAddress());
            this.parentProvider.getConnection().sendPacket(responsePacket);
            return;
        }
        throw new IllegalArgumentException("The specified contact is not an jabber contact." + contact);
    }

    public void reRequestAuthorization(AuthorizationRequest request, Contact contact) throws OperationFailedException {
        this.opSetPersPresence.assertConnected();
        if (contact instanceof ContactJabberImpl) {
            Presence responsePacket = new Presence(Type.subscribe);
            responsePacket.setTo(contact.getAddress());
            this.parentProvider.getConnection().sendPacket(responsePacket);
            return;
        }
        throw new IllegalArgumentException("The specified contact is not an jabber contact." + contact);
    }

    public SubscriptionStatus getSubscriptionStatus(Contact contact) {
        if (contact instanceof ContactJabberImpl) {
            RosterEntry entry = ((ContactJabberImpl) contact).getSourceEntry();
            if (entry == null) {
                return null;
            }
            if ((entry.getType() == ItemType.none || entry.getType() == ItemType.from) && ItemStatus.SUBSCRIPTION_PENDING == entry.getStatus()) {
                return SubscriptionStatus.SubscriptionPending;
            }
            if (entry.getType() == ItemType.to || entry.getType() == ItemType.both) {
                return SubscriptionStatus.Subscribed;
            }
            return SubscriptionStatus.NotSubscribed;
        }
        throw new IllegalArgumentException("The specified contact is not an jabber contact." + contact);
    }
}
