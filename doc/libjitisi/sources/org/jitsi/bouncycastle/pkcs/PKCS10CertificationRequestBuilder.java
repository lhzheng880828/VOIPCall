package org.jitsi.bouncycastle.pkcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.jitsi.bouncycastle.asn1.ASN1Encodable;
import org.jitsi.bouncycastle.asn1.ASN1EncodableVector;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.DERBitString;
import org.jitsi.bouncycastle.asn1.DERSet;
import org.jitsi.bouncycastle.asn1.pkcs.Attribute;
import org.jitsi.bouncycastle.asn1.pkcs.CertificationRequest;
import org.jitsi.bouncycastle.asn1.pkcs.CertificationRequestInfo;
import org.jitsi.bouncycastle.asn1.x500.X500Name;
import org.jitsi.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.jitsi.bouncycastle.operator.ContentSigner;

public class PKCS10CertificationRequestBuilder {
    private List attributes = new ArrayList();
    private boolean leaveOffEmpty = false;
    private SubjectPublicKeyInfo publicKeyInfo;
    private X500Name subject;

    public PKCS10CertificationRequestBuilder(X500Name x500Name, SubjectPublicKeyInfo subjectPublicKeyInfo) {
        this.subject = x500Name;
        this.publicKeyInfo = subjectPublicKeyInfo;
    }

    public PKCS10CertificationRequestBuilder addAttribute(ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1Encodable aSN1Encodable) {
        this.attributes.add(new Attribute(aSN1ObjectIdentifier, new DERSet(aSN1Encodable)));
        return this;
    }

    public PKCS10CertificationRequestBuilder addAttribute(ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1Encodable[] aSN1EncodableArr) {
        this.attributes.add(new Attribute(aSN1ObjectIdentifier, new DERSet(aSN1EncodableArr)));
        return this;
    }

    public PKCS10CertificationRequest build(ContentSigner contentSigner) {
        CertificationRequestInfo certificationRequestInfo;
        if (this.attributes.isEmpty()) {
            certificationRequestInfo = this.leaveOffEmpty ? new CertificationRequestInfo(this.subject, this.publicKeyInfo, null) : new CertificationRequestInfo(this.subject, this.publicKeyInfo, new DERSet());
        } else {
            ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
            for (Object instance : this.attributes) {
                aSN1EncodableVector.add(Attribute.getInstance(instance));
            }
            certificationRequestInfo = new CertificationRequestInfo(this.subject, this.publicKeyInfo, new DERSet(aSN1EncodableVector));
        }
        try {
            OutputStream outputStream = contentSigner.getOutputStream();
            outputStream.write(certificationRequestInfo.getEncoded("DER"));
            outputStream.close();
            return new PKCS10CertificationRequest(new CertificationRequest(certificationRequestInfo, contentSigner.getAlgorithmIdentifier(), new DERBitString(contentSigner.getSignature())));
        } catch (IOException e) {
            throw new IllegalStateException("cannot produce certification request signature");
        }
    }

    public PKCS10CertificationRequestBuilder setLeaveOffEmptyAttributes(boolean z) {
        this.leaveOffEmpty = z;
        return this;
    }
}
