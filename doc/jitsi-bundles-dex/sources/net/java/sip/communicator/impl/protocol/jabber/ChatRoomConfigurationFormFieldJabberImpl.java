package net.java.sip.communicator.impl.protocol.jabber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.sdp.SdpConstants;
import net.java.sip.communicator.service.protocol.ChatRoomConfigurationFormField;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.FormField.Option;

public class ChatRoomConfigurationFormFieldJabberImpl implements ChatRoomConfigurationFormField {
    private final FormField smackFormField;
    private final FormField smackSubmitFormField;

    public ChatRoomConfigurationFormFieldJabberImpl(FormField formField, Form submitForm) {
        this.smackFormField = formField;
        if (formField.getType().equals(FormField.TYPE_FIXED)) {
            this.smackSubmitFormField = null;
        } else {
            this.smackSubmitFormField = submitForm.getField(formField.getVariable());
        }
    }

    public String getName() {
        return this.smackFormField.getVariable();
    }

    public String getDescription() {
        return this.smackFormField.getDescription();
    }

    public String getLabel() {
        return this.smackFormField.getLabel();
    }

    public Iterator<String> getOptions() {
        List<String> options = new ArrayList();
        Iterator<Option> smackOptions = this.smackFormField.getOptions();
        while (smackOptions.hasNext()) {
            options.add(((Option) smackOptions.next()).getValue());
        }
        return Collections.unmodifiableList(options).iterator();
    }

    public boolean isRequired() {
        return this.smackFormField.isRequired();
    }

    public String getType() {
        String smackType = this.smackFormField.getType();
        if (smackType.equals(FormField.TYPE_BOOLEAN)) {
            return "Boolean";
        }
        if (smackType.equals(FormField.TYPE_FIXED)) {
            return "FixedText";
        }
        if (smackType.equals(FormField.TYPE_TEXT_PRIVATE)) {
            return "PrivateText";
        }
        if (smackType.equals(FormField.TYPE_TEXT_SINGLE)) {
            return "SingleLineText";
        }
        if (smackType.equals(FormField.TYPE_TEXT_MULTI)) {
            return "MultipleLinesText";
        }
        if (smackType.equals(FormField.TYPE_LIST_SINGLE)) {
            return "ListSingleChoice";
        }
        if (smackType.equals(FormField.TYPE_LIST_MULTI)) {
            return "ListMultiChoice";
        }
        if (smackType.equals(FormField.TYPE_JID_SINGLE)) {
            return "SingleIDChoice";
        }
        if (smackType.equals(FormField.TYPE_JID_MULTI)) {
            return "MultiIDChoice";
        }
        return "Undefined";
    }

    public Iterator<?> getValues() {
        Iterator<String> smackValues = this.smackFormField.getValues();
        if (!this.smackFormField.getType().equals(FormField.TYPE_BOOLEAN)) {
            return smackValues;
        }
        List<Boolean> values = new ArrayList();
        while (smackValues.hasNext()) {
            String smackValue = (String) smackValues.next();
            Object obj = (smackValue.equals("1") || smackValue.equals("true")) ? Boolean.TRUE : Boolean.FALSE;
            values.add(obj);
        }
        return values.iterator();
    }

    public void addValue(Object value) {
        if (value instanceof Boolean) {
            value = ((Boolean) value).booleanValue() ? "1" : SdpConstants.RESERVED;
        }
        this.smackSubmitFormField.addValue(value.toString());
    }

    public void setValues(Object[] newValues) {
        List<String> list = new ArrayList();
        for (Object value : newValues) {
            String stringValue = value instanceof Boolean ? ((Boolean) value).booleanValue() ? "1" : SdpConstants.RESERVED : value == null ? null : value.toString();
            list.add(stringValue);
        }
        this.smackSubmitFormField.addValues(list);
    }
}
