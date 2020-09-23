package org.jivesoftware.smack;

import java.util.Collection;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.XMPPError;

public interface RosterListener {
    void entriesAdded(Collection<String> collection);

    void entriesDeleted(Collection<String> collection);

    void entriesUpdated(Collection<String> collection);

    void presenceChanged(Presence presence);

    void rosterError(XMPPError xMPPError, Packet packet);
}
