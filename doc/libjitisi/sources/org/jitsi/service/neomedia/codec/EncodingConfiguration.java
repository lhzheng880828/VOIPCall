package org.jitsi.service.neomedia.codec;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.format.MediaFormat;
import org.jitsi.util.Logger;

public abstract class EncodingConfiguration {
    private final Comparator<MediaFormat> encodingComparator = new Comparator<MediaFormat>() {
        public int compare(MediaFormat s1, MediaFormat s2) {
            return EncodingConfiguration.this.compareEncodingPreferences(s1, s2);
        }
    };
    protected final Map<String, Integer> encodingPreferences = new HashMap();
    private final Logger logger = Logger.getLogger(EncodingConfiguration.class);
    private Set<MediaFormat> supportedAudioEncodings;
    private Set<MediaFormat> supportedVideoEncodings;

    public abstract int compareEncodingPreferences(MediaFormat mediaFormat, MediaFormat mediaFormat2);

    public abstract MediaFormat[] getAllEncodings(MediaType mediaType);

    public abstract void setEncodingPreference(String str, double d, int i);

    private void updateSupportedEncodings() {
        this.supportedAudioEncodings = null;
        this.supportedVideoEncodings = null;
    }

    private Set<MediaFormat> updateSupportedEncodings(MediaType type) {
        Set<MediaFormat> enabled = new TreeSet(this.encodingComparator);
        for (MediaFormat format : getAllEncodings(type)) {
            if (getPriority(format) > 0) {
                enabled.add(format);
            }
        }
        return enabled;
    }

    public void setPriority(MediaFormat encoding, int priority) {
        setEncodingPreference(encoding.getEncoding(), encoding.getClockRate(), priority);
        updateSupportedEncodings();
    }

    public int getPriority(MediaFormat encoding) {
        Integer priority = (Integer) this.encodingPreferences.get(getEncodingPreferenceKey(encoding));
        return priority == null ? 0 : priority.intValue();
    }

    public MediaFormat[] getEnabledEncodings(MediaType type) {
        Set<MediaFormat> supportedEncodings;
        switch (type) {
            case AUDIO:
                if (this.supportedAudioEncodings == null) {
                    this.supportedAudioEncodings = updateSupportedEncodings(type);
                }
                supportedEncodings = this.supportedAudioEncodings;
                break;
            case VIDEO:
                if (this.supportedVideoEncodings == null) {
                    this.supportedVideoEncodings = updateSupportedEncodings(type);
                }
                supportedEncodings = this.supportedVideoEncodings;
                break;
            default:
                return new MediaFormat[0];
        }
        return (MediaFormat[]) supportedEncodings.toArray(new MediaFormat[supportedEncodings.size()]);
    }

    /* access modifiers changed from: protected */
    public String getEncodingPreferenceKey(MediaFormat encoding) {
        return encoding.getEncoding() + "/" + encoding.getClockRateString();
    }

    public void storeProperties(Map<String, String> properties, String prefix) {
        for (MediaType mediaType : MediaType.values()) {
            for (MediaFormat mediaFormat : getAllEncodings(mediaType)) {
                properties.put(prefix + getEncodingPreferenceKey(mediaFormat), Integer.toString(getPriority(mediaFormat)));
            }
        }
    }

    public void storeProperties(Map<String, String> properties) {
        storeProperties(properties, "");
    }

    public void loadProperties(Map<String, String> properties) {
        loadProperties(properties, "");
    }

    public void loadProperties(Map<String, String> properties, String prefix) {
        for (Entry<String, String> entry : properties.entrySet()) {
            String pName = (String) entry.getKey();
            String prefStr = (String) entry.getValue();
            if (pName.startsWith(prefix)) {
                String fmtName;
                if (pName.contains(".")) {
                    fmtName = pName.substring(pName.lastIndexOf(46) + 1);
                } else {
                    fmtName = pName;
                }
                if (fmtName.contains("sdp")) {
                    fmtName = fmtName.replaceAll("sdp", "");
                    if (properties.containsKey(pName.replaceAll("sdp", ""))) {
                    }
                }
                try {
                    String encoding;
                    double clockRate;
                    int preference = Integer.parseInt(prefStr);
                    int encodingClockRateSeparator = fmtName.lastIndexOf(47);
                    if (encodingClockRateSeparator > -1) {
                        encoding = fmtName.substring(0, encodingClockRateSeparator);
                        clockRate = Double.parseDouble(fmtName.substring(encodingClockRateSeparator + 1));
                    } else {
                        encoding = fmtName;
                        clockRate = -1.0d;
                    }
                    setEncodingPreference(encoding, clockRate, preference);
                } catch (NumberFormatException nfe) {
                    this.logger.warn("Failed to parse format (" + fmtName + ") or preference (" + prefStr + ").", nfe);
                }
            }
        }
        updateSupportedEncodings();
    }

    public void loadEncodingConfiguration(EncodingConfiguration encodingConfiguration) {
        Map<String, String> properties = new HashMap();
        encodingConfiguration.storeProperties(properties);
        loadProperties(properties);
    }

    public boolean hasEnabledFormat(MediaType mediaType) {
        return getEnabledEncodings(mediaType).length > 0;
    }
}
