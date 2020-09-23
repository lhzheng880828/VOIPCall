package net.java.sip.communicator.impl.protocol.sip;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.resourcelists.DisplayNameType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.resourcelists.EntryType;
import net.java.sip.communicator.service.protocol.AbstractContact;
import net.java.sip.communicator.service.protocol.ContactGroup;
import net.java.sip.communicator.service.protocol.ContactResource;
import net.java.sip.communicator.service.protocol.OperationSetPresence;
import net.java.sip.communicator.service.protocol.PresenceStatus;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.javax.sip.address.Address;
import org.jitsi.javax.sip.address.SipURI;
import org.w3c.dom.Element;

public class ContactSipImpl extends AbstractContact {
    private static final String XCAP_RESOLVED_PROPERTY = "xcap.resolved";
    private final EntryType entry;
    private byte[] image;
    private URI imageUri;
    private boolean isPersistent = true;
    private boolean isResolvable = true;
    private boolean isResolved = false;
    private ContactGroupSipImpl parentGroup = null;
    private final ProtocolProviderServiceSipImpl parentProvider;
    private PresenceStatus presenceStatus;
    private final Address sipAddress;
    private String subscriptionState;
    private boolean xCapResolved = false;

    public ContactSipImpl(Address contactAddress, ProtocolProviderServiceSipImpl parentProvider) {
        this.sipAddress = contactAddress;
        this.entry = new EntryType(contactAddress.getURI().toString());
        this.parentProvider = parentProvider;
        this.presenceStatus = parentProvider.getSipStatusEnum().getStatus(SipStatusEnum.UNKNOWN);
    }

    /* access modifiers changed from: 0000 */
    public EntryType getEntry() {
        return this.entry;
    }

    public String getUri() {
        return this.entry.getUri();
    }

    /* access modifiers changed from: 0000 */
    public void setParentGroup(ContactGroupSipImpl newParentGroup) {
        this.parentGroup = newParentGroup;
    }

    public String getAddress() {
        SipURI sipURI = (SipURI) this.sipAddress.getURI();
        return sipURI.getUser() + Separators.AT + sipURI.getHost();
    }

    public Address getSipAddress() {
        return this.sipAddress;
    }

    public String getDisplayName() {
        if (this.entry.getDisplayName() != null) {
            return this.entry.getDisplayName().getValue();
        }
        String sipUserName = ((SipURI) this.sipAddress.getURI()).getUser();
        if (sipUserName != null && sipUserName.length() > 0) {
            return sipUserName;
        }
        if (getAddress().startsWith("sip:")) {
            return getAddress().substring(4);
        }
        return getAddress();
    }

    public void setDisplayName(String displayName) {
        DisplayNameType displayNameType = new DisplayNameType();
        displayNameType.setValue(displayName);
        this.entry.setDisplayName(displayNameType);
    }

    public void setDisplayName(DisplayNameType displayName) {
        this.entry.setDisplayName(displayName);
    }

    /* access modifiers changed from: 0000 */
    public void setOtherAttributes(Map<QName, String> otherAttributes) {
        this.entry.setAnyAttributes(otherAttributes);
    }

    /* access modifiers changed from: 0000 */
    public void setAny(List<Element> any) {
        this.entry.setAny(any);
    }

    /* access modifiers changed from: 0000 */
    public List<Element> getAny() {
        return this.entry.getAny();
    }

    /* access modifiers changed from: 0000 */
    public URI getImageUri() {
        return this.imageUri;
    }

    /* access modifiers changed from: 0000 */
    public void setImageUri(URI imageUri) {
        this.imageUri = imageUri;
    }

    public byte[] getImage() {
        return this.image;
    }

    /* access modifiers changed from: 0000 */
    public void setImage(byte[] image) {
        this.image = image;
    }

    public PresenceStatus getPresenceStatus() {
        return this.presenceStatus;
    }

    public void setPresenceStatus(PresenceStatus sipPresenceStatus) {
        this.presenceStatus = sipPresenceStatus;
    }

    public ProtocolProviderService getProtocolProvider() {
        return this.parentProvider;
    }

    public boolean isLocal() {
        return false;
    }

    public ContactGroup getParentContactGroup() {
        return this.parentGroup;
    }

    public String toString() {
        return new StringBuffer("ContactSipImpl[ DisplayName=").append(getDisplayName()).append("]").toString();
    }

    public boolean isPersistent() {
        return this.isPersistent;
    }

    public void setPersistent(boolean isPersistent) {
        this.isPersistent = isPersistent;
    }

    public boolean isXCapResolved() {
        return this.xCapResolved;
    }

    public void setXCapResolved(boolean xCapResolved) {
        this.xCapResolved = xCapResolved;
    }

    public String getPersistentData() {
        return "xcap.resolved=" + Boolean.toString(this.xCapResolved) + Separators.SEMICOLON;
    }

    public void setPersistentData(String persistentData) {
        if (persistentData != null) {
            StringTokenizer tokenizer = new StringTokenizer(persistentData, Separators.SEMICOLON);
            while (tokenizer.hasMoreTokens()) {
                String[] data = tokenizer.nextToken().split(Separators.EQUALS);
                if (data[0].equals(XCAP_RESOLVED_PROPERTY) && data.length > 1) {
                    this.xCapResolved = Boolean.valueOf(data[1]).booleanValue();
                }
            }
        }
    }

    public boolean isResolved() {
        return this.isResolved;
    }

    public void setResolved(boolean resolved) {
        this.isResolved = resolved;
    }

    public boolean isResolvable() {
        return this.isResolvable;
    }

    public void setResolvable(boolean resolvable) {
        this.isResolvable = resolvable;
    }

    public boolean equals(Object obj) {
        if (obj == null || (!(obj instanceof ContactSipImpl) && !(obj instanceof String))) {
            return false;
        }
        if (obj instanceof String) {
            String sobj = (String) obj;
            if (sobj.startsWith("sip:")) {
                sobj = sobj.substring(4);
            }
            if (getAddress().equalsIgnoreCase(sobj) || ((SipURI) this.sipAddress.getURI()).getUser().equalsIgnoreCase(sobj)) {
                return true;
            }
            return false;
        }
        return getAddress().equals(((ContactSipImpl) obj).getAddress());
    }

    public OperationSetPresenceSipImpl getParentPresenceOperationSet() {
        return (OperationSetPresenceSipImpl) this.parentProvider.getOperationSet(OperationSetPresence.class);
    }

    public String getStatusMessage() {
        return null;
    }

    public String getSubscriptionState() {
        return this.subscriptionState;
    }

    public void setSubscriptionState(String subscriptionState) {
        this.subscriptionState = subscriptionState;
    }

    public boolean supportResources() {
        return false;
    }

    public Collection<ContactResource> getResources() {
        return null;
    }
}
