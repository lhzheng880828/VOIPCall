package org.jitsi.impl.neomedia.transform.zrtp;

import gnu.java.zrtp.ZrtpConfigure;
import gnu.java.zrtp.ZrtpConstants.SupportedAuthLengths;
import gnu.java.zrtp.ZrtpConstants.SupportedHashes;
import gnu.java.zrtp.ZrtpConstants.SupportedPubKeys;
import gnu.java.zrtp.ZrtpConstants.SupportedSASTypes;
import gnu.java.zrtp.ZrtpConstants.SupportedSymCiphers;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.libjitsi.LibJitsi;

public class ZrtpConfigureUtils {
    public static <T extends Enum<T>> String getPropertyID(T algo) {
        return "net.java.sip.communicator." + algo.getDeclaringClass().getName().replace('$', '_');
    }

    public static ZrtpConfigure getZrtpConfiguration() {
        ZrtpConfigure active = new ZrtpConfigure();
        setupConfigure(SupportedPubKeys.DH2K, active);
        setupConfigure(SupportedHashes.S256, active);
        setupConfigure(SupportedSymCiphers.AES1, active);
        setupConfigure(SupportedSASTypes.B32, active);
        setupConfigure(SupportedAuthLengths.HS32, active);
        return active;
    }

    private static <T extends Enum<T>> void setupConfigure(T algo, ZrtpConfigure active) {
        ConfigurationService cfg = LibJitsi.getConfigurationService();
        String savedConf = null;
        if (cfg != null) {
            savedConf = cfg.getString(getPropertyID(algo));
        }
        if (savedConf == null) {
            savedConf = "";
        }
        Class<T> clazz = algo.getDeclaringClass();
        for (String str : savedConf.split(";")) {
            try {
                T algoEnum = Enum.valueOf(clazz, str);
                if (algoEnum != null) {
                    active.addAlgo(algoEnum);
                }
            } catch (IllegalArgumentException e) {
            }
        }
    }
}
