package org.jitsi.impl.neomedia.transform.srtp;

import com.lti.utils.UnsignedUtils;
import java.util.Arrays;
import org.jitsi.bccontrib.params.ParametersForSkein;
import org.jitsi.bouncycastle.crypto.BlockCipher;
import org.jitsi.bouncycastle.crypto.Mac;
import org.jitsi.bouncycastle.crypto.params.KeyParameter;
import org.jitsi.impl.neomedia.RawPacket;

public class SRTCPCryptoContext {
    private static final long REPLAY_WINDOW_SIZE = 64;
    private byte[] authKey;
    private BlockCipher cipher;
    private final SRTPCipherCTR cipherCtr;
    private BlockCipher cipherF8;
    private byte[] encKey;
    private final byte[] ivStore;
    private Mac mac;
    private byte[] masterKey;
    private byte[] masterSalt;
    private byte[] mki;
    private final SRTPPolicy policy;
    private final byte[] rbStore;
    private int receivedIndex;
    private long replayWindow;
    private byte[] saltKey;
    private int sentIndex;
    private final int ssrc;
    private final byte[] tagStore;
    private final byte[] tempStore;

    public SRTCPCryptoContext(int ssrc) {
        this.receivedIndex = 0;
        this.sentIndex = 0;
        this.cipher = null;
        this.cipherF8 = null;
        this.cipherCtr = new SRTPCipherCTR();
        this.ivStore = new byte[16];
        this.rbStore = new byte[4];
        this.tempStore = new byte[100];
        this.ssrc = ssrc;
        this.mki = null;
        this.masterKey = null;
        this.masterSalt = null;
        this.encKey = null;
        this.authKey = null;
        this.saltKey = null;
        this.policy = null;
        this.tagStore = null;
    }

