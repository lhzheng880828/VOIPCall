package net.sf.fmj.media;

import javax.media.Buffer;
import javax.media.Format;

public interface ModuleListener {
    void bufferPrefetched(Module module);

    void dataBlocked(Module module, boolean z);

    void formatChanged(Module module, Format format, Format format2);

    void formatChangedFailure(Module module, Format format, Format format2);

    void framesBehind(Module module, float f, InputConnector inputConnector);

    void internalErrorOccurred(Module module);

    void markedDataArrived(Module module, Buffer buffer);

    void mediaEnded(Module module);

    void pluginTerminated(Module module);

    void resetted(Module module);

    void stopAtTime(Module module);
}
