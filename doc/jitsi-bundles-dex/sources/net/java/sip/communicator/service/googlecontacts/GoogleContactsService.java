package net.java.sip.communicator.service.googlecontacts;

import java.util.List;

public interface GoogleContactsService {
    void addContactSource(String str, String str2);

    void addContactSource(GoogleContactsConnection googleContactsConnection, boolean z);

    GoogleContactsConnection getConnection(String str, String str2);

    List<GoogleContactsEntry> getContacts();

    void removeContactSource(String str);

    void removeContactSource(GoogleContactsConnection googleContactsConnection);

    List<GoogleContactsEntry> searchContact(GoogleContactsConnection googleContactsConnection, GoogleQuery googleQuery, int i, GoogleEntryCallback googleEntryCallback);
}
