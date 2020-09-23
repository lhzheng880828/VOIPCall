package net.sf.fmj.media.renderer.video;

import com.lti.utils.UnsignedUtils;
import java.util.logging.Logger;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.renderer.VideoRenderer;
import net.sf.fmj.media.AbstractVideoRenderer;
import net.sf.fmj.media.util.BufferToImage;
import net.sf.fmj.utility.FPSCounter;
import net.sf.fmj.utility.LoggerSingleton;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.Graphics;
import org.jitsi.android.util.java.awt.Image;
import org.jitsi.android.util.java.awt.Rectangle;
import org.jitsi.android.util.java.awt.image.BufferedImage;
import org.jitsi.android.util.java.awt.image.ImagingOpException;
import org.jitsi.android.util.javax.swing.JComponent;

public class SimpleSwingRenderer extends AbstractVideoRenderer implements VideoRenderer {
    private static final boolean PAINT_IMMEDIATELY = false;
    private static final boolean TRACE_FPS = false;
    private static final Logger logger = LoggerSingleton.logger;
    private BufferToImage bufferToImage;
    private SwingVideoComponent component = new SwingVideoComponent();
    private Object[] controls = new Object[]{this};
    private final FPSCounter fpsCounter = new FPSCounter();
    private final Format[] supportedInputFormats = new Format[]{new RGBFormat(null, -1, Format.intArray, -1.0f, 32, 16711680, 65280, UnsignedUtils.MAX_UBYTE, 1, -1, 0, -1), new RGBFormat(null, -1, Format.intArray, -1.0f, 32, UnsignedUtils.MAX_UBYTE, 65280, 16711680, 1, -1, 0, -1), new RGBFormat(null, -1, Format.byteArray, -1.0f, 32, 1, 2, 3), new RGBFormat(null, -1, Format.byteArray, -1.0f, 32, 3, 2, 1), new RGBFormat(null, -1, Format.byteArray, -1.0f, 24, 1, 2, 3), new RGBFormat(null, -1, Format.byteArray, -1.0f, 24, 3, 2, 1), new RGBFormat(null, -1, Format.shortArray, -1.0f, 16, -1, -1, -1, 1, -1, 0, -1), new RGBFormat(null, -1, Format.byteArray, -1.0f, 8, -1, -1, -1, 1, -1, 0, -1)};

    private class SwingVideoComponent extends JComponent {
        private BufferedImage biCompatible;
        private Image image;
        private boolean scale = true;

        public SwingVideoComponent() {
            setDoubleBuffered(false);
        }

        private BufferedImage getCompatibleBufferedImage() {
            if (!(this.biCompatible != null && this.biCompatible.getWidth() == this.image.getWidth(null) && this.biCompatible.getHeight() == this.image.getHeight(null))) {
                this.biCompatible = getGraphicsConfiguration().createCompatibleImage(this.image.getWidth(null), this.image.getHeight(null));
            }
            return this.biCompatible;
        }

        public Dimension getPreferredSize() {
            if (SimpleSwingRenderer.this.inputFormat == null) {
                return SimpleSwingRenderer.super.getPreferredSize();
            }
            return ((VideoFormat) SimpleSwingRenderer.this.inputFormat).getSize();
        }

        private Rectangle getVideoRect(boolean scale) {
            int x;
            int w;
            int y;
            int h;
            Dimension preferredSize = getPreferredSize();
            Dimension size = getSize();
            if (!scale) {
                if (preferredSize.width <= size.width) {
                    x = (size.width - preferredSize.width) / 2;
                    w = preferredSize.width;
                } else {
                    x = 0;
                    w = preferredSize.width;
                }
                if (preferredSize.height <= size.height) {
                    y = (size.height - preferredSize.height) / 2;
                    h = preferredSize.height;
                } else {
                    y = 0;
                    h = preferredSize.height;
                }
            } else if (((float) size.width) / ((float) preferredSize.width) < ((float) size.height) / ((float) preferredSize.height)) {
                w = size.width;
                h = (size.width * preferredSize.height) / preferredSize.width;
                x = 0;
                y = (size.height - h) / 2;
            } else {
                w = (size.height * preferredSize.width) / preferredSize.height;
                h = size.height;
                x = (size.width - w) / 2;
                y = 0;
            }
            return new Rectangle(x, y, w, h);
        }

        public void paint(Graphics g) {
            if (this.image != null) {
                Rectangle rect = getVideoRect(this.scale);
                try {
                    if (this.biCompatible == null) {
                        g.drawImage(this.image, rect.x, rect.y, rect.width, rect.height, null);
                        return;
                    }
                } catch (ImagingOpException e) {
                }
                getCompatibleBufferedImage();
                this.biCompatible.getGraphics().drawImage(this.image, 0, 0, this.image.getWidth(null), this.image.getHeight(null), null);
                g.drawImage(this.biCompatible, rect.x, rect.y, rect.width, rect.height, null);
            }
        }

        public void setImage(Image image) {
            this.image = image;
            repaint();
        }

        public void update(Graphics g) {
            paint(g);
        }
    }

    public int doProcess(Buffer buffer) {
        if (buffer.isEOM()) {
            logger.warning(getClass().getSimpleName() + "passed buffer with EOM flag");
            return 0;
        } else if (buffer.getData() == null) {
            return 1;
        } else {
            this.component.setImage(this.bufferToImage.createImage(buffer));
            return 0;
        }
    }

    public Component getComponent() {
        return this.component;
    }

    public Object[] getControls() {
        return this.controls;
    }

    public String getName() {
        return "Simple Swing Renderer";
    }

    public Format[] getSupportedInputFormats() {
        return this.supportedInputFormats;
    }

    public Format setInputFormat(Format format) {
        this.bufferToImage = new BufferToImage((VideoFormat) format);
        return super.setInputFormat(format);
    }
}
