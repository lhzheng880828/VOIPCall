package org.jitsi.gov.nist.javax.sip.address;

import java.text.ParseException;
import java.util.regex.Pattern;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.parser.StringMsgParser;
import org.jitsi.gov.nist.javax.sip.parser.URLParser;
import org.jitsi.javax.sip.address.Address;
import org.jitsi.javax.sip.address.SipURI;
import org.jitsi.javax.sip.address.TelURL;
import org.jitsi.javax.sip.address.URI;

public class AddressFactoryImpl implements AddressFactoryEx {
    public static final Pattern SCHEME_PATTERN = Pattern.compile("\\p{Alpha}[[{\\p{Alpha}][\\p{Digit}][\\+][-][\\.]]*");

    public Address createAddress() {
        return new AddressImpl();
    }

    public Address createAddress(String displayName, URI uri) {
        if (uri == null) {
            throw new NullPointerException("null  URI");
        }
        AddressImpl addressImpl = new AddressImpl();
        if (displayName != null) {
            addressImpl.setDisplayName(displayName);
        }
        addressImpl.setURI(uri);
        return addressImpl;
    }

    public Address createAddress(URI uri) {
        if (uri == null) {
            throw new NullPointerException("null address");
        }
        AddressImpl addressImpl = new AddressImpl();
        addressImpl.setURI(uri);
        return addressImpl;
    }

    public Address createAddress(String address) throws ParseException {
        if (address == null) {
            throw new NullPointerException("null address");
        } else if (!address.equals(Separators.STAR)) {
            return new StringMsgParser().parseAddress(address);
        } else {
            AddressImpl addressImpl = new AddressImpl();
            addressImpl.setAddressType(3);
            SipURI uri = new SipUri();
            uri.setUser(Separators.STAR);
            addressImpl.setURI(uri);
            return addressImpl;
        }
    }

    public SipURI createSipURI(String uri) throws ParseException {
        if (uri == null) {
            throw new NullPointerException("null URI");
        }
        try {
            return new StringMsgParser().parseSIPUrl(uri);
        } catch (ParseException ex) {
            throw new ParseException(ex.getMessage(), 0);
        }
    }

    public SipURI createSipURI(String user, String host) throws ParseException {
        if (host == null) {
            throw new NullPointerException("null host");
        }
        StringBuilder uriString = new StringBuilder("sip:");
        if (user != null) {
            uriString.append(user);
            uriString.append(Separators.AT);
        }
        if (!(host.indexOf(58) == host.lastIndexOf(58) || host.trim().charAt(0) == '[')) {
            host = '[' + host + ']';
        }
        uriString.append(host);
        try {
            return createSipURI(uriString.toString());
        } catch (ParseException ex) {
            throw new ParseException(ex.getMessage(), 0);
        }
    }

    public TelURL createTelURL(String uri) throws ParseException {
        if (uri == null) {
            throw new NullPointerException("null url");
        }
        String telUrl;
        if (uri.startsWith("tel:")) {
            telUrl = uri;
        } else {
            telUrl = "tel:" + uri;
        }
        try {
            return (TelURLImpl) new StringMsgParser().parseUrl(telUrl);
        } catch (ParseException ex) {
            throw new ParseException(ex.getMessage(), 0);
        }
    }

    public URI createURI(String uri) throws ParseException {
        if (uri == null) {
            throw new NullPointerException("null arg");
        }
        try {
            String scheme = new URLParser(uri).peekScheme();
            if (scheme == null) {
                throw new ParseException("bad scheme", 0);
            } else if (scheme.equalsIgnoreCase("sip") || scheme.equalsIgnoreCase("sips")) {
                return createSipURI(uri);
            } else {
                if (scheme.equalsIgnoreCase("tel")) {
                    return createTelURL(uri);
                }
                if (SCHEME_PATTERN.matcher(scheme).matches()) {
                    return new GenericURI(uri);
                }
                throw new ParseException("the scheme " + scheme + " from the following uri " + uri + " doesn't match ALPHA *(ALPHA / DIGIT / \"+\" / \"-\" / \".\" ) from RFC3261", 0);
            }
        } catch (ParseException ex) {
            throw new ParseException(ex.getMessage(), 0);
        }
    }
}
