package net.java.sip.communicator.impl.protocol.jabber.extensions.geolocation;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;

public class GeolocationMessage extends Message {
    public GeolocationMessage(GeolocationPacketExtension geoloc) {
        addExtension(geoloc);
    }

    public GeolocationMessage(String to, GeolocationPacketExtension geoloc) {
        super(to);
        addExtension(geoloc);
    }

    public GeolocationMessage(String to, Type type, GeolocationPacketExtension geoloc) {
        super(to, type);
        addExtension(geoloc);
    }
}
