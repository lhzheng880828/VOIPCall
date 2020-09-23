package org.jivesoftware.smackx.workgroup.ext.macros;

import java.io.StringReader;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.util.StringUtils;
import org.xmlpull.mxp1.MXParser;

public class Macros extends IQ {
    public static final String ELEMENT_NAME = "macros";
    public static final String NAMESPACE = "http://jivesoftware.com/protocol/workgroup";
    private boolean personal;
    private MacroGroup personalMacroGroup;
    private MacroGroup rootGroup;

    public static class InternalProvider implements IQProvider {
        public IQ parseIQ(XmlPullParser parser) throws Exception {
            Macros macroGroup = new Macros();
            boolean done = false;
            while (!done) {
                int eventType = parser.next();
                if (eventType == 2) {
                    if (parser.getName().equals("model")) {
                        macroGroup.setRootGroup(parseMacroGroups(parser.nextText()));
                    }
                } else if (eventType == 3 && parser.getName().equals(Macros.ELEMENT_NAME)) {
                    done = true;
                }
            }
            return macroGroup;
        }

        public Macro parseMacro(XmlPullParser parser) throws Exception {
            Macro macro = new Macro();
            boolean done = false;
            while (!done) {
                int eventType = parser.next();
                if (eventType == 2) {
                    if (parser.getName().equals("title")) {
                        parser.next();
                        macro.setTitle(parser.getText());
                    } else if (parser.getName().equals("description")) {
                        macro.setDescription(parser.nextText());
                    } else if (parser.getName().equals("response")) {
                        macro.setResponse(parser.nextText());
                    } else if (parser.getName().equals("type")) {
                        macro.setType(Integer.valueOf(parser.nextText()).intValue());
                    }
                } else if (eventType == 3 && parser.getName().equals("macro")) {
                    done = true;
                }
            }
            return macro;
        }

        public MacroGroup parseMacroGroup(XmlPullParser parser) throws Exception {
            MacroGroup group = new MacroGroup();
            boolean done = false;
            while (!done) {
                int eventType = parser.next();
                if (eventType == 2) {
                    if (parser.getName().equals("macrogroup")) {
                        group.addMacroGroup(parseMacroGroup(parser));
                    }
                    if (parser.getName().equals("title")) {
                        group.setTitle(parser.nextText());
                    }
                    if (parser.getName().equals("macro")) {
                        group.addMacro(parseMacro(parser));
                    }
                } else if (eventType == 3 && parser.getName().equals("macrogroup")) {
                    done = true;
                }
            }
            return group;
        }

        public MacroGroup parseMacroGroups(String macros) throws Exception {
            MacroGroup group = null;
            XmlPullParser parser = new MXParser();
            parser.setInput(new StringReader(macros));
            int eventType = parser.getEventType();
            while (eventType != 1) {
                eventType = parser.next();
                if (eventType == 2 && parser.getName().equals("macrogroup")) {
                    group = parseMacroGroup(parser);
                }
            }
            return group;
        }
    }

    public MacroGroup getRootGroup() {
        return this.rootGroup;
    }

    public void setRootGroup(MacroGroup rootGroup) {
        this.rootGroup = rootGroup;
    }

    public boolean isPersonal() {
        return this.personal;
    }

    public void setPersonal(boolean personal) {
        this.personal = personal;
    }

    public MacroGroup getPersonalMacroGroup() {
        return this.personalMacroGroup;
    }

    public void setPersonalMacroGroup(MacroGroup personalMacroGroup) {
        this.personalMacroGroup = personalMacroGroup;
    }

    public String getChildElementXML() {
        StringBuilder buf = new StringBuilder();
        buf.append(Separators.LESS_THAN).append(ELEMENT_NAME).append(" xmlns=\"").append("http://jivesoftware.com/protocol/workgroup").append("\">");
        if (isPersonal()) {
            buf.append("<personal>true</personal>");
        }
        if (getPersonalMacroGroup() != null) {
            buf.append("<personalMacro>");
            buf.append(StringUtils.escapeForXML(getPersonalMacroGroup().toXML()));
            buf.append("</personalMacro>");
        }
        buf.append("</").append(ELEMENT_NAME).append("> ");
        return buf.toString();
    }
}
