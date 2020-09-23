package net.java.sip.communicator.impl.protocol.sip.xcap.model.prescontent;

import java.util.Map;
import javax.xml.namespace.QName;
import net.java.sip.communicator.impl.protocol.jabber.extensions.thumbnail.ThumbnailElement;
import net.java.sip.communicator.impl.protocol.sip.xcap.PresContentClient;
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

public class PresContentParser {
    private static String CONTENT_ELEMENT = "content";
    private static String DATA_ELEMENT = "data";
    private static String DESCRIPTION_ELEMENT = "description";
    private static String DESCRIPTION_LANG_ATTR = "lang";
    private static String ENCODING_ELEMENT = "encoding";
    private static String MIMETYPE_ELEMENT = ThumbnailElement.MIME_TYPE;
    private static String NAMESPACE = PresContentClient.NAMESPACE;

    public static ContentType fromXml(String xml) throws ParsingException {
        if (StringUtils.isNullOrEmpty(xml)) {
            throw new IllegalArgumentException("XML cannot be null or empty");
        }
        try {
            ContentType content = new ContentType();
            Element contentElement = XMLUtils.createDocument(xml).getDocumentElement();
            if (NAMESPACE.equals(contentElement.getNamespaceURI()) && CONTENT_ELEMENT.equals(contentElement.getLocalName())) {
                int i;
                String namespaceUri;
                NamedNodeMap attributes = contentElement.getAttributes();
                for (i = 0; i < attributes.getLength(); i++) {
                    Attr attribute = (Attr) attributes.item(i);
                    namespaceUri = XMLUtils.getNamespaceUri(attribute);
                    if (namespaceUri == null) {
                        throw new Exception("content element is invalid");
                    }
                    if (!XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                        if (NAMESPACE.equals(namespaceUri)) {
                            throw new Exception("content element is invalid");
                        }
                        content.getAnyAttributes().put(new QName(namespaceUri, attribute.getLocalName(), attribute.getPrefix() == null ? "" : attribute.getPrefix()), attribute.getValue());
                    }
                }
                NodeList childNodes = contentElement.getChildNodes();
                for (i = 0; i < childNodes.getLength(); i++) {
                    Node node = childNodes.item(i);
                    if (node.getNodeType() == (short) 1) {
                        Element element = (Element) node;
                        namespaceUri = XMLUtils.getNamespaceUri(element);
                        if (namespaceUri == null) {
                            throw new Exception("content element is invalid");
                        }
                        String localName = node.getLocalName();
                        if (!NAMESPACE.equals(namespaceUri)) {
                            content.getAny().add(element);
                        } else if (DATA_ELEMENT.equals(localName)) {
                            content.setData(dataFromElement(element));
                        } else if (MIMETYPE_ELEMENT.equals(localName)) {
                            content.setMimeType(mimeTypeFromElement(element));
                        } else if (ENCODING_ELEMENT.equals(localName)) {
                            content.setEncoding(encodingFromElement(element));
                        } else if (DESCRIPTION_ELEMENT.equals(localName)) {
                            content.getDescription().add(descriptionFromElement(element));
                        } else {
                            throw new Exception("content element is invalid");
                        }
                    }
                }
                return content;
            }
            throw new Exception("Document doesn't contain content element");
        } catch (Exception ex) {
            throw new ParsingException(ex);
        }
    }

    public static String toXml(ContentType content) throws ParsingException {
        if (content == null) {
            throw new IllegalArgumentException("pres-content cannot be null");
        }
        try {
            Document document = XMLUtils.createDocument();
            Element presContentElement = document.createElementNS(NAMESPACE, CONTENT_ELEMENT);
            if (content.getData() != null) {
                presContentElement.appendChild(elementFromValue(document, DATA_ELEMENT, content.getData().getValue(), content.getData().getAnyAttributes()));
            }
            if (content.getEncoding() != null) {
                presContentElement.appendChild(elementFromValue(document, ENCODING_ELEMENT, content.getEncoding().getValue(), content.getEncoding().getAnyAttributes()));
            }
            if (content.getMimeType() != null) {
                presContentElement.appendChild(elementFromValue(document, MIMETYPE_ELEMENT, content.getMimeType().getValue(), content.getMimeType().getAnyAttributes()));
            }
            for (DescriptionType description : content.getDescription()) {
                presContentElement.appendChild(elementFromDescription(document, description));
            }
            XmlUtils.processAnyAttributes(presContentElement, content.getAnyAttributes());
            XmlUtils.processAny(presContentElement, content.getAny());
            document.appendChild(presContentElement);
            return XMLUtils.createXml(document);
        } catch (Exception ex) {
            throw new ParsingException(ex);
        }
    }

    private static Element elementFromValue(Document document, String nodeName, String value, Map<QName, String> anyAttributes) {
        Element element = document.createElementNS(NAMESPACE, nodeName);
        if (value != null) {
            element.setTextContent(value);
        }
        XmlUtils.processAnyAttributes(element, anyAttributes);
        return element;
    }

