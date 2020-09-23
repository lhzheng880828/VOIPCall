package net.java.sip.communicator.impl.protocol.jabber;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.java.sip.communicator.service.protocol.AbstractOperationSetServerStoredAccountInfo;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.AboutMeDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.AddressDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.BirthDateDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.CityDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.CountryDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.EmailAddressDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.FirstNameDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.GenericDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.ImageDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.JobTitleDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.LastNameDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.MiddleNameDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.NicknameDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.PhoneNumberDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.PostalCodeDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.ProvinceDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.URLDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.WorkEmailAddressDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.WorkOrganizationNameDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.WorkPhoneDetail;
import net.java.sip.communicator.util.Logger;
import org.jivesoftware.smack.XMPPException;

public class OperationSetServerStoredAccountInfoJabberImpl extends AbstractOperationSetServerStoredAccountInfo {
    private static final Logger logger = Logger.getLogger(OperationSetServerStoredAccountInfoJabberImpl.class);
    public static final List<Class<? extends GenericDetail>> supportedTypes = new ArrayList();
    private InfoRetreiver infoRetreiver = null;
    private ProtocolProviderServiceJabberImpl jabberProvider = null;
    private String uin = null;

    static {
        supportedTypes.add(ImageDetail.class);
        supportedTypes.add(FirstNameDetail.class);
        supportedTypes.add(MiddleNameDetail.class);
        supportedTypes.add(LastNameDetail.class);
        supportedTypes.add(NicknameDetail.class);
        supportedTypes.add(AddressDetail.class);
        supportedTypes.add(CityDetail.class);
        supportedTypes.add(ProvinceDetail.class);
        supportedTypes.add(PostalCodeDetail.class);
        supportedTypes.add(CountryDetail.class);
        supportedTypes.add(EmailAddressDetail.class);
        supportedTypes.add(WorkEmailAddressDetail.class);
        supportedTypes.add(PhoneNumberDetail.class);
        supportedTypes.add(WorkPhoneDetail.class);
        supportedTypes.add(WorkOrganizationNameDetail.class);
        supportedTypes.add(URLDetail.class);
        supportedTypes.add(BirthDateDetail.class);
        supportedTypes.add(JobTitleDetail.class);
        supportedTypes.add(AboutMeDetail.class);
    }

    protected OperationSetServerStoredAccountInfoJabberImpl(ProtocolProviderServiceJabberImpl jabberProvider, InfoRetreiver infoRetreiver, String uin) {
        this.infoRetreiver = infoRetreiver;
        this.jabberProvider = jabberProvider;
        this.uin = uin;
    }

    public <T extends GenericDetail> Iterator<T> getDetailsAndDescendants(Class<T> detailClass) {
        assertConnected();
        return this.infoRetreiver.getDetailsAndDescendants(this.uin, detailClass);
    }

    public Iterator<GenericDetail> getDetails(Class<? extends GenericDetail> detailClass) {
        assertConnected();
        return this.infoRetreiver.getDetails(this.uin, detailClass);
    }

    public Iterator<GenericDetail> getAllAvailableDetails() {
        assertConnected();
        return this.infoRetreiver.getContactDetails(this.uin).iterator();
    }

    public Iterator<Class<? extends GenericDetail>> getSupportedDetailTypes() {
        return supportedTypes.iterator();
    }

    public boolean isDetailClassSupported(Class<? extends GenericDetail> detailClass) {
        return supportedTypes.contains(detailClass);
    }

    public int getMaxDetailInstances(Class<? extends GenericDetail> cls) {
        return 1;
    }

