package net.java.sip.communicator.impl.protocol.sip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URI;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.ImageDetail;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.Sha1Crypto;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.util.OSUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class ServerStoredContactListXivoImpl extends ServerStoredContactList implements Runnable {
    private static final Logger logger = Logger.getLogger(ServerStoredContactListXivoImpl.class);
    private Socket connection;
    private BufferedReader connectionReader;
    private PrintStream connectionWriter;
    private boolean stopped = false;

    ServerStoredContactListXivoImpl(ProtocolProviderServiceSipImpl sipProvider, OperationSetPresenceSipImpl parentOperationSet) {
        super(sipProvider, parentOperationSet);
    }

    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    public void init() {
        /*
        r11 = this;
        r8 = r11.sipProvider;	 Catch:{ Throwable -> 0x005b }
        r0 = r8.getAccountID();	 Catch:{ Throwable -> 0x005b }
        r0 = (net.java.sip.communicator.impl.protocol.sip.SipAccountIDImpl) r0;	 Catch:{ Throwable -> 0x005b }
        r8 = r0.isXiVOEnable();	 Catch:{ Throwable -> 0x005b }
        if (r8 != 0) goto L_0x000f;
    L_0x000e:
        return;
    L_0x000f:
        r5 = r0.isClistOptionUseSipCredentials();	 Catch:{ Throwable -> 0x005b }
        r2 = r0.getClistOptionServerUri();	 Catch:{ Throwable -> 0x005b }
        r8 = "USER_ID";
        r7 = r0.getAccountPropertyString(r8);	 Catch:{ Throwable -> 0x005b }
        r8 = r11.sipProvider;	 Catch:{ Throwable -> 0x005b }
        r6 = r8.parseAddressString(r7);	 Catch:{ Throwable -> 0x005b }
        if (r5 == 0) goto L_0x0064;
    L_0x0025:
        r8 = r6.getURI();	 Catch:{ Throwable -> 0x005b }
        r8 = (org.jitsi.gov.nist.javax.sip.address.SipUri) r8;	 Catch:{ Throwable -> 0x005b }
        r7 = r8.getUser();	 Catch:{ Throwable -> 0x005b }
    L_0x002f:
        r11.connect(r2);	 Catch:{ Throwable -> 0x0069 }
        r4 = new java.lang.Thread;	 Catch:{ Throwable -> 0x005b }
        r8 = r11.getClass();	 Catch:{ Throwable -> 0x005b }
        r8 = r8.getName();	 Catch:{ Throwable -> 0x005b }
        r4.<init>(r11, r8);	 Catch:{ Throwable -> 0x005b }
        r8 = 1;
        r4.setDaemon(r8);	 Catch:{ Throwable -> 0x005b }
        r4.start();	 Catch:{ Throwable -> 0x005b }
        r8 = r11.login(r7);	 Catch:{ Throwable -> 0x005b }
        if (r8 != 0) goto L_0x000e;
    L_0x004c:
        r8 = 0;
        r9 = 0;
        r10 = "Unauthorized. Cannot login.";
        showError(r8, r9, r10);	 Catch:{ Throwable -> 0x005b }
        r8 = logger;	 Catch:{ Throwable -> 0x005b }
        r9 = "Cannot login.";
        r8.error(r9);	 Catch:{ Throwable -> 0x005b }
        goto L_0x000e;
    L_0x005b:
        r3 = move-exception;
        r8 = logger;
        r9 = "Error init clist from xivo server";
        r8.error(r9);
        goto L_0x000e;
    L_0x0064:
        r7 = r0.getClistOptionUser();	 Catch:{ Throwable -> 0x005b }
        goto L_0x002f;
    L_0x0069:
        r1 = move-exception;
        r8 = 0;
        r9 = 0;
        showError(r1, r8, r9);	 Catch:{ Throwable -> 0x005b }
        r8 = logger;	 Catch:{ Throwable -> 0x005b }
        r9 = "Error connecting to server";
        r8.error(r9, r1);	 Catch:{ Throwable -> 0x005b }
        goto L_0x000e;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.protocol.sip.ServerStoredContactListXivoImpl.init():void");
    }

    private void connect(String serverAddress) throws IOException {
        if (serverAddress != null) {
            this.connection = new Socket(serverAddress, 5003);
        } else {
            this.connection = new Socket(this.sipProvider.getConnection().getAddress().getAddress(), 5003);
        }
        this.connectionWriter = new PrintStream(this.connection.getOutputStream());
    }

    public void destroy() {
        this.stopped = true;
        try {
            if (this.connection != null) {
                this.connection.shutdownInput();
                this.connection.close();
                this.connection = null;
            }
        } catch (IOException e) {
        }
        try {
            if (this.connectionReader != null) {
                this.connectionReader.close();
                this.connectionReader = null;
            }
        } catch (IOException e2) {
        }
        if (this.connectionWriter != null) {
            this.connectionWriter.close();
            this.connectionWriter = null;
        }
    }

    public void run() {
        if (this.connection == null) {
            logger.error("No connection.");
            return;
        }
        try {
            this.connectionReader = new BufferedReader(new InputStreamReader(this.connection.getInputStream()));
            if (this.connectionReader.readLine().contains("XiVO")) {
                while (true) {
                    String line = this.connectionReader.readLine();
                    if (line != null || !this.stopped) {
                        try {
                            if (logger.isTraceEnabled()) {
                                logger.trace("Read from server:" + line);
                            }
                            handle((JSONObject) JSONValue.parseWithException(line));
                        } catch (Throwable ex) {
                            logger.error("Error parsing object:" + line, ex);
                        }
                    } else {
                        return;
                    }
                }
            }
            logger.error("Error xivo with server!");
            destroy();
        } catch (IOException e) {
            destroy();
        }
    }

    public URI getImageUri() {
        return null;
    }

    public byte[] getImage(URI imageUri) {
        return new byte[0];
    }

    public ContactGroupSipImpl createGroup(ContactGroupSipImpl parentGroup, String groupName, boolean persistent) throws OperationFailedException {
        throw new OperationFailedException("Modification not supported.", 18);
    }

    public void renameGroup(ContactGroupSipImpl group, String newName) {
    }

    public void moveContactToGroup(ContactSipImpl contact, ContactGroupSipImpl newParentGroup) throws OperationFailedException {
        throw new OperationFailedException("Modification not supported.", 18);
    }

    public void removeGroup(ContactGroupSipImpl group) {
    }

    public ContactSipImpl createContact(ContactGroupSipImpl parentGroup, String contactId, String displayName, boolean persistent, String contactType) throws OperationFailedException {
        throw new OperationFailedException("Modification not supported.", 18);
    }

    public void removeContact(ContactSipImpl contact) throws OperationFailedException {
        throw new OperationFailedException("Modification not supported.", 18);
    }

    public void renameContact(ContactSipImpl contact, String newName) {
    }

    public void authorizationAccepted(ContactSipImpl contact) {
    }

    public void authorizationRejected(ContactSipImpl contact) {
    }

    public void authorizationIgnored(ContactSipImpl contact) {
    }

    public ImageDetail getAccountImage() throws OperationFailedException {
        throw new OperationFailedException("Modification not supported.", 18);
    }

    public void deleteAccountImage() throws OperationFailedException {
        throw new OperationFailedException("Modification not supported.", 18);
    }

    public void setAccountImage(byte[] newImageBytes) throws OperationFailedException {
        throw new OperationFailedException("Modification not supported.", 18);
    }

    public boolean isAccountImageSupported() {
        return false;
    }

    private void handle(JSONObject incomingObject) {
        if (incomingObject.containsKey("class")) {
            try {
                String classField = (String) incomingObject.get("class");
                if (classField.equals("loginko")) {
                    showError(null, null, "Unauthorized. Cannot login: " + incomingObject.get("errorstring"));
                    logger.error("Error login: " + incomingObject.get("errorstring"));
                    destroy();
                } else if (classField.equals("login_id_ok")) {
                    String password;
                    SipAccountIDImpl accountID = (SipAccountIDImpl) this.sipProvider.getAccountID();
                    if (accountID.isClistOptionUseSipCredentials()) {
                        password = SipActivator.getProtocolProviderFactory().loadPassword(accountID);
                    } else {
                        password = accountID.getClistOptionPassword();
                    }
                    if (!authorize((String) incomingObject.get("sessionid"), password)) {
                        logger.error("Error login authorization!");
                    }
                } else if (classField.equals("login_pass_ok")) {
                    if (!sendCapas((JSONArray) incomingObject.get("capalist"))) {
                        logger.error("Error send capas!");
                    }
                } else if (classField.equals("login_capas_ok")) {
                    if (!sendFeatures((String) incomingObject.get("astid"), (String) incomingObject.get("xivo_userid"))) {
                        logger.error("Problem send features get!");
                    }
                } else if (classField.equals("features")) {
                    if (!getPhoneList()) {
                        logger.error("Problem send get phones!");
                    }
                } else if (classField.equals("phones")) {
                    phonesRecieved(incomingObject);
                } else if (classField.equals("disconn")) {
                    destroy();
                } else if (logger.isTraceEnabled()) {
                    logger.trace("unhandled classField: " + incomingObject);
                }
            } catch (Throwable t) {
                logger.error("Error handling incoming object", t);
            }
        }
    }

    private boolean login(String username) {
        if (this.connection == null || username == null) {
            return false;
        }
        JSONObject obj = new JSONObject();
        try {
            obj.put("class", "login_id");
            obj.put("company", "Jitsi");
            String os = "x11";
            if (OSUtils.IS_WINDOWS) {
                os = "win";
            } else if (OSUtils.IS_MAC) {
                os = "mac";
            }
            obj.put("ident", username + Separators.AT + os);
            obj.put("userid", username);
            obj.put("version", "9999");
            obj.put("xivoversion", "1.1");
            return send(obj);
        } catch (Exception e) {
            logger.error("Error login", e);
            return false;
        }
    }

    private boolean authorize(String sessionId, String password) {
        boolean z = false;
        if (this.connection == null || sessionId == null || password == null) {
            return z;
        }
        JSONObject obj = new JSONObject();
        try {
            obj.put("class", "login_pass");
            obj.put("hashedpassword", Sha1Crypto.encode(sessionId + Separators.COLON + password));
            return send(obj);
        } catch (Exception e) {
            logger.error("Error login with password", e);
            return z;
        }
    }

    private boolean sendCapas(JSONArray capalistParam) {
        boolean z = false;
        if (this.connection == null || capalistParam == null || capalistParam.isEmpty()) {
            return z;
        }
        JSONObject obj = new JSONObject();
        try {
            obj.put("class", "login_capas");
            obj.put("capaid", capalistParam.get(0));
            obj.put("lastconnwins", "false");
            obj.put("loginkind", "agent");
            obj.put("state", "");
            return send(obj);
        } catch (Exception e) {
            logger.error("Error login", e);
            return z;
        }
    }

    private boolean sendFeatures(String astid, String xivoUserId) {
        boolean z = false;
        if (this.connection == null || astid == null || xivoUserId == null) {
            return z;
        }
        JSONObject obj = new JSONObject();
        try {
            obj.put("class", "featuresget");
            obj.put("userid", astid + Separators.SLASH + xivoUserId);
            return send(obj);
        } catch (Exception e) {
            logger.error("Error send features get command", e);
            return z;
        }
    }

    private boolean getPhoneList() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("class", "phones");
            obj.put("function", "getlist");
            return send(obj);
        } catch (Exception e) {
            logger.error("Error retrieving phones");
            return false;
        }
    }

    private boolean send(JSONObject obj) {
        if (this.connection == null || this.connectionWriter == null) {
            return false;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Send to server:" + obj);
        }
        this.connectionWriter.println(obj);
        return true;
    }

    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    private void phonesRecieved(org.json.simple.JSONObject r18) {
        /*
        r17 = this;
        r14 = "function";
        r0 = r18;
        r14 = r0.get(r14);	 Catch:{ Throwable -> 0x005e }
        r15 = "sendlist";
        r14 = r14.equals(r15);	 Catch:{ Throwable -> 0x005e }
        if (r14 == 0) goto L_0x001a;
    L_0x0010:
        r14 = "payload";
        r0 = r18;
        r14 = r0.containsKey(r14);	 Catch:{ Throwable -> 0x005e }
        if (r14 != 0) goto L_0x001b;
    L_0x001a:
        return;
    L_0x001b:
        r14 = "payload";
        r0 = r18;
        r9 = r0.get(r14);	 Catch:{ Throwable -> 0x005e }
        r9 = (org.json.simple.JSONObject) r9;	 Catch:{ Throwable -> 0x005e }
        r14 = r9.keySet();	 Catch:{ Throwable -> 0x005e }
        r5 = r14.iterator();	 Catch:{ Throwable -> 0x005e }
        r11 = new java.util.ArrayList;	 Catch:{ Throwable -> 0x005e }
        r11.<init>();	 Catch:{ Throwable -> 0x005e }
    L_0x0032:
        r14 = r5.hasNext();	 Catch:{ Throwable -> 0x005e }
        if (r14 == 0) goto L_0x0067;
    L_0x0038:
        r14 = r5.next();	 Catch:{ Throwable -> 0x005e }
        r7 = r9.get(r14);	 Catch:{ Throwable -> 0x005e }
        r7 = (org.json.simple.JSONObject) r7;	 Catch:{ Throwable -> 0x005e }
        r14 = r7.keySet();	 Catch:{ Throwable -> 0x005e }
        r12 = r14.iterator();	 Catch:{ Throwable -> 0x005e }
    L_0x004a:
        r14 = r12.hasNext();	 Catch:{ Throwable -> 0x005e }
        if (r14 == 0) goto L_0x0032;
    L_0x0050:
        r14 = r12.next();	 Catch:{ Throwable -> 0x005e }
        r14 = r7.get(r14);	 Catch:{ Throwable -> 0x005e }
        r14 = (org.json.simple.JSONObject) r14;	 Catch:{ Throwable -> 0x005e }
        r11.add(r14);	 Catch:{ Throwable -> 0x005e }
        goto L_0x004a;
    L_0x005e:
        r13 = move-exception;
        r14 = logger;
        r15 = "Error init list from server";
        r14.error(r15, r13);
        goto L_0x001a;
    L_0x0067:
        r4 = r11.iterator();	 Catch:{ Throwable -> 0x005e }
    L_0x006b:
        r14 = r4.hasNext();	 Catch:{ Throwable -> 0x005e }
        if (r14 == 0) goto L_0x001a;
    L_0x0071:
        r10 = r4.next();	 Catch:{ Throwable -> 0x005e }
        r10 = (org.json.simple.JSONObject) r10;	 Catch:{ Throwable -> 0x005e }
        r14 = "tech";
        r14 = r10.get(r14);	 Catch:{ Throwable -> 0x010c }
        r14 = (java.lang.String) r14;	 Catch:{ Throwable -> 0x010c }
        r15 = "sip";
        r14 = r14.equalsIgnoreCase(r15);	 Catch:{ Throwable -> 0x010c }
        if (r14 == 0) goto L_0x006b;
    L_0x0087:
        r14 = "context";
        r3 = r10.get(r14);	 Catch:{ Throwable -> 0x010c }
        r3 = (java.lang.String) r3;	 Catch:{ Throwable -> 0x010c }
        r0 = r17;
        r8 = r0.findGroupByName(r3);	 Catch:{ Throwable -> 0x010c }
        if (r8 != 0) goto L_0x00b1;
    L_0x0097:
        r8 = new net.java.sip.communicator.impl.protocol.sip.ContactGroupSipImpl;	 Catch:{ Throwable -> 0x010c }
        r0 = r17;
        r14 = r0.sipProvider;	 Catch:{ Throwable -> 0x010c }
        r8.m682init(r3, r14);	 Catch:{ Throwable -> 0x010c }
        r14 = 1;
        r8.setPersistent(r14);	 Catch:{ Throwable -> 0x010c }
        r14 = r17.getRootGroup();	 Catch:{ Throwable -> 0x010c }
        r14.addSubgroup(r8);	 Catch:{ Throwable -> 0x010c }
        r14 = 1;
        r0 = r17;
        r0.fireGroupEvent(r8, r14);	 Catch:{ Throwable -> 0x010c }
    L_0x00b1:
        r14 = "number";
        r6 = r10.get(r14);	 Catch:{ Throwable -> 0x010c }
        r6 = (java.lang.String) r6;	 Catch:{ Throwable -> 0x010c }
        r0 = r17;
        r14 = r0.sipProvider;	 Catch:{ Throwable -> 0x010c }
        r1 = r14.parseAddressString(r6);	 Catch:{ Throwable -> 0x010c }
        r0 = r17;
        r14 = r0.parentOperationSet;	 Catch:{ Throwable -> 0x010c }
        r15 = r1.toString();	 Catch:{ Throwable -> 0x010c }
        r2 = r14.resolveContactID(r15);	 Catch:{ Throwable -> 0x010c }
        if (r2 != 0) goto L_0x0127;
    L_0x00cf:
        r2 = new net.java.sip.communicator.impl.protocol.sip.ContactSipImpl;	 Catch:{ Throwable -> 0x010c }
        r0 = r17;
        r14 = r0.sipProvider;	 Catch:{ Throwable -> 0x010c }
        r2.m683init(r1, r14);	 Catch:{ Throwable -> 0x010c }
        r14 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x010c }
        r14.<init>();	 Catch:{ Throwable -> 0x010c }
        r15 = "firstname";
        r15 = r10.get(r15);	 Catch:{ Throwable -> 0x010c }
        r14 = r14.append(r15);	 Catch:{ Throwable -> 0x010c }
        r15 = " ";
        r14 = r14.append(r15);	 Catch:{ Throwable -> 0x010c }
        r15 = "lastname";
        r15 = r10.get(r15);	 Catch:{ Throwable -> 0x010c }
        r14 = r14.append(r15);	 Catch:{ Throwable -> 0x010c }
        r14 = r14.toString();	 Catch:{ Throwable -> 0x010c }
        r2.setDisplayName(r14);	 Catch:{ Throwable -> 0x010c }
        r14 = 1;
        r2.setResolved(r14);	 Catch:{ Throwable -> 0x010c }
        r8.addContact(r2);	 Catch:{ Throwable -> 0x010c }
        r0 = r17;
        r0.fireContactAdded(r8, r2);	 Catch:{ Throwable -> 0x010c }
        goto L_0x006b;
    L_0x010c:
        r13 = move-exception;
        r14 = logger;	 Catch:{ Throwable -> 0x005e }
        r15 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x005e }
        r15.<init>();	 Catch:{ Throwable -> 0x005e }
        r16 = "Error parsing ";
        r15 = r15.append(r16);	 Catch:{ Throwable -> 0x005e }
        r15 = r15.append(r10);	 Catch:{ Throwable -> 0x005e }
        r15 = r15.toString();	 Catch:{ Throwable -> 0x005e }
        r14.error(r15);	 Catch:{ Throwable -> 0x005e }
        goto L_0x006b;
    L_0x0127:
        r14 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x010c }
        r14.<init>();	 Catch:{ Throwable -> 0x010c }
        r15 = "firstname";
        r15 = r10.get(r15);	 Catch:{ Throwable -> 0x010c }
        r14 = r14.append(r15);	 Catch:{ Throwable -> 0x010c }
        r15 = " ";
        r14 = r14.append(r15);	 Catch:{ Throwable -> 0x010c }
        r15 = "lastname";
        r15 = r10.get(r15);	 Catch:{ Throwable -> 0x010c }
        r14 = r14.append(r15);	 Catch:{ Throwable -> 0x010c }
        r14 = r14.toString();	 Catch:{ Throwable -> 0x010c }
        r2.setDisplayName(r14);	 Catch:{ Throwable -> 0x010c }
        r14 = 1;
        r2.setResolved(r14);	 Catch:{ Throwable -> 0x010c }
        r0 = r17;
        r0.fireContactResolved(r8, r2);	 Catch:{ Throwable -> 0x010c }
        goto L_0x006b;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.protocol.sip.ServerStoredContactListXivoImpl.phonesRecieved(org.json.simple.JSONObject):void");
    }

    private ContactGroupSipImpl findGroupByName(String name) {
        for (int i = 0; i < getRootGroup().countSubgroups(); i++) {
            ContactGroupSipImpl gr = (ContactGroupSipImpl) getRootGroup().getGroup(i);
            if (gr.getGroupName().equalsIgnoreCase(name)) {
                return gr;
            }
        }
        return null;
    }

    static void showError(Throwable ex, String title, String message) {
        if (title == null) {
            title = "Error in SIP contactlist storage";
        }
        if (message == null) {
            try {
                message = title + Separators.RETURN + ex.getClass().getName() + ": " + ex.getLocalizedMessage();
            } catch (Throwable t) {
                logger.error("Error for error dialog", t);
                return;
            }
        }
        if (SipActivator.getUIService() != null) {
            SipActivator.getUIService().getPopupDialog().showMessagePopupDialog(message, title, 0);
        }
    }
}
