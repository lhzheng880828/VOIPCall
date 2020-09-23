package net.java.sip.communicator.impl.protocol.jabber.extensions.inputevt;

import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.Point;
import org.jitsi.android.util.java.awt.event.ComponentEvent;
import org.jitsi.android.util.java.awt.event.InputEvent;
import org.jitsi.android.util.java.awt.event.KeyEvent;
import org.jitsi.android.util.java.awt.event.MouseEvent;
import org.jitsi.android.util.java.awt.event.MouseWheelEvent;
import org.jitsi.javax.sip.message.Response;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager;

public class RemoteControlExtension implements PacketExtension {
    private final ComponentEvent event;
    private final Dimension videoPanelSize;

    public RemoteControlExtension() {
        this.videoPanelSize = null;
        this.event = null;
    }

    public RemoteControlExtension(Dimension videoPanelSize) {
        this.videoPanelSize = videoPanelSize;
        this.event = null;
    }

    public RemoteControlExtension(ComponentEvent event) {
        this.event = event;
        this.videoPanelSize = null;
    }

    public RemoteControlExtension(InputEvent event, Dimension videoPanelSize) {
        this.videoPanelSize = videoPanelSize;
        this.event = event;
    }

    public ComponentEvent getEvent() {
        return this.event;
    }

    public String getElementName() {
        return "remote-control";
    }

    public String getNamespace() {
        return "http://jitsi.org/protocol/inputevt";
    }

    public String toXML() {
        String ret = null;
        if (this.event == null) {
            return null;
        }
        if (this.event instanceof MouseEvent) {
            MouseEvent e = this.event;
            switch (e.getID()) {
                case Response.NOT_IMPLEMENTED /*501*/:
                    ret = RemoteControlExtensionProvider.getMousePressedXML(e.getModifiers());
                    break;
                case Response.BAD_GATEWAY /*502*/:
                    ret = RemoteControlExtensionProvider.getMouseReleasedXML(e.getModifiers());
                    break;
                case Response.SERVICE_UNAVAILABLE /*503*/:
                case 506:
                    if (this.videoPanelSize != null) {
                        Point p = e.getPoint();
                        ret = RemoteControlExtensionProvider.getMouseMovedXML(p.getX() / ((double) this.videoPanelSize.width), p.getY() / ((double) this.videoPanelSize.height));
                        break;
                    }
                    break;
                case 507:
                    ret = RemoteControlExtensionProvider.getMouseWheelXML(((MouseWheelEvent) e).getWheelRotation());
                    break;
            }
        } else if (this.event instanceof KeyEvent) {
            KeyEvent e2 = this.event;
            int keycode = e2.getKeyCode();
            if (e2.getKeyChar() != InBandBytestreamManager.MAXIMUM_BLOCK_SIZE) {
                keycode = e2.getKeyChar();
            } else {
                keycode = e2.getKeyCode();
            }
            if (keycode != 0) {
                switch (e2.getID()) {
                    case Response.BAD_REQUEST /*400*/:
                        ret = RemoteControlExtensionProvider.getKeyTypedXML(keycode);
                        break;
                    case Response.UNAUTHORIZED /*401*/:
                        ret = RemoteControlExtensionProvider.getKeyPressedXML(keycode);
                        break;
                    case Response.PAYMENT_REQUIRED /*402*/:
                        ret = RemoteControlExtensionProvider.getKeyReleasedXML(keycode);
                        break;
                }
            }
            return null;
        }
        return ret;
    }
}
