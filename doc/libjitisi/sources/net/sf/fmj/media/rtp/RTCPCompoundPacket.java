package net.sf.fmj.media.rtp;

import com.lti.utils.UnsignedUtils;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.sf.fmj.media.rtp.util.Packet;

public class RTCPCompoundPacket extends RTCPPacket {
    RTCPPacket[] packets;

    public RTCPCompoundPacket(Packet base) {
        super(base);
        this.type = -1;
    }

    public RTCPCompoundPacket(RTCPPacket[] packets) {
        this.packets = packets;
        this.type = -1;
        this.received = false;
    }

    /* access modifiers changed from: 0000 */
    public void assemble(DataOutputStream out) throws IOException {
        throw new IllegalArgumentException("Recursive Compound Packet");
    }

    public void assemble(int len, boolean encrypted) {
        this.length = len;
        this.offset = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(len);
        DataOutputStream out = new DataOutputStream(baos);
        if (encrypted) {
            try {
                this.offset += 4;
            } catch (IOException e) {
                throw new NullPointerException("Impossible IO Exception");
            }
        }
        int laststart = this.offset;
        for (RTCPPacket assemble : this.packets) {
            laststart = baos.size();
            assemble.assemble(out);
        }
        int prelen = baos.size();
        this.data = baos.toByteArray();
        if (prelen > len) {
            throw new NullPointerException("RTCP Packet overflow");
        } else if (prelen < len) {
            byte[] bArr;
            if (this.data.length < len) {
                bArr = this.data;
                byte[] bArr2 = new byte[len];
                this.data = bArr2;
                System.arraycopy(bArr, 0, bArr2, 0, prelen);
            }
            bArr = this.data;
            bArr[laststart] = (byte) (bArr[laststart] | 32);
            this.data[len - 1] = (byte) (len - prelen);
            int temp = (this.data[laststart + 3] & UnsignedUtils.MAX_UBYTE) + ((len - prelen) >> 2);
            if (temp >= 256) {
                bArr = this.data;
                int i = laststart + 2;
                bArr[i] = (byte) (bArr[i] + ((len - prelen) >> 10));
            }
            this.data[laststart + 3] = (byte) temp;
        }
    }

    public int calcLength() {
        int len = 0;
        if (this.packets == null || this.packets.length < 1) {
            throw new IllegalArgumentException("Bad RTCP Compound Packet");
        }
        for (RTCPPacket calcLength : this.packets) {
            len += calcLength.calcLength();
        }
        return len;
    }

    public String toString() {
        return "RTCP Packet with the following subpackets:\n" + toString(this.packets);
    }

    public String toString(RTCPPacket[] packets) {
        String s = "";
        for (Object obj : packets) {
            s = s + obj;
        }
        return s;
    }
}
