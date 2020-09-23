package net.java.sip.communicator.impl.protocol.jabber.extensions.thumbnail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.w3c.dom.Element;

public class ThumbnailElement {
    public static final String CID = "cid";
    public static final String ELEMENT_NAME = "thumbnail";
    public static final String HEIGHT = "height";
    public static final String MIME_TYPE = "mime-type";
    public static final String NAMESPACE = "urn:xmpp:thumbs:0";
    public static final String WIDTH = "width";
    private static final Logger logger = Logger.getLogger(ThumbnailElement.class);
    private String cid;
    private int height;
    private String mimeType;
    private int width;

    public ThumbnailElement(String serverAddress, byte[] thumbnailData, String mimeType, int width, int height) {
        this.cid = createCid(serverAddress, thumbnailData);
        this.mimeType = mimeType;
        this.width = width;
        this.height = height;
    }

    public ThumbnailElement(String xml) {
        try {
            Element e = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes())).getDocumentElement();
            if (e.getNodeName().equals(ELEMENT_NAME)) {
                setCid(e.getAttribute("cid"));
                setMimeType(e.getAttribute(MIME_TYPE));
                setHeight(Integer.parseInt(e.getAttribute(HEIGHT)));
                setHeight(Integer.parseInt(e.getAttribute(WIDTH)));
            } else if (logger.isDebugEnabled()) {
                logger.debug("Element name unknown!");
            }
        } catch (ParserConfigurationException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Problem parsing Thumbnail Element : " + xml, ex);
            }
        } catch (IOException ex2) {
            if (logger.isDebugEnabled()) {
                logger.debug("Problem parsing Thumbnail Element : " + xml, ex2);
            }
        } catch (Exception ex3) {
            if (logger.isDebugEnabled()) {
                logger.debug("Problem parsing Thumbnail Element : " + xml, ex3);
            }
        }
    }

    public String toXML() {
        StringBuffer buf = new StringBuffer();
        buf.append(Separators.LESS_THAN).append(ELEMENT_NAME).append(" xmlns=\"").append(NAMESPACE).append(Separators.DOUBLE_QUOTE);
        buf = addXmlIntAttribute(addXmlIntAttribute(addXmlAttribute(addXmlAttribute(buf, "cid", getCid()), MIME_TYPE, getMimeType()), WIDTH, getWidth()), HEIGHT, getWidth());
        buf.append("/>");
        return buf.toString();
    }

    public String getCid() {
        return this.cid;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    private StringBuffer addXmlAttribute(StringBuffer buff, String attrName, String attrValue) {
        buff.append(Separators.SP + attrName + "=\"").append(attrValue).append(Separators.DOUBLE_QUOTE);
        return buff;
    }

    private StringBuffer addXmlIntAttribute(StringBuffer buff, String attrName, int attrValue) {
        return addXmlAttribute(buff, attrName, String.valueOf(attrValue));
    }

    /* JADX WARNING: Missing block: B:11:?, code skipped:
            return null;
     */
    private java.lang.String createCid(java.lang.String r4, byte[] r5) {
        /*
        r3 = this;
        r1 = new java.lang.StringBuilder;	 Catch:{ NoSuchAlgorithmException -> 0x0022, UnsupportedEncodingException -> 0x0034 }
        r1.<init>();	 Catch:{ NoSuchAlgorithmException -> 0x0022, UnsupportedEncodingException -> 0x0034 }
        r2 = "sha1+";
        r1 = r1.append(r2);	 Catch:{ NoSuchAlgorithmException -> 0x0022, UnsupportedEncodingException -> 0x0034 }
        r2 = net.java.sip.communicator.util.Sha1Crypto.encode(r5);	 Catch:{ NoSuchAlgorithmException -> 0x0022, UnsupportedEncodingException -> 0x0034 }
        r1 = r1.append(r2);	 Catch:{ NoSuchAlgorithmException -> 0x0022, UnsupportedEncodingException -> 0x0034 }
        r2 = "@";
        r1 = r1.append(r2);	 Catch:{ NoSuchAlgorithmException -> 0x0022, UnsupportedEncodingException -> 0x0034 }
        r1 = r1.append(r4);	 Catch:{ NoSuchAlgorithmException -> 0x0022, UnsupportedEncodingException -> 0x0034 }
        r1 = r1.toString();	 Catch:{ NoSuchAlgorithmException -> 0x0022, UnsupportedEncodingException -> 0x0034 }
    L_0x0021:
        return r1;
    L_0x0022:
        r0 = move-exception;
        r1 = logger;
        r1 = r1.isDebugEnabled();
        if (r1 == 0) goto L_0x0032;
    L_0x002b:
        r1 = logger;
        r2 = "Failed to encode the thumbnail in SHA-1.";
        r1.debug(r2, r0);
    L_0x0032:
        r1 = 0;
        goto L_0x0021;
    L_0x0034:
        r0 = move-exception;
        r1 = logger;
        r1 = r1.isDebugEnabled();
        if (r1 == 0) goto L_0x0032;
    L_0x003d:
        r1 = logger;
        r2 = "Failed to encode the thumbnail in SHA-1.";
        r1.debug(r2, r0);
        goto L_0x0032;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.protocol.jabber.extensions.thumbnail.ThumbnailElement.createCid(java.lang.String, byte[]):java.lang.String");
    }
}
