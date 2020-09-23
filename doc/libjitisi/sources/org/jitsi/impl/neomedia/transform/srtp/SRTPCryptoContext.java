package org.jitsi.impl.neomedia.transform.srtp;

import com.lti.utils.UnsignedUtils;
import java.util.Arrays;
import org.jitsi.bccontrib.params.ParametersForSkein;
import org.jitsi.bouncycastle.crypto.BlockCipher;
import org.jitsi.bouncycastle.crypto.Mac;
import org.jitsi.bouncycastle.crypto.params.KeyParameter;
import org.jitsi.impl.neomedia.RawPacket;
import org.jitsi.util.Logger;

public class SRTPCryptoContext {
    private static final String CHECK_REPLAY_PROPERTY_NAME = (SRTPCryptoContext.class.getName() + ".checkReplay");
    private static final long REPLAY_WINDOW_SIZE = 64;
    private static Boolean checkReplay;
    private static final Logger logger = Logger.getLogger(SRTPCryptoContext.class);
    private byte[] authKey;
    private BlockCipher cipher;
    private final SRTPCipherCTR cipherCtr;
    private BlockCipher cipherF8;
    private byte[] encKey;
    private int guessedROC;
    private final byte[] ivStore;
    private long keyDerivationRate;
    private Mac mac;
    private byte[] masterKey;
    private byte[] masterSalt;
    private byte[] mki;
    private final SRTPPolicy policy;
    private final byte[] rbStore;
    private long replayWindow;
    private int roc;
    private int s_l;
    private byte[] saltKey;
    private final boolean sender;
    private boolean seqNumSet;
    private final int ssrc;
    private final byte[] tagStore;
    private final byte[] tempStore;

    public SRTPCryptoContext(boolean sender, int ssrc) {
        this.s_l = 0;
        this.seqNumSet = false;
        this.cipher = null;
        this.cipherF8 = null;
        this.cipherCtr = new SRTPCipherCTR();
        this.ivStore = new byte[16];
        this.rbStore = new byte[4];
        this.tempStore = new byte[100];
        this.authKey = null;
        this.encKey = null;
        this.keyDerivationRate = 0;
        this.masterKey = null;
        this.masterSalt = null;
        this.mki = null;
        this.policy = null;
        this.roc = 0;
        this.sender = sender;
        this.ssrc = ssrc;
        this.tagStore = null;
    }

