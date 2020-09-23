package org.jitsi.impl.configuration.xml;

import org.jitsi.util.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLConfUtils extends XMLUtils {
    public static Element getChildElementByChain(Element parent, String[] chain) {
        if (chain == null) {
            return null;
        }
        Element e = parent;
        for (String findChild : chain) {
            if (e == null) {
                return null;
            }
            e = XMLUtils.findChild(e, findChild);
        }
        return e;
    }

    public static Element createLastPathComponent(Document doc, String[] path, int pathLength) {
        if (doc == null) {
            throw new IllegalArgumentException("doc must not be null");
        } else if (path == null) {
            throw new IllegalArgumentException("path must not be null");
        } else {
            Element parent = (Element) doc.getFirstChild();
            if (parent == null) {
                throw new IllegalArgumentException("parentmust not be null");
            }
            Element e = parent;
            for (int i = 0; i < pathLength; i++) {
                String pathEl = path[i];
                Element newEl = XMLUtils.findChild(e, pathEl);
                if (newEl == null) {
                    newEl = doc.createElement(pathEl);
                    e.appendChild(newEl);
                }
                e = newEl;
            }
            return e;
        }
    }
}
