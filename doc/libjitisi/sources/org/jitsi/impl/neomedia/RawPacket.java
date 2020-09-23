package org.jitsi.impl.neomedia;

import com.lti.utils.UnsignedUtils;

public class RawPacket {
    public static final int EXT_HEADER_SIZE = 4;
    public static final int FIXED_HEADER_SIZE = 12;
    private byte[] buffer;
    private int length;
    private int offset;

    public RawPacket(byte[] buffer, int offset, int length) {
        this.buffer = buffer;
        this.offset = offset;
        this.length = length;
    }

    public void addExtension(byte[] extBuff, int newExtensionLen) {
        int i;
        int newBuffLen = (this.length + this.offset) + newExtensionLen;
        int bufferOffset = this.offset;
        int newBufferOffset = this.offset;
        int lengthToCopy = (getCsrcCount() * 4) + 12;
        boolean extensionBit = getExtensionBit();
        if (extensionBit) {
            lengthToCopy += 2;
        } else {
            newBuffLen += 4;
        }
        byte[] newBuffer = new byte[newBuffLen];
        System.arraycopy(this.buffer, bufferOffset, newBuffer, newBufferOffset, lengthToCopy);
        newBuffer[newBufferOffset] = (byte) (newBuffer[newBufferOffset] | 16);
        bufferOffset += lengthToCopy;
        newBufferOffset += lengthToCopy;
        int totalExtensionLen = newExtensionLen + getExtensionLength();
        if (extensionBit) {
            bufferOffset += 4;
        } else {
            i = newBufferOffset + 1;
            newBuffer[newBufferOffset] = (byte) -66;
            newBufferOffset = i + 1;
            newBuffer[i] = (byte) -34;
        }
        int lengthInWords = (totalExtensionLen + 3) / 4;
        i = newBufferOffset + 1;
        newBuffer[newBufferOffset] = (byte) (lengthInWords >> 8);
        newBufferOffset = i + 1;
        newBuffer[i] = (byte) lengthInWords;
        if (extensionBit) {
            lengthToCopy = getExtensionLength();
            System.arraycopy(this.buffer, bufferOffset, newBuffer, newBufferOffset, lengthToCopy);
            bufferOffset += lengthToCopy;
            newBufferOffset += lengthToCopy;
        }
        System.arraycopy(extBuff, 0, newBuffer, newBufferOffset, newExtensionLen);
        newBufferOffset += newExtensionLen;
        System.arraycopy(this.buffer, bufferOffset, newBuffer, newBufferOffset, getPayloadLength());
        newBufferOffset += getPayloadLength();
        this.buffer = newBuffer;
        this.length = newBufferOffset - this.offset;
    }

    public void append(byte[] data, int len) {
        if (data != null && len != 0) {
            if (this.length + len > this.buffer.length - this.offset) {
                byte[] newBuffer = new byte[(this.length + len)];
                System.arraycopy(this.buffer, this.offset, newBuffer, 0, this.length);
                this.offset = 0;
                this.buffer = newBuffer;
            }
            System.arraycopy(data, 0, this.buffer, this.length, len);
            this.length += len;
        }
    }

    public long[] extractCsrcAudioLevels(byte csrcExtID) {
        long[] csrcLevels = null;
        if (getExtensionBit() && getExtensionLength() != 0) {
            int csrcCount = getCsrcCount();
            if (csrcCount != 0) {
                csrcLevels = new long[(csrcCount * 2)];
                int i = 0;
                int csrcStartIndex = this.offset + 12;
                while (i < csrcCount) {
                    int csrcLevelsIndex = i * 2;
                    csrcLevels[csrcLevelsIndex] = 4294967295L & ((long) readInt(csrcStartIndex));
                    csrcLevels[csrcLevelsIndex + 1] = (long) getCsrcAudioLevel(csrcExtID, i);
                    i++;
                    csrcStartIndex += 4;
                }
            }
        }
        return csrcLevels;
    }

    public long[] extractCsrcList() {
        int csrcCount = getCsrcCount();
        long[] csrcList = new long[csrcCount];
        int i = 0;
        int csrcStartIndex = this.offset + 12;
        while (i < csrcCount) {
            csrcList[i] = (long) readInt(csrcStartIndex);
            i++;
            csrcStartIndex += 4;
        }
        return csrcList;
    }

    public byte extractSsrcAudioLevel(byte ssrcExtID) {
        return getCsrcAudioLevel(ssrcExtID, 0);
    }

    private int findExtension(int extensionID) {
        if (!getExtensionBit() || getExtensionLength() == 0) {
            return 0;
        }
        int extOffset = ((this.offset + 12) + (getCsrcCount() * 4)) + 4;
        int extensionEnd = extOffset + getExtensionLength();
        int extHdrLen = getExtensionHeaderLength();
        if (extHdrLen != 1 && extHdrLen != 2) {
            return -1;
        }
        while (extOffset < extensionEnd) {
            int currType;
            int currLen;
            if (extHdrLen == 1) {
                currType = this.buffer[extOffset] >> 4;
                currLen = (this.buffer[extOffset] & 15) + 1;
                extOffset++;
            } else {
                currType = this.buffer[extOffset];
                currLen = this.buffer[extOffset + 1];
                extOffset += 2;
            }
            if (currType == extensionID) {
                return extOffset;
            }
            extOffset += currLen;
        }
        return -1;
    }

