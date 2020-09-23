package org.jitsi.impl.neomedia.codec;

import java.util.HashMap;
import java.util.Map;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.neomedia.format.MediaFormat;

public class EncodingConfigurationConfigImpl extends EncodingConfigurationImpl {
    private ConfigurationService configurationService = LibJitsi.getConfigurationService();
    private String propPrefix;

    public EncodingConfigurationConfigImpl(String prefix) {
        this.propPrefix = prefix;
        loadConfig();
    }

    private void loadConfig() {
        Map<String, String> properties = new HashMap();
        for (String pName : this.configurationService.getPropertyNamesByPrefix(this.propPrefix, false)) {
            properties.put(pName, this.configurationService.getString(pName));
        }
        loadProperties(properties);
    }

    public void setPriority(MediaFormat encoding, int priority) {
        super.setPriority(encoding, priority);
        this.configurationService.setProperty(this.propPrefix + "." + getEncodingPreferenceKey(encoding), Integer.valueOf(priority));
    }
}
