package net.java.sip.communicator.impl.protocol.sip;

import java.util.Map;
import net.java.sip.communicator.service.credentialsstorage.CredentialsStorageService;
import net.java.sip.communicator.service.protocol.sip.SipAccountID;
import net.java.sip.communicator.util.ServiceUtils;
import org.jitsi.gov.nist.core.Separators;

public class SipAccountIDImpl extends SipAccountID {
    private static String stripServerNameFromUserID(String userID) {
        int index = userID.indexOf(Separators.AT);
        return index > -1 ? userID.substring(0, index) : userID;
    }

    static String sipUriToUserID(String sipUri) {
        String userID;
        if (sipUri.indexOf("sip:") > -1) {
            userID = sipUri.substring(4);
        } else {
            userID = sipUri;
        }
        return stripServerNameFromUserID(userID);
    }

    static String sipUriToUserAddress(String sipUri) {
        if (sipUri.indexOf("sip:") > -1) {
            return sipUri.substring(4);
        }
        return sipUri;
    }

    protected SipAccountIDImpl(String userID, Map<String, String> accountProperties, String serverName) {
        super(stripServerNameFromUserID(userID), accountProperties, serverName);
    }

    public String getAccountAddress() {
        StringBuffer accountAddress = new StringBuffer();
        accountAddress.append("sip:");
        accountAddress.append(getUserID());
        String service = getService();
        if (service != null) {
            accountAddress.append('@');
            accountAddress.append(service);
        }
        return accountAddress.toString();
    }

    public String getDisplayName() {
        String protocolName = getAccountPropertyString("PROTOCOL_NAME");
        String service = getService();
        if (service == null || service.trim().length() == 0) {
            protocolName = "RegistrarLess " + protocolName;
        }
        String accountDisplayName = (String) this.accountProperties.get("ACCOUNT_DISPLAY_NAME");
        if (accountDisplayName != null && accountDisplayName.length() > 0) {
            return accountDisplayName + " (" + protocolName + Separators.RPAREN;
        }
        String returnValue = SipAccountIDImpl.super.getAccountPropertyString("USER_ID");
        if (protocolName == null || protocolName.trim().length() <= 0) {
            return returnValue;
        }
        return returnValue + " (" + protocolName + Separators.RPAREN;
    }

    public boolean equals(Object obj) {
        return SipAccountIDImpl.super.equals(obj) && ((SipAccountIDImpl) obj).getProtocolName().equals(getProtocolName()) && (getService() == null || getService().equals(((SipAccountIDImpl) obj).getService()));
    }

    public String getAccountPropertyString(Object key) {
        if (key.equals("OPT_CLIST_PASSWORD")) {
            return ((CredentialsStorageService) ServiceUtils.getService(SipActivator.getBundleContext(), CredentialsStorageService.class)).loadPassword(getAccountUniqueID() + ".xcap");
        }
        return SipAccountIDImpl.super.getAccountPropertyString(key);
    }
}
