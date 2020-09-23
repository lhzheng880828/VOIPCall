package org.dhcp4java;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DHCPPacket implements Cloneable, Serializable {
    static final /* synthetic */ boolean $assertionsDisabled = (!DHCPPacket.class.desiredAssertionStatus() ? true : $assertionsDisabled);
    private static final char[] hex = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final Logger logger = Logger.getLogger(DHCPPacket.class.getName().toLowerCase());
    private static final long serialVersionUID = 1;
    private InetAddress address;
    private byte[] chaddr = new byte[16];
    private byte[] ciaddr = new byte[4];
    private String comment = "";
    private byte[] file = new byte[128];
    private short flags;
    private byte[] giaddr = new byte[4];
    private byte hlen = (byte) 6;
    private byte hops;
    private byte htype = (byte) 1;
    private boolean isDhcp = true;
    private byte op = (byte) 2;
    private Map<Byte, DHCPOption> options = new LinkedHashMap();
    private byte[] padding = new byte[0];
    private int port;
    private short secs;
    private byte[] siaddr = new byte[4];
    private byte[] sname = new byte[64];
    private boolean truncated;
    private int xid;
    private byte[] yiaddr = new byte[4];

    public static DHCPPacket getPacket(DatagramPacket datagram) throws DHCPBadPacketException {
        if (datagram == null) {
            throw new IllegalArgumentException("datagram is null");
        }
        DHCPPacket packet = new DHCPPacket();
        packet.marshall(datagram.getData(), datagram.getOffset(), datagram.getLength(), datagram.getAddress(), datagram.getPort(), true);
        return packet;
    }

    public static DHCPPacket getPacket(byte[] buf, int offset, int length, boolean strict) throws DHCPBadPacketException {
        DHCPPacket packet = new DHCPPacket();
        packet.marshall(buf, offset, length, null, 0, strict);
        return packet;
    }

    public DHCPPacket clone() {
        try {
            DHCPPacket p = (DHCPPacket) super.clone();
            p.ciaddr = (byte[]) this.ciaddr.clone();
            p.yiaddr = (byte[]) this.yiaddr.clone();
            p.siaddr = (byte[]) this.siaddr.clone();
            p.giaddr = (byte[]) this.giaddr.clone();
            p.chaddr = (byte[]) this.chaddr.clone();
            p.sname = (byte[]) this.sname.clone();
            p.file = (byte[]) this.file.clone();
            p.options = new LinkedHashMap(this.options);
            p.padding = (byte[]) this.padding.clone();
            p.truncated = $assertionsDisabled;
            return p;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    public boolean equals(Object o) {
        int i = 1;
        if (o == this) {
            return true;
        }
        if (!(o instanceof DHCPPacket)) {
            return $assertionsDisabled;
        }
        int i2;
        DHCPPacket p = (DHCPPacket) o;
        boolean b = this.comment.equals(p.comment) & (this.op == p.op ? 1 : 0);
        if (this.htype == p.htype) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        b &= i2;
        if (this.hlen == p.hlen) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        b &= i2;
        if (this.hops == p.hops) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        b &= i2;
        if (this.xid == p.xid) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        b &= i2;
        if (this.secs == p.secs) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        b &= i2;
        if (this.flags == p.flags) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        b = ((((((((b & i2) & Arrays.equals(this.ciaddr, p.ciaddr)) & Arrays.equals(this.yiaddr, p.yiaddr)) & Arrays.equals(this.siaddr, p.siaddr)) & Arrays.equals(this.giaddr, p.giaddr)) & Arrays.equals(this.chaddr, p.chaddr)) & Arrays.equals(this.sname, p.sname)) & Arrays.equals(this.file, p.file)) & this.options.equals(p.options);
        if (this.isDhcp == p.isDhcp) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        b = ((b & i2) & Arrays.equals(this.padding, p.padding)) & equalsStatic(this.address, p.address);
        if (this.port != p.port) {
            i = 0;
        }
        return b & i;
    }

    public int hashCode() {
        int i = 0;
        int h = (((((((((((((((((-1 ^ this.comment.hashCode()) + this.op) + this.htype) + this.hlen) + this.hops) + this.xid) + this.secs) ^ this.flags) ^ Arrays.hashCode(this.ciaddr)) ^ Arrays.hashCode(this.yiaddr)) ^ Arrays.hashCode(this.siaddr)) ^ Arrays.hashCode(this.giaddr)) ^ Arrays.hashCode(this.chaddr)) ^ Arrays.hashCode(this.sname)) ^ Arrays.hashCode(this.file)) ^ this.options.hashCode()) + (this.isDhcp ? 1 : 0)) ^ Arrays.hashCode(this.padding);
        if (this.address != null) {
            i = this.address.hashCode();
        }
        return (h ^ i) + this.port;
    }

    private static boolean equalsStatic(Object a, Object b) {
        if (a == null) {
            return b == null ? true : $assertionsDisabled;
        } else {
            return a.equals(b);
        }
    }

    private void assertInvariants() {
        if (!$assertionsDisabled && this.comment == null) {
            throw new AssertionError();
        } else if (!$assertionsDisabled && this.ciaddr == null) {
            throw new AssertionError();
        } else if (!$assertionsDisabled && this.ciaddr.length != 4) {
            throw new AssertionError();
        } else if (!$assertionsDisabled && this.yiaddr == null) {
            throw new AssertionError();
        } else if (!$assertionsDisabled && this.yiaddr.length != 4) {
            throw new AssertionError();
        } else if (!$assertionsDisabled && this.siaddr == null) {
            throw new AssertionError();
        } else if (!$assertionsDisabled && this.siaddr.length != 4) {
            throw new AssertionError();
        } else if (!$assertionsDisabled && this.giaddr == null) {
            throw new AssertionError();
        } else if (!$assertionsDisabled && this.giaddr.length != 4) {
            throw new AssertionError();
        } else if (!$assertionsDisabled && this.chaddr == null) {
            throw new AssertionError();
        } else if (!$assertionsDisabled && this.chaddr.length != 16) {
            throw new AssertionError();
        } else if (!$assertionsDisabled && this.sname == null) {
            throw new AssertionError();
        } else if (!$assertionsDisabled && this.sname.length != 64) {
            throw new AssertionError();
        } else if (!$assertionsDisabled && this.file == null) {
            throw new AssertionError();
        } else if (!$assertionsDisabled && this.file.length != 128) {
            throw new AssertionError();
        } else if (!$assertionsDisabled && this.padding == null) {
            throw new AssertionError();
        } else if ($assertionsDisabled || this.options != null) {
            for (Entry<Byte, DHCPOption> mapEntry : this.options.entrySet()) {
                Byte key = (Byte) mapEntry.getKey();
                DHCPOption opt = (DHCPOption) mapEntry.getValue();
                if (!$assertionsDisabled && key == null) {
                    throw new AssertionError();
                } else if (!$assertionsDisabled && key.byteValue() == (byte) 0) {
                    throw new AssertionError();
                } else if (!$assertionsDisabled && key.byteValue() == (byte) -1) {
                    throw new AssertionError();
                } else if (!$assertionsDisabled && opt == null) {
                    throw new AssertionError();
                } else if (!$assertionsDisabled && opt.getCode() != key.byteValue()) {
                    throw new AssertionError();
                } else if (!$assertionsDisabled && opt.getValueFast() == null) {
                    throw new AssertionError();
                }
            }
        } else {
            throw new AssertionError();
        }
    }

    /* access modifiers changed from: protected */
    public DHCPPacket marshall(byte[] buffer, int offset, int length, InetAddress address0, int port0, boolean strict) {
        if (buffer == null) {
            throw new IllegalArgumentException("null buffer not allowed");
        } else if (offset < 0) {
            throw new IndexOutOfBoundsException("negative offset not allowed");
        } else if (length < 0) {
            throw new IllegalArgumentException("negative length not allowed");
        } else if (buffer.length < offset + length) {
            throw new IndexOutOfBoundsException("offset+length exceeds buffer length");
        } else if (length < 236) {
            throw new DHCPBadPacketException("DHCP Packet too small (" + length + ") absolute minimum is " + 236);
        } else if (length > 1500) {
            throw new DHCPBadPacketException("DHCP Packet too big (" + length + ") max MTU is " + 1500);
        } else {
            this.address = address0;
            this.port = port0;
            try {
                ByteArrayInputStream inBStream = new ByteArrayInputStream(buffer, offset, length);
                DataInputStream inStream = new DataInputStream(inBStream);
                this.op = inStream.readByte();
                this.htype = inStream.readByte();
                this.hlen = inStream.readByte();
                this.hops = inStream.readByte();
                this.xid = inStream.readInt();
                this.secs = inStream.readShort();
                this.flags = inStream.readShort();
                inStream.readFully(this.ciaddr, 0, 4);
                inStream.readFully(this.yiaddr, 0, 4);
                inStream.readFully(this.siaddr, 0, 4);
                inStream.readFully(this.giaddr, 0, 4);
                inStream.readFully(this.chaddr, 0, 16);
                inStream.readFully(this.sname, 0, 64);
                inStream.readFully(this.file, 0, 128);
                this.isDhcp = true;
                inBStream.mark(4);
                if (inStream.readInt() != 1669485411) {
                    this.isDhcp = $assertionsDisabled;
                    inBStream.reset();
                }
                if (this.isDhcp) {
                    int type = 0;
                    while (true) {
                        int r = inBStream.read();
                        if (r >= 0) {
                            type = (byte) r;
                            if (type != 0) {
                                if (type == -1) {
                                    break;
                                }
                                r = inBStream.read();
                                if (r < 0) {
                                    break;
                                }
                                byte[] unit_opt = new byte[Math.min(r, inBStream.available())];
                                inBStream.read(unit_opt);
                                setOption(new DHCPOption((byte) type, unit_opt));
                            }
                        } else {
                            break;
                        }
                    }
                    this.truncated = type != -1 ? true : $assertionsDisabled;
                    if (strict && this.truncated) {
                        throw new DHCPBadPacketException("Packet seams to be truncated");
                    }
                }
                this.padding = new byte[inBStream.available()];
                inBStream.read(this.padding);
                assertInvariants();
                return this;
            } catch (IOException e) {
                throw new DHCPBadPacketException("IOException: " + e.toString(), e);
            }
        }
    }

    public byte[] serialize() {
        int minLen = 236;
        if (this.isDhcp) {
            minLen = 236 + 64;
        }
        return serialize(minLen, 576);
    }

    public byte[] serialize(int minSize, int maxSize) {
        assertInvariants();
        ByteArrayOutputStream outBStream = new ByteArrayOutputStream(750);
        DataOutputStream outStream = new DataOutputStream(outBStream);
        try {
            outStream.writeByte(this.op);
            outStream.writeByte(this.htype);
            outStream.writeByte(this.hlen);
            outStream.writeByte(this.hops);
            outStream.writeInt(this.xid);
            outStream.writeShort(this.secs);
            outStream.writeShort(this.flags);
            outStream.write(this.ciaddr, 0, 4);
            outStream.write(this.yiaddr, 0, 4);
            outStream.write(this.siaddr, 0, 4);
            outStream.write(this.giaddr, 0, 4);
            outStream.write(this.chaddr, 0, 16);
            outStream.write(this.sname, 0, 64);
            outStream.write(this.file, 0, 128);
            if (this.isDhcp) {
                outStream.writeInt(1669485411);
                for (DHCPOption opt : getOptionsCollection()) {
                    if (!$assertionsDisabled && opt == null) {
                        throw new AssertionError();
                    } else if (!$assertionsDisabled && opt.getCode() == (byte) 0) {
                        throw new AssertionError();
                    } else if (!$assertionsDisabled && opt.getCode() == (byte) -1) {
                        throw new AssertionError();
                    } else if ($assertionsDisabled || opt.getValueFast() != null) {
                        int size = opt.getValueFast().length;
                        if (!$assertionsDisabled && size < 0) {
                            throw new AssertionError();
                        } else if (size > 255) {
                            throw new DHCPBadPacketException("Options larger than 255 bytes are not yet supported");
                        } else {
                            outStream.writeByte(opt.getCode());
                            outStream.writeByte(size);
                            outStream.write(opt.getValueFast());
                        }
                    } else {
                        throw new AssertionError();
                    }
                }
                outStream.writeByte(-1);
            }
            outStream.write(this.padding);
            int min_padding = minSize - outBStream.size();
            if (min_padding > 0) {
                outStream.write(new byte[min_padding]);
            }
            byte[] data = outBStream.toByteArray();
            if (data.length <= 1500) {
                return data;
            }
            throw new DHCPBadPacketException("serialize: packet too big (" + data.length + " greater than max MAX_MTU (" + 1500 + ')');
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unexpected Exception", e);
            throw new DHCPBadPacketException("IOException raised: " + e.toString());
        }
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        try {
            String hostAddress;
            StringBuilder append = buffer.append(this.isDhcp ? "DHCP Packet" : "BOOTP Packet").append("\ncomment=").append(this.comment).append("\naddress=");
            if (this.address != null) {
                hostAddress = this.address.getHostAddress();
            } else {
                hostAddress = "";
            }
            append.append(hostAddress).append('(').append(this.port).append(')').append("\nop=");
            Object bootName = DHCPConstants._BOOT_NAMES.get(Byte.valueOf(this.op));
            if (bootName != null) {
                buffer.append(bootName).append('(').append(this.op).append(')');
            } else {
                buffer.append(this.op);
            }
            buffer.append("\nhtype=");
            Object htypeName = DHCPConstants._HTYPE_NAMES.get(Byte.valueOf(this.htype));
            if (htypeName != null) {
                buffer.append(htypeName).append('(').append(this.htype).append(')');
            } else {
                buffer.append(this.htype);
            }
            buffer.append("\nhlen=").append(this.hlen).append("\nhops=").append(this.hops).append("\nxid=0x");
            appendHex(buffer, this.xid);
            buffer.append("\nsecs=").append(this.secs).append("\nflags=0x").append(Integer.toHexString(this.flags)).append("\nciaddr=");
            appendHostAddress(buffer, InetAddress.getByAddress(this.ciaddr));
            buffer.append("\nyiaddr=");
            appendHostAddress(buffer, InetAddress.getByAddress(this.yiaddr));
            buffer.append("\nsiaddr=");
            appendHostAddress(buffer, InetAddress.getByAddress(this.siaddr));
            buffer.append("\ngiaddr=");
            appendHostAddress(buffer, InetAddress.getByAddress(this.giaddr));
            buffer.append("\nchaddr=0x");
            appendChaddrAsHex(buffer);
            buffer.append("\nsname=").append(getSname()).append("\nfile=").append(getFile());
            if (this.isDhcp) {
                buffer.append("\nOptions follows:");
                for (DHCPOption opt : getOptionsCollection()) {
                    buffer.append(10);
                    opt.append(buffer);
                }
            }
            buffer.append("\npadding[").append(this.padding.length).append("]=");
            appendHex(buffer, this.padding);
        } catch (Exception e) {
        }
        return buffer.toString();
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public byte[] getChaddr() {
        return (byte[]) this.chaddr.clone();
    }

    private StringBuilder appendChaddrAsHex(StringBuilder buffer) {
        appendHex(buffer, this.chaddr, 0, this.hlen & 255);
        return buffer;
    }

    public HardwareAddress getHardwareAddress() {
        int len = this.hlen & 255;
        if (len > 16) {
            len = 16;
        }
        byte[] buf = new byte[len];
        System.arraycopy(this.chaddr, 0, buf, 0, len);
        return new HardwareAddress(this.htype, buf);
    }

    public String getChaddrAsHex() {
        return appendChaddrAsHex(new StringBuilder(this.hlen & 255)).toString();
    }

    public void setChaddr(byte[] chaddr) {
        if (chaddr == null) {
            Arrays.fill(this.chaddr, (byte) 0);
        } else if (chaddr.length > this.chaddr.length) {
            throw new IllegalArgumentException("chaddr is too long: " + chaddr.length + ", max is: " + this.chaddr.length);
        } else {
            Arrays.fill(this.chaddr, (byte) 0);
            System.arraycopy(chaddr, 0, this.chaddr, 0, chaddr.length);
        }
    }

    public void setChaddrHex(String hex) {
        setChaddr(hex2Bytes(hex));
    }

    public InetAddress getCiaddr() {
        try {
            return InetAddress.getByAddress(getCiaddrRaw());
        } catch (UnknownHostException e) {
            logger.log(Level.SEVERE, "Unexpected UnknownHostException", e);
            return null;
        }
    }

    public byte[] getCiaddrRaw() {
        return (byte[]) this.ciaddr.clone();
    }

    public void setCiaddr(InetAddress ciaddr) {
        if (ciaddr instanceof Inet4Address) {
            setCiaddrRaw(ciaddr.getAddress());
            return;
        }
        throw new IllegalArgumentException("Inet4Address required");
    }

    public void setCiaddr(String ciaddr) throws UnknownHostException {
        setCiaddr(InetAddress.getByName(ciaddr));
    }

    public void setCiaddrRaw(byte[] ciaddr) {
        if (ciaddr.length != 4) {
            throw new IllegalArgumentException("4-byte array required");
        }
        System.arraycopy(ciaddr, 0, this.ciaddr, 0, 4);
    }

    public byte[] getFileRaw() {
        return (byte[]) this.file.clone();
    }

    public String getFile() {
        return bytesToString(getFileRaw());
    }

    public void setFile(String file) {
        setFileRaw(stringToBytes(file));
    }

    public void setFileRaw(byte[] file) {
        if (file == null) {
            Arrays.fill(this.file, (byte) 0);
        } else if (file.length > this.file.length) {
            throw new IllegalArgumentException("File is too long:" + file.length + " max is:" + this.file.length);
        } else {
            Arrays.fill(this.file, (byte) 0);
            System.arraycopy(file, 0, this.file, 0, file.length);
        }
    }

    public short getFlags() {
        return this.flags;
    }

    public void setFlags(short flags) {
        this.flags = flags;
    }

    public InetAddress getGiaddr() {
        try {
            return InetAddress.getByAddress(getGiaddrRaw());
        } catch (UnknownHostException e) {
            logger.log(Level.SEVERE, "Unexpected UnknownHostException", e);
            return null;
        }
    }

    public byte[] getGiaddrRaw() {
        return (byte[]) this.giaddr.clone();
    }

    public void setGiaddr(InetAddress giaddr) {
        if (giaddr instanceof Inet4Address) {
            setGiaddrRaw(giaddr.getAddress());
            return;
        }
        throw new IllegalArgumentException("Inet4Address required");
    }

    public void setGiaddr(String giaddr) throws UnknownHostException {
        setGiaddr(InetAddress.getByName(giaddr));
    }

    public void setGiaddrRaw(byte[] giaddr) {
        if (giaddr.length != 4) {
            throw new IllegalArgumentException("4-byte array required");
        }
        System.arraycopy(giaddr, 0, this.giaddr, 0, 4);
    }

    public byte getHlen() {
        return this.hlen;
    }

    public void setHlen(byte hlen) {
        this.hlen = hlen;
    }

    public byte getHops() {
        return this.hops;
    }

    public void setHops(byte hops) {
        this.hops = hops;
    }

    public byte getHtype() {
        return this.htype;
    }

    public void setHtype(byte htype) {
        this.htype = htype;
    }

    public boolean isDhcp() {
        return this.isDhcp;
    }

    public void setDhcp(boolean isDhcp) {
        this.isDhcp = isDhcp;
    }

    public byte getOp() {
        return this.op;
    }

    public void setOp(byte op) {
        this.op = op;
    }

    public byte[] getPadding() {
        return (byte[]) this.padding.clone();
    }

    public void setPadding(byte[] padding) {
        this.padding = padding == null ? new byte[0] : (byte[]) padding.clone();
    }

    public void setPaddingWithZeroes(int length) {
        if (length < 0) {
            length = 0;
        }
        if (length > 1500) {
            throw new IllegalArgumentException("length is > 1500");
        }
        setPadding(new byte[length]);
    }

    public short getSecs() {
        return this.secs;
    }

    public void setSecs(short secs) {
        this.secs = secs;
    }

    public InetAddress getSiaddr() {
        try {
            return InetAddress.getByAddress(getSiaddrRaw());
        } catch (UnknownHostException e) {
            logger.log(Level.SEVERE, "Unexpected UnknownHostException", e);
            return null;
        }
    }

    public byte[] getSiaddrRaw() {
        return (byte[]) this.siaddr.clone();
    }

    public void setSiaddr(InetAddress siaddr) {
        if (siaddr instanceof Inet4Address) {
            setSiaddrRaw(siaddr.getAddress());
            return;
        }
        throw new IllegalArgumentException("Inet4Address required");
    }

    public void setSiaddr(String siaddr) throws UnknownHostException {
        setSiaddr(InetAddress.getByName(siaddr));
    }

    public void setSiaddrRaw(byte[] siaddr) {
        if (siaddr.length != 4) {
            throw new IllegalArgumentException("4-byte array required");
        }
        System.arraycopy(siaddr, 0, this.siaddr, 0, 4);
    }

    public byte[] getSnameRaw() {
        return (byte[]) this.sname.clone();
    }

    public String getSname() {
        return bytesToString(getSnameRaw());
    }

    public void setSname(String sname) {
        setSnameRaw(stringToBytes(sname));
    }

    public void setSnameRaw(byte[] sname) {
        if (sname == null) {
            Arrays.fill(this.sname, (byte) 0);
        } else if (sname.length > this.sname.length) {
            throw new IllegalArgumentException("Sname is too long:" + sname.length + " max is:" + this.sname.length);
        } else {
            Arrays.fill(this.sname, (byte) 0);
            System.arraycopy(sname, 0, this.sname, 0, sname.length);
        }
    }

    public int getXid() {
        return this.xid;
    }

    public void setXid(int xid) {
        this.xid = xid;
    }

    public InetAddress getYiaddr() {
        try {
            return InetAddress.getByAddress(getYiaddrRaw());
        } catch (UnknownHostException e) {
            logger.log(Level.SEVERE, "Unexpected UnknownHostException", e);
            return null;
        }
    }

    public byte[] getYiaddrRaw() {
        return (byte[]) this.yiaddr.clone();
    }

    public void setYiaddr(InetAddress yiaddr) {
        if (yiaddr instanceof Inet4Address) {
            setYiaddrRaw(yiaddr.getAddress());
            return;
        }
        throw new IllegalArgumentException("Inet4Address required");
    }

    public void setYiaddr(String yiaddr) throws UnknownHostException {
        setYiaddr(InetAddress.getByName(yiaddr));
    }

    public void setYiaddrRaw(byte[] yiaddr) {
        if (yiaddr.length != 4) {
            throw new IllegalArgumentException("4-byte array required");
        }
        System.arraycopy(yiaddr, 0, this.yiaddr, 0, 4);
    }

    public Byte getDHCPMessageType() {
        return getOptionAsByte(DHCPConstants.DHO_DHCP_MESSAGE_TYPE);
    }

    public void setDHCPMessageType(byte optionType) {
        setOptionAsByte(DHCPConstants.DHO_DHCP_MESSAGE_TYPE, optionType);
    }

    public boolean isTruncated() {
        return this.truncated;
    }

    public Integer getOptionAsNum(byte code) {
        DHCPOption opt = getOption(code);
        return opt != null ? opt.getValueAsNum() : null;
    }

    public Byte getOptionAsByte(byte code) throws IllegalArgumentException {
        DHCPOption opt = getOption(code);
        return opt == null ? null : Byte.valueOf(opt.getValueAsByte());
    }

    public Short getOptionAsShort(byte code) throws IllegalArgumentException {
        DHCPOption opt = getOption(code);
        return opt == null ? null : Short.valueOf(opt.getValueAsShort());
    }

    public Integer getOptionAsInteger(byte code) throws IllegalArgumentException {
        DHCPOption opt = getOption(code);
        return opt == null ? null : Integer.valueOf(opt.getValueAsInt());
    }

    public InetAddress getOptionAsInetAddr(byte code) throws IllegalArgumentException {
        DHCPOption opt = getOption(code);
        return opt == null ? null : opt.getValueAsInetAddr();
    }

    public String getOptionAsString(byte code) throws IllegalArgumentException {
        DHCPOption opt = getOption(code);
        return opt == null ? null : opt.getValueAsString();
    }

    public short[] getOptionAsShorts(byte code) throws IllegalArgumentException {
        DHCPOption opt = getOption(code);
        return opt == null ? null : opt.getValueAsShorts();
    }

    public InetAddress[] getOptionAsInetAddrs(byte code) throws IllegalArgumentException {
        DHCPOption opt = getOption(code);
        return opt == null ? null : opt.getValueAsInetAddrs();
    }

    public byte[] getOptionAsBytes(byte code) throws IllegalArgumentException {
        DHCPOption opt = getOption(code);
        return opt == null ? null : opt.getValueAsBytes();
    }

    public void setOptionAsByte(byte code, byte val) {
        setOption(DHCPOption.newOptionAsByte(code, val));
    }

    public void setOptionAsShort(byte code, short val) {
        setOption(DHCPOption.newOptionAsShort(code, val));
    }

    public void setOptionAsInt(byte code, int val) {
        setOption(DHCPOption.newOptionAsInt(code, val));
    }

    public void setOptionAsInetAddress(byte code, InetAddress val) {
        setOption(DHCPOption.newOptionAsInetAddress(code, val));
    }

    public void setOptionAsInetAddress(byte code, String val) throws UnknownHostException {
        setOption(DHCPOption.newOptionAsInetAddress(code, InetAddress.getByName(val)));
    }

    public void setOptionAsInetAddresses(byte code, InetAddress[] val) {
        setOption(DHCPOption.newOptionAsInetAddresses(code, val));
    }

    public void setOptionAsString(byte code, String val) {
        setOption(DHCPOption.newOptionAsString(code, val));
    }

    public byte[] getOptionRaw(byte code) {
        DHCPOption opt = getOption(code);
        return opt == null ? null : opt.getValueFast();
    }

    public DHCPOption getOption(byte code) {
        DHCPOption opt = (DHCPOption) this.options.get(Byte.valueOf(code));
        if (opt == null) {
            return null;
        }
        if (!$assertionsDisabled && opt.getCode() != code) {
            throw new AssertionError();
        } else if ($assertionsDisabled || opt.getValueFast() != null) {
            return opt;
        } else {
            throw new AssertionError();
        }
    }

    public boolean containsOption(byte code) {
        return this.options.containsKey(Byte.valueOf(code));
    }

    public Collection<DHCPOption> getOptionsCollection() {
        return Collections.unmodifiableCollection(this.options.values());
    }

    public DHCPOption[] getOptionsArray() {
        return (DHCPOption[]) this.options.values().toArray(new DHCPOption[this.options.size()]);
    }

    public void setOptionRaw(byte code, byte[] buf) {
        if (buf == null) {
            removeOption(code);
        } else {
            setOption(new DHCPOption(code, buf));
        }
    }

    public void setOption(DHCPOption opt) {
        if (opt == null) {
            return;
        }
        if (opt.getValueFast() == null) {
            removeOption(opt.getCode());
        } else {
            this.options.put(Byte.valueOf(opt.getCode()), opt);
        }
    }

    public void setOptions(DHCPOption[] opts) {
        if (opts != null) {
            for (DHCPOption opt : opts) {
                setOption(opt);
            }
        }
    }

    public void setOptions(Collection<DHCPOption> opts) {
        if (opts != null) {
            for (DHCPOption opt : opts) {
                setOption(opt);
            }
        }
    }

    public void removeOption(byte opt) {
        this.options.remove(Byte.valueOf(opt));
    }

    public void removeAllOptions() {
        this.options.clear();
    }

    public InetAddress getAddress() {
        return this.address;
    }

    public void setAddress(InetAddress address) {
        if (address == null) {
            this.address = null;
        } else if (address instanceof Inet4Address) {
            this.address = address;
        } else {
            throw new IllegalArgumentException("only IPv4 addresses accepted");
        }
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public InetSocketAddress getAddrPort() {
        return new InetSocketAddress(this.address, this.port);
    }

    public void setAddrPort(InetSocketAddress addrPort) {
        if (addrPort == null) {
            setAddress(null);
            setPort(0);
            return;
        }
        setAddress(addrPort.getAddress());
        setPort(addrPort.getPort());
    }

    static String bytesToString(byte[] buf) {
        if (buf == null) {
            return "";
        }
        return bytesToString(buf, 0, buf.length);
    }

    static String bytesToString(byte[] buf, int src, int len) {
        if (buf == null) {
            return "";
        }
        if (src < 0) {
            len += src;
            src = 0;
        }
        if (len <= 0) {
            return "";
        }
        if (src >= buf.length) {
            return "";
        }
        int i;
        if (src + len > buf.length) {
            len = buf.length - src;
        }
        for (i = src; i < src + len; i++) {
            if (buf[i] == (byte) 0) {
                len = i - src;
                break;
            }
        }
        char[] chars = new char[len];
        for (i = src; i < src + len; i++) {
            chars[i - src] = (char) buf[i];
        }
        return new String(chars);
    }

    static void appendHex(StringBuilder sbuf, byte b) {
        int i = b & 255;
        sbuf.append(hex[(i & 240) >> 4]).append(hex[i & 15]);
    }

    static void appendHex(StringBuilder sbuf, byte[] buf, int src, int len) {
        if (buf != null) {
            if (src < 0) {
                len += src;
                src = 0;
            }
            if (len > 0 && src < buf.length) {
                if (src + len > buf.length) {
                    len = buf.length - src;
                }
                for (int i = src; i < src + len; i++) {
                    appendHex(sbuf, buf[i]);
                }
            }
        }
    }

    static void appendHex(StringBuilder sbuf, byte[] buf) {
        appendHex(sbuf, buf, 0, buf.length);
    }

    static String bytes2Hex(byte[] buf) {
        if (buf == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(buf.length * 2);
        appendHex(sb, buf);
        return sb.toString();
    }

    static byte[] hex2Bytes(String s) {
        if ((s.length() & 1) != 0) {
            throw new IllegalArgumentException("String length must be even: " + s.length());
        }
        byte[] buf = new byte[(s.length() / 2)];
        for (int index = 0; index < buf.length; index++) {
            int stringIndex = index << 1;
            buf[index] = (byte) Integer.parseInt(s.substring(stringIndex, stringIndex + 2), 16);
        }
        return buf;
    }

    private static void appendHex(StringBuilder sbuf, int i) {
        appendHex(sbuf, (byte) ((-16777216 & i) >>> 24));
        appendHex(sbuf, (byte) ((16711680 & i) >>> 16));
        appendHex(sbuf, (byte) ((65280 & i) >>> 8));
        appendHex(sbuf, (byte) (i & 255));
    }

    public static byte[] stringToBytes(String str) {
        if (str == null) {
            return null;
        }
        char[] chars = str.toCharArray();
        int len = chars.length;
        byte[] buf = new byte[len];
        for (int i = 0; i < len; i++) {
            buf[i] = (byte) chars[i];
        }
        return buf;
    }

    public static void appendHostAddress(StringBuilder sbuf, InetAddress addr) {
        if (addr == null) {
            throw new IllegalArgumentException("addr must not be null");
        } else if (addr instanceof Inet4Address) {
            byte[] src = addr.getAddress();
            sbuf.append(src[0] & 255).append('.').append(src[1] & 255).append('.').append(src[2] & 255).append('.').append(src[3] & 255);
        } else {
            throw new IllegalArgumentException("addr must be an instance of Inet4Address");
        }
    }

    public static String getHostAddress(InetAddress addr) {
        StringBuilder sbuf = new StringBuilder(15);
        appendHostAddress(sbuf, addr);
        return sbuf.toString();
    }
}
