package net.java.sip.communicator.impl.protocol.sip.net;

import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.java.sip.communicator.impl.protocol.sip.ProtocolProviderServiceSipImpl;
import net.java.sip.communicator.impl.protocol.sip.SipActivator;
import net.java.sip.communicator.service.certificate.CertificateMatcher;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.NetworkUtils;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.javax.sip.address.SipURI;

public class RFC5922Matcher implements CertificateMatcher {
    public static final String PNAME_STRICT_RFC5922 = "net.java.sip.communicator.sip.tls.STRICT_RFC5922";
    private static final Logger logger = Logger.getLogger(CertificateMatcher.class);
    private ProtocolProviderServiceSipImpl provider;

    public RFC5922Matcher(ProtocolProviderServiceSipImpl provider) {
        this.provider = provider;
    }

    public void verify(Iterable<String> identitiesToTest, X509Certificate cert) throws CertificateException {
        boolean strict = SipActivator.getConfigurationService().getBoolean(PNAME_STRICT_RFC5922, false);
        Iterable<String> certIdentities = extractCertIdentities(cert);
        for (String identity : identitiesToTest) {
            for (String dnsName : certIdentities) {
                try {
                    if (NetworkUtils.compareDnsNames(dnsName, identity) != 0) {
                        if (!strict && dnsName.startsWith("*.") && identity.indexOf(Separators.DOT) < identity.lastIndexOf(Separators.DOT) && NetworkUtils.compareDnsNames(dnsName.substring(2), identity.substring(identity.indexOf(Separators.DOT) + 1)) == 0) {
                            return;
                        }
                    }
                    return;
                } catch (ParseException e) {
                }
            }
        }
        if (!false) {
            throw new CertificateException("None of <" + identitiesToTest + "> matched by the rules of RFC5922 to the cert with CN=" + cert.getSubjectDN());
        }
    }

    private Iterable<String> extractCertIdentities(X509Certificate cert) {
        List<String> certIdentities = new ArrayList();
        Collection<List<?>> subjAltNames = null;
        try {
            subjAltNames = cert.getSubjectAlternativeNames();
        } catch (CertificateParsingException ex) {
            logger.error("Error parsing TLS certificate", ex);
        }
        Integer dnsNameType = Integer.valueOf(2);
        Integer uriNameType = Integer.valueOf(6);
        if (subjAltNames != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("found subjAltNames: " + subjAltNames);
            }
            for (List<?> altName : subjAltNames) {
                if (altName.get(0).equals(uriNameType)) {
                    try {
                        SipURI altNameUri = this.provider.getAddressFactory().createSipURI((String) altName.get(1));
                        if ("sip".equals(altNameUri.getScheme()) && altNameUri.getUser() == null) {
                            String altHostName = altNameUri.getHost();
                            if (logger.isDebugEnabled()) {
                                logger.debug("found uri " + altName.get(1) + ", hostName " + altHostName);
                            }
                            certIdentities.add(altHostName);
                        }
                    } catch (ParseException e) {
                        logger.error("certificate contains invalid uri: " + altName.get(1));
                    }
                }
            }
            if (certIdentities.isEmpty()) {
                for (List<?> altName2 : subjAltNames) {
                    if (altName2.get(0).equals(dnsNameType)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("found dns " + altName2.get(1));
                        }
                        certIdentities.add(altName2.get(1).toString());
                    }
                }
            }
        } else {
            String dname = cert.getSubjectDN().getName();
            String cname = "";
            try {
                Matcher matcher = Pattern.compile(".*CN\\s*=\\s*([\\w*\\.]+).*").matcher(dname);
                if (matcher.matches()) {
                    cname = matcher.group(1);
                    if (logger.isDebugEnabled()) {
                        logger.debug("found CN: " + cname + " from DN: " + dname);
                    }
                    certIdentities.add(cname);
                }
            } catch (Exception ex2) {
                logger.error("exception while extracting CN", ex2);
            }
        }
        return certIdentities;
    }
}
