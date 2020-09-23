package net.java.sip.communicator.impl.protocol.jabber;

import java.util.List;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.OperationSetCusaxUtils;
import net.java.sip.communicator.service.protocol.PhoneNumberI18nService;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import net.java.sip.communicator.util.call.ContactPhoneUtil;

public class OperationSetCusaxUtilsJabberImpl implements OperationSetCusaxUtils {
    private final ProtocolProviderServiceJabberImpl jabberProvider;

    public OperationSetCusaxUtilsJabberImpl(ProtocolProviderServiceJabberImpl jabberProvider) {
        this.jabberProvider = jabberProvider;
    }

    public boolean doesDetailBelong(Contact contact, String detailAddress) {
        List<String> contactPhones = ContactPhoneUtil.getContactAdditionalPhones(contact, null, false, false);
        if (contactPhones == null || contactPhones.size() <= 0) {
            return false;
        }
        for (String phone : contactPhones) {
            String normalizedPhone = PhoneNumberI18nService.normalize(phone);
            if (phone.equals(detailAddress) || normalizedPhone.equals(detailAddress) || detailAddress.contains(phone)) {
                return true;
            }
            if (detailAddress.contains(normalizedPhone)) {
                return true;
            }
        }
        return false;
    }

    public ProtocolProviderService getLinkedCusaxProvider() {
        return null;
    }
}
