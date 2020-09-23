package org.jivesoftware.smackx.commands;

import org.jivesoftware.smackx.packet.AdHocCommandData;

public abstract class LocalCommand extends AdHocCommand {
    private long creationDate = System.currentTimeMillis();
    private int currenStage = -1;
    private String ownerJID;
    private String sessionID;

    public abstract boolean hasPermission(String str);

    public abstract boolean isLastStage();

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
        getData().setSessionID(sessionID);
    }

    public String getSessionID() {
        return this.sessionID;
    }

    public void setOwnerJID(String ownerJID) {
        this.ownerJID = ownerJID;
    }

    public String getOwnerJID() {
        return this.ownerJID;
    }

    public long getCreationDate() {
        return this.creationDate;
    }

    public int getCurrentStage() {
        return this.currenStage;
    }

    /* access modifiers changed from: 0000 */
    public void setData(AdHocCommandData data) {
        data.setSessionID(this.sessionID);
        super.setData(data);
    }

    /* access modifiers changed from: 0000 */
    public void incrementStage() {
        this.currenStage++;
    }

    /* access modifiers changed from: 0000 */
    public void decrementStage() {
        this.currenStage--;
    }
}