    /* JADX WARNING: Missing block: B:3:0x0076, code skipped:
            switch(r6.policy.getAuthType()) {
                case 0: goto L_0x00e2;
                case 1: goto L_0x00e7;
                case 2: goto L_0x0109;
                default: goto L_0x0079;
            };
     */
    /* JADX WARNING: Missing block: B:4:0x0079, code skipped:
            r6.tagStore = null;
     */
    /* JADX WARNING: Missing block: B:5:0x007b, code skipped:
            r3 = org.jitsi.impl.neomedia.transform.srtp.SRTPCryptoContext.class;
     */
    /* JADX WARNING: Missing block: B:6:0x007d, code skipped:
            monitor-enter(r3);
     */
    /* JADX WARNING: Missing block: B:9:0x0080, code skipped:
            if (checkReplay != null) goto L_0x0095;
     */
    /* JADX WARNING: Missing block: B:10:0x0082, code skipped:
            r0 = org.jitsi.service.libjitsi.LibJitsi.getConfigurationService();
            r1 = true;
     */
    /* JADX WARNING: Missing block: B:11:0x0087, code skipped:
            if (r0 == null) goto L_0x008f;
     */
    /* JADX WARNING: Missing block: B:12:0x0089, code skipped:
            r1 = r0.getBoolean(CHECK_REPLAY_PROPERTY_NAME, true);
     */
    /* JADX WARNING: Missing block: B:13:0x008f, code skipped:
            checkReplay = java.lang.Boolean.valueOf(r1);
     */
    /* JADX WARNING: Missing block: B:14:0x0095, code skipped:
            monitor-exit(r3);
     */
    /* JADX WARNING: Missing block: B:15:0x0096, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:18:0x00a3, code skipped:
            r6.cipher = new org.jitsi.bouncycastle.crypto.engines.AESFastEngine();
            r6.encKey = new byte[r6.policy.getEncKeyLength()];
            r6.saltKey = new byte[r6.policy.getSaltKeyLength()];
     */
    /* JADX WARNING: Missing block: B:20:0x00c6, code skipped:
            r6.cipher = new org.jitsi.bouncycastle.crypto.engines.TwofishEngine();
            r6.encKey = new byte[r6.policy.getEncKeyLength()];
            r6.saltKey = new byte[r6.policy.getSaltKeyLength()];
     */
    /* JADX WARNING: Missing block: B:21:0x00e2, code skipped:
            r6.authKey = null;
            r6.tagStore = null;
     */
    /* JADX WARNING: Missing block: B:22:0x00e7, code skipped:
            r6.mac = new org.jitsi.bouncycastle.crypto.macs.HMac(new org.jitsi.bouncycastle.crypto.digests.SHA1Digest());
            r6.authKey = new byte[r6.policy.getAuthKeyLength()];
            r6.tagStore = new byte[r6.mac.getMacSize()];
     */
    /* JADX WARNING: Missing block: B:23:0x0109, code skipped:
            r6.mac = new org.jitsi.bccontrib.macs.SkeinMac();
            r6.authKey = new byte[r6.policy.getAuthKeyLength()];
            r6.tagStore = new byte[r6.policy.getAuthTagLength()];
     */
    public SRTPCryptoContext(boolean r7, int r8, int r9, long r10, byte[] r12, byte[] r13, org.jitsi.impl.neomedia.transform.srtp.SRTPPolicy r14) {
        /*
        r6 = this;
        r5 = 0;
        r4 = 0;
        r6.<init>();
        r6.s_l = r5;
        r6.seqNumSet = r5;
        r6.cipher = r4;
        r6.cipherF8 = r4;
        r2 = new org.jitsi.impl.neomedia.transform.srtp.SRTPCipherCTR;
        r2.m2667init();
        r6.cipherCtr = r2;
        r2 = 16;
        r2 = new byte[r2];
        r6.ivStore = r2;
        r2 = 4;
        r2 = new byte[r2];
        r6.rbStore = r2;
        r2 = 100;
        r2 = new byte[r2];
        r6.tempStore = r2;
        r6.keyDerivationRate = r10;
        r6.mki = r4;
        r6.roc = r9;
        r6.policy = r14;
        r6.sender = r7;
        r6.ssrc = r8;
        r2 = r6.policy;
        r2 = r2.getEncKeyLength();
        r2 = new byte[r2];
        r6.masterKey = r2;
        r2 = r6.masterKey;
        r3 = r6.policy;
        r3 = r3.getEncKeyLength();
        java.lang.System.arraycopy(r12, r5, r2, r5, r3);
        r2 = r6.policy;
        r2 = r2.getSaltKeyLength();
        r2 = new byte[r2];
        r6.masterSalt = r2;
        r2 = r6.masterSalt;
        r3 = r6.policy;
        r3 = r3.getSaltKeyLength();
        java.lang.System.arraycopy(r13, r5, r2, r5, r3);
        r2 = new org.jitsi.bouncycastle.crypto.macs.HMac;
        r3 = new org.jitsi.bouncycastle.crypto.digests.SHA1Digest;
        r3.<init>();
        r2.<init>(r3);
        r6.mac = r2;
        r2 = r6.policy;
        r2 = r2.getEncType();
        switch(r2) {
            case 0: goto L_0x0097;
            case 1: goto L_0x00a3;
            case 2: goto L_0x009c;
            case 3: goto L_0x00c6;
            case 4: goto L_0x00bf;
            default: goto L_0x0070;
        };
    L_0x0070:
        r2 = r6.policy;
        r2 = r2.getAuthType();
        switch(r2) {
            case 0: goto L_0x00e2;
            case 1: goto L_0x00e7;
            case 2: goto L_0x0109;
            default: goto L_0x0079;
        };
    L_0x0079:
        r6.tagStore = r4;
    L_0x007b:
        r3 = org.jitsi.impl.neomedia.transform.srtp.SRTPCryptoContext.class;
        monitor-enter(r3);
        r2 = checkReplay;	 Catch:{ all -> 0x0126 }
        if (r2 != 0) goto L_0x0095;
    L_0x0082:
        r0 = org.jitsi.service.libjitsi.LibJitsi.getConfigurationService();	 Catch:{ all -> 0x0126 }
        r1 = 1;
        if (r0 == 0) goto L_0x008f;
    L_0x0089:
        r2 = CHECK_REPLAY_PROPERTY_NAME;	 Catch:{ all -> 0x0126 }
        r1 = r0.getBoolean(r2, r1);	 Catch:{ all -> 0x0126 }
    L_0x008f:
        r2 = java.lang.Boolean.valueOf(r1);	 Catch:{ all -> 0x0126 }
        checkReplay = r2;	 Catch:{ all -> 0x0126 }
    L_0x0095:
        monitor-exit(r3);	 Catch:{ all -> 0x0126 }
        return;
    L_0x0097:
        r6.encKey = r4;
        r6.saltKey = r4;
        goto L_0x0070;
    L_0x009c:
        r2 = new org.jitsi.bouncycastle.crypto.engines.AESFastEngine;
        r2.<init>();
        r6.cipherF8 = r2;
    L_0x00a3:
        r2 = new org.jitsi.bouncycastle.crypto.engines.AESFastEngine;
        r2.<init>();
        r6.cipher = r2;
        r2 = r6.policy;
        r2 = r2.getEncKeyLength();
        r2 = new byte[r2];
        r6.encKey = r2;
        r2 = r6.policy;
        r2 = r2.getSaltKeyLength();
        r2 = new byte[r2];
        r6.saltKey = r2;
        goto L_0x0070;
    L_0x00bf:
        r2 = new org.jitsi.bouncycastle.crypto.engines.TwofishEngine;
        r2.<init>();
        r6.cipherF8 = r2;
    L_0x00c6:
        r2 = new org.jitsi.bouncycastle.crypto.engines.TwofishEngine;
        r2.<init>();
        r6.cipher = r2;
        r2 = r6.policy;
        r2 = r2.getEncKeyLength();
        r2 = new byte[r2];
        r6.encKey = r2;
        r2 = r6.policy;
        r2 = r2.getSaltKeyLength();
        r2 = new byte[r2];
        r6.saltKey = r2;
        goto L_0x0070;
    L_0x00e2:
        r6.authKey = r4;
        r6.tagStore = r4;
        goto L_0x007b;
    L_0x00e7:
        r2 = new org.jitsi.bouncycastle.crypto.macs.HMac;
        r3 = new org.jitsi.bouncycastle.crypto.digests.SHA1Digest;
        r3.<init>();
        r2.<init>(r3);
        r6.mac = r2;
        r2 = r6.policy;
        r2 = r2.getAuthKeyLength();
        r2 = new byte[r2];
        r6.authKey = r2;
        r2 = r6.mac;
        r2 = r2.getMacSize();
        r2 = new byte[r2];
        r6.tagStore = r2;
        goto L_0x007b;
    L_0x0109:
        r2 = new org.jitsi.bccontrib.macs.SkeinMac;
        r2.<init>();
        r6.mac = r2;
        r2 = r6.policy;
        r2 = r2.getAuthKeyLength();
        r2 = new byte[r2];
        r6.authKey = r2;
        r2 = r6.policy;
        r2 = r2.getAuthTagLength();
        r2 = new byte[r2];
        r6.tagStore = r2;
        goto L_0x007b;
    L_0x0126:
        r2 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x0126 }
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.transform.srtp.SRTPCryptoContext.m2673init(boolean, int, int, long, byte[], byte[], org.jitsi.impl.neomedia.transform.srtp.SRTPPolicy):void");
    }

    public void close() {
        Arrays.fill(this.masterKey, (byte) 0);
        Arrays.fill(this.masterSalt, (byte) 0);
    }

    public int getAuthTagLength() {
        return this.policy.getAuthTagLength();
    }

    public int getMKILength() {
        return this.mki == null ? 0 : this.mki.length;
    }

    public int getSSRC() {
        return this.ssrc;
    }

    public boolean transformPacket(RawPacket pkt) {
        int seqNo = pkt.getSequenceNumber();
        if (!this.seqNumSet) {
            this.seqNumSet = true;
            this.s_l = seqNo;
        }
        long guessedIndex = guessIndex(seqNo);
        if (!checkReplay(seqNo, guessedIndex)) {
            return false;
        }
        switch (this.policy.getEncType()) {
            case 1:
            case 3:
                processPacketAESCM(pkt);
                break;
            case 2:
            case 4:
                processPacketAESF8(pkt);
                break;
        }
        if (this.policy.getAuthType() != 0) {
            authenticatePacketHMCSHA1(pkt, this.guessedROC);
            pkt.append(this.tagStore, this.policy.getAuthTagLength());
        }
        update(seqNo, guessedIndex);
        return true;
    }

    public boolean reverseTransformPacket(RawPacket pkt) {
        int seqNo = pkt.getSequenceNumber();
        if (!this.seqNumSet) {
            this.seqNumSet = true;
            this.s_l = seqNo;
        }
        long guessedIndex = guessIndex(seqNo);
        if (!checkReplay(seqNo, guessedIndex)) {
            return false;
        }
        if (this.policy.getAuthType() != 0) {
            int tagLength = this.policy.getAuthTagLength();
            pkt.readRegionToBuff(pkt.getLength() - tagLength, tagLength, this.tempStore);
            pkt.shrink(tagLength);
            authenticatePacketHMCSHA1(pkt, this.guessedROC);
            for (int i = 0; i < tagLength; i++) {
                if ((this.tempStore[i] & UnsignedUtils.MAX_UBYTE) != (this.tagStore[i] & UnsignedUtils.MAX_UBYTE)) {
                    return false;
                }
            }
        }
        switch (this.policy.getEncType()) {
            case 1:
            case 3:
                processPacketAESCM(pkt);
                break;
            case 2:
            case 4:
                processPacketAESF8(pkt);
                break;
        }
        update(seqNo, guessedIndex);
        return true;
    }

    public void processPacketAESCM(RawPacket pkt) {
        int i;
        int ssrc = pkt.getSSRC();
        long index = (((long) this.guessedROC) << 16) | ((long) pkt.getSequenceNumber());
        this.ivStore[0] = this.saltKey[0];
        this.ivStore[1] = this.saltKey[1];
        this.ivStore[2] = this.saltKey[2];
        this.ivStore[3] = this.saltKey[3];
        for (i = 4; i < 8; i++) {
            this.ivStore[i] = (byte) (((ssrc >> ((7 - i) * 8)) & UnsignedUtils.MAX_UBYTE) ^ this.saltKey[i]);
        }
        for (i = 8; i < 14; i++) {
            this.ivStore[i] = (byte) ((((byte) ((int) (index >> ((13 - i) * 8)))) & UnsignedUtils.MAX_UBYTE) ^ this.saltKey[i]);
        }
        byte[] bArr = this.ivStore;
        this.ivStore[15] = (byte) 0;
        bArr[14] = (byte) 0;
        int payloadOffset = pkt.getHeaderLength();
        this.cipherCtr.process(this.cipher, pkt.getBuffer(), pkt.getOffset() + payloadOffset, pkt.getPayloadLength(), this.ivStore);
    }

    public void processPacketAESF8(RawPacket pkt) {
        System.arraycopy(pkt.getBuffer(), pkt.getOffset(), this.ivStore, 0, 12);
        this.ivStore[0] = (byte) 0;
        int roc = this.guessedROC;
        this.ivStore[12] = (byte) (roc >> 24);
        this.ivStore[13] = (byte) (roc >> 16);
        this.ivStore[14] = (byte) (roc >> 8);
        this.ivStore[15] = (byte) roc;
        int payloadOffset = pkt.getHeaderLength();
        SRTPCipherF8.process(this.cipher, pkt.getBuffer(), pkt.getOffset() + payloadOffset, pkt.getPayloadLength(), this.ivStore, this.cipherF8);
    }

    private void authenticatePacketHMCSHA1(RawPacket pkt, int rocIn) {
        this.mac.update(pkt.getBuffer(), pkt.getOffset(), pkt.getLength());
        this.rbStore[0] = (byte) (rocIn >> 24);
        this.rbStore[1] = (byte) (rocIn >> 16);
        this.rbStore[2] = (byte) (rocIn >> 8);
        this.rbStore[3] = (byte) rocIn;
        this.mac.update(this.rbStore, 0, this.rbStore.length);
        this.mac.doFinal(this.tagStore, 0);
    }

    /* access modifiers changed from: 0000 */
    public boolean checkReplay(int seqNo, long guessedIndex) {
        if (!checkReplay.booleanValue()) {
            return true;
        }
        long delta = guessedIndex - ((((long) this.roc) << 16) | ((long) this.s_l));
        if (delta > 0) {
            return true;
        }
        if ((-delta) > REPLAY_WINDOW_SIZE) {
            if (this.sender) {
                logger.error("Discarding RTP packet with sequence number " + seqNo + ", SSRC " + Long.toString(4294967295L & ((long) this.ssrc)) + " because it is outside the replay window! (roc " + this.roc + ", s_l " + this.s_l + ", guessedROC " + this.guessedROC);
            }
            return false;
        } else if (((this.replayWindow >> ((int) (-delta))) & 1) == 0) {
            return true;
        } else {
            if (this.sender) {
                logger.error("Discarding RTP packet with sequence number " + seqNo + ", SSRC " + Long.toString(4294967295L & ((long) this.ssrc)) + " because it has been received already! (roc " + this.roc + ", s_l " + this.s_l + ", guessedROC " + this.guessedROC);
            }
            return false;
        }
    }

    private void computeIv(long label, long index) {
        long key_id;
        int i;
        if (this.keyDerivationRate == 0) {
            key_id = label << 48;
        } else {
            key_id = (label << 48) | (index / this.keyDerivationRate);
        }
        for (i = 0; i < 7; i++) {
            this.ivStore[i] = this.masterSalt[i];
        }
        for (i = 7; i < 14; i++) {
            this.ivStore[i] = (byte) (((byte) ((int) (255 & (key_id >> ((13 - i) * 8))))) ^ this.masterSalt[i]);
        }
        byte[] bArr = this.ivStore;
        this.ivStore[15] = (byte) 0;
        bArr[14] = (byte) 0;
    }

    public void deriveSrtpKeys(long index) {
        computeIv(0, index);
        this.cipher.init(true, new KeyParameter(this.masterKey));
        Arrays.fill(this.masterKey, (byte) 0);
        this.cipherCtr.getCipherStream(this.cipher, this.encKey, this.policy.getEncKeyLength(), this.ivStore);
        if (this.authKey != null) {
            computeIv(1, index);
            this.cipherCtr.getCipherStream(this.cipher, this.authKey, this.policy.getAuthKeyLength(), this.ivStore);
            switch (this.policy.getAuthType()) {
                case 1:
                    this.mac.init(new KeyParameter(this.authKey));
                    break;
                case 2:
                    this.mac.init(new ParametersForSkein(new KeyParameter(this.authKey), 512, this.tagStore.length * 8));
                    break;
            }
        }
        Arrays.fill(this.authKey, (byte) 0);
        computeIv(2, index);
        this.cipherCtr.getCipherStream(this.cipher, this.saltKey, this.policy.getSaltKeyLength(), this.ivStore);
        Arrays.fill(this.masterSalt, (byte) 0);
        if (this.cipherF8 != null) {
            SRTPCipherF8.deriveForIV(this.cipherF8, this.encKey, this.saltKey);
        }
        this.cipher.init(true, new KeyParameter(this.encKey));
        Arrays.fill(this.encKey, (byte) 0);
    }

    private long guessIndex(int seqNo) {
        if (this.s_l < 32768) {
            if (seqNo - this.s_l > 32768) {
                this.guessedROC = this.roc - 1;
            } else {
                this.guessedROC = this.roc;
            }
        } else if (this.s_l - 32768 > seqNo) {
            this.guessedROC = this.roc + 1;
        } else {
            this.guessedROC = this.roc;
        }
        return (((long) this.guessedROC) << 16) | ((long) seqNo);
    }

    private void update(int seqNo, long guessedIndex) {
        long delta = guessedIndex - ((((long) this.roc) << 16) | ((long) this.s_l));
        if (delta > 0) {
            this.replayWindow <<= (int) delta;
            this.replayWindow |= 1;
        } else {
            this.replayWindow |= (long) (1 << ((int) (-delta)));
        }
        if (this.guessedROC == this.roc) {
            if (seqNo > this.s_l) {
                this.s_l = seqNo & 65535;
            }
        } else if (this.guessedROC == this.roc + 1) {
            this.s_l = seqNo & 65535;
            this.roc = this.guessedROC;
        }
    }

    public SRTPCryptoContext deriveContext(int ssrc, int roc, long deriveRate) {
        return new SRTPCryptoContext(this.sender, ssrc, roc, deriveRate, this.masterKey, this.masterSalt, this.policy);
    }
}
