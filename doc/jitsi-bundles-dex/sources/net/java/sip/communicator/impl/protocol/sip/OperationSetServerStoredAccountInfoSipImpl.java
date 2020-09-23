package net.java.sip.communicator.impl.protocol.sip;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import net.java.sip.communicator.service.protocol.AbstractOperationSetServerStoredAccountInfo;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetPersistentPresence;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.DisplayNameDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.GenericDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.ImageDetail;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import net.java.sip.communicator.util.Logger;

public class OperationSetServerStoredAccountInfoSipImpl extends AbstractOperationSetServerStoredAccountInfo implements RegistrationStateChangeListener {
    private static final Logger logger = Logger.getLogger(OperationSetServerStoredAccountInfoSipImpl.class);
    private ImageDetail accountImage;
    private DisplayNameDetail displayNameDetail;
    private boolean isAccountImageLoaded = false;
    private ProtocolProviderServiceSipImpl provider;

    public OperationSetServerStoredAccountInfoSipImpl(ProtocolProviderServiceSipImpl provider) {
        this.provider = provider;
        this.provider.addRegistrationStateChangeListener(this);
    }

    public <T extends GenericDetail> Iterator<T> getDetailsAndDescendants(Class<T> detailClass) {
        List<T> result = new Vector();
        if (ImageDetail.class.isAssignableFrom(detailClass) && isImageDetailSupported()) {
            if (getAccountImage() != null) {
                result.add(getAccountImage());
            }
        } else if (DisplayNameDetail.class.isAssignableFrom(detailClass) && this.displayNameDetail != null) {
            result.add(this.displayNameDetail);
        }
        return result.iterator();
    }

    public Iterator<GenericDetail> getDetails(Class<? extends GenericDetail> detailClass) {
        List<GenericDetail> result = new ArrayList();
        if (ImageDetail.class.isAssignableFrom(detailClass) && isImageDetailSupported()) {
            if (getAccountImage() != null) {
                result.add(getAccountImage());
            }
        } else if (DisplayNameDetail.class.isAssignableFrom(detailClass) && this.displayNameDetail != null) {
            result.add(this.displayNameDetail);
        }
        return result.iterator();
    }

    public Iterator<GenericDetail> getAllAvailableDetails() {
        List<GenericDetail> details = new ArrayList();
        if (isImageDetailSupported() && getAccountImage() != null) {
            details.add(getAccountImage());
        }
        if (this.displayNameDetail != null) {
            details.add(this.displayNameDetail);
        }
        return details.iterator();
    }

    public Iterator<Class<? extends GenericDetail>> getSupportedDetailTypes() {
        List<Class<? extends GenericDetail>> result = new Vector();
        if (isImageDetailSupported()) {
            result.add(ImageDetail.class);
        }
        result.add(DisplayNameDetail.class);
        return result.iterator();
    }

    public boolean isDetailClassSupported(Class<? extends GenericDetail> detailClass) {
        return (ImageDetail.class.isAssignableFrom(detailClass) && isImageDetailSupported()) || DisplayNameDetail.class.isAssignableFrom(detailClass);
    }

    public boolean isDetailClassEditable(Class<? extends GenericDetail> detailClass) {
        return isDetailClassSupported(detailClass) && ImageDetail.class.isAssignableFrom(detailClass);
    }

    public int getMaxDetailInstances(Class<? extends GenericDetail> detailClass) {
        if ((ImageDetail.class.isAssignableFrom(detailClass) && isImageDetailSupported()) || DisplayNameDetail.class.isAssignableFrom(detailClass)) {
            return 1;
        }
        return 0;
    }

    public void addDetail(GenericDetail detail) throws IllegalArgumentException, OperationFailedException, ArrayIndexOutOfBoundsException {
        addDetail(detail, true);
    }

