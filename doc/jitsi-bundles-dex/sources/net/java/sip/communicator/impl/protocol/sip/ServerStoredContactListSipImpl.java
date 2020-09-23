package net.java.sip.communicator.impl.protocol.sip;

import gov.nist.javax.sdp.fields.SDPKeywords;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.java.sip.communicator.impl.protocol.sip.xcap.XCapClient;
import net.java.sip.communicator.impl.protocol.sip.xcap.XCapClientImpl;
import net.java.sip.communicator.impl.protocol.sip.xcap.XCapException;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.commonpolicy.ActionsType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.commonpolicy.ConditionsType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.commonpolicy.IdentityType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.commonpolicy.OneType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.commonpolicy.RuleType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.commonpolicy.RulesetType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.commonpolicy.TransformationsType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.prescontent.ContentType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.prescontent.DataType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.prescontent.DescriptionType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.prescontent.EncodingType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.prescontent.MimeType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.presrules.ProvideDevicePermissionType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.presrules.ProvideDevicePermissionType.AllDevicesType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.presrules.ProvidePersonPermissionType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.presrules.ProvidePersonPermissionType.AllPersonsType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.presrules.ProvideServicePermissionType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.presrules.ProvideServicePermissionType.AllServicesType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.presrules.SubHandlingType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.resourcelists.EntryType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.resourcelists.ListType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.resourcelists.ResourceListsType;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.ContactGroup;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.ImageDetail;
import net.java.sip.communicator.util.Base64;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.address.SipUri;
import org.jitsi.javax.sip.address.Address;
import org.jitsi.javax.sip.address.SipURI;
import org.jitsi.util.xml.XMLUtils;
import org.w3c.dom.Element;

public class ServerStoredContactListSipImpl extends ServerStoredContactList {
    private static final String CONTACT_TYPE_ELEMENT_NAME = "contact-type";
    private static final String CONTACT_TYPE_NS = "http://jitsi.org/contact-type";
    private static final String DEFAULT_BLOCK_RULE_ID = "presence_block";
    private static final String DEFAULT_POLITE_BLOCK_RULE_ID = "presence_polite_block";
    private static final String DEFAULT_WHITE_RULE_ID = "presence_allow";
    public static final String PRES_CONTENT_IMAGE_NAME = "sip_communicator";
    private static final Logger logger = Logger.getLogger(ServerStoredContactListSipImpl.class);
    private RulesetType presRules;
    private final XCapClient xCapClient = new XCapClientImpl();

    ServerStoredContactListSipImpl(ProtocolProviderServiceSipImpl sipProvider, OperationSetPresenceSipImpl parentOperationSet) {
        super(sipProvider, parentOperationSet);
    }

    public synchronized ContactSipImpl createContact(ContactGroupSipImpl parentGroup, String contactId, String displayName, boolean persistent, String contactType) throws OperationFailedException {
        ContactSipImpl newContact;
        if (parentGroup == null) {
            throw new IllegalArgumentException("Parent group cannot be null");
        }
        if (contactId != null) {
            if (contactId.trim().length() != 0) {
                if (logger.isTraceEnabled()) {
                    logger.trace(String.format("createContact %1s, %2s, %3s", new Object[]{parentGroup.getGroupName(), contactId, Boolean.valueOf(persistent)}));
                }
                if (parentGroup.getContact(contactId) != null) {
                    throw new OperationFailedException("Contact " + contactId + " already exists.", 5);
                }
                try {
                    Address contactAddress = this.sipProvider.parseAddressString(contactId);
                    newContact = this.parentOperationSet.resolveContactID(contactAddress.getURI().toString());
                    if (!(newContact == null || newContact.isPersistent() || newContact.getParentContactGroup().isPersistent())) {
                        ContactGroupSipImpl oldParentGroup = (ContactGroupSipImpl) newContact.getParentContactGroup();
                        oldParentGroup.removeContact(newContact);
                        fireContactRemoved(oldParentGroup, newContact);
                    }
                    newContact = new ContactSipImpl(contactAddress, this.sipProvider);
                    newContact.setPersistent(persistent);
                    if (displayName == null || displayName.length() <= 0) {
                        displayName = ((SipURI) contactAddress.getURI()).getUser();
                    }
                    newContact.setDisplayName(displayName);
                    if (contactType != null) {
                        setContactType(newContact, contactType);
                    }
                    parentGroup.addContact(newContact);
                    if (newContact.isPersistent()) {
                        updateResourceLists();
                        newContact.setResolved(true);
                        if (this.xCapClient.isConnected() && this.xCapClient.isResourceListsSupported()) {
                            newContact.setXCapResolved(true);
                            try {
                                if (!isContactInWhiteRule(contactId) && addContactToWhiteList(newContact)) {
                                    updatePresRules();
                                }
                            } catch (XCapException e) {
                                logger.error("Cannot add contact to white list while creating it", e);
                            }
                        }
                    }
                    fireContactAdded(parentGroup, newContact);
                } catch (XCapException e2) {
                    parentGroup.removeContact(newContact);
                    throw new OperationFailedException("Error while creating XCAP contact", 2, e2);
                } catch (ParseException ex) {
                    throw new IllegalArgumentException(contactId + " is not a valid string.", ex);
                }
            }
        }
        throw new IllegalArgumentException("Contact identifier cannot be null or empty");
        return newContact;
    }

