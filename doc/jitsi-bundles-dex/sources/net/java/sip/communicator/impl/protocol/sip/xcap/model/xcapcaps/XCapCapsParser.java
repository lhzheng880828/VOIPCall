package net.java.sip.communicator.impl.protocol.sip.xcap.model.xcapcaps;

import net.java.sip.communicator.impl.protocol.sip.xcap.model.ParsingException;
import org.jitsi.util.StringUtils;
import org.jitsi.util.xml.XMLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class XCapCapsParser {
    private static String AUIDS_ELEMENT = "auids";
    private static String AUID_ELEMENT = "auid";
    private static String EXTENSIONS_ELEMENT = "extensions";
    private static String EXTENSION_ELEMENT = "extension";
    private static final String NAMESPACE = "urn:ietf:params:xml:ns:xcap-caps";
    private static String NAMESPACES_ELEMENT = "namespaces";
    private static String NAMESPACE_ELEMENT = "namespace";
    private static String XCAPCAPS_ELEMENT = "xcap-caps";

    public static XCapCapsType fromXml(String xml) throws ParsingException {
        if (StringUtils.isNullOrEmpty(xml)) {
            throw new IllegalArgumentException("XML cannot be null or empty");
        }
        try {
            XCapCapsType xCapCaps = new XCapCapsType();
            Element xCapCapsElement = XMLUtils.createDocument(xml).getDocumentElement();
            if (!XCAPCAPS_ELEMENT.equals(xCapCapsElement.getLocalName()) || "urn:ietf:params:xml:ns:xcap-caps".equals(xCapCapsElement.getNamespaceURI())) {
                int i;
                String namespaceUri;
                boolean auidsFound = false;
                boolean namespacesFound = false;
                NamedNodeMap attributes = xCapCapsElement.getAttributes();
                for (i = 0; i < attributes.getLength(); i++) {
                    namespaceUri = XMLUtils.getNamespaceUri((Attr) attributes.item(i));
                    if (namespaceUri == null || !XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                        throw new Exception("xcap-caps element is invalid");
                    }
                }
                NodeList childNodes = xCapCapsElement.getChildNodes();
                for (i = 0; i < childNodes.getLength(); i++) {
                    Node node = childNodes.item(i);
                    if (node.getNodeType() == (short) 1) {
                        Element element = (Element) node;
                        namespaceUri = XMLUtils.getNamespaceUri(element);
                        if (namespaceUri == null) {
                            throw new Exception("xcap-caps element is invalid");
                        }
                        String localName = node.getLocalName();
                        if (!"urn:ietf:params:xml:ns:xcap-caps".equals(namespaceUri)) {
                            xCapCaps.getAny().add(element);
                        } else if (AUIDS_ELEMENT.equals(localName)) {
                            xCapCaps.setAuids(auidsFromElement(element));
                            auidsFound = true;
                        } else if (NAMESPACES_ELEMENT.equals(localName)) {
                            xCapCaps.setNamespaces(namespacesFromElement(element));
                            namespacesFound = true;
                        } else if (EXTENSIONS_ELEMENT.equals(localName)) {
                            xCapCaps.setExtensions(extensionsFromElement(element));
                        } else {
                            throw new Exception("xcap-caps element is invalid");
                        }
                    }
                }
                if (!auidsFound) {
                    throw new ParsingException("xcap-caps auids element is missed");
                } else if (namespacesFound) {
                    return xCapCaps;
                } else {
                    throw new ParsingException("xcap-caps namespaces element is missed");
                }
            }
            throw new Exception("Document doesn't contain xcap-caps element");
        } catch (Exception ex) {
            throw new ParsingException(ex);
        }
    }

    private static AuidsType auidsFromElement(Element auidsElement) throws Exception {
        AuidsType auidsType = new AuidsType();
        if (AUIDS_ELEMENT.equals(auidsElement.getLocalName()) && "urn:ietf:params:xml:ns:xcap-caps".equals(XMLUtils.getNamespaceUri(auidsElement))) {
            int i;
            String namespaceUri;
            NamedNodeMap attributes = auidsElement.getAttributes();
            for (i = 0; i < attributes.getLength(); i++) {
                namespaceUri = XMLUtils.getNamespaceUri((Attr) attributes.item(i));
                if (namespaceUri == null || !XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    throw new Exception("auids element is invalid");
                }
            }
            NodeList childNodes = auidsElement.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == (short) 1) {
                    Element element = (Element) node;
                    namespaceUri = XMLUtils.getNamespaceUri(element);
                    if (namespaceUri == null) {
                        throw new Exception("auids element is invalid");
                    } else if ("urn:ietf:params:xml:ns:xcap-caps".equals(namespaceUri) && AUID_ELEMENT.equals(element.getLocalName())) {
                        auidsType.getAuid().add(element.getTextContent());
                    } else {
                        throw new Exception("auids element is invalid");
                    }
                }
            }
            return auidsType;
        }
        throw new Exception("auids element is invalid");
    }

    private static NamespacesType namespacesFromElement(Element namespacesElement) throws Exception {
        NamespacesType namespaces = new NamespacesType();
        if (NAMESPACES_ELEMENT.equals(namespacesElement.getLocalName()) && "urn:ietf:params:xml:ns:xcap-caps".equals(XMLUtils.getNamespaceUri(namespacesElement))) {
            int i;
            String namespaceUri;
            NamedNodeMap attributes = namespacesElement.getAttributes();
            for (i = 0; i < attributes.getLength(); i++) {
                namespaceUri = XMLUtils.getNamespaceUri((Attr) attributes.item(i));
                if (namespaceUri == null || !XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    throw new Exception("namespaces element is invalid");
                }
            }
            NodeList childNodes = namespacesElement.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == (short) 1) {
                    Element element = (Element) node;
                    namespaceUri = XMLUtils.getNamespaceUri(element);
                    if (namespaceUri == null) {
                        throw new Exception("namespaces element is invalid");
                    } else if ("urn:ietf:params:xml:ns:xcap-caps".equals(namespaceUri) && NAMESPACE_ELEMENT.equals(element.getLocalName())) {
                        namespaces.getNamespace().add(element.getTextContent());
                    } else {
                        throw new Exception("namespaces element is invalid");
                    }
                }
            }
            return namespaces;
        }
        throw new Exception("namespaces element is invalid");
    }

    private static ExtensionsType extensionsFromElement(Element extensionsElement) throws Exception {
        ExtensionsType extensions = new ExtensionsType();
        if (EXTENSIONS_ELEMENT.equals(extensionsElement.getLocalName()) && "urn:ietf:params:xml:ns:xcap-caps".equals(XMLUtils.getNamespaceUri(extensionsElement))) {
            int i;
            String namespaceUri;
            NamedNodeMap attributes = extensionsElement.getAttributes();
            for (i = 0; i < attributes.getLength(); i++) {
                namespaceUri = XMLUtils.getNamespaceUri((Attr) attributes.item(i));
                if (namespaceUri == null || !XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    throw new Exception("extensions element is invalid");
                }
            }
            NodeList childNodes = extensionsElement.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == (short) 1) {
                    Element element = (Element) node;
                    namespaceUri = XMLUtils.getNamespaceUri(element);
                    if (namespaceUri == null) {
                        throw new Exception("extensions element is invalid");
                    } else if ("urn:ietf:params:xml:ns:xcap-caps".equals(namespaceUri) && EXTENSION_ELEMENT.equals(element.getLocalName())) {
                        extensions.getExtension().add(element.getTextContent());
                    } else {
                        throw new Exception("extensions element is invalid");
                    }
                }
            }
            return extensions;
        }
        throw new Exception("extensions element is invalid");
    }
}
