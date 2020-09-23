package gov.nist.javax.sdp.parser;

import gov.nist.javax.sdp.fields.Email;
import gov.nist.javax.sdp.fields.EmailAddress;
import gov.nist.javax.sdp.fields.EmailField;
import gov.nist.javax.sdp.fields.SDPField;
import java.text.ParseException;
import org.jitsi.gov.nist.core.Separators;

public class EmailFieldParser extends SDPParser {
    public EmailFieldParser(String emailField) {
        this.lexer = new Lexer("charLexer", emailField);
    }

    public String getDisplayName(String rest) {
        try {
            int begin = rest.indexOf(Separators.LPAREN);
            int end = rest.indexOf(Separators.RPAREN);
            if (begin != -1) {
                return rest.substring(begin + 1, end);
            }
            int ind = rest.indexOf(Separators.LESS_THAN);
            if (ind != -1) {
                return rest.substring(0, ind);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Email getEmail(String rest) {
        Email email = new Email();
        try {
            int begin = rest.indexOf(Separators.LPAREN);
            String emailTemp;
            int i;
            if (begin != -1) {
                emailTemp = rest.substring(0, begin);
                i = emailTemp.indexOf(Separators.AT);
                if (i != -1) {
                    email.setUserName(emailTemp.substring(0, i));
                    email.setHostName(emailTemp.substring(i + 1));
                }
            } else {
                int ind = rest.indexOf(Separators.LESS_THAN);
                int end = rest.indexOf(Separators.GREATER_THAN);
                if (ind != -1) {
                    emailTemp = rest.substring(ind + 1, end);
                    i = emailTemp.indexOf(Separators.AT);
                    if (i != -1) {
                        email.setUserName(emailTemp.substring(0, i));
                        email.setHostName(emailTemp.substring(i + 1));
                    }
                } else {
                    i = rest.indexOf(Separators.AT);
                    int j = rest.indexOf(Separators.RETURN);
                    if (i != -1) {
                        email.setUserName(rest.substring(0, i));
                        email.setHostName(rest.substring(i + 1));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return email;
    }

    public EmailField emailField() throws ParseException {
        try {
            this.lexer.match(101);
            this.lexer.SPorHT();
            this.lexer.match(61);
            this.lexer.SPorHT();
            EmailField emailField = new EmailField();
            EmailAddress emailAddress = new EmailAddress();
            String rest = this.lexer.getRest();
            emailAddress.setDisplayName(getDisplayName(rest.trim()));
            emailAddress.setEmail(getEmail(rest));
            emailField.setEmailAddress(emailAddress);
            return emailField;
        } catch (Exception e) {
            throw new ParseException(this.lexer.getBuffer(), this.lexer.getPtr());
        }
    }

    public SDPField parse() throws ParseException {
        return emailField();
    }
}
