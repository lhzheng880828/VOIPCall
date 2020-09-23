package org.jitsi.bouncycastle.cms;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class CMSAuthenticatedGenerator extends CMSEnvelopedGenerator {
    protected CMSAttributeTableGenerator authGen;
    protected CMSAttributeTableGenerator unauthGen;

    public CMSAuthenticatedGenerator(SecureRandom secureRandom) {
        super(secureRandom);
    }

    /* access modifiers changed from: protected */
    public Map getBaseParameters(ASN1ObjectIdentifier aSN1ObjectIdentifier, AlgorithmIdentifier algorithmIdentifier, byte[] bArr) {
        HashMap hashMap = new HashMap();
        hashMap.put(CMSAttributeTableGenerator.CONTENT_TYPE, aSN1ObjectIdentifier);
        hashMap.put(CMSAttributeTableGenerator.DIGEST_ALGORITHM_IDENTIFIER, algorithmIdentifier);
        hashMap.put(CMSAttributeTableGenerator.DIGEST, bArr.clone());
        return hashMap;
    }

    public void setAuthenticatedAttributeGenerator(CMSAttributeTableGenerator cMSAttributeTableGenerator) {
        this.authGen = cMSAttributeTableGenerator;
    }

    public void setUnauthenticatedAttributeGenerator(CMSAttributeTableGenerator cMSAttributeTableGenerator) {
        this.unauthGen = cMSAttributeTableGenerator;
    }
}
