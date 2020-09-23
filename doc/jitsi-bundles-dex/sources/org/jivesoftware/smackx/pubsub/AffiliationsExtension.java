package org.jivesoftware.smackx.pubsub;

import java.util.Collections;
import java.util.List;
import org.jitsi.gov.nist.core.Separators;

public class AffiliationsExtension extends NodeExtension {
    protected List<Affiliation> items = Collections.EMPTY_LIST;

    public AffiliationsExtension() {
        super(PubSubElementType.AFFILIATIONS);
    }

    public AffiliationsExtension(List<Affiliation> subList) {
        super(PubSubElementType.AFFILIATIONS);
        this.items = subList;
    }

    public List<Affiliation> getAffiliations() {
        return this.items;
    }

    public String toXML() {
        if (this.items == null || this.items.size() == 0) {
            return super.toXML();
        }
        StringBuilder builder = new StringBuilder(Separators.LESS_THAN);
        builder.append(getElementName());
        builder.append(Separators.GREATER_THAN);
        for (Affiliation item : this.items) {
            builder.append(item.toXML());
        }
        builder.append("</");
        builder.append(getElementName());
        builder.append(Separators.GREATER_THAN);
        return builder.toString();
    }
}
