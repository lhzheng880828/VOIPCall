package net.sf.fmj.media.rtp;

public class RTCPSDES {
    public RTCPSDESItem[] items;
    public int ssrc;

    public static String toString(RTCPSDES[] chunks) {
        String s = "";
        for (Object obj : chunks) {
            s = s + obj;
        }
        return s;
    }

    public String toString() {
        return "\t\tSource Description for sync source " + this.ssrc + ":\n" + RTCPSDESItem.toString(this.items);
    }
}
