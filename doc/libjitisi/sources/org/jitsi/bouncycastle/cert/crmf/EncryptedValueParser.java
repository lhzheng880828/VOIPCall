package org.jitsi.bouncycastle.cert.crmf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.jitsi.bouncycastle.asn1.crmf.EncryptedValue;
import org.jitsi.bouncycastle.asn1.x509.Certificate;
import org.jitsi.bouncycastle.cert.X509CertificateHolder;
import org.jitsi.bouncycastle.util.Strings;
import org.jitsi.bouncycastle.util.io.Streams;

public class EncryptedValueParser {
    private EncryptedValuePadder padder;
    private EncryptedValue value;

    public EncryptedValueParser(EncryptedValue encryptedValue) {
        this.value = encryptedValue;
    }

    public EncryptedValueParser(EncryptedValue encryptedValue, EncryptedValuePadder encryptedValuePadder) {
        this.value = encryptedValue;
        this.padder = encryptedValuePadder;
    }

    private byte[] decryptValue(ValueDecryptorGenerator valueDecryptorGenerator) throws CRMFException {
        if (this.value.getIntendedAlg() != null) {
            throw new UnsupportedOperationException();
        } else if (this.value.getValueHint() != null) {
            throw new UnsupportedOperationException();
        } else {
            try {
                byte[] readAll = Streams.readAll(valueDecryptorGenerator.getValueDecryptor(this.value.getKeyAlg(), this.value.getSymmAlg(), this.value.getEncSymmKey().getBytes()).getInputStream(new ByteArrayInputStream(this.value.getEncValue().getBytes())));
                return this.padder != null ? this.padder.getUnpaddedData(readAll) : readAll;
            } catch (IOException e) {
                throw new CRMFException("Cannot parse decrypted data: " + e.getMessage(), e);
            }
        }
    }

    public X509CertificateHolder readCertificateHolder(ValueDecryptorGenerator valueDecryptorGenerator) throws CRMFException {
        return new X509CertificateHolder(Certificate.getInstance(decryptValue(valueDecryptorGenerator)));
    }

    public char[] readPassphrase(ValueDecryptorGenerator valueDecryptorGenerator) throws CRMFException {
        return Strings.fromUTF8ByteArray(decryptValue(valueDecryptorGenerator)).toCharArray();
    }
}