    public byte[] getBuffer() {
        return this.buffer;
    }

    private byte getCsrcAudioLevel(byte csrcExtID, int index) {
        if (!getExtensionBit() || getExtensionLength() == 0) {
            return (byte) 0;
        }
        int levelsStart = findExtension(csrcExtID);
        if (levelsStart == -1 || getLengthForExtension(levelsStart) < index) {
            return (byte) 0;
        }
        return (byte) (this.buffer[levelsStart + index] & 127);
    }

    public int getCsrcCount() {
        return this.buffer[this.offset] & 15;
    }

    public boolean getExtensionBit() {
        return (this.buffer[this.offset] & 16) == 16;
    }

    private int getExtensionHeaderLength() {
        if (!getExtensionBit()) {
            return -1;
        }
        int extLenIndex = (this.offset + 12) + (getCsrcCount() * 4);
        if (this.buffer[extLenIndex] == (byte) -66 && this.buffer[extLenIndex + 1] == (byte) -34) {
            return 1;
        }
        if (this.buffer[extLenIndex] == (byte) 16 && (this.buffer[extLenIndex + 1] >> 4) == 0) {
            return 2;
        }
        return -1;
    }

    public int getExtensionLength() {
        if (!getExtensionBit()) {
            return 0;
        }
        int extLenIndex = ((this.offset + 12) + (getCsrcCount() * 4)) + 2;
        return ((this.buffer[extLenIndex] << 8) | (this.buffer[extLenIndex + 1] & UnsignedUtils.MAX_UBYTE)) * 4;
    }

    public int getHeaderExtensionType() {
        if (getExtensionBit()) {
            return readUnsignedShortAsInt((this.offset + 12) + (getCsrcCount() * 4));
        }
        return 0;
    }

    public int getHeaderLength() {
        int headerLength = (getCsrcCount() * 4) + 12;
        if (getExtensionBit()) {
            return headerLength + (getExtensionLength() + 4);
        }
        return headerLength;
    }

    public int getLength() {
        return this.length;
    }

    private int getLengthForExtension(int contentStart) {
        if (getExtensionHeaderLength() == 1) {
            return (this.buffer[contentStart - 1] & 15) + 1;
        }
        return this.buffer[contentStart - 1];
    }

    public int getOffset() {
        return this.offset;
    }

    public int getPaddingSize() {
        if ((this.buffer[this.offset] & 32) == 0) {
            return 0;
        }
        return this.buffer[(this.offset + this.length) - 1];
    }

    public byte[] getPayload() {
        return readRegion(getHeaderLength(), getPayloadLength());
    }

    public int getPayloadLength() {
        return this.length - getHeaderLength();
    }

    public byte getPayloadType() {
        return (byte) (this.buffer[this.offset + 1] & 127);
    }

    public int getRTCPSSRC() {
        return readInt(4);
    }

    public int getSequenceNumber() {
        return readUnsignedShortAsInt(2);
    }

    public int getSRTCPIndex(int authTagLen) {
        return readInt(getLength() - (authTagLen + 4));
    }

    public int getSSRC() {
        return readInt(8);
    }

    public long getTimestamp() {
        return (long) readInt(4);
    }

    public void grow(int howMuch) {
        if (howMuch != 0) {
            byte[] newBuffer = new byte[(this.length + howMuch)];
            System.arraycopy(this.buffer, this.offset, newBuffer, 0, this.length);
            this.offset = 0;
            this.buffer = newBuffer;
        }
    }

    public boolean isInvalid() {
        return this.buffer == null || this.buffer.length < this.offset + this.length || this.length < 12;
    }

    public boolean isPacketMarked() {
        return (this.buffer[this.offset + 1] & 128) != 0;
    }

    public byte readByte(int off) {
        return this.buffer[this.offset + off];
    }

    public int readInt(int off) {
        off += this.offset;
        int off2 = off + 1;
        off = off2 + 1;
        return ((((this.buffer[off] & UnsignedUtils.MAX_UBYTE) << 24) | ((this.buffer[off2] & UnsignedUtils.MAX_UBYTE) << 16)) | ((this.buffer[off] & UnsignedUtils.MAX_UBYTE) << 8)) | (this.buffer[off + 1] & UnsignedUtils.MAX_UBYTE);
    }

    public byte[] readRegion(int off, int len) {
        int startOffset = this.offset + off;
        if (off < 0 || len <= 0 || startOffset + len > this.buffer.length) {
            return null;
        }
        byte[] region = new byte[len];
        System.arraycopy(this.buffer, startOffset, region, 0, len);
        return region;
    }