    public synchronized void removeContact(ContactSipImpl contact) throws OperationFailedException {
        if (contact == null) {
            throw new IllegalArgumentException("Removing contact cannot be null");
        }
        if (logger.isTraceEnabled()) {
            logger.trace("removeContact " + contact.getUri());
        }
        ContactGroupSipImpl parentGroup = (ContactGroupSipImpl) contact.getParentContactGroup();
        parentGroup.removeContact(contact);
        if (contact.isPersistent()) {
            try {
                boolean updateRules = removeContactFromWhiteList(contact);
                if (removeContactFromBlockList(contact) || updateRules) {
                    updateRules = true;
                } else {
                    updateRules = false;
                }
                if (removeContactFromPoliteBlockList(contact) || updateRules) {
                    updateRules = true;
                } else {
                    updateRules = false;
                }
                if (updateRules) {
                    updatePresRules();
                }
            } catch (XCapException e) {
                parentGroup.removeContact(contact);
                throw new OperationFailedException("Error while removing XCAP contact", 2, e);
            } catch (XCapException e2) {
                logger.error("Error while removing XCAP contact", e2);
            }
            updateResourceLists();
        }
        fireContactRemoved(parentGroup, contact);
    }

    public void moveContactToGroup(ContactSipImpl contact, ContactGroupSipImpl newParentGroup) throws OperationFailedException {
        if (contact == null) {
            throw new IllegalArgumentException("Moving contact cannot be null");
        } else if (newParentGroup == null) {
            throw new IllegalArgumentException("New contact's parent group  be null");
        } else if (newParentGroup.getContact(contact.getUri()) != null) {
            throw new OperationFailedException("Contact " + contact.getUri() + " already exists.", 5);
        } else {
            ContactGroupSipImpl oldParentGroup = (ContactGroupSipImpl) contact.getParentContactGroup();
            oldParentGroup.removeContact(contact);
            boolean wasContactPersistent = contact.isPersistent();
            if (newParentGroup.isPersistent()) {
                contact.setPersistent(true);
            }
            newParentGroup.addContact(contact);
            if (contact.isPersistent()) {
                try {
                    updateResourceLists();
                    if (!wasContactPersistent) {
                        contact.setResolved(true);
                        if (this.xCapClient.isConnected() && this.xCapClient.isResourceListsSupported()) {
                            contact.setXCapResolved(true);
                            try {
                                if (!isContactInWhiteRule(contact.getAddress()) && addContactToWhiteList(contact)) {
                                    updatePresRules();
                                }
                            } catch (XCapException e) {
                                logger.error("Cannot add contact to white list while creating it", e);
                            }
                        }
                    }
                } catch (XCapException e2) {
                    newParentGroup.removeContact(contact);
                    oldParentGroup.addContact(contact);
                    throw new OperationFailedException("Error while moving XCAP contact", 2, e2);
                }
            }
            fireContactMoved(oldParentGroup, newParentGroup, contact);
        }
    }

    public synchronized void renameContact(ContactSipImpl contact, String newName) {
        if (contact == null) {
            throw new IllegalArgumentException("Renaming contact cannot be null");
        }
        String oldName = contact.getDisplayName();
        if (!oldName.equals(newName)) {
            contact.setDisplayName(newName);
            if (contact.isPersistent()) {
                try {
                    updateResourceLists();
                } catch (XCapException e) {
                    contact.setDisplayName(oldName);
                    throw new IllegalStateException("Error while renaming XCAP group", e);
                }
            }
            this.parentOperationSet.fireContactPropertyChangeEvent("DisplayName", contact, oldName, newName);
        }
    }

