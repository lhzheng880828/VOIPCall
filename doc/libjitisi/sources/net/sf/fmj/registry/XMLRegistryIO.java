package net.sf.fmj.registry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;
import javax.media.CaptureDeviceInfo;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.sf.fmj.utility.LoggerSingleton;
import net.sf.fmj.utility.SerializationUtils;
import org.jitsi.impl.neomedia.format.MediaFormatImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class XMLRegistryIO implements RegistryIO {
    private static final String ATTR_VERSION = "version";
    private static final String ELEMENT_CAPTURE_DEVICES = "capture-devices";
    private static final String ELEMENT_CLASS = "class";
    private static final String ELEMENT_CODECS = "codecs";
    private static final String ELEMENT_CONTENT_PREFIX = "content-prefixes";
    private static final String ELEMENT_DEMUXES = "demuxes";
    private static final String ELEMENT_DEVICE = "device";
    private static final String ELEMENT_DEVICE_FORMAT = "format";
    private static final String ELEMENT_DEVICE_FORMAT_CLASS = "class";
    private static final String ELEMENT_DEVICE_FORMAT_DESCRIPTION = "description";
    private static final String ELEMENT_DEVICE_FORMAT_SERIALIZED = "serialized";
    private static final String ELEMENT_DEVICE_LOCATOR = "locator";
    private static final String ELEMENT_DEVICE_NAME = "name";
    private static final String ELEMENT_EFFECTS = "effects";
    private static final String ELEMENT_MIMETYPE = "type";
    private static final String ELEMENT_MIMETYPES = "mime-types";
    private static final String ELEMENT_MUXES = "muxes";
    private static final String ELEMENT_PLUGINS = "plugins";
    private static final String ELEMENT_PREFIX = "prefix";
    private static final String ELEMENT_PROTO_PREFIX = "protocol-prefixes";
    private static final String ELEMENT_REGISTRY = "registry";
    private static final String ELEMENT_RENDERERS = "renderers";
    private static final Logger logger = LoggerSingleton.logger;
    private static final String version = "0.1";
    private final RegistryContents registryContents;

    public XMLRegistryIO(RegistryContents registryContents) {
        this.registryContents = registryContents;
    }

    private Document buildDocument() throws IOException {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element rootElement = document.createElement(ELEMENT_REGISTRY);
            rootElement.setAttribute(ATTR_VERSION, version);
            document.appendChild(rootElement);
            rootElement.appendChild(getPluginsElement(document));
            rootElement.appendChild(getContentElement(document));
            rootElement.appendChild(getProtocolElement(document));
            rootElement.appendChild(getMimeElement(document));
            rootElement.appendChild(getCaptureDeviceElement(document));
            return document;
        } catch (ParserConfigurationException pce) {
            IOException ioe = new IOException();
            ioe.initCause(pce);
            throw ioe;
        }
    }

    private Element getCaptureDeviceElement(Document document) throws IOException {
        Element captureDeviceElement = document.createElement(ELEMENT_CAPTURE_DEVICES);
        Iterator<CaptureDeviceInfo> iter = this.registryContents.captureDeviceInfoList.iterator();
        while (iter.hasNext()) {
            CaptureDeviceInfo info = (CaptureDeviceInfo) iter.next();
            if (info.getLocator() != null) {
                Element deviceElement = document.createElement(ELEMENT_DEVICE);
                Element deviceNameElement = document.createElement(ELEMENT_DEVICE_NAME);
                deviceNameElement.setTextContent(info.getName());
                deviceElement.appendChild(deviceNameElement);
                Element e = document.createElement(ELEMENT_DEVICE_LOCATOR);
                e.setTextContent(info.getLocator().toExternalForm());
                deviceElement.appendChild(e);
                Format[] formats = info.getFormats();
                for (int i = 0; i < formats.length; i++) {
                    Element formatElement = document.createElement(ELEMENT_DEVICE_FORMAT);
                    Element e2 = document.createElement("class");
                    e2.setTextContent(formats[i].getClass().getName());
                    formatElement.appendChild(e2);
                    e2 = document.createElement(ELEMENT_DEVICE_FORMAT_DESCRIPTION);
                    e2.setTextContent(formats[i].toString());
                    formatElement.appendChild(e2);
                    e2 = document.createElement(ELEMENT_DEVICE_FORMAT_SERIALIZED);
                    e2.setTextContent(SerializationUtils.serialize(formats[i]));
                    formatElement.appendChild(e2);
                    deviceElement.appendChild(formatElement);
                }
                captureDeviceElement.appendChild(deviceElement);
            }
        }
        return captureDeviceElement;
    }

    private Element getChild(Element element, String name) {
        NodeList childNodes = element.getChildNodes();
        int childNodeCount = childNodes.getLength();
        for (int i = 0; i < childNodeCount; i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == (short) 1 && childNode.getNodeName().equals(name)) {
                return (Element) childNode;
            }
        }
        return null;
    }

    private List<Element> getChildren(Element element, String name) {
        NodeList childNodes = element.getChildNodes();
        int childNodeCount = childNodes.getLength();
        List<Element> children = new ArrayList(childNodeCount);
        for (int i = 0; i < childNodeCount; i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == (short) 1 && childNode.getNodeName().equals(name)) {
                children.add((Element) childNode);
            }
        }
        return children;
    }

    private Element getCodecElement(Document document) {
        return getPluginElement(2, ELEMENT_CODECS, document);
    }

    private Element getContentElement(Document document) {
        Element contentElement = document.createElement(ELEMENT_CONTENT_PREFIX);
        Iterator prefixIter = this.registryContents.contentPrefixList.iterator();
        while (prefixIter.hasNext()) {
            String prefix = (String) prefixIter.next();
            Element prefixElement = document.createElement(ELEMENT_PREFIX);
            prefixElement.setTextContent(prefix);
            contentElement.appendChild(prefixElement);
        }
        return contentElement;
    }

    private Element getDemuxElement(Document document) {
        return getPluginElement(1, ELEMENT_DEMUXES, document);
    }

    private Element getEffectElement(Document document) {
        return getPluginElement(3, ELEMENT_EFFECTS, document);
    }

    private Element getMimeElement(Document document) {
        Element mimeElement = document.createElement(ELEMENT_MIMETYPES);
        for (String type : this.registryContents.mimeTable.getMimeTypes()) {
            List extensions = this.registryContents.mimeTable.getExtensions(type);
            Element typeElement = document.createElement(ELEMENT_MIMETYPE);
            typeElement.setAttribute("value", type);
            typeElement.setAttribute("default-ext", this.registryContents.mimeTable.getDefaultExtension(type));
            mimeElement.appendChild(typeElement);
            for (int i = 0; i < extensions.size(); i++) {
                String ext = (String) extensions.get(i);
                Element extElement = document.createElement("ext");
                extElement.setTextContent(ext);
                typeElement.appendChild(extElement);
            }
        }
        return mimeElement;
    }

    private Element getMuxElement(Document document) {
        return getPluginElement(5, ELEMENT_MUXES, document);
    }

    private Element getPluginElement(int pluginType, String typeName, Document document) {
        Element pluginsElement = document.createElement(typeName);
        Vector plugins = this.registryContents.plugins[pluginType - 1];
        if (plugins != null) {
            Iterator pluginIter = plugins.iterator();
            while (pluginIter.hasNext()) {
                String classname = (String) pluginIter.next();
                Element pluginElement = document.createElement("class");
                pluginElement.setTextContent(classname);
                pluginsElement.appendChild(pluginElement);
            }
        }
        return pluginsElement;
    }

    private Element getPluginsElement(Document document) {
        Element pluginElement = document.createElement(ELEMENT_PLUGINS);
        pluginElement.appendChild(getCodecElement(document));
        pluginElement.appendChild(getDemuxElement(document));
        pluginElement.appendChild(getEffectElement(document));
        pluginElement.appendChild(getMuxElement(document));
        pluginElement.appendChild(getRendererElement(document));
        return pluginElement;
    }

    private Element getProtocolElement(Document document) {
        Element protocolElement = document.createElement(ELEMENT_PROTO_PREFIX);
        Iterator prefixIter = this.registryContents.protocolPrefixList.iterator();
        while (prefixIter.hasNext()) {
            String prefix = (String) prefixIter.next();
            Element prefixElement = document.createElement(ELEMENT_PREFIX);
            prefixElement.setTextContent(prefix);
            protocolElement.appendChild(prefixElement);
        }
        return protocolElement;
    }

    private Element getRendererElement(Document document) {
        return getPluginElement(4, ELEMENT_RENDERERS, document);
    }

    private String getTextTrim(Element element) {
        String text = element.getTextContent();
        return text == null ? null : text.trim();
    }

    public void load(InputStream is) throws IOException {
        Throwable t = null;
        try {
            loadDocument(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is));
        } catch (ParserConfigurationException pce) {
            t = pce;
        } catch (SAXException saxe) {
            t = saxe;
        }
        if (t != null) {
            IOException ioe = new IOException();
            ioe.initCause(t);
            throw ioe;
        }
    }

    private void loadCaptureDevices(Element element) throws IOException, ClassNotFoundException {
        this.registryContents.captureDeviceInfoList.clear();
        List list = getChildren(element, ELEMENT_DEVICE);
        for (int i = 0; i < list.size(); i++) {
            Element deviceElement = (Element) list.get(i);
            Element deviceNameElement = getChild(deviceElement, ELEMENT_DEVICE_NAME);
            Element deviceLocatorElement = getChild(deviceElement, ELEMENT_DEVICE_LOCATOR);
            List formatElementsList = getChildren(deviceElement, ELEMENT_DEVICE_FORMAT);
            Format[] formats = new Format[formatElementsList.size()];
            for (int j = 0; j < formatElementsList.size(); j++) {
                formats[j] = SerializationUtils.deserialize(getTextTrim(getChild((Element) formatElementsList.get(j), ELEMENT_DEVICE_FORMAT_SERIALIZED)));
            }
            this.registryContents.captureDeviceInfoList.add(new CaptureDeviceInfo(getTextTrim(deviceNameElement), new MediaLocator(getTextTrim(deviceLocatorElement)), formats));
        }
    }

    private void loadContentPrefixes(Element element) {
        this.registryContents.contentPrefixList.clear();
        List list = getChildren(element, ELEMENT_PREFIX);
        for (int i = 0; i < list.size(); i++) {
            this.registryContents.contentPrefixList.add(getTextTrim((Element) list.get(i)));
        }
    }

    private void loadDocument(Document document) throws IOException {
        Element rootElement = (Element) document.getFirstChild();
        logger.info("FMJ registry document version " + rootElement.getAttribute(ATTR_VERSION));
        loadPlugins(getChild(rootElement, ELEMENT_PLUGINS));
        loadContentPrefixes(getChild(rootElement, ELEMENT_CONTENT_PREFIX));
        loadProtocolPrefixes(getChild(rootElement, ELEMENT_PROTO_PREFIX));
        loadMimeTypes(getChild(rootElement, ELEMENT_MIMETYPES));
        try {
            loadCaptureDevices(getChild(rootElement, ELEMENT_CAPTURE_DEVICES));
        } catch (ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }

    private void loadMimeTypes(Element element) {
        this.registryContents.mimeTable.clear();
        List list = getChildren(element, ELEMENT_MIMETYPE);
        for (int i = 0; i < list.size(); i++) {
            Element typeElement = (Element) list.get(i);
            String type = typeElement.getAttribute("value");
            String defaultExtension = typeElement.getAttribute("default-ext");
            List list2 = getChildren(typeElement, "ext");
            for (int j = 0; j < list2.size(); j++) {
                this.registryContents.mimeTable.addMimeType(((Element) list2.get(j)).getTextContent(), type);
            }
            this.registryContents.mimeTable.addMimeType(defaultExtension, type);
        }
    }

    private void loadPlugins(Element element) {
        loadPlugins(getChild(element, ELEMENT_CODECS), 2);
        loadPlugins(getChild(element, ELEMENT_EFFECTS), 3);
        loadPlugins(getChild(element, ELEMENT_RENDERERS), 4);
        loadPlugins(getChild(element, ELEMENT_MUXES), 5);
        loadPlugins(getChild(element, ELEMENT_DEMUXES), 1);
    }

    private void loadPlugins(Element element, int type) {
        if (element != null) {
            List<String> vector = this.registryContents.plugins[type - 1];
            for (Element pluginElement : getChildren(element, "class")) {
                vector.add(getTextTrim(pluginElement));
            }
        }
    }

    private void loadProtocolPrefixes(Element element) {
        this.registryContents.protocolPrefixList.clear();
        for (Element prefixElement : getChildren(element, ELEMENT_PREFIX)) {
            this.registryContents.protocolPrefixList.add(getTextTrim(prefixElement));
        }
    }

    public void write(OutputStream os) throws IOException {
        IOException ioe;
        DOMSource domSource = new DOMSource(buildDocument());
        TransformerFactory tf = TransformerFactory.newInstance();
        try {
            tf.setAttribute("indent-number", Integer.valueOf(4));
        } catch (Exception e) {
        }
        try {
            Transformer serializer = tf.newTransformer();
            try {
                serializer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "4");
            } catch (Exception e2) {
            }
            serializer.setOutputProperty(MediaFormatImpl.ENCODING_PNAME, "UTF-8");
            serializer.setOutputProperty("indent", "yes");
            try {
                serializer.transform(domSource, new StreamResult(new OutputStreamWriter(os, "UTF-8")));
            } catch (TransformerException te) {
                ioe = new IOException();
                ioe.initCause(te);
                throw ioe;
            }
        } catch (TransformerConfigurationException tce) {
            ioe = new IOException();
            ioe.initCause(tce);
            throw ioe;
        }
    }
}
