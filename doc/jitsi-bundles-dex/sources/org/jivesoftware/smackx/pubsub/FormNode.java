package org.jivesoftware.smackx.pubsub;

import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smackx.Form;

public class FormNode extends NodeExtension {
    private Form configForm;

    public FormNode(FormNodeType formType, Form submitForm) {
        super(formType.getNodeElement());
        if (submitForm == null) {
            throw new IllegalArgumentException("Submit form cannot be null");
        }
        this.configForm = submitForm;
    }

    public FormNode(FormNodeType formType, String nodeId, Form submitForm) {
        super(formType.getNodeElement(), nodeId);
        if (submitForm == null) {
            throw new IllegalArgumentException("Submit form cannot be null");
        }
        this.configForm = submitForm;
    }

    public Form getForm() {
        return this.configForm;
    }

    public String toXML() {
        if (this.configForm == null) {
            return super.toXML();
        }
        StringBuilder builder = new StringBuilder(Separators.LESS_THAN);
        builder.append(getElementName());
        if (getNode() != null) {
            builder.append(" node='");
            builder.append(getNode());
            builder.append("'>");
        } else {
            builder.append('>');
        }
        builder.append(this.configForm.getDataFormToSend().toXML());
        builder.append("</");
        builder.append(getElementName() + '>');
        return builder.toString();
    }
}
