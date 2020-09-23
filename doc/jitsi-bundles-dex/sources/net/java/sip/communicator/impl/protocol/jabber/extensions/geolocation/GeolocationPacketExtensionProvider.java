package net.java.sip.communicator.impl.protocol.jabber.extensions.geolocation;

import net.java.sip.communicator.util.Logger;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;

public class GeolocationPacketExtensionProvider implements PacketExtensionProvider {
    public static final String ELEMENT_NAME = "geoloc";
    public static final String NAMESPACE = "http://jabber.org/protocol/geoloc";
    private static final Logger logger = Logger.getLogger(GeolocationPacketExtensionProvider.class);

    public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
        GeolocationPacketExtension result = new GeolocationPacketExtension();
        if (logger.isTraceEnabled()) {
            logger.trace("Trying to map XML Geolocation Extension");
        }
        boolean done = false;
        while (!done) {
            try {
                int eventType = parser.next();
                if (eventType == 2) {
                    if (parser.getName().equals(GeolocationPacketExtension.ALT)) {
                        result.setAlt(Float.parseFloat(parser.nextText()));
                    }
                    if (parser.getName().equals(GeolocationPacketExtension.AREA)) {
                        result.setArea(parser.nextText());
                    }
                    if (parser.getName().equals(GeolocationPacketExtension.BEARING)) {
                        result.setBearing(Float.parseFloat(parser.nextText()));
                    }
                    if (parser.getName().equals(GeolocationPacketExtension.BUILDING)) {
                        result.setBuilding(parser.nextText());
                    }
                    if (parser.getName().equals(GeolocationPacketExtension.COUNTRY)) {
                        result.setCountry(parser.nextText());
                    }
                    if (parser.getName().equals(GeolocationPacketExtension.DATUM)) {
                        result.setDatum(parser.nextText());
                    }
                    if (parser.getName().equals("description")) {
                        result.setDescription(parser.nextText());
                    }
                    if (parser.getName().equals(GeolocationPacketExtension.ERROR)) {
                        result.setError(Float.parseFloat(parser.nextText()));
                    }
                    if (parser.getName().equals(GeolocationPacketExtension.FLOOR)) {
                        result.setFloor(parser.nextText());
                    }
                    if (parser.getName().equals(GeolocationPacketExtension.LAT)) {
                        result.setLat(Float.parseFloat(parser.nextText()));
                    }
                    if (parser.getName().equals(GeolocationPacketExtension.LOCALITY)) {
                        result.setLocality(parser.nextText());
                    }
                    if (parser.getName().equals(GeolocationPacketExtension.LON)) {
                        result.setLon(Float.parseFloat(parser.nextText()));
                    }
                    if (parser.getName().equals(GeolocationPacketExtension.POSTALCODE)) {
                        result.setPostalCode(parser.nextText());
                    }
                    if (parser.getName().equals(GeolocationPacketExtension.REGION)) {
                        result.setRegion(parser.nextText());
                    }
                    if (parser.getName().equals(GeolocationPacketExtension.ROOM)) {
                        result.setRoom(parser.nextText());
                    }
                    if (parser.getName().equals(GeolocationPacketExtension.STREET)) {
                        result.setStreet(parser.nextText());
                    }
                    if (parser.getName().equals("text")) {
                        result.setText(parser.nextText());
                    }
                    if (parser.getName().equals(GeolocationPacketExtension.TIMESTAMP)) {
                        result.setText(parser.nextText());
                    }
                } else if (eventType == 3) {
                    if (parser.getName().equals(ELEMENT_NAME)) {
                        done = true;
                        if (logger.isTraceEnabled()) {
                            logger.trace("Parsing finish");
                        }
                    }
                }
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }
}
