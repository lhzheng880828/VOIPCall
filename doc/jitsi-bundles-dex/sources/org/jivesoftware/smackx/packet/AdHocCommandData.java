package org.jivesoftware.smackx.packet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.commands.AdHocCommand.Action;
import org.jivesoftware.smackx.commands.AdHocCommand.SpecificErrorCondition;
import org.jivesoftware.smackx.commands.AdHocCommand.Status;
import org.jivesoftware.smackx.commands.AdHocCommandNote;

public class AdHocCommandData extends IQ {
    private Action action;
    private ArrayList<Action> actions = new ArrayList();
    private Action executeAction;
    private DataForm form;
    private String id;
    private String lang;
    private String name;
    private String node;
    private List<AdHocCommandNote> notes = new ArrayList();
    private String sessionID;
    private Status status;

    public static class SpecificError implements PacketExtension {
        public static final String namespace = "http://jabber.org/protocol/commands";
        public SpecificErrorCondition condition;

        public SpecificError(SpecificErrorCondition condition) {
            this.condition = condition;
        }

        public String getElementName() {
            return this.condition.toString();
        }

        public String getNamespace() {
            return namespace;
        }

        public SpecificErrorCondition getCondition() {
            return this.condition;
        }

        public String toXML() {
            StringBuilder buf = new StringBuilder();
            buf.append(Separators.LESS_THAN).append(getElementName());
            buf.append(" xmlns=\"").append(getNamespace()).append("\"/>");
            return buf.toString();
        }
    }

    public String getChildElementXML() {
        Iterator i$;
        StringBuilder buf = new StringBuilder();
        buf.append("<command xmlns=\"http://jabber.org/protocol/commands\"");
        buf.append(" node=\"").append(this.node).append(Separators.DOUBLE_QUOTE);
        if (!(this.sessionID == null || this.sessionID.equals(""))) {
            buf.append(" sessionid=\"").append(this.sessionID).append(Separators.DOUBLE_QUOTE);
        }
        if (this.status != null) {
            buf.append(" status=\"").append(this.status).append(Separators.DOUBLE_QUOTE);
        }
        if (this.action != null) {
            buf.append(" action=\"").append(this.action).append(Separators.DOUBLE_QUOTE);
        }
        if (!(this.lang == null || this.lang.equals(""))) {
            buf.append(" lang=\"").append(this.lang).append(Separators.DOUBLE_QUOTE);
        }
        buf.append(Separators.GREATER_THAN);
        if (getType() == Type.RESULT) {
            buf.append("<actions");
            if (this.executeAction != null) {
                buf.append(" execute=\"").append(this.executeAction).append(Separators.DOUBLE_QUOTE);
            }
            if (this.actions.size() == 0) {
                buf.append("/>");
            } else {
                buf.append(Separators.GREATER_THAN);
                i$ = this.actions.iterator();
                while (i$.hasNext()) {
                    buf.append(Separators.LESS_THAN).append((Action) i$.next()).append("/>");
                }
                buf.append("</actions>");
            }
        }
        if (this.form != null) {
            buf.append(this.form.toXML());
        }
        for (AdHocCommandNote note : this.notes) {
            buf.append("<note type=\"").append(note.getType().toString()).append("\">");
            buf.append(note.getValue());
            buf.append("</note>");
        }
        buf.append("</command>");
        return buf.toString();
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNode() {
        return this.node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public List<AdHocCommandNote> getNotes() {
        return this.notes;
    }

    public void addNote(AdHocCommandNote note) {
        this.notes.add(note);
    }

    public void remveNote(AdHocCommandNote note) {
        this.notes.remove(note);
    }

    public DataForm getForm() {
        return this.form;
    }

    public void setForm(DataForm form) {
        this.form = form;
    }

    public Action getAction() {
        return this.action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Action> getActions() {
        return this.actions;
    }

    public void addAction(Action action) {
        this.actions.add(action);
    }

    public void setExecuteAction(Action executeAction) {
        this.executeAction = executeAction;
    }

    public Action getExecuteAction() {
        return this.executeAction;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public String getSessionID() {
        return this.sessionID;
    }
}
