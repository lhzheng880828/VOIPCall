package net.java.sip.communicator.impl.protocol.jabber.extensions.whiteboard;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.java.sip.communicator.service.protocol.WhiteboardPoint;
import net.java.sip.communicator.service.protocol.whiteboardobjects.WhiteboardObjectText;
import net.java.sip.communicator.util.Logger;
import org.jitsi.android.util.java.awt.Color;
import org.w3c.dom.Element;

public class WhiteboardObjectTextJabberImpl extends WhiteboardObjectJabberImpl implements WhiteboardObjectText {
    private static final Logger logger = Logger.getLogger(WhiteboardObjectTextJabberImpl.class);
    private String fontName = "Dialog";
    private int fontSize = 0;
    private String text = "";
    private WhiteboardPoint whiteboardPoint;

    public WhiteboardObjectTextJabberImpl() {
        setWhiteboardPoint(new WhiteboardPoint(0.0d, 0.0d));
        setFontName(this.fontName);
        setFontSize(this.fontSize);
        setText(this.text);
    }

    public WhiteboardObjectTextJabberImpl(String xml) {
        try {
            Element e = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes())).getDocumentElement();
            if (e.getNodeName().equals("text")) {
                String id = e.getAttribute("id");
                double x = Double.parseDouble(e.getAttribute("x"));
                double y = Double.parseDouble(e.getAttribute("y"));
                String fill = e.getAttribute("fill");
                String fontFamily = e.getAttribute("font-family");
                int fontSize = Integer.parseInt(e.getAttribute("font-size"));
                String text = e.getTextContent();
                setID(id);
                setWhiteboardPoint(new WhiteboardPoint(x, y));
                setFontName(fontFamily);
                setFontSize(fontSize);
                setText(text);
                setColor(Color.decode(fill).getRGB());
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

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getFontSize() {
        return this.fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public String getFontName() {
        return this.fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public String toXML() {
        String s = "<text id=\"#i\" x=\"#x\" y=\"#y\" fill=\"#fi\" font-family=\"#ff\" font-size=\"#fs\">#t</text>".replaceAll("#i", getID()).replaceAll("#fi", colorToHex(getColor()));
        WhiteboardPoint p = getWhiteboardPoint();
        return s.replaceAll("#x", "" + p.getX()).replaceAll("#y", "" + p.getY()).replaceAll("#ff", getFontName()).replaceAll("#fs", "" + getFontSize()).replaceAll("#t", getText());
    }
}
