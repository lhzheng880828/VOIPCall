package org.jitsi.bouncycastle.cms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CRLException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jitsi.bouncycastle.asn1.ASN1Encodable;
import org.jitsi.bouncycastle.asn1.ASN1EncodableVector;
import org.jitsi.bouncycastle.asn1.ASN1InputStream;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.ASN1Primitive;
import org.jitsi.bouncycastle.asn1.ASN1Set;
import org.jitsi.bouncycastle.asn1.BEROctetStringGenerator;
import org.jitsi.bouncycastle.asn1.BERSet;
import org.jitsi.bouncycastle.asn1.DERSet;
import org.jitsi.bouncycastle.asn1.DERTaggedObject;
import org.jitsi.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.cms.ContentInfo;
import org.jitsi.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.jitsi.bouncycastle.asn1.cms.OtherRevocationInfoFormat;
import org.jitsi.bouncycastle.asn1.ocsp.OCSPResponse;
import org.jitsi.bouncycastle.asn1.x509.Certificate;
import org.jitsi.bouncycastle.asn1.x509.CertificateList;
import org.jitsi.bouncycastle.asn1.x509.TBSCertificate;
import org.jitsi.bouncycastle.cert.X509AttributeCertificateHolder;
import org.jitsi.bouncycastle.cert.X509CRLHolder;
import org.jitsi.bouncycastle.cert.X509CertificateHolder;
import org.jitsi.bouncycastle.operator.DigestCalculator;
import org.jitsi.bouncycastle.util.Store;
import org.jitsi.bouncycastle.util.io.Streams;
import org.jitsi.bouncycastle.util.io.TeeInputStream;
import org.jitsi.bouncycastle.util.io.TeeOutputStream;

class CMSUtils {
    CMSUtils() {
    }

    static InputStream attachDigestsToInputStream(Collection collection, InputStream inputStream) {
        for (DigestCalculator outputStream : collection) {
            inputStream = new TeeInputStream(inputStream, outputStream.getOutputStream());
        }
        return inputStream;
    }

    static OutputStream attachSignersToOutputStream(Collection collection, OutputStream outputStream) {
        for (SignerInfoGenerator calculatingOutputStream : collection) {
            outputStream = getSafeTeeOutputStream(outputStream, calculatingOutputStream.getCalculatingOutputStream());
        }
        return outputStream;
    }

    static OutputStream createBEROctetOutputStream(OutputStream outputStream, int i, boolean z, int i2) throws IOException {
        BEROctetStringGenerator bEROctetStringGenerator = new BEROctetStringGenerator(outputStream, i, z);
        return i2 != 0 ? bEROctetStringGenerator.getOctetOutputStream(new byte[i2]) : bEROctetStringGenerator.getOctetOutputStream();
    }

    static ASN1Set createBerSetFromList(List list) {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        for (ASN1Encodable add : list) {
            aSN1EncodableVector.add(add);
        }
        return new BERSet(aSN1EncodableVector);
    }

    static ASN1Set createDerSetFromList(List list) {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        for (ASN1Encodable add : list) {
            aSN1EncodableVector.add(add);
        }
        return new DERSet(aSN1EncodableVector);
    }

    static List getAttributeCertificatesFromStore(Store store) throws CMSException {
        ArrayList arrayList = new ArrayList();
        try {
            for (X509AttributeCertificateHolder toASN1Structure : store.getMatches(null)) {
                arrayList.add(new DERTaggedObject(false, 2, toASN1Structure.toASN1Structure()));
            }
            return arrayList;
        } catch (ClassCastException e) {
            throw new CMSException("error processing certs", e);
        }
    }

    static List getCRLsFromStore(CertStore certStore) throws CertStoreException, CMSException {
        ArrayList arrayList = new ArrayList();
        try {
            for (X509CRL encoded : certStore.getCRLs(null)) {
                arrayList.add(CertificateList.getInstance(ASN1Primitive.fromByteArray(encoded.getEncoded())));
            }
            return arrayList;
        } catch (IllegalArgumentException e) {
            throw new CMSException("error processing crls", e);
        } catch (IOException e2) {
            throw new CMSException("error processing crls", e2);
        } catch (CRLException e3) {
            throw new CMSException("error encoding crls", e3);
        }
    }

    static List getCRLsFromStore(Store store) throws CMSException {
        ArrayList arrayList = new ArrayList();
        try {
            for (X509CRLHolder toASN1Structure : store.getMatches(null)) {
                arrayList.add(toASN1Structure.toASN1Structure());
            }
            return arrayList;
        } catch (ClassCastException e) {
            throw new CMSException("error processing certs", e);
        }
    }

