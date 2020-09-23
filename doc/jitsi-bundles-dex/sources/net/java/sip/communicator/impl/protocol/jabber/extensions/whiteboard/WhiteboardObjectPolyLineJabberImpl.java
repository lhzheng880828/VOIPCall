package net.java.sip.communicator.impl.protocol.jabber.extensions.whiteboard;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.java.sip.communicator.service.protocol.WhiteboardPoint;
import net.java.sip.communicator.service.protocol.whiteboardobjects.WhiteboardObjectPolyLine;
import net.java.sip.communicator.util.Logger;
import org.jitsi.android.util.java.awt.Color;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.PrivacyItem.PrivacyRule;
import org.w3c.dom.Element;

public class WhiteboardObjectPolyLineJabberImpl extends WhiteboardObjectJabberImpl implements WhiteboardObjectPolyLine {
    private static final Logger logger = Logger.getLogger(WhiteboardObjectPolyLineJabberImpl.class);
    private List<WhiteboardPoint> listPoints = new LinkedList();

    public WhiteboardObjectPolyLineJabberImpl(String xml) {
        try {
            Element e = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes())).getDocumentElement();
            if (e.getNodeName().equals("polyline")) {
                String id = e.getAttribute("id");
                String d = e.getAttribute("points");
                String stroke = e.getAttribute("stroke");
                String stroke_width = e.getAttribute("stroke-width");
                setID(id);
                setThickness(Integer.parseInt(stroke_width));
                setColor(Color.decode(stroke).getRGB());
                setPoints(getPolyPoints(d));
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

    public void setPoints(List<WhiteboardPoint> points) {
        this.listPoints = new LinkedList(points);
    }

    public List<WhiteboardPoint> getPoints() {
        return this.listPoints;
    }

    private List<WhiteboardPoint> getPolyPoints(String points) {
        List<WhiteboardPoint> list = new LinkedList();
        if (points != null) {
            StringTokenizer tokenizer = new StringTokenizer(points);
            while (tokenizer.hasMoreTokens()) {
                String[] coords = tokenizer.nextToken().split(Separators.COMMA);
                list.add(new WhiteboardPoint(Double.parseDouble(coords[0]), Double.parseDouble(coords[1])));
            }
        }
        return list;
    }

    public String toXML() {
        String s = "<polyline id=\"#i\" points=\"#p\" fill=\"#f\" stroke=\"#s\" stroke-width=\"#w\"/>".replaceAll("#i", getID()).replaceAll("#s", colorToHex(getColor())).replaceAll("#w", "" + getThickness()).replaceAll("#f", PrivacyRule.SUBSCRIPTION_NONE);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.listPoints.size(); i++) {
            WhiteboardPoint point = (WhiteboardPoint) this.listPoints.get(i);
            sb.append(point.getX());
            sb.append(Separators.COMMA);
            sb.append(point.getY());
            sb.append(Separators.SP);
        }
        return s.replaceAll("#p", sb.toString());
    }
}
