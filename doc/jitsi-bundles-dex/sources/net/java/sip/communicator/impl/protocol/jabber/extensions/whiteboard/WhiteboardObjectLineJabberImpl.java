package net.java.sip.communicator.impl.protocol.jabber.extensions.whiteboard;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.java.sip.communicator.service.protocol.WhiteboardPoint;
import net.java.sip.communicator.service.protocol.whiteboardobjects.WhiteboardObjectLine;
import net.java.sip.communicator.util.Logger;
import org.jitsi.android.util.java.awt.Color;
import org.w3c.dom.Element;

public class WhiteboardObjectLineJabberImpl extends WhiteboardObjectJabberImpl implements WhiteboardObjectLine {
    private static final Logger logger = Logger.getLogger(WhiteboardObjectLineJabberImpl.class);
    private WhiteboardPoint whiteboardPointEnd = new WhiteboardPoint(0.0d, 0.0d);
    private WhiteboardPoint whiteboardPointStart = new WhiteboardPoint(0.0d, 0.0d);

    public WhiteboardObjectLineJabberImpl(String xml) {
        try {
            Element e = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes())).getDocumentElement();
            if (e.getNodeName().equals("line")) {
                String id = e.getAttribute("id");
                double x1 = Double.parseDouble(e.getAttribute("x1"));
                double y1 = Double.parseDouble(e.getAttribute("y1"));
                double x2 = Double.parseDouble(e.getAttribute("x2"));
                double y2 = Double.parseDouble(e.getAttribute("y2"));
                String stroke = e.getAttribute("stroke");
                String stroke_width = e.getAttribute("stroke-width");
                setID(id);
                setThickness(Integer.parseInt(stroke_width));
                setColor(Color.decode(stroke).getRGB());
                setWhiteboardPointStart(new WhiteboardPoint(x1, y1));
                setWhiteboardPointEnd(new WhiteboardPoint(x2, y2));
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

    public WhiteboardPoint getWhiteboardPointStart() {
        return this.whiteboardPointStart;
    }

    public WhiteboardPoint getWhiteboardPointEnd() {
        return this.whiteboardPointEnd;
    }

    public void setWhiteboardPointStart(WhiteboardPoint whiteboardPointStart) {
        this.whiteboardPointStart = whiteboardPointStart;
    }

    public void setWhiteboardPointEnd(WhiteboardPoint whiteboardPointEnd) {
        this.whiteboardPointEnd = whiteboardPointEnd;
    }

    public String toXML() {
        String s = "<line id=\"#i\" x1=\"#x1\" y1=\"#y1\" x2=\"#x2\" y2=\"#y2\" stroke=\"#s\" stroke-width=\"#w\"/> ".replaceAll("#i", getID()).replaceAll("#s", colorToHex(getColor())).replaceAll("#w", "" + getThickness());
        WhiteboardPoint p1 = getWhiteboardPointStart();
        WhiteboardPoint p2 = getWhiteboardPointEnd();
        return s.replaceAll("#x1", "" + p1.getX()).replaceAll("#y1", "" + p1.getY()).replaceAll("#x2", "" + p2.getX()).replaceAll("#y2", "" + p2.getY());
    }
}
