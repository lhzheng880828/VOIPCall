package org.jitsi.bouncycastle.cms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jitsi.bouncycastle.asn1.x500.X500Name;

public class RecipientInformationStore {
    private final List all;
    private final Map table = new HashMap();

    public RecipientInformationStore(Collection collection) {
        for (RecipientInformation recipientInformation : collection) {
            RecipientId rid = recipientInformation.getRID();
            List list = (ArrayList) this.table.get(rid);
            if (list == null) {
                list = new ArrayList(1);
                this.table.put(rid, list);
            }
            list.add(recipientInformation);
        }
        this.all = new ArrayList(collection);
    }

    public RecipientInformation get(RecipientId recipientId) {
        Collection recipients = getRecipients(recipientId);
        return recipients.size() == 0 ? null : (RecipientInformation) recipients.iterator().next();
    }

    public Collection getRecipients() {
        return new ArrayList(this.all);
    }

    public Collection getRecipients(RecipientId recipientId) {
        if (recipientId instanceof KeyTransRecipientId) {
            KeyTransRecipientId keyTransRecipientId = (KeyTransRecipientId) recipientId;
            X500Name issuer = keyTransRecipientId.getIssuer();
            byte[] subjectKeyIdentifier = keyTransRecipientId.getSubjectKeyIdentifier();
            if (!(issuer == null || subjectKeyIdentifier == null)) {
                ArrayList arrayList = new ArrayList();
                Collection recipients = getRecipients(new KeyTransRecipientId(issuer, keyTransRecipientId.getSerialNumber()));
                if (recipients != null) {
                    arrayList.addAll(recipients);
                }
                recipients = getRecipients(new KeyTransRecipientId(subjectKeyIdentifier));
                if (recipients != null) {
                    arrayList.addAll(recipients);
                }
                return arrayList;
            }
        }
        ArrayList arrayList2 = (ArrayList) this.table.get(recipientId);
        return arrayList2 == null ? new ArrayList() : new ArrayList(arrayList2);
    }

    public int size() {
        return this.all.size();
    }
}
