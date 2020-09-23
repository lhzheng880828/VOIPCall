package org.jitsi.impl.neomedia.transform.zrtp;

import gnu.java.zrtp.utils.ZrtpCrc32;
import org.jitsi.impl.neomedia.RawPacket;

public class ZrtpRawPacket extends RawPacket {
    public static final byte[] ZRTP_MAGIC = new byte[]{(byte) 90, (byte) 82, (byte) 84, (byte) 80};

    public ZrtpRawPacket(RawPacket pkt) {
        super(pkt.getBuffer(), pkt.getOffset(), pkt.getLength());
    }

    public ZrtpRawPacket(byte[] buf, int off, int len) {
        super(buf, off, len);
        writeByte(0, (byte) 16);
        writeByte(1, (byte) 0);
        int i = 4 + 1;
        writeByte(4, ZRTP_MAGIC[0]);
        int i2 = i + 1;
        writeByte(i, ZRTP_MAGIC[1]);
        i = i2 + 1;
        writeByte(i2, ZRTP_MAGIC[2]);
        writeByte(i, ZRTP_MAGIC[3]);
    }

    /* access modifiers changed from: protected */
    public boolean isZrtpPacket() {
        return isZrtpData(this);
    }

    static boolean isZrtpData(RawPacket pkt) {
        return pkt.getExtensionBit() && pkt.getHeaderExtensionType() == 20570;
    }

    /* access modifiers changed from: protected */
    public boolean hasMagic() {
        return readByte(4) == ZRTP_MAGIC[0] && readByte(5) == ZRTP_MAGIC[1] && readByte(6) == ZRTP_MAGIC[2] && readByte(7) == ZRTP_MAGIC[3];
    }

    /* access modifiers changed from: protected */
    public void setSeqNum(short seq) {
        int at = 2 + 1;
        writeByte(2, (byte) (seq >> 8));
        writeByte(at, (byte) seq);
    }

    /* access modifiers changed from: protected */
    public void setSSRC(int ssrc) {
        writeInt(8, ssrc);
    }

    /* access modifiers changed from: protected */
    public boolean checkCrc() {
        return ZrtpCrc32.zrtpCheckCksum(getBuffer(), getOffset(), getLength() - 4, readInt(getLength() - 4));
    }

    /* access modifiers changed from: protected */
    public void setCrc() {
        writeInt(getLength() - 4, ZrtpCrc32.zrtpEndCksum(ZrtpCrc32.zrtpGenerateCksum(getBuffer(), getOffset(), getLength() - 4)));
    }
}
