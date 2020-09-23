package org.jitsi.util.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.jitsi.impl.neomedia.format.MediaFormatImpl;
import org.jitsi.util.Logger;
import org.jitsi.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class XMLUtils {
    private static final Logger logger = Logger.getLogger(XMLUtils.class);

    public static String getAttribute(Node node, String name) {
        if (node == null) {
            return null;
        }
        Node attribute = node.getAttributes().getNamedItem(name);
        if (attribute != null) {
            return attribute.getNodeValue().trim();
        }
        return null;
    }

    public static String getText(Element parentNode) {
        Text text = getTextNode(parentNode);
        return text == null ? null : text.getData();
    }

    public static void setText(Element parentNode, String data) {
        if (data != null) {
            Text txt = getTextNode(parentNode);
            if (txt != null) {
                txt.setData(data);
            } else {
                parentNode.appendChild(parentNode.getOwnerDocument().createTextNode(data));
            }
        }
    }

    public static void setCData(Element element, String data) {
        if (data != null) {
            CDATASection txt = getCDataNode(element);
            if (txt != null) {
                txt.setData(data);
            } else {
                element.appendChild(element.getOwnerDocument().createCDATASection(data));
            }
        }
    }

    public static String getCData(Element element) {
        CDATASection text = getCDataNode(element);
        return text == null ? null : text.getData().trim();
    }

    public static CDATASection getCDataNode(Element element) {
        return (CDATASection) getChildByType(element, (short) 4);
    }

    public static Text getTextNode(Element element) {
        return (Text) getChildByType(element, (short) 3);
    }

    public static Node getChildByType(Element element, short nodeType) {
        if (element == null) {
            return null;
        }
        NodeList nodes = element.getChildNodes();
        if (nodes == null || nodes.getLength() < 1) {
            return null;
        }
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            short type = node.getNodeType();
            if (type == nodeType) {
                if (type != (short) 3 && type != (short) 4) {
                    return node;
                }
                String data = ((Text) node).getData();
                if (data != null && data.trim().length() >= 1) {
                    return node;
                }
            }
        }
        return null;
    }

    public static void writeXML(Document document, File out) throws IOException {
        writeXML(document, new StreamResult(new OutputStreamWriter(new FileOutputStream(out), "UTF-8")), null, null);
    }

    public static void writeXML(Document document, Writer writer) throws IOException {
        writeXML(document, new StreamResult(writer), null, null);
    }

    public static void writeXML(Document document, StreamResult streamResult, String doctypeSystem, String doctypePublic) throws IOException {
        try {
            DOMSource domSource = new DOMSource(document);
            TransformerFactory tf = TransformerFactory.newInstance();
            try {
                tf.setAttribute("indent-number", Integer.valueOf(4));
            } catch (Exception e) {
            }
            Transformer serializer = tf.newTransformer();
            if (doctypeSystem != null) {
                serializer.setOutputProperty("doctype-system", doctypeSystem);
            }
            if (doctypePublic != null) {
                serializer.setOutputProperty("doctype-public", doctypePublic);
            }
            serializer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "4");
            serializer.setOutputProperty(MediaFormatImpl.ENCODING_PNAME, "UTF-8");
            serializer.setOutputProperty("indent", "yes");
            serializer.transform(domSource, streamResult);
        } catch (TransformerException ex) {
            logger.error("Error saving configuration file", ex);
            throw new IOException("Failed to write the configuration file: " + ex.getMessageAndLocation());
        } catch (IllegalArgumentException ex2) {
            logger.error("Error saving configuration file", ex2);
        }
    }

    public static void indentedWriteXML(Document doc, OutputStream out) {
        if (out != null) {
            try {
                writeXML(doc, new StreamResult(new OutputStreamWriter(out, "UTF-8")), null, null);
                out.close();
            } catch (IOException exc) {
                throw new RuntimeException("Unable to write xml", exc);
            }
        }
    }

    public static void printChildElements(Element root, PrintStream out, boolean recurse, String prefix) {
        int i;
        Node node;
        out.print(prefix + "<" + root.getNodeName());
        NamedNodeMap attrs = root.getAttributes();
        for (i = 0; i < attrs.getLength(); i++) {
            node = attrs.item(i);
            out.print(" " + node.getNodeName() + "=\"" + node.getNodeValue() + "\"");
        }
        out.println(">");
        String data = getText(root);
        if (data != null && data.trim().length() > 0) {
            out.println(prefix + "\t" + data);
        }
        data = getCData(root);
        if (data != null && data.trim().length() > 0) {
            out.println(prefix + "\t<![CDATA[" + data + "]]>");
        }
        NodeList nodes = root.getChildNodes();
        for (i = 0; i < nodes.getLength(); i++) {
            node = nodes.item(i);
            if (node.getNodeType() == (short) 1) {
                if (recurse) {
                    printChildElements((Element) node, out, recurse, prefix + "\t");
                } else {
                    out.println(prefix + node.getNodeName());
                }
            }
        }
        out.println(prefix + "</" + root.getNodeName() + ">");
    }

    public static Element findChild(Element parent, String tagName) {
        if (parent == null || tagName == null) {
            throw new NullPointerException("Parent or tagname were null! parent = " + parent + "; tagName = " + tagName);
        }
        NodeList nodes = parent.getChildNodes();
        int len = nodes.getLength();
        for (int i = 0; i < len; i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == (short) 1 && ((Element) node).getNodeName().equals(tagName)) {
                return (Element) node;
            }
        }
        return null;
    }

    public static List<Element> findChildren(Element parent, String tagName) {
        if (parent == null || tagName == null) {
            throw new NullPointerException("Parent or tagname were null! parent = " + parent + "; tagName = " + tagName);
        }
        List<Element> result = new ArrayList();
        NodeList nodes = parent.getChildNodes();
        int len = nodes.getLength();
        for (int i = 0; i < len; i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == (short) 1) {
                Element element = (Element) node;
                if (element.getNodeName().equals(tagName)) {
                    result.add(element);
                }
            }
        }
        return result;
    }

    public static Element locateElement(Element root, String tagName, String keyAttributeName, String keyAttributeValue) {
        NodeList nodes = root.getChildNodes();
        int len = nodes.getLength();
        for (int i = 0; i < len; i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == (short) 1) {
                Element element = (Element) node;
                if (node.getNodeName().equals(tagName)) {
                    String attr = element.getAttribute(keyAttributeName);
                    if (attr != null && attr.equals(keyAttributeValue)) {
                        return element;
                    }
                }
                Element child = locateElement(element, tagName, keyAttributeName, keyAttributeValue);
                if (child != null) {
                    return child;
                }
            }
        }
        return null;
    }

    public static List<Element> locateElements(Element root, String tagName, String keyAttributeName, String keyAttributeValue) {
        List<Element> result = new ArrayList();
        NodeList nodes = root.getChildNodes();
        int len = nodes.getLength();
        for (int i = 0; i < len; i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == (short) 1) {
                if (node.getNodeName().equals(tagName)) {
                    Element element = (Element) node;
                    String attr = element.getAttribute(keyAttributeName);
                    if (attr != null && attr.equals(keyAttributeValue)) {
                        result.add(element);
                    }
                }
                List<Element> childs = locateElements((Element) node, tagName, keyAttributeName, keyAttributeValue);
                if (childs != null) {
                    result.addAll(childs);
                }
            }
        }
        return result;
    }

    public static boolean isStandartXmlNamespace(String namespace) {
        namespace = normalizeNamespace(namespace);
        return normalizeNamespace("http://www.w3.org/XML/1998/namespace").equals(namespace) || normalizeNamespace("http://www.w3.org/2000/xmlns/").equals(namespace) || normalizeNamespace("http://www.w3.org/2001/XMLSchema").equals(namespace) || normalizeNamespace("http://www.w3.org/2001/XMLSchema-instance").equals(namespace);
    }

    public static String getNamespaceUri(Node node) {
        String prefix = node.getPrefix();
        String namespaceUri = node.getNamespaceURI();
        if (!StringUtils.isNullOrEmpty(namespaceUri)) {
            return normalizeNamespace(namespaceUri);
        }
        if ("xmlns".equals(node.getNodeName()) || "xmlns".equals(prefix)) {
            return normalizeNamespace("http://www.w3.org/2000/xmlns/");
        }
        Node rootElement = node.getOwnerDocument().getDocumentElement();
        Node parentNode = null;
        while (parentNode != rootElement) {
            if (parentNode != null) {
                parentNode = parentNode.getParentNode();
            } else if (node.getNodeType() == (short) 2) {
                parentNode = ((Attr) node).getOwnerElement();
                if (StringUtils.isNullOrEmpty(prefix)) {
                    prefix = parentNode.getPrefix();
                }
            } else if (node.getNodeType() != (short) 1) {
                return null;
            } else {
                parentNode = node.getParentNode();
            }
            String parentPrefix = parentNode.getPrefix();
            String parentNamespaceUri = parentNode.getNamespaceURI();
            if (StringUtils.isNullOrEmpty(prefix)) {
                Node xmlnsAttribute = parentNode.getAttributes().getNamedItem("xmlns");
                if (xmlnsAttribute != null) {
                    return ((Attr) xmlnsAttribute).getValue();
                }
            } else if (StringUtils.isEquals(prefix, parentPrefix) && !StringUtils.isNullOrEmpty(parentNamespaceUri)) {
                return normalizeNamespace(parentNamespaceUri);
            }
        }
        return "xml".equals(prefix) ? normalizeNamespace("http://www.w3.org/XML/1998/namespace") : null;
    }

    private static String normalizeNamespace(String namespace) {
        if (namespace.endsWith("/")) {
            return namespace.substring(0, namespace.length() - 1);
        }
        return namespace;
    }

    public static boolean hasChildElements(Element element) {
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == (short) 1) {
                return true;
            }
        }
        return false;
    }

    public static Document createDocument() throws Exception {
        return createDocument(null);
    }

    public static Document createDocument(String xml) throws Exception {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
        if (StringUtils.isNullOrEmpty(xml)) {
            return documentBuilder.newDocument();
        }
        return documentBuilder.parse(StringUtils.fromString(xml));
    }

    public static String createXml(Document document) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter stringWriter = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
        return stringWriter.toString();
    }
}
