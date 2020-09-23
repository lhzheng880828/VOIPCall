package org.dhcp4java;

import java.io.Serializable;
import java.util.Arrays;
import javax.sdp.SdpConstants;
import org.jitsi.gov.nist.core.Separators;

public class HardwareAddress implements Serializable {
    private static final byte HTYPE_ETHER = (byte) 1;
    private static final long serialVersionUID = 2;
    private final byte[] hardwareAddress;
    private final byte hardwareType;

    public HardwareAddress(byte[] macAddr) {
        this.hardwareType = (byte) 1;
        this.hardwareAddress = macAddr;
    }

    public HardwareAddress(byte hType, byte[] macAddr) {
        this.hardwareType = hType;
        this.hardwareAddress = macAddr;
    }

    public HardwareAddress(String macHex) {
        this(DHCPPacket.hex2Bytes(macHex));
    }

    public HardwareAddress(byte hType, String macHex) {
        this(hType, DHCPPacket.hex2Bytes(macHex));
    }

    public byte getHardwareType() {
        return this.hardwareType;
    }

    public byte[] getHardwareAddress() {
        return (byte[]) this.hardwareAddress.clone();
    }

    public int hashCode() {
        return this.hardwareType ^ Arrays.hashCode(this.hardwareAddress);
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof HardwareAddress)) {
            return false;
        }
        HardwareAddress hwAddr = (HardwareAddress) obj;
        if (this.hardwareType == hwAddr.hardwareType && Arrays.equals(this.hardwareAddress, hwAddr.hardwareAddress)) {
            return true;
        }
        return false;
    }

    public String getHardwareAddressHex() {
        return DHCPPacket.bytes2Hex(this.hardwareAddress);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(28);
        if (this.hardwareType != (byte) 1) {
            sb.append(this.hardwareType).append(Separators.SLASH);
        }
        for (int i = 0; i < this.hardwareAddress.length; i++) {
            if ((this.hardwareAddress[i] & 255) < 16) {
                sb.append(SdpConstants.RESERVED);
            }
            sb.append(Integer.toString(this.hardwareAddress[i] & 255, 16));
            if (i < this.hardwareAddress.length - 1) {
                sb.append(Separators.COLON);
            }
        }
        return sb.toString();
    }

    public static HardwareAddress getHardwareAddressByString(String macStr) {
        if (macStr == null) {
            throw new NullPointerException("macStr is null");
        }
        String[] macAdrItems = macStr.split(Separators.COLON);
        if (macAdrItems.length != 6) {
            throw new IllegalArgumentException("macStr[" + macStr + "] has not 6 items");
        }
        byte[] macBytes = new byte[6];
        for (int i = 0; i < 6; i++) {
            int val = Integer.parseInt(macAdrItems[i], 16);
            if (val < -128 || val > 255) {
                throw new IllegalArgumentException("Value is out of range:" + macAdrItems[i]);
            }
            macBytes[i] = (byte) val;
        }
        return new HardwareAddress(macBytes);
    }
}
