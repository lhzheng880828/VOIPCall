package org.jitsi.bouncycastle.pkcs.bc;

import java.security.SecureRandom;
import org.jitsi.bouncycastle.asn1.DERNull;
import org.jitsi.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.crypto.ExtendedDigest;
import org.jitsi.bouncycastle.crypto.digests.SHA1Digest;
import org.jitsi.bouncycastle.operator.MacCalculator;
import org.jitsi.bouncycastle.pkcs.PKCS12MacCalculatorBuilder;

public class BcPKCS12MacCalculatorBuilder implements PKCS12MacCalculatorBuilder {
    private AlgorithmIdentifier algorithmIdentifier;
    private ExtendedDigest digest;
    private int iterationCount;
    private SecureRandom random;
    private int saltLength;

    public BcPKCS12MacCalculatorBuilder() {
        this(new SHA1Digest(), new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1, DERNull.INSTANCE));
    }

    public BcPKCS12MacCalculatorBuilder(ExtendedDigest extendedDigest, AlgorithmIdentifier algorithmIdentifier) {
        this.iterationCount = 1024;
        this.digest = extendedDigest;
        this.algorithmIdentifier = algorithmIdentifier;
        this.saltLength = extendedDigest.getDigestSize();
    }

    public MacCalculator build(char[] cArr) {
        if (this.random == null) {
            this.random = new SecureRandom();
        }
        byte[] bArr = new byte[this.saltLength];
        this.random.nextBytes(bArr);
        return PKCS12PBEUtils.createMacCalculator(this.algorithmIdentifier.getAlgorithm(), this.digest, new PKCS12PBEParams(bArr, this.iterationCount), cArr);
    }

    public AlgorithmIdentifier getDigestAlgorithmIdentifier() {
        return this.algorithmIdentifier;
    }
}
