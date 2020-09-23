package org.jivesoftware.smackx.packet;

import java.util.Collection;
import java.util.Collections;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.PacketExtension;

public class HeadersExtension implements PacketExtension {
    public static final String NAMESPACE = "http://jabber.org/protocol/shim";
    private Collection<Header> headers = Collections.EMPTY_LIST;

    public HeadersExtension(Collection<Header> headerList) {
        if (headerList != null) {
            this.headers = headerList;
        }
    }

    public Collection<Header> getHeaders() {
        return this.headers;
    }

    public String getElementName() {
        return "headers";
    }

    public String getNamespace() {
        return NAMESPACE;
    }

    public String toXML() {
        StringBuilder builder = new StringBuilder(Separators.LESS_THAN + getElementName() + " xmlns='" + getNamespace() + "'>");
        for (Header header : this.headers) {
            builder.append(header.toXML());
        }
        builder.append("</" + getElementName() + '>');
        return builder.toString();
    }
}
