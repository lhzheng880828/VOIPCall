package net.java.sip.communicator.impl.protocol.sip.xcap.model.xcaperror;

import net.java.sip.communicator.impl.protocol.sip.xcap.model.ParsingException;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.xcaperror.UniquenessFailureType.ExistsType;
import org.jitsi.util.StringUtils;
import org.jitsi.util.xml.XMLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class XCapErrorParser {
    private static final String CANNOT_DELETE_ELEMENT = "cannot-delete";
    private static final String CANNOT_INSERT_ELEMENT = "cannot-insert";
    private static final String CONSTRAINT_FAILURE_ELEMENT = "constraint-failure";
    private static final String EXTENSION_ELEMENT = "extension";
    private static final String NAMESPACE = "urn:ietf:params:xml:ns:xcap-error";
    private static final String NOPARENT_ANCESTOR_ELEMENT = "ancestor";
    private static final String NOPARENT_ELEMENT = "no-parent";
    private static final String NOT_UTF8_ELEMENT = "not-utf-8";
    private static final String NOT_WELL_FORMED_ELEMENT = "not-well-formed";
    private static final String NOT_XMLF_FRAG_ELEMENT = "not-xml-frag";
    private static final String NOT_XML_ATT_VALUE_ELEMENT = "not-xml-att-value";
    private static final String PHRASE_ATTR = "phrase";
    private static final String SCHEMA_VALIDATION_ERROR_ELEMENT = "schema-validation-error";
    private static final String UNIQUENESS_FAILURE_ELEMENT = "uniqueness-failure";
    private static final String UNIQUENESS_FAILURE_EXISTS_ALT_VALUE_ELEMENT = "alt-value";
    private static final String UNIQUENESS_FAILURE_EXISTS_ELEMENT = "exists";
    private static final String UNIQUENESS_FAILURE_EXISTS_FIELD_ATTR = "field";
    private static final String XCAP_ERROR_ELEMENT = "xcap-error";

    public static XCapErrorType fromXml(String xml) throws ParsingException {
        if (StringUtils.isNullOrEmpty(xml)) {
            throw new IllegalArgumentException("XML cannot be null or empty");
        }
        try {
            XCapErrorType error = new XCapErrorType();
            Element xCapErrorElement = XMLUtils.createDocument(xml).getDocumentElement();
            if (NAMESPACE.equals(xCapErrorElement.getNamespaceURI()) && XCAP_ERROR_ELEMENT.equals(xCapErrorElement.getLocalName())) {
                int i;
                NamedNodeMap attributes = xCapErrorElement.getAttributes();
                for (i = 0; i < attributes.getLength(); i++) {
                    String namespaceUri = XMLUtils.getNamespaceUri((Attr) attributes.item(i));
                    if (namespaceUri == null || !XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                        throw new Exception("xcap-error element is invalid");
                    }
                }
                NodeList childNodes = xCapErrorElement.getChildNodes();
                for (i = 0; i < childNodes.getLength(); i++) {
                    Node node = childNodes.item(i);
                    if (node.getNodeType() == (short) 1) {
                        Element element = (Element) node;
                        if (NAMESPACE.equals(XMLUtils.getNamespaceUri(element))) {
                            error.setError(errorFromElement(element));
                        } else {
                            throw new Exception("xcap-error element is invalid");
                        }
                    }
                }
                return error;
            }
            throw new Exception("Document doesn't contain xcap-error element");
        } catch (Exception ex) {
            throw new ParsingException(ex);
        }
    }

    private static XCapError errorFromElement(Element element) throws Exception {
        if (NAMESPACE.equals(XMLUtils.getNamespaceUri(element))) {
            String localName = element.getLocalName();
            if (CANNOT_DELETE_ELEMENT.equals(localName)) {
                return new CannotDeleteType(getPhraseAttribute(element));
            }
            if (CANNOT_INSERT_ELEMENT.equals(localName)) {
                return new CannotInsertType(getPhraseAttribute(element));
            }
            if (CONSTRAINT_FAILURE_ELEMENT.equals(localName)) {
                return new ConstraintFailureType(getPhraseAttribute(element));
            }
            if (EXTENSION_ELEMENT.equals(localName)) {
                return getExtensionFromElement(element);
            }
            if (NOPARENT_ELEMENT.equals(localName)) {
                return getNoParentFromElement(element);
            }
            if (NOT_UTF8_ELEMENT.equals(localName)) {
                return new NotUtf8Type(getPhraseAttribute(element));
            }
            if (NOT_WELL_FORMED_ELEMENT.equals(localName)) {
                return new NotWellFormedType(getPhraseAttribute(element));
            }
            if (NOT_XML_ATT_VALUE_ELEMENT.equals(localName)) {
                return new NotXmlAttValueType(getPhraseAttribute(element));
            }
            if (NOT_XMLF_FRAG_ELEMENT.equals(localName)) {
                return new NotXmlAttValueType(getPhraseAttribute(element));
            }
            if (SCHEMA_VALIDATION_ERROR_ELEMENT.equals(localName)) {
                return new SchemaValidationErrorType(getPhraseAttribute(element));
            }
            if (UNIQUENESS_FAILURE_ELEMENT.equals(element.getLocalName())) {
                return getUniquenessFailureFromElement(element);
            }
            throw new Exception("content element is invalid");
        }
        throw new Exception("error-element element is invalid");
    }

    private static String getPhraseAttribute(Element element) throws Exception {
        String result = null;
        if (NAMESPACE.equals(XMLUtils.getNamespaceUri(element))) {
            int i;
            NamedNodeMap attributes = element.getAttributes();
            for (i = 0; i < attributes.getLength(); i++) {
                Attr attribute = (Attr) attributes.item(i);
                String namespaceUri = XMLUtils.getNamespaceUri(attribute);
                if (namespaceUri == null) {
                    throw new Exception("data element is invalid");
                }
                if (!XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    if (NAMESPACE.equals(namespaceUri) && PHRASE_ATTR.equals(attribute.getLocalName()) && result == null) {
                        result = attribute.getValue();
                    } else {
                        throw new Exception("error element is invalid");
                    }
                }
            }
            NodeList childNodes = element.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                if (childNodes.item(i).getNodeType() == (short) 1) {
                    throw new Exception("error element is invalid");
                }
            }
            return result;
        }
        throw new Exception("error element is invalid");
    }

    private static ExtensionType getExtensionFromElement(Element element) throws Exception {
        ExtensionType result = new ExtensionType();
        if (EXTENSION_ELEMENT.equals(element.getLocalName()) && NAMESPACE.equals(XMLUtils.getNamespaceUri(element))) {
            int i;
            String namespaceUri;
            NamedNodeMap attributes = element.getAttributes();
            for (i = 0; i < attributes.getLength(); i++) {
                namespaceUri = XMLUtils.getNamespaceUri((Attr) attributes.item(i));
                if (namespaceUri == null || !XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    throw new Exception("extension element is invalid");
                }
            }
            NodeList childNodes = element.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == (short) 1) {
                    Element childElement = (Element) node;
                    namespaceUri = XMLUtils.getNamespaceUri(childElement);
                    if (namespaceUri == null) {
                        throw new Exception("extension element is invalid");
                    } else if (NAMESPACE.equals(namespaceUri)) {
                        throw new Exception("extension element is invalid");
                    } else {
                        result.getAny().add(childElement);
                    }
                }
            }
            return result;
        }
        throw new Exception("extension element is invalid");
    }

    private static NoParentType getNoParentFromElement(Element element) throws Exception {
        NoParentType result = new NoParentType();
        if (NOPARENT_ELEMENT.equals(element.getLocalName()) && NAMESPACE.equals(XMLUtils.getNamespaceUri(element))) {
            int i;
            String namespaceUri;
            String phrase = null;
            String ancestor = null;
            NamedNodeMap attributes = element.getAttributes();
            for (i = 0; i < attributes.getLength(); i++) {
                Attr attribute = (Attr) attributes.item(i);
                namespaceUri = XMLUtils.getNamespaceUri(attribute);
                if (namespaceUri == null) {
                    throw new Exception("no-parent element is invalid");
                }
                if (!XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    if (NAMESPACE.equals(namespaceUri) && PHRASE_ATTR.equals(attribute.getLocalName()) && phrase == null) {
                        phrase = attribute.getValue();
                    } else {
                        throw new Exception("no-parent element is invalid");
                    }
                }
            }
            NodeList childNodes = element.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == (short) 1) {
                    Element childElement = (Element) node;
                    namespaceUri = XMLUtils.getNamespaceUri(childElement);
                    String localName = childElement.getLocalName();
                    if (namespaceUri == null) {
                        throw new Exception("no-parent element is invalid");
                    } else if (NAMESPACE.equals(namespaceUri) && NOPARENT_ANCESTOR_ELEMENT.equals(localName) && ancestor == null) {
                        ancestor = childElement.getTextContent();
                    } else {
                        throw new Exception("no-parent element is invalid");
                    }
                }
            }
            result.setPhrase(phrase);
            result.setAncestor(ancestor);
            return result;
        }
        throw new Exception("no-parent element is invalid");
    }

    private static UniquenessFailureType getUniquenessFailureFromElement(Element element) throws Exception {
        UniquenessFailureType result = new UniquenessFailureType();
        if (UNIQUENESS_FAILURE_ELEMENT.equals(element.getLocalName()) && NAMESPACE.equals(XMLUtils.getNamespaceUri(element))) {
            int i;
            String namespaceUri;
            String phrase = null;
            NamedNodeMap attributes = element.getAttributes();
            for (i = 0; i < attributes.getLength(); i++) {
                Attr attribute = (Attr) attributes.item(i);
                namespaceUri = XMLUtils.getNamespaceUri(attribute);
                if (namespaceUri == null) {
                    throw new Exception("uniqueness-failure element is invalid");
                }
                if (!XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    if (NAMESPACE.equals(namespaceUri) && PHRASE_ATTR.equals(attribute.getLocalName()) && phrase == null) {
                        phrase = attribute.getValue();
                    } else {
                        throw new Exception("uniqueness-failure element is invalid");
                    }
                }
            }
            NodeList childNodes = element.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == (short) 1) {
                    Element childElement = (Element) node;
                    namespaceUri = XMLUtils.getNamespaceUri(childElement);
                    String localName = childElement.getLocalName();
                    if (namespaceUri == null) {
                        throw new Exception("uniqueness-failure element is invalid");
                    } else if (NAMESPACE.equals(namespaceUri) && UNIQUENESS_FAILURE_EXISTS_ELEMENT.equals(localName)) {
                        result.getExists().add(getExistsFromElement(childElement));
                    } else {
                        throw new Exception("uniqueness-failure element is invalid");
                    }
                }
            }
            result.setPhrase(phrase);
            return result;
        }
        throw new Exception("uniqueness-failure element is invalid");
    }

    private static ExistsType getExistsFromElement(Element element) throws Exception {
        ExistsType result = new ExistsType();
        if (UNIQUENESS_FAILURE_EXISTS_ELEMENT.equals(element.getLocalName()) && NAMESPACE.equals(XMLUtils.getNamespaceUri(element))) {
            int i;
            String namespaceUri;
            String field = null;
            NamedNodeMap attributes = element.getAttributes();
            for (i = 0; i < attributes.getLength(); i++) {
                Attr attribute = (Attr) attributes.item(i);
                namespaceUri = XMLUtils.getNamespaceUri(attribute);
                if (namespaceUri == null) {
                    throw new Exception("exists element is invalid");
                }
                if (!XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    if (NAMESPACE.equals(namespaceUri) && UNIQUENESS_FAILURE_EXISTS_FIELD_ATTR.equals(attribute.getLocalName()) && field == null) {
                        field = attribute.getValue();
                    } else {
                        throw new Exception("exists element is invalid");
                    }
                }
            }
            NodeList childNodes = element.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == (short) 1) {
                    Element childElement = (Element) node;
                    namespaceUri = XMLUtils.getNamespaceUri(childElement);
                    String localName = childElement.getLocalName();
                    if (namespaceUri == null) {
                        throw new Exception("exists element is invalid");
                    } else if (NAMESPACE.equals(namespaceUri) && UNIQUENESS_FAILURE_EXISTS_ALT_VALUE_ELEMENT.equals(localName)) {
                        result.getAltValue().add(childElement.getTextContent());
                    } else {
                        throw new Exception("exists element is invalid");
                    }
                }
            }
            if (field == null) {
                throw new Exception("exists element is invalid");
            }
            result.setField(field);
            return result;
        }
        throw new Exception("exists element is invalid");
    }
}
