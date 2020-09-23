package gov.nist.javax.sdp.parser;

import gov.nist.javax.sdp.fields.SDPField;
import java.text.ParseException;
import org.jitsi.gov.nist.core.ParserCore;

public abstract class SDPParser extends ParserCore {
    public abstract SDPField parse() throws ParseException;
}
