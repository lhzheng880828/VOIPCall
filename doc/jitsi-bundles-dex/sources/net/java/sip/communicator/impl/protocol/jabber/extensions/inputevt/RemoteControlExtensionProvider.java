package net.java.sip.communicator.impl.protocol.jabber.extensions.inputevt;

import org.jitsi.android.util.java.awt.Canvas;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.android.util.java.awt.event.ComponentEvent;
import org.jitsi.android.util.java.awt.event.KeyEvent;
import org.jitsi.android.util.java.awt.event.MouseEvent;
import org.jitsi.android.util.java.awt.event.MouseWheelEvent;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.javax.sip.message.Response;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;

public class RemoteControlExtensionProvider implements PacketExtensionProvider {
    public static final String ELEMENT_KEY_PRESS = "key-press";
    public static final String ELEMENT_KEY_RELEASE = "key-release";
    public static final String ELEMENT_KEY_TYPE = "key-type";
    public static final String ELEMENT_MOUSE_MOVE = "mouse-move";
    public static final String ELEMENT_MOUSE_PRESS = "mouse-press";
    public static final String ELEMENT_MOUSE_RELEASE = "mouse-release";
    public static final String ELEMENT_MOUSE_WHEEL = "mouse-wheel";
    public static final String ELEMENT_REMOTE_CONTROL = "remote-control";
    public static final String NAMESPACE = "http://jitsi.org/protocol/inputevt";
    private static final Component component = new Canvas();

