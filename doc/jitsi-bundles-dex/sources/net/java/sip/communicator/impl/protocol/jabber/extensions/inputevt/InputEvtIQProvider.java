package net.java.sip.communicator.impl.protocol.jabber.extensions.inputevt;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;

public class InputEvtIQProvider implements IQProvider {
    public IQ parseIQ(XmlPullParser parser) throws Exception {
        InputEvtIQ inputEvtIQ = new InputEvtIQ();
        inputEvtIQ.setAction(InputEvtAction.parseString(parser.getAttributeValue("", "action")));
        boolean done = false;
        while (!done) {
            switch (parser.next()) {
                case 2:
                    if (!"remote-control".equals(parser.getName())) {
                        break;
                    }
                    inputEvtIQ.addRemoteControl((RemoteControlExtension) new RemoteControlExtensionProvider().parseExtension(parser));
                    break;
                case 3:
                    if (!"inputevt".equals(parser.getName())) {
                        break;
                    }
                    done = true;
                    break;
                default:
                    break;
            }
        }
        return inputEvtIQ;
    }
}
