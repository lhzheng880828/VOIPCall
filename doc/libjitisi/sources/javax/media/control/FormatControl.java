package javax.media.control;

import javax.media.Control;
import javax.media.Format;

public interface FormatControl extends Control {
    Format getFormat();

    Format[] getSupportedFormats();

    boolean isEnabled();

    void setEnabled(boolean z);

    Format setFormat(Format format);
}
