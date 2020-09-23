package net.java.sip.communicator.impl.protocol.jabber;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.OperationSetServerStoredContactInfo;
import net.java.sip.communicator.service.protocol.OperationSetServerStoredContactInfo.DetailsResponseListener;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.GenericDetail;
import net.java.sip.communicator.util.Logger;

public class OperationSetServerStoredContactInfoJabberImpl implements OperationSetServerStoredContactInfo {
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(OperationSetServerStoredContactInfoJabberImpl.class);
    /* access modifiers changed from: private */
    public InfoRetreiver infoRetreiver = null;
    /* access modifiers changed from: private */
    public Hashtable<String, List<DetailsResponseListener>> listenersForDetails = new Hashtable();

    protected OperationSetServerStoredContactInfoJabberImpl(InfoRetreiver infoRetreiver) {
        this.infoRetreiver = infoRetreiver;
    }

    /* access modifiers changed from: 0000 */
    public InfoRetreiver getInfoRetriever() {
        return this.infoRetreiver;
    }

    public <T extends GenericDetail> Iterator<T> getDetailsAndDescendants(Contact contact, Class<T> detailClass) {
        List<GenericDetail> details = this.infoRetreiver.getContactDetails(contact.getAddress());
        List<T> result = new LinkedList();
        if (details == null) {
            return result.iterator();
        }
        Iterator i$ = details.iterator();
        while (i$.hasNext()) {
            T item = (GenericDetail) i$.next();
            if (detailClass.isInstance(item)) {
                result.add(item);
            }
        }
        return result.iterator();
    }

    public Iterator<GenericDetail> getDetails(Contact contact, Class<? extends GenericDetail> detailClass) {
        List<GenericDetail> details = this.infoRetreiver.getContactDetails(contact.getAddress());
        List<GenericDetail> result = new LinkedList();
        if (details == null) {
            return result.iterator();
        }
        for (GenericDetail item : details) {
            if (detailClass.equals(item.getClass())) {
                result.add(item);
            }
        }
        return result.iterator();
    }

    public Iterator<GenericDetail> getAllDetailsForContact(Contact contact) {
        List<GenericDetail> details = this.infoRetreiver.getContactDetails(contact.getAddress());
        if (details == null) {
            return new LinkedList().iterator();
        }
        return new LinkedList(details).iterator();
    }

    public Iterator<GenericDetail> requestAllDetailsForContact(final Contact contact, DetailsResponseListener listener) {
        List<GenericDetail> res = this.infoRetreiver.getCachedContactDetails(contact.getAddress());
        if (res != null) {
            return res.iterator();
        }
        synchronized (this.listenersForDetails) {
            List<DetailsResponseListener> ls = (List) this.listenersForDetails.get(contact.getAddress());
            boolean isFirst = false;
            if (ls == null) {
                ls = new ArrayList();
                isFirst = true;
                this.listenersForDetails.put(contact.getAddress(), ls);
            }
            if (!ls.contains(listener)) {
                ls.add(listener);
            }
            if (isFirst) {
                new Thread(new Runnable() {
                    public void run() {
                        List<DetailsResponseListener> listeners;
                        List<GenericDetail> result = OperationSetServerStoredContactInfoJabberImpl.this.infoRetreiver.retrieveDetails(contact.getAddress());
                        synchronized (OperationSetServerStoredContactInfoJabberImpl.this.listenersForDetails) {
                            listeners = (List) OperationSetServerStoredContactInfoJabberImpl.this.listenersForDetails.remove(contact.getAddress());
                        }
                        if (listeners != null && result != null) {
                            for (DetailsResponseListener l : listeners) {
                                try {
                                    l.detailsRetrieved(result.iterator());
                                } catch (Throwable t) {
                                    OperationSetServerStoredContactInfoJabberImpl.logger.error("Error delivering for retrieved details", t);
                                }
                            }
                        }
                    }
                }, getClass().getName() + ".RetrieveDetails").start();
                return null;
            }
            return null;
        }
    }
}
