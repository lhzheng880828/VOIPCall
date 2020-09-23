package org.jitsi.bouncycastle.cms;

import org.jitsi.bouncycastle.operator.OperatorCreationException;

public interface SignerInformationVerifierProvider {
    SignerInformationVerifier get(SignerId signerId) throws OperatorCreationException;
}
