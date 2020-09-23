package net.java.sip.communicator.impl.packetlogging;

import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.packetlogging.PacketLoggingConfiguration;

public class PacketLoggingConfigurationImpl extends PacketLoggingConfiguration {
    PacketLoggingConfigurationImpl() {
        ConfigurationService configService = PacketLoggingActivator.getConfigurationService();
        PacketLoggingConfigurationImpl.super.setGlobalLoggingEnabled(configService.getBoolean("net.java.sip.communicator.packetlogging.PACKET_LOGGING_ENABLED", isGlobalLoggingEnabled()));
        PacketLoggingConfigurationImpl.super.setSipLoggingEnabled(configService.getBoolean("net.java.sip.communicator.packetlogging.PACKET_LOGGING_SIP_ENABLED", isSipLoggingEnabled()));
        PacketLoggingConfigurationImpl.super.setJabberLoggingEnabled(configService.getBoolean("net.java.sip.communicator.packetlogging.PACKET_LOGGING_JABBER_ENABLED", isJabberLoggingEnabled()));
        PacketLoggingConfigurationImpl.super.setRTPLoggingEnabled(configService.getBoolean("net.java.sip.communicator.packetlogging.PACKET_LOGGING_RTP_ENABLED", isRTPLoggingEnabled()));
        PacketLoggingConfigurationImpl.super.setIce4JLoggingEnabled(configService.getBoolean("net.java.sip.communicator.packetlogging.PACKET_LOGGING_ICE4J_ENABLED", isIce4JLoggingEnabled()));
        PacketLoggingConfigurationImpl.super.setLimit(configService.getLong("net.java.sip.communicator.packetlogging.PACKET_LOGGING_FILE_SIZE", getLimit()));
        PacketLoggingConfigurationImpl.super.setLogfileCount(configService.getInt("net.java.sip.communicator.packetlogging.PACKET_LOGGING_FILE_COUNT", getLogfileCount()));
    }

    public void setGlobalLoggingEnabled(boolean enabled) {
        PacketLoggingConfigurationImpl.super.setGlobalLoggingEnabled(enabled);
        PacketLoggingActivator.getConfigurationService().setProperty("net.java.sip.communicator.packetlogging.PACKET_LOGGING_ENABLED", Boolean.valueOf(enabled));
    }

    public void setSipLoggingEnabled(boolean enabled) {
        PacketLoggingConfigurationImpl.super.setSipLoggingEnabled(enabled);
        PacketLoggingActivator.getConfigurationService().setProperty("net.java.sip.communicator.packetlogging.PACKET_LOGGING_SIP_ENABLED", Boolean.valueOf(enabled));
    }

    public void setJabberLoggingEnabled(boolean enabled) {
        PacketLoggingConfigurationImpl.super.setJabberLoggingEnabled(enabled);
        PacketLoggingActivator.getConfigurationService().setProperty("net.java.sip.communicator.packetlogging.PACKET_LOGGING_JABBER_ENABLED", Boolean.valueOf(enabled));
    }

    public void setRTPLoggingEnabled(boolean enabled) {
        PacketLoggingConfigurationImpl.super.setRTPLoggingEnabled(enabled);
        PacketLoggingActivator.getConfigurationService().setProperty("net.java.sip.communicator.packetlogging.PACKET_LOGGING_RTP_ENABLED", Boolean.valueOf(enabled));
    }

    public void setIce4JLoggingEnabled(boolean enabled) {
        PacketLoggingConfigurationImpl.super.setIce4JLoggingEnabled(enabled);
        PacketLoggingActivator.getConfigurationService().setProperty("net.java.sip.communicator.packetlogging.PACKET_LOGGING_ICE4J_ENABLED", Boolean.valueOf(enabled));
    }

    public void setLimit(long limit) {
        PacketLoggingConfigurationImpl.super.setLimit(limit);
        PacketLoggingActivator.getConfigurationService().setProperty("net.java.sip.communicator.packetlogging.PACKET_LOGGING_FILE_SIZE", Long.valueOf(limit));
    }

    public void setLogfileCount(int logfileCount) {
        PacketLoggingConfigurationImpl.super.setLogfileCount(logfileCount);
        PacketLoggingActivator.getConfigurationService().setProperty("net.java.sip.communicator.packetlogging.PACKET_LOGGING_FILE_COUNT", Integer.valueOf(logfileCount));
    }
}
