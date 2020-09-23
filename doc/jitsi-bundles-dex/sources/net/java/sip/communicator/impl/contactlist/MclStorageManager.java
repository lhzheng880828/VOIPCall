package net.java.sip.communicator.impl.contactlist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.java.sip.communicator.service.contactlist.MetaContact;
import net.java.sip.communicator.service.contactlist.MetaContactGroup;
import net.java.sip.communicator.service.contactlist.event.MetaContactAvatarUpdateEvent;
import net.java.sip.communicator.service.contactlist.event.MetaContactEvent;
import net.java.sip.communicator.service.contactlist.event.MetaContactGroupEvent;
import net.java.sip.communicator.service.contactlist.event.MetaContactListListener;
import net.java.sip.communicator.service.contactlist.event.MetaContactModifiedEvent;
import net.java.sip.communicator.service.contactlist.event.MetaContactMovedEvent;
import net.java.sip.communicator.service.contactlist.event.MetaContactRenamedEvent;
import net.java.sip.communicator.service.contactlist.event.ProtoContactEvent;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.ContactGroup;
import net.java.sip.communicator.util.Logger;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.fileaccess.FailSafeTransaction;
import org.jitsi.service.fileaccess.FileAccessService;
import org.jitsi.service.fileaccess.FileCategory;
import org.jitsi.util.xml.XMLException;
import org.jitsi.util.xml.XMLUtils;
import org.osgi.framework.BundleContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MclStorageManager implements MetaContactListListener {
    private static final String ACCOUNT_ID_ATTR_NAME = "account-id";
    private static final String CHILD_CONTACTS_NODE_NAME = "child-contacts";
    private static final String DEFAULT_FILE_NAME = "contactlist.xml";
    private static final String DETAIL_NAME_ATTR_NAME = "name";
    private static final String DETAIL_VALUE_ATTR_NAME = "value";
    private static String DOCUMENT_ROOT_NAME = "sip-communicator";
    private static final String FILE_NAME_PROPERTY = "net.java.sip.communicator.CONTACTLIST_FILE_NAME";
    private static final String GROUP_NAME_ATTR_NAME = "name";
    private static final String GROUP_NODE_NAME = "group";
    private static final String GROUP_UID_ATTR_NAME = "uid";
    private static final String META_CONTACT_DETAIL_NAME_NODE_NAME = "detail";
    private static final String META_CONTACT_DISPLAY_NAME_NODE_NAME = "display-name";
    private static final String META_CONTACT_NODE_NAME = "meta-contact";
    private static final String PARENT_PROTO_GROUP_UID_ATTR_NAME = "parent-proto-group-uid";
    private static final String PERSISTENT_DATA_NODE_NAME = "persistent-data";
    private static final String PROTO_CONTACT_ADDRESS_ATTR_NAME = "address";
    private static final String PROTO_CONTACT_NODE_NAME = "contact";
    private static final String PROTO_GROUPS_NODE_NAME = "proto-groups";
    private static final String PROTO_GROUP_NODE_NAME = "proto-group";
    private static final String SUBGROUPS_NODE_NAME = "subgroups";
    private static final String UID_ATTR_NAME = "uid";
    private static final String USER_DEFINED_DISPLAY_NAME_ATTR_NAME = "user-defined";
    /* access modifiers changed from: private|static|final */
    public static final Object contactListRWLock = new Object();
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(MclStorageManager.class);
    private BundleContext bundleContext = null;
    private Document contactListDocument = null;
    private File contactlistFile = null;
    private FailSafeTransaction contactlistTrans = null;
    private FileAccessService faService = null;
    /* access modifiers changed from: private */
    public boolean isModified = false;
    private MetaContactListServiceImpl mclServiceImpl = null;
    /* access modifiers changed from: private */
    public boolean started = false;

    static class StoredProtoContactDescriptor {
        String contactAddress = null;
        ContactGroup parentProtoGroup = null;
        String persistentData = null;

        StoredProtoContactDescriptor(String contactAddress, String persistentData, ContactGroup parentProtoGroup) {
            this.contactAddress = contactAddress;
            this.persistentData = persistentData;
            this.parentProtoGroup = parentProtoGroup;
        }

        public String toString() {
            return "StoredProtocoContactDescriptor[  contactAddress=" + this.contactAddress + " persistenData=" + this.persistentData + " parentProtoGroup=" + (this.parentProtoGroup == null ? "" : this.parentProtoGroup.getGroupName()) + "]";
        }

        /* access modifiers changed from: private|static */
        public static StoredProtoContactDescriptor findContactInList(String contactAddress, List<StoredProtoContactDescriptor> list) {
            if (list != null && list.size() > 0) {
                for (StoredProtoContactDescriptor desc : list) {
                    if (desc.contactAddress.equals(contactAddress)) {
                        return desc;
                    }
                }
            }
            return null;
        }
    }

    /* access modifiers changed from: 0000 */
    public boolean isStarted() {
        return this.started;
    }

    public void stop() {
        if (logger.isTraceEnabled()) {
            logger.trace("Stopping the MCL XML storage manager.");
        }
        this.started = false;
        synchronized (contactListRWLock) {
            contactListRWLock.notifyAll();
        }
    }

    /* access modifiers changed from: 0000 */
    public void start(BundleContext bc, MetaContactListServiceImpl mclServImpl) throws IOException, XMLException {
        this.bundleContext = bc;
        this.faService = (FileAccessService) this.bundleContext.getService(this.bundleContext.getServiceReference(FileAccessService.class.getName()));
        String fileName = ((ConfigurationService) this.bundleContext.getService(this.bundleContext.getServiceReference(ConfigurationService.class.getName()))).getString(FILE_NAME_PROPERTY);
        if (fileName == null) {
            fileName = System.getProperty(FILE_NAME_PROPERTY);
            if (fileName == null) {
                fileName = DEFAULT_FILE_NAME;
            }
        }
        try {
            this.contactlistFile = this.faService.getPrivatePersistentFile(fileName, FileCategory.PROFILE);
            if (this.contactlistFile.exists() || this.contactlistFile.createNewFile()) {
                try {
                    this.contactlistTrans = this.faService.createFailSafeTransaction(this.contactlistFile);
                    this.contactlistTrans.restoreFile();
                } catch (NullPointerException e) {
                    logger.error("the contactlist file is null", e);
                } catch (IllegalStateException e2) {
                    logger.error("The contactlist file can't be found", e2);
                }
                try {
                    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    if (this.contactlistFile.length() == 0) {
                        this.contactListDocument = builder.newDocument();
                        initVirginDocument(mclServImpl, this.contactListDocument);
                        storeContactList0();
                    } else {
                        try {
                            this.contactListDocument = builder.parse(this.contactlistFile);
                        } catch (Throwable ex) {
                            logger.error("Error parsing configuration file", ex);
                            logger.error("Creating replacement file");
                            this.contactlistFile.delete();
                            this.contactlistFile.createNewFile();
                            this.contactListDocument = builder.newDocument();
                            initVirginDocument(mclServImpl, this.contactListDocument);
                            storeContactList0();
                        }
                    }
                } catch (ParserConfigurationException ex2) {
                    logger.error("Error finding configuration for default parsers", ex2);
                }
                mclServImpl.addMetaContactListListener(this);
                this.mclServiceImpl = mclServImpl;
                this.started = true;
                launchStorageThread();
                return;
            }
            throw new IOException("Failed to create file" + this.contactlistFile.getAbsolutePath());
        } catch (Exception ex3) {
            logger.error("Failed to get a reference to the contact list file.", ex3);
            throw new IOException("Failed to get a reference to the contact list file=" + fileName + ". error was:" + ex3.getMessage());
        }
    }

    private void scheduleContactListStorage() throws IOException {
        synchronized (contactListRWLock) {
            if (isStarted()) {
                this.isModified = true;
                contactListRWLock.notifyAll();
                return;
            }
        }
    }

    /* access modifiers changed from: private */
    public void storeContactList0() throws IOException {
        if (logger.isTraceEnabled()) {
            logger.trace("storing contact list. because is started ==" + isStarted());
        }
        if (logger.isTraceEnabled()) {
            logger.trace("storing contact list. because is modified ==" + this.isModified);
        }
        if (isStarted()) {
            try {
                this.contactlistTrans.beginTransaction();
            } catch (IllegalStateException e) {
                logger.error("the contactlist file is missing", e);
            }
            OutputStream stream = new FileOutputStream(this.contactlistFile);
            XMLUtils.indentedWriteXML(this.contactListDocument, stream);
            stream.close();
            try {
                this.contactlistTrans.commit();
            } catch (IllegalStateException e2) {
                logger.error("the contactlist file is missing", e2);
            }
        }
    }

    private void launchStorageThread() {
        new Thread() {
            public void run() {
                try {
                    synchronized (MclStorageManager.contactListRWLock) {
                        while (MclStorageManager.this.isStarted()) {
                            MclStorageManager.contactListRWLock.wait(5000);
                            if (MclStorageManager.this.isModified) {
                                MclStorageManager.this.storeContactList0();
                                MclStorageManager.this.isModified = false;
                            }
                        }
                    }
                } catch (IOException ex) {
                    MclStorageManager.logger.error("Storing contact list failed", ex);
                    MclStorageManager.this.started = false;
                } catch (InterruptedException ex2) {
                    MclStorageManager.logger.error("Storing contact list failed", ex2);
                    MclStorageManager.this.started = false;
                }
            }
        }.start();
    }

    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    public void storeContactListAndStopStorageManager() {
        /*
        r4 = this;
        r2 = contactListRWLock;
        monitor-enter(r2);
        r1 = r4.isStarted();	 Catch:{ all -> 0x0018 }
        if (r1 != 0) goto L_0x000b;
    L_0x0009:
        monitor-exit(r2);	 Catch:{ all -> 0x0018 }
    L_0x000a:
        return;
    L_0x000b:
        r1 = 0;
        r4.started = r1;	 Catch:{ all -> 0x0018 }
        r1 = contactListRWLock;	 Catch:{ all -> 0x0018 }
        r1.notifyAll();	 Catch:{ all -> 0x0018 }
        r4.storeContactList0();	 Catch:{ IOException -> 0x001b }
    L_0x0016:
        monitor-exit(r2);	 Catch:{ all -> 0x0018 }
        goto L_0x000a;
    L_0x0018:
        r1 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x0018 }
        throw r1;
    L_0x001b:
        r0 = move-exception;
        r1 = logger;	 Catch:{ all -> 0x0018 }
        r3 = "Failed to store contact list before stopping";
        r1.debug(r3, r0);	 Catch:{ all -> 0x0018 }
        goto L_0x0016;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.contactlist.MclStorageManager.storeContactListAndStopStorageManager():void");
    }

    private void updatePersistentDataForMetaContact(MetaContact metaContact) {
        Element metaContactNode = findMetaContactNode(metaContact.getMetaUID());
        Iterator<Contact> iter = metaContact.getContacts();
        while (iter.hasNext()) {
            Contact item = (Contact) iter.next();
            String persistentData = item.getPersistentData();
            if (persistentData != null) {
                Element currentNode = XMLUtils.locateElement(metaContactNode, PROTO_CONTACT_NODE_NAME, PROTO_CONTACT_ADDRESS_ATTR_NAME, item.getAddress());
                Element persistentDataNode = XMLUtils.findChild(currentNode, PERSISTENT_DATA_NODE_NAME);
                if (persistentDataNode == null) {
                    persistentDataNode = this.contactListDocument.createElement(PERSISTENT_DATA_NODE_NAME);
                    currentNode.appendChild(persistentDataNode);
                }
                XMLUtils.setText(persistentDataNode, persistentData);
            }
        }
    }

    private void initVirginDocument(MetaContactListServiceImpl mclServImpl, Document contactListDoc) {
        Element root = contactListDoc.createElement(DOCUMENT_ROOT_NAME);
        contactListDoc.appendChild(root);
        root.appendChild(createMetaContactGroupNode(mclServImpl.getRoot()));
    }

    /* access modifiers changed from: 0000 */
    public void extractContactsForAccount(String accountID) throws XMLException {
        if (isStarted()) {
            this.mclServiceImpl.removeMetaContactListListener(this);
            try {
                Element root = findMetaContactGroupNode(this.mclServiceImpl.getRoot().getMetaUID());
                if (root == null) {
                    logger.fatal("The contactlist file is recreated cause its broken");
                    this.contactListDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                    initVirginDocument(this.mclServiceImpl, this.contactListDocument);
                    storeContactList0();
                } else {
                    processGroupXmlNode(this.mclServiceImpl, accountID, root, null, null);
                    scheduleContactListStorage();
                }
                this.mclServiceImpl.addMetaContactListListener(this);
            } catch (Throwable th) {
                this.mclServiceImpl.addMetaContactListListener(this);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:95:0x0032 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x019d A:{Catch:{ Throwable -> 0x01a5, Throwable -> 0x01d9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x019d A:{Catch:{ Throwable -> 0x01a5, Throwable -> 0x01d9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x0032 A:{SYNTHETIC} */
    private void processGroupXmlNode(net.java.sip.communicator.impl.contactlist.MetaContactListServiceImpl r52, java.lang.String r53, org.w3c.dom.Element r54, net.java.sip.communicator.impl.contactlist.MetaContactGroupImpl r55, java.util.Map<java.lang.String, net.java.sip.communicator.service.protocol.ContactGroup> r56) {
        /*
        r51 = this;
        r20 = new java.util.Hashtable;
        r20.<init>();
        if (r55 != 0) goto L_0x0035;
    L_0x0007:
        r0 = r52;
        r5 = r0.rootMetaGroup;
    L_0x000b:
        r4 = "child-contacts";
        r0 = r54;
        r22 = org.jitsi.util.xml.XMLUtils.findChild(r0, r4);
        if (r22 != 0) goto L_0x00d7;
    L_0x0015:
        r21 = 0;
    L_0x0017:
        r36 = 0;
    L_0x0019:
        if (r21 == 0) goto L_0x0201;
    L_0x001b:
        r4 = r21.getLength();
        r0 = r36;
        if (r0 >= r4) goto L_0x0201;
    L_0x0023:
        r0 = r21;
        r1 = r36;
        r24 = r0.item(r1);
        r4 = r24.getNodeType();
        r9 = 1;
        if (r4 == r9) goto L_0x00dd;
    L_0x0032:
        r36 = r36 + 1;
        goto L_0x0019;
    L_0x0035:
        r4 = "uid";
        r0 = r54;
        r35 = org.jitsi.util.xml.XMLUtils.getAttribute(r0, r4);
        r4 = "name";
        r0 = r54;
        r34 = org.jitsi.util.xml.XMLUtils.getAttribute(r0, r4);
        r0 = r52;
        r1 = r55;
        r2 = r35;
        r3 = r34;
        r5 = r0.loadStoredMetaContactGroup(r1, r2, r3);
        r4 = "proto-groups";
        r0 = r54;
        r45 = org.jitsi.util.xml.XMLUtils.findChild(r0, r4);
        r44 = r45.getChildNodes();
        r36 = 0;
    L_0x005f:
        r4 = r44.getLength();
        r0 = r36;
        if (r0 >= r4) goto L_0x00d0;
    L_0x0067:
        r0 = r44;
        r1 = r36;
        r25 = r0.item(r1);
        r4 = r25.getNodeType();
        r9 = 1;
        if (r4 == r9) goto L_0x0079;
    L_0x0076:
        r36 = r36 + 1;
        goto L_0x005f;
    L_0x0079:
        r4 = "account-id";
        r0 = r25;
        r33 = org.jitsi.util.xml.XMLUtils.getAttribute(r0, r4);
        r0 = r53;
        r1 = r33;
        r4 = r0.equals(r1);
        if (r4 == 0) goto L_0x0076;
    L_0x008b:
        r4 = "uid";
        r0 = r25;
        r6 = org.jitsi.util.xml.XMLUtils.getAttribute(r0, r4);
        r4 = "parent-proto-group-uid";
        r0 = r25;
        r42 = org.jitsi.util.xml.XMLUtils.getAttribute(r0, r4);
        r25 = (org.w3c.dom.Element) r25;
        r4 = "persistent-data";
        r0 = r25;
        r43 = org.jitsi.util.xml.XMLUtils.findChild(r0, r4);
        r8 = "";
        if (r43 == 0) goto L_0x00ad;
    L_0x00a9:
        r8 = org.jitsi.util.xml.XMLUtils.getText(r43);
    L_0x00ad:
        r7 = 0;
        if (r56 == 0) goto L_0x00c0;
    L_0x00b0:
        r4 = r56.size();
        if (r4 <= 0) goto L_0x00c0;
    L_0x00b6:
        r0 = r56;
        r1 = r42;
        r7 = r0.get(r1);
        r7 = (net.java.sip.communicator.service.protocol.ContactGroup) r7;
    L_0x00c0:
        r4 = r52;
        r9 = r53;
        r41 = r4.loadStoredContactGroup(r5, r6, r7, r8, r9);
        r0 = r20;
        r1 = r41;
        r0.put(r6, r1);
        goto L_0x0076;
    L_0x00d0:
        r4 = r20.size();
        if (r4 != 0) goto L_0x000b;
    L_0x00d6:
        return;
    L_0x00d7:
        r21 = r22.getChildNodes();
        goto L_0x0017;
    L_0x00dd:
        r4 = "uid";
        r0 = r24;
        r11 = org.jitsi.util.xml.XMLUtils.getAttribute(r0, r4);	 Catch:{ Throwable -> 0x01a5 }
        r0 = r24;
        r0 = (org.w3c.dom.Element) r0;	 Catch:{ Throwable -> 0x01a5 }
        r4 = r0;
        r9 = "display-name";
        r29 = org.jitsi.util.xml.XMLUtils.findChild(r4, r9);	 Catch:{ Throwable -> 0x01a5 }
        r12 = org.jitsi.util.xml.XMLUtils.getText(r29);	 Catch:{ Throwable -> 0x01a5 }
        r4 = "user-defined";
        r0 = r29;
        r4 = r0.getAttribute(r4);	 Catch:{ Throwable -> 0x01a5 }
        r4 = java.lang.Boolean.valueOf(r4);	 Catch:{ Throwable -> 0x01a5 }
        r38 = r4.booleanValue();	 Catch:{ Throwable -> 0x01a5 }
        r0 = r24;
        r0 = (org.w3c.dom.Element) r0;	 Catch:{ Throwable -> 0x01a5 }
        r4 = r0;
        r0 = r51;
        r1 = r53;
        r2 = r20;
        r14 = r0.extractProtoContacts(r4, r1, r2);	 Catch:{ Throwable -> 0x01a5 }
        r4 = r14.size();	 Catch:{ Throwable -> 0x01a5 }
        r9 = 1;
        if (r4 < r9) goto L_0x0032;
    L_0x011a:
        r13 = 0;
        r0 = r24;
        r0 = (org.w3c.dom.Element) r0;	 Catch:{ Exception -> 0x029a }
        r4 = r0;
        r9 = "detail";
        r27 = org.jitsi.util.xml.XMLUtils.findChildren(r4, r9);	 Catch:{ Exception -> 0x029a }
        r4 = r27.size();	 Catch:{ Exception -> 0x029a }
        if (r4 <= 0) goto L_0x0192;
    L_0x012c:
        r26 = new java.util.Hashtable;	 Catch:{ Exception -> 0x029a }
        r26.<init>();	 Catch:{ Exception -> 0x029a }
        r37 = r27.iterator();	 Catch:{ Exception -> 0x0173 }
    L_0x0135:
        r4 = r37.hasNext();	 Catch:{ Exception -> 0x0173 }
        if (r4 == 0) goto L_0x029d;
    L_0x013b:
        r31 = r37.next();	 Catch:{ Exception -> 0x0173 }
        r31 = (org.w3c.dom.Element) r31;	 Catch:{ Exception -> 0x0173 }
        r4 = "name";
        r0 = r31;
        r40 = r0.getAttribute(r4);	 Catch:{ Exception -> 0x0173 }
        r4 = "value";
        r0 = r31;
        r50 = r0.getAttribute(r4);	 Catch:{ Exception -> 0x0173 }
        r0 = r26;
        r1 = r40;
        r28 = r0.get(r1);	 Catch:{ Exception -> 0x0173 }
        r28 = (java.util.List) r28;	 Catch:{ Exception -> 0x0173 }
        if (r28 != 0) goto L_0x01f8;
    L_0x015d:
        r30 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0173 }
        r30.<init>();	 Catch:{ Exception -> 0x0173 }
        r0 = r30;
        r1 = r50;
        r0.add(r1);	 Catch:{ Exception -> 0x0173 }
        r0 = r26;
        r1 = r40;
        r2 = r30;
        r0.put(r1, r2);	 Catch:{ Exception -> 0x0173 }
        goto L_0x0135;
    L_0x0173:
        r32 = move-exception;
        r13 = r26;
    L_0x0176:
        r4 = logger;	 Catch:{ Throwable -> 0x01a5 }
        r9 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x01a5 }
        r9.<init>();	 Catch:{ Throwable -> 0x01a5 }
        r10 = "Cannot load details for contact node ";
        r9 = r9.append(r10);	 Catch:{ Throwable -> 0x01a5 }
        r0 = r24;
        r9 = r9.append(r0);	 Catch:{ Throwable -> 0x01a5 }
        r9 = r9.toString();	 Catch:{ Throwable -> 0x01a5 }
        r0 = r32;
        r4.error(r9, r0);	 Catch:{ Throwable -> 0x01a5 }
    L_0x0192:
        r9 = r52;
        r10 = r5;
        r15 = r53;
        r39 = r9.loadStoredMetaContact(r10, r11, r12, r13, r14, r15);	 Catch:{ Throwable -> 0x01a5 }
        if (r38 == 0) goto L_0x0032;
    L_0x019d:
        r4 = 1;
        r0 = r39;
        r0.setDisplayNameUserDefined(r4);	 Catch:{ Throwable -> 0x01a5 }
        goto L_0x0032;
    L_0x01a5:
        r48 = move-exception;
        r4 = logger;
        r9 = new java.lang.StringBuilder;
        r9.<init>();
        r10 = "Failed to parse meta contact ";
        r9 = r9.append(r10);
        r0 = r24;
        r9 = r9.append(r0);
        r10 = ". Will remove and continue with other contacts";
        r9 = r9.append(r10);
        r9 = r9.toString();
        r0 = r48;
        r4.warn(r9, r0);
        r4 = r24.getParentNode();
        if (r4 == 0) goto L_0x0032;
    L_0x01ce:
        r4 = r24.getParentNode();	 Catch:{ Throwable -> 0x01d9 }
        r0 = r24;
        r4.removeChild(r0);	 Catch:{ Throwable -> 0x01d9 }
        goto L_0x0032;
    L_0x01d9:
        r49 = move-exception;
        r4 = logger;
        r9 = new java.lang.StringBuilder;
        r9.<init>();
        r10 = "Failed to remove meta contact node ";
        r9 = r9.append(r10);
        r0 = r24;
        r9 = r9.append(r0);
        r9 = r9.toString();
        r0 = r49;
        r4.error(r9, r0);
        goto L_0x0032;
    L_0x01f8:
        r0 = r28;
        r1 = r50;
        r0.add(r1);	 Catch:{ Exception -> 0x0173 }
        goto L_0x0135;
    L_0x0201:
        r4 = "subgroups";
        r0 = r54;
        r47 = org.jitsi.util.xml.XMLUtils.findChild(r0, r4);
        if (r47 == 0) goto L_0x00d6;
    L_0x020b:
        r46 = r47.getChildNodes();
        r36 = 0;
    L_0x0211:
        r4 = r46.getLength();
        r0 = r36;
        if (r0 >= r4) goto L_0x00d6;
    L_0x0219:
        r0 = r46;
        r1 = r36;
        r23 = r0.item(r1);
        r4 = r23.getNodeType();
        r9 = 1;
        if (r4 != r9) goto L_0x0234;
    L_0x0228:
        r4 = r23.getNodeName();
        r9 = "group";
        r4 = r4.equals(r9);
        if (r4 != 0) goto L_0x0237;
    L_0x0234:
        r36 = r36 + 1;
        goto L_0x0211;
    L_0x0237:
        r0 = r23;
        r0 = (org.w3c.dom.Element) r0;	 Catch:{ Throwable -> 0x0249 }
        r18 = r0;
        r15 = r51;
        r16 = r52;
        r17 = r53;
        r19 = r5;
        r15.processGroupXmlNode(r16, r17, r18, r19, r20);	 Catch:{ Throwable -> 0x0249 }
        goto L_0x0234;
    L_0x0249:
        r49 = move-exception;
        r4 = logger;
        r9 = new java.lang.StringBuilder;
        r9.<init>();
        r10 = "Failed to process group node ";
        r9 = r9.append(r10);
        r0 = r23;
        r9 = r9.append(r0);
        r10 = ". Removing.";
        r9 = r9.append(r10);
        r9 = r9.toString();
        r0 = r49;
        r4.error(r9, r0);
        r4 = r23.getParentNode();
        if (r4 == 0) goto L_0x0234;
    L_0x0272:
        r4 = r23.getParentNode();	 Catch:{ Throwable -> 0x027c }
        r0 = r23;
        r4.removeChild(r0);	 Catch:{ Throwable -> 0x027c }
        goto L_0x0234;
    L_0x027c:
        r48 = move-exception;
        r4 = logger;
        r9 = new java.lang.StringBuilder;
        r9.<init>();
        r10 = "Failed to remove group node ";
        r9 = r9.append(r10);
        r0 = r23;
        r9 = r9.append(r0);
        r9 = r9.toString();
        r0 = r48;
        r4.error(r9, r0);
        goto L_0x0234;
    L_0x029a:
        r32 = move-exception;
        goto L_0x0176;
    L_0x029d:
        r13 = r26;
        goto L_0x0192;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.contactlist.MclStorageManager.processGroupXmlNode(net.java.sip.communicator.impl.contactlist.MetaContactListServiceImpl, java.lang.String, org.w3c.dom.Element, net.java.sip.communicator.impl.contactlist.MetaContactGroupImpl, java.util.Map):void");
    }

    private List<StoredProtoContactDescriptor> extractProtoContacts(Element metaContactNode, String accountID, Map<String, ContactGroup> protoGroups) {
        if (logger.isTraceEnabled()) {
            logger.trace("Extracting proto contacts for " + XMLUtils.getAttribute(metaContactNode, "uid"));
        }
        List<StoredProtoContactDescriptor> protoContacts = new LinkedList();
        NodeList children = metaContactNode.getChildNodes();
        List<Node> duplicates = new LinkedList();
        for (int i = 0; i < children.getLength(); i++) {
            Node currentNode = children.item(i);
            if (currentNode.getNodeName() != null && currentNode.getNodeType() == (short) 1 && currentNode.getNodeName().equals(PROTO_CONTACT_NODE_NAME)) {
                if (accountID.equals(XMLUtils.getAttribute(currentNode, ACCOUNT_ID_ATTR_NAME))) {
                    String contactAddress = XMLUtils.getAttribute(currentNode, PROTO_CONTACT_ADDRESS_ATTR_NAME);
                    if (StoredProtoContactDescriptor.findContactInList(contactAddress, protoContacts) != null) {
                        duplicates.add(currentNode);
                    } else {
                        String protoGroupUID = XMLUtils.getAttribute(currentNode, PARENT_PROTO_GROUP_UID_ATTR_NAME);
                        Element persistentDataNode = XMLUtils.findChild((Element) currentNode, PERSISTENT_DATA_NODE_NAME);
                        protoContacts.add(new StoredProtoContactDescriptor(contactAddress, persistentDataNode == null ? "" : XMLUtils.getText(persistentDataNode), (ContactGroup) protoGroups.get(protoGroupUID)));
                    }
                }
            }
        }
        for (Node node : duplicates) {
            metaContactNode.removeChild(node);
        }
        return protoContacts;
    }

    private Element createProtoContactNode(Contact protoContact) {
        Element protoContactElement = this.contactListDocument.createElement(PROTO_CONTACT_NODE_NAME);
        protoContactElement.setAttribute(PROTO_CONTACT_ADDRESS_ATTR_NAME, protoContact.getAddress());
        protoContactElement.setAttribute(ACCOUNT_ID_ATTR_NAME, protoContact.getProtocolProvider().getAccountID().getAccountUniqueID());
        if (logger.isTraceEnabled() && protoContact.getParentContactGroup() == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("the following contact looks weird:" + protoContact);
            }
            if (logger.isTraceEnabled()) {
                logger.trace("group:" + protoContact.getParentContactGroup());
            }
        }
        protoContactElement.setAttribute(PARENT_PROTO_GROUP_UID_ATTR_NAME, protoContact.getParentContactGroup().getUID());
        String persistentData = protoContact.getPersistentData();
        if (!(persistentData == null || persistentData.length() == 0)) {
            Element persDataNode = this.contactListDocument.createElement(PERSISTENT_DATA_NODE_NAME);
            XMLUtils.setText(persDataNode, persistentData);
            protoContactElement.appendChild(persDataNode);
        }
        return protoContactElement;
    }

    private Element createProtoContactGroupNode(ContactGroup protoGroup) {
        Element protoGroupElement = this.contactListDocument.createElement(PROTO_GROUP_NODE_NAME);
        protoGroupElement.setAttribute("uid", protoGroup.getUID());
        protoGroupElement.setAttribute(ACCOUNT_ID_ATTR_NAME, protoGroup.getProtocolProvider().getAccountID().getAccountUniqueID());
        ContactGroup parentContactGroup = protoGroup.getParentContactGroup();
        if (parentContactGroup != null) {
            protoGroupElement.setAttribute(PARENT_PROTO_GROUP_UID_ATTR_NAME, parentContactGroup.getUID());
        }
        String persistentData = protoGroup.getPersistentData();
        if (!(persistentData == null || persistentData.length() == 0)) {
            Element persDataNode = this.contactListDocument.createElement(PERSISTENT_DATA_NODE_NAME);
            XMLUtils.setText(persDataNode, persistentData);
            protoGroupElement.appendChild(persDataNode);
        }
        return protoGroupElement;
    }

    private Element createMetaContactNode(MetaContact metaContact) {
        Element metaContactElement = this.contactListDocument.createElement(META_CONTACT_NODE_NAME);
        metaContactElement.setAttribute("uid", metaContact.getMetaUID());
        Element displayNameNode = this.contactListDocument.createElement(META_CONTACT_DISPLAY_NAME_NODE_NAME);
        displayNameNode.appendChild(this.contactListDocument.createTextNode(metaContact.getDisplayName()));
        if (((MetaContactImpl) metaContact).isDisplayNameUserDefined()) {
            displayNameNode.setAttribute(USER_DEFINED_DISPLAY_NAME_ATTR_NAME, Boolean.TRUE.toString());
        }
        metaContactElement.appendChild(displayNameNode);
        Iterator<Contact> contacts = metaContact.getContacts();
        while (contacts.hasNext()) {
            metaContactElement.appendChild(createProtoContactNode((Contact) contacts.next()));
        }
        return metaContactElement;
    }

    private Element createMetaContactGroupNode(MetaContactGroup metaGroup) {
        Element metaGroupElement = this.contactListDocument.createElement(GROUP_NODE_NAME);
        metaGroupElement.setAttribute("name", metaGroup.getGroupName());
        metaGroupElement.setAttribute("uid", metaGroup.getMetaUID());
        Element protoGroupsElement = this.contactListDocument.createElement(PROTO_GROUPS_NODE_NAME);
        metaGroupElement.appendChild(protoGroupsElement);
        Iterator<ContactGroup> protoGroups = metaGroup.getContactGroups();
        while (protoGroups.hasNext()) {
            ContactGroup group = (ContactGroup) protoGroups.next();
            if (group.isPersistent()) {
                protoGroupsElement.appendChild(createProtoContactGroupNode(group));
            }
        }
        Element subgroupsElement = this.contactListDocument.createElement(SUBGROUPS_NODE_NAME);
        metaGroupElement.appendChild(subgroupsElement);
        Iterator<MetaContactGroup> subgroups = metaGroup.getSubgroups();
        while (subgroups.hasNext()) {
            subgroupsElement.appendChild(createMetaContactGroupNode((MetaContactGroup) subgroups.next()));
        }
        Element childContactsElement = this.contactListDocument.createElement(CHILD_CONTACTS_NODE_NAME);
        metaGroupElement.appendChild(childContactsElement);
        Iterator<MetaContact> childContacts = metaGroup.getChildContacts();
        while (childContacts.hasNext()) {
            childContactsElement.appendChild(createMetaContactNode((MetaContact) childContacts.next()));
        }
        return metaGroupElement;
    }

    public void metaContactAdded(MetaContactEvent evt) {
        Element parentGroupNode = findMetaContactGroupNode(evt.getParentGroup().getMetaUID());
        if (parentGroupNode == null) {
            logger.error("Couldn't find parent of a newly added contact: " + evt.getSourceMetaContact());
            if (logger.isTraceEnabled()) {
                logger.trace("The above exception occurred with the following stack trace: ", new Exception());
                return;
            }
            return;
        }
        XMLUtils.findChild(parentGroupNode, CHILD_CONTACTS_NODE_NAME).appendChild(createMetaContactNode(evt.getSourceMetaContact()));
        try {
            scheduleContactListStorage();
        } catch (IOException ex) {
            logger.error("Writing CL failed after adding contact " + evt.getSourceMetaContact(), ex);
        }
    }

    public void metaContactGroupAdded(MetaContactGroupEvent evt) {
        if (evt.getSourceProtoGroup() == null || evt.getSourceProtoGroup().isPersistent()) {
            MetaContactGroup parentGroup = evt.getSourceMetaContactGroup().getParentMetaContactGroup();
            Element parentGroupNode = findMetaContactGroupNode(parentGroup.getMetaUID());
            if (parentGroupNode == null) {
                logger.error("Couldn't find parent of a newly added group: " + parentGroup);
                return;
            }
            XMLUtils.findChild(parentGroupNode, SUBGROUPS_NODE_NAME).appendChild(createMetaContactGroupNode(evt.getSourceMetaContactGroup()));
            try {
                scheduleContactListStorage();
            } catch (IOException ex) {
                logger.error("Writing CL failed after adding contact " + evt.getSourceMetaContactGroup(), ex);
            }
        }
    }

    public void metaContactGroupRemoved(MetaContactGroupEvent evt) {
        Element metaContactGroupNode = findMetaContactGroupNode(evt.getSourceMetaContactGroup().getMetaUID());
        if (metaContactGroupNode == null) {
            logger.error("Save after removing an MN group. Groupt not found: " + evt.getSourceMetaContactGroup());
            return;
        }
        metaContactGroupNode.getParentNode().removeChild(metaContactGroupNode);
        try {
            scheduleContactListStorage();
        } catch (IOException ex) {
            logger.error("Writing CL failed after removing group " + evt.getSourceMetaContactGroup(), ex);
        }
    }

    public void metaContactMoved(MetaContactMovedEvent evt) {
        Element metaContactNode = findMetaContactNode(evt.getSourceMetaContact().getMetaUID());
        Element newParentNode = findMetaContactGroupNode(evt.getNewParent().getMetaUID());
        if (newParentNode == null) {
            logger.error("Save after metacontact moved. new parent not found: " + evt.getNewParent());
            if (logger.isTraceEnabled()) {
                logger.error("The above exception has occurred with the following stack trace", new Exception());
                return;
            }
            return;
        }
        if (metaContactNode == null) {
            metaContactNode = createMetaContactNode(evt.getSourceMetaContact());
        } else {
            metaContactNode.getParentNode().removeChild(metaContactNode);
        }
        updateParentsForMetaContactNode(metaContactNode, evt.getNewParent());
        XMLUtils.findChild(newParentNode, CHILD_CONTACTS_NODE_NAME).appendChild(metaContactNode);
        try {
            scheduleContactListStorage();
        } catch (IOException ex) {
            logger.error("Writing CL failed after moving " + evt.getSourceMetaContact(), ex);
        }
    }

    private void updateParentsForMetaContactNode(Element metaContactNode, MetaContactGroup newParent) {
        NodeList children = metaContactNode.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node currentNode = children.item(i);
            if (currentNode.getNodeType() == (short) 1 && currentNode.getNodeName().equals(PROTO_CONTACT_NODE_NAME)) {
                Element contactElement = (Element) currentNode;
                String attribute = contactElement.getAttribute(PARENT_PROTO_GROUP_UID_ATTR_NAME);
                if (!(attribute == null || attribute.trim().length() == 0)) {
                    contactElement.setAttribute(PARENT_PROTO_GROUP_UID_ATTR_NAME, ((ContactGroup) newParent.getContactGroupsForAccountID(contactElement.getAttribute(ACCOUNT_ID_ATTR_NAME)).next()).getUID());
                }
            }
        }
    }

    public void metaContactRemoved(MetaContactEvent evt) {
        Element metaContactNode = findMetaContactNode(evt.getSourceMetaContact().getMetaUID());
        if (metaContactNode == null) {
            logger.error("Save after metacontact removed. Contact not found: " + evt.getSourceMetaContact());
            return;
        }
        metaContactNode.getParentNode().removeChild(metaContactNode);
        try {
            scheduleContactListStorage();
        } catch (IOException ex) {
            logger.error("Writing CL failed after removing " + evt.getSourceMetaContact(), ex);
        }
    }

    public void metaContactRenamed(MetaContactRenamedEvent evt) {
        Element metaContactNode = findMetaContactNode(evt.getSourceMetaContact().getMetaUID());
        if (metaContactNode == null) {
            logger.error("Save after renam failed. Contact not found: " + evt.getSourceMetaContact());
            return;
        }
        Element displayNameNode = XMLUtils.findChild(metaContactNode, META_CONTACT_DISPLAY_NAME_NODE_NAME);
        if (((MetaContactImpl) evt.getSourceMetaContact()).isDisplayNameUserDefined()) {
            displayNameNode.setAttribute(USER_DEFINED_DISPLAY_NAME_ATTR_NAME, Boolean.TRUE.toString());
        } else {
            displayNameNode.removeAttribute(USER_DEFINED_DISPLAY_NAME_ATTR_NAME);
        }
        XMLUtils.setText(displayNameNode, evt.getNewDisplayName());
        updatePersistentDataForMetaContact(evt.getSourceMetaContact());
        try {
            scheduleContactListStorage();
        } catch (IOException ex) {
            logger.error("Writing CL failed after rename of " + evt.getSourceMetaContact(), ex);
        }
    }

    public void protoContactModified(ProtoContactEvent evt) {
        if (findMetaContactNode(evt.getParent().getMetaUID()) == null) {
            logger.error("Save after proto contact modification failed. Contact not found: " + evt.getParent());
            return;
        }
        updatePersistentDataForMetaContact(evt.getParent());
        try {
            scheduleContactListStorage();
        } catch (IOException ex) {
            logger.error("Writing CL failed after rename of " + evt.getParent(), ex);
        }
    }

    public void metaContactModified(MetaContactModifiedEvent evt) {
        String name = evt.getModificationName();
        Element metaContactNode = findMetaContactNode(evt.getSourceMetaContact().getMetaUID());
        if (metaContactNode == null) {
            logger.error("Save after rename failed. Contact not found: " + evt.getSourceMetaContact());
            return;
        }
        List<?> oldValue = evt.getOldValue();
        Object newValue = evt.getNewValue();
        boolean isChanged = false;
        if (oldValue != null || newValue == null) {
            if (oldValue == null || newValue != null) {
                if (!(oldValue == null || newValue == null)) {
                    Element changedElement = null;
                    for (Element e : XMLUtils.locateElements(metaContactNode, META_CONTACT_DETAIL_NAME_NODE_NAME, "name", name)) {
                        if (e.getAttribute("value").equals(oldValue)) {
                            changedElement = e;
                            break;
                        }
                    }
                    if (changedElement != null) {
                        changedElement.setAttribute("value", (String) newValue);
                        isChanged = true;
                    } else {
                        return;
                    }
                }
            } else if (oldValue instanceof List) {
                List<?> valuesToRemove = oldValue;
                List<Element> nodes = XMLUtils.locateElements(metaContactNode, META_CONTACT_DETAIL_NAME_NODE_NAME, "name", name);
                List<Element> nodesToRemove = new ArrayList();
                for (Element e2 : nodes) {
                    if (valuesToRemove.contains(e2.getAttribute("value"))) {
                        nodesToRemove.add(e2);
                    }
                }
                for (Element e22 : nodesToRemove) {
                    metaContactNode.removeChild(e22);
                }
                if (nodesToRemove.size() > 0) {
                    isChanged = true;
                }
            } else if (oldValue instanceof String) {
                Element elementToRemove = null;
                for (Element e222 : XMLUtils.locateElements(metaContactNode, META_CONTACT_DETAIL_NAME_NODE_NAME, "name", name)) {
                    if (e222.getAttribute("value").equals(oldValue)) {
                        elementToRemove = e222;
                        break;
                    }
                }
                if (elementToRemove != null) {
                    metaContactNode.removeChild(elementToRemove);
                    isChanged = true;
                } else {
                    return;
                }
            }
        } else if (newValue instanceof String) {
            Element detailElement = this.contactListDocument.createElement(META_CONTACT_DETAIL_NAME_NODE_NAME);
            detailElement.setAttribute("name", name);
            detailElement.setAttribute("value", (String) newValue);
            metaContactNode.appendChild(detailElement);
            isChanged = true;
        } else {
            return;
        }
        if (isChanged) {
            try {
                scheduleContactListStorage();
            } catch (IOException ex) {
                logger.error("Writing CL failed after rename of " + evt.getSourceMetaContact(), ex);
            }
        }
    }

    public void protoContactRemoved(ProtoContactEvent evt) {
        Element oldMcNode = findMetaContactNode(evt.getOldParent().getMetaUID());
        if (oldMcNode == null) {
            logger.error("Failed to find meta contact (old parent): " + oldMcNode);
            return;
        }
        Element protoNode = XMLUtils.locateElement(oldMcNode, PROTO_CONTACT_NODE_NAME, PROTO_CONTACT_ADDRESS_ATTR_NAME, evt.getProtoContact().getAddress());
        protoNode.getParentNode().removeChild(protoNode);
        try {
            scheduleContactListStorage();
        } catch (IOException ex) {
            logger.error("Writing CL failed after removing proto contact " + evt.getProtoContact(), ex);
        }
    }

    public void childContactsReordered(MetaContactGroupEvent evt) {
    }

    public void metaContactGroupModified(MetaContactGroupEvent evt) {
        MetaContactGroup mcGroup = evt.getSourceMetaContactGroup();
        Element mcGroupNode = findMetaContactGroupNode(mcGroup.getMetaUID());
        if (mcGroupNode == null) {
            logger.error("Failed to find meta contact group: " + mcGroup);
            if (logger.isTraceEnabled()) {
                logger.trace("The above error occurred with the following stack trace: ", new Exception());
                return;
            }
            return;
        }
        switch (evt.getEventID()) {
            case 3:
            case 5:
            case 6:
                Node parentNode = mcGroupNode.getParentNode();
                parentNode.removeChild(mcGroupNode);
                parentNode.appendChild(createMetaContactGroupNode(mcGroup));
                try {
                    scheduleContactListStorage();
                    break;
                } catch (IOException ex) {
                    logger.error("Writing CL failed after adding contact " + mcGroup, ex);
                    break;
                }
            case 7:
                mcGroupNode.setAttribute("name", mcGroup.getGroupName());
                break;
        }
        try {
            scheduleContactListStorage();
        } catch (IOException ex2) {
            logger.error("Writing CL failed after removing proto group " + mcGroup.getGroupName(), ex2);
        }
    }

    public void protoContactAdded(ProtoContactEvent evt) {
        Element mcNode = findMetaContactNode(evt.getParent().getMetaUID());
        if (mcNode == null) {
            logger.error("Failed to find meta contact: " + evt.getParent());
            return;
        }
        mcNode.appendChild(createProtoContactNode(evt.getProtoContact()));
        try {
            scheduleContactListStorage();
        } catch (IOException ex) {
            logger.error("Writing CL failed after adding proto contact " + evt.getProtoContact(), ex);
        }
    }

    public void protoContactMoved(ProtoContactEvent evt) {
        Element newMcNode = findMetaContactNode(evt.getNewParent().getMetaUID());
        Element oldMcNode = findMetaContactNode(evt.getOldParent().getMetaUID());
        if (oldMcNode == null) {
            logger.error("Failed to find meta contact (old parent): " + oldMcNode);
        } else if (newMcNode == null) {
            logger.error("Failed to find meta contact (old parent): " + newMcNode);
        } else {
            Element protoNode = XMLUtils.locateElement(oldMcNode, PROTO_CONTACT_NODE_NAME, PROTO_CONTACT_ADDRESS_ATTR_NAME, evt.getProtoContact().getAddress());
            protoNode.getParentNode().removeChild(protoNode);
            protoNode.setAttribute(PARENT_PROTO_GROUP_UID_ATTR_NAME, evt.getProtoContact().getParentContactGroup().getUID());
            newMcNode.appendChild(protoNode);
            try {
                scheduleContactListStorage();
            } catch (IOException ex) {
                logger.error("Writing CL failed after moving proto contact " + evt.getProtoContact(), ex);
            }
        }
    }

    private Element findMetaContactNode(String metaContactUID) {
        return XMLUtils.locateElement((Element) this.contactListDocument.getFirstChild(), META_CONTACT_NODE_NAME, "uid", metaContactUID);
    }

    private Element findMetaContactGroupNode(String metaContactGroupUID) {
        return XMLUtils.locateElement((Element) this.contactListDocument.getFirstChild(), GROUP_NODE_NAME, "uid", metaContactGroupUID);
    }

    /* access modifiers changed from: 0000 */
    public void removeContactListFile() {
        this.contactlistFile.delete();
    }

    public void metaContactAvatarUpdated(MetaContactAvatarUpdateEvent evt) {
    }
}
