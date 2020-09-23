package org.jitsi.util.xml;

import com.sun.media.format.WavAudioFormat;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class DOMElementWriter {
    private static final String lSep = System.getProperty("line.separator");
    protected String[] knownEntities = new String[]{"gt", "amp", "lt", "apos", "quot"};

    public static String decodeName(String name) {
        int length = name.length();
        StringBuilder value = new StringBuilder(length);
        int i = 0;
        while (i < length) {
            int start = name.indexOf(95, i);
            if (start == -1) {
                value.append(name, i, length);
                break;
            }
            if (i != start) {
                value.append(name, i, start);
            }
            int end = start + 6;
            if (end < length && name.charAt(start + 1) == 'x' && name.charAt(end) == '_' && isHexDigit(name.charAt(start + 2)) && isHexDigit(name.charAt(start + 3)) && isHexDigit(name.charAt(start + 4)) && isHexDigit(name.charAt(start + 5))) {
                char c = (char) Integer.parseInt(name.substring(start + 2, end), 16);
                if (start != 0 ? !isNameChar(c) : !isNameStartChar(c)) {
                    value.append(c);
                    i = end + 1;
                }
            }
            value.append(name.charAt(start));
            i = start + 1;
        }
        return value.toString();
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0047  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0031  */
    public static java.lang.String encodeName(java.lang.String r5) {
        /*
        r2 = r5.length();
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r1 = 0;
    L_0x000a:
        if (r1 >= r2) goto L_0x005b;
    L_0x000c:
        r0 = r5.charAt(r1);
        if (r1 != 0) goto L_0x001e;
    L_0x0012:
        r4 = isNameStartChar(r0);
        if (r4 == 0) goto L_0x0028;
    L_0x0018:
        r3.append(r0);
    L_0x001b:
        r1 = r1 + 1;
        goto L_0x000a;
    L_0x001e:
        r4 = isNameChar(r0);
        if (r4 == 0) goto L_0x0028;
    L_0x0024:
        r3.append(r0);
        goto L_0x001b;
    L_0x0028:
        r4 = "_x";
        r3.append(r4);
        r4 = 15;
        if (r0 > r4) goto L_0x0047;
    L_0x0031:
        r4 = "000";
        r3.append(r4);
    L_0x0036:
        r4 = java.lang.Integer.toHexString(r0);
        r4 = r4.toUpperCase();
        r3.append(r4);
        r4 = 95;
        r3.append(r4);
        goto L_0x001b;
    L_0x0047:
        r4 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        if (r0 > r4) goto L_0x0051;
    L_0x004b:
        r4 = "00";
        r3.append(r4);
        goto L_0x0036;
    L_0x0051:
        r4 = 4095; // 0xfff float:5.738E-42 double:2.023E-320;
        if (r0 > r4) goto L_0x0036;
    L_0x0055:
        r4 = 48;
        r3.append(r4);
        goto L_0x0036;
    L_0x005b:
        r4 = r3.toString();
        return r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.util.xml.DOMElementWriter.encodeName(java.lang.String):java.lang.String");
    }

    private static boolean isHexDigit(char c) {
        return ('0' <= c && c <= '9') || (('A' <= c && c <= 'F') || ('a' <= c && c <= 'f'));
    }

    private static boolean isNameChar(char c) {
        if (isNameStartChar(c) || c == '-' || c == '.') {
            return true;
        }
        if (('0' <= c && c <= '9') || c == 183) {
            return true;
        }
        if (c < 768) {
            return false;
        }
        if (c <= 879) {
            return true;
        }
        if (c < 8255) {
            return false;
        }
        if (c > 8256) {
            return false;
        }
        return true;
    }

    private static boolean isNameStartChar(char c) {
        if (c == ':' || c == '_') {
            return true;
        }
        if ('A' <= c && c <= 'Z') {
            return true;
        }
        if ('a' <= c && c <= 'z') {
            return true;
        }
        if (c < 192) {
            return false;
        }
        if (c <= 214) {
            return true;
        }
        if (c < 216) {
            return false;
        }
        if (c <= 246) {
            return true;
        }
        if (c < 248) {
            return false;
        }
        if (c <= 767) {
            return true;
        }
        if (c < 880) {
            return false;
        }
        if (c <= 893) {
            return true;
        }
        if (c < 895) {
            return false;
        }
        if (c <= 8191) {
            return true;
        }
        if (c < 8204) {
            return false;
        }
        if (c <= 8205) {
            return true;
        }
        if (c < 8304) {
            return false;
        }
        if (c <= 8591) {
            return true;
        }
        if (c < 11264) {
            return false;
        }
        if (c <= 12271) {
            return true;
        }
        if (c < 12289) {
            return false;
        }
        if (c <= 55295) {
            return true;
        }
        if (c < 63744) {
            return false;
        }
        if (c <= 64975) {
            return true;
        }
        if (c < 65008) {
            return false;
        }
        if (c > 65533) {
            return false;
        }
        return true;
    }

    public void write(Element root, OutputStream out) throws IOException {
        Writer wri = new OutputStreamWriter(out, "UTF-8");
        wri.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + lSep);
        write(root, wri, 0, "  ");
        wri.flush();
    }

    public void write(Node element, Writer out, int indent, String indentWith) throws IOException {
        int i;
        for (i = 0; i < indent; i++) {
            out.write(indentWith);
        }
        if (element.getNodeType() == (short) 8) {
            out.write("<!--");
            out.write(encode(element.getNodeValue()));
            out.write("-->");
        } else {
            out.write("<");
            out.write(((Element) element).getTagName());
            NamedNodeMap attrs = element.getAttributes();
            for (i = 0; i < attrs.getLength(); i++) {
                Attr attr = (Attr) attrs.item(i);
                out.write(" ");
                out.write(attr.getName());
                out.write("=\"");
                out.write(encode(attr.getValue()));
                out.write("\"");
            }
            out.write(">");
        }
        boolean hasChildren = false;
        NodeList children = element.getChildNodes();
        i = 0;
        while (element.hasChildNodes() && i < children.getLength()) {
            Node child = children.item(i);
            switch (child.getNodeType()) {
                case (short) 1:
                case (short) 8:
                    if (!hasChildren) {
                        out.write(lSep);
                        hasChildren = true;
                    }
                    write(child, out, indent + 1, indentWith);
                    break;
                case (short) 3:
                    if (child.getNodeValue() != null && (child.getNodeValue().indexOf("\n") == -1 || child.getNodeValue().trim().length() != 0)) {
                        out.write(encode(child.getNodeValue()));
                        break;
                    }
                case (short) 4:
                    out.write("<![CDATA[");
                    out.write(encodedata(((Text) child).getData()));
                    out.write("]]>");
                    break;
                case (short) 5:
                    out.write(38);
                    out.write(child.getNodeName());
                    out.write(59);
                    break;
                case (short) 7:
                    out.write("<?");
                    out.write(child.getNodeName());
                    String data = child.getNodeValue();
                    if (data != null && data.length() > 0) {
                        out.write(32);
                        out.write(data);
                    }
                    out.write("?>");
                    break;
                default:
                    break;
            }
            i++;
        }
        if (hasChildren) {
            for (i = 0; i < indent; i++) {
                out.write(indentWith);
            }
        }
        if (element.getNodeType() == (short) 1) {
            out.write("</");
            out.write(((Element) element).getTagName());
            out.write(">");
        }
        out.write(lSep);
        out.flush();
    }

    public String encode(String value) {
        StringBuffer sb = new StringBuffer();
        int len = value.length();
        int i = 0;
        while (i < len) {
            char c = value.charAt(i);
            switch (c) {
                case WavAudioFormat.WAVE_FORMAT_DSPGROUP_TRUESPEECH /*34*/:
                    sb.append("&quot;");
                    break;
                case '&':
                    int nextSemi = value.indexOf(";", i);
                    if (nextSemi >= 0 && isReference(value.substring(i, nextSemi + 1))) {
                        sb.append('&');
                        break;
                    }
                    sb.append("&amp;");
                    break;
                case '\'':
                    sb.append("&apos;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                default:
                    if (!isLegalCharacter(c)) {
                        break;
                    }
                    sb.append(c);
                    break;
            }
            i++;
        }
        return sb.substring(0);
    }

    public String encodedata(String value) {
        StringBuffer sb = new StringBuffer();
        int len = value.length();
        for (int i = 0; i < len; i++) {
            char c = value.charAt(i);
            if (isLegalCharacter(c)) {
                sb.append(c);
            }
        }
        String result = sb.substring(0);
        int cdEnd = result.indexOf("]]>");
        while (cdEnd != -1) {
            sb.setLength(cdEnd);
            sb.append("&#x5d;&#x5d;&gt;").append(result.substring(cdEnd + 3));
            result = sb.substring(0);
            cdEnd = result.indexOf("]]>");
        }
        return result;
    }

    public boolean isReference(String ent) {
        if (ent.charAt(0) != '&' || !ent.endsWith(";")) {
            return false;
        }
        if (ent.charAt(1) != '#') {
            String name = ent.substring(1, ent.length() - 1);
            for (Object equals : this.knownEntities) {
                if (name.equals(equals)) {
                    return true;
                }
            }
            return false;
        } else if (ent.charAt(2) == 'x') {
            try {
                Integer.parseInt(ent.substring(3, ent.length() - 1), 16);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        } else {
            try {
                Integer.parseInt(ent.substring(2, ent.length() - 1));
                return true;
            } catch (NumberFormatException e2) {
                return false;
            }
        }
    }

    public boolean isLegalCharacter(char c) {
        if (c == 9 || c == 10 || c == 13) {
            return true;
        }
        if (c < ' ') {
            return false;
        }
        if (c <= 55295) {
            return true;
        }
        if (c < 57344 || c > 65533) {
            return false;
        }
        return true;
    }
}