    public synchronized ContactGroupSipImpl createGroup(ContactGroupSipImpl parentGroup, String groupName, boolean persistent) throws OperationFailedException {
        ContactGroupSipImpl subGroup;
        if (parentGroup == null) {
            throw new IllegalArgumentException("Parent group cannot be null");
        }
        if (groupName != null) {
            if (groupName.length() != 0) {
                if (logger.isTraceEnabled()) {
                    logger.trace("createGroup " + parentGroup.getGroupName() + Separators.COMMA + groupName + Separators.COMMA + persistent);
                }
                if (parentGroup.getGroup(groupName) != null) {
                    throw new OperationFailedException(String.format("Group %1s already exists.", new Object[]{groupName}), 6);
                }
                subGroup = new ContactGroupSipImpl(groupName, this.sipProvider);
                subGroup.setPersistent(persistent);
                parentGroup.addSubgroup(subGroup);
                if (subGroup.isPersistent()) {
                    try {
                        updateResourceLists();
                        subGroup.setResolved(true);
                    } catch (XCapException e) {
                        parentGroup.removeSubGroup(subGroup);
                        throw new OperationFailedException("Error while creating XCAP group", 2, e);
                    }
                }
                fireGroupEvent(subGroup, 1);
            }
        }
        throw new IllegalArgumentException("Creating group name cannot be null or empry");
        return subGroup;
    }

