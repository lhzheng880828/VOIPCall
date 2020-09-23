package javax.media.rtp;

import javax.media.Control;
import javax.media.Format;

public interface RTPControl extends Control {
    void addFormat(Format format, int i);

    Format getFormat();

    Format getFormat(int i);

    Format[] getFormatList();

    GlobalReceptionStats getGlobalStats();

    ReceptionStats getReceptionStats();
}
