package org.jitsi.bouncycastle.pkcs.jcajce;

import java.io.OutputStream;
import java.security.Provider;
import java.security.SecureRandom;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.DERNull;
import org.jitsi.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.crypto.ExtendedDigest;
import org.jitsi.bouncycastle.crypto.generators.PKCS12ParametersGenerator;
import org.jitsi.bouncycastle.jcajce.DefaultJcaJceHelper;
import org.jitsi.bouncycastle.jcajce.JcaJceHelper;
import org.jitsi.bouncycastle.jcajce.NamedJcaJceHelper;
import org.jitsi.bouncycastle.jcajce.ProviderJcaJceHelper;
import org.jitsi.bouncycastle.jcajce.io.MacOutputStream;
import org.jitsi.bouncycastle.operator.GenericKey;
import org.jitsi.bouncycastle.operator.MacCalculator;
import org.jitsi.bouncycastle.operator.OperatorCreationException;
import org.jitsi.bouncycastle.pkcs.PKCS12MacCalculatorBuilder;

public class JcePKCS12MacCalculatorBuilder implements PKCS12MacCalculatorBuilder {
    /* access modifiers changed from: private */
    public ASN1ObjectIdentifier algorithm;
    private ExtendedDigest digest;
    private JcaJceHelper helper;
    /* access modifiers changed from: private */
    public int iterationCount;
    private SecureRandom random;
    private int saltLength;

    public JcePKCS12MacCalculatorBuilder() {
        this(OIWObjectIdentifiers.idSHA1);
    }

    public JcePKCS12MacCalculatorBuilder(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        this.helper = new DefaultJcaJceHelper();
        this.iterationCount = 1024;
        this.algorithm = aSN1ObjectIdentifier;
    }

    public MacCalculator build(final char[] cArr) throws OperatorCreationException {
        if (this.random == null) {
            this.random = new SecureRandom();
        }
        try {
            final Mac createMac = this.helper.createMac(this.algorithm.getId());
            this.saltLength = createMac.getMacLength();
            final byte[] bArr = new byte[this.saltLength];
            this.random.nextBytes(bArr);
            SecretKeyFactory createSecretKeyFactory = this.helper.createSecretKeyFactory(this.algorithm.getId());
            createMac.init(createSecretKeyFactory.generateSecret(new PBEKeySpec(cArr)), new PBEParameterSpec(bArr, this.iterationCount));
            return new MacCalculator() {
                public AlgorithmIdentifier getAlgorithmIdentifier() {
                    return new AlgorithmIdentifier(JcePKCS12MacCalculatorBuilder.this.algorithm, new PKCS12PBEParams(bArr, JcePKCS12MacCalculatorBuilder.this.iterationCount));
                }

                public GenericKey getKey() {
                    return new GenericKey(getAlgorithmIdentifier(), PKCS12ParametersGenerator.PKCS12PasswordToBytes(cArr));
                }

                public byte[] getMac() {
                    return createMac.doFinal();
                }

                public OutputStream getOutputStream() {
                    return new MacOutputStream(createMac);
                }
            };
        } catch (Exception e) {
            throw new OperatorCreationException("unable to create MAC calculator: " + e.getMessage(), e);
        }
    }

    public AlgorithmIdentifier getDigestAlgorithmIdentifier() {
        return new AlgorithmIdentifier(this.algorithm, DERNull.INSTANCE);
    }

    public JcePKCS12MacCalculatorBuilder setProvider(String str) {
        this.helper = new NamedJcaJceHelper(str);
        return this;
    }

    public JcePKCS12MacCalculatorBuilder setProvider(Provider provider) {
        this.helper = new ProviderJcaJceHelper(provider);
        return this;
    }
}
