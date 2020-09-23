package net.java.sip.communicator.impl.protocol.jabber;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.AboutMeDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.AddressDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.BirthDateDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.CityDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.CountryDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.DisplayNameDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.EmailAddressDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.FaxDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.FirstNameDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.GenericDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.ImageDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.JobTitleDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.LastNameDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.MiddleNameDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.MobilePhoneDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.NameDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.NicknameDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.PagerDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.PhoneNumberDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.PostalCodeDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.ProvinceDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.URLDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.VideoDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.WorkAddressDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.WorkCityDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.WorkEmailAddressDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.WorkMobilePhoneDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.WorkOrganizationNameDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.WorkPhoneDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.WorkPostalCodeDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.WorkProvinceDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.WorkVideoDetail;
import net.java.sip.communicator.util.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.packet.XMPPError.Condition;
import org.jivesoftware.smackx.packet.VCard;

public class InfoRetreiver {
    private static final String TAG_FN_CLOSE = "</FN>";
    private static final String TAG_FN_OPEN = "<FN>";
    private static final Logger logger = Logger.getLogger(InfoRetreiver.class);
    private ProtocolProviderServiceJabberImpl jabberProvider = null;
    private final Map<String, List<GenericDetail>> retreivedDetails = new Hashtable();
    private final long vcardTimeoutReply;

    public static class WorkDepartmentNameDetail extends NameDetail {
        public WorkDepartmentNameDetail(String workDepartmentName) {
            super("Work Department Name", workDepartmentName);
        }
    }

    public static class WorkFaxDetail extends FaxDetail {
        public WorkFaxDetail(String number) {
            super(number);
            this.detailDisplayName = "WorkFax";
        }
    }

    public static class WorkPagerDetail extends PhoneNumberDetail {
        public WorkPagerDetail(String number) {
            super(number);
            this.detailDisplayName = "WorkPager";
        }
    }

    protected InfoRetreiver(ProtocolProviderServiceJabberImpl jabberProvider, String ownerUin) {
        this.jabberProvider = jabberProvider;
        this.vcardTimeoutReply = JabberActivator.getConfigurationService().getLong(ProtocolProviderServiceJabberImpl.VCARD_REPLY_TIMEOUT_PROPERTY, -1);
    }

    /* access modifiers changed from: 0000 */
    public <T extends GenericDetail> Iterator<T> getDetailsAndDescendants(String uin, Class<T> detailClass) {
        List<GenericDetail> details = getContactDetails(uin);
        List<T> result = new LinkedList();
        Iterator i$ = details.iterator();
        while (i$.hasNext()) {
            T item = (GenericDetail) i$.next();
            if (detailClass.isInstance(item)) {
                result.add(item);
            }
        }
        return result.iterator();
    }

    /* access modifiers changed from: 0000 */
    public Iterator<GenericDetail> getDetails(String uin, Class<? extends GenericDetail> detailClass) {
        List<GenericDetail> details = getContactDetails(uin);
        List<GenericDetail> result = new LinkedList();
        for (GenericDetail item : details) {
            if (detailClass.equals(item.getClass())) {
                result.add(item);
            }
        }
        return result.iterator();
    }

    /* access modifiers changed from: 0000 */
    public List<GenericDetail> getContactDetails(String contactAddress) {
        List<GenericDetail> result = getCachedContactDetails(contactAddress);
        if (result == null) {
            return retrieveDetails(contactAddress);
        }
        return result;
    }