    public void addDetail(GenericDetail detail, boolean fireChangeEvents) throws IllegalArgumentException, OperationFailedException, ArrayIndexOutOfBoundsException {
        if (isDetailClassSupported(detail.getClass())) {
            List<GenericDetail> alreadySetDetails = new Vector();
            Iterator<GenericDetail> iter = getDetails(detail.getClass());
            while (iter.hasNext()) {
                alreadySetDetails.add(iter.next());
            }
            if (alreadySetDetails.size() >= getMaxDetailInstances(detail.getClass())) {
                throw new ArrayIndexOutOfBoundsException("Max count for this detail is already reached");
            }
            if (ImageDetail.class.isAssignableFrom(detail.getClass()) && isImageDetailSupported()) {
                ImageDetail imageDetail = (ImageDetail) detail;
                putImageDetail(imageDetail);
                this.accountImage = imageDetail;
                this.isAccountImageLoaded = true;
            } else if (DisplayNameDetail.class.isAssignableFrom(detail.getClass())) {
                this.displayNameDetail = (DisplayNameDetail) detail;
            }
            if (fireChangeEvents) {
                fireServerStoredDetailsChangeEvent(this.provider, 1, null, detail);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("Implementation does not support such details " + detail.getClass());
    }

    public boolean removeDetail(GenericDetail detail) throws OperationFailedException {
        return removeDetail(detail, true);
    }

    private boolean removeDetail(GenericDetail detail, boolean fireChangeEvents) throws OperationFailedException {
        boolean isFound = false;
        Iterator<?> iter = getAllAvailableDetails();
        while (iter.hasNext()) {
            if (((GenericDetail) iter.next()).equals(detail)) {
                isFound = true;
            }
        }
        if (!isFound) {
            return false;
        }
        if (ImageDetail.class.isAssignableFrom(detail.getClass()) && isImageDetailSupported()) {
            deleteImageDetail();
            this.accountImage = null;
        }
        if (fireChangeEvents) {
            fireServerStoredDetailsChangeEvent(this.provider, 2, detail, null);
        }
        return true;
    }

    public boolean replaceDetail(GenericDetail currentDetailValue, GenericDetail newDetailValue) throws ClassCastException, OperationFailedException {
        if (newDetailValue.getClass().equals(currentDetailValue.getClass())) {
            if (!currentDetailValue.equals(newDetailValue)) {
                removeDetail(currentDetailValue, false);
                addDetail(newDetailValue, false);
                fireServerStoredDetailsChangeEvent(this.provider, 3, currentDetailValue, newDetailValue);
            }
            return true;
        }
        throw new ClassCastException("New value to be replaced is not as the current one");
    }

    public void save() throws OperationFailedException {
    }

    private boolean isImageDetailSupported() {
        OperationSetPresenceSipImpl opSet = (OperationSetPresenceSipImpl) this.provider.getOperationSet(OperationSetPersistentPresence.class);
        if (opSet == null) {
            return false;
        }
        return opSet.getSsContactList().isAccountImageSupported();
    }

    private ImageDetail getAccountImage() {
        if (this.isAccountImageLoaded) {
            return this.accountImage;
        }
        this.isAccountImageLoaded = true;
        try {
            this.accountImage = getImageDetail();
        } catch (OperationFailedException e) {
            if (logger.isInfoEnabled()) {
                logger.info("Avatar image cannot be loaded", e);
            }
        }
        return this.accountImage;
    }

    private ImageDetail getImageDetail() throws OperationFailedException {
        OperationSetPresenceSipImpl opSet = (OperationSetPresenceSipImpl) this.provider.getOperationSet(OperationSetPersistentPresence.class);
        if (opSet == null) {
            return null;
        }
        return opSet.getSsContactList().getAccountImage();
    }

    private void putImageDetail(ImageDetail imageDetail) throws OperationFailedException {
        OperationSetPresenceSipImpl opSet = (OperationSetPresenceSipImpl) this.provider.getOperationSet(OperationSetPersistentPresence.class);
        if (opSet != null) {
            opSet.getSsContactList().setAccountImage(imageDetail.getBytes());
        }
    }

    private void deleteImageDetail() throws OperationFailedException {
        OperationSetPresenceSipImpl opSet = (OperationSetPresenceSipImpl) this.provider.getOperationSet(OperationSetPersistentPresence.class);
        if (opSet != null) {
            opSet.getSsContactList().deleteAccountImage();
        }
    }

    public void registrationStateChanged(RegistrationStateChangeEvent evt) {
        if (evt.getNewState().equals(RegistrationState.UNREGISTERED) || evt.getNewState().equals(RegistrationState.AUTHENTICATION_FAILED) || evt.getNewState().equals(RegistrationState.CONNECTION_FAILED)) {
            this.isAccountImageLoaded = false;
            this.accountImage = null;
        }
    }

    /* access modifiers changed from: 0000 */
    public void setOurDisplayName(String newDisplayName) {
        DisplayNameDetail oldDisplayName = this.displayNameDetail;
        DisplayNameDetail newDisplayNameDetail = new DisplayNameDetail(newDisplayName);
        List<GenericDetail> alreadySetDetails = new Vector();
        Iterator<GenericDetail> iter = getDetails(newDisplayNameDetail.getClass());
        while (iter.hasNext()) {
            alreadySetDetails.add(iter.next());
        }
        try {
            if (alreadySetDetails.size() > 0) {
                replaceDetail(oldDisplayName, newDisplayNameDetail);
            } else {
                addDetail(newDisplayNameDetail);
            }
        } catch (OperationFailedException e) {
            logger.error("Filed to set display name", e);
        }
    }

    /* access modifiers changed from: 0000 */
    public void shutdown() {
        this.provider.removeRegistrationStateChangeListener(this);
    }
}
