package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;

public class RedirectPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_NAME = "redirect";
    public static final String NAMESPACE = "http://www.google.com/session";
    private String redir = null;

    public RedirectPacketExtension() {
        super(NAMESPACE, "redirect");
    }

    public void setRedir(String redir) {
        setText(redir);
        this.redir = redir;
    }

    public String getRedir() {
        return this.redir;
    }
}
