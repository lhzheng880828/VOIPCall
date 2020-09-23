package org.jitsi.service.packetlogging;

public class PacketLoggingConfiguration {
    public static final String PACKET_LOGGING_ENABLED_PROPERTY_NAME = "net.java.sip.communicator.packetlogging.PACKET_LOGGING_ENABLED";
    public static final String PACKET_LOGGING_FILE_COUNT_PROPERTY_NAME = "net.java.sip.communicator.packetlogging.PACKET_LOGGING_FILE_COUNT";
    public static final String PACKET_LOGGING_FILE_SIZE_PROPERTY_NAME = "net.java.sip.communicator.packetlogging.PACKET_LOGGING_FILE_SIZE";
    public static final String PACKET_LOGGING_ICE4J_ENABLED_PROPERTY_NAME = "net.java.sip.communicator.packetlogging.PACKET_LOGGING_ICE4J_ENABLED";
    public static final String PACKET_LOGGING_JABBER_ENABLED_PROPERTY_NAME = "net.java.sip.communicator.packetlogging.PACKET_LOGGING_JABBER_ENABLED";
    public static final String PACKET_LOGGING_RTP_ENABLED_PROPERTY_NAME = "net.java.sip.communicator.packetlogging.PACKET_LOGGING_RTP_ENABLED";
    public static final String PACKET_LOGGING_SIP_ENABLED_PROPERTY_NAME = "net.java.sip.communicator.packetlogging.PACKET_LOGGING_SIP_ENABLED";
    private boolean globalLoggingEnabled = true;
    private boolean ice4jLoggingEnabled = true;
    private boolean jabberLoggingEnabled = true;
    private long limit = 5000000;
    private int logfileCount = 3;
    private boolean rtpLoggingEnabled = true;
    private boolean sipLoggingEnabled = true;

    public boolean isGlobalLoggingEnabled() {
        return this.globalLoggingEnabled;
    }

    public boolean isSipLoggingEnabled() {
        return this.sipLoggingEnabled;
    }

    public boolean isJabberLoggingEnabled() {
        return this.jabberLoggingEnabled;
    }

    public boolean isRTPLoggingEnabled() {
        return this.rtpLoggingEnabled;
    }

    public boolean isIce4JLoggingEnabled() {
        return this.ice4jLoggingEnabled;
    }

    public long getLimit() {
        return this.limit;
    }

    public int getLogfileCount() {
        return this.logfileCount;
    }

    public void setGlobalLoggingEnabled(boolean enabled) {
        if (!enabled) {
            this.sipLoggingEnabled = false;
            this.jabberLoggingEnabled = false;
            this.rtpLoggingEnabled = false;
            this.ice4jLoggingEnabled = false;
        }
        this.globalLoggingEnabled = enabled;
    }

    public void setSipLoggingEnabled(boolean enabled) {
        this.sipLoggingEnabled = enabled;
    }

    public void setJabberLoggingEnabled(boolean enabled) {
        this.jabberLoggingEnabled = enabled;
    }

    public void setRTPLoggingEnabled(boolean enabled) {
        this.rtpLoggingEnabled = true;
    }

    public void setIce4JLoggingEnabled(boolean enabled) {
        this.ice4jLoggingEnabled = true;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public void setLogfileCount(int logfileCount) {
        this.logfileCount = logfileCount;
    }
}
