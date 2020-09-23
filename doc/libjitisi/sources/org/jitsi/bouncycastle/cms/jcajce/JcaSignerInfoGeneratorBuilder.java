package org.jitsi.bouncycastle.cms.jcajce;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import org.jitsi.bouncycastle.cert.X509CertificateHolder;
import org.jitsi.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.jitsi.bouncycastle.cms.CMSAttributeTableGenerator;
import org.jitsi.bouncycastle.cms.SignerInfoGenerator;
import org.jitsi.bouncycastle.cms.SignerInfoGeneratorBuilder;
import org.jitsi.bouncycastle.operator.ContentSigner;
import org.jitsi.bouncycastle.operator.DigestCalculatorProvider;
import org.jitsi.bouncycastle.operator.OperatorCreationException;

public class JcaSignerInfoGeneratorBuilder {
    private SignerInfoGeneratorBuilder builder;

    public JcaSignerInfoGeneratorBuilder(DigestCalculatorProvider digestCalculatorProvider) {
        this.builder = new SignerInfoGeneratorBuilder(digestCalculatorProvider);
    }

    public SignerInfoGenerator build(ContentSigner contentSigner, X509Certificate x509Certificate) throws OperatorCreationException, CertificateEncodingException {
        return build(contentSigner, new JcaX509CertificateHolder(x509Certificate));
    }

    public SignerInfoGenerator build(ContentSigner contentSigner, X509CertificateHolder x509CertificateHolder) throws OperatorCreationException {
        return this.builder.build(contentSigner, x509CertificateHolder);
    }

    public SignerInfoGenerator build(ContentSigner contentSigner, byte[] bArr) throws OperatorCreationException {
        return this.builder.build(contentSigner, bArr);
    }

    public JcaSignerInfoGeneratorBuilder setDirectSignature(boolean z) {
        this.builder.setDirectSignature(z);
        return this;
    }

    public JcaSignerInfoGeneratorBuilder setSignedAttributeGenerator(CMSAttributeTableGenerator cMSAttributeTableGenerator) {
        this.builder.setSignedAttributeGenerator(cMSAttributeTableGenerator);
        return this;
    }

    public JcaSignerInfoGeneratorBuilder setUnsignedAttributeGenerator(CMSAttributeTableGenerator cMSAttributeTableGenerator) {
        this.builder.setUnsignedAttributeGenerator(cMSAttributeTableGenerator);
        return this;
    }
}
