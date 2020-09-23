package net.sf.fmj.utility;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.Codec;
import javax.media.Demultiplexer;
import javax.media.Effect;
import javax.media.Format;
import javax.media.Multiplexer;
import javax.media.PlugInManager;
import javax.media.Renderer;
import javax.media.protocol.ContentDescriptor;

public class PlugInUtility {
    private static final boolean TRACE = false;
    private static final Logger logger = LoggerSingleton.logger;

    public static PlugInInfo getPlugInInfo(String className) {
        try {
            Object o = Class.forName(className).newInstance();
            if (o instanceof Demultiplexer) {
                ContentDescriptor[] contentDescriptors = ((Demultiplexer) o).getSupportedInputContentDescriptors();
                Format[] formats = new Format[contentDescriptors.length];
                for (int i = 0; i < contentDescriptors.length; i++) {
                    formats[i] = contentDescriptors[i];
                }
                return new PlugInInfo(className, formats, new Format[0], 1);
            } else if (o instanceof Codec) {
                int i2;
                Codec oCast = (Codec) o;
                Format[] inputFormats = oCast.getSupportedInputFormats();
                Format[] outputFormats = oCast.getSupportedOutputFormats(null);
                if (o instanceof Effect) {
                    i2 = 3;
                } else {
                    i2 = 2;
                }
                return new PlugInInfo(className, inputFormats, outputFormats, i2);
            } else if (o instanceof Renderer) {
                return new PlugInInfo(className, ((Renderer) o).getSupportedInputFormats(), new Format[0], 4);
            } else {
                if (o instanceof Multiplexer) {
                    return new PlugInInfo(className, new Format[0], ((Multiplexer) o).getSupportedOutputContentDescriptors(null), 5);
                }
                logger.warning("PlugInUtility: Unknown or unsupported plug-in: " + o.getClass());
                return null;
            }
        } catch (Throwable e) {
            logger.log(Level.FINE, "PlugInUtility: Unable to get plugin info for " + className + ": " + e);
            return null;
        }
    }

    public static boolean registerPlugIn(String className) {
        boolean z = false;
        PlugInInfo i = getPlugInInfo(className);
        if (i == null) {
            return z;
        }
        try {
            return PlugInManager.addPlugIn(i.className, i.in, i.out, i.type);
        } catch (Throwable e) {
            logger.fine("PlugInUtility: Unable to register plugin " + className + ": " + e);
            return z;
        }
    }
}