    public void addDetail(GenericDetail detail) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (isDetailClassSupported(detail.getClass())) {
            Iterator<GenericDetail> iter = getDetails(detail.getClass());
            int currentDetailsSize = 0;
            while (iter.hasNext()) {
                currentDetailsSize++;
                iter.next();
            }
            if (currentDetailsSize > getMaxDetailInstances(detail.getClass())) {
                throw new ArrayIndexOutOfBoundsException("Max count for this detail is already reached");
            }
            this.infoRetreiver.getCachedContactDetails(this.uin).add(detail);
            return;
        }
        throw new IllegalArgumentException("implementation does not support such details " + detail.getClass());
    }

    public boolean removeDetail(GenericDetail detail) {
        return this.infoRetreiver.getCachedContactDetails(this.uin).remove(detail);
    }

    public boolean replaceDetail(GenericDetail currentDetailValue, GenericDetail newDetailValue) throws ClassCastException {
        if (!newDetailValue.getClass().equals(currentDetailValue.getClass())) {
            throw new ClassCastException("New value to be replaced is not as the current one");
        } else if (currentDetailValue.equals(newDetailValue)) {
            return true;
        } else {
            boolean isFound = false;
            Iterator<GenericDetail> iter = this.infoRetreiver.getDetails(this.uin, currentDetailValue.getClass());
            while (iter.hasNext()) {
                if (((GenericDetail) iter.next()).equals(currentDetailValue)) {
                    isFound = true;
                    break;
                }
            }
            if (!isFound) {
                return false;
            }
            removeDetail(currentDetailValue);
            addDetail(newDetailValue);
            return true;
        }
    }

    public void save() throws OperationFailedException {
        assertConnected();
        List<GenericDetail> details = this.infoRetreiver.getContactDetails(this.uin);
        VCardXEP0153 vCard = new VCardXEP0153();
        for (GenericDetail detail : details) {
            if (detail instanceof ImageDetail) {
                byte[] avatar = ((ImageDetail) detail).getBytes();
                if (avatar == null) {
                    vCard.setAvatar(new byte[0]);
                } else {
                    vCard.setAvatar(avatar);
                }
                fireServerStoredDetailsChangeEvent(this.jabberProvider, 1, null, detail);
            } else if (detail.getClass().equals(FirstNameDetail.class)) {
                vCard.setFirstName((String) detail.getDetailValue());
            } else if (detail.getClass().equals(MiddleNameDetail.class)) {
                vCard.setMiddleName((String) detail.getDetailValue());
            } else if (detail.getClass().equals(LastNameDetail.class)) {
                vCard.setLastName((String) detail.getDetailValue());
            } else if (detail.getClass().equals(NicknameDetail.class)) {
                vCard.setNickName((String) detail.getDetailValue());
            } else if (detail.getClass().equals(URLDetail.class)) {
                if (detail.getDetailValue() != null) {
                    vCard.setField("URL", ((URL) detail.getDetailValue()).toString());
                }
            } else if (detail.getClass().equals(BirthDateDetail.class)) {
                if (detail.getDetailValue() != null) {
                    vCard.setField("BDAY", new SimpleDateFormat(JabberActivator.getResources().getI18NString("plugin.accountinfo.BDAY_FORMAT")).format(((BirthDateDetail) detail).getCalendar().getTime()));
                }
            } else if (detail.getClass().equals(AddressDetail.class)) {
                vCard.setAddressFieldHome("STREET", (String) detail.getDetailValue());
            } else if (detail.getClass().equals(CityDetail.class)) {
                vCard.setAddressFieldHome("LOCALITY", (String) detail.getDetailValue());
            } else if (detail.getClass().equals(ProvinceDetail.class)) {
                vCard.setAddressFieldHome("REGION", (String) detail.getDetailValue());
            } else if (detail.getClass().equals(PostalCodeDetail.class)) {
                vCard.setAddressFieldHome("PCODE", (String) detail.getDetailValue());
            } else if (detail.getClass().equals(CountryDetail.class)) {
                vCard.setAddressFieldHome("CTRY", (String) detail.getDetailValue());
            } else if (detail.getClass().equals(PhoneNumberDetail.class)) {
                vCard.setPhoneHome("VOICE", (String) detail.getDetailValue());
            } else if (detail.getClass().equals(WorkPhoneDetail.class)) {
                vCard.setPhoneWork("VOICE", (String) detail.getDetailValue());
            } else if (detail.getClass().equals(EmailAddressDetail.class)) {
                vCard.setEmailHome((String) detail.getDetailValue());
            } else if (detail.getClass().equals(WorkEmailAddressDetail.class)) {
                vCard.setEmailWork((String) detail.getDetailValue());
            } else if (detail.getClass().equals(WorkOrganizationNameDetail.class)) {
                vCard.setOrganization((String) detail.getDetailValue());
            } else if (detail.getClass().equals(JobTitleDetail.class)) {
                vCard.setField("TITLE", (String) detail.getDetailValue());
            } else if (detail.getClass().equals(AboutMeDetail.class)) {
                vCard.setField("ABOUTME", (String) detail.getDetailValue());
            }
        }
        try {
            vCard.save(this.jabberProvider.getConnection());
        } catch (XMPPException xmppe) {
            logger.error("Error loading/saving vcard: ", xmppe);
            throw new OperationFailedException("Error loading/saving vcard: ", 1, xmppe);
        }
    }

    public boolean isDetailClassEditable(Class<? extends GenericDetail> detailClass) {
        if (isDetailClassSupported(detailClass)) {
            return true;
        }
        return false;
    }

    private void assertConnected() throws IllegalStateException {
        if (this.jabberProvider == null) {
            throw new IllegalStateException("The jabber provider must be non-null and signed on before being able to communicate.");
        } else if (!this.jabberProvider.isRegistered()) {
            throw new IllegalStateException("The jabber provider must be signed on before being able to communicate.");
        }
    }
}
