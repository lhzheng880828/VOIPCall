package net.java.sip.communicator.impl.protocol.jabber.extensions.whiteboard;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.java.sip.communicator.impl.protocol.jabber.extensions.thumbnail.ThumbnailElement;
import net.java.sip.communicator.service.protocol.WhiteboardPoint;
import net.java.sip.communicator.service.protocol.whiteboardobjects.WhiteboardObjectRect;
import net.java.sip.communicator.util.Logger;
import org.jitsi.android.util.java.awt.Color;
import org.jivesoftware.smack.packet.PrivacyItem.PrivacyRule;
import org.w3c.dom.Element;

public class WhiteboardObjectRectJabberImpl extends WhiteboardObjectJabberImpl implements WhiteboardObjectRect {
    private static final Logger logger = Logger.getLogger(WhiteboardObjectRectJabberImpl.class);
    private int backColor;
    private boolean fill;
    private double height;
    private WhiteboardPoint whiteboardPoint;
    private double width;

    public WhiteboardObjectRectJabberImpl(String id, int thickness, int color, int backColor, WhiteboardPoint whiteboardPoint, double width, double height, boolean fill) {
        super(id, thickness, color);
        setBackgroundColor(backColor);
        setWhiteboardPoint(whiteboardPoint);
        setWidth(width);
        setHeight(height);
        setFill(fill);
    }

    public WhiteboardObjectRectJabberImpl(String xml) {
        try {
            Element e = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes())).getDocumentElement();
            if (e.getNodeName().equals("rect")) {
                String id = e.getAttribute("id");
                double x = Double.parseDouble(e.getAttribute("x"));
                double y = Double.parseDouble(e.getAttribute("y"));
                double width = Double.parseDouble(e.getAttribute(ThumbnailElement.WIDTH));
                double height = Double.parseDouble(e.getAttribute(ThumbnailElement.HEIGHT));
                String stroke = e.getAttribute("stroke");
                String stroke_width = e.getAttribute("stroke-width");
                String fill = e.getAttribute("fill");
                setID(id);
                setWhiteboardPoint(new WhiteboardPoint(x, y));
                setWidth(width);
                setHeight(height);
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

    public double getHeight() {
        return this.height;
    }

    public double getWidth() {
        return this.width;
    }

    public boolean isFill() {
        return this.fill;
    }

    public void setFill(boolean fill) {
        this.fill = fill;
    }

    public WhiteboardPoint getWhiteboardPoint() {
        return this.whiteboardPoint;
    }

    public void setWhiteboardPoint(WhiteboardPoint whiteboardPoint) {
        this.whiteboardPoint = whiteboardPoint;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setBackgroundColor(int backColor) {
        this.backColor = backColor;
    }

    public int getBackgroundColor() {
        return this.backColor;
    }

    public String toXML() {
        String s = "<rect id=\"#i\" x=\"#x\" y=\"#y\" width=\"#w\" height=\"#h\" fill=\"#f\" stroke=\"#s\" stroke-width=\"#ow\"/>".replaceAll("#i", getID()).replaceAll("#s", colorToHex(getColor())).replaceAll("#ow", "" + getThickness());
        WhiteboardPoint p = getWhiteboardPoint();
        return s.replaceAll("#x", "" + p.getX()).replaceAll("#y", "" + p.getY()).replaceAll("#w", "" + getWidth()).replaceAll("#h", "" + getHeight()).replaceAll("#f", isFill() ? "" + getColor() : PrivacyRule.SUBSCRIPTION_NONE);
    }
}
