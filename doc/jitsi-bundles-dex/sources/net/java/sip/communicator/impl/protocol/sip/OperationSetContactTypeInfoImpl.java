package net.java.sip.communicator.impl.protocol.sip;

import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.ContactGroup;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetContactTypeInfo;

public class OperationSetContactTypeInfoImpl implements OperationSetContactTypeInfo {
    private ServerStoredContactListSipImpl contactList;
    private final OperationSetPresenceSipImpl parentOperationSet;

    OperationSetContactTypeInfoImpl(OperationSetPresenceSipImpl parentOperationSet) {
        this.parentOperationSet = parentOperationSet;
        this.contactList = (ServerStoredContactListSipImpl) parentOperationSet.getSsContactList();
    }

    public String getContactType(Contact contact) {
        return this.contactList.getContactType(contact);
    }

    public void setContactType(Contact contact, String contactType) {
        this.contactList.setContactType(contact, contactType);
        if (contact.isPersistent()) {
            try {
                this.contactList.updateResourceLists();
            } catch (Throwable e) {
                IllegalStateException illegalStateException = new IllegalStateException("Error while setting contact type", e);
            }
        }
    }

    public void subscribe(String contactIdentifier, String contactType) throws IllegalArgumentException, IllegalStateException, OperationFailedException {
        this.parentOperationSet.subscribe(this.parentOperationSet.getServerStoredContactListRoot(), contactIdentifier, contactType);
    }

    public void subscribe(ContactGroup parent, String contactIdentifier, String contactType) throws IllegalArgumentException, IllegalStateException, OperationFailedException {
        this.parentOperationSet.subscribe(parent, contactIdentifier, contactType);
    }
}