    /* JADX WARNING: Missing block: B:3:0x0064, code skipped:
            switch(r4.policy.getAuthType()) {
                case 0: goto L_0x00b5;
                case 1: goto L_0x00ba;
                case 2: goto L_0x00db;
                default: goto L_0x0067;
            };
     */
    /* JADX WARNING: Missing block: B:4:0x0067, code skipped:
            r4.tagStore = null;
     */
    /* JADX WARNING: Missing block: B:7:0x0076, code skipped:
            r4.cipher = new org.jitsi.bouncycastle.crypto.engines.AESFastEngine();
            r4.encKey = new byte[r4.policy.getEncKeyLength()];
            r4.saltKey = new byte[r4.policy.getSaltKeyLength()];
     */
    /* JADX WARNING: Missing block: B:9:0x0099, code skipped:
            r4.cipher = new org.jitsi.bouncycastle.crypto.engines.TwofishEngine();
            r4.encKey = new byte[r4.policy.getEncKeyLength()];
            r4.saltKey = new byte[r4.policy.getSaltKeyLength()];
     */
    /* JADX WARNING: Missing block: B:10:0x00b5, code skipped:
            r4.authKey = null;
            r4.tagStore = null;
     */
    /* JADX WARNING: Missing block: B:11:0x00ba, code skipped:
            r4.mac = new org.jitsi.bouncycastle.crypto.macs.HMac(new org.jitsi.bouncycastle.crypto.digests.SHA1Digest());
            r4.authKey = new byte[r4.policy.getAuthKeyLength()];
            r4.tagStore = new byte[r4.mac.getMacSize()];
     */
    /* JADX WARNING: Missing block: B:12:0x00db, code skipped:
            r4.mac = new org.jitsi.bccontrib.macs.SkeinMac();
            r4.authKey = new byte[r4.policy.getAuthKeyLength()];
            r4.tagStore = new byte[r4.policy.getAuthTagLength()];
     */
    /* JADX WARNING: Missing block: B:13:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:14:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:15:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:16:?, code skipped:
            return;
     */
    public SRTCPCryptoContext(int r5, byte[] r6, byte[] r7, org.jitsi.impl.neomedia.transform.srtp.SRTPPolicy r8) {
        /*
        r4 = this;
        r3 = 0;
        r2 = 0;
        r4.<init>();
        r4.receivedIndex = r3;
        r4.sentIndex = r3;
        r4.cipher = r2;
        r4.cipherF8 = r2;
        r0 = new org.jitsi.impl.neomedia.transform.srtp.SRTPCipherCTR;
        r0.m2667init();
        r4.cipherCtr = r0;
        r0 = 16;
        r0 = new byte[r0];
        r4.ivStore = r0;
        r0 = 4;
        r0 = new byte[r0];
        r4.rbStore = r0;
        r0 = 100;
        r0 = new byte[r0];
        r4.tempStore = r0;
        r4.ssrc = r5;
        r4.mki = r2;
        r4.policy = r8;
        r0 = r4.policy;
        r0 = r0.getEncKeyLength();
        r0 = new byte[r0];
        r4.masterKey = r0;
        r0 = r4.masterKey;
        r1 = r4.policy;
        r1 = r1.getEncKeyLength();
        java.lang.System.arraycopy(r6, r3, r0, r3, r1);
        r0 = r4.policy;
        r0 = r0.getSaltKeyLength();
        r0 = new byte[r0];
        r4.masterSalt = r0;
        r0 = r4.masterSalt;
        r1 = r4.policy;
        r1 = r1.getSaltKeyLength();
        java.lang.System.arraycopy(r7, r3, r0, r3, r1);
        r0 = r4.policy;
        r0 = r0.getEncType();
        switch(r0) {
            case 0: goto L_0x006a;
            case 1: goto L_0x0076;
            case 2: goto L_0x006f;
            case 3: goto L_0x0099;
            case 4: goto L_0x0092;
            default: goto L_0x005e;
        };
    L_0x005e:
        r0 = r4.policy;
        r0 = r0.getAuthType();
        switch(r0) {
            case 0: goto L_0x00b5;
            case 1: goto L_0x00ba;
            case 2: goto L_0x00db;
            default: goto L_0x0067;
        };
    L_0x0067:
        r4.tagStore = r2;
    L_0x0069:
        return;
    L_0x006a:
        r4.encKey = r2;
        r4.saltKey = r2;
        goto L_0x005e;
    L_0x006f:
        r0 = new org.jitsi.bouncycastle.crypto.engines.AESFastEngine;
        r0.<init>();
        r4.cipherF8 = r0;
    L_0x0076:
        r0 = new org.jitsi.bouncycastle.crypto.engines.AESFastEngine;
        r0.<init>();
        r4.cipher = r0;
        r0 = r4.policy;
        r0 = r0.getEncKeyLength();
        r0 = new byte[r0];
        r4.encKey = r0;
        r0 = r4.policy;
        r0 = r0.getSaltKeyLength();
        r0 = new byte[r0];
        r4.saltKey = r0;
        goto L_0x005e;
    L_0x0092:
        r0 = new org.jitsi.bouncycastle.crypto.engines.TwofishEngine;
        r0.<init>();
        r4.cipherF8 = r0;
    L_0x0099:
        r0 = new org.jitsi.bouncycastle.crypto.engines.TwofishEngine;
        r0.<init>();
        r4.cipher = r0;
        r0 = r4.policy;
        r0 = r0.getEncKeyLength();
        r0 = new byte[r0];
        r4.encKey = r0;
        r0 = r4.policy;
        r0 = r0.getSaltKeyLength();
        r0 = new byte[r0];
        r4.saltKey = r0;
        goto L_0x005e;
    L_0x00b5:
        r4.authKey = r2;
        r4.tagStore = r2;
        goto L_0x0069;
    L_0x00ba:
        r0 = new org.jitsi.bouncycastle.crypto.macs.HMac;
        r1 = new org.jitsi.bouncycastle.crypto.digests.SHA1Digest;
        r1.<init>();
        r0.<init>(r1);
        r4.mac = r0;
        r0 = r4.policy;
        r0 = r0.getAuthKeyLength();
        r0 = new byte[r0];
        r4.authKey = r0;
        r0 = r4.mac;
        r0 = r0.getMacSize();
        r0 = new byte[r0];
        r4.tagStore = r0;
        goto L_0x0069;
    L_0x00db:
        r0 = new org.jitsi.bccontrib.macs.SkeinMac;
        r0.<init>();
        r4.mac = r0;
        r0 = r4.policy;
        r0 = r0.getAuthKeyLength();
        r0 = new byte[r0];
        r4.authKey = r0;
        r0 = r4.policy;
        r0 = r0.getAuthTagLength();
        r0 = new byte[r0];
        r4.tagStore = r0;
        goto L_0x0069;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.transform.srtp.SRTCPCryptoContext.m2664init(int, byte[], byte[], org.jitsi.impl.neomedia.transform.srtp.SRTPPolicy):void");
    }

    public void close() {
        Arrays.fill(this.masterKey, (byte) 0);
        Arrays.fill(this.masterSalt, (byte) 0);
    }

    public int getAuthTagLength() {
        return this.policy.getAuthTagLength();
    }

    public int getMKILength() {
        if (this.mki != null) {
            return this.mki.length;
        }
        return 0;
    }

    public int getSSRC() {
        return this.ssrc;
    }

    public void transformPacket(RawPacket pkt) {
        boolean encrypt = false;
        if (this.policy.getEncType() == 1 || this.policy.getEncType() == 3) {
            processPacketAESCM(pkt, this.sentIndex);
            encrypt = true;
        } else if (this.policy.getEncType() == 2 || this.policy.getEncType() == 4) {
            processPacketAESF8(pkt, this.sentIndex);
            encrypt = true;
        }
        int index = 0;
        if (encrypt) {
            index = this.sentIndex | Integer.MIN_VALUE;
        }
        pkt.grow(this.policy.getAuthTagLength() + 4);
        if (this.policy.getAuthType() != 0) {
            authenticatePacket(pkt, index);
            pkt.append(this.rbStore, 4);
            pkt.append(this.tagStore, this.policy.getAuthTagLength());
        }
        this.sentIndex++;
        this.sentIndex &= Integer.MAX_VALUE;
    }

    public boolean reverseTransformPacket(RawPacket pkt) {
        boolean decrypt = false;
        int tagLength = this.policy.getAuthTagLength();
        int indexEflag = pkt.getSRTCPIndex(tagLength);
        if ((indexEflag & Integer.MIN_VALUE) == Integer.MIN_VALUE) {
            decrypt = true;
        }
        int index = indexEflag & Integer.MAX_VALUE;
        if (!checkReplay(index)) {
            return false;
        }
        if (this.policy.getAuthType() != 0) {
            pkt.readRegionToBuff(pkt.getLength() - tagLength, tagLength, this.tempStore);
            pkt.shrink(tagLength + 4);
            authenticatePacket(pkt, indexEflag);
            for (int i = 0; i < tagLength; i++) {
                if ((this.tempStore[i] & UnsignedUtils.MAX_UBYTE) != (this.tagStore[i] & UnsignedUtils.MAX_UBYTE)) {
                    return false;
                }
            }
        }
        if (decrypt) {
            if (this.policy.getEncType() == 1 || this.policy.getEncType() == 3) {
                processPacketAESCM(pkt, index);
            } else if (this.policy.getEncType() == 2 || this.policy.getEncType() == 4) {
                processPacketAESF8(pkt, index);
            }
        }
        update(index);
        return true;
    }

    public void processPacketAESCM(RawPacket pkt, int index) {
        int ssrc = pkt.getRTCPSSRC();
        this.ivStore[0] = this.saltKey[0];
        this.ivStore[1] = this.saltKey[1];
        this.ivStore[2] = this.saltKey[2];
        this.ivStore[3] = this.saltKey[3];
        this.ivStore[4] = (byte) (((ssrc >> 24) & UnsignedUtils.MAX_UBYTE) ^ this.saltKey[4]);
        this.ivStore[5] = (byte) (((ssrc >> 16) & UnsignedUtils.MAX_UBYTE) ^ this.saltKey[5]);
        this.ivStore[6] = (byte) (((ssrc >> 8) & UnsignedUtils.MAX_UBYTE) ^ this.saltKey[6]);
        this.ivStore[7] = (byte) ((ssrc & UnsignedUtils.MAX_UBYTE) ^ this.saltKey[7]);
        this.ivStore[8] = this.saltKey[8];
        this.ivStore[9] = this.saltKey[9];
        this.ivStore[10] = (byte) (((index >> 24) & UnsignedUtils.MAX_UBYTE) ^ this.saltKey[10]);
        this.ivStore[11] = (byte) (((index >> 16) & UnsignedUtils.MAX_UBYTE) ^ this.saltKey[11]);
        this.ivStore[12] = (byte) (((index >> 8) & UnsignedUtils.MAX_UBYTE) ^ this.saltKey[12]);
        this.ivStore[13] = (byte) ((index & UnsignedUtils.MAX_UBYTE) ^ this.saltKey[13]);
        byte[] bArr = this.ivStore;
        this.ivStore[15] = (byte) 0;
        bArr[14] = (byte) 0;
        this.cipherCtr.process(this.cipher, pkt.getBuffer(), pkt.getOffset() + 8, pkt.getLength() - 8, this.ivStore);
    }

    public void processPacketAESF8(RawPacket pkt, int index) {
        this.ivStore[0] = (byte) 0;
        this.ivStore[1] = (byte) 0;
        this.ivStore[2] = (byte) 0;
        this.ivStore[3] = (byte) 0;
        index |= Integer.MIN_VALUE;
        this.ivStore[4] = (byte) (index >> 24);
        this.ivStore[5] = (byte) (index >> 16);
        this.ivStore[6] = (byte) (index >> 8);
        this.ivStore[7] = (byte) index;
        System.arraycopy(pkt.getBuffer(), pkt.getOffset(), this.ivStore, 8, 8);
        SRTPCipherF8.process(this.cipher, pkt.getBuffer(), pkt.getOffset() + 8, pkt.getLength() - (this.policy.getAuthTagLength() + 4), this.ivStore, this.cipherF8);
    }

    private void authenticatePacket(RawPacket pkt, int index) {
        this.mac.update(pkt.getBuffer(), 0, pkt.getLength());
        this.rbStore[0] = (byte) (index >> 24);
        this.rbStore[1] = (byte) (index >> 16);
        this.rbStore[2] = (byte) (index >> 8);
        this.rbStore[3] = (byte) index;
        this.mac.update(this.rbStore, 0, this.rbStore.length);
        this.mac.doFinal(this.tagStore, 0);
    }

    /* access modifiers changed from: 0000 */
    public boolean checkReplay(int index) {
        long delta = (long) (index - this.receivedIndex);
        if (delta > 0) {
            return true;
        }
        if ((-delta) > REPLAY_WINDOW_SIZE) {
            return false;
        }
        if (((this.replayWindow >> ((int) (-delta))) & 1) != 0) {
            return false;
        }
        return true;
    }

    private void computeIv(byte label) {
        for (int i = 0; i < 14; i++) {
            this.ivStore[i] = this.masterSalt[i];
        }
        byte[] bArr = this.ivStore;
        bArr[7] = (byte) (bArr[7] ^ label);
        bArr = this.ivStore;
        this.ivStore[15] = (byte) 0;
        bArr[14] = (byte) 0;
    }

    public void deriveSrtcpKeys() {
        computeIv((byte) 3);
        this.cipher.init(true, new KeyParameter(this.masterKey));
        Arrays.fill(this.masterKey, (byte) 0);
        this.cipherCtr.getCipherStream(this.cipher, this.encKey, this.policy.getEncKeyLength(), this.ivStore);
        if (this.authKey != null) {
            computeIv((byte) 4);
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
        computeIv((byte) 5);
        this.cipherCtr.getCipherStream(this.cipher, this.saltKey, this.policy.getSaltKeyLength(), this.ivStore);
        Arrays.fill(this.masterSalt, (byte) 0);
        if (this.cipherF8 != null) {
            SRTPCipherF8.deriveForIV(this.cipherF8, this.encKey, this.saltKey);
        }
        this.cipher.init(true, new KeyParameter(this.encKey));
        Arrays.fill(this.encKey, (byte) 0);
    }

    private void update(int index) {
        int delta = this.receivedIndex - index;
        if (delta > 0) {
            this.replayWindow <<= delta;
            this.replayWindow |= 1;
        } else {
            this.replayWindow |= (long) (1 << delta);
        }
        this.receivedIndex = index;
    }

    public SRTCPCryptoContext deriveContext(int ssrc) {
        return new SRTCPCryptoContext(ssrc, this.masterKey, this.masterSalt, this.policy);
    }
}
