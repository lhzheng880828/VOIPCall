package org.jivesoftware.smackx.muc;

import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.packet.DiscoverInfo;

public class RoomInfo {
    private String description = "";
    private boolean membersOnly;
    private boolean moderated;
    private boolean nonanonymous;
    private int occupantsCount = -1;
    private boolean passwordProtected;
    private boolean persistent;
    private String room;
    private String subject = "";

    RoomInfo(DiscoverInfo info) {
        this.room = info.getFrom();
        this.membersOnly = info.containsFeature("muc_membersonly");
        this.moderated = info.containsFeature("muc_moderated");
        this.nonanonymous = info.containsFeature("muc_nonanonymous");
        this.passwordProtected = info.containsFeature("muc_passwordprotected");
        this.persistent = info.containsFeature("muc_persistent");
        Form form = Form.getFormFrom(info);
        if (form != null) {
            FormField descField = form.getField("muc#roominfo_description");
            String str = (descField == null || !descField.getValues().hasNext()) ? "" : (String) descField.getValues().next();
            this.description = str;
            FormField subjField = form.getField("muc#roominfo_subject");
            str = (subjField == null || !subjField.getValues().hasNext()) ? "" : (String) subjField.getValues().next();
            this.subject = str;
            FormField occCountField = form.getField("muc#roominfo_occupants");
            this.occupantsCount = occCountField == null ? -1 : Integer.parseInt((String) occCountField.getValues().next());
        }
    }

    public String getRoom() {
        return this.room;
    }

    public String getDescription() {
        return this.description;
    }

    public String getSubject() {
        return this.subject;
    }

    public int getOccupantsCount() {
        return this.occupantsCount;
    }

    public boolean isMembersOnly() {
        return this.membersOnly;
    }

    public boolean isModerated() {
        return this.moderated;
    }

    public boolean isNonanonymous() {
        return this.nonanonymous;
    }

    public boolean isPasswordProtected() {
        return this.passwordProtected;
    }

    public boolean isPersistent() {
        return this.persistent;
    }
}
