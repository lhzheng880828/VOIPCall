package net.java.sip.communicator.impl.protocol.jabber;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import net.java.sip.communicator.impl.protocol.jabber.extensions.caps.EntityCapsManager.Caps;
import net.java.sip.communicator.impl.protocol.jabber.extensions.caps.UserCapsNodeListener;
import net.java.sip.communicator.service.protocol.ContactResource;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.event.ContactResourceEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.util.StringUtils;

public class MobileIndicator implements RegistrationStateChangeListener, UserCapsNodeListener {
    private static final String MOBILE_INDICATOR_CAPS_ACC_PROP = "MOBILE_INDICATOR_CAPS";
    private static final String MOBILE_INDICATOR_RESOURCE_ACC_PROP = "MOBILE_INDICATOR_RESOURCE";
    private final String[] checkStrings;
    private boolean isCapsMobileIndicator = true;
    private final ProtocolProviderServiceJabberImpl parentProvider;
    private final ServerStoredContactListJabberImpl ssclCallback;

    public MobileIndicator(ProtocolProviderServiceJabberImpl parentProvider, ServerStoredContactListJabberImpl ssclCallback) {
        this.parentProvider = parentProvider;
        this.ssclCallback = ssclCallback;
        String indicatorResource = (String) parentProvider.getAccountID().getAccountProperties().get(MOBILE_INDICATOR_RESOURCE_ACC_PROP);
        if (indicatorResource == null || indicatorResource.length() <= 0) {
            String indicatorCaps = (String) parentProvider.getAccountID().getAccountProperties().get(MOBILE_INDICATOR_CAPS_ACC_PROP);
            if (indicatorCaps == null || indicatorCaps.length() == 0) {
                indicatorCaps = "android";
            }
            this.checkStrings = indicatorCaps.split(Separators.COMMA);
            this.parentProvider.addRegistrationStateChangeListener(this);
            return;
        }
        this.isCapsMobileIndicator = false;
        this.checkStrings = indicatorResource.split(Separators.COMMA);
    }

    public void resourcesUpdated(ContactJabberImpl contact) {
        if (this.isCapsMobileIndicator) {
            updateMobileIndicatorUsingCaps(contact.getAddress());
            return;
        }
        int highestPriority = Integer.MIN_VALUE;
        List<ContactResource> highestPriorityResources = new ArrayList();
        for (ContactResource res : contact.getResources()) {
            if (res.getPresenceStatus().isOnline()) {
                int prio = res.getPriority();
                if (prio >= highestPriority) {
                    if (highestPriority != prio) {
                        highestPriorityResources.clear();
                    }
                    highestPriority = prio;
                    highestPriorityResources.add(res);
                }
            }
        }
        boolean allMobile = false;
        for (ContactResource res2 : highestPriorityResources) {
            if (!res2.isMobile()) {
                allMobile = false;
                break;
            }
            allMobile = true;
        }
        if (highestPriorityResources.size() > 0) {
            contact.setMobile(allMobile);
        } else {
            contact.setMobile(false);
        }
    }

    /* access modifiers changed from: 0000 */
    public boolean isMobileResource(String resourceName, String fullJid) {
        if (this.isCapsMobileIndicator) {
            Caps caps = this.ssclCallback.getParentProvider().getDiscoveryManager().getCapsManager().getCapsByUser(fullJid);
            if (caps == null || !containsStrings(caps.node, this.checkStrings)) {
                return false;
            }
            return true;
        } else if (startsWithStrings(resourceName, this.checkStrings)) {
            return true;
        } else {
            return false;
        }
    }

    public void registrationStateChanged(RegistrationStateChangeEvent evt) {
        if (evt.getNewState() == RegistrationState.REGISTERED) {
            this.parentProvider.getDiscoveryManager().getCapsManager().addUserCapsNodeListener(this);
        }
    }

    public void userCapsNodeAdded(String user, String node, boolean online) {
        updateMobileIndicatorUsingCaps(user);
    }

    public void userCapsNodeRemoved(String user, String node, boolean online) {
        updateMobileIndicatorUsingCaps(user);
    }

    private void updateMobileIndicatorUsingCaps(String user) {
        ContactJabberImpl contact = this.ssclCallback.findContactById(StringUtils.parseBareAddress(user));
        if (contact != null) {
            int currentMostConnectedStatus = 0;
            List<ContactResource> mostAvailableResources = new ArrayList();
            for (Entry<String, ContactResourceJabberImpl> resEntry : contact.getResourcesMap().entrySet()) {
                ContactResourceJabberImpl res = (ContactResourceJabberImpl) resEntry.getValue();
                if (res.getPresenceStatus().isOnline()) {
                    boolean oldIndicator = res.isMobile();
                    res.setMobile(isMobileResource(res.getResourceName(), res.getFullJid()));
                    if (oldIndicator != res.isMobile()) {
                        contact.fireContactResourceEvent(new ContactResourceEvent(contact, res, 2));
                    }
                    int status = res.getPresenceStatus().getStatus();
                    if (status > currentMostConnectedStatus) {
                        if (currentMostConnectedStatus != status) {
                            mostAvailableResources.clear();
                        }
                        currentMostConnectedStatus = status;
                        mostAvailableResources.add(res);
                    }
                }
            }
            boolean allMobile = false;
            for (ContactResource res2 : mostAvailableResources) {
                if (!res2.isMobile()) {
                    allMobile = false;
                    break;
                }
                allMobile = true;
            }
            if (mostAvailableResources.size() > 0) {
                contact.setMobile(allMobile);
            } else {
                contact.setMobile(false);
            }
        }
    }

    private static boolean startsWithStrings(String value, String[] checkStrs) {
        for (String str : checkStrs) {
            if (str.length() > 0 && value.startsWith(str)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsStrings(String value, String[] checkStrs) {
        for (String str : checkStrs) {
            if (str.length() > 0 && value.contains(str)) {
                return true;
            }
        }
        return false;
    }
}
