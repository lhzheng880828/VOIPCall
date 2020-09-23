package net.java.sip.communicator.impl.protocol.jabber.extensions.whiteboard;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.java.sip.communicator.impl.protocol.jabber.extensions.thumbnail.ThumbnailElement;
import net.java.sip.communicator.service.protocol.WhiteboardPoint;
import net.java.sip.communicator.service.protocol.whiteboardobjects.WhiteboardObjectImage;
import net.java.sip.communicator.util.Base64;
import net.java.sip.communicator.util.Logger;
import org.w3c.dom.Element;

public class WhiteboardObjectImageJabberImpl extends WhiteboardObjectJabberImpl implements WhiteboardObjectImage {
    private static final Logger logger = Logger.getLogger(WhiteboardObjectImageJabberImpl.class);
    private byte[] background;
    private double height;
    private WhiteboardPoint whiteboardPoint;
    private double width;

    public WhiteboardObjectImageJabberImpl(String xml) {
        try {
            Element e = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes())).getDocumentElement();
            if (e.getNodeName().equals("image")) {
                String id = e.getAttribute("id");
                double x = Double.parseDouble(e.getAttribute("x"));
                double y = Double.parseDouble(e.getAttribute("y"));
                double width = Double.parseDouble(e.getAttribute(ThumbnailElement.WIDTH));
                double height = Double.parseDouble(e.getAttribute(ThumbnailElement.HEIGHT));
                String img = e.getTextContent();
                setID(id);
                setWhiteboardPoint(new WhiteboardPoint(x, y));
                setWidth(width);
                setHeight(height);
                setBackgroundImage(Base64.decode(img));
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

    public void setBackgroundImage(byte[] background) {
        this.background = background;
    }

    public byte[] getBackgroundImage() {
        return this.background;
    }

    public String toXML() {
        String s = "<image id=\"#id\" x=\"#x\" y=\"#y\" width=\"#w\" height=\"#h\">#img</image>".replaceAll("#id", getID());
        WhiteboardPoint p = getWhiteboardPoint();
        return s.replaceAll("#x", "" + p.getX()).replaceAll("#y", "" + p.getY()).replaceAll("#w", "" + getWidth()).replaceAll("#h", "" + getHeight()).replaceAll("#img", new String(Base64.encode(getBackgroundImage())));
    }
}
