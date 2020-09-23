package org.jitsi.bouncycastle.pkcs.bc;

import org.jitsi.bouncycastle.asn1.DERNull;
import org.jitsi.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.operator.MacCalculator;
import org.jitsi.bouncycastle.operator.OperatorCreationException;
import org.jitsi.bouncycastle.operator.bc.BcDigestProvider;
import org.jitsi.bouncycastle.pkcs.PKCS12MacCalculatorBuilder;
import org.jitsi.bouncycastle.pkcs.PKCS12MacCalculatorBuilderProvider;

public class BcPKCS12MacCalculatorBuilderProvider implements PKCS12MacCalculatorBuilderProvider {
    /* access modifiers changed from: private */
    public BcDigestProvider digestProvider;

    public BcPKCS12MacCalculatorBuilderProvider(BcDigestProvider bcDigestProvider) {
        this.digestProvider = bcDigestProvider;
    }

    public PKCS12MacCalculatorBuilder get(final AlgorithmIdentifier algorithmIdentifier) {
        return new PKCS12MacCalculatorBuilder() {
            public MacCalculator build(char[] cArr) throws OperatorCreationException {
                return PKCS12PBEUtils.createMacCalculator(algorithmIdentifier.getAlgorithm(), BcPKCS12MacCalculatorBuilderProvider.this.digestProvider.get(algorithmIdentifier), PKCS12PBEParams.getInstance(algorithmIdentifier.getParameters()), cArr);
            }

            public AlgorithmIdentifier getDigestAlgorithmIdentifier() {
                return new AlgorithmIdentifier(algorithmIdentifier.getAlgorithm(), DERNull.INSTANCE);
            }
        };
    }
}