    public synchronized void removeGroup(ContactGroupSipImpl group) {
        if (group == null) {
            throw new IllegalArgumentException("Removing group cannot be null");
        } else if (this.rootGroup.equals(group)) {
            throw new IllegalArgumentException("Root group cannot be deleted");
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("removeGroup " + group.getGroupName());
            }
            ContactGroupSipImpl parentGroup = (ContactGroupSipImpl) group.getParentContactGroup();
            parentGroup.removeSubGroup(group);
            if (group.isPersistent()) {
                try {
                    updateResourceLists();
                    Iterator<Contact> iter = group.contacts();
                    boolean updateRules = false;
                    while (iter.hasNext()) {
                        ContactSipImpl c = (ContactSipImpl) iter.next();
                        if (removeContactFromWhiteList(c) || updateRules) {
                            updateRules = true;
                        } else {
                            updateRules = false;
                        }
                        if (removeContactFromBlockList(c) || updateRules) {
                            updateRules = true;
                        } else {
                            updateRules = false;
                        }
                        if (removeContactFromPoliteBlockList(c) || updateRules) {
                            updateRules = true;
                        } else {
                            updateRules = false;
                        }
                    }
                    if (updateRules) {
                        updatePresRules();
                    }
                } catch (XCapException e) {
                    parentGroup.addSubgroup(group);
                    throw new IllegalStateException("Error while removing XCAP group", e);
                }
            }
            fireGroupEvent(group, 2);
        }
    }

    public synchronized void renameGroup(ContactGroupSipImpl group, String newName) {
        if (group == null) {
            throw new IllegalArgumentException("Renaming group cannot be null");
        } else if (this.rootGroup.equals(group)) {
            throw new IllegalArgumentException("Root group cannot be renamed");
        } else {
            String oldName = group.getGroupName();
            if (!oldName.equals(newName)) {
                if (((ContactGroupSipImpl) group.getParentContactGroup()).getGroup(newName) != null) {
                    throw new IllegalStateException(String.format("Group with name %1s already exists", new Object[]{newName}));
                }
                group.setName(newName);
                if (group.isPersistent()) {
                    try {
                        updateResourceLists();
                    } catch (XCapException e) {
                        group.setName(oldName);
                        throw new IllegalStateException("Error while renaming XCAP group", e);
                    }
                }
                fireGroupEvent(group, 3);
            }
        }
    }

    public synchronized void init() {
        try {
            SipAccountIDImpl accountID = (SipAccountIDImpl) this.sipProvider.getAccountID();
            if (accountID.isXCapEnable()) {
                String username;
                String password;
                String serverUri = accountID.getClistOptionServerUri();
                Address userAddress = this.sipProvider.parseAddressString(accountID.getAccountPropertyString("USER_ID"));
                if (accountID.isClistOptionUseSipCredentials()) {
                    username = ((SipUri) userAddress.getURI()).getUser();
                    password = SipActivator.getProtocolProviderFactory().loadPassword(accountID);
                } else {
                    username = accountID.getClistOptionUser();
                    password = accountID.getClistOptionPassword();
                }
                if (serverUri != null) {
                    URI uri = new URI(serverUri.trim());
                    if (!(uri.getHost() == null || uri.getPath() == null)) {
                        this.xCapClient.connect(uri, userAddress, username, password);
                    }
                }
                try {
                    if (this.xCapClient.isConnected() && this.xCapClient.isResourceListsSupported()) {
                        ResourceListsType resourceLists = this.xCapClient.getResourceLists();
                        ListType serverRootList = new ListType();
                        for (ListType list : resourceLists.getList()) {
                            if (list.getName().equals("RootGroup")) {
                                serverRootList.setName("RootGroup");
                                serverRootList.setDisplayName(list.getDisplayName());
                                serverRootList.getEntries().addAll(list.getEntries());
                                serverRootList.getEntryRefs().addAll(list.getEntryRefs());
                                serverRootList.getExternals().addAll(list.getExternals());
                                serverRootList.setAny(list.getAny());
                                serverRootList.setAnyAttributes(list.getAnyAttributes());
                            } else {
                                serverRootList.getLists().add(list);
                            }
                        }
                        boolean updateResourceLists = false;
                        resolveContactGroup(this.rootGroup, serverRootList, false);
                        for (ContactSipImpl contact : getAllContacts(this.rootGroup)) {
                            if (!contact.isResolved() && contact.isPersistent()) {
                                contact.setResolved(true);
                                ContactGroupSipImpl parentGroup = (ContactGroupSipImpl) contact.getParentContactGroup();
                                if (contact.isXCapResolved()) {
                                    parentGroup.removeContact(contact);
                                    fireContactRemoved(parentGroup, contact);
                                } else {
                                    updateResourceLists = true;
                                    String oldValue = contact.getPersistentData();
                                    contact.setXCapResolved(true);
                                    fireContactResolved(parentGroup, contact);
                                    this.parentOperationSet.fireContactPropertyChangeEvent("PersistentData", contact, oldValue, contact.getPersistentData());
                                }
                            }
                        }
                        for (ContactGroupSipImpl group : getAllGroups(this.rootGroup)) {
                            if (!group.isResolved() && group.isPersistent()) {
                                updateResourceLists = true;
                                group.setResolved(true);
                                fireGroupEvent(group, 4);
                            }
                        }
                        if (updateResourceLists) {
                            updateResourceLists();
                        }
                        if (this.xCapClient.isPresRulesSupported()) {
                            RuleType whiteRule = getRule(SubHandlingType.Allow);
                            boolean updateRules = false;
                            if (whiteRule == null) {
                                whiteRule = createWhiteRule();
                                this.presRules.getRules().add(whiteRule);
                            }
                            for (ContactSipImpl contact2 : getUniqueContacts(this.rootGroup)) {
                                if (contact2.isPersistent() && !isContactInRule(whiteRule, contact2.getUri())) {
                                    addContactToRule(whiteRule, contact2);
                                    updateRules = true;
                                }
                            }
                            if (updateRules) {
                                updatePresRules();
                            }
                        }
                    }
                } catch (XCapException e) {
                    logger.error("Error initializing serverside list!", e);
                    this.xCapClient.disconnect();
                }
            }
        } catch (Throwable ex) {
            logger.error("Error while connecting to XCAP server. Contact list won't be saved", ex);
        }
        return;
    }

    public URI getImageUri() {
        if (this.xCapClient.isConnected() && this.xCapClient.isPresContentSupported()) {
            return this.xCapClient.getPresContentImageUri(PRES_CONTENT_IMAGE_NAME);
        }
        return null;
    }

    public byte[] getImage(URI imageUri) {
        if (this.xCapClient.isConnected()) {
            try {
                return this.xCapClient.getImage(imageUri);
            } catch (XCapException e) {
                String errorMessage = String.format("Error while getting icon %1s", new Object[]{imageUri});
                logger.warn(errorMessage);
                if (logger.isDebugEnabled()) {
                    logger.debug(errorMessage, e);
                }
            }
        }
        return null;
    }

    public synchronized void destroy() {
        this.xCapClient.disconnect();
        for (ContactSipImpl contact : getAllContacts(this.rootGroup)) {
            contact.setResolved(false);
        }
        this.presRules = null;
    }

    private static RuleType createWhiteRule() {
        RuleType whiteList = new RuleType();
        whiteList.setId(DEFAULT_WHITE_RULE_ID);
        whiteList.setConditions(new ConditionsType());
        ActionsType actions = new ActionsType();
        actions.setSubHandling(SubHandlingType.Allow);
        whiteList.setActions(actions);
        TransformationsType transformations = new TransformationsType();
        ProvideServicePermissionType servicePermission = new ProvideServicePermissionType();
        servicePermission.setAllServices(new AllServicesType());
        transformations.setServicePermission(servicePermission);
        ProvidePersonPermissionType personPermission = new ProvidePersonPermissionType();
        personPermission.setAllPersons(new AllPersonsType());
        transformations.setPersonPermission(personPermission);
        ProvideDevicePermissionType devicePermission = new ProvideDevicePermissionType();
        devicePermission.setAllDevices(new AllDevicesType());
        transformations.setDevicePermission(devicePermission);
        whiteList.setTransformations(transformations);
        return whiteList;
    }

    private static RuleType createBlockRule() {
        RuleType blackList = new RuleType();
        blackList.setId(DEFAULT_BLOCK_RULE_ID);
        blackList.setConditions(new ConditionsType());
        ActionsType actions = new ActionsType();
        actions.setSubHandling(SubHandlingType.Block);
        blackList.setActions(actions);
        blackList.setTransformations(new TransformationsType());
        return blackList;
    }

    private static RuleType createPoliteBlockRule() {
        RuleType blackList = new RuleType();
        blackList.setId(DEFAULT_POLITE_BLOCK_RULE_ID);
        blackList.setConditions(new ConditionsType());
        ActionsType actions = new ActionsType();
        actions.setSubHandling(SubHandlingType.PoliteBlock);
        blackList.setActions(actions);
        blackList.setTransformations(new TransformationsType());
        return blackList;
    }

    private RuleType getRule(SubHandlingType type) throws XCapException {
        if (this.presRules == null) {
            if (!this.xCapClient.isConnected() || !this.xCapClient.isResourceListsSupported()) {
                return null;
            }
            this.presRules = this.xCapClient.getPresRules();
        }
        for (RuleType rule : this.presRules.getRules()) {
            SubHandlingType currType = rule.getActions().getSubHandling();
            if (currType != null && currType.equals(type)) {
                return rule;
            }
        }
        return null;
    }

    private static boolean isContactInRule(RuleType rule, String contactUri) {
        if (rule.getConditions().getIdentities().size() == 0) {
            return false;
        }
        for (OneType one : ((IdentityType) rule.getConditions().getIdentities().get(0)).getOneList()) {
            if (one.getId().equals(contactUri)) {
                return true;
            }
        }
        return false;
    }

    private static boolean addContactToRule(RuleType rule, ContactSipImpl contact) {
        if (isContactInRule(rule, contact.getUri())) {
            return false;
        }
        IdentityType identity;
        if (rule.getConditions().getIdentities().size() == 0) {
            identity = new IdentityType();
            rule.getConditions().getIdentities().add(identity);
        } else {
            identity = (IdentityType) rule.getConditions().getIdentities().get(0);
        }
        OneType one = new OneType();
        one.setId(contact.getUri());
        identity.getOneList().add(one);
        return true;
    }

    private static boolean removeContactFromRule(RuleType rule, ContactSipImpl contact) {
        if (rule.getConditions().getIdentities().size() == 0) {
            return false;
        }
        IdentityType identity = (IdentityType) rule.getConditions().getIdentities().get(0);
        OneType contactOne = null;
        for (OneType one : identity.getOneList()) {
            if (contact.getUri().equals(one.getId())) {
                contactOne = one;
                break;
            }
        }
        if (contactOne != null) {
            identity.getOneList().remove(contactOne);
        }
        if (identity.getOneList().size() == 0) {
            rule.getConditions().getIdentities().remove(identity);
            rule.getConditions().getIdentities().remove(identity);
        }
        return true;
    }

    /* access modifiers changed from: 0000 */
    public boolean addContactToWhiteList(ContactSipImpl contact) throws XCapException {
        RuleType whiteRule = getRule(SubHandlingType.Allow);
        RuleType blockRule = getRule(SubHandlingType.Block);
        RuleType politeBlockRule = getRule(SubHandlingType.PoliteBlock);
        if (whiteRule == null) {
            whiteRule = createWhiteRule();
            this.presRules.getRules().add(whiteRule);
        }
        boolean updateRule = addContactToRule(whiteRule, contact);
        if (blockRule != null) {
            updateRule = removeContactFromRule(blockRule, contact) || updateRule;
        }
        if (politeBlockRule == null) {
            return updateRule;
        }
        if (removeContactFromRule(politeBlockRule, contact) || updateRule) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: 0000 */
    public boolean addContactToBlockList(ContactSipImpl contact) throws XCapException {
        RuleType whiteRule = getRule(SubHandlingType.Allow);
        RuleType blockRule = getRule(SubHandlingType.Block);
        RuleType politeBlockRule = getRule(SubHandlingType.PoliteBlock);
        if (blockRule == null) {
            blockRule = createBlockRule();
            this.presRules.getRules().add(blockRule);
        }
        boolean updateRule = addContactToRule(blockRule, contact);
        if (whiteRule != null) {
            updateRule = removeContactFromRule(whiteRule, contact) || updateRule;
        }
        if (politeBlockRule == null) {
            return updateRule;
        }
        if (removeContactFromRule(politeBlockRule, contact) || updateRule) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: 0000 */
    public boolean addContactToPoliteBlockList(ContactSipImpl contact) throws XCapException {
        RuleType whiteRule = getRule(SubHandlingType.Allow);
        RuleType blockRule = getRule(SubHandlingType.Block);
        RuleType politeBlockRule = getRule(SubHandlingType.PoliteBlock);
        if (politeBlockRule == null) {
            politeBlockRule = createPoliteBlockRule();
            this.presRules.getRules().add(politeBlockRule);
        }
        boolean updateRule = addContactToRule(politeBlockRule, contact);
        if (whiteRule != null) {
            updateRule = removeContactFromRule(whiteRule, contact) || updateRule;
        }
        if (blockRule == null) {
            return updateRule;
        }
        if (removeContactFromRule(blockRule, contact) || updateRule) {
            return true;
        }
        return false;
    }

    private boolean isContactInWhiteRule(String contactUri) throws XCapException {
        RuleType whiteRule = getRule(SubHandlingType.Allow);
        if (whiteRule == null) {
            return false;
        }
        return isContactInRule(whiteRule, contactUri);
    }

    /* access modifiers changed from: 0000 */
    public boolean removeContactFromWhiteList(ContactSipImpl contact) throws XCapException {
        RuleType whiteRule = getRule(SubHandlingType.Allow);
        if (whiteRule != null) {
            return removeContactFromRule(whiteRule, contact);
        }
        return false;
    }

    /* access modifiers changed from: 0000 */
    public boolean removeContactFromBlockList(ContactSipImpl contact) throws XCapException {
        RuleType blockRule = getRule(SubHandlingType.Block);
        if (blockRule != null) {
            return removeContactFromRule(blockRule, contact);
        }
        return false;
    }

    /* access modifiers changed from: 0000 */
    public boolean removeContactFromPoliteBlockList(ContactSipImpl contact) throws XCapException {
        RuleType blockRule = getRule(SubHandlingType.PoliteBlock);
        if (blockRule != null) {
            return removeContactFromRule(blockRule, contact);
        }
        return false;
    }

    private void resolveContactGroup(ContactGroupSipImpl clientGroup, ListType serverGroup, boolean deleteUnresolved) {
        List<ContactGroupSipImpl> unresolvedGroups = new ArrayList();
        Iterator<ContactGroup> groupIterator = clientGroup.subgroups();
        while (groupIterator.hasNext()) {
            unresolvedGroups.add((ContactGroupSipImpl) groupIterator.next());
        }
        List<ContactSipImpl> unresolvedContacts = new ArrayList();
        Iterator<Contact> contactIterator = clientGroup.contacts();
        while (contactIterator.hasNext()) {
            unresolvedContacts.add((ContactSipImpl) contactIterator.next());
        }
        for (ListType serverList : serverGroup.getLists()) {
            ContactGroupSipImpl newGroup = (ContactGroupSipImpl) clientGroup.getGroup(serverList.getName());
            if (newGroup == null) {
                newGroup = new ContactGroupSipImpl(serverList.getName(), this.sipProvider);
                newGroup.setOtherAttributes(serverList.getAnyAttributes());
                newGroup.setAny(serverList.getAny());
                newGroup.setResolved(true);
                clientGroup.addSubgroup(newGroup);
                fireGroupEvent(newGroup, 1);
                resolveContactGroup(newGroup, serverList, deleteUnresolved);
            } else {
                newGroup.setResolved(true);
                newGroup.setOtherAttributes(serverList.getAnyAttributes());
                newGroup.setAny(serverList.getAny());
                unresolvedGroups.remove(newGroup);
                fireGroupEvent(newGroup, 4);
                resolveContactGroup(newGroup, serverList, deleteUnresolved);
            }
        }
        for (EntryType serverEntry : serverGroup.getEntries()) {
            ContactSipImpl newContact = (ContactSipImpl) clientGroup.getContact(serverEntry.getUri());
            if (newContact == null) {
                try {
                    newContact = new ContactSipImpl(this.sipProvider.parseAddressString(serverEntry.getUri()), this.sipProvider);
                    newContact.setDisplayName(serverEntry.getDisplayName());
                    newContact.setOtherAttributes(serverEntry.getAnyAttributes());
                    newContact.setAny(serverEntry.getAny());
                    newContact.setResolved(true);
                    newContact.setXCapResolved(true);
                    clientGroup.addContact(newContact);
                    fireContactAdded(clientGroup, newContact);
                } catch (ParseException e) {
                    logger.error(e);
                }
            } else {
                newContact.setDisplayName(serverEntry.getDisplayName());
                newContact.setOtherAttributes(serverEntry.getAnyAttributes());
                newContact.setAny(serverEntry.getAny());
                newContact.setResolved(true);
                newContact.setXCapResolved(true);
                unresolvedContacts.remove(newContact);
                fireContactResolved(clientGroup, newContact);
            }
        }
        clientGroup.getList().getExternals().addAll(serverGroup.getExternals());
        clientGroup.getList().getEntryRefs().addAll(serverGroup.getEntryRefs());
        clientGroup.getList().getAny().addAll(serverGroup.getAny());
        if (deleteUnresolved) {
            for (ContactSipImpl unresolvedContact : unresolvedContacts) {
                if (unresolvedContact.isPersistent()) {
                    unresolvedContact.setResolved(true);
                    unresolvedContact.setXCapResolved(true);
                    clientGroup.removeContact(unresolvedContact);
                    fireContactRemoved(clientGroup, unresolvedContact);
                }
            }
        }
        if (deleteUnresolved) {
            for (ContactGroupSipImpl unresolvedGroup : unresolvedGroups) {
                if (unresolvedGroup.isPersistent()) {
                    unresolvedGroup.setResolved(true);
                    clientGroup.removeSubGroup(unresolvedGroup);
                    fireGroupEvent(unresolvedGroup, 2);
                }
            }
        }
    }

    /* access modifiers changed from: declared_synchronized */
    public synchronized void updateResourceLists() throws XCapException {
        if (this.xCapClient.isConnected() && this.xCapClient.isResourceListsSupported()) {
            ResourceListsType resourceLists = new ResourceListsType();
            for (ListType list : this.rootGroup.getList().getLists()) {
                resourceLists.getList().add(list);
            }
            ListType serverRootList = new ListType();
            serverRootList.setName("RootGroup");
            serverRootList.setDisplayName(this.rootGroup.getList().getDisplayName());
            serverRootList.getEntries().addAll(this.rootGroup.getList().getEntries());
            serverRootList.getEntryRefs().addAll(this.rootGroup.getList().getEntryRefs());
            serverRootList.getExternals().addAll(this.rootGroup.getList().getExternals());
            serverRootList.setAny(this.rootGroup.getList().getAny());
            serverRootList.setAnyAttributes(this.rootGroup.getList().getAnyAttributes());
            resourceLists.getList().add(serverRootList);
            this.xCapClient.putResourceLists(resourceLists);
        }
    }

    /* access modifiers changed from: declared_synchronized */
    public synchronized void updatePresRules() throws XCapException {
        if (this.xCapClient.isConnected() && this.xCapClient.isPresRulesSupported()) {
            this.xCapClient.putPresRules(this.presRules);
        }
    }

    public void authorizationAccepted(ContactSipImpl contact) {
        try {
            if (addContactToWhiteList(contact)) {
                updatePresRules();
            }
        } catch (XCapException ex) {
            logger.error("Cannot save presence rules!", ex);
        }
    }

    public void authorizationRejected(ContactSipImpl contact) {
        try {
            if (addContactToBlockList(contact)) {
                updatePresRules();
            }
        } catch (XCapException ex) {
            logger.error("Cannot save presence rules!", ex);
        }
    }

    public void authorizationIgnored(ContactSipImpl contact) {
        try {
            if (addContactToPoliteBlockList(contact)) {
                updatePresRules();
            }
        } catch (XCapException ex) {
            logger.error("Cannot save presence rules!", ex);
        }
    }

    public ImageDetail getAccountImage() throws OperationFailedException {
        try {
            ContentType presContent = this.xCapClient.getPresContent(PRES_CONTENT_IMAGE_NAME);
            if (presContent == null) {
                return null;
            }
            String description = null;
            byte[] content = null;
            if (presContent.getDescription().size() > 0) {
                description = ((DescriptionType) presContent.getDescription().get(0)).getValue();
            }
            if (presContent.getData() != null) {
                content = Base64.decode(presContent.getData().getValue());
            }
            return new ImageDetail(description, content);
        } catch (XCapException e) {
            throw new OperationFailedException("Cannot get image detail", 2);
        }
    }

    public void deleteAccountImage() throws OperationFailedException {
        try {
            this.xCapClient.deletePresContent(PRES_CONTENT_IMAGE_NAME);
        } catch (XCapException e) {
            throw new OperationFailedException("Cannot delete image detail", 2);
        }
    }

    public boolean isAccountImageSupported() {
        return this.xCapClient != null && this.xCapClient.isConnected() && this.xCapClient.isPresContentSupported();
    }

    public void setAccountImage(byte[] newImageBytes) throws OperationFailedException {
        ContentType presContent = new ContentType();
        MimeType mimeType = new MimeType();
        mimeType.setValue("image/png");
        presContent.setMimeType(mimeType);
        EncodingType encoding = new EncodingType();
        encoding.setValue(SDPKeywords.BASE64);
        presContent.setEncoding(encoding);
        String encodedImageContent = new String(Base64.encode(newImageBytes));
        DataType data = new DataType();
        data.setValue(encodedImageContent);
        presContent.setData(data);
        try {
            this.xCapClient.putPresContent(presContent, PRES_CONTENT_IMAGE_NAME);
        } catch (XCapException e) {
            throw new OperationFailedException("Cannot put image detail", 2);
        }
    }

    public String getContactType(Contact contact) {
        if (contact instanceof ContactSipImpl) {
            for (Element e : ((ContactSipImpl) contact).getAny()) {
                if (e.getNodeName().equals(CONTACT_TYPE_ELEMENT_NAME)) {
                    return XMLUtils.getText(e);
                }
            }
            return null;
        }
        throw new IllegalArgumentException(String.format("Contact %1s does not seem to belong to this protocol's contact list", new Object[]{contact.getAddress()}));
    }

    public void setContactType(Contact contact, String contactType) {
        if (contact instanceof ContactSipImpl) {
            ContactSipImpl contactSip = (ContactSipImpl) contact;
            List<Element> anyElements = contactSip.getAny();
            Element typeElement = null;
            try {
                for (Element el : anyElements) {
                    if (el.getNodeName().equals(CONTACT_TYPE_ELEMENT_NAME)) {
                        typeElement = el;
                        break;
                    }
                }
                if (typeElement == null) {
                    typeElement = XMLUtils.createDocument().createElementNS(CONTACT_TYPE_NS, CONTACT_TYPE_ELEMENT_NAME);
                    anyElements.add(typeElement);
                }
                typeElement.setTextContent(contactType);
                contactSip.setAny(anyElements);
                return;
            } catch (Throwable t) {
                logger.error("Error creating element", t);
                return;
            }
        }
        throw new IllegalArgumentException(String.format("Contact %1s does not seem to belong to this protocol's contact list", new Object[]{contact.getAddress()}));
    }
}
