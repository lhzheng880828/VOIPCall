package net.java.sip.communicator.impl.protocol.jabber;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.ContactGroup;
import net.java.sip.communicator.service.protocol.OperationSetPersistentPresence;
import net.java.sip.communicator.service.protocol.OperationSetPersistentPresencePermissions;
import org.jitsi.gov.nist.core.Separators;

public class OperationSetPersistentPresencePermissionsJabberImpl implements OperationSetPersistentPresencePermissions {
    private static final String ALL_GROUPS_STR = "all";
    private static final String ROOT_GROUP_STR = "root";
    private final ProtocolProviderServiceJabberImpl provider;
    private List<String> readonlyGroups = new ArrayList();

    OperationSetPersistentPresencePermissionsJabberImpl(ProtocolProviderServiceJabberImpl provider) {
        this.provider = provider;
        String readOnlyGroupsStr = provider.getAccountID().getAccountPropertyString("READ_ONLY_GROUPS");
        if (readOnlyGroupsStr != null) {
            StringTokenizer tokenizer = new StringTokenizer(readOnlyGroupsStr, Separators.COMMA);
            while (tokenizer.hasMoreTokens()) {
                this.readonlyGroups.add(tokenizer.nextToken().trim());
            }
        }
    }

    public boolean isReadOnly() {
        if (this.readonlyGroups.contains(ALL_GROUPS_STR)) {
            return true;
        }
        List<String> groupsList = new ArrayList();
        groupsList.add(ROOT_GROUP_STR);
        Iterator<ContactGroup> groupsIter = ((OperationSetPersistentPresence) this.provider.getOperationSet(OperationSetPersistentPresence.class)).getServerStoredContactListRoot().subgroups();
        while (groupsIter.hasNext()) {
            groupsList.add(((ContactGroup) groupsIter.next()).getGroupName());
        }
        if (groupsList.size() > this.readonlyGroups.size()) {
            return false;
        }
        groupsList.removeAll(this.readonlyGroups);
        if (groupsList.size() > 0) {
            return false;
        }
        return true;
    }

    public boolean isReadOnly(Contact contact) {
        return isReadOnly(contact.getParentContactGroup());
    }

    public boolean isReadOnly(ContactGroup group) {
        if (isReadOnly()) {
            return true;
        }
        if (group instanceof RootContactGroupJabberImpl) {
            return this.readonlyGroups.contains(ROOT_GROUP_STR);
        }
        return this.readonlyGroups.contains(group.getGroupName());
    }
}
