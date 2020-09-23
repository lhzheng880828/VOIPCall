package net.java.sip.communicator.impl.protocol.sip.xcap.model.resourcelists;

import javax.xml.namespace.QName;
import net.java.sip.communicator.impl.protocol.jabber.extensions.coin.UserRolesPacketExtension;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.ParsingException;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.XmlUtils;
import org.jitsi.util.StringUtils;
import org.jitsi.util.xml.XMLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class ResourceListsParser {
    private static String DISPALY_NAME_ELEMENT = "display-name";
    private static String DISPALY_NAME_LANG_ATTR = "lang";
    private static String ENTRYREF_ELEMENT = "entry-ref";
    private static String ENTRYREF_REF_ATTR = "ref";
    private static String ENTRY_ELEMENT = UserRolesPacketExtension.ELEMENT_ROLE;
    private static String ENTRY_URI_ATTR = "uri";
    private static String EXTERNAL_ANCHOR_ATTR = "anchor";
    private static String EXTERNAL_ELEMENT = "external";
    private static String LIST_ELEMENT = "list";
    private static String LIST_NAME_ATTR = "name";
    private static String NAMESPACE = "urn:ietf:params:xml:ns:resource-lists";
    private static String RESOURCE_LISTS_ELEMENT = "resource-lists";

    private ResourceListsParser() {
    }

    public static ResourceListsType fromXml(String xml) throws ParsingException {
        if (StringUtils.isNullOrEmpty(xml)) {
            throw new IllegalArgumentException("XML cannot be null or empty");
        }
        try {
            ResourceListsType resourceLists = new ResourceListsType();
            Element resourceListsElement = XMLUtils.createDocument(xml).getDocumentElement();
            String localName = resourceListsElement.getLocalName();
            if (NAMESPACE.equals(resourceListsElement.getNamespaceURI()) && RESOURCE_LISTS_ELEMENT.equals(localName)) {
                NamedNodeMap attributes = resourceListsElement.getAttributes();
                int i = 0;
                while (i < attributes.getLength()) {
                    String namespaceUri = XMLUtils.getNamespaceUri((Attr) attributes.item(i));
                    if (namespaceUri == null) {
                        throw new Exception("resource-lists element is invalid");
                    } else if (XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                        i++;
                    } else {
                        throw new Exception("resource-lists element is invalid");
                    }
                }
                NodeList childNodes = resourceListsElement.getChildNodes();
                for (i = 0; i < childNodes.getLength(); i++) {
                    Node node = childNodes.item(i);
                    if (node.getNodeType() == (short) 1) {
                        resourceLists.getList().add(listFromElement((Element) node));
                    }
                }
                return resourceLists;
            }
            throw new Exception("Document doesn't contain resource-lists element");
        } catch (Exception ex) {
            throw new ParsingException(ex);
        }
    }

    public static String toXml(ResourceListsType resourceLists) throws ParsingException {
        if (resourceLists == null) {
            throw new IllegalArgumentException("resource-lists cannot be null");
        }
        try {
            Document document = XMLUtils.createDocument();
            Element resourceListsElement = document.createElementNS(NAMESPACE, RESOURCE_LISTS_ELEMENT);
            for (ListType list : resourceLists.getList()) {
                resourceListsElement.appendChild(elementFromList(document, list));
            }
            document.appendChild(resourceListsElement);
            return XMLUtils.createXml(document);
        } catch (Exception ex) {
            throw new ParsingException(ex);
        }
    }

    private static ListType listFromElement(Element listElement) throws Exception {
        ListType list = new ListType();
        if (LIST_ELEMENT.equals(listElement.getLocalName()) && NAMESPACE.equals(XMLUtils.getNamespaceUri(listElement))) {
            int i;
            NamedNodeMap attributes = listElement.getAttributes();
            for (i = 0; i < attributes.getLength(); i++) {
                Attr attribute = (Attr) attributes.item(i);
                String namespaceUri = XMLUtils.getNamespaceUri(attribute);
                if (namespaceUri == null) {
                    throw new Exception("list element is invalid");
                }
                if (!XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    if (!NAMESPACE.equals(namespaceUri)) {
                        list.getAnyAttributes().put(new QName(namespaceUri, attribute.getLocalName(), attribute.getPrefix() == null ? "" : attribute.getPrefix()), attribute.getValue());
                    } else if (LIST_NAME_ATTR.equals(attribute.getLocalName())) {
                        list.setName(attribute.getValue());
                    } else {
                        throw new Exception("list element is invalid");
                    }
                }
            }
            NodeList childNodes = listElement.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == (short) 1) {
                    Element element = (Element) node;
                    String localName = element.getLocalName();
                    if (!NAMESPACE.equals(XMLUtils.getNamespaceUri(element))) {
                        list.getAny().add(element);
                    } else if (DISPALY_NAME_ELEMENT.equals(localName)) {
                        list.setDisplayName(displayNameFromElement(element));
                    } else if (ENTRY_ELEMENT.equals(localName)) {
                        list.getEntries().add(entryFromElement(element));
                    } else if (ENTRYREF_ELEMENT.equals(localName)) {
                        list.getEntryRefs().add(entryRefFromElement(element));
                    } else if (LIST_ELEMENT.equals(localName)) {
                        list.getLists().add(listFromElement(element));
                    } else if (EXTERNAL_ELEMENT.equals(localName)) {
                        list.getExternals().add(externalFromElement(element));
                    } else {
                        throw new Exception("list element is invalid");
                    }
                }
            }
            return list;
        }
        throw new Exception("list element is invalid");
    }

    private static Element elementFromList(Document document, ListType list) throws Exception {
        Element listElement = document.createElementNS(NAMESPACE, LIST_ELEMENT);
        if (list.getName() != null) {
            listElement.setAttribute(LIST_NAME_ATTR, list.getName());
        }
        if (list.getDisplayName() != null) {
            listElement.appendChild(elementFromDisplayName(document, list.getDisplayName()));
        }
        for (EntryType entry : list.getEntries()) {
            listElement.appendChild(elementFromEntry(document, entry));
        }
        for (EntryRefType entryRef : list.getEntryRefs()) {
            listElement.appendChild(elementFromEntryRef(document, entryRef));
        }
        for (ListType subList : list.getLists()) {
            listElement.appendChild(elementFromList(document, subList));
        }
        for (ExternalType external : list.getExternals()) {
            listElement.appendChild(elementFromExternal(document, external));
        }
        XmlUtils.processAnyAttributes(listElement, list.getAnyAttributes());
        XmlUtils.processAny(listElement, list.getAny());
        return listElement;
    }

    private static Element elementFromEntry(Document document, EntryType entry) throws Exception {
        Element entryElement = document.createElementNS(NAMESPACE, ENTRY_ELEMENT);
        if (StringUtils.isNullOrEmpty(entry.getUri())) {
            throw new Exception("entry uri attribute is missed");
        }
        entryElement.setAttribute(ENTRY_URI_ATTR, entry.getUri());
        if (entry.getDisplayName() != null) {
            entryElement.appendChild(elementFromDisplayName(document, entry.getDisplayName()));
        }
        XmlUtils.processAnyAttributes(entryElement, entry.getAnyAttributes());
        XmlUtils.processAny(entryElement, entry.getAny());
        return entryElement;
    }

    private static Element elementFromEntryRef(Document document, EntryRefType entryRef) throws Exception {
        Element entryRefElement = document.createElementNS(NAMESPACE, ENTRYREF_ELEMENT);
        if (StringUtils.isNullOrEmpty(entryRef.getRef())) {
            throw new Exception("entry-ref ref attribute is missed");
        }
        entryRefElement.setAttribute(ENTRYREF_REF_ATTR, entryRef.getRef());
        if (entryRef.getDisplayName() != null) {
            entryRefElement.appendChild(elementFromDisplayName(document, entryRef.getDisplayName()));
        }
        XmlUtils.processAnyAttributes(entryRefElement, entryRef.getAnyAttributes());
        XmlUtils.processAny(entryRefElement, entryRef.getAny());
        return entryRefElement;
    }

    private static Element elementFromExternal(Document document, ExternalType external) throws Exception {
        Element externalElement = document.createElementNS(NAMESPACE, EXTERNAL_ELEMENT);
        if (!StringUtils.isNullOrEmpty(external.getAnchor())) {
            externalElement.setAttribute(EXTERNAL_ANCHOR_ATTR, external.getAnchor());
        }
        if (external.getDisplayName() != null) {
            externalElement.appendChild(elementFromDisplayName(document, external.getDisplayName()));
        }
        XmlUtils.processAnyAttributes(externalElement, external.getAnyAttributes());
        XmlUtils.processAny(externalElement, external.getAny());
        return externalElement;
    }

    private static Element elementFromDisplayName(Document document, DisplayNameType displayName) throws Exception {
        Element displayNameElement = document.createElementNS(NAMESPACE, DISPALY_NAME_ELEMENT);
        if (displayName.getLang() != null) {
            displayNameElement.setAttribute("xml:" + DISPALY_NAME_LANG_ATTR, displayName.getLang());
        }
        if (displayName.getValue() != null) {
            displayNameElement.setTextContent(displayName.getValue());
        }
        return displayNameElement;
    }

    private static EntryType entryFromElement(Element entryElement) throws Exception {
        EntryType entry = new EntryType();
        if (ENTRY_ELEMENT.equals(entryElement.getNodeName()) && NAMESPACE.equals(XMLUtils.getNamespaceUri(entryElement))) {
            int i;
            String namespaceUri;
            String uri = null;
            NamedNodeMap attributes = entryElement.getAttributes();
            for (i = 0; i < attributes.getLength(); i++) {
                Attr attribute = (Attr) attributes.item(i);
                namespaceUri = XMLUtils.getNamespaceUri(attribute);
                if (namespaceUri == null) {
                    throw new Exception("entry element is invalid");
                }
                if (!XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    if (!NAMESPACE.equals(namespaceUri)) {
                        entry.getAnyAttributes().put(new QName(namespaceUri, attribute.getLocalName(), attribute.getPrefix() == null ? "" : attribute.getPrefix()), attribute.getValue());
                    } else if (ENTRY_URI_ATTR.equals(attribute.getLocalName())) {
                        uri = attribute.getValue();
                    } else {
                        throw new Exception("entry element is invalid");
                    }
                }
            }
            if (uri == null) {
                throw new Exception("entry uri attribute is missed");
            }
            entry.setUri(uri);
            NodeList childNodes = entryElement.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == (short) 1) {
                    Element element = (Element) node;
                    namespaceUri = XMLUtils.getNamespaceUri(element);
                    if (namespaceUri == null) {
                        throw new Exception("entry element is invalid");
                    } else if (!NAMESPACE.equals(namespaceUri)) {
                        entry.getAny().add(element);
                    } else if (DISPALY_NAME_ELEMENT.equals(element.getLocalName())) {
                        entry.setDisplayName(displayNameFromElement(element));
                    } else {
                        throw new Exception("entry element is invalid");
                    }
                }
            }
            return entry;
        }
        throw new Exception("entry element is invalid");
    }

    private static EntryRefType entryRefFromElement(Element entryRefElement) throws Exception {
        EntryRefType entryRef = new EntryRefType();
        if (ENTRYREF_ELEMENT.equals(entryRefElement.getLocalName()) && NAMESPACE.equals(XMLUtils.getNamespaceUri(entryRefElement))) {
            int i;
            String namespaceUri;
            String ref = null;
            NamedNodeMap attributes = entryRefElement.getAttributes();
            for (i = 0; i < attributes.getLength(); i++) {
                Attr attribute = (Attr) attributes.item(i);
                namespaceUri = XMLUtils.getNamespaceUri(attribute);
                if (namespaceUri == null) {
                    throw new Exception("entry-ref element is invalid");
                }
                if (!XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    if (!NAMESPACE.equals(namespaceUri)) {
                        entryRef.getAnyAttributes().put(new QName(attribute.getNamespaceURI(), attribute.getName(), attribute.getPrefix() == null ? "" : attribute.getPrefix()), attribute.getValue());
                    } else if (ENTRYREF_REF_ATTR.equals(attribute.getLocalName())) {
                        ref = attribute.getValue();
                    } else {
                        throw new Exception("entry-ref element is invalid");
                    }
                }
            }
            if (ref == null) {
                throw new Exception("entry-ref ref attribute is missed");
            }
            entryRef.setRef(ref);
            NodeList childNodes = entryRefElement.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == (short) 1) {
                    Element element = (Element) node;
                    namespaceUri = XMLUtils.getNamespaceUri(element);
                    if (namespaceUri == null) {
                        throw new Exception("entry-ref element is invalid");
                    } else if (!NAMESPACE.equals(namespaceUri)) {
                        entryRef.getAny().add(element);
                    } else if (DISPALY_NAME_ELEMENT.equals(element.getLocalName())) {
                        entryRef.setDisplayName(displayNameFromElement(element));
                    } else {
                        throw new Exception("entry-ref element is invalid");
                    }
                }
            }
            return entryRef;
        }
        throw new Exception("entry-ref element is invalid");
    }

    private static ExternalType externalFromElement(Element entryElement) throws Exception {
        ExternalType external = new ExternalType();
        if (EXTERNAL_ELEMENT.equals(entryElement.getLocalName()) && NAMESPACE.equals(XMLUtils.getNamespaceUri(entryElement))) {
            int i;
            String namespaceUri;
            NamedNodeMap attributes = entryElement.getAttributes();
            for (i = 0; i < attributes.getLength(); i++) {
                Attr attribute = (Attr) attributes.item(i);
                namespaceUri = XMLUtils.getNamespaceUri(attribute);
                if (namespaceUri == null) {
                    throw new Exception("external element is invalid");
                }
                if (!XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    if (!NAMESPACE.equals(namespaceUri)) {
                        external.getAnyAttributes().put(new QName(attribute.getNamespaceURI(), attribute.getName(), attribute.getPrefix() == null ? "" : attribute.getPrefix()), attribute.getValue());
                    } else if (EXTERNAL_ANCHOR_ATTR.equals(attribute.getLocalName())) {
                        external.setAnchor(attribute.getValue());
                    } else {
                        throw new Exception("external element is invalid");
                    }
                }
            }
            NodeList childNodes = entryElement.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == (short) 1) {
                    Element element = (Element) node;
                    namespaceUri = XMLUtils.getNamespaceUri(element);
                    if (namespaceUri == null) {
                        throw new Exception("external element is invalid");
                    } else if (!NAMESPACE.equals(namespaceUri)) {
                        external.getAny().add(element);
                    } else if (DISPALY_NAME_ELEMENT.equals(element.getLocalName())) {
                        external.setDisplayName(displayNameFromElement(element));
                    } else {
                        throw new Exception("external element is invalid");
                    }
                }
            }
            return external;
        }
        throw new Exception("external element is invalid");
    }

    private static DisplayNameType displayNameFromElement(Element displayNameElement) throws Exception {
        DisplayNameType displayName = new DisplayNameType();
        if (DISPALY_NAME_ELEMENT.equals(displayNameElement.getLocalName()) && NAMESPACE.equals(XMLUtils.getNamespaceUri(displayNameElement))) {
            int i;
            NamedNodeMap attributes = displayNameElement.getAttributes();
            for (i = 0; i < attributes.getLength(); i++) {
                Attr attribute = (Attr) attributes.item(i);
                String namespaceUri = XMLUtils.getNamespaceUri(attribute);
                if (namespaceUri == null) {
                    throw new Exception("display-name element is invalid");
                }
                if (DISPALY_NAME_LANG_ATTR.equals(attribute.getLocalName()) && "http://www.w3.org/XML/1998/namespace".equals(namespaceUri)) {
                    displayName.setLang(attribute.getValue());
                } else if (!XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    throw new Exception("display-name element is invalid");
                }
            }
            NodeList childNodes = displayNameElement.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                if (childNodes.item(i).getNodeType() == (short) 1) {
                    throw new Exception("display-name element is invalid");
                }
            }
            displayName.setValue(displayNameElement.getTextContent());
            return displayName;
        }
        throw new Exception("display-name element is invalid");
    }
}
