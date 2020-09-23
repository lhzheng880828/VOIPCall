package gov.nist.javax.sdp.parser;

import java.text.ParseException;
import java.util.Hashtable;
import net.java.sip.communicator.impl.protocol.jabber.extensions.caps.CapsPacketExtension;
import org.jitsi.gov.nist.core.InternalErrorHandler;

public class ParserFactory {
    private static Class[] constructorArgs = new Class[1];
    private static final String packageName = "gov.nist.javax.sdp.parser";
    private static Hashtable parserTable = new Hashtable();

    private static Class getParser(String parserClass) {
        try {
            return Class.forName("gov.nist.javax.sdp.parser." + parserClass);
        } catch (ClassNotFoundException ex) {
            System.out.println("Could not find class");
            ex.printStackTrace();
            System.exit(0);
            return null;
        }
    }

    static {
        constructorArgs[0] = String.class;
        parserTable.put("a", getParser("AttributeFieldParser"));
        parserTable.put("b", getParser("BandwidthFieldParser"));
        parserTable.put(CapsPacketExtension.ELEMENT_NAME, getParser("ConnectionFieldParser"));
        parserTable.put("e", getParser("EmailFieldParser"));
        parserTable.put("i", getParser("InformationFieldParser"));
        parserTable.put("k", getParser("KeyFieldParser"));
        parserTable.put("m", getParser("MediaFieldParser"));
        parserTable.put("o", getParser("OriginFieldParser"));
        parserTable.put("p", getParser("PhoneFieldParser"));
        parserTable.put("v", getParser("ProtoVersionFieldParser"));
        parserTable.put("r", getParser("RepeatFieldParser"));
        parserTable.put("s", getParser("SessionNameFieldParser"));
        parserTable.put("t", getParser("TimeFieldParser"));
        parserTable.put("u", getParser("URIFieldParser"));
        parserTable.put("z", getParser("ZoneFieldParser"));
    }

    public static SDPParser createParser(String field) throws ParseException {
        String fieldName = Lexer.getFieldName(field);
        if (fieldName == null) {
            return null;
        }
        Class parserClass = (Class) parserTable.get(fieldName.toLowerCase());
        if (parserClass != null) {
            try {
                return (SDPParser) parserClass.getConstructor(constructorArgs).newInstance(new Object[]{field});
            } catch (Exception ex) {
                InternalErrorHandler.handleException(ex);
                return null;
            }
        }
        throw new ParseException("Could not find parser for " + fieldName, 0);
    }
}
