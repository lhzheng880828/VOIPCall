package net.java.sip.communicator.impl.protocol.jabber;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.java.sip.communicator.impl.protocol.sip.SipStatusEnum;
import net.java.sip.communicator.service.protocol.AbstractContact;
import net.java.sip.communicator.service.protocol.ContactGroup;
import net.java.sip.communicator.service.protocol.ContactResource;
import net.java.sip.communicator.service.protocol.PresenceStatus;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import net.java.sip.communicator.service.protocol.event.ContactResourceEvent;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.RosterEntry;

public class ContactJabberImpl extends AbstractContact {
    private byte[] image = null;
    private boolean isPersistent = false;
    private boolean isResolved = false;
    private String jid = null;
    private boolean mobile = false;
    private Map<String, ContactResourceJabberImpl> resources = null;
    private String serverDisplayName = null;
    private final ServerStoredContactListJabberImpl ssclCallback;
    private PresenceStatus status;
    private String statusMessage = null;
    private final String tempId;

    ContactJabberImpl(RosterEntry rosterEntry, ServerStoredContactListJabberImpl ssclCallback, boolean isPersistent, boolean isResolved) {
        if (rosterEntry != null) {
            this.jid = rosterEntry.getUser();
            this.serverDisplayName = rosterEntry.getName();
        }
        this.tempId = null;
        this.ssclCallback = ssclCallback;
        this.isPersistent = isPersistent;
        this.isResolved = isResolved;
        this.status = ((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getJabberStatusEnum().getStatus(SipStatusEnum.OFFLINE);
    }

    ContactJabberImpl(String id, ServerStoredContactListJabberImpl ssclCallback, boolean isPersistent) {
        this.tempId = id;
        this.ssclCallback = ssclCallback;
        this.isPersistent = isPersistent;
        this.isResolved = false;
        this.status = ((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getJabberStatusEnum().getStatus(SipStatusEnum.OFFLINE);
    }

    public String getAddress() {
        if (this.isResolved) {
            return this.jid;
        }
        return this.tempId;
    }

    public boolean isLocal() {
        return false;
    }

    public byte[] getImage() {
        return getImage(true);
    }

    public byte[] getImage(boolean retrieveIfNecessary) {
        if (this.image == null && retrieveIfNecessary) {
            this.ssclCallback.addContactForImageUpdate(this);
        }
        return this.image;
    }

    public void setImage(byte[] imgBytes) {
        this.image = imgBytes;
    }

    public int hashCode() {
        return getAddress().toLowerCase().hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null || (!(obj instanceof String) && !(obj instanceof ContactJabberImpl))) {
            return false;
        }
        if ((obj instanceof ContactJabberImpl) && ((ContactJabberImpl) obj).getAddress().equalsIgnoreCase(getAddress()) && ((ContactJabberImpl) obj).getProtocolProvider() == getProtocolProvider()) {
            return true;
        }
        if (obj instanceof String) {
            int atIndex = getAddress().indexOf(Separators.AT);
            if (atIndex > 0) {
                if (getAddress().equalsIgnoreCase((String) obj) || getAddress().substring(0, atIndex).equalsIgnoreCase((String) obj)) {
                    return true;
                }
            } else if (getAddress().equalsIgnoreCase((String) obj)) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        StringBuffer buff = new StringBuffer("JabberContact[ id=");
        buff.append(getAddress()).append(", isPersistent=").append(this.isPersistent).append(", isResolved=").append(this.isResolved).append("]");
        return buff.toString();
    }

    /* access modifiers changed from: 0000 */
    public void updatePresenceStatus(PresenceStatus status) {
        this.status = status;
    }

    public PresenceStatus getPresenceStatus() {
        return this.status;
    }

    public String getDisplayName() {
        if (this.isResolved) {
            RosterEntry entry = this.ssclCallback.getRosterEntry(this.jid);
            String name = null;
            if (entry != null) {
                name = entry.getName();
            }
            if (!(name == null || name.trim().length() == 0)) {
                return name;
            }
        }
        return getAddress();
    }

    /* access modifiers changed from: 0000 */
    public String getServerDisplayName() {
        return this.serverDisplayName;
    }

    /* access modifiers changed from: 0000 */
    public void setServerDisplayName(String newValue) {
        this.serverDisplayName = newValue;
    }

    public ContactGroup getParentContactGroup() {
        return this.ssclCallback.findContactGroup(this);
    }

    public ProtocolProviderService getProtocolProvider() {
        return this.ssclCallback.getParentProvider();
    }

    public boolean isPersistent() {
        return this.isPersistent;
    }

    /* access modifiers changed from: 0000 */
    public void setPersistent(boolean persistent) {
        this.isPersistent = persistent;
    }

    /* access modifiers changed from: 0000 */
    public void setResolved(RosterEntry entry) {
        if (!this.isResolved) {
            this.isResolved = true;
            this.isPersistent = true;
            this.jid = entry.getUser();
            this.serverDisplayName = entry.getName();
        }
    }

    public String getPersistentData() {
        return null;
    }

    public boolean isResolved() {
        return this.isResolved;
    }

    public void setPersistentData(String persistentData) {
    }

    /* access modifiers changed from: 0000 */
    public RosterEntry getSourceEntry() {
        return this.ssclCallback.getRosterEntry(this.jid);
    }

    public String getStatusMessage() {
        return this.statusMessage;
    }

    /* access modifiers changed from: protected */
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public boolean supportResources() {
        return true;
    }

    public Collection<ContactResource> getResources() {
        if (this.resources != null) {
            return new ArrayList(this.resources.values());
        }
        return null;
    }

    /* access modifiers changed from: 0000 */
    public ContactResource getResourceFromJid(String jid) {
        return (ContactResource) this.resources.get(jid);
    }

    /* access modifiers changed from: 0000 */
    public Map<String, ContactResourceJabberImpl> getResourcesMap() {
        if (this.resources == null) {
            this.resources = new ConcurrentHashMap();
        }
        return this.resources;
    }

    public void fireContactResourceEvent(ContactResourceEvent event) {
        ContactJabberImpl.super.fireContactResourceEvent(event);
    }

    /* access modifiers changed from: protected */
    public void setJid(String fullJid) {
        this.jid = fullJid;
        if (this.resources == null) {
            this.resources = new ConcurrentHashMap();
        }
    }

    public boolean isMobile() {
        if (getPresenceStatus().isOnline()) {
            return this.mobile;
        }
        return false;
    }

    /* access modifiers changed from: 0000 */
    public void setMobile(boolean mobile) {
        this.mobile = mobile;
    }
}
