package org.jitsi.bouncycastle.openssl;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import org.jitsi.bouncycastle.asn1.ASN1InputStream;
import org.jitsi.bouncycastle.asn1.ASN1Integer;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.ASN1Primitive;
import org.jitsi.bouncycastle.asn1.ASN1Sequence;
import org.jitsi.bouncycastle.asn1.DERNull;
import org.jitsi.bouncycastle.asn1.cms.ContentInfo;
import org.jitsi.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo;
import org.jitsi.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.jitsi.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.jitsi.bouncycastle.asn1.pkcs.RSAPublicKey;
import org.jitsi.bouncycastle.asn1.sec.ECPrivateKey;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.asn1.x509.DSAParameter;
import org.jitsi.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.jitsi.bouncycastle.asn1.x9.X9ECParameters;
import org.jitsi.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.jitsi.bouncycastle.cert.X509AttributeCertificateHolder;
import org.jitsi.bouncycastle.cert.X509CRLHolder;
import org.jitsi.bouncycastle.cert.X509CertificateHolder;
import org.jitsi.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.jitsi.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.jitsi.bouncycastle.util.encoders.Hex;
import org.jitsi.bouncycastle.util.io.pem.PemHeader;
import org.jitsi.bouncycastle.util.io.pem.PemObject;
import org.jitsi.bouncycastle.util.io.pem.PemObjectParser;
import org.jitsi.bouncycastle.util.io.pem.PemReader;

public class PEMParser extends PemReader {
    private final Map parsers = new HashMap();

    private class DSAKeyPairParser implements PEMKeyPairParser {
        private DSAKeyPairParser() {
        }

