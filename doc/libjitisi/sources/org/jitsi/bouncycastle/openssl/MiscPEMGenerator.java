package org.jitsi.bouncycastle.openssl;

import com.lti.utils.UnsignedUtils;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import org.jitsi.bouncycastle.asn1.ASN1EncodableVector;
import org.jitsi.bouncycastle.asn1.ASN1Integer;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.DERInteger;
import org.jitsi.bouncycastle.asn1.DERSequence;
import org.jitsi.bouncycastle.asn1.cms.ContentInfo;
import org.jitsi.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.jitsi.bouncycastle.asn1.x509.DSAParameter;
import org.jitsi.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.jitsi.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.jitsi.bouncycastle.cert.X509AttributeCertificateHolder;
import org.jitsi.bouncycastle.cert.X509CRLHolder;
import org.jitsi.bouncycastle.cert.X509CertificateHolder;
import org.jitsi.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.jitsi.bouncycastle.util.Strings;
import org.jitsi.bouncycastle.util.io.pem.PemGenerationException;
import org.jitsi.bouncycastle.util.io.pem.PemHeader;
import org.jitsi.bouncycastle.util.io.pem.PemObject;
import org.jitsi.bouncycastle.util.io.pem.PemObjectGenerator;

public class MiscPEMGenerator implements PemObjectGenerator {
    private static final ASN1ObjectIdentifier[] dsaOids = new ASN1ObjectIdentifier[]{X9ObjectIdentifiers.id_dsa, OIWObjectIdentifiers.dsaWithSHA1};
    private static final byte[] hexEncodingTable = new byte[]{(byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 65, (byte) 66, (byte) 67, (byte) 68, (byte) 69, (byte) 70};
    private final PEMEncryptor encryptor;
    private final Object obj;

    public MiscPEMGenerator(Object obj) {
        this.obj = obj;
        this.encryptor = null;
    }

    public MiscPEMGenerator(Object obj, PEMEncryptor pEMEncryptor) {
        this.obj = obj;
        this.encryptor = pEMEncryptor;
    }

    private PemObject createPemObject(Object obj) throws IOException {
        if (obj instanceof PemObject) {
            return (PemObject) obj;
        }
        if (obj instanceof PemObjectGenerator) {
            return ((PemObjectGenerator) obj).generate();
        }
        String str;
        byte[] encoded;
        if (obj instanceof X509CertificateHolder) {
            str = "CERTIFICATE";
            encoded = ((X509CertificateHolder) obj).getEncoded();
        } else if (obj instanceof X509CRLHolder) {
            str = "X509 CRL";
            encoded = ((X509CRLHolder) obj).getEncoded();
        } else if (obj instanceof PrivateKeyInfo) {
            PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) obj;
            ASN1ObjectIdentifier algorithm = privateKeyInfo.getPrivateKeyAlgorithm().getAlgorithm();
            if (algorithm.equals(PKCSObjectIdentifiers.rsaEncryption)) {
                str = "RSA PRIVATE KEY";
                encoded = privateKeyInfo.parsePrivateKey().toASN1Primitive().getEncoded();
            } else if (algorithm.equals(dsaOids[0]) || algorithm.equals(dsaOids[1])) {
                str = "DSA PRIVATE KEY";
                DSAParameter instance = DSAParameter.getInstance(privateKeyInfo.getPrivateKeyAlgorithm().getParameters());
                ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
                aSN1EncodableVector.add(new DERInteger(0));
                aSN1EncodableVector.add(new DERInteger(instance.getP()));
                aSN1EncodableVector.add(new DERInteger(instance.getQ()));
                aSN1EncodableVector.add(new DERInteger(instance.getG()));
                BigInteger value = ASN1Integer.getInstance(privateKeyInfo.parsePrivateKey()).getValue();
                aSN1EncodableVector.add(new DERInteger(instance.getG().modPow(value, instance.getP())));
                aSN1EncodableVector.add(new DERInteger(value));
                encoded = new DERSequence(aSN1EncodableVector).getEncoded();
            } else if (algorithm.equals(X9ObjectIdentifiers.id_ecPublicKey)) {
                str = "EC PRIVATE KEY";
                encoded = privateKeyInfo.parsePrivateKey().toASN1Primitive().getEncoded();
            } else {
                throw new IOException("Cannot identify private key");
            }
        } else if (obj instanceof SubjectPublicKeyInfo) {
            str = "PUBLIC KEY";
            encoded = ((SubjectPublicKeyInfo) obj).getEncoded();
        } else if (obj instanceof X509AttributeCertificateHolder) {
            str = "ATTRIBUTE CERTIFICATE";
            encoded = ((X509AttributeCertificateHolder) obj).getEncoded();
        } else if (obj instanceof PKCS10CertificationRequest) {
            str = "CERTIFICATE REQUEST";
            encoded = ((PKCS10CertificationRequest) obj).getEncoded();
        } else if (obj instanceof ContentInfo) {
            str = "PKCS7";
            encoded = ((ContentInfo) obj).getEncoded();
        } else {
            throw new PemGenerationException("unknown object passed - can't encode.");
        }
        if (this.encryptor == null) {
            return new PemObject(str, encoded);
        }
        String toUpperCase = Strings.toUpperCase(this.encryptor.getAlgorithm());
        if (toUpperCase.equals("DESEDE")) {
            toUpperCase = "DES-EDE3-CBC";
        }
        byte[] iv = this.encryptor.getIV();
        encoded = this.encryptor.encrypt(encoded);
        ArrayList arrayList = new ArrayList(2);
        arrayList.add(new PemHeader("Proc-Type", "4,ENCRYPTED"));
        arrayList.add(new PemHeader("DEK-Info", toUpperCase + "," + getHexEncoded(iv)));
        return new PemObject(str, arrayList, encoded);
    }

    private String getHexEncoded(byte[] bArr) throws IOException {
        char[] cArr = new char[(bArr.length * 2)];
        for (int i = 0; i != bArr.length; i++) {
            int i2 = bArr[i] & UnsignedUtils.MAX_UBYTE;
            cArr[i * 2] = (char) hexEncodingTable[i2 >>> 4];
            cArr[(i * 2) + 1] = (char) hexEncodingTable[i2 & 15];
        }
        return new String(cArr);
    }

    public PemObject generate() throws PemGenerationException {
        try {
            return createPemObject(this.obj);
        } catch (IOException e) {
            throw new PemGenerationException("encoding exception: " + e.getMessage(), e);
        }
    }
}
