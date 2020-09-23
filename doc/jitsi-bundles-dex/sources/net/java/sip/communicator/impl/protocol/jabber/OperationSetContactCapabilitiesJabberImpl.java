package net.java.sip.communicator.impl.protocol.jabber;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.java.sip.communicator.impl.protocol.jabber.extensions.caps.EntityCapsManager;
import net.java.sip.communicator.impl.protocol.jabber.extensions.caps.UserCapsNodeListener;
import net.java.sip.communicator.impl.protocol.jabber.extensions.messagecorrection.MessageCorrectionExtension;
import net.java.sip.communicator.service.protocol.AbstractOperationSetContactCapabilities;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.OperationSet;
import net.java.sip.communicator.service.protocol.OperationSetBasicInstantMessaging;
import net.java.sip.communicator.service.protocol.OperationSetBasicTelephony;
import net.java.sip.communicator.service.protocol.OperationSetDesktopSharingServer;
import net.java.sip.communicator.service.protocol.OperationSetMessageCorrection;
import net.java.sip.communicator.service.protocol.OperationSetPresence;
import net.java.sip.communicator.service.protocol.OperationSetServerStoredContactInfo;
import net.java.sip.communicator.service.protocol.OperationSetVideoTelephony;
import net.java.sip.communicator.service.protocol.event.ContactPresenceStatusChangeEvent;
import net.java.sip.communicator.service.protocol.event.ContactPresenceStatusListener;
import net.java.sip.communicator.util.Logger;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

public class OperationSetContactCapabilitiesJabberImpl extends AbstractOperationSetContactCapabilities<ProtocolProviderServiceJabberImpl> implements UserCapsNodeListener, ContactPresenceStatusListener {
    private static final Map<Class<? extends OperationSet>, String[]> CAPS_OPERATION_SETS_TO_FEATURES = new HashMap();
    private static final Set<Class<? extends OperationSet>> OFFLINE_OPERATION_SETS = new HashSet();
    private static final Map<Class<? extends OperationSet>, String[]> OPERATION_SETS_TO_FEATURES = new HashMap();
    private static final Logger logger = Logger.getLogger(OperationSetContactCapabilitiesJabberImpl.class);
    private EntityCapsManager capsManager;

    static {
        OFFLINE_OPERATION_SETS.add(OperationSetBasicInstantMessaging.class);
        OFFLINE_OPERATION_SETS.add(OperationSetMessageCorrection.class);
        OFFLINE_OPERATION_SETS.add(OperationSetServerStoredContactInfo.class);
        OPERATION_SETS_TO_FEATURES.put(OperationSetBasicTelephony.class, new String[]{"urn:xmpp:jingle:1", "urn:xmpp:jingle:apps:rtp:1", ProtocolProviderServiceJabberImpl.URN_XMPP_JINGLE_RTP_AUDIO});
        OPERATION_SETS_TO_FEATURES.put(OperationSetVideoTelephony.class, new String[]{"urn:xmpp:jingle:1", "urn:xmpp:jingle:apps:rtp:1", ProtocolProviderServiceJabberImpl.URN_XMPP_JINGLE_RTP_VIDEO});
        OPERATION_SETS_TO_FEATURES.put(OperationSetDesktopSharingServer.class, new String[]{"urn:xmpp:jingle:1", "urn:xmpp:jingle:apps:rtp:1", ProtocolProviderServiceJabberImpl.URN_XMPP_JINGLE_RTP_VIDEO});
        OPERATION_SETS_TO_FEATURES.put(OperationSetMessageCorrection.class, new String[]{MessageCorrectionExtension.NAMESPACE});
        CAPS_OPERATION_SETS_TO_FEATURES.put(OperationSetBasicTelephony.class, new String[]{ProtocolProviderServiceJabberImpl.CAPS_GTALK_WEB_VOICE});
        CAPS_OPERATION_SETS_TO_FEATURES.put(OperationSetVideoTelephony.class, new String[]{ProtocolProviderServiceJabberImpl.CAPS_GTALK_WEB_VOICE, ProtocolProviderServiceJabberImpl.CAPS_GTALK_WEB_VIDEO});
    }

    public OperationSetContactCapabilitiesJabberImpl(ProtocolProviderServiceJabberImpl parentProvider) {
        super(parentProvider);
        OperationSetPresence presenceOpSet = (OperationSetPresence) parentProvider.getOperationSet(OperationSetPresence.class);
        if (presenceOpSet != null) {
            presenceOpSet.addContactPresenceStatusListener(this);
        }
    }

    /* access modifiers changed from: protected */
    public <U extends OperationSet> U getOperationSet(Contact contact, Class<U> opsetClass, boolean online) {
        String jid = ((ProtocolProviderServiceJabberImpl) this.parentProvider).getFullJid(contact);
        if (jid == null) {
            jid = contact.getAddress();
        }
        return getOperationSet(jid, (Class) opsetClass, online);
    }