        public PEMKeyPair parse(byte[] bArr) throws IOException {
            try {
                ASN1Sequence instance = ASN1Sequence.getInstance(bArr);
                if (instance.size() != 6) {
                    throw new PEMException("malformed sequence in DSA private key");
                }
                ASN1Integer instance2 = ASN1Integer.getInstance(instance.getObjectAt(1));
                ASN1Integer instance3 = ASN1Integer.getInstance(instance.getObjectAt(2));
                ASN1Integer instance4 = ASN1Integer.getInstance(instance.getObjectAt(3));
                return new PEMKeyPair(new SubjectPublicKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_dsa, new DSAParameter(instance2.getValue(), instance3.getValue(), instance4.getValue())), ASN1Integer.getInstance(instance.getObjectAt(4))), new PrivateKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_dsa, new DSAParameter(instance2.getValue(), instance3.getValue(), instance4.getValue())), ASN1Integer.getInstance(instance.getObjectAt(5))));
            } catch (IOException e) {
                throw e;
            } catch (Exception e2) {
                throw new PEMException("problem creating DSA private key: " + e2.toString(), e2);
            }
        }
    }

    private class ECCurveParamsParser implements PemObjectParser {
        private ECCurveParamsParser() {
        }

        public Object parseObject(PemObject pemObject) throws IOException {
            try {
                ASN1Primitive fromByteArray = ASN1Primitive.fromByteArray(pemObject.getContent());
                return fromByteArray instanceof ASN1ObjectIdentifier ? ASN1Primitive.fromByteArray(pemObject.getContent()) : fromByteArray instanceof ASN1Sequence ? X9ECParameters.getInstance(fromByteArray) : null;
            } catch (IOException e) {
                throw e;
            } catch (Exception e2) {
                throw new PEMException("exception extracting EC named curve: " + e2.toString());
            }
        }
    }

    private class ECDSAKeyPairParser implements PEMKeyPairParser {
        private ECDSAKeyPairParser() {
        }

        public PEMKeyPair parse(byte[] bArr) throws IOException {
            try {
                ECPrivateKey instance = ECPrivateKey.getInstance(ASN1Sequence.getInstance(bArr));
                AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, instance.getParameters());
                return new PEMKeyPair(new SubjectPublicKeyInfo(algorithmIdentifier, instance.getPublicKey().getBytes()), new PrivateKeyInfo(algorithmIdentifier, instance));
            } catch (IOException e) {
                throw e;
            } catch (Exception e2) {
                throw new PEMException("problem creating EC private key: " + e2.toString(), e2);
            }
        }
    }

    private class EncryptedPrivateKeyParser implements PemObjectParser {
        public Object parseObject(PemObject pemObject) throws IOException {
            try {
                return new PKCS8EncryptedPrivateKeyInfo(EncryptedPrivateKeyInfo.getInstance(pemObject.getContent()));
            } catch (Exception e) {
                throw new PEMException("problem parsing ENCRYPTED PRIVATE KEY: " + e.toString(), e);
            }
        }
    }

    private class KeyPairParser implements PemObjectParser {
        private final PEMKeyPairParser pemKeyPairParser;

        public KeyPairParser(PEMKeyPairParser pEMKeyPairParser) {
            this.pemKeyPairParser = pEMKeyPairParser;
        }

        public Object parseObject(PemObject pemObject) throws IOException {
            Object obj = null;
            String str = null;
            for (PemHeader pemHeader : pemObject.getHeaders()) {
                String str2;
                if (pemHeader.getName().equals("Proc-Type") && pemHeader.getValue().equals("4,ENCRYPTED")) {
                    obj = 1;
                    str2 = str;
                } else {
                    str2 = pemHeader.getName().equals("DEK-Info") ? pemHeader.getValue() : str;
                }
                str = str2;
            }
            byte[] content = pemObject.getContent();
            if (obj == null) {
                return this.pemKeyPairParser.parse(content);
            }
            try {
                StringTokenizer stringTokenizer = new StringTokenizer(str, ",");
                return new PEMEncryptedKeyPair(stringTokenizer.nextToken(), Hex.decode(stringTokenizer.nextToken()), content, this.pemKeyPairParser);
            } catch (IOException e) {
                if (obj != null) {
                    throw new PEMException("exception decoding - please check password and data.", e);
                }
                throw new PEMException(e.getMessage(), e);
            } catch (IllegalArgumentException e2) {
                if (obj != null) {
                    throw new PEMException("exception decoding - please check password and data.", e2);
                }
                throw new PEMException(e2.getMessage(), e2);
            }
        }
    }

    private class PKCS10CertificationRequestParser implements PemObjectParser {
        private PKCS10CertificationRequestParser() {
        }

        public Object parseObject(PemObject pemObject) throws IOException {
            try {
                return new PKCS10CertificationRequest(pemObject.getContent());
            } catch (Exception e) {
                throw new PEMException("problem parsing certrequest: " + e.toString(), e);
            }
        }
    }

    private class PKCS7Parser implements PemObjectParser {
        private PKCS7Parser() {
        }

        public Object parseObject(PemObject pemObject) throws IOException {
            try {
                return ContentInfo.getInstance(new ASN1InputStream(pemObject.getContent()).readObject());
            } catch (Exception e) {
                throw new PEMException("problem parsing PKCS7 object: " + e.toString(), e);
            }
        }
    }

    private class PrivateKeyParser implements PemObjectParser {
        public Object parseObject(PemObject pemObject) throws IOException {
            try {
                return PrivateKeyInfo.getInstance(pemObject.getContent());
            } catch (Exception e) {
                throw new PEMException("problem parsing PRIVATE KEY: " + e.toString(), e);
            }
        }
    }

    private class PublicKeyParser implements PemObjectParser {
        public Object parseObject(PemObject pemObject) throws IOException {
            return SubjectPublicKeyInfo.getInstance(pemObject.getContent());
        }
    }

    private class RSAKeyPairParser implements PEMKeyPairParser {
        private RSAKeyPairParser() {
        }

        public PEMKeyPair parse(byte[] bArr) throws IOException {
            try {
                ASN1Sequence instance = ASN1Sequence.getInstance(bArr);
                if (instance.size() != 9) {
                    throw new PEMException("malformed sequence in RSA private key");
                }
                RSAPrivateKey instance2 = RSAPrivateKey.getInstance(instance);
                RSAPublicKey rSAPublicKey = new RSAPublicKey(instance2.getModulus(), instance2.getPublicExponent());
                AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE);
                return new PEMKeyPair(new SubjectPublicKeyInfo(algorithmIdentifier, rSAPublicKey), new PrivateKeyInfo(algorithmIdentifier, instance2));
            } catch (IOException e) {
                throw e;
            } catch (Exception e2) {
                throw new PEMException("problem creating RSA private key: " + e2.toString(), e2);
            }
        }
    }

    private class RSAPublicKeyParser implements PemObjectParser {
        public Object parseObject(PemObject pemObject) throws IOException {
            try {
                return new SubjectPublicKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE), RSAPublicKey.getInstance(pemObject.getContent()));
            } catch (IOException e) {
                throw e;
            } catch (Exception e2) {
                throw new PEMException("problem extracting key: " + e2.toString(), e2);
            }
        }
    }

    private class X509AttributeCertificateParser implements PemObjectParser {
        private X509AttributeCertificateParser() {
        }

        public Object parseObject(PemObject pemObject) throws IOException {
            return new X509AttributeCertificateHolder(pemObject.getContent());
        }
    }

    private class X509CRLParser implements PemObjectParser {
        private X509CRLParser() {
        }

        public Object parseObject(PemObject pemObject) throws IOException {
            try {
                return new X509CRLHolder(pemObject.getContent());
            } catch (Exception e) {
                throw new PEMException("problem parsing cert: " + e.toString(), e);
            }
        }
    }

    private class X509CertificateParser implements PemObjectParser {
        private X509CertificateParser() {
        }

        public Object parseObject(PemObject pemObject) throws IOException {
            try {
                return new X509CertificateHolder(pemObject.getContent());
            } catch (Exception e) {
                throw new PEMException("problem parsing cert: " + e.toString(), e);
            }
        }
    }

    public PEMParser(Reader reader) {
        super(reader);
        this.parsers.put("CERTIFICATE REQUEST", new PKCS10CertificationRequestParser());
        this.parsers.put("NEW CERTIFICATE REQUEST", new PKCS10CertificationRequestParser());
        this.parsers.put("CERTIFICATE", new X509CertificateParser());
        this.parsers.put("X509 CERTIFICATE", new X509CertificateParser());
        this.parsers.put("X509 CRL", new X509CRLParser());
        this.parsers.put("PKCS7", new PKCS7Parser());
        this.parsers.put("ATTRIBUTE CERTIFICATE", new X509AttributeCertificateParser());
        this.parsers.put("EC PARAMETERS", new ECCurveParamsParser());
        this.parsers.put("PUBLIC KEY", new PublicKeyParser());
        this.parsers.put("RSA PUBLIC KEY", new RSAPublicKeyParser());
        this.parsers.put("RSA PRIVATE KEY", new KeyPairParser(new RSAKeyPairParser()));
        this.parsers.put("DSA PRIVATE KEY", new KeyPairParser(new DSAKeyPairParser()));
        this.parsers.put("EC PRIVATE KEY", new KeyPairParser(new ECDSAKeyPairParser()));
        this.parsers.put("ENCRYPTED PRIVATE KEY", new EncryptedPrivateKeyParser());
        this.parsers.put("PRIVATE KEY", new PrivateKeyParser());
    }

    public Object readObject() throws IOException {
        PemObject readPemObject = readPemObject();
        if (readPemObject == null) {
            return null;
        }
        String type = readPemObject.getType();
        if (this.parsers.containsKey(type)) {
            return ((PemObjectParser) this.parsers.get(type)).parseObject(readPemObject);
        }
        throw new IOException("unrecognised object: " + type);
    }
}
