package net.java.sip.communicator.impl.protocol.jabber;

import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;
import net.java.sip.communicator.service.protocol.ChatRoomConfigurationForm;
import net.java.sip.communicator.service.protocol.ChatRoomConfigurationFormField;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.util.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.muc.MultiUserChat;

public class ChatRoomConfigurationFormJabberImpl implements ChatRoomConfigurationForm {
    private Logger logger = Logger.getLogger(ChatRoomConfigurationFormJabberImpl.class);
    private Form smackConfigForm;
    private MultiUserChat smackMultiUserChat;
    private Form smackSubmitForm;

    public ChatRoomConfigurationFormJabberImpl(MultiUserChat multiUserChat, Form smackConfigForm) {
        this.smackMultiUserChat = multiUserChat;
        this.smackConfigForm = smackConfigForm;
        this.smackSubmitForm = smackConfigForm.createAnswerForm();
    }

    public Iterator<ChatRoomConfigurationFormField> getConfigurationSet() {
        Vector<ChatRoomConfigurationFormField> configFormFields = new Vector();
        Iterator<FormField> smackFormFields = this.smackConfigForm.getFields();
        while (smackFormFields.hasNext()) {
            FormField smackFormField = (FormField) smackFormFields.next();
            if (!(smackFormField == null || smackFormField.getType().equals("hidden"))) {
                configFormFields.add(new ChatRoomConfigurationFormFieldJabberImpl(smackFormField, this.smackSubmitForm));
            }
        }
        return Collections.unmodifiableList(configFormFields).iterator();
    }

    public void submit() throws OperationFailedException {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Sends chat room configuration form to the server.");
        }
        try {
            this.smackMultiUserChat.sendConfigurationForm(this.smackSubmitForm);
        } catch (XMPPException e) {
            this.logger.error("Failed to submit the configuration form.", e);
            throw new OperationFailedException("Failed to submit the configuration form.", 1);
        }
    }
}
