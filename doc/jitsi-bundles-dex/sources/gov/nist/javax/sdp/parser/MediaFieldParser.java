package gov.nist.javax.sdp.parser;

import gov.nist.javax.sdp.fields.MediaField;
import gov.nist.javax.sdp.fields.SDPField;
import java.text.ParseException;
import java.util.Vector;
import org.jitsi.gov.nist.core.Debug;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.core.Token;

public class MediaFieldParser extends SDPParser {
    public MediaFieldParser(String mediaField) {
        this.lexer = new Lexer("charLexer", mediaField);
    }

    public MediaField mediaField() throws ParseException {
        if (Debug.parserDebug) {
            dbg_enter("mediaField");
        }
        try {
            MediaField mediaField = new MediaField();
            this.lexer.match(109);
            this.lexer.SPorHT();
            this.lexer.match(61);
            this.lexer.SPorHT();
            this.lexer.match(4095);
            mediaField.setMedia(this.lexer.getNextToken().getTokenValue());
            this.lexer.SPorHT();
            this.lexer.match(4095);
            mediaField.setPort(Integer.parseInt(this.lexer.getNextToken().getTokenValue()));
            this.lexer.SPorHT();
            if (this.lexer.hasMoreChars() && this.lexer.lookAhead(1) == 10) {
                dbg_leave("mediaField");
            } else {
                if (this.lexer.lookAhead(0) == '/') {
                    this.lexer.consume(1);
                    this.lexer.match(4095);
                    mediaField.setNports(Integer.parseInt(this.lexer.getNextToken().getTokenValue()));
                    this.lexer.SPorHT();
                }
                this.lexer.match(4095);
                StringBuilder transport = new StringBuilder(this.lexer.getNextToken().getTokenValue());
                while (this.lexer.lookAhead(0) == '/') {
                    this.lexer.consume(1);
                    this.lexer.match(4095);
                    transport.append(Separators.SLASH).append(this.lexer.getNextToken().getTokenValue());
                }
                this.lexer.SPorHT();
                mediaField.setProto(transport.toString());
                Vector formatList = new Vector();
                while (this.lexer.hasMoreChars()) {
                    char la = this.lexer.lookAhead(0);
                    if (la == 10 || la == 13) {
                        break;
                    }
                    this.lexer.SPorHT();
                    this.lexer.match(4095);
                    Token tok = this.lexer.getNextToken();
                    this.lexer.SPorHT();
                    String format = tok.getTokenValue().trim();
                    if (!format.equals("")) {
                        formatList.add(format);
                    }
                }
                mediaField.setFormats(formatList);
                dbg_leave("mediaField");
            }
            return mediaField;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ParseException(this.lexer.getBuffer(), this.lexer.getPtr());
        } catch (Throwable th) {
            dbg_leave("mediaField");
        }
    }

    public SDPField parse() throws ParseException {
        return mediaField();
    }
}
