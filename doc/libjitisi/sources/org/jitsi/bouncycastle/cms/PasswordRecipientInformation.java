package org.jitsi.bouncycastle.cms;

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.util.HashMap;
import java.util.Map;
import org.jitsi.bouncycastle.asn1.ASN1Encodable;
import org.jitsi.bouncycastle.asn1.cms.PasswordRecipientInfo;
import org.jitsi.bouncycastle.asn1.pkcs.PBKDF2Params;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.cms.jcajce.JceAlgorithmIdentifierConverter;
import org.jitsi.bouncycastle.cms.jcajce.JcePasswordAuthenticatedRecipient;
import org.jitsi.bouncycastle.cms.jcajce.JcePasswordEnvelopedRecipient;
import org.jitsi.bouncycastle.cms.jcajce.JcePasswordRecipient;
import org.jitsi.bouncycastle.crypto.PBEParametersGenerator;
import org.jitsi.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.jitsi.bouncycastle.crypto.params.KeyParameter;
import org.jitsi.bouncycastle.util.Integers;

public class PasswordRecipientInformation extends RecipientInformation {
    static Map BLOCKSIZES = new HashMap();
    static Map KEYSIZES = new HashMap();
    private PasswordRecipientInfo info;

    static {
        BLOCKSIZES.put(CMSAlgorithm.DES_EDE3_CBC, Integers.valueOf(8));
        BLOCKSIZES.put(CMSAlgorithm.AES128_CBC, Integers.valueOf(16));
        BLOCKSIZES.put(CMSAlgorithm.AES192_CBC, Integers.valueOf(16));
        BLOCKSIZES.put(CMSAlgorithm.AES256_CBC, Integers.valueOf(16));
        KEYSIZES.put(CMSAlgorithm.DES_EDE3_CBC, Integers.valueOf(192));
        KEYSIZES.put(CMSAlgorithm.AES128_CBC, Integers.valueOf(128));
        KEYSIZES.put(CMSAlgorithm.AES192_CBC, Integers.valueOf(192));
        KEYSIZES.put(CMSAlgorithm.AES256_CBC, Integers.valueOf(256));
    }

    PasswordRecipientInformation(PasswordRecipientInfo passwordRecipientInfo, AlgorithmIdentifier algorithmIdentifier, CMSSecureReadable cMSSecureReadable, AuthAttributesProvider authAttributesProvider) {
        super(passwordRecipientInfo.getKeyEncryptionAlgorithm(), algorithmIdentifier, cMSSecureReadable, authAttributesProvider);
        this.info = passwordRecipientInfo;
        this.rid = new PasswordRecipientId();
    }

    public CMSTypedStream getContentStream(Key key, String str) throws CMSException, NoSuchProviderException {
        return getContentStream(key, CMSUtils.getProvider(str));
    }

    public CMSTypedStream getContentStream(Key key, Provider provider) throws CMSException {
        try {
            CMSPBEKey cMSPBEKey = (CMSPBEKey) key;
            JcePasswordRecipient jcePasswordEnvelopedRecipient = this.secureReadable instanceof CMSEnvelopedSecureReadable ? new JcePasswordEnvelopedRecipient(cMSPBEKey.getPassword()) : new JcePasswordAuthenticatedRecipient(cMSPBEKey.getPassword());
            jcePasswordEnvelopedRecipient.setPasswordConversionScheme(cMSPBEKey instanceof PKCS5Scheme2UTF8PBEKey ? 1 : 0);
            if (provider != null) {
                jcePasswordEnvelopedRecipient.setProvider(provider);
            }
            return getContentStream(jcePasswordEnvelopedRecipient);
        } catch (IOException e) {
            throw new CMSException("encoding error: " + e.getMessage(), e);
        }
    }

    public String getKeyDerivationAlgOID() {
        return this.info.getKeyDerivationAlgorithm() != null ? this.info.getKeyDerivationAlgorithm().getAlgorithm().getId() : null;
    }

    public AlgorithmParameters getKeyDerivationAlgParameters(String str) throws NoSuchProviderException {
        return getKeyDerivationAlgParameters(CMSUtils.getProvider(str));
    }

    public AlgorithmParameters getKeyDerivationAlgParameters(Provider provider) {
        try {
            return new JceAlgorithmIdentifierConverter().setProvider(provider).getAlgorithmParameters(this.info.getKeyDerivationAlgorithm());
        } catch (Exception e) {
            throw new RuntimeException("exception getting encryption parameters " + e);
        }
    }

    public byte[] getKeyDerivationAlgParams() {
        try {
            if (this.info.getKeyDerivationAlgorithm() != null) {
                ASN1Encodable parameters = this.info.getKeyDerivationAlgorithm().getParameters();
                if (parameters != null) {
                    return parameters.toASN1Primitive().getEncoded();
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("exception getting encryption parameters " + e);
        }
    }

    public AlgorithmIdentifier getKeyDerivationAlgorithm() {
        return this.info.getKeyDerivationAlgorithm();
    }

    /* access modifiers changed from: protected */
    public byte[] getPasswordBytes(int i, char[] cArr) {
        return i == 0 ? PBEParametersGenerator.PKCS5PasswordToBytes(cArr) : PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(cArr);
    }

    /* access modifiers changed from: protected */
    public RecipientOperator getRecipientOperator(Recipient recipient) throws CMSException, IOException {
        PasswordRecipient passwordRecipient = (PasswordRecipient) recipient;
        AlgorithmIdentifier instance = AlgorithmIdentifier.getInstance(AlgorithmIdentifier.getInstance(this.info.getKeyEncryptionAlgorithm()).getParameters());
        byte[] passwordBytes = getPasswordBytes(passwordRecipient.getPasswordConversionScheme(), passwordRecipient.getPassword());
        PBKDF2Params instance2 = PBKDF2Params.getInstance(this.info.getKeyDerivationAlgorithm().getParameters());
        PKCS5S2ParametersGenerator pKCS5S2ParametersGenerator = new PKCS5S2ParametersGenerator();
        pKCS5S2ParametersGenerator.init(passwordBytes, instance2.getSalt(), instance2.getIterationCount().intValue());
        return passwordRecipient.getRecipientOperator(instance, this.messageAlgorithm, ((KeyParameter) pKCS5S2ParametersGenerator.generateDerivedParameters(((Integer) KEYSIZES.get(instance.getAlgorithm())).intValue())).getKey(), this.info.getEncryptedKey().getOctets());
    }
}
