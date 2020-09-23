package org.jivesoftware.smackx.packet;

import java.util.ArrayList;
import java.util.Iterator;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.PacketExtension;

public class MessageEvent implements PacketExtension {
    public static final String CANCELLED = "cancelled";
    public static final String COMPOSING = "composing";
    public static final String DELIVERED = "delivered";
    public static final String DISPLAYED = "displayed";
    public static final String OFFLINE = "offline";
    private boolean cancelled = true;
    private boolean composing = false;
    private boolean delivered = false;
    private boolean displayed = false;
    private boolean offline = false;
    private String packetID = null;

    public String getElementName() {
        return "x";
    }

    public String getNamespace() {
        return "jabber:x:event";
    }

    public boolean isComposing() {
        return this.composing;
    }

    public boolean isDelivered() {
        return this.delivered;
    }

    public boolean isDisplayed() {
        return this.displayed;
    }

    public boolean isOffline() {
        return this.offline;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public String getPacketID() {
        return this.packetID;
    }

    public Iterator getEventTypes() {
        ArrayList allEvents = new ArrayList();
        if (isDelivered()) {
            allEvents.add(DELIVERED);
        }
        if (!isMessageEventRequest() && isCancelled()) {
            allEvents.add(CANCELLED);
        }
        if (isComposing()) {
            allEvents.add(COMPOSING);
        }
        if (isDisplayed()) {
            allEvents.add(DISPLAYED);
        }
        if (isOffline()) {
            allEvents.add(OFFLINE);
        }
        return allEvents.iterator();
    }

    public void setComposing(boolean composing) {
        this.composing = composing;
        setCancelled(false);
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
        setCancelled(false);
    }

    public void setDisplayed(boolean displayed) {
        this.displayed = displayed;
        setCancelled(false);
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
        setCancelled(false);
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void setPacketID(String packetID) {
        this.packetID = packetID;
    }

    public boolean isMessageEventRequest() {
        return this.packetID == null;
    }

    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append(Separators.LESS_THAN).append(getElementName()).append(" xmlns=\"").append(getNamespace()).append("\">");
        if (isOffline()) {
            buf.append(Separators.LESS_THAN).append(OFFLINE).append("/>");
        }
        if (isDelivered()) {
            buf.append(Separators.LESS_THAN).append(DELIVERED).append("/>");
        }
        if (isDisplayed()) {
            buf.append(Separators.LESS_THAN).append(DISPLAYED).append("/>");
        }
        if (isComposing()) {
            buf.append(Separators.LESS_THAN).append(COMPOSING).append("/>");
        }
        if (getPacketID() != null) {
            buf.append("<id>").append(getPacketID()).append("</id>");
        }
        buf.append("</").append(getElementName()).append(Separators.GREATER_THAN);
        return buf.toString();
    }
}