    static List getCertificatesFromStore(CertStore certStore) throws CertStoreException, CMSException {
        ArrayList arrayList = new ArrayList();
        try {
            for (X509Certificate encoded : certStore.getCertificates(null)) {
                arrayList.add(Certificate.getInstance(ASN1Primitive.fromByteArray(encoded.getEncoded())));
            }
            return arrayList;
        } catch (IllegalArgumentException e) {
            throw new CMSException("error processing certs", e);
        } catch (IOException e2) {
            throw new CMSException("error processing certs", e2);
        } catch (CertificateEncodingException e3) {
            throw new CMSException("error encoding certs", e3);
        }
    }

    static List getCertificatesFromStore(Store store) throws CMSException {
        ArrayList arrayList = new ArrayList();
        try {
            for (X509CertificateHolder toASN1Structure : store.getMatches(null)) {
                arrayList.add(toASN1Structure.toASN1Structure());
            }
            return arrayList;
        } catch (ClassCastException e) {
            throw new CMSException("error processing certs", e);
        }
    }

    static IssuerAndSerialNumber getIssuerAndSerialNumber(X509Certificate x509Certificate) {
        TBSCertificate tBSCertificateStructure = getTBSCertificateStructure(x509Certificate);
        return new IssuerAndSerialNumber(tBSCertificateStructure.getIssuer(), tBSCertificateStructure.getSerialNumber().getValue());
    }

    static Collection getOthersFromStore(ASN1ObjectIdentifier aSN1ObjectIdentifier, Store store) {
        ArrayList arrayList = new ArrayList();
        for (ASN1Encodable aSN1Encodable : store.getMatches(null)) {
            if (!CMSObjectIdentifiers.id_ri_ocsp_response.equals(aSN1ObjectIdentifier) || OCSPResponse.getInstance(aSN1Encodable).getResponseStatus().getValue().intValue() == 0) {
                arrayList.add(new DERTaggedObject(false, 1, new OtherRevocationInfoFormat(aSN1ObjectIdentifier, aSN1Encodable)));
            } else {
                throw new IllegalArgumentException("cannot add unsuccessful OCSP response to CMS SignedData");
            }
        }
        return arrayList;
    }

    public static Provider getProvider(String str) throws NoSuchProviderException {
        if (str == null) {
            return null;
        }
        Provider provider = Security.getProvider(str);
        if (provider != null) {
            return provider;
        }
        throw new NoSuchProviderException("provider " + str + " not found.");
    }

    static OutputStream getSafeOutputStream(OutputStream outputStream) {
        return outputStream == null ? new NullOutputStream() : outputStream;
    }

    static OutputStream getSafeTeeOutputStream(OutputStream outputStream, OutputStream outputStream2) {
        return outputStream == null ? getSafeOutputStream(outputStream2) : outputStream2 == null ? getSafeOutputStream(outputStream) : new TeeOutputStream(outputStream, outputStream2);
    }

    static TBSCertificate getTBSCertificateStructure(X509Certificate x509Certificate) {
        try {
            return TBSCertificate.getInstance(ASN1Primitive.fromByteArray(x509Certificate.getTBSCertificate()));
        } catch (Exception e) {
            throw new IllegalArgumentException("can't extract TBS structure from this cert");
        }
    }

    static ContentInfo readContentInfo(InputStream inputStream) throws CMSException {
        return readContentInfo(new ASN1InputStream(inputStream));
    }

    private static ContentInfo readContentInfo(ASN1InputStream aSN1InputStream) throws CMSException {
        try {
            return ContentInfo.getInstance(aSN1InputStream.readObject());
        } catch (IOException e) {
            throw new CMSException("IOException reading content.", e);
        } catch (ClassCastException e2) {
            throw new CMSException("Malformed content.", e2);
        } catch (IllegalArgumentException e3) {
            throw new CMSException("Malformed content.", e3);
        }
    }

    static ContentInfo readContentInfo(byte[] bArr) throws CMSException {
        return readContentInfo(new ASN1InputStream(bArr));
    }

    public static byte[] streamToByteArray(InputStream inputStream) throws IOException {
        return Streams.readAll(inputStream);
    }

    public static byte[] streamToByteArray(InputStream inputStream, int i) throws IOException {
        return Streams.readAllLimited(inputStream, i);
    }
}
