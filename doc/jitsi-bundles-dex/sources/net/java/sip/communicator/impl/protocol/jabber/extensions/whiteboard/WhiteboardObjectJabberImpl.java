package net.java.sip.communicator.impl.protocol.jabber.extensions.whiteboard;

import javax.sdp.SdpConstants;
import net.java.sip.communicator.service.protocol.whiteboardobjects.WhiteboardObject;
import org.jitsi.android.util.java.awt.Color;
import org.jitsi.gov.nist.core.Separators;

public abstract class WhiteboardObjectJabberImpl implements WhiteboardObject {
    private String ID;
    private int color;
    private int thickness = 1;

    public abstract String toXML();

    public WhiteboardObjectJabberImpl() {
        setID(generateID());
    }

    public WhiteboardObjectJabberImpl(String id, int thickness, int color) {
        setID(id);
        setColor(color);
        setThickness(thickness);
    }

    /* access modifiers changed from: protected */
    public String generateID() {
        return String.valueOf(System.currentTimeMillis()) + String.valueOf(super.hashCode());
    }

    public String getID() {
        return this.ID;
    }

    /* access modifiers changed from: protected */
    public void setID(String ID) {
        this.ID = ID;
    }

    public int getThickness() {
        return this.thickness;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof WhiteboardObject)) {
            return false;
        }
        if (obj == this || ((WhiteboardObject) obj).getID().equals(getID())) {
            return true;
        }
        return false;
    }

    public int getColor() {
        return this.color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    private String hex(int i) {
        String h = Integer.toHexString(i);
        if (i < 10) {
            h = SdpConstants.RESERVED + h;
        }
        return h.toUpperCase();
    }

    /* access modifiers changed from: protected */
    public String colorToHex(int color) {
        return colorToHex(Color.getColor("", color));
    }

    /* access modifiers changed from: protected */
    public String colorToHex(Color color) {
        return Separators.POUND + hex(color.getRed()) + hex(color.getGreen()) + hex(color.getBlue());
    }

    public void setThickness(int thickness) {
        this.thickness = thickness;
    }
}
