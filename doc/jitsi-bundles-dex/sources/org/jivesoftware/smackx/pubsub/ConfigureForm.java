package org.jivesoftware.smackx.pubsub;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.packet.DataForm;

public class ConfigureForm extends Form {
    public ConfigureForm(DataForm configDataForm) {
        super(configDataForm);
    }

    public ConfigureForm(Form nodeConfigForm) {
        super(nodeConfigForm.getDataFormToSend());
    }

    public ConfigureForm(FormType formType) {
        super(formType.toString());
    }

    public AccessModel getAccessModel() {
        String value = getFieldValue(ConfigureNodeFields.access_model);
        if (value == null) {
            return null;
        }
        return AccessModel.valueOf(value);
    }

    public void setAccessModel(AccessModel accessModel) {
        addField(ConfigureNodeFields.access_model, FormField.TYPE_LIST_SINGLE);
        setAnswer(ConfigureNodeFields.access_model.getFieldName(), getListSingle(accessModel.toString()));
    }

    public String getBodyXSLT() {
        return getFieldValue(ConfigureNodeFields.body_xslt);
    }

    public void setBodyXSLT(String bodyXslt) {
        addField(ConfigureNodeFields.body_xslt, FormField.TYPE_TEXT_SINGLE);
        setAnswer(ConfigureNodeFields.body_xslt.getFieldName(), bodyXslt);
    }

    public Iterator<String> getChildren() {
        return getFieldValues(ConfigureNodeFields.children);
    }

    public void setChildren(List<String> children) {
        addField(ConfigureNodeFields.children, FormField.TYPE_TEXT_MULTI);
        setAnswer(ConfigureNodeFields.children.getFieldName(), (List) children);
    }

    public ChildrenAssociationPolicy getChildrenAssociationPolicy() {
        String value = getFieldValue(ConfigureNodeFields.children_association_policy);
        if (value == null) {
            return null;
        }
        return ChildrenAssociationPolicy.valueOf(value);
    }

    public void setChildrenAssociationPolicy(ChildrenAssociationPolicy policy) {
        addField(ConfigureNodeFields.children_association_policy, FormField.TYPE_LIST_SINGLE);
        List<String> values = new ArrayList(1);
        values.add(policy.toString());
        setAnswer(ConfigureNodeFields.children_association_policy.getFieldName(), (List) values);
    }

    public Iterator<String> getChildrenAssociationWhitelist() {
        return getFieldValues(ConfigureNodeFields.children_association_whitelist);
    }

    public void setChildrenAssociationWhitelist(List<String> whitelist) {
        addField(ConfigureNodeFields.children_association_whitelist, FormField.TYPE_JID_MULTI);
        setAnswer(ConfigureNodeFields.children_association_whitelist.getFieldName(), (List) whitelist);
    }

    public int getChildrenMax() {
        return Integer.parseInt(getFieldValue(ConfigureNodeFields.children_max));
    }

    public void setChildrenMax(int max) {
        addField(ConfigureNodeFields.children_max, FormField.TYPE_TEXT_SINGLE);
        setAnswer(ConfigureNodeFields.children_max.getFieldName(), max);
    }

    public String getCollection() {
        return getFieldValue(ConfigureNodeFields.collection);
    }

    public void setCollection(String collection) {
        addField(ConfigureNodeFields.collection, FormField.TYPE_TEXT_SINGLE);
        setAnswer(ConfigureNodeFields.collection.getFieldName(), collection);
    }

    public String getDataformXSLT() {
        return getFieldValue(ConfigureNodeFields.dataform_xslt);
    }

    public void setDataformXSLT(String url) {
        addField(ConfigureNodeFields.dataform_xslt, FormField.TYPE_TEXT_SINGLE);
        setAnswer(ConfigureNodeFields.dataform_xslt.getFieldName(), url);
    }

    public boolean isDeliverPayloads() {
        return parseBoolean(getFieldValue(ConfigureNodeFields.deliver_payloads));
    }

