package org.xmpp.jnodes.smack;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;

public class JingleTrackerIQ extends IQ {
    public static final String NAME = "services";
    public static final String NAMESPACE = "http://jabber.org/protocol/jinglenodes";
    private final ConcurrentHashMap<String, TrackerEntry> entries = new ConcurrentHashMap();

    public JingleTrackerIQ() {
        setType(Type.GET);
        setPacketID(Packet.nextID());
    }

    public boolean isRequest() {
        return Type.GET.equals(getType());
    }

    public void addEntry(TrackerEntry entry) {
        this.entries.put(entry.getJid(), entry);
    }

    public void removeEntry(TrackerEntry entry) {
        this.entries.remove(entry.getJid());
    }

    public String getChildElementXML() {
        StringBuilder str = new StringBuilder();
        str.append(Separators.LESS_THAN).append(NAME).append(" xmlns='").append("http://jabber.org/protocol/jinglenodes").append("'>");
        for (TrackerEntry entry : this.entries.values()) {
            str.append(Separators.LESS_THAN).append(entry.getType().toString());
            str.append(" policy='").append(entry.getPolicy().toString()).append(Separators.QUOTE);
            str.append(" address='").append(entry.getJid()).append(Separators.QUOTE);
            str.append(" protocol='").append(entry.getProtocol()).append(Separators.QUOTE);
            if (entry.isVerified()) {
                str.append(" verified='").append(entry.isVerified()).append(Separators.QUOTE);
            }
            str.append("/>");
        }
        str.append("</").append(NAME).append(Separators.GREATER_THAN);
        return str.toString();
    }

    public Collection<TrackerEntry> getEntries() {
        return this.entries.values();
    }
}
