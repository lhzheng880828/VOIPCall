package gov.nist.javax.sdp.parser;

import gov.nist.javax.sdp.SessionDescriptionImpl;
import gov.nist.javax.sdp.fields.SDPField;
import java.text.ParseException;
import java.util.Vector;
import org.jitsi.gov.nist.core.ParserCore;
import org.jitsi.gov.nist.core.Separators;

public class SDPAnnounceParser extends ParserCore {
    protected Lexer lexer;
    protected Vector sdpMessage;

    public SDPAnnounceParser(Vector sdpMessage) {
        this.sdpMessage = sdpMessage;
    }

    public SDPAnnounceParser(String message) {
        int start = 0;
        String line = null;
        if (message != null) {
            this.sdpMessage = new Vector();
            String sdpAnnounce = message.trim() + Separators.NEWLINE;
            while (start < sdpAnnounce.length()) {
                int lfPos = sdpAnnounce.indexOf(Separators.RETURN, start);
                int crPos = sdpAnnounce.indexOf("\r", start);
                if (lfPos > 0 && crPos < 0) {
                    line = sdpAnnounce.substring(start, lfPos);
                    start = lfPos + 1;
                } else if (lfPos < 0 && crPos > 0) {
                    line = sdpAnnounce.substring(start, crPos);
                    start = crPos + 1;
                } else if (lfPos <= 0 || crPos <= 0) {
                    if (lfPos < 0 && crPos < 0) {
                        return;
                    }
                } else if (lfPos > crPos) {
                    line = sdpAnnounce.substring(start, crPos);
                    if (lfPos == crPos + 1) {
                        start = lfPos + 1;
                    } else {
                        start = crPos + 1;
                    }
                } else {
                    line = sdpAnnounce.substring(start, lfPos);
                    if (crPos == lfPos + 1) {
                        start = crPos + 1;
                    } else {
                        start = lfPos + 1;
                    }
                }
                this.sdpMessage.addElement(line);
            }
        }
    }

    public SessionDescriptionImpl parse() throws ParseException {
        SessionDescriptionImpl retval = new SessionDescriptionImpl();
        for (int i = 0; i < this.sdpMessage.size(); i++) {
            SDPParser sdpParser = ParserFactory.createParser((String) this.sdpMessage.elementAt(i));
            SDPField sdpField = null;
            if (sdpParser != null) {
                sdpField = sdpParser.parse();
            }
            retval.addField(sdpField);
        }
        return retval;
    }
}