    public void setDeliverPayloads(boolean deliver) {
        addField(ConfigureNodeFields.deliver_payloads, FormField.TYPE_BOOLEAN);
        setAnswer(ConfigureNodeFields.deliver_payloads.getFieldName(), deliver);
    }

    public ItemReply getItemReply() {
        String value = getFieldValue(ConfigureNodeFields.itemreply);
        if (value == null) {
            return null;
        }
        return ItemReply.valueOf(value);
    }

    public void setItemReply(ItemReply reply) {
        addField(ConfigureNodeFields.itemreply, FormField.TYPE_LIST_SINGLE);
        setAnswer(ConfigureNodeFields.itemreply.getFieldName(), getListSingle(reply.toString()));
    }

    public int getMaxItems() {
        return Integer.parseInt(getFieldValue(ConfigureNodeFields.max_items));
    }

    public void setMaxItems(int max) {
        addField(ConfigureNodeFields.max_items, FormField.TYPE_TEXT_SINGLE);
        setAnswer(ConfigureNodeFields.max_items.getFieldName(), max);
    }

    public int getMaxPayloadSize() {
        return Integer.parseInt(getFieldValue(ConfigureNodeFields.max_payload_size));
    }

    public void setMaxPayloadSize(int max) {
        addField(ConfigureNodeFields.max_payload_size, FormField.TYPE_TEXT_SINGLE);
        setAnswer(ConfigureNodeFields.max_payload_size.getFieldName(), max);
    }

    public NodeType getNodeType() {
        String value = getFieldValue(ConfigureNodeFields.node_type);
        if (value == null) {
            return null;
        }
        return NodeType.valueOf(value);
    }

    public void setNodeType(NodeType type) {
        addField(ConfigureNodeFields.node_type, FormField.TYPE_LIST_SINGLE);
        setAnswer(ConfigureNodeFields.node_type.getFieldName(), getListSingle(type.toString()));
    }

    public boolean isNotifyConfig() {
        return parseBoolean(getFieldValue(ConfigureNodeFields.notify_config));
    }

    public void setNotifyConfig(boolean notify) {
        addField(ConfigureNodeFields.notify_config, FormField.TYPE_BOOLEAN);
        setAnswer(ConfigureNodeFields.notify_config.getFieldName(), notify);
    }

    public boolean isNotifyDelete() {
        return parseBoolean(getFieldValue(ConfigureNodeFields.notify_delete));
    }

    public void setNotifyDelete(boolean notify) {
        addField(ConfigureNodeFields.notify_delete, FormField.TYPE_BOOLEAN);
        setAnswer(ConfigureNodeFields.notify_delete.getFieldName(), notify);
    }

    public boolean isNotifyRetract() {
        return parseBoolean(getFieldValue(ConfigureNodeFields.notify_retract));
    }

    public void setNotifyRetract(boolean notify) {
        addField(ConfigureNodeFields.notify_retract, FormField.TYPE_BOOLEAN);
        setAnswer(ConfigureNodeFields.notify_retract.getFieldName(), notify);
    }

    public boolean isPersistItems() {
        return parseBoolean(getFieldValue(ConfigureNodeFields.persist_items));
    }

    public void setPersistentItems(boolean persist) {
        addField(ConfigureNodeFields.persist_items, FormField.TYPE_BOOLEAN);
        setAnswer(ConfigureNodeFields.persist_items.getFieldName(), persist);
    }

    public boolean isPresenceBasedDelivery() {
        return parseBoolean(getFieldValue(ConfigureNodeFields.presence_based_delivery));
    }

    public void setPresenceBasedDelivery(boolean presenceBased) {
        addField(ConfigureNodeFields.presence_based_delivery, FormField.TYPE_BOOLEAN);
        setAnswer(ConfigureNodeFields.presence_based_delivery.getFieldName(), presenceBased);
    }

    public PublishModel getPublishModel() {
        String value = getFieldValue(ConfigureNodeFields.publish_model);
        if (value == null) {
            return null;
        }
        return PublishModel.valueOf(value);
    }

