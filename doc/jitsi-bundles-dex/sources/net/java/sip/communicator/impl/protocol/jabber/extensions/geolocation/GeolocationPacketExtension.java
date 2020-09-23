package net.java.sip.communicator.impl.protocol.jabber.extensions.geolocation;

import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.PacketExtension;

public class GeolocationPacketExtension implements PacketExtension {
    public static final String ALT = "alt";
    public static final String AREA = "area";
    public static final String BEARING = "bearing";
    public static final String BUILDING = "building";
    public static final String COUNTRY = "country";
    public static final String DATUM = "datum";
    public static final String DESCRIPTION = "description";
    public static final String ERROR = "error";
    public static final String FLOOR = "floor";
    public static final String LAT = "lat";
    public static final String LOCALITY = "locality";
    public static final String LON = "lon";
    public static final String POSTALCODE = "postalcode";
    public static final String REGION = "region";
    public static final String ROOM = "room";
    public static final String STREET = "street";
    public static final String TEXT = "text";
    public static final String TIMESTAMP = "timestamp";
    private float alt = -1.0f;
    private String area = null;
    private float bearing = -1.0f;
    private String building = null;
    private String country = null;
    private String datum = null;
    private String description = null;
    private float error = -1.0f;
    private String floor = null;
    private float lat = -1.0f;
    private String locality = null;
    private float lon = -1.0f;
    private String postalcode = null;
    private String region = null;
    private String room = null;
    private String street = null;
    private String text = null;
    private String timestamp = null;

    public String toXML() {
        StringBuffer buf = new StringBuffer();
        buf.append(Separators.LESS_THAN).append(getElementName()).append(" xmlns=\"").append(getNamespace()).append("\">");
        buf = addXmlElement(addXmlElement(addXmlElement(addXmlElement(addXmlElement(addXmlElement(addFloatXmlElement(addXmlElement(addFloatXmlElement(addXmlElement(addFloatXmlElement(addXmlElement(addXmlElement(addXmlElement(addXmlElement(addFloatXmlElement(addXmlElement(addFloatXmlElement(buf, ALT, getAlt()), AREA, getArea()), BEARING, getBearing()), BUILDING, getBuilding()), COUNTRY, getCountry()), DATUM, getDatum()), "description", getDescription()), ERROR, getError()), FLOOR, getFloor()), LAT, getLat()), LOCALITY, getLocality()), LON, getLon()), POSTALCODE, getPostalCode()), REGION, getRegion()), ROOM, getRoom()), STREET, getStreet()), "text", getText()), TIMESTAMP, getTimestamp());
        buf.append("</").append(getElementName()).append(Separators.GREATER_THAN);
        return buf.toString();
    }

    private StringBuffer addXmlElement(StringBuffer buff, String element, String value) {
        if (value != null) {
            buff.append(Separators.LESS_THAN).append(element).append(Separators.GREATER_THAN).append(value).append("</").append(element).append(Separators.GREATER_THAN);
        }
        return buff;
    }

    private StringBuffer addFloatXmlElement(StringBuffer buff, String element, float value) {
        if (value != -1.0f) {
            buff.append(Separators.LESS_THAN).append(element).append(Separators.GREATER_THAN).append(value).append("</").append(element).append(Separators.GREATER_THAN);
        }
        return buff;
    }

    public String getElementName() {
        return GeolocationPacketExtensionProvider.ELEMENT_NAME;
    }

    public String getNamespace() {
        return GeolocationPacketExtensionProvider.NAMESPACE;
    }

    public float getAlt() {
        return this.alt;
    }

    public void setAlt(float alt) {
        this.alt = alt;
    }

    public void setAlt(String alt) {
        this.alt = new Float(alt).floatValue();
    }

    public String getArea() {
        return this.area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public float getBearing() {
        return this.bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public void setBearing(String bearing) {
        this.bearing = new Float(bearing).floatValue();
    }

    public String getBuilding() {
        return this.building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDatum() {
        return this.datum;
    }

    public void setDatum(String datum) {
        this.datum = datum;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getError() {
        return this.error;
    }

    public void setError(float error) {
        this.error = error;
    }

    public void setError(String error) {
        this.error = new Float(error).floatValue();
    }

    public String getFloor() {
        return this.floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public float getLat() {
        return this.lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public void setLat(String lat) {
        this.lat = new Float(lat).floatValue();
    }

    public String getLocality() {
        return this.locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public float getLon() {
        return this.lon;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public void setLon(String lon) {
        this.lon = new Float(lon).floatValue();
    }

    public String getPostalCode() {
        return this.postalcode;
    }

    public void setPostalCode(String postalCode) {
        this.postalcode = postalCode;
    }

    public String getRegion() {
        return this.region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getRoom() {
        return this.room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getStreet() {
        return this.street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean containsLatLon() {
        if (this.lat == -1.0f || this.lon == -1.0f) {
            return false;
        }
        return true;
    }
}
