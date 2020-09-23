package net.java.sip.communicator.impl.protocol.jabber.extensions.whiteboard;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.java.sip.communicator.service.protocol.WhiteboardPoint;
import net.java.sip.communicator.service.protocol.whiteboardobjects.WhiteboardObjectPath;
import net.java.sip.communicator.util.Logger;
import org.jitsi.android.util.java.awt.Color;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.parser.TokenNames;
import org.w3c.dom.Element;

public class WhiteboardObjectPathJabberImpl extends WhiteboardObjectJabberImpl implements WhiteboardObjectPath {
    private static final Logger logger = Logger.getLogger(WhiteboardObjectPathJabberImpl.class);
    private List<WhiteboardPoint> listPoints = new LinkedList();

    public WhiteboardObjectPathJabberImpl(String xml) {
        try {
            Element e = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes())).getDocumentElement();
            if (e.getNodeName().equals("path")) {
                String id = e.getAttribute("id");
                String d = e.getAttribute("d");
                String stroke = e.getAttribute("stroke");
                String stroke_width = e.getAttribute("stroke-width");
                setID(id);
                setThickness(Integer.parseInt(stroke_width));
                setColor(Color.decode(stroke).getRGB());
                setPoints(getPathPoints(d));
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

    private List<WhiteboardPoint> getPathPoints(String points) {
        List<WhiteboardPoint> list = new LinkedList();
        if (points != null) {
            Matcher matcher = Pattern.compile("[ML]\\S+ \\S+ ").matcher(points);
            while (matcher.find()) {
                String[] coords = matcher.group(0).substring(1).split(Separators.SP);
                list.add(new WhiteboardPoint(Double.parseDouble(coords[0]), Double.parseDouble(coords[1])));
            }
        }
        return list;
    }

    public String toXML() {
        String s = "<path id=\"#i\" d=\"#p Z\" stroke=\"#s\" stroke-width=\"#w\"/>".replaceAll("#i", getID()).replaceAll("#s", colorToHex(getColor())).replaceAll("#w", "" + getThickness());
        StringBuilder sb = new StringBuilder();
        int size = this.listPoints.size();
        int i = 0;
        while (i < size) {
            WhiteboardPoint point = (WhiteboardPoint) this.listPoints.get(i);
            sb.append(i == 0 ? TokenNames.M : TokenNames.L);
            sb.append(point.getX());
            sb.append(Separators.SP);
            sb.append(point.getY());
            sb.append(Separators.SP);
            i++;
        }
        return s.replaceAll("#p", sb.toString());
    }
}
