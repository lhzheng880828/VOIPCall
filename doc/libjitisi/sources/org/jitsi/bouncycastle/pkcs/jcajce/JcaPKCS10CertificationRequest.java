package org.jitsi.bouncycastle.pkcs.jcajce;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Hashtable;
import org.jitsi.bouncycastle.asn1.pkcs.CertificationRequest;
import org.jitsi.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.jitsi.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.jitsi.bouncycastle.jcajce.DefaultJcaJceHelper;
import org.jitsi.bouncycastle.jcajce.JcaJceHelper;
import org.jitsi.bouncycastle.jcajce.NamedJcaJceHelper;
import org.jitsi.bouncycastle.jcajce.ProviderJcaJceHelper;
import org.jitsi.bouncycastle.pkcs.PKCS10CertificationRequest;

public class JcaPKCS10CertificationRequest extends PKCS10CertificationRequest {
    private static Hashtable keyAlgorithms = new Hashtable();
    private JcaJceHelper helper = new DefaultJcaJceHelper();

    static {
        keyAlgorithms.put(PKCSObjectIdentifiers.rsaEncryption, "RSA");
        keyAlgorithms.put(X9ObjectIdentifiers.id_dsa, "DSA");
    }

    public JcaPKCS10CertificationRequest(CertificationRequest certificationRequest) {
        super(certificationRequest);
    }

    public JcaPKCS10CertificationRequest(PKCS10CertificationRequest pKCS10CertificationRequest) {
        super(pKCS10CertificationRequest.toASN1Structure());
    }

    public JcaPKCS10CertificationRequest(byte[] bArr) throws IOException {
        super(bArr);
    }

    public PublicKey getPublicKey() throws InvalidKeyException, NoSuchAlgorithmException {
        X509EncodedKeySpec x509EncodedKeySpec;
        KeyFactory createKeyFactory;
        SubjectPublicKeyInfo subjectPublicKeyInfo;
        try {
            subjectPublicKeyInfo = getSubjectPublicKeyInfo();
            x509EncodedKeySpec = new X509EncodedKeySpec(subjectPublicKeyInfo.getEncoded());
            createKeyFactory = this.helper.createKeyFactory(subjectPublicKeyInfo.getAlgorithm().getAlgorithm().getId());
        } catch (NoSuchAlgorithmException e) {
            if (keyAlgorithms.get(subjectPublicKeyInfo.getAlgorithm().getAlgorithm()) != null) {
                createKeyFactory = this.helper.createKeyFactory((String) keyAlgorithms.get(subjectPublicKeyInfo.getAlgorithm().getAlgorithm()));
            } else {
                throw e;
            }
        } catch (InvalidKeySpecException e2) {
            throw new InvalidKeyException("error decoding public key");
        } catch (IOException e3) {
            throw new InvalidKeyException("error extracting key encoding");
        } catch (NoSuchProviderException e4) {
            throw new NoSuchAlgorithmException("cannot find provider: " + e4.getMessage());
        }
        return createKeyFactory.generatePublic(x509EncodedKeySpec);
    }

    public JcaPKCS10CertificationRequest setProvider(String str) {
        this.helper = new NamedJcaJceHelper(str);
        return this;
    }

    public JcaPKCS10CertificationRequest setProvider(Provider provider) {
        this.helper = new ProviderJcaJceHelper(provider);
        return this;
    }
}
