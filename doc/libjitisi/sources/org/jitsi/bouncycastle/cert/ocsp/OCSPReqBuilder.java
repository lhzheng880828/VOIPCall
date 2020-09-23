package org.jitsi.bouncycastle.cert.ocsp;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.jitsi.bouncycastle.asn1.ASN1EncodableVector;
import org.jitsi.bouncycastle.asn1.DERBitString;
import org.jitsi.bouncycastle.asn1.DERSequence;
import org.jitsi.bouncycastle.asn1.ocsp.OCSPRequest;
import org.jitsi.bouncycastle.asn1.ocsp.Request;
import org.jitsi.bouncycastle.asn1.ocsp.Signature;
import org.jitsi.bouncycastle.asn1.ocsp.TBSRequest;
import org.jitsi.bouncycastle.asn1.x500.X500Name;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.asn1.x509.Extensions;
import org.jitsi.bouncycastle.asn1.x509.GeneralName;
import org.jitsi.bouncycastle.cert.X509CertificateHolder;
import org.jitsi.bouncycastle.operator.ContentSigner;

public class OCSPReqBuilder {
    private List list = new ArrayList();
    private Extensions requestExtensions = null;
    private GeneralName requestorName = null;

    private class RequestObject {
        CertificateID certId;
        Extensions extensions;

        public RequestObject(CertificateID certificateID, Extensions extensions) {
            this.certId = certificateID;
            this.extensions = extensions;
        }

        public Request toRequest() throws Exception {
            return new Request(this.certId.toASN1Object(), this.extensions);
        }
    }

    private OCSPReq generateRequest(ContentSigner contentSigner, X509CertificateHolder[] x509CertificateHolderArr) throws OCSPException {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        for (RequestObject toRequest : this.list) {
            try {
                aSN1EncodableVector.add(toRequest.toRequest());
            } catch (Exception e) {
                throw new OCSPException("exception creating Request", e);
            }
        }
        TBSRequest tBSRequest = new TBSRequest(this.requestorName, new DERSequence(aSN1EncodableVector), this.requestExtensions);
        Signature signature = null;
        if (contentSigner != null) {
            if (this.requestorName == null) {
                throw new OCSPException("requestorName must be specified if request is signed.");
            }
            try {
                OutputStream outputStream = contentSigner.getOutputStream();
                outputStream.write(tBSRequest.getEncoded("DER"));
                outputStream.close();
                DERBitString dERBitString = new DERBitString(contentSigner.getSignature());
                AlgorithmIdentifier algorithmIdentifier = contentSigner.getAlgorithmIdentifier();
                if (x509CertificateHolderArr == null || x509CertificateHolderArr.length <= 0) {
                    signature = new Signature(algorithmIdentifier, dERBitString);
                } else {
                    ASN1EncodableVector aSN1EncodableVector2 = new ASN1EncodableVector();
                    for (int i = 0; i != x509CertificateHolderArr.length; i++) {
                        aSN1EncodableVector2.add(x509CertificateHolderArr[i].toASN1Structure());
                    }
                    signature = new Signature(algorithmIdentifier, dERBitString, new DERSequence(aSN1EncodableVector2));
                }
            } catch (Exception e2) {
                throw new OCSPException("exception processing TBSRequest: " + e2, e2);
            }
        }
        return new OCSPReq(new OCSPRequest(tBSRequest, signature));
    }

    public OCSPReqBuilder addRequest(CertificateID certificateID) {
        this.list.add(new RequestObject(certificateID, null));
        return this;
    }

    public OCSPReqBuilder addRequest(CertificateID certificateID, Extensions extensions) {
        this.list.add(new RequestObject(certificateID, extensions));
        return this;
    }

    public OCSPReq build() throws OCSPException {
        return generateRequest(null, null);
    }

    public OCSPReq build(ContentSigner contentSigner, X509CertificateHolder[] x509CertificateHolderArr) throws OCSPException, IllegalArgumentException {
        if (contentSigner != null) {
            return generateRequest(contentSigner, x509CertificateHolderArr);
        }
        throw new IllegalArgumentException("no signer specified");
    }

    public OCSPReqBuilder setRequestExtensions(Extensions extensions) {
        this.requestExtensions = extensions;
        return this;
    }

    public OCSPReqBuilder setRequestorName(X500Name x500Name) {
        this.requestorName = new GeneralName(4, x500Name);
        return this;
    }

    public OCSPReqBuilder setRequestorName(GeneralName generalName) {
        this.requestorName = generalName;
        return this;
    }
}