    public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
        boolean done = false;
        RemoteControlExtension result = null;
        while (!done) {
            try {
                int eventType = parser.next();
                if (eventType == 2) {
                    String attr;
                    if (parser.getName().equals(ELEMENT_MOUSE_MOVE)) {
                        attr = parser.getAttributeValue("", "x");
                        String attr2 = parser.getAttributeValue("", "y");
                        if (!(attr == null || attr2 == null)) {
                            result = new RemoteControlExtension(new MouseEvent(component, Response.SERVICE_UNAVAILABLE, System.currentTimeMillis(), 0, (int) (Double.parseDouble(attr) * 1000.0d), (int) (Double.parseDouble(attr2) * 1000.0d), 0, false, 0));
                        }
                    }
                    if (parser.getName().equals(ELEMENT_MOUSE_WHEEL)) {
                        attr = parser.getAttributeValue("", "notch");
                        if (attr != null) {
                            result = new RemoteControlExtension((ComponentEvent) new MouseWheelEvent(component, 507, System.currentTimeMillis(), 0, 0, 0, 0, false, 0, 0, Integer.parseInt(attr)));
                        }
                    }
                    if (parser.getName().equals(ELEMENT_MOUSE_PRESS)) {
                        attr = parser.getAttributeValue("", "btns");
                        if (attr != null) {
                            result = new RemoteControlExtension((ComponentEvent) new MouseEvent(component, Response.NOT_IMPLEMENTED, System.currentTimeMillis(), Integer.parseInt(attr), 0, 0, 0, false, 0));
                        }
                    }
                    if (parser.getName().equals(ELEMENT_MOUSE_RELEASE)) {
                        attr = parser.getAttributeValue("", "btns");
                        if (attr != null) {
                            result = new RemoteControlExtension((ComponentEvent) new MouseEvent(component, Response.BAD_GATEWAY, System.currentTimeMillis(), Integer.parseInt(attr), 0, 0, 0, false, 0));
                        }
                    }
                    if (parser.getName().equals(ELEMENT_KEY_PRESS)) {
                        attr = parser.getAttributeValue("", "keycode");
                        if (attr != null) {
                            result = new RemoteControlExtension(new KeyEvent(component, Response.UNAUTHORIZED, System.currentTimeMillis(), 0, Integer.parseInt(attr), 0));
                        }
                    }
                    if (parser.getName().equals(ELEMENT_KEY_RELEASE)) {
                        attr = parser.getAttributeValue("", "keycode");
                        if (attr != null) {
                            result = new RemoteControlExtension(new KeyEvent(component, Response.PAYMENT_REQUIRED, System.currentTimeMillis(), 0, Integer.parseInt(attr), 0));
                        }
                    }
                    if (parser.getName().equals(ELEMENT_KEY_TYPE)) {
                        attr = parser.getAttributeValue("", "keychar");
                        if (attr != null) {
                            result = new RemoteControlExtension(new KeyEvent(component, Response.BAD_REQUEST, System.currentTimeMillis(), 0, 0, (char) Integer.parseInt(attr)));
                        }
                    }
                } else if (eventType == 3 && parser.getName().equals("remote-control")) {
                    done = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            return new RemoteControlExtension(new ComponentEvent(component, 0));
        }
        return result;
    }

    private static void append(StringBuffer stringBuffer, String... strings) {
        for (String str : strings) {
            stringBuffer.append(str);
        }
    }

    public static String getKeyPressedXML(int keycode) {
        StringBuffer xml = new StringBuffer();
        append(xml, "<remote-control xmlns=\"http://jitsi.org/protocol/inputevt\">");
        append(xml, Separators.LESS_THAN, ELEMENT_KEY_PRESS);
        append(xml, " keycode=\"", Integer.toString(keycode), "\"/>");
        append(xml, "</remote-control>");
        return xml.toString();
    }

    public static String getKeyReleasedXML(int keycode) {
        StringBuffer xml = new StringBuffer();
        append(xml, "<remote-control xmlns=\"http://jitsi.org/protocol/inputevt\">");
        append(xml, Separators.LESS_THAN, ELEMENT_KEY_RELEASE);
        append(xml, " keycode=\"", Integer.toString(keycode), "\"/>");
        append(xml, "</remote-control>");
        return xml.toString();
    }

    public static String getKeyTypedXML(int keycode) {
        StringBuffer xml = new StringBuffer();
        append(xml, "<remote-control xmlns=\"http://jitsi.org/protocol/inputevt\">");
        append(xml, Separators.LESS_THAN, ELEMENT_KEY_TYPE);
        append(xml, " keychar=\"", Integer.toString(keycode), "\"/>");
        append(xml, "</remote-control>");
        return xml.toString();
    }

    public static String getMousePressedXML(int btns) {
        StringBuffer xml = new StringBuffer();
        append(xml, "<remote-control xmlns=\"http://jitsi.org/protocol/inputevt\">");
        append(xml, Separators.LESS_THAN, ELEMENT_MOUSE_PRESS);
        append(xml, " btns=\"", Integer.toString(btns), "\"/>");
        append(xml, "</remote-control>");
        return xml.toString();
    }

    public static String getMouseReleasedXML(int btns) {
        StringBuffer xml = new StringBuffer();
        append(xml, "<remote-control xmlns=\"http://jitsi.org/protocol/inputevt\">");
        append(xml, Separators.LESS_THAN, ELEMENT_MOUSE_RELEASE);
        append(xml, " btns=\"", Integer.toString(btns), "\"/>");
        append(xml, "</remote-control>");
        return xml.toString();
    }

    public static String getMouseMovedXML(double x, double y) {
        StringBuffer xml = new StringBuffer();
        append(xml, "<remote-control xmlns=\"http://jitsi.org/protocol/inputevt\">");
        append(xml, Separators.LESS_THAN, ELEMENT_MOUSE_MOVE);
        append(xml, " x=\"", Double.toString(x), "\" y=\"", Double.toString(y), "\"/>");
        append(xml, "</remote-control>");
        return xml.toString();
    }

    public static String getMouseWheelXML(int notch) {
        StringBuffer xml = new StringBuffer();
        append(xml, "<remote-control xmlns=\"http://jitsi.org/protocol/inputevt\">");
        append(xml, Separators.LESS_THAN, ELEMENT_MOUSE_WHEEL);
        append(xml, " notch=\"", Integer.toString(notch), "\"/>");
        append(xml, "</remote-control>");
        return xml.toString();
    }
}
