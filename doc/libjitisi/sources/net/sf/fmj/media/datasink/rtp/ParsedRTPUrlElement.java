package net.sf.fmj.media.datasink.rtp;

public class ParsedRTPUrlElement {
    public static final String AUDIO = "audio";
    public static final String VIDEO = "video";
    public String host;
    public int port;
    public int ttl;
    public String type;

    public String toString() {
        return this.host + ":" + this.port + "/" + this.type + "/" + this.ttl;
    }
}