    /* access modifiers changed from: protected */
    public Map<String, OperationSet> getSupportedOperationSets(Contact contact, boolean online) {
        String jid = ((ProtocolProviderServiceJabberImpl) this.parentProvider).getFullJid(contact);
        if (jid == null) {
            jid = contact.getAddress();
        }
        return getSupportedOperationSets(jid, online);
    }

    private Map<String, OperationSet> getSupportedOperationSets(String jid, boolean online) {
        Map<String, OperationSet> supportedOperationSets = ((ProtocolProviderServiceJabberImpl) this.parentProvider).getSupportedOperationSets();
        int supportedOperationSetCount = supportedOperationSets.size();
        Map<String, OperationSet> contactSupportedOperationSets = new HashMap(supportedOperationSetCount);
        if (supportedOperationSetCount != 0) {
            for (Entry<String, OperationSet> supportedOperationSetEntry : supportedOperationSets.entrySet()) {
                Class opsetClass;
                String opsetClassName = (String) supportedOperationSetEntry.getKey();
                try {
                    opsetClass = Class.forName(opsetClassName);
                } catch (ClassNotFoundException cnfex) {
                    opsetClass = null;
                    logger.error("Failed to get OperationSet class for name: " + opsetClassName, cnfex);
                }
                if (opsetClass != null) {
                    OperationSet opset = getOperationSet(jid, opsetClass, online);
                    if (opset != null) {
                        contactSupportedOperationSets.put(opsetClassName, opset);
                    }
                }
            }
        }
        return contactSupportedOperationSets;
    }

    private <U extends OperationSet> U getOperationSet(String jid, Class<U> opsetClass, boolean online) {
        U opset = ((ProtocolProviderServiceJabberImpl) this.parentProvider).getOperationSet(opsetClass);
        if (opset == null) {
            return null;
        }
        if (online) {
            if (OPERATION_SETS_TO_FEATURES.containsKey(opsetClass)) {
                String[] features = (String[]) OPERATION_SETS_TO_FEATURES.get(opsetClass);
                if (features == null || !(features.length == 0 || ((ProtocolProviderServiceJabberImpl) this.parentProvider).isFeatureListSupported(jid, features))) {
                    if (CAPS_OPERATION_SETS_TO_FEATURES.containsKey(opsetClass)) {
                        String[] extFeatures = (String[]) CAPS_OPERATION_SETS_TO_FEATURES.get(opsetClass);
                        if (!((ProtocolProviderServiceJabberImpl) this.parentProvider).isGTalkTesting()) {
                            opset = null;
                        } else if (extFeatures == null || !(extFeatures.length == 0 || ((ProtocolProviderServiceJabberImpl) this.parentProvider).isExtFeatureListSupported(jid, extFeatures))) {
                            opset = null;
                        }
                    } else {
                        opset = null;
                    }
                }
            }
            return opset;
        }
        return OFFLINE_OPERATION_SETS.contains(opsetClass) ? opset : null;
    }

    private void setCapsManager(EntityCapsManager capsManager) {
        if (this.capsManager != capsManager) {
            if (this.capsManager != null) {
                this.capsManager.removeUserCapsNodeListener(this);
            }
            this.capsManager = capsManager;
            if (this.capsManager != null) {
                this.capsManager.addUserCapsNodeListener(this);
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void setDiscoveryManager(ScServiceDiscoveryManager discoveryManager) {
        setCapsManager(discoveryManager == null ? null : discoveryManager.getCapsManager());
    }

    public void userCapsNodeAdded(String user, String node, boolean online) {
        userCapsNodeRemoved(user, node, online);
    }

    public void userCapsNodeRemoved(String user, String node, boolean online) {
        OperationSetPresence opsetPresence = (OperationSetPresence) ((ProtocolProviderServiceJabberImpl) this.parentProvider).getOperationSet(OperationSetPresence.class);
        if (opsetPresence != null) {
            Contact contact = opsetPresence.findContactByID(StringUtils.parseBareAddress(user));
            if (contact == null) {
                return;
            }
            if (online) {
                fireContactCapabilitiesEvent(contact, 1, getSupportedOperationSets(user, online));
            } else {
                fireContactCapabilitiesEvent(contact, 1, getSupportedOperationSets(contact));
            }
        }
    }

    public void contactPresenceStatusChanged(ContactPresenceStatusChangeEvent evt) {
        if (this.capsManager != null && evt.getNewStatus().getStatus() < 20) {
            this.capsManager.removeContactCapsNode(evt.getSourceContact());
        }
    }

    public void fireContactCapabilitiesChanged(String user) {
        OperationSetPresence opsetPresence = (OperationSetPresence) ((ProtocolProviderServiceJabberImpl) this.parentProvider).getOperationSet(OperationSetPresence.class);
        if (opsetPresence != null) {
            Contact contact = opsetPresence.findContactByID(StringUtils.parseBareAddress(user));
            boolean online = false;
            Presence presence = ((ProtocolProviderServiceJabberImpl) this.parentProvider).getConnection().getRoster().getPresence(user);
            if (presence != null) {
                online = presence.isAvailable();
            }
            if (contact != null) {
                fireContactCapabilitiesEvent(contact, 1, getSupportedOperationSets(user, online));
            }
        }
    }
}
