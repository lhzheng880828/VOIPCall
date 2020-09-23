package org.jitsi.bouncycastle.cms.jcajce;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.ASN1OctetString;
import org.jitsi.bouncycastle.asn1.ASN1Primitive;
import org.jitsi.bouncycastle.asn1.cms.ecc.MQVuserKeyingMaterial;
import org.jitsi.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.jitsi.bouncycastle.cms.CMSEnvelopedGenerator;
import org.jitsi.bouncycastle.cms.CMSException;
import org.jitsi.bouncycastle.cms.KeyAgreeRecipient;
import org.jitsi.bouncycastle.jce.spec.MQVPrivateKeySpec;
import org.jitsi.bouncycastle.jce.spec.MQVPublicKeySpec;

public abstract class JceKeyAgreeRecipient implements KeyAgreeRecipient {
    protected EnvelopedDataHelper contentHelper = this.helper;
    protected EnvelopedDataHelper helper = new EnvelopedDataHelper(new DefaultJcaJceExtHelper());
    private PrivateKey recipientKey;

    public JceKeyAgreeRecipient(PrivateKey privateKey) {
        this.recipientKey = privateKey;
    }

    private SecretKey calculateAgreedWrapKey(AlgorithmIdentifier algorithmIdentifier, ASN1ObjectIdentifier aSN1ObjectIdentifier, PublicKey publicKey, ASN1OctetString aSN1OctetString, PrivateKey privateKey) throws CMSException, GeneralSecurityException, IOException {
        Key privateKey2;
        Key publicKey2;
        if (algorithmIdentifier.getAlgorithm().getId().equals(CMSEnvelopedGenerator.ECMQV_SHA1KDF)) {
            MQVPublicKeySpec mQVPublicKeySpec = new MQVPublicKeySpec(publicKey2, this.helper.createKeyFactory(algorithmIdentifier.getAlgorithm()).generatePublic(new X509EncodedKeySpec(new SubjectPublicKeyInfo(getPrivateKeyAlgorithmIdentifier(), MQVuserKeyingMaterial.getInstance(ASN1Primitive.fromByteArray(aSN1OctetString.getOctets())).getEphemeralPublicKey().getPublicKey().getBytes()).getEncoded())));
            privateKey2 = new MQVPrivateKeySpec(privateKey2, privateKey2);
            publicKey2 = mQVPublicKeySpec;
        }
        KeyAgreement createKeyAgreement = this.helper.createKeyAgreement(algorithmIdentifier.getAlgorithm());
        createKeyAgreement.init(privateKey2);
        createKeyAgreement.doPhase(publicKey2, true);
        return createKeyAgreement.generateSecret(aSN1ObjectIdentifier.getId());
    }

    private Key unwrapSessionKey(ASN1ObjectIdentifier aSN1ObjectIdentifier, SecretKey secretKey, ASN1ObjectIdentifier aSN1ObjectIdentifier2, byte[] bArr) throws CMSException, InvalidKeyException, NoSuchAlgorithmException {
        Cipher createCipher = this.helper.createCipher(aSN1ObjectIdentifier);
        createCipher.init(4, secretKey);
        return createCipher.unwrap(bArr, this.helper.getBaseCipherName(aSN1ObjectIdentifier2), 3);
    }

    /* access modifiers changed from: protected */
    public Key extractSecretKey(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2, SubjectPublicKeyInfo subjectPublicKeyInfo, ASN1OctetString aSN1OctetString, byte[] bArr) throws CMSException {
        try {
            ASN1ObjectIdentifier algorithm = AlgorithmIdentifier.getInstance(algorithmIdentifier.getParameters()).getAlgorithm();
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(subjectPublicKeyInfo.getEncoded());
            return unwrapSessionKey(algorithm, calculateAgreedWrapKey(algorithmIdentifier, algorithm, this.helper.createKeyFactory(algorithmIdentifier.getAlgorithm()).generatePublic(x509EncodedKeySpec), aSN1OctetString, this.recipientKey), algorithmIdentifier2.getAlgorithm(), bArr);
        } catch (NoSuchAlgorithmException e) {
            throw new CMSException("can't find algorithm.", e);
        } catch (InvalidKeyException e2) {
            throw new CMSException("key invalid in message.", e2);
        } catch (InvalidKeySpecException e3) {
            throw new CMSException("originator key spec invalid.", e3);
        } catch (NoSuchPaddingException e4) {
            throw new CMSException("required padding not supported.", e4);
        } catch (Exception e5) {
            throw new CMSException("originator key invalid.", e5);
        }
    }

    public AlgorithmIdentifier getPrivateKeyAlgorithmIdentifier() {
        return PrivateKeyInfo.getInstance(this.recipientKey.getEncoded()).getPrivateKeyAlgorithm();
    }

    public JceKeyAgreeRecipient setContentProvider(String str) {
        this.contentHelper = CMSUtils.createContentHelper(str);
        return this;
    }

    public JceKeyAgreeRecipient setContentProvider(Provider provider) {
        this.contentHelper = CMSUtils.createContentHelper(provider);
        return this;
    }

    public JceKeyAgreeRecipient setProvider(String str) {
        this.helper = new EnvelopedDataHelper(new NamedJcaJceExtHelper(str));
        this.contentHelper = this.helper;
        return this;
    }

    public JceKeyAgreeRecipient setProvider(Provider provider) {
        this.helper = new EnvelopedDataHelper(new ProviderJcaJceExtHelper(provider));
        this.contentHelper = this.helper;
        return this;
    }
}