    private static Element elementFromDescription(Document document, DescriptionType description) throws Exception {
        Element element = document.createElementNS(NAMESPACE, DESCRIPTION_ELEMENT);
        if (description.getLang() != null) {
            element.setAttribute("xml:" + DESCRIPTION_LANG_ATTR, description.getLang());
        }
        if (description.getValue() != null) {
            element.setTextContent(description.getValue());
        }
        XmlUtils.processAnyAttributes(element, description.getAnyAttributes());
        return element;
    }

    private static DataType dataFromElement(Element element) throws Exception {
        DataType result = new DataType();
        if (DATA_ELEMENT.equals(element.getLocalName()) && NAMESPACE.equals(XMLUtils.getNamespaceUri(element))) {
            int i;
            NamedNodeMap attributes = element.getAttributes();
            for (i = 0; i < attributes.getLength(); i++) {
                Attr attribute = (Attr) attributes.item(i);
                String namespaceUri = XMLUtils.getNamespaceUri(attribute);
                if (namespaceUri == null) {
                    throw new Exception("data element is invalid");
                }
                if (!XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    if (NAMESPACE.equals(namespaceUri)) {
                        throw new Exception("data element is invalid");
                    }
                    result.getAnyAttributes().put(new QName(namespaceUri, attribute.getLocalName(), attribute.getPrefix() == null ? "" : attribute.getPrefix()), attribute.getValue());
                }
            }
            NodeList childNodes = element.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                if (childNodes.item(i).getNodeType() == (short) 1) {
                    throw new Exception("data element is invalid");
                }
            }
            result.setValue(element.getTextContent());
            return result;
        }
        throw new Exception("data element is invalid");
    }

    private static EncodingType encodingFromElement(Element element) throws Exception {
        EncodingType result = new EncodingType();
        if (ENCODING_ELEMENT.equals(element.getLocalName()) && NAMESPACE.equals(XMLUtils.getNamespaceUri(element))) {
            int i;
            NamedNodeMap attributes = element.getAttributes();
            for (i = 0; i < attributes.getLength(); i++) {
                Attr attribute = (Attr) attributes.item(i);
                String namespaceUri = XMLUtils.getNamespaceUri(attribute);
                if (namespaceUri == null) {
                    throw new Exception("encoding element is invalid");
                }
                if (!XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    if (NAMESPACE.equals(namespaceUri)) {
                        throw new Exception("encoding element is invalid");
                    }
                    result.getAnyAttributes().put(new QName(namespaceUri, attribute.getLocalName(), attribute.getPrefix() == null ? "" : attribute.getPrefix()), attribute.getValue());
                }
            }
            NodeList childNodes = element.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                if (childNodes.item(i).getNodeType() == (short) 1) {
                    throw new Exception("encoding element is invalid");
                }
            }
            result.setValue(element.getTextContent());
            return result;
        }
        throw new Exception("encoding element is invalid");
    }

    private static MimeType mimeTypeFromElement(Element element) throws Exception {
        MimeType result = new MimeType();
        if (MIMETYPE_ELEMENT.equals(element.getLocalName()) && NAMESPACE.equals(XMLUtils.getNamespaceUri(element))) {
            NamedNodeMap attributes = element.getAttributes();
            int i = 0;
            while (i < attributes.getLength()) {
                String namespaceUri = XMLUtils.getNamespaceUri((Attr) attributes.item(i));
                if (namespaceUri == null) {
                    throw new Exception("mime-type element is invalid");
                } else if (!XMLUtils.isStandartXmlNamespace(namespaceUri) && NAMESPACE.equals(namespaceUri)) {
                    throw new Exception("mime-type element is invalid");
                } else {
                    i++;
                }
            }
            NodeList childNodes = element.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                if (childNodes.item(i).getNodeType() == (short) 1) {
                    throw new Exception("encoding element is invalid");
                }
            }
            result.setValue(element.getTextContent());
            return result;
        }
        throw new Exception("mime-type element is invalid");
    }

    private static DescriptionType descriptionFromElement(Element element) throws Exception {
        DescriptionType result = new DescriptionType();
        if (DESCRIPTION_ELEMENT.equals(element.getLocalName()) && NAMESPACE.equals(XMLUtils.getNamespaceUri(element))) {
            int i;
            NamedNodeMap attributes = element.getAttributes();
            for (i = 0; i < attributes.getLength(); i++) {
                Attr attribute = (Attr) attributes.item(i);
                String namespaceUri = XMLUtils.getNamespaceUri(attribute);
                if (namespaceUri == null) {
                    throw new Exception("description element is invalid");
                }
                if (DESCRIPTION_LANG_ATTR.equals(attribute.getLocalName()) && "http://www.w3.org/XML/1998/namespace".equals(namespaceUri)) {
                    result.setLang(attribute.getValue());
                } else if (XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    continue;
                } else if (NAMESPACE.equals(namespaceUri)) {
                    throw new Exception("description element is invalid");
                } else {
                    result.getAnyAttributes().put(new QName(namespaceUri, attribute.getLocalName(), attribute.getPrefix() == null ? "" : attribute.getPrefix()), attribute.getValue());
                }
            }
            NodeList childNodes = element.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                if (childNodes.item(i).getNodeType() == (short) 1) {
                    throw new Exception("description element is invalid");
                }
            }
            result.setValue(element.getTextContent());
            return result;
        }
        throw new Exception("description element is invalid");
    }
}
