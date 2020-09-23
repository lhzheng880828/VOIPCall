package net.java.sip.communicator.impl.protocol.jabber.extensions.geolocation;

import net.java.sip.communicator.impl.protocol.jabber.OperationSetPersistentPresenceJabberImpl;
import net.java.sip.communicator.service.protocol.OperationSetPresence;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;

public class GeolocationPresence {
    private Presence prez;

    public GeolocationPresence(OperationSetPresence persistentPresence) {
        this.prez = null;
        this.prez = new Presence(Type.available);
        this.prez.setStatus(persistentPresence.getCurrentStatusMessage());
        this.prez.setMode(OperationSetPersistentPresenceJabberImpl.presenceStatusToJabberMode(persistentPresence.getPresenceStatus()));
    }

    public void setGeolocationExtention(GeolocationPacketExtension ext) {
        this.prez.addExtension(ext);
    }

    public Presence getGeolocPresence() {
        return this.prez;
    }
}
