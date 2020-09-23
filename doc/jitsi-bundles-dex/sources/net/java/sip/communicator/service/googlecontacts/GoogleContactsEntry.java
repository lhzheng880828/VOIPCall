package net.java.sip.communicator.service.googlecontacts;

import java.util.List;
import java.util.Map;

public interface GoogleContactsEntry {

    public enum IMProtocol {
        GOOGLETALK,
        YAHOO,
        AIM,
        MSN,
        ICQ,
        JABBER,
        OTHER
    }

    void addHomeMail(String str);

    void addHomePhone(String str);

    void addIMAddress(String str, IMProtocol iMProtocol);

    void addMobilePhone(String str);

    void addWorkMails(String str);

    void addWorkPhone(String str);

    List<String> getAllMails();

    List<String> getAllPhones();

    String getFamilyName();

    String getFullName();

    String getGivenName();

    List<String> getHomeMails();

    List<String> getHomePhones();

    Map<String, IMProtocol> getIMAddresses();

    List<String> getMobilePhones();

    String getPhoto();

    List<String> getWorkMails();

    List<String> getWorkPhones();
}
