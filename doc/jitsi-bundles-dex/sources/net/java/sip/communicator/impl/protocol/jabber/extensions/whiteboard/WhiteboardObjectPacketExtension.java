package net.java.sip.communicator.impl.protocol.jabber.extensions.whiteboard;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.PacketExtension;
import org.w3c.dom.Element;

public class WhiteboardObjectPacketExtension implements PacketExtension {
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_DRAW = "DRAW";
    public static final String ACTION_MOVE = "MOVE";
    public static final String ELEMENT_NAME = "xObject";
    public static final String NAMESPACE = "http://jabber.org/protocol/swb";
    private static final Logger logger = Logger.getLogger(WhiteboardObjectPacketExtension.class);
    private String action;
    private WhiteboardObjectJabberImpl whiteboardObject;
    private String whiteboardObjectID;

    public WhiteboardObjectPacketExtension() {
        this.action = ACTION_DRAW;
    }

    public WhiteboardObjectPacketExtension(String id, String action) {
        this.whiteboardObjectID = id;
        this.action = action;
    }

    public WhiteboardObjectPacketExtension(WhiteboardObjectJabberImpl whiteboardObject, String action) {
        this.whiteboardObject = whiteboardObject;
        this.action = action;
    }

    public WhiteboardObjectPacketExtension(String xml) {
        try {
            Element e = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes())).getDocumentElement();
            String elementName = e.getNodeName();
            this.action = ACTION_DRAW;
            if (elementName.equals("rect")) {
                this.whiteboardObject = new WhiteboardObjectRectJabberImpl(xml);
            } else if (elementName.equals("circle")) {
                this.whiteboardObject = new WhiteboardObjectCircleJabberImpl(xml);
            } else if (elementName.equals("path")) {
                this.whiteboardObject = new WhiteboardObjectPathJabberImpl(xml);
            } else if (elementName.equals("polyline")) {
                this.whiteboardObject = new WhiteboardObjectPolyLineJabberImpl(xml);
            } else if (elementName.equals("polygon")) {
                this.whiteboardObject = new WhiteboardObjectPolygonJabberImpl(xml);
            } else if (elementName.equals("line")) {
                this.whiteboardObject = new WhiteboardObjectLineJabberImpl(xml);
            } else if (elementName.equals("text")) {
                this.whiteboardObject = new WhiteboardObjectTextJabberImpl(xml);
            } else if (elementName.equals("image")) {
                this.whiteboardObject = new WhiteboardObjectImageJabberImpl(xml);
            } else if (elementName.equals("delete")) {
                setWhiteboardObjectID(e.getAttribute("id"));
                this.action = ACTION_DELETE;
            } else if (logger.isDebugEnabled()) {
                logger.debug("elementName unknow\n");
            }
        } catch (ParserConfigurationException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Problem WhiteboardObject : " + xml, ex);
            }
        } catch (IOException ex2) {
            if (logger.isDebugEnabled()) {
                logger.debug("Problem WhiteboardObject : " + xml, ex2);
            }
        } catch (Exception ex3) {
            if (logger.isDebugEnabled()) {
                logger.debug("Problem WhiteboardObject : " + xml, ex3);
            }
        }
    }

    public String getElementName() {
        return ELEMENT_NAME;
    }

    public String getNamespace() {
        return "http://jabber.org/protocol/swb";
    }

    public String toXML() {
        String s = "";
        if (getAction().equals(ACTION_DELETE)) {
            s = "<delete id=\"#i\"/>".replaceAll("#i", getWhiteboardObjectID());
        } else {
            s = getWhiteboardObject().toXML();
        }
        return "<xObject xmlns=\"http://jabber.org/protocol/swb\">" + s + "</" + ELEMENT_NAME + Separators.GREATER_THAN;
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public WhiteboardObjectJabberImpl getWhiteboardObject() {
        return this.whiteboardObject;
    }

    public String getWhiteboardObjectID() {
        return this.whiteboardObjectID;
    }

    public void setWhiteboardObjectID(String objectID) {
        this.whiteboardObjectID = objectID;
    }
}
