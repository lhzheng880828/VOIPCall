package org.jivesoftware.smackx.packet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.PacketExtension;

public class MUCInitialPresence implements PacketExtension {
    private History history;
    private String password;

    public static class History {
        private int maxChars = -1;
        private int maxStanzas = -1;
        private int seconds = -1;
        private Date since;

        public int getMaxChars() {
            return this.maxChars;
        }

        public int getMaxStanzas() {
            return this.maxStanzas;
        }

        public int getSeconds() {
            return this.seconds;
        }

        public Date getSince() {
            return this.since;
        }

        public void setMaxChars(int maxChars) {
            this.maxChars = maxChars;
        }

        public void setMaxStanzas(int maxStanzas) {
            this.maxStanzas = maxStanzas;
        }

        public void setSeconds(int seconds) {
            this.seconds = seconds;
        }

        public void setSince(Date since) {
            this.since = since;
        }

        public String toXML() {
            StringBuilder buf = new StringBuilder();
            buf.append("<history");
            if (getMaxChars() != -1) {
                buf.append(" maxchars=\"").append(getMaxChars()).append(Separators.DOUBLE_QUOTE);
            }
            if (getMaxStanzas() != -1) {
                buf.append(" maxstanzas=\"").append(getMaxStanzas()).append(Separators.DOUBLE_QUOTE);
            }
            if (getSeconds() != -1) {
                buf.append(" seconds=\"").append(getSeconds()).append(Separators.DOUBLE_QUOTE);
            }
            if (getSince() != null) {
                SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                buf.append(" since=\"").append(utcFormat.format(getSince())).append(Separators.DOUBLE_QUOTE);
            }
            buf.append("/>");
            return buf.toString();
        }
    }

    public String getElementName() {
        return "x";
    }

    public String getNamespace() {
        return "http://jabber.org/protocol/muc";
    }

    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append(Separators.LESS_THAN).append(getElementName()).append(" xmlns=\"").append(getNamespace()).append("\">");
        if (getPassword() != null) {
            buf.append("<password>").append(getPassword()).append("</password>");
        }
        if (getHistory() != null) {
            buf.append(getHistory().toXML());
        }
        buf.append("</").append(getElementName()).append(Separators.GREATER_THAN);
        return buf.toString();
    }

    public History getHistory() {
        return this.history;
    }

    public String getPassword() {
        return this.password;
    }

    public void setHistory(History history) {
        this.history = history;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
