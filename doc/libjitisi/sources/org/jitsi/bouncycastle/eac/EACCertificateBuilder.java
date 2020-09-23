package org.jitsi.bouncycastle.eac;

import java.io.OutputStream;
import org.jitsi.bouncycastle.asn1.DERApplicationSpecific;
import org.jitsi.bouncycastle.asn1.eac.CVCertificate;
import org.jitsi.bouncycastle.asn1.eac.CertificateBody;
import org.jitsi.bouncycastle.asn1.eac.CertificateHolderAuthorization;
import org.jitsi.bouncycastle.asn1.eac.CertificateHolderReference;
import org.jitsi.bouncycastle.asn1.eac.CertificationAuthorityReference;
import org.jitsi.bouncycastle.asn1.eac.PackedDate;
import org.jitsi.bouncycastle.asn1.eac.PublicKeyDataObject;
import org.jitsi.bouncycastle.eac.operator.EACSigner;

public class EACCertificateBuilder {
    private static final byte[] ZeroArray = new byte[]{(byte) 0};
    private PackedDate certificateEffectiveDate;
    private PackedDate certificateExpirationDate;
    private CertificateHolderAuthorization certificateHolderAuthorization;
    private CertificateHolderReference certificateHolderReference;
    private CertificationAuthorityReference certificationAuthorityReference;
    private PublicKeyDataObject publicKey;

    public EACCertificateBuilder(CertificationAuthorityReference certificationAuthorityReference, PublicKeyDataObject publicKeyDataObject, CertificateHolderReference certificateHolderReference, CertificateHolderAuthorization certificateHolderAuthorization, PackedDate packedDate, PackedDate packedDate2) {
        this.certificationAuthorityReference = certificationAuthorityReference;
        this.publicKey = publicKeyDataObject;
        this.certificateHolderReference = certificateHolderReference;
        this.certificateHolderAuthorization = certificateHolderAuthorization;
        this.certificateEffectiveDate = packedDate;
        this.certificateExpirationDate = packedDate2;
    }

    private CertificateBody buildBody() {
        return new CertificateBody(new DERApplicationSpecific(41, ZeroArray), this.certificationAuthorityReference, this.publicKey, this.certificateHolderReference, this.certificateHolderAuthorization, this.certificateEffectiveDate, this.certificateExpirationDate);
    }

    public EACCertificateHolder build(EACSigner eACSigner) throws EACException {
        try {
            CertificateBody buildBody = buildBody();
            OutputStream outputStream = eACSigner.getOutputStream();
            outputStream.write(buildBody.getEncoded("DER"));
            outputStream.close();
            return new EACCertificateHolder(new CVCertificate(buildBody, eACSigner.getSignature()));
        } catch (Exception e) {
            throw new EACException("unable to process signature: " + e.getMessage(), e);
        }
    }
}
