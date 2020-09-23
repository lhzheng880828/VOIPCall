package org.jitsi.service.neomedia.control;

import java.util.Map;
import javax.media.Control;

public interface FormatParametersAwareCodec extends Control {
    void setFormatParameters(Map<String, String> map);
}
