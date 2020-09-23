package net.sf.fmj.media.rtp;

public class RTCPSDESItem {
    public static final int CNAME = 1;
    public static final int EMAIL = 3;
    public static final int HIGHEST = 8;
    public static final int LOC = 5;
    public static final int NAME = 2;
    public static final int NOTE = 7;
    public static final int PHONE = 4;
    public static final int PRIV = 8;
    public static final int TOOL = 6;
    public static final String[] names = new String[]{"CNAME", "NAME", "EMAIL", "PHONE", "LOC", "TOOL", "NOTE", "PRIV"};
    public byte[] data;
    public int type;

    public static String toString(RTCPSDESItem[] items) {
        String s = "";
        for (Object obj : items) {
            s = s + obj;
        }
        return s;
    }

    public RTCPSDESItem(int type, String s) {
        this.type = type;
        this.data = new byte[s.length()];
        this.data = s.getBytes();
    }

    public String toString() {
        return "\t\t\t" + names[this.type - 1] + ": " + new String(this.data) + "\n";
    }
}
