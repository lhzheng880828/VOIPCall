package org.jivesoftware.smackx.pubsub;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UnknownFormatConversionException;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.packet.DataForm;

public class SubscribeForm extends Form {
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public SubscribeForm(DataForm configDataForm) {
        super(configDataForm);
    }

    public SubscribeForm(Form subscribeOptionsForm) {
        super(subscribeOptionsForm.getDataFormToSend());
    }

    public SubscribeForm(FormType formType) {
        super(formType.toString());
    }

    public boolean isDeliverOn() {
        return parseBoolean(getFieldValue(SubscribeOptionFields.deliver));
    }

    public void setDeliverOn(boolean deliverNotifications) {
        addField(SubscribeOptionFields.deliver, FormField.TYPE_BOOLEAN);
        setAnswer(SubscribeOptionFields.deliver.getFieldName(), deliverNotifications);
    }

    public boolean isDigestOn() {
        return parseBoolean(getFieldValue(SubscribeOptionFields.digest));
    }

    public void setDigestOn(boolean digestOn) {
        addField(SubscribeOptionFields.deliver, FormField.TYPE_BOOLEAN);
        setAnswer(SubscribeOptionFields.deliver.getFieldName(), digestOn);
    }

    public int getDigestFrequency() {
        return Integer.parseInt(getFieldValue(SubscribeOptionFields.digest_frequency));
    }

    public void setDigestFrequency(int frequency) {
        addField(SubscribeOptionFields.digest_frequency, FormField.TYPE_TEXT_SINGLE);
        setAnswer(SubscribeOptionFields.digest_frequency.getFieldName(), frequency);
    }

    public Date getExpiry() {
        String dateTime = getFieldValue(SubscribeOptionFields.expire);
        try {
            return format.parse(dateTime);
        } catch (ParseException e) {
            UnknownFormatConversionException exc = new UnknownFormatConversionException(dateTime);
            exc.initCause(e);
            throw exc;
        }
    }

    public void setExpiry(Date expire) {
        addField(SubscribeOptionFields.expire, FormField.TYPE_TEXT_SINGLE);
        setAnswer(SubscribeOptionFields.expire.getFieldName(), format.format(expire));
    }

    public boolean isIncludeBody() {
        return parseBoolean(getFieldValue(SubscribeOptionFields.include_body));
    }

    public void setIncludeBody(boolean include) {
        addField(SubscribeOptionFields.include_body, FormField.TYPE_BOOLEAN);
        setAnswer(SubscribeOptionFields.include_body.getFieldName(), include);
    }

    public Iterator<PresenceState> getShowValues() {
        ArrayList<PresenceState> result = new ArrayList(5);
        Iterator<String> it = getFieldValues(SubscribeOptionFields.show_values);
        while (it.hasNext()) {
            result.add(PresenceState.valueOf((String) it.next()));
        }
        return result.iterator();
    }

    public void setShowValues(Collection<PresenceState> stateValues) {
        ArrayList<String> values = new ArrayList(stateValues.size());
        for (PresenceState state : stateValues) {
            values.add(state.toString());
        }
        addField(SubscribeOptionFields.show_values, FormField.TYPE_LIST_MULTI);
        setAnswer(SubscribeOptionFields.show_values.getFieldName(), (List) values);
    }

    private static boolean parseBoolean(String fieldValue) {
        return "1".equals(fieldValue) || "true".equals(fieldValue);
    }

    private String getFieldValue(SubscribeOptionFields field) {
        return (String) getField(field.getFieldName()).getValues().next();
    }

    private Iterator<String> getFieldValues(SubscribeOptionFields field) {
        return getField(field.getFieldName()).getValues();
    }

    private void addField(SubscribeOptionFields nodeField, String type) {
        String fieldName = nodeField.getFieldName();
        if (getField(fieldName) == null) {
            FormField field = new FormField(fieldName);
            field.setType(type);
            addField(field);
        }
    }
}
