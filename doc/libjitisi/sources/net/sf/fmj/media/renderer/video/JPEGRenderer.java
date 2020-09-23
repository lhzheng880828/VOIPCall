package net.sf.fmj.media.renderer.video;

import com.lti.utils.StringUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.JPEGFormat;
import javax.media.format.VideoFormat;
import javax.media.renderer.VideoRenderer;
import net.sf.fmj.media.AbstractVideoRenderer;
import net.sf.fmj.utility.LoggerSingleton;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.android.util.java.awt.Image;

public class JPEGRenderer extends AbstractVideoRenderer implements VideoRenderer {
    private static final Logger logger = LoggerSingleton.logger;
    private JVideoComponent component = new JVideoComponent();
    private Object[] controls = new Object[]{this};
    private boolean scale;
    private final Format[] supportedInputFormats = new Format[]{new JPEGFormat()};

    public int doProcess(Buffer buffer) {
        if (buffer.isEOM()) {
            logger.warning(getClass().getSimpleName() + "passed buffer with EOM flag");
            return 0;
        } else if (buffer.getData() == null) {
            logger.warning("buffer.getData() == null, eom=" + buffer.isEOM());
            return 1;
        } else if (buffer.getLength() == 0) {
            logger.warning("buffer.getLength() == 0, eom=" + buffer.isEOM());
            return 1;
        } else if (buffer.isDiscard()) {
            logger.warning("JPEGRenderer passed buffer with discard flag");
            return 1;
        } else {
            try {
                Image image = ImageIO.read(new ByteArrayInputStream((byte[]) buffer.getData(), buffer.getOffset(), buffer.getLength()));
                if (image == null) {
                    logger.log(Level.WARNING, "Failed to read image (ImageIO.read returned null).");
                    logger.log(Level.WARNING, "data: " + StringUtils.byteArrayToHexString((byte[]) buffer.getData(), buffer.getLength(), buffer.getOffset()));
                    return 1;
                }
                try {
                    this.component.setImage(image);
                    return 0;
                } catch (Exception e) {
                    logger.log(Level.WARNING, "" + e, e);
                    logger.log(Level.WARNING, "data: " + StringUtils.byteArrayToHexString((byte[]) buffer.getData(), buffer.getLength(), buffer.getOffset()));
                    return 1;
                }
            } catch (IOException e2) {
                logger.log(Level.WARNING, "" + e2, e2);
                logger.log(Level.WARNING, "data: " + StringUtils.byteArrayToHexString((byte[]) buffer.getData(), buffer.getLength(), buffer.getOffset()));
                return 1;
            }
        }
    }

    public Component getComponent() {
        return this.component;
    }

    public Object[] getControls() {
        return this.controls;
    }

    public String getName() {
        return "JPEG Renderer";
    }

    public Format[] getSupportedInputFormats() {
        return this.supportedInputFormats;
    }

    public Format setInputFormat(Format format) {
        VideoFormat chosenFormat = (VideoFormat) super.setInputFormat(format);
        if (chosenFormat != null) {
            getComponent().setPreferredSize(chosenFormat.getSize());
        }
        return chosenFormat;
    }
}
