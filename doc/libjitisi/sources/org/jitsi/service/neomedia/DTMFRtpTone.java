package org.jitsi.service.neomedia;

import org.jitsi.service.protocol.DTMFTone;

public final class DTMFRtpTone {
    public static final DTMFRtpTone DTMF_0 = new DTMFRtpTone("0", (byte) 0);
    public static final DTMFRtpTone DTMF_1 = new DTMFRtpTone("1", (byte) 1);
    public static final DTMFRtpTone DTMF_2 = new DTMFRtpTone("2", (byte) 2);
    public static final DTMFRtpTone DTMF_3 = new DTMFRtpTone("3", (byte) 3);
    public static final DTMFRtpTone DTMF_4 = new DTMFRtpTone("4", (byte) 4);
    public static final DTMFRtpTone DTMF_5 = new DTMFRtpTone("5", (byte) 5);
    public static final DTMFRtpTone DTMF_6 = new DTMFRtpTone("6", (byte) 6);
    public static final DTMFRtpTone DTMF_7 = new DTMFRtpTone("7", (byte) 7);
    public static final DTMFRtpTone DTMF_8 = new DTMFRtpTone("8", (byte) 8);
    public static final DTMFRtpTone DTMF_9 = new DTMFRtpTone("9", (byte) 9);
    public static final DTMFRtpTone DTMF_A = new DTMFRtpTone("A", (byte) 12);
    public static final DTMFRtpTone DTMF_B = new DTMFRtpTone("B", (byte) 13);
    public static final DTMFRtpTone DTMF_C = new DTMFRtpTone("C", (byte) 14);
    public static final DTMFRtpTone DTMF_D = new DTMFRtpTone("D", (byte) 15);
    public static final DTMFRtpTone DTMF_SHARP = new DTMFRtpTone("#", (byte) 11);
    public static final DTMFRtpTone DTMF_STAR = new DTMFRtpTone("*", (byte) 10);
    private final byte code;
    private final String value;

    private DTMFRtpTone(String value, byte code) {
        this.value = value;
        this.code = code;
    }

    public String getValue() {
        return this.value;
    }

    public boolean equals(Object target) {
        if (target instanceof DTMFRtpTone) {
            return ((DTMFRtpTone) target).value.equals(this.value);
        }
        return false;
    }

    public int hashCode() {
        return getValue().hashCode();
    }

    public byte getCode() {
        return this.code;
    }

    public static DTMFRtpTone mapTone(DTMFTone tone) {
        if (tone.equals(DTMFTone.DTMF_0)) {
            return DTMF_0;
        }
        if (tone.equals(DTMFTone.DTMF_1)) {
            return DTMF_1;
        }
        if (tone.equals(DTMFTone.DTMF_2)) {
            return DTMF_2;
        }
        if (tone.equals(DTMFTone.DTMF_3)) {
            return DTMF_3;
        }
        if (tone.equals(DTMFTone.DTMF_4)) {
            return DTMF_4;
        }
        if (tone.equals(DTMFTone.DTMF_5)) {
            return DTMF_5;
        }
        if (tone.equals(DTMFTone.DTMF_6)) {
            return DTMF_6;
        }
        if (tone.equals(DTMFTone.DTMF_7)) {
            return DTMF_7;
        }
        if (tone.equals(DTMFTone.DTMF_8)) {
            return DTMF_8;
        }
        if (tone.equals(DTMFTone.DTMF_9)) {
            return DTMF_9;
        }
        if (tone.equals(DTMFTone.DTMF_A)) {
            return DTMF_A;
        }
        if (tone.equals(DTMFTone.DTMF_B)) {
            return DTMF_B;
        }
        if (tone.equals(DTMFTone.DTMF_C)) {
            return DTMF_C;
        }
        if (tone.equals(DTMFTone.DTMF_D)) {
            return DTMF_D;
        }
        if (tone.equals(DTMFTone.DTMF_SHARP)) {
            return DTMF_SHARP;
        }
        if (tone.equals(DTMFTone.DTMF_STAR)) {
            return DTMF_STAR;
        }
        return null;
    }
}
