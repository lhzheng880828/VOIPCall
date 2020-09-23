package org.xmpp.jnodes.smack;

import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.CandidatePacketExtension;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmpp.jnodes.smack.TrackerEntry.Policy;
import org.xmpp.jnodes.smack.TrackerEntry.Type;

public class JingleTrackerProvider implements IQProvider {
    public JingleTrackerIQ parseIQ(XmlPullParser parser) throws Exception {
        JingleTrackerIQ iq = new JingleTrackerIQ();
        boolean done = false;
        while (!done) {
            int eventType = parser.getEventType();
            String elementName = parser.getName();
            if (eventType == 2) {
                Type type;
                if (elementName.equals(Type.relay.toString())) {
                    type = Type.relay;
                } else if (elementName.equals(Type.tracker.toString())) {
                    type = Type.tracker;
                } else {
                    parser.next();
                }
                String protocol = parser.getAttributeValue(null, CandidatePacketExtension.PROTOCOL_ATTR_NAME);
                Policy policy = Policy.valueOf("_" + parser.getAttributeValue(null, "policy"));
                String address = parser.getAttributeValue(null, "address");
                String verified = parser.getAttributeValue(null, "verified");
                if (address != null && address.length() > 0) {
                    TrackerEntry entry = new TrackerEntry(type, policy, address, protocol);
                    if (verified != null && verified.equals("true")) {
                        entry.setVerified(true);
                    }
                    iq.addEntry(entry);
                }
            } else if (eventType == 3 && elementName.equals(JingleTrackerIQ.NAME)) {
                done = true;
            }
            if (!done) {
                parser.next();
            }
        }
        return iq;
    }
}