    /* access modifiers changed from: protected */
    public List<GenericDetail> retrieveDetails(String contactAddress) {
        List<GenericDetail> result = new LinkedList();
        try {
            XMPPConnection connection = this.jabberProvider.getConnection();
            if (connection == null || !connection.isAuthenticated()) {
                return null;
            }
            VCard card = new VCard();
            if (this.vcardTimeoutReply == -1 || this.vcardTimeoutReply == ((long) SmackConfiguration.getPacketReplyTimeout())) {
                card.load(connection, contactAddress);
            } else {
                load(card, connection, contactAddress, this.vcardTimeoutReply);
            }
            String tmp = checkForFullName(card);
            if (tmp != null) {
                result.add(new DisplayNameDetail(tmp));
            }
            tmp = card.getFirstName();
            if (tmp != null) {
                result.add(new FirstNameDetail(tmp));
            }
            tmp = card.getMiddleName();
            if (tmp != null) {
                result.add(new MiddleNameDetail(tmp));
            }
            tmp = card.getLastName();
            if (tmp != null) {
                result.add(new LastNameDetail(tmp));
            }
            tmp = card.getNickName();
            if (tmp != null) {
                result.add(new NicknameDetail(tmp));
            }
            tmp = card.getField("BDAY");
            if (tmp != null) {
                try {
                    Calendar birthDateCalendar = Calendar.getInstance();
                    birthDateCalendar.setTime(new SimpleDateFormat(JabberActivator.getResources().getI18NString("plugin.accountinfo.BDAY_FORMAT")).parse(tmp));
                    result.add(new BirthDateDetail(birthDateCalendar));
                } catch (ParseException e) {
                }
            }
            tmp = card.getAddressFieldHome("STREET");
            if (tmp != null) {
                result.add(new AddressDetail(tmp));
            }
            tmp = card.getAddressFieldHome("LOCALITY");
            if (tmp != null) {
                result.add(new CityDetail(tmp));
            }
            tmp = card.getAddressFieldHome("REGION");
            if (tmp != null) {
                result.add(new ProvinceDetail(tmp));
            }
            tmp = card.getAddressFieldHome("PCODE");
            if (tmp != null) {
                result.add(new PostalCodeDetail(tmp));
            }
            tmp = card.getAddressFieldHome("CTRY");
            if (tmp != null) {
                result.add(new CountryDetail(tmp));
            }
            tmp = card.getPhoneHome("VOICE");
            if (tmp != null) {
                result.add(new PhoneNumberDetail(tmp));
            }
            tmp = card.getPhoneHome("VIDEO");
            if (tmp != null) {
                result.add(new VideoDetail(tmp));
            }
            tmp = card.getPhoneHome("FAX");
            if (tmp != null) {
                result.add(new FaxDetail(tmp));
            }
            tmp = card.getPhoneHome("PAGER");
            if (tmp != null) {
                result.add(new PagerDetail(tmp));
            }
            tmp = card.getPhoneHome("CELL");
            if (tmp != null) {
                result.add(new MobilePhoneDetail(tmp));
            }
            tmp = card.getPhoneHome("TEXT");
            if (tmp != null) {
                result.add(new MobilePhoneDetail(tmp));
            }
            tmp = card.getEmailHome();
            if (tmp != null) {
                result.add(new EmailAddressDetail(tmp));
            }
            tmp = card.getAddressFieldWork("STREET");
            if (tmp != null) {
                result.add(new WorkAddressDetail(tmp));
            }
            tmp = card.getAddressFieldWork("LOCALITY");
            if (tmp != null) {
                result.add(new WorkCityDetail(tmp));
            }
            tmp = card.getAddressFieldWork("REGION");
            if (tmp != null) {
                result.add(new WorkProvinceDetail(tmp));
            }
            tmp = card.getAddressFieldWork("PCODE");
            if (tmp != null) {
                result.add(new WorkPostalCodeDetail(tmp));
            }
            tmp = card.getPhoneWork("VOICE");
            if (tmp != null) {
                result.add(new WorkPhoneDetail(tmp));
            }
            tmp = card.getPhoneWork("VIDEO");
            if (tmp != null) {
                result.add(new WorkVideoDetail(tmp));
            }
            tmp = card.getPhoneWork("FAX");
            if (tmp != null) {
                result.add(new WorkFaxDetail(tmp));
            }
            tmp = card.getPhoneWork("PAGER");
            if (tmp != null) {
                result.add(new WorkPagerDetail(tmp));
            }
            tmp = card.getPhoneWork("CELL");
            if (tmp != null) {
                result.add(new WorkMobilePhoneDetail(tmp));
            }
            tmp = card.getPhoneWork("TEXT");
            if (tmp != null) {
                result.add(new WorkMobilePhoneDetail(tmp));
            }
            tmp = card.getEmailWork();
            if (tmp != null) {
                result.add(new WorkEmailAddressDetail(tmp));
            }
            tmp = card.getOrganization();
            if (tmp != null) {
                result.add(new WorkOrganizationNameDetail(tmp));
            }
            tmp = card.getOrganizationUnit();
            if (tmp != null) {
                result.add(new WorkDepartmentNameDetail(tmp));
            }
            tmp = card.getField("TITLE");
            if (tmp != null) {
                result.add(new JobTitleDetail(tmp));
            }
            tmp = card.getField("ABOUTME");
            if (tmp != null) {
                result.add(new AboutMeDetail(tmp));
            }
            byte[] imageBytes = card.getAvatar();
            if (imageBytes != null && imageBytes.length > 0) {
                result.add(new ImageDetail("Image", imageBytes));
            }
            try {
                tmp = card.getField("URL");
                if (tmp != null) {
                    result.add(new URLDetail("URL", new URL(tmp)));
                }
            } catch (MalformedURLException e2) {
            }
            this.retreivedDetails.put(contactAddress, result);
            return result;
        } catch (Throwable exc) {
            String msg = "Cannot load details for contact " + contactAddress + " : " + exc.getMessage();
            if (logger.isTraceEnabled()) {
                logger.error(msg, exc);
            } else {
                logger.error(msg);
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public List<GenericDetail> getCachedContactDetails(String contactAddress) {
        return (List) this.retreivedDetails.get(contactAddress);
    }

    /* access modifiers changed from: 0000 */
    public void addCachedContactDetails(String contactAddress, List<GenericDetail> details) {
        this.retreivedDetails.put(contactAddress, details);
    }

    private String checkForFullName(VCard card) {
        String vcardXml = card.toXML();
        int indexOpen = vcardXml.indexOf(TAG_FN_OPEN);
        if (indexOpen == -1) {
            return null;
        }
        int indexClose = vcardXml.indexOf(TAG_FN_CLOSE, indexOpen);
        if (indexClose != -1) {
            return vcardXml.substring(TAG_FN_OPEN.length() + indexOpen, indexClose);
        }
        return null;
    }

    public void load(VCard vcard, Connection connection, String user, long timeout) throws XMPPException {
        vcard.setTo(user);
        vcard.setType(Type.GET);
        PacketCollector collector = connection.createPacketCollector(new PacketIDFilter(vcard.getPacketID()));
        connection.sendPacket(vcard);
        VCard result = null;
        try {
            result = (VCard) collector.nextResult(timeout);
            if (result == null) {
                String errorMessage = "Timeout getting VCard information";
                throw new XMPPException(errorMessage, new XMPPError(Condition.request_timeout, errorMessage));
            }
            if (result.getError() != null) {
                throw new XMPPException(result.getError());
            }
            if (result == null) {
                result = new VCard();
            }
            for (Field field : VCard.class.getDeclaredFields()) {
                if (field.getDeclaringClass() == VCard.class && !Modifier.isFinal(field.getModifiers())) {
                    try {
                        field.setAccessible(true);
                        field.set(vcard, field.get(result));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Cannot set field:" + field, e);
                    }
                }
            }
        } catch (ClassCastException e2) {
            logger.error("No vcard for " + user);
        }
    }
}
