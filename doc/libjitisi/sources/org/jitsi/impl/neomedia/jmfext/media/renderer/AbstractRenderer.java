package org.jitsi.impl.neomedia.jmfext.media.renderer;

import javax.media.Format;
import javax.media.Renderer;
import org.jitsi.impl.neomedia.control.ControlsAdapter;
import org.jitsi.util.Logger;

public abstract class AbstractRenderer<T extends Format> extends ControlsAdapter implements Renderer {
    private static final Logger logger = Logger.getLogger(AbstractRenderer.class);
    protected T inputFormat;

    public void reset() {
    }

    public Format setInputFormat(Format format) {
        T matchingFormat = null;
        for (Format supportedInputFormat : getSupportedInputFormats()) {
            if (supportedInputFormat.matches(format)) {
                matchingFormat = supportedInputFormat.intersects(format);
                break;
            }
        }
        if (matchingFormat == null) {
            return null;
        }
        this.inputFormat = matchingFormat;
        return this.inputFormat;
    }

    public static void useThreadPriority(int threadPriority) {
        Throwable throwable = null;
        try {
            Thread.currentThread().setPriority(threadPriority);
        } catch (IllegalArgumentException iae) {
            throwable = iae;
        } catch (SecurityException se) {
            throwable = se;
        }
        if (throwable != null) {
            logger.warn("Failed to use thread priority: " + threadPriority, throwable);
        }
    }
}
