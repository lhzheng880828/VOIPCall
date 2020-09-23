package org.jitsi.bouncycastle.cert.selector;

import java.io.IOException;
import org.jitsi.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.jitsi.bouncycastle.crypto.digests.SHA1Digest;

class MSOutlookKeyIdCalculator {
    MSOutlookKeyIdCalculator() {
    }

    static byte[] calculateKeyId(SubjectPublicKeyInfo subjectPublicKeyInfo) {
        SHA1Digest sHA1Digest = new SHA1Digest();
        byte[] bArr = new byte[sHA1Digest.getDigestSize()];
        byte[] bArr2 = new byte[0];
        try {
            bArr2 = subjectPublicKeyInfo.getEncoded("DER");
            sHA1Digest.update(bArr2, 0, bArr2.length);
            sHA1Digest.doFinal(bArr, 0);
            return bArr;
        } catch (IOException e) {
            return new byte[0];
        }
    }
}
