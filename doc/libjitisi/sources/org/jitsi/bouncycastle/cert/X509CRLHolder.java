package org.jitsi.bouncycastle.cert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import org.jitsi.bouncycastle.asn1.ASN1InputStream;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.DEROutputStream;
import org.jitsi.bouncycastle.asn1.x500.X500Name;
import org.jitsi.bouncycastle.asn1.x509.CertificateList;
import org.jitsi.bouncycastle.asn1.x509.Extension;
import org.jitsi.bouncycastle.asn1.x509.Extensions;
import org.jitsi.bouncycastle.asn1.x509.GeneralName;
import org.jitsi.bouncycastle.asn1.x509.GeneralNames;
import org.jitsi.bouncycastle.asn1.x509.IssuingDistributionPoint;
import org.jitsi.bouncycastle.asn1.x509.TBSCertList;
import org.jitsi.bouncycastle.asn1.x509.TBSCertList.CRLEntry;
import org.jitsi.bouncycastle.operator.ContentVerifier;
import org.jitsi.bouncycastle.operator.ContentVerifierProvider;

public class X509CRLHolder {
    private Extensions extensions;
    private boolean isIndirect;
    private GeneralNames issuerName;
    private CertificateList x509CRL;

    public X509CRLHolder(InputStream inputStream) throws IOException {
        this(parseStream(inputStream));
    }

    public X509CRLHolder(CertificateList certificateList) {
        this.x509CRL = certificateList;
        this.extensions = certificateList.getTBSCertList().getExtensions();
        this.isIndirect = isIndirectCRL(this.extensions);
        this.issuerName = new GeneralNames(new GeneralName(certificateList.getIssuer()));
    }

    public X509CRLHolder(byte[] bArr) throws IOException {
        this(parseStream(new ByteArrayInputStream(bArr)));
    }

    private static boolean isIndirectCRL(Extensions extensions) {
        if (extensions == null) {
            return false;
        }
        Extension extension = extensions.getExtension(Extension.issuingDistributionPoint);
        return extension != null && IssuingDistributionPoint.getInstance(extension.getParsedValue()).isIndirectCRL();
    }

    private static CertificateList parseStream(InputStream inputStream) throws IOException {
        try {
            return CertificateList.getInstance(new ASN1InputStream(inputStream, true).readObject());
        } catch (ClassCastException e) {
            throw new CertIOException("malformed data: " + e.getMessage(), e);
        } catch (IllegalArgumentException e2) {
            throw new CertIOException("malformed data: " + e2.getMessage(), e2);
        }
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof X509CRLHolder)) {
            return false;
        }
        return this.x509CRL.equals(((X509CRLHolder) obj).x509CRL);
    }

    public Set getCriticalExtensionOIDs() {
        return CertUtils.getCriticalExtensionOIDs(this.extensions);
    }

    public byte[] getEncoded() throws IOException {
        return this.x509CRL.getEncoded();
    }

    public Extension getExtension(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return this.extensions != null ? this.extensions.getExtension(aSN1ObjectIdentifier) : null;
    }

    public List getExtensionOIDs() {
        return CertUtils.getExtensionOIDs(this.extensions);
    }

    public Extensions getExtensions() {
        return this.extensions;
    }

    public X500Name getIssuer() {
        return X500Name.getInstance(this.x509CRL.getIssuer());
    }

    public Set getNonCriticalExtensionOIDs() {
        return CertUtils.getNonCriticalExtensionOIDs(this.extensions);
    }

    public X509CRLEntryHolder getRevokedCertificate(BigInteger bigInteger) {
        GeneralNames generalNames = this.issuerName;
        Enumeration revokedCertificateEnumeration = this.x509CRL.getRevokedCertificateEnumeration();
        while (true) {
            GeneralNames generalNames2 = generalNames;
            if (!revokedCertificateEnumeration.hasMoreElements()) {
                return null;
            }
            CRLEntry cRLEntry = (CRLEntry) revokedCertificateEnumeration.nextElement();
            if (cRLEntry.getUserCertificate().getValue().equals(bigInteger)) {
                return new X509CRLEntryHolder(cRLEntry, this.isIndirect, generalNames2);
            }
            if (this.isIndirect && cRLEntry.hasExtensions()) {
                Extension extension = cRLEntry.getExtensions().getExtension(Extension.certificateIssuer);
                if (extension != null) {
                    generalNames2 = GeneralNames.getInstance(extension.getParsedValue());
                }
            }
            generalNames = generalNames2;
        }
    }

    public Collection getRevokedCertificates() {
        ArrayList arrayList = new ArrayList(this.x509CRL.getRevokedCertificates().length);
        GeneralNames generalNames = this.issuerName;
        Enumeration revokedCertificateEnumeration = this.x509CRL.getRevokedCertificateEnumeration();
        while (true) {
            GeneralNames generalNames2 = generalNames;
            if (!revokedCertificateEnumeration.hasMoreElements()) {
                return arrayList;
            }
            X509CRLEntryHolder x509CRLEntryHolder = new X509CRLEntryHolder((CRLEntry) revokedCertificateEnumeration.nextElement(), this.isIndirect, generalNames2);
            arrayList.add(x509CRLEntryHolder);
            generalNames = x509CRLEntryHolder.getCertificateIssuer();
        }
    }

    public boolean hasExtensions() {
        return this.extensions != null;
    }

    public int hashCode() {
        return this.x509CRL.hashCode();
    }

    public boolean isSignatureValid(ContentVerifierProvider contentVerifierProvider) throws CertException {
        TBSCertList tBSCertList = this.x509CRL.getTBSCertList();
        if (CertUtils.isAlgIdEqual(tBSCertList.getSignature(), this.x509CRL.getSignatureAlgorithm())) {
            try {
                ContentVerifier contentVerifier = contentVerifierProvider.get(tBSCertList.getSignature());
                OutputStream outputStream = contentVerifier.getOutputStream();
                new DEROutputStream(outputStream).writeObject(tBSCertList);
                outputStream.close();
                return contentVerifier.verify(this.x509CRL.getSignature().getBytes());
            } catch (Exception e) {
                throw new CertException("unable to process signature: " + e.getMessage(), e);
            }
        }
        throw new CertException("signature invalid - algorithm identifier mismatch");
    }

    public CertificateList toASN1Structure() {
        return this.x509CRL;
    }
}
