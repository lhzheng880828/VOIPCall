package net.java.sip.communicator.impl.protocol.jabber;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.util.StringUtils;

public class VCardTempXUpdatePresenceExtension implements PacketExtension, PacketInterceptor {
    public static final String ELEMENT_NAME = "x";
    public static final String NAMESPACE = "vcard-temp:x:update";
    private String imageSha1 = null;
    private String xmlString = null;

    public VCardTempXUpdatePresenceExtension(byte[] imageBytes) {
        computeXML();
        updateImage(imageBytes);
    }

    public boolean updateImage(byte[] imageBytes) {
        String tmpImageSha1 = getImageSha1(imageBytes);
        if (tmpImageSha1 == this.imageSha1) {
            return false;
        }
        this.imageSha1 = tmpImageSha1;
        computeXML();
        return true;
    }

    public static String getImageSha1(byte[] image) {
        String imageSha1 = null;
        if (image == null) {
            return imageSha1;
        }
        try {
            return StringUtils.encodeHex(MessageDigest.getInstance("SHA1").digest(image));
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            return imageSha1;
        }
    }

    private void computeXML() {
        StringBuilder stringBuilder = new StringBuilder(Separators.LESS_THAN + getElementName() + " xmlns='" + getNamespace() + "'>");
        if (this.imageSha1 == null) {
            stringBuilder.append("<photo/>");
        } else {
            stringBuilder.append("<photo>" + this.imageSha1 + "</photo>");
        }
        stringBuilder.append("</" + getElementName() + Separators.GREATER_THAN);
        this.xmlString = stringBuilder.toString();
    }

    public String getElementName() {
        return "x";
    }

    public String getNamespace() {
        return NAMESPACE;
    }

    public String toXML() {
        return this.xmlString;
    }

    public void interceptPacket(Packet packet) {
        packet.addExtension(this);
    }
}
