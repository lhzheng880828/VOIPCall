package javax.media.format;

import javax.media.Controller;
import javax.media.ControllerEvent;
import javax.media.Format;

public class FormatChangeEvent extends ControllerEvent {
    protected Format newFormat;
    protected Format oldFormat;

    public FormatChangeEvent(Controller source) {
        super(source);
    }

    public FormatChangeEvent(Controller source, Format oldFormat, Format newFormat) {
        super(source);
        this.oldFormat = oldFormat;
        this.newFormat = newFormat;
    }

    public Format getNewFormat() {
        return this.newFormat;
    }

    public Format getOldFormat() {
        return this.oldFormat;
    }
}
