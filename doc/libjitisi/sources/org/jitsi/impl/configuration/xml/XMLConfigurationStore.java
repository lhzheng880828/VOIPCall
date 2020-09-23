package org.jitsi.impl.configuration.xml;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jitsi.impl.configuration.ConfigurationStore;
import org.jitsi.util.Logger;
import org.jitsi.util.xml.DOMElementWriter;
import org.jitsi.util.xml.XMLException;
import org.jitsi.util.xml.XMLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLConfigurationStore implements ConfigurationStore {
    private static final String ATTRIBUTE_VALUE = "value";
    private static final String SYSTEM_ATTRIBUTE_NAME = "system";
    private static final String SYSTEM_ATTRIBUTE_TRUE = "true";
    private static final Logger logger = Logger.getLogger(XMLConfigurationStore.class);
    private Map<String, Object> fileExtractedProperties = new Hashtable();
    private Hashtable<String, Object> properties = new Hashtable();
    private Document propertiesDocument;

    private static class PropertyReference {
        private final String propertyName;

        public PropertyReference(String propertyName) {
            this.propertyName = propertyName;
        }

        public Object getValue() {
            return System.getProperty(this.propertyName);
        }
    }

    private Map<String, Object> cloneProperties() {
        return (Map) this.properties.clone();
    }

    private Document createPropertiesDocument() {
        if (this.propertiesDocument == null) {
            try {
                this.propertiesDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                this.propertiesDocument.appendChild(this.propertiesDocument.createElement("sip-communicator"));
            } catch (ParserConfigurationException ex) {
                logger.error("Failed to create a DocumentBuilder", ex);
                return null;
            }
        }
        return this.propertiesDocument;
    }

    public Object getProperty(String propertyName) {
        Object obj = this.properties.get(propertyName);
        if (obj instanceof PropertyReference) {
            return ((PropertyReference) obj).getValue();
        }
        return obj;
    }

    public String[] getPropertyNames() {
        Set<String> propertyNames = this.properties.keySet();
        return (String[]) propertyNames.toArray(new String[propertyNames.size()]);
    }

    public boolean isSystemProperty(String propertyName) {
        return this.properties.get(propertyName) instanceof PropertyReference;
    }

    private Map<String, Object> loadConfiguration(File file) throws IOException, XMLException {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Map<String, Object> hashtable = new Hashtable();
            if (file.length() < ((long) ("<sip-communicator>".length() * 2))) {
                this.propertiesDocument = createPropertiesDocument();
            } else {
                this.propertiesDocument = builder.parse(file);
            }
            NodeList children = this.propertiesDocument.getFirstChild().getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node currentNode = children.item(i);
                if (currentNode.getNodeType() == (short) 1) {
                    loadNode(currentNode, DOMElementWriter.decodeName(currentNode.getNodeName()), hashtable);
                }
            }
            return hashtable;
        } catch (SAXException ex) {
            logger.error("Error parsing configuration file", ex);
            throw new XMLException(ex.getMessage(), ex);
        } catch (ParserConfigurationException ex2) {
            logger.error("Error finding configuration for default parsers", ex2);
            return new Hashtable();
        }
    }

    private void loadNode(Node node, String propertyName, Map<String, Object> props) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node currentNode = children.item(i);
            if (currentNode.getNodeType() == (short) 1) {
                String newProp = propertyName + "." + DOMElementWriter.decodeName(currentNode.getNodeName());
                String value = XMLUtils.getAttribute(currentNode, ATTRIBUTE_VALUE);
                if (value != null) {
                    String propertyType = XMLUtils.getAttribute(currentNode, SYSTEM_ATTRIBUTE_NAME);
                    if (propertyType == null || !propertyType.equals(SYSTEM_ATTRIBUTE_TRUE)) {
                        props.put(newProp, value);
                    } else {
                        props.put(newProp, new PropertyReference(newProp));
                        System.setProperty(newProp, value);
                    }
                }
                loadNode(currentNode, newProp, props);
            }
        }
    }

    private void processNewProperties(Document doc, Map<String, Object> newProperties) {
        for (Entry<String, Object> entry : newProperties.entrySet()) {
            Object value = entry.getValue();
            boolean system = value instanceof PropertyReference;
            if (system) {
                value = ((PropertyReference) value).getValue();
            }
            processNewProperty(doc, (String) entry.getKey(), value.toString(), system);
        }
    }

    private void processNewProperty(Document doc, String key, String value, boolean isSystem) {
        StringTokenizer tokenizer = new StringTokenizer(key, ".");
        String[] toks = new String[tokenizer.countTokens()];
        int i = 0;
        while (tokenizer.hasMoreTokens()) {
            int i2 = i + 1;
            toks[i] = DOMElementWriter.encodeName(tokenizer.nextToken());
            i = i2;
        }
        String nodeName = toks[toks.length - 1];
        Element parent = XMLConfUtils.createLastPathComponent(doc, toks, toks.length - 1);
        Element newNode = XMLUtils.findChild(parent, nodeName);
        if (newNode == null) {
            newNode = doc.createElement(nodeName);
            parent.appendChild(newNode);
        }
        newNode.setAttribute(ATTRIBUTE_VALUE, value);
        if (isSystem) {
            newNode.setAttribute(SYSTEM_ATTRIBUTE_NAME, SYSTEM_ATTRIBUTE_TRUE);
        }
    }

    public void reloadConfiguration(File file) throws IOException, XMLException {
        this.properties = new Hashtable();
        this.fileExtractedProperties = loadConfiguration(file);
        this.properties.putAll(this.fileExtractedProperties);
    }

    public void removeProperty(String propertyName) {
        this.properties.remove(propertyName);
        this.fileExtractedProperties.remove(propertyName);
    }

    public void setNonSystemProperty(String propertyName, Object property) {
        this.properties.put(propertyName, property);
    }

    public void setSystemProperty(String propertyName) {
        setNonSystemProperty(propertyName, new PropertyReference(propertyName));
    }

    public void storeConfiguration(OutputStream out) {
        if (this.propertiesDocument == null) {
            this.propertiesDocument = createPropertiesDocument();
        }
        NodeList children = this.propertiesDocument.getFirstChild().getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node currentNode = children.item(i);
            if (currentNode.getNodeType() == (short) 1) {
                updateNode(currentNode, DOMElementWriter.decodeName(currentNode.getNodeName()), this.properties);
            }
        }
        Map<String, Object> newlyAddedProperties = cloneProperties();
        for (String propName : this.fileExtractedProperties.keySet()) {
            newlyAddedProperties.remove(propName);
        }
        processNewProperties(this.propertiesDocument, newlyAddedProperties);
        XMLUtils.indentedWriteXML(this.propertiesDocument, out);
    }

    private void updateNode(Node node, String propertyName, Map<String, Object> props) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node currentNode = children.item(i);
            if (currentNode.getNodeType() == (short) 1) {
                String newProp = propertyName + "." + DOMElementWriter.decodeName(currentNode.getNodeName());
                Attr attr = ((Element) currentNode).getAttributeNode(ATTRIBUTE_VALUE);
                if (attr != null) {
                    Object value = props.get(newProp);
                    if (value == null) {
                        node.removeChild(currentNode);
                    } else {
                        boolean isSystem = value instanceof PropertyReference;
                        attr.setNodeValue(isSystem ? ((PropertyReference) value).getValue().toString() : value.toString());
                        if (isSystem) {
                            ((Element) currentNode).setAttribute(SYSTEM_ATTRIBUTE_NAME, SYSTEM_ATTRIBUTE_TRUE);
                        } else {
                            ((Element) currentNode).removeAttribute(SYSTEM_ATTRIBUTE_NAME);
                        }
                    }
                }
                updateNode(currentNode, newProp, props);
            }
        }
    }
}
