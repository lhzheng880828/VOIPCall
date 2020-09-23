package net.java.sip.communicator.impl.protocol.jabber;

import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.packet.VCard;

public class VCardXEP0153 extends VCard {
    public void setAvatar(byte[] bytes) {
        setAvatar(bytes, "image/jpeg");
    }

    public void setAvatar(byte[] bytes, String mimeType) {
        if (bytes == null) {
            super.setAvatar(bytes, mimeType);
        } else if (bytes.length == 0) {
            setEncodedImage("");
            setField("PHOTO", "", true);
        } else {
            String encodedImage = StringUtils.encodeBase64(bytes);
            setEncodedImage(encodedImage);
            setField("PHOTO", "<TYPE>" + mimeType + "</TYPE>" + "<BINVAL>" + encodedImage + "</BINVAL>", true);
        }
    }
}
