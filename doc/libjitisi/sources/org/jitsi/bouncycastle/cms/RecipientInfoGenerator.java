package org.jitsi.bouncycastle.cms;

import org.jitsi.bouncycastle.asn1.cms.RecipientInfo;
import org.jitsi.bouncycastle.operator.GenericKey;

public interface RecipientInfoGenerator {
    RecipientInfo generate(GenericKey genericKey) throws CMSException;
}
