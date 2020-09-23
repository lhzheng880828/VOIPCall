package net.java.sip.communicator.impl.protocol.jabber.extensions;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;

public abstract class AbstractPacketExtension implements PacketExtension {
    protected final Map<String, String> attributes = new LinkedHashMap();
    private final List<PacketExtension> childExtensions = new ArrayList();
    private final String elementName;
    private String namespace;
    private final List<Packet> packets = new LinkedList();
    private String textContent;

    protected AbstractPacketExtension(String namespace, String elementName) {
        this.namespace = namespace;
        this.elementName = elementName;
    }

    public String getElementName() {
        return this.elementName;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String toXML() {
        StringBuilder bldr = new StringBuilder();
        bldr.append(Separators.LESS_THAN).append(getElementName()).append(Separators.SP);
        String namespace = getNamespace();
        if (namespace != null) {
            bldr.append("xmlns='").append(namespace).append(Separators.QUOTE);
        }
        for (Entry<String, String> entry : this.attributes.entrySet()) {
            bldr.append(Separators.SP).append((String) entry.getKey()).append("='").append((String) entry.getValue()).append(Separators.QUOTE);
        }
        List<? extends PacketExtension> childElements = getChildExtensions();
        String text = getText();
        List<Packet> packets = getPackets();
        if (childElements != null || packets != null) {
            synchronized (childElements) {
                if (childElements.isEmpty() && packets.isEmpty() && (text == null || text.length() == 0)) {
                    bldr.append("/>");
                    String stringBuilder = bldr.toString();
                    return stringBuilder;
                }
                bldr.append(Separators.GREATER_THAN);
                for (PacketExtension packExt : childElements) {
                    bldr.append(packExt.toXML());
                }
                for (Packet packet : packets) {
                    bldr.append(packet.toXML());
                }
            }
        } else if (text == null || text.length() == 0) {
            bldr.append("/>");
            return bldr.toString();
        } else {
            bldr.append('>');
        }
        if (text != null && text.trim().length() > 0) {
            bldr.append(text);
        }
        bldr.append("</").append(getElementName()).append(Separators.GREATER_THAN);
        return bldr.toString();
    }

    public List<? extends PacketExtension> getChildExtensions() {
        return this.childExtensions;
    }

    public void addChildExtension(PacketExtension childExtension) {
        this.childExtensions.add(childExtension);
    }

    public List<Packet> getPackets() {
        return this.packets;
    }

    public void addPacket(Packet packet) {
        this.packets.add(packet);
    }

    public void setAttribute(String name, Object value) {
        synchronized (this.attributes) {
            if (value != null) {
                this.attributes.put(name, value.toString());
            } else {
                this.attributes.remove(name);
            }
        }
    }

    public void removeAttribute(String name) {
        synchronized (this.attributes) {
            this.attributes.remove(name);
        }
    }

    public Object getAttribute(String attribute) {
        Object obj;
        synchronized (this.attributes) {
            obj = this.attributes.get(attribute);
        }
        return obj;
    }

    public String getAttributeAsString(String attribute) {
        String obj;
        synchronized (this.attributes) {
            Object attributeVal = this.attributes.get(attribute);
            obj = attributeVal == null ? null : attributeVal.toString();
        }
        return obj;
    }

    public int getAttributeAsInt(String attribute) {
        return getAttributeAsInt(attribute, -1);
    }

    public int getAttributeAsInt(String attribute, int defaultValue) {
        synchronized (this.attributes) {
            String value = getAttributeAsString(attribute);
            if (value != null) {
                defaultValue = Integer.parseInt(value);
            }
        }
        return defaultValue;
    }

    public URI getAttributeAsURI(String attribute) throws IllegalArgumentException {
        URI uri;
        synchronized (this.attributes) {
            String attributeVal = getAttributeAsString(attribute);
            if (attributeVal == null) {
                uri = null;
            } else {
                try {
                    uri = new URI(attributeVal);
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }
        return uri;
    }

    public List<String> getAttributeNames() {
        ArrayList arrayList;
        synchronized (this.attributes) {
            arrayList = new ArrayList(this.attributes.keySet());
        }
        return arrayList;
    }

    public void setText(String text) {
        this.textContent = text;
    }

    public String getText() {
        return this.textContent;
    }

    public <T extends PacketExtension> T getFirstChildOfType(Class<T> type) {
        List<? extends PacketExtension> childExtensions = getChildExtensions();
        synchronized (childExtensions) {
            Iterator i$ = childExtensions.iterator();
            while (i$.hasNext()) {
                T extension = (PacketExtension) i$.next();
                if (type.isInstance(extension)) {
                    T extensionAsType = extension;
                    return extensionAsType;
                }
            }
            return null;
        }
    }

    public <T extends PacketExtension> List<T> getChildExtensionsOfType(Class<T> type) {
        List<? extends PacketExtension> childExtensions = getChildExtensions();
        List<T> result = new ArrayList();
        if (childExtensions != null) {
            synchronized (childExtensions) {
                Iterator i$ = childExtensions.iterator();
                while (i$.hasNext()) {
                    T extension = (PacketExtension) i$.next();
                    if (type.isInstance(extension)) {
                        result.add(extension);
                    }
                }
            }
        }
        return result;
    }
}