    public void setPublishModel(PublishModel publish) {
        addField(ConfigureNodeFields.publish_model, FormField.TYPE_LIST_SINGLE);
        setAnswer(ConfigureNodeFields.publish_model.getFieldName(), getListSingle(publish.toString()));
    }

    public Iterator<String> getReplyRoom() {
        return getFieldValues(ConfigureNodeFields.replyroom);
    }

    public void setReplyRoom(List<String> replyRooms) {
        addField(ConfigureNodeFields.replyroom, FormField.TYPE_LIST_MULTI);
        setAnswer(ConfigureNodeFields.replyroom.getFieldName(), (List) replyRooms);
    }

    public Iterator<String> getReplyTo() {
        return getFieldValues(ConfigureNodeFields.replyto);
    }

    public void setReplyTo(List<String> replyTos) {
        addField(ConfigureNodeFields.replyto, FormField.TYPE_LIST_MULTI);
        setAnswer(ConfigureNodeFields.replyto.getFieldName(), (List) replyTos);
    }

    public Iterator<String> getRosterGroupsAllowed() {
        return getFieldValues(ConfigureNodeFields.roster_groups_allowed);
    }

    public void setRosterGroupsAllowed(List<String> groups) {
        addField(ConfigureNodeFields.roster_groups_allowed, FormField.TYPE_LIST_MULTI);
        setAnswer(ConfigureNodeFields.roster_groups_allowed.getFieldName(), (List) groups);
    }

    public boolean isSubscibe() {
        return parseBoolean(getFieldValue(ConfigureNodeFields.subscribe));
    }

    public void setSubscribe(boolean subscribe) {
        addField(ConfigureNodeFields.subscribe, FormField.TYPE_BOOLEAN);
        setAnswer(ConfigureNodeFields.subscribe.getFieldName(), subscribe);
    }

    public String getTitle() {
        return getFieldValue(ConfigureNodeFields.title);
    }

    public void setTitle(String title) {
        addField(ConfigureNodeFields.title, FormField.TYPE_TEXT_SINGLE);
        setAnswer(ConfigureNodeFields.title.getFieldName(), title);
    }

    public String getDataType() {
        return getFieldValue(ConfigureNodeFields.type);
    }

    public void setDataType(String type) {
        addField(ConfigureNodeFields.type, FormField.TYPE_TEXT_SINGLE);
        setAnswer(ConfigureNodeFields.type.getFieldName(), type);
    }

    public String toString() {
        StringBuilder result = new StringBuilder(getClass().getName() + " Content [");
        Iterator<FormField> fields = getFields();
        while (fields.hasNext()) {
            FormField formField = (FormField) fields.next();
            result.append('(');
            result.append(formField.getVariable());
            result.append(':');
            Iterator<String> values = formField.getValues();
            StringBuilder valuesBuilder = new StringBuilder();
            while (values.hasNext()) {
                if (valuesBuilder.length() > 0) {
                    result.append(',');
                }
                valuesBuilder.append((String) values.next());
            }
            if (valuesBuilder.length() == 0) {
                valuesBuilder.append("NOT SET");
            }
            result.append(valuesBuilder);
            result.append(')');
        }
        result.append(']');
        return result.toString();
    }

    private static boolean parseBoolean(String fieldValue) {
        return "1".equals(fieldValue) || "true".equals(fieldValue);
    }

    private String getFieldValue(ConfigureNodeFields field) {
        return (String) getField(field.getFieldName()).getValues().next();
    }

    private Iterator<String> getFieldValues(ConfigureNodeFields field) {
        return getField(field.getFieldName()).getValues();
    }

    private void addField(ConfigureNodeFields nodeField, String type) {
        String fieldName = nodeField.getFieldName();
        if (getField(fieldName) == null) {
            FormField field = new FormField(fieldName);
            field.setType(type);
            addField(field);
        }
    }

    private List<String> getListSingle(String value) {
        List<String> list = new ArrayList(1);
        list.add(value);
        return list;
    }
}
