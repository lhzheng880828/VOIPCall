package org.jitsi.bouncycastle.cms;

import org.jitsi.bouncycastle.asn1.DEROctetString;
import org.jitsi.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.jitsi.bouncycastle.asn1.cms.SignerIdentifier;
import org.jitsi.bouncycastle.cert.X509CertificateHolder;
import org.jitsi.bouncycastle.operator.ContentSigner;
import org.jitsi.bouncycastle.operator.DigestCalculatorProvider;
import org.jitsi.bouncycastle.operator.OperatorCreationException;

public class SignerInfoGeneratorBuilder {
    private DigestCalculatorProvider digestProvider;
    private boolean directSignature;
    private CMSSignatureEncryptionAlgorithmFinder sigEncAlgFinder;
    private CMSAttributeTableGenerator signedGen;
    private CMSAttributeTableGenerator unsignedGen;

    public SignerInfoGeneratorBuilder(DigestCalculatorProvider digestCalculatorProvider) {
        this(digestCalculatorProvider, new DefaultCMSSignatureEncryptionAlgorithmFinder());
    }

    public SignerInfoGeneratorBuilder(DigestCalculatorProvider digestCalculatorProvider, CMSSignatureEncryptionAlgorithmFinder cMSSignatureEncryptionAlgorithmFinder) {
        this.digestProvider = digestCalculatorProvider;
        this.sigEncAlgFinder = cMSSignatureEncryptionAlgorithmFinder;
    }

    private SignerInfoGenerator createGenerator(ContentSigner contentSigner, SignerIdentifier signerIdentifier) throws OperatorCreationException {
        if (this.directSignature) {
            return new SignerInfoGenerator(signerIdentifier, contentSigner, this.digestProvider, this.sigEncAlgFinder, true);
        } else if (this.signedGen == null && this.unsignedGen == null) {
            return new SignerInfoGenerator(signerIdentifier, contentSigner, this.digestProvider, this.sigEncAlgFinder);
        } else {
            if (this.signedGen == null) {
                this.signedGen = new DefaultSignedAttributeTableGenerator();
            }
            return new SignerInfoGenerator(signerIdentifier, contentSigner, this.digestProvider, this.sigEncAlgFinder, this.signedGen, this.unsignedGen);
        }
    }

    public SignerInfoGenerator build(ContentSigner contentSigner, X509CertificateHolder x509CertificateHolder) throws OperatorCreationException {
        SignerInfoGenerator createGenerator = createGenerator(contentSigner, new SignerIdentifier(new IssuerAndSerialNumber(x509CertificateHolder.toASN1Structure())));
        createGenerator.setAssociatedCertificate(x509CertificateHolder);
        return createGenerator;
    }

    public SignerInfoGenerator build(ContentSigner contentSigner, byte[] bArr) throws OperatorCreationException {
        return createGenerator(contentSigner, new SignerIdentifier(new DEROctetString(bArr)));
    }

    public SignerInfoGeneratorBuilder setDirectSignature(boolean z) {
        this.directSignature = z;
        return this;
    }

    public SignerInfoGeneratorBuilder setSignedAttributeGenerator(CMSAttributeTableGenerator cMSAttributeTableGenerator) {
        this.signedGen = cMSAttributeTableGenerator;
        return this;
    }

    public SignerInfoGeneratorBuilder setUnsignedAttributeGenerator(CMSAttributeTableGenerator cMSAttributeTableGenerator) {
        this.unsignedGen = cMSAttributeTableGenerator;
        return this;
    }
}
