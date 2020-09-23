package org.jitsi.service.protocol;

public final class DTMFTone {
    public static final DTMFTone DTMF_0 = new DTMFTone("0");
    public static final DTMFTone DTMF_1 = new DTMFTone("1");
    public static final DTMFTone DTMF_2 = new DTMFTone("2");
    public static final DTMFTone DTMF_3 = new DTMFTone("3");
    public static final DTMFTone DTMF_4 = new DTMFTone("4");
    public static final DTMFTone DTMF_5 = new DTMFTone("5");
    public static final DTMFTone DTMF_6 = new DTMFTone("6");
    public static final DTMFTone DTMF_7 = new DTMFTone("7");
    public static final DTMFTone DTMF_8 = new DTMFTone("8");
    public static final DTMFTone DTMF_9 = new DTMFTone("9");
    public static final DTMFTone DTMF_A = new DTMFTone("A");
    public static final DTMFTone DTMF_B = new DTMFTone("B");
    public static final DTMFTone DTMF_C = new DTMFTone("C");
    public static final DTMFTone DTMF_D = new DTMFTone("D");
    public static final DTMFTone DTMF_SHARP = new DTMFTone("#");
    public static final DTMFTone DTMF_STAR = new DTMFTone("*");
    private final String value;

    private DTMFTone(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public boolean equals(Object target) {
        if (target instanceof DTMFTone) {
            return ((DTMFTone) target).value.equals(this.value);
        }
        return false;
    }

    public int hashCode() {
        return getValue().hashCode();
    }

    public static DTMFTone getDTMFTone(String value) {
        if (value == null) {
            return null;
        }
        if (value.equals(DTMF_0.getValue())) {
            return DTMF_0;
        }
        if (value.equals(DTMF_1.getValue())) {
            return DTMF_1;
        }
        if (value.equals(DTMF_2.getValue())) {
            return DTMF_2;
        }
        if (value.equals(DTMF_3.getValue())) {
            return DTMF_3;
        }
        if (value.equals(DTMF_4.getValue())) {
            return DTMF_4;
        }
        if (value.equals(DTMF_5.getValue())) {
            return DTMF_5;
        }
        if (value.equals(DTMF_6.getValue())) {
            return DTMF_6;
        }
        if (value.equals(DTMF_7.getValue())) {
            return DTMF_7;
        }
        if (value.equals(DTMF_8.getValue())) {
            return DTMF_8;
        }
        if (value.equals(DTMF_9.getValue())) {
            return DTMF_9;
        }
        if (value.equals(DTMF_A.getValue())) {
            return DTMF_A;
        }
        if (value.equals(DTMF_B.getValue())) {
            return DTMF_B;
        }
        if (value.equals(DTMF_C.getValue())) {
            return DTMF_C;
        }
        if (value.equals(DTMF_D.getValue())) {
            return DTMF_D;
        }
        if (value.equals(DTMF_SHARP.getValue())) {
            return DTMF_SHARP;
        }
        if (value.equals(DTMF_STAR.getValue())) {
            return DTMF_STAR;
        }
        return null;
    }
}
