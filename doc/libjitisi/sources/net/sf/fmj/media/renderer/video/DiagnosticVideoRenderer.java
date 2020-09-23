package net.sf.fmj.media.renderer.video;

import java.util.logging.Logger;
import javax.media.Buffer;
import javax.media.Control;
import javax.media.Format;
import javax.media.Renderer;
import javax.media.ResourceUnavailableException;
import javax.media.format.RGBFormat;
import javax.media.format.YUVFormat;
import net.sf.fmj.utility.LoggerSingleton;

public class DiagnosticVideoRenderer implements Renderer {
    private static final Logger logger = LoggerSingleton.logger;
    String name = "Disgnostic Video Renderer";
    int noFrames = 0;
    boolean started = false;
    Format[] supportedFormats = new Format[]{new RGBFormat(), new YUVFormat()};

    public synchronized void close() {
    }

    public Object getControl(String controlType) {
        try {
            Class<?> cls = Class.forName(controlType);
            Object[] cs = getControls();
            for (int i = 0; i < cs.length; i++) {
                if (cls.isInstance(cs[i])) {
                    return cs[i];
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public Object[] getControls() {
        return new Control[0];
    }

    public String getName() {
        return this.name;
    }

    public Format[] getSupportedInputFormats() {
        return this.supportedFormats;
    }

    public void open() throws ResourceUnavailableException {
    }

    public int process(Buffer buffer) {
        if (this.noFrames % 10 == 0) {
            logger.fine("Received frame " + this.noFrames);
        }
        this.noFrames++;
        return 0;
    }

    public void reset() {
    }

    public Format setInputFormat(Format format) {
        return format;
    }

    public void start() {
        this.started = true;
    }

    public void stop() {
        this.started = false;
    }
}
