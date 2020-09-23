package net.java.sip.communicator.impl.protocol.sip.xcap.model;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.namespace.QName;
import org.jitsi.gov.nist.core.Separators;
import org.w3c.dom.Element;

public final class XmlUtils {
    public static void processAnyAttributes(Element element, Map<QName, String> anyAttributes) {
        for (Entry<QName, String> attribute : anyAttributes.entrySet()) {
            element.setAttributeNS(((QName) attribute.getKey()).getNamespaceURI(), ((QName) attribute.getKey()).getPrefix() + Separators.COLON + ((QName) attribute.getKey()).getLocalPart(), (String) attribute.getValue());
        }
    }

    public static void processAny(Element element, List<Element> any) throws Exception {
        for (Element anyElement : any) {
            element.appendChild(element.getOwnerDocument().importNode(anyElement, true));
        }
    }
}
