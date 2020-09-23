package org.jitsi.bouncycastle.cms.bc;

import java.io.InputStream;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.cms.CMSException;
import org.jitsi.bouncycastle.cms.RecipientOperator;
import org.jitsi.bouncycastle.crypto.BufferedBlockCipher;
import org.jitsi.bouncycastle.crypto.StreamCipher;
import org.jitsi.bouncycastle.crypto.io.CipherInputStream;
import org.jitsi.bouncycastle.operator.InputDecryptor;

public class BcPasswordEnvelopedRecipient extends BcPasswordRecipient {
    public BcPasswordEnvelopedRecipient(char[] cArr) {
        super(cArr);
    }

    public RecipientOperator getRecipientOperator(AlgorithmIdentifier algorithmIdentifier, final AlgorithmIdentifier algorithmIdentifier2, byte[] bArr, byte[] bArr2) throws CMSException {
        final Object createContentCipher = EnvelopedDataHelper.createContentCipher(false, extractSecretKey(algorithmIdentifier, algorithmIdentifier2, bArr, bArr2), algorithmIdentifier2);
        return new RecipientOperator(new InputDecryptor() {
            public AlgorithmIdentifier getAlgorithmIdentifier() {
                return algorithmIdentifier2;
            }

            public InputStream getInputStream(InputStream inputStream) {
                return createContentCipher instanceof BufferedBlockCipher ? new CipherInputStream(inputStream, (BufferedBlockCipher) createContentCipher) : new CipherInputStream(inputStream, (StreamCipher) createContentCipher);
            }
        });
    }
}
