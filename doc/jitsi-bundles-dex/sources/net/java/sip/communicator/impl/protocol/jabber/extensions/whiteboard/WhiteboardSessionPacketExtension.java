package net.java.sip.communicator.impl.protocol.jabber.extensions.whiteboard;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.java.sip.communicator.impl.protocol.jabber.WhiteboardSessionJabberImpl;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.PacketExtension;
import org.w3c.dom.Element;

public class WhiteboardSessionPacketExtension implements PacketExtension {
    public static final String ACTION_LEAVE = "LEAVE";
    public static final String ELEMENT_NAME = "xSession";
    public static final String NAMESPACE = "http://jabber.org/protocol/swb";
    private String action;
    private String contactAddress;
    private Logger logger = Logger.getLogger(WhiteboardSessionPacketExtension.class);
    private WhiteboardSessionJabberImpl whiteboardSession;
    private String whiteboardSessionId;

    public WhiteboardSessionPacketExtension(WhiteboardSessionJabberImpl session, String contactAddress, String action) {
        this.whiteboardSession = session;
        this.whiteboardSessionId = session.getWhiteboardID();
        this.contactAddress = contactAddress;
        this.action = action;
    }

    public WhiteboardSessionPacketExtension(String xml) {
        try {
            Element e = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes())).getDocumentElement();
            if (e.getNodeName().equals(ACTION_LEAVE)) {
                setWhiteboardSessionId(e.getAttribute("id"));
                setContactAddress(e.getAttribute("userId"));
                this.action = ACTION_LEAVE;
            } else if (this.logger.isDebugEnabled()) {
                this.logger.debug("Element name unknown!");
            }
        } catch (ParserConfigurationException ex) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Problem WhiteboardSession : " + xml, ex);
            }
        } catch (IOException ex2) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Problem WhiteboardSession : " + xml, ex2);
            }
        } catch (Exception ex3) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Problem WhiteboardSession : " + xml, ex3);
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
        if (this.action.equals(ACTION_LEAVE)) {
            s = "<LEAVE id=\"#sessionId\" userId=\"#userId\"/>".replaceAll("#sessionId", this.whiteboardSession.getWhiteboardID()).replaceAll("#userId", this.contactAddress);
        }
        return "<xSession xmlns=\"http://jabber.org/protocol/swb\">" + s + "</" + ELEMENT_NAME + Separators.GREATER_THAN;
    }

    public String getWhiteboardSessionId() {
        return this.whiteboardSessionId;
    }

    public void setWhiteboardSessionId(String whiteboardSessionId) {
        this.whiteboardSessionId = whiteboardSessionId;
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getContactAddress() {
        return this.contactAddress;
    }

    public void setContactAddress(String contactAddress) {
        this.contactAddress = contactAddress;
    }
}
