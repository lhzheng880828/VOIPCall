package org.jitsi.impl.neomedia.format;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.media.Format;
import javax.media.format.VideoFormat;
import org.jitsi.android.util.java.awt.Dimension;

public class ParameterizedVideoFormat extends VideoFormat {
    private static final long serialVersionUID = 0;
    private Map<String, String> fmtps;

    public ParameterizedVideoFormat(String encoding, Dimension size, int maxDataLength, Class<?> dataType, float frameRate, Map<String, String> fmtps) {
        super(encoding, size, maxDataLength, dataType, frameRate);
        Map hashMap = (fmtps == null || fmtps.isEmpty()) ? MediaFormatImpl.EMPTY_FORMAT_PARAMETERS : new HashMap(fmtps);
        this.fmtps = hashMap;
    }

    public ParameterizedVideoFormat(String encoding, Map<String, String> fmtps) {
        super(encoding);
        Map hashMap = (fmtps == null || fmtps.isEmpty()) ? MediaFormatImpl.EMPTY_FORMAT_PARAMETERS : new HashMap(fmtps);
        this.fmtps = hashMap;
    }

    public ParameterizedVideoFormat(String encoding, String... fmtps) {
        this(encoding, toMap(fmtps));
    }

    public Object clone() {
        ParameterizedVideoFormat f = new ParameterizedVideoFormat(getEncoding(), getSize(), getMaxDataLength(), getDataType(), getFrameRate(), null);
        f.copy(this);
        return f;
    }

    /* access modifiers changed from: protected */
    public void copy(Format f) {
        super.copy(f);
        if (f instanceof ParameterizedVideoFormat) {
            Map<String, String> pvfFmtps = ((ParameterizedVideoFormat) f).getFormatParameters();
            Map hashMap = (pvfFmtps == null || pvfFmtps.isEmpty()) ? MediaFormatImpl.EMPTY_FORMAT_PARAMETERS : new HashMap(pvfFmtps);
            this.fmtps = hashMap;
        }
    }

    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        Map<String, String> objFmtps = null;
        if (obj instanceof ParameterizedVideoFormat) {
            objFmtps = ((ParameterizedVideoFormat) obj).getFormatParameters();
        }
        return VideoMediaFormatImpl.formatParametersAreEqual(getEncoding(), getFormatParameters(), objFmtps);
    }

    public boolean formatParametersMatch(Format format) {
        Map<String, String> formatFmtps = null;
        if (format instanceof ParameterizedVideoFormat) {
            formatFmtps = ((ParameterizedVideoFormat) format).getFormatParameters();
        }
        return VideoMediaFormatImpl.formatParametersMatch(getEncoding(), getFormatParameters(), formatFmtps);
    }

    public String getFormatParameter(String name) {
        return (String) this.fmtps.get(name);
    }

    public Map<String, String> getFormatParameters() {
        return new HashMap(this.fmtps);
    }

    public Format intersects(Format format) {
        Format intersection = super.intersects(format);
        if (intersection == null) {
            return null;
        }
        ((ParameterizedVideoFormat) intersection).fmtps = this.fmtps.isEmpty() ? MediaFormatImpl.EMPTY_FORMAT_PARAMETERS : getFormatParameters();
        return intersection;
    }

    public boolean matches(Format format) {
        return super.matches(format) && formatParametersMatch(format);
    }

    public static <T> Map<T, T> toMap(T... entries) {
        if (entries == null || entries.length == 0) {
            return null;
        }
        Map<T, T> map = new HashMap();
        int i = 0;
        while (i < entries.length) {
            int i2 = i + 1;
            map.put(entries[i], entries[i2]);
            i = i2 + 1;
        }
        return map;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(super.toString());
        s.append(", fmtps={");
        for (Entry<String, String> fmtp : this.fmtps.entrySet()) {
            s.append((String) fmtp.getKey());
            s.append('=');
            s.append((String) fmtp.getValue());
            s.append(',');
        }
        int lastIndex = s.length() - 1;
        if (s.charAt(lastIndex) == ',') {
            s.setCharAt(lastIndex, '}');
        } else {
            s.append('}');
        }
        return s.toString();
    }
}
