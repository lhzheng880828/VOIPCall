package net.java.sip.communicator.impl.protocol.jabber.extensions.whiteboard;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.java.sip.communicator.service.protocol.WhiteboardPoint;
import net.java.sip.communicator.service.protocol.whiteboardobjects.WhiteboardObjectCircle;
import net.java.sip.communicator.util.Logger;
import org.jitsi.android.util.java.awt.Color;
import org.jivesoftware.smack.packet.PrivacyItem.PrivacyRule;
import org.w3c.dom.Element;

public class WhiteboardObjectCircleJabberImpl extends WhiteboardObjectJabberImpl implements WhiteboardObjectCircle {
    private static final Logger logger = Logger.getLogger(WhiteboardObjectCircleJabberImpl.class);
    private int bgColor;
    private boolean fill;
    private double radius;
    private WhiteboardPoint whiteboardPoint;

    public WhiteboardObjectCircleJabberImpl(String xml) {
        try {
            Element e = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes())).getDocumentElement();
            if (e.getNodeName().equals("circle")) {
                String id = e.getAttribute("id");
                double cx = Double.parseDouble(e.getAttribute("cx"));
                double cy = Double.parseDouble(e.getAttribute("cy"));
                double r = Double.parseDouble(e.getAttribute("r"));
                String stroke = e.getAttribute("stroke");
                String stroke_width = e.getAttribute("stroke-width");
                String fill = e.getAttribute("fill");
                setID(id);
                setWhiteboardPoint(new WhiteboardPoint(cx, cy));
                setRadius(r);
                setFill(!fill.equals(PrivacyRule.SUBSCRIPTION_NONE));
                setThickness(Integer.parseInt(stroke_width));
                setColor(Color.decode(stroke).getRGB());
            }
        } catch (ParserConfigurationException e2) {
            if (logger.isDebugEnabled()) {
                logger.debug("Problem WhiteboardObject : " + xml);
            }
        } catch (IOException e3) {
            if (logger.isDebugEnabled()) {
                logger.debug("Problem WhiteboardObject : " + xml);
            }
        } catch (Exception e4) {
            if (logger.isDebugEnabled()) {
                logger.debug("Problem WhiteboardObject : " + xml);
            }
        }
    }

    public WhiteboardPoint getWhiteboardPoint() {
        return this.whiteboardPoint;
    }

    public void setWhiteboardPoint(WhiteboardPoint whiteboardPoint) {
        this.whiteboardPoint = whiteboardPoint;
    }

    public double getRadius() {
        return this.radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public boolean isFill() {
        return this.fill;
    }

    public void setFill(boolean fill) {
        this.fill = fill;
    }

    public void setBackgroundColor(int backColor) {
        this.bgColor = backColor;
    }

    public int getBackgroundColor() {
        return this.bgColor;
    }

    public String toXML() {
        String s = "<circle id=\"#i\" cx=\"#cx\" cy=\"#cy\" r=\"#r\" fill=\"#f\" stroke=\"#s\" stroke-width=\"#ow\" />".replaceAll("#i", getID()).replaceAll("#s", colorToHex(getColor())).replaceAll("#ow", "" + getThickness());
        WhiteboardPoint p = getWhiteboardPoint();
        return s.replaceAll("#cx", "" + p.getX()).replaceAll("#cy", "" + p.getY()).replaceAll("#r", "" + getRadius()).replaceAll("#f", this.fill ? colorToHex(getColor()) : PrivacyRule.SUBSCRIPTION_NONE);
    }
}
