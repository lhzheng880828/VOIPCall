package org.jitsi.bouncycastle.cms.bc;

import org.jitsi.bouncycastle.cert.X509CertificateHolder;
import org.jitsi.bouncycastle.cms.CMSSignatureAlgorithmNameGenerator;
import org.jitsi.bouncycastle.cms.SignerInformationVerifier;
import org.jitsi.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.jitsi.bouncycastle.operator.DigestAlgorithmIdentifierFinder;
import org.jitsi.bouncycastle.operator.DigestCalculatorProvider;
import org.jitsi.bouncycastle.operator.OperatorCreationException;
import org.jitsi.bouncycastle.operator.SignatureAlgorithmIdentifierFinder;
import org.jitsi.bouncycastle.operator.bc.BcRSAContentVerifierProviderBuilder;

public class BcRSASignerInfoVerifierBuilder {
    private BcRSAContentVerifierProviderBuilder contentVerifierProviderBuilder;
    private DigestCalculatorProvider digestCalculatorProvider;
    private SignatureAlgorithmIdentifierFinder sigAlgIdFinder;
    private CMSSignatureAlgorithmNameGenerator sigAlgNameGen;

    public BcRSASignerInfoVerifierBuilder(CMSSignatureAlgorithmNameGenerator cMSSignatureAlgorithmNameGenerator, SignatureAlgorithmIdentifierFinder signatureAlgorithmIdentifierFinder, DigestAlgorithmIdentifierFinder digestAlgorithmIdentifierFinder, DigestCalculatorProvider digestCalculatorProvider) {
        this.sigAlgNameGen = cMSSignatureAlgorithmNameGenerator;
        this.sigAlgIdFinder = signatureAlgorithmIdentifierFinder;
        this.contentVerifierProviderBuilder = new BcRSAContentVerifierProviderBuilder(digestAlgorithmIdentifierFinder);
        this.digestCalculatorProvider = digestCalculatorProvider;
    }

    public SignerInformationVerifier build(X509CertificateHolder x509CertificateHolder) throws OperatorCreationException {
        return new SignerInformationVerifier(this.sigAlgNameGen, this.sigAlgIdFinder, this.contentVerifierProviderBuilder.build(x509CertificateHolder), this.digestCalculatorProvider);
    }

    public SignerInformationVerifier build(AsymmetricKeyParameter asymmetricKeyParameter) throws OperatorCreationException {
        return new SignerInformationVerifier(this.sigAlgNameGen, this.sigAlgIdFinder, this.contentVerifierProviderBuilder.build(asymmetricKeyParameter), this.digestCalculatorProvider);
    }
}
