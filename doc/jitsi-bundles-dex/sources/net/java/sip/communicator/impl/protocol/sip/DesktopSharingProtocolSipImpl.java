package net.java.sip.communicator.impl.protocol.sip;

import java.util.ArrayList;
import java.util.List;
import org.jitsi.android.util.java.awt.Canvas;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.Point;
import org.jitsi.android.util.java.awt.event.ComponentEvent;
import org.jitsi.android.util.java.awt.event.KeyEvent;
import org.jitsi.android.util.java.awt.event.MouseEvent;
import org.jitsi.android.util.java.awt.event.MouseWheelEvent;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.javax.sip.message.Response;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DesktopSharingProtocolSipImpl {
    public static final String CONTENT_SUB_TYPE = "remote-control+xml";
    private static final String ELEMENT_KEY_PRESS = "key-press";
    private static final String ELEMENT_KEY_RELEASE = "key-release";
    private static final String ELEMENT_KEY_TYPE = "key-type";
    private static final String ELEMENT_MOUSE_MOVE = "mouse-move";
    private static final String ELEMENT_MOUSE_PRESS = "mouse-press";
    private static final String ELEMENT_MOUSE_RELEASE = "mouse-release";
    private static final String ELEMENT_MOUSE_WHEEL = "mouse-wheel";
    private static final String ELEMENT_REMOTE_CONTROL = "remote-control";
    public static final String EVENT_PACKAGE = "remote-control";
    public static final int REFRESH_MARGIN = 60;
    public static final int SUBSCRIPTION_DURATION = 3600;
    private static final Component component = new Canvas();

    private static void append(StringBuffer stringBuffer, String... strings) {
        for (String str : strings) {
            stringBuffer.append(str);
        }
    }

    public static String getKeyPressedXML(int keycode) {
        StringBuffer xml = new StringBuffer();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        append(xml, Separators.LESS_THAN, "remote-control", Separators.GREATER_THAN);
        append(xml, Separators.LESS_THAN, "key-press");
        append(xml, " keycode=\"", Integer.toString(keycode), "\" />");
        append(xml, "</", "remote-control", Separators.GREATER_THAN);
        return xml.toString();
    }

    public static String getKeyReleasedXML(int keycode) {
        StringBuffer xml = new StringBuffer();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        append(xml, Separators.LESS_THAN, "remote-control", Separators.GREATER_THAN);
        append(xml, Separators.LESS_THAN, "key-release");
        append(xml, " keycode=\"", Integer.toString(keycode), "\" />");
        append(xml, "</", "remote-control", Separators.GREATER_THAN);
        return xml.toString();
    }

    public static String getKeyTypedXML(int keycode) {
        StringBuffer xml = new StringBuffer();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        append(xml, Separators.LESS_THAN, "remote-control", Separators.GREATER_THAN);
        append(xml, Separators.LESS_THAN, "key-type");
        append(xml, " keychar=\"", Integer.toString(keycode), "\" />");
        append(xml, "</", "remote-control", Separators.GREATER_THAN);
        return xml.toString();
    }

    public static String getMousePressedXML(int btns) {
        StringBuffer xml = new StringBuffer();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        append(xml, Separators.LESS_THAN, "remote-control", Separators.GREATER_THAN);
        append(xml, Separators.LESS_THAN, "mouse-press");
        append(xml, " btns=\"", Integer.toString(btns), "\" />");
        append(xml, "</", "remote-control", Separators.GREATER_THAN);
        return xml.toString();
    }

    public static String getMouseReleasedXML(int btns) {
        StringBuffer xml = new StringBuffer();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        append(xml, Separators.LESS_THAN, "remote-control", Separators.GREATER_THAN);
        append(xml, Separators.LESS_THAN, "mouse-release");
        append(xml, " btns=\"", Integer.toString(btns), "\" />");
        append(xml, "</", "remote-control", Separators.GREATER_THAN);
        return xml.toString();
    }

    public static String getMouseMovedXML(double x, double y) {
        StringBuffer xml = new StringBuffer();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        append(xml, Separators.LESS_THAN, "remote-control", Separators.GREATER_THAN);
        append(xml, Separators.LESS_THAN, "mouse-move");
        append(xml, " x=\"", Double.toString(x), "\" y=\"", Double.toString(y), "\" />");
        append(xml, "</", "remote-control", Separators.GREATER_THAN);
        return xml.toString();
    }

    public static String getMouseWheelXML(int notch) {
        StringBuffer xml = new StringBuffer();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        append(xml, Separators.LESS_THAN, "remote-control", Separators.GREATER_THAN);
        append(xml, Separators.LESS_THAN, "mouse-wheel");
        append(xml, " notch=\"", Integer.toString(notch), "\" />");
        append(xml, "</", "remote-control", Separators.GREATER_THAN);
        return xml.toString();
    }

    public static List<ComponentEvent> parse(Element root, Dimension size, Point origin) {
        int i;
        Element el;
        List<ComponentEvent> events = new ArrayList();
        int originX = origin != null ? origin.x : 0;
        int originY = origin != null ? origin.y : 0;
        NodeList nl = root.getElementsByTagName("mouse-press");
        if (nl != null) {
            for (i = 0; i < nl.getLength(); i++) {
                el = (Element) nl.item(i);
                if (el.hasAttribute("btns")) {
                    events.add(new MouseEvent(component, Response.NOT_IMPLEMENTED, System.currentTimeMillis(), Integer.parseInt(el.getAttribute("btns")), 0, 0, 0, false, 0));
                }
            }
        }
        nl = root.getElementsByTagName("mouse-release");
        if (nl != null) {
            for (i = 0; i < nl.getLength(); i++) {
                el = (Element) nl.item(i);
                if (el.hasAttribute("btns")) {
                    events.add(new MouseEvent(component, Response.BAD_GATEWAY, System.currentTimeMillis(), Integer.parseInt(el.getAttribute("btns")), 0, 0, 0, false, 0));
                }
            }
        }
        nl = root.getElementsByTagName("mouse-move");
        if (nl != null) {
            int x = -1;
            int y = -1;
            for (i = 0; i < nl.getLength(); i++) {
                el = (Element) nl.item(i);
                if (el.hasAttribute("x")) {
                    x = (int) ((Double.parseDouble(el.getAttribute("x")) * ((double) size.width)) + ((double) originX));
                }
                if (el.hasAttribute("y")) {
                    y = (int) ((Double.parseDouble(el.getAttribute("y")) * ((double) size.height)) + ((double) originY));
                }
                events.add(new MouseEvent(component, Response.SERVICE_UNAVAILABLE, System.currentTimeMillis(), 0, x, y, 0, false, 0));
            }
        }
        nl = root.getElementsByTagName("mouse-wheel");
        if (nl != null) {
            for (i = 0; i < nl.getLength(); i++) {
                el = (Element) nl.item(i);
                if (el.hasAttribute("notch")) {
                    events.add(new MouseWheelEvent(component, 507, System.currentTimeMillis(), 0, 0, 0, 0, false, 0, 0, Integer.parseInt(el.getAttribute("notch"))));
                }
            }
        }
        nl = root.getElementsByTagName("key-press");
        if (nl != null) {
            for (i = 0; i < nl.getLength(); i++) {
                el = (Element) nl.item(i);
                if (el.hasAttribute("keycode")) {
                    events.add(new KeyEvent(component, Response.UNAUTHORIZED, System.currentTimeMillis(), 0, Integer.parseInt(el.getAttribute("keycode")), 0));
                }
            }
        }
        nl = root.getElementsByTagName("key-release");
        if (nl != null) {
            for (i = 0; i < nl.getLength(); i++) {
                el = (Element) nl.item(i);
                if (el.hasAttribute("keycode")) {
                    events.add(new KeyEvent(component, Response.PAYMENT_REQUIRED, System.currentTimeMillis(), 0, Integer.parseInt(el.getAttribute("keycode")), 0));
                }
            }
        }
        nl = root.getElementsByTagName("key-type");
        if (nl != null) {
            for (i = 0; i < nl.getLength(); i++) {
                el = (Element) nl.item(i);
                if (el.hasAttribute("keychar")) {
                    events.add(new KeyEvent(component, Response.BAD_REQUEST, System.currentTimeMillis(), 0, 0, (char) Integer.parseInt(el.getAttribute("keychar"))));
                }
            }
        }
        return events;
    }
}
