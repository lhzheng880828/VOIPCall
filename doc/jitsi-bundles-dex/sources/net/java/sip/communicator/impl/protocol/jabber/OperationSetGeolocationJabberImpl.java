package net.java.sip.communicator.impl.protocol.jabber;

import java.util.List;
import java.util.Map;
import java.util.Vector;
import net.java.sip.communicator.impl.protocol.jabber.extensions.geolocation.GeolocationJabberUtils;
import net.java.sip.communicator.impl.protocol.jabber.extensions.geolocation.GeolocationPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.geolocation.GeolocationPacketExtensionProvider;
import net.java.sip.communicator.impl.protocol.jabber.extensions.geolocation.GeolocationPresence;
import net.java.sip.communicator.service.protocol.OperationSetGeolocation;
import net.java.sip.communicator.service.protocol.OperationSetPersistentPresence;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.event.GeolocationEvent;
import net.java.sip.communicator.service.protocol.event.GeolocationListener;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketExtensionFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;

public class OperationSetGeolocationJabberImpl implements OperationSetGeolocation {
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(OperationSetGeolocationJabberImpl.class);
    /* access modifiers changed from: private|final */
    public final List<GeolocationListener> geolocationContactsListeners = new Vector();
    /* access modifiers changed from: private|final */
    public final ProtocolProviderServiceJabberImpl jabberProvider;
    /* access modifiers changed from: private|final */
    public final OperationSetPersistentPresence opsetprez;

    private class GeolocationPresencePacketListener implements PacketListener {
        private GeolocationPresencePacketListener() {
        }

        public void processPacket(Packet packet) {
            String from = StringUtils.parseBareAddress(packet.getFrom());
            GeolocationPacketExtension geolocExt = (GeolocationPacketExtension) packet.getExtension(GeolocationPacketExtensionProvider.ELEMENT_NAME, GeolocationPacketExtensionProvider.NAMESPACE);
            if (geolocExt != null) {
                if (OperationSetGeolocationJabberImpl.logger.isDebugEnabled()) {
                    OperationSetGeolocationJabberImpl.logger.debug("GeolocationExtension found from " + from + Separators.COLON + geolocExt.toXML());
                }
                fireGeolocationContactChangeEvent(from, GeolocationJabberUtils.convertExtensionToMap(geolocExt));
            }
        }

        public void fireGeolocationContactChangeEvent(String sourceContact, Map<String, String> newGeolocation) {
            if (OperationSetGeolocationJabberImpl.logger.isDebugEnabled()) {
                OperationSetGeolocationJabberImpl.logger.debug("Trying to dispatch geolocation contact update for " + sourceContact);
            }
            GeolocationEvent evt = new GeolocationEvent(OperationSetGeolocationJabberImpl.this.opsetprez.findContactByID(sourceContact), OperationSetGeolocationJabberImpl.this.jabberProvider, newGeolocation, OperationSetGeolocationJabberImpl.this);
            if (OperationSetGeolocationJabberImpl.logger.isDebugEnabled()) {
                OperationSetGeolocationJabberImpl.logger.debug("Dispatching  geolocation contact update. Listeners=" + OperationSetGeolocationJabberImpl.this.geolocationContactsListeners.size() + " evt=" + evt);
            }
            synchronized (OperationSetGeolocationJabberImpl.this.geolocationContactsListeners) {
            }
            for (GeolocationListener listener : (GeolocationListener[]) OperationSetGeolocationJabberImpl.this.geolocationContactsListeners.toArray(new GeolocationListener[OperationSetGeolocationJabberImpl.this.geolocationContactsListeners.size()])) {
                listener.contactGeolocationChanged(evt);
            }
        }
    }

    private class RegistrationStateListener implements RegistrationStateChangeListener {
        private RegistrationStateListener() {
        }

        public void registrationStateChanged(RegistrationStateChangeEvent evt) {
            if (OperationSetGeolocationJabberImpl.logger.isDebugEnabled()) {
                OperationSetGeolocationJabberImpl.logger.debug("The Jabber provider changed state from: " + evt.getOldState() + " to: " + evt.getNewState());
            }
            if (evt.getNewState() == RegistrationState.REGISTERED) {
                try {
                    OperationSetGeolocationJabberImpl.this.jabberProvider.getConnection().addPacketListener(new GeolocationPresencePacketListener(), new PacketExtensionFilter(GeolocationPacketExtensionProvider.ELEMENT_NAME, GeolocationPacketExtensionProvider.NAMESPACE));
                    return;
                } catch (Exception e) {
                    OperationSetGeolocationJabberImpl.logger.error(e);
                    return;
                }
            }
            if (evt.getNewState() == RegistrationState.UNREGISTERED || evt.getNewState() == RegistrationState.AUTHENTICATION_FAILED || evt.getNewState() == RegistrationState.CONNECTION_FAILED) {
            }
        }
    }

    public OperationSetGeolocationJabberImpl(ProtocolProviderServiceJabberImpl provider) {
        this.jabberProvider = provider;
        this.opsetprez = (OperationSetPersistentPresence) provider.getOperationSet(OperationSetPersistentPresence.class);
        this.jabberProvider.addRegistrationStateChangeListener(new RegistrationStateListener());
        ProviderManager.getInstance().addExtensionProvider(GeolocationPacketExtensionProvider.ELEMENT_NAME, GeolocationPacketExtensionProvider.NAMESPACE, new GeolocationPacketExtensionProvider());
    }

    public void publishGeolocation(Map<String, String> geolocation) {
        GeolocationPresence myGeolocPrez = new GeolocationPresence(this.opsetprez);
        myGeolocPrez.setGeolocationExtention(GeolocationJabberUtils.convertMapToExtension(geolocation));
        this.jabberProvider.getConnection().sendPacket(myGeolocPrez.getGeolocPresence());
    }

    public Map<String, String> queryContactGeolocation(String contactIdentifier) {
        return null;
    }

    public void addGeolocationListener(GeolocationListener listener) {
        synchronized (this.geolocationContactsListeners) {
            this.geolocationContactsListeners.add(listener);
        }
    }

    public void removeGeolocationListener(GeolocationListener listener) {
        synchronized (this.geolocationContactsListeners) {
            this.geolocationContactsListeners.remove(listener);
        }
    }
}
