package org.jitsi.bouncycastle.cert.cmp;

import java.io.IOException;
import java.io.OutputStream;
import org.jitsi.bouncycastle.asn1.ASN1EncodableVector;
import org.jitsi.bouncycastle.asn1.DERSequence;
import org.jitsi.bouncycastle.asn1.cmp.CMPCertificate;
import org.jitsi.bouncycastle.asn1.cmp.CMPObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.cmp.PBMParameter;
import org.jitsi.bouncycastle.asn1.cmp.PKIBody;
import org.jitsi.bouncycastle.asn1.cmp.PKIHeader;
import org.jitsi.bouncycastle.asn1.cmp.PKIMessage;
import org.jitsi.bouncycastle.cert.X509CertificateHolder;
import org.jitsi.bouncycastle.cert.crmf.PKMACBuilder;
import org.jitsi.bouncycastle.operator.ContentVerifier;
import org.jitsi.bouncycastle.operator.ContentVerifierProvider;
import org.jitsi.bouncycastle.operator.MacCalculator;
import org.jitsi.bouncycastle.util.Arrays;

public class ProtectedPKIMessage {
    private PKIMessage pkiMessage;

    ProtectedPKIMessage(PKIMessage pKIMessage) {
        if (pKIMessage.getHeader().getProtectionAlg() == null) {
            throw new IllegalArgumentException("PKIMessage not protected");
        }
        this.pkiMessage = pKIMessage;
    }

    public ProtectedPKIMessage(GeneralPKIMessage generalPKIMessage) {
        if (generalPKIMessage.hasProtection()) {
            this.pkiMessage = generalPKIMessage.toASN1Structure();
            return;
        }
        throw new IllegalArgumentException("PKIMessage not protected");
    }

    private boolean verifySignature(byte[] bArr, ContentVerifier contentVerifier) throws IOException {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.pkiMessage.getHeader());
        aSN1EncodableVector.add(this.pkiMessage.getBody());
        OutputStream outputStream = contentVerifier.getOutputStream();
        outputStream.write(new DERSequence(aSN1EncodableVector).getEncoded("DER"));
        outputStream.close();
        return contentVerifier.verify(bArr);
    }

    public PKIBody getBody() {
        return this.pkiMessage.getBody();
    }

    public X509CertificateHolder[] getCertificates() {
        int i = 0;
        CMPCertificate[] extraCerts = this.pkiMessage.getExtraCerts();
        if (extraCerts == null) {
            return new X509CertificateHolder[0];
        }
        X509CertificateHolder[] x509CertificateHolderArr = new X509CertificateHolder[extraCerts.length];
        while (i != extraCerts.length) {
            x509CertificateHolderArr[i] = new X509CertificateHolder(extraCerts[i].getX509v3PKCert());
            i++;
        }
        return x509CertificateHolderArr;
    }

    public PKIHeader getHeader() {
        return this.pkiMessage.getHeader();
    }

    public boolean hasPasswordBasedMacProtection() {
        return this.pkiMessage.getHeader().getProtectionAlg().getAlgorithm().equals(CMPObjectIdentifiers.passwordBasedMac);
    }

    public PKIMessage toASN1Structure() {
        return this.pkiMessage;
    }

    public boolean verify(PKMACBuilder pKMACBuilder, char[] cArr) throws CMPException {
        if (CMPObjectIdentifiers.passwordBasedMac.equals(this.pkiMessage.getHeader().getProtectionAlg().getAlgorithm())) {
            try {
                pKMACBuilder.setParameters(PBMParameter.getInstance(this.pkiMessage.getHeader().getProtectionAlg().getParameters()));
                MacCalculator build = pKMACBuilder.build(cArr);
                OutputStream outputStream = build.getOutputStream();
                ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
                aSN1EncodableVector.add(this.pkiMessage.getHeader());
                aSN1EncodableVector.add(this.pkiMessage.getBody());
                outputStream.write(new DERSequence(aSN1EncodableVector).getEncoded("DER"));
                outputStream.close();
                return Arrays.areEqual(build.getMac(), this.pkiMessage.getProtection().getBytes());
            } catch (Exception e) {
                throw new CMPException("unable to verify MAC: " + e.getMessage(), e);
            }
        }
        throw new CMPException("protection algorithm not mac based");
    }

    public boolean verify(ContentVerifierProvider contentVerifierProvider) throws CMPException {
        try {
            return verifySignature(this.pkiMessage.getProtection().getBytes(), contentVerifierProvider.get(this.pkiMessage.getHeader().getProtectionAlg()));
        } catch (Exception e) {
            throw new CMPException("unable to verify signature: " + e.getMessage(), e);
        }
    }
}