package net.java.sip.communicator.impl.protocol.jabber.extensions.inputevt;

import java.util.ArrayList;
import java.util.List;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.IQ;

public class InputEvtIQ extends IQ {
    public static final String ACTION_ATTR_NAME = "action";
    public static final String ELEMENT_NAME = "inputevt";
    public static final String NAMESPACE = "http://jitsi.org/protocol/inputevt";
    public static final String NAMESPACE_CLIENT = "http://jitsi.org/protocol/inputevt/sharee";
    public static final String NAMESPACE_SERVER = "http://jitsi.org/protocol/inputevt/sharer";
    private InputEvtAction action = null;
    private List<RemoteControlExtension> remoteControls = new ArrayList();

    public String getChildElementXML() {
        StringBuilder bldr = new StringBuilder("<inputevt");
        bldr.append(" xmlns='http://jitsi.org/protocol/inputevt'");
        bldr.append(" action='" + getAction() + Separators.QUOTE);
        if (this.remoteControls.size() > 0) {
            bldr.append(Separators.GREATER_THAN);
            for (RemoteControlExtension p : this.remoteControls) {
                bldr.append(p.toXML());
            }
            bldr.append("</inputevt>");
        } else {
            bldr.append("/>");
        }
        return bldr.toString();
    }

    public void setAction(InputEvtAction action) {
        this.action = action;
    }

    public InputEvtAction getAction() {
        return this.action;
    }

    public void addRemoteControl(RemoteControlExtension item) {
        this.remoteControls.add(item);
    }

    public void removeRemoteControl(RemoteControlExtension item) {
        this.remoteControls.remove(item);
    }

    public List<RemoteControlExtension> getRemoteControls() {
        return this.remoteControls;
    }
}
