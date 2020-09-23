package net.java.sip.communicator.impl.protocol.jabber;

import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.ContactResource;
import net.java.sip.communicator.service.protocol.PresenceStatus;

public class ContactResourceJabberImpl extends ContactResource {
    private final String fullJid;

    public ContactResourceJabberImpl(String fullJid, Contact contact, String resourceName, PresenceStatus presenceStatus, int priority, boolean isMobile) {
        super(contact, resourceName, presenceStatus, priority, isMobile);
        this.fullJid = fullJid;
    }

    public String getFullJid() {
        return this.fullJid;
    }

    /* access modifiers changed from: protected */
    public void setPresenceStatus(PresenceStatus newStatus) {
        this.presenceStatus = newStatus;
    }

    public void setMobile(boolean isMobile) {
        this.mobile = isMobile;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
