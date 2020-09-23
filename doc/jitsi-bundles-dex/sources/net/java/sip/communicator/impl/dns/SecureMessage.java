package net.java.sip.communicator.impl.dns;

import java.io.IOException;
import org.xbill.DNS.Message;

public class SecureMessage extends Message {
    private boolean bogus;
    private String bogusReason;
    private boolean secure;

    public SecureMessage(UnboundResult msg) throws IOException {
        super(msg.answerPacket);
        this.secure = msg.secure;
        this.bogus = msg.bogus;
        this.bogusReason = msg.whyBogus;
    }

    public boolean isSecure() {
        return this.secure;
    }

    public boolean isBogus() {
        return this.bogus;
    }

    public String getBogusReason() {
        return this.bogusReason;
    }

    public String toString() {
        StringBuilder s = new StringBuilder(SecureMessage.super.toString());
        s.append(10);
        s.append(";; Secure: ");
        s.append(this.secure);
        s.append(10);
        s.append(";; Bogus:  ");
        s.append(this.bogus);
        s.append(10);
        if (this.bogus) {
            s.append(";;  Reason: ");
            s.append(this.bogusReason);
            s.append(10);
        }
        return s.toString();
    }
}
