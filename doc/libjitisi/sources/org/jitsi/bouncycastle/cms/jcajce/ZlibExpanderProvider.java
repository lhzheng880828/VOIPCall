package org.jitsi.bouncycastle.cms.jcajce;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.operator.InputExpander;
import org.jitsi.bouncycastle.operator.InputExpanderProvider;

public class ZlibExpanderProvider implements InputExpanderProvider {
    /* access modifiers changed from: private|final */
    public final long limit;

    private static class LimitedInputStream extends FilterInputStream {
        private long remaining;

        public LimitedInputStream(InputStream inputStream, long j) {
            super(inputStream);
            this.remaining = j;
        }

        /* JADX WARNING: Missing block: B:5:0x0019, code skipped:
            if (r2 >= 0) goto L_0x001b;
     */
        public int read() throws java.io.IOException {
            /*
            r8 = this;
            r6 = 0;
            r0 = r8.remaining;
            r0 = (r0 > r6 ? 1 : (r0 == r6 ? 0 : -1));
            if (r0 < 0) goto L_0x001c;
        L_0x0008:
            r0 = r8.in;
            r0 = r0.read();
            if (r0 < 0) goto L_0x001b;
        L_0x0010:
            r2 = r8.remaining;
            r4 = 1;
            r2 = r2 - r4;
            r8.remaining = r2;
            r1 = (r2 > r6 ? 1 : (r2 == r6 ? 0 : -1));
            if (r1 < 0) goto L_0x001c;
        L_0x001b:
            return r0;
        L_0x001c:
            r0 = new org.jitsi.bouncycastle.util.io.StreamOverflowException;
            r1 = "expanded byte limit exceeded";
            r0.<init>(r1);
            throw r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: org.jitsi.bouncycastle.cms.jcajce.ZlibExpanderProvider$LimitedInputStream.read():int");
        }

        public int read(byte[] bArr, int i, int i2) throws IOException {
            if (i2 < 1) {
                return super.read(bArr, i, i2);
            }
            if (this.remaining < 1) {
                read();
                return -1;
            }
            if (this.remaining <= ((long) i2)) {
                i2 = (int) this.remaining;
            }
            int read = this.in.read(bArr, i, i2);
            if (read <= 0) {
                return read;
            }
            this.remaining -= (long) read;
            return read;
        }
    }

    public ZlibExpanderProvider() {
        this.limit = -1;
    }

    public ZlibExpanderProvider(long j) {
        this.limit = j;
    }

    public InputExpander get(final AlgorithmIdentifier algorithmIdentifier) {
        return new InputExpander() {
            public AlgorithmIdentifier getAlgorithmIdentifier() {
                return algorithmIdentifier;
            }

            public InputStream getInputStream(InputStream inputStream) {
                InputStream inflaterInputStream = new InflaterInputStream(inputStream);
                return ZlibExpanderProvider.this.limit >= 0 ? new LimitedInputStream(inflaterInputStream, ZlibExpanderProvider.this.limit) : inflaterInputStream;
            }
        };
    }
}
