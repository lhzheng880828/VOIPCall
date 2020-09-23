package javax.media.rtp.rtcp;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class SourceDescription implements Serializable {
    public static final int SOURCE_DESC_CNAME = 1;
    public static final int SOURCE_DESC_EMAIL = 3;
    public static final int SOURCE_DESC_LOC = 5;
    public static final int SOURCE_DESC_NAME = 2;
    public static final int SOURCE_DESC_NOTE = 7;
    public static final int SOURCE_DESC_PHONE = 4;
    public static final int SOURCE_DESC_PRIV = 8;
    public static final int SOURCE_DESC_TOOL = 6;
    private String description;
    private boolean encrypted;
    private int frequency;
    private int type;

    public static String generateCNAME() {
        try {
            return System.getProperty("user.name") + '@' + InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public SourceDescription(int type, String description, int frequency, boolean encrypted) {
        this.type = type;
        this.description = description;
        this.frequency = frequency;
        this.encrypted = encrypted;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean getEncrypted() {
        return this.encrypted;
    }

    public int getFrequency() {
        return this.frequency;
    }

    public int getType() {
        return this.type;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }
}
