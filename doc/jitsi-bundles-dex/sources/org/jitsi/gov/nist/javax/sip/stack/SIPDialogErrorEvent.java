package org.jitsi.gov.nist.javax.sip.stack;

import java.util.EventObject;
import org.jitsi.gov.nist.javax.sip.DialogTimeoutEvent.Reason;

public class SIPDialogErrorEvent extends EventObject {
    public static final int DIALOG_ACK_NOT_RECEIVED_TIMEOUT = 1;
    public static final int DIALOG_ACK_NOT_SENT_TIMEOUT = 2;
    public static final int DIALOG_ERROR_INTERNAL_COULD_NOT_TAKE_ACK_SEM = 5;
    public static final int DIALOG_REINVITE_TIMEOUT = 3;
    public static final int EARLY_STATE_TIMEOUT = 4;
    private int errorID;

    SIPDialogErrorEvent(SIPDialog sourceDialog, int dialogErrorID) {
        super(sourceDialog);
        this.errorID = dialogErrorID;
    }

    public SIPDialogErrorEvent(SIPDialog sourceDialog, Reason reason) {
        super(sourceDialog);
        if (reason == Reason.AckNotReceived) {
            this.errorID = 1;
        } else if (reason == Reason.AckNotSent) {
            this.errorID = 2;
        } else if (reason == Reason.ReInviteTimeout) {
            this.errorID = 3;
        } else if (reason == Reason.CannotAcquireAckSemaphoreForOk) {
            this.errorID = 5;
        } else if (reason == Reason.EarlyStateTimeout) {
            this.errorID = 4;
        }
    }

    public int getErrorID() {
        return this.errorID;
    }
}