    public void readRegionToBuff(int off, int len, byte[] outBuff) {
        int startOffset = this.offset + off;
        if (off >= 0 && len > 0 && startOffset + len <= this.buffer.length && outBuff.length >= len) {
            System.arraycopy(this.buffer, startOffset, outBuff, 0, len);
        }
    }

    public short readShort(int off) {
        return (short) ((this.buffer[(this.offset + off) + 0] << 8) | (this.buffer[(this.offset + off) + 1] & UnsignedUtils.MAX_UBYTE));
    }

    public byte[] readTimeStampIntoByteArray() {
        return readRegion(4, 4);
    }

    public long readUnsignedIntAsLong(int off) {
        return ((long) (((((this.buffer[(this.offset + off) + 0] & UnsignedUtils.MAX_UBYTE) << 24) | ((this.buffer[(this.offset + off) + 1] & UnsignedUtils.MAX_UBYTE) << 16)) | ((this.buffer[(this.offset + off) + 2] & UnsignedUtils.MAX_UBYTE) << 8)) | (this.buffer[(this.offset + off) + 3] & UnsignedUtils.MAX_UBYTE))) & 4294967295L;
    }

    public int readUnsignedShortAsInt(int off) {
        return ((this.buffer[(this.offset + off) + 0] & UnsignedUtils.MAX_UBYTE) << 8) | (this.buffer[(this.offset + off) + 1] & UnsignedUtils.MAX_UBYTE);
    }

    public void removeExtension() {
        if (getExtensionBit()) {
            int payloadOffset = this.offset + getHeaderLength();
            int extHeaderLen = getExtensionLength() + 4;
            System.arraycopy(this.buffer, payloadOffset, this.buffer, payloadOffset - extHeaderLen, getPayloadLength());
            this.length -= extHeaderLen;
            setExtensionBit(false);
        }
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public void setCsrcList(long[] newCsrcList) {
        int newCsrcCount = newCsrcList.length;
        byte[] csrcBuff = new byte[(newCsrcCount * 4)];
        int csrcOffset = 0;
        for (long csrc : newCsrcList) {
            csrcBuff[csrcOffset] = (byte) ((int) (csrc >> 24));
            csrcBuff[csrcOffset + 1] = (byte) ((int) (csrc >> 16));
            csrcBuff[csrcOffset + 2] = (byte) ((int) (csrc >> 8));
            csrcBuff[csrcOffset + 3] = (byte) ((int) csrc);
            csrcOffset += 4;
        }
        int oldCsrcCount = getCsrcCount();
        byte[] oldBuffer = getBuffer();
        byte[] newBuffer = new byte[(((this.length + this.offset) + csrcBuff.length) - (oldCsrcCount * 4))];
        System.arraycopy(oldBuffer, 0, newBuffer, 0, this.offset + 12);
        System.arraycopy(csrcBuff, 0, newBuffer, this.offset + 12, csrcBuff.length);
        int payloadOffsetForOldBuff = (this.offset + 12) + (oldCsrcCount * 4);
        int payloadOffsetForNewBuff = (this.offset + 12) + (newCsrcCount * 4);
        System.arraycopy(oldBuffer, payloadOffsetForOldBuff, newBuffer, payloadOffsetForNewBuff, this.length - payloadOffsetForOldBuff);
        newBuffer[this.offset] = (byte) ((newBuffer[this.offset] & 240) | newCsrcCount);
        this.buffer = newBuffer;
        this.length = ((this.length + payloadOffsetForNewBuff) - payloadOffsetForOldBuff) - this.offset;
    }

    private void setExtensionBit(boolean extBit) {
        byte[] bArr;
        int i;
        if (extBit) {
            bArr = this.buffer;
            i = this.offset;
            bArr[i] = (byte) (bArr[i] | 16);
            return;
        }
        bArr = this.buffer;
        i = this.offset;
        bArr[i] = (byte) (bArr[i] & 239);
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setMarker(boolean marker) {
        byte[] bArr;
        int i;
        if (marker) {
            bArr = this.buffer;
            i = this.offset + 1;
            bArr[i] = (byte) (bArr[i] | -128);
            return;
        }
        bArr = this.buffer;
        i = this.offset + 1;
        bArr[i] = (byte) (bArr[i] & 127);
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setPayload(byte payload) {
        this.buffer[this.offset + 1] = (byte) ((this.buffer[this.offset + 1] & 128) | ((byte) (payload & 127)));
    }

    public void setTimestamp(long timestamp) {
        writeInt(4, (int) timestamp);
    }

    public void shrink(int len) {
        if (len > 0) {
            this.length -= len;
            if (this.length < 0) {
                this.length = 0;
            }
        }
    }

    public void writeByte(int off, byte b) {
        this.buffer[this.offset + off] = b;
    }

    public void writeInt(int off, int data) {
        int off2 = off + 1;
        this.buffer[this.offset + off] = (byte) (data >> 24);
        off = off2 + 1;
        this.buffer[this.offset + off2] = (byte) (data >> 16);
        off2 = off + 1;
        this.buffer[this.offset + off] = (byte) (data >> 8);
        this.buffer[this.offset + off2] = (byte) data;
    }
}
