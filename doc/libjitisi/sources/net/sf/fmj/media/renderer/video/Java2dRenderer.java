package net.sf.fmj.media.renderer.video;

import com.lti.utils.UnsignedUtils;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.RGBFormat;
import javax.media.renderer.VideoRenderer;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.Rectangle;
import org.jitsi.android.util.java.awt.image.BufferedImage;

public class Java2dRenderer implements VideoRenderer {
    private Rectangle bounds = new Rectangle(0, 0, 10, 10);
    private BufferedImage bufferedImage;
    private JVideoComponent component;
    private RGBFormat inputFormat;
    private String name = "Java2D Video Renderer";
    private Format[] supportedFormats = new Format[]{new RGBFormat(null, -1, Format.intArray, -1.0f, 32, 16711680, 65280, UnsignedUtils.MAX_UBYTE, 1, -1, 0, -1), new RGBFormat(null, -1, Format.intArray, -1.0f, 32, UnsignedUtils.MAX_UBYTE, 65280, 16711680, 1, -1, 0, -1)};

    public synchronized void close() {
        this.bufferedImage = null;
    }

    private void createImage() {
        if (this.inputFormat != null) {
            Dimension size = this.inputFormat.getSize();
            if (size != null) {
                int imageType;
                if (this.inputFormat.getRedMask() == UnsignedUtils.MAX_UBYTE) {
                    imageType = 4;
                } else {
                    imageType = 1;
                }
                this.bufferedImage = new BufferedImage(size.width, size.height, imageType);
            }
        }
    }

    public Rectangle getBounds() {
        return this.bounds;
    }

    public Component getComponent() {
        if (this.component == null) {
            this.component = new JVideoComponent();
        }
        return this.component;
    }

    public Object getControl(String controlType) {
        return null;
    }

    public Object[] getControls() {
        return new Object[0];
    }

    public String getName() {
        return this.name;
    }

    public Format[] getSupportedInputFormats() {
        return this.supportedFormats;
    }

    public void open() throws ResourceUnavailableException {
        createImage();
    }

    public int process(Buffer buffer) {
        if (this.component == null) {
            return 1;
        }
        Format inFormat = buffer.getFormat();
        if (inFormat == null) {
            return 1;
        }
        if (!(inFormat == this.inputFormat && inFormat.equals(this.inputFormat))) {
            if (setInputFormat(inFormat) == null) {
                return 1;
            }
            createImage();
        }
        Object data = buffer.getData();
        if (data == null || inFormat.getDataType() != Format.intArray) {
            return 1;
        }
        Dimension size = this.inputFormat.getSize();
        synchronized (this.component) {
            this.bufferedImage.getRaster().setDataElements(0, 0, size.width, size.height, data);
            this.component.setImage(this.bufferedImage);
        }
        return 0;
    }

    public void reset() {
    }

    public void setBounds(Rectangle rect) {
        this.bounds.setBounds(rect);
    }

    public boolean setComponent(Component comp) {
        return false;
    }

    public Format setInputFormat(Format format) {
        for (Format matches : this.supportedFormats) {
            if (format.matches(matches)) {
                this.inputFormat = (RGBFormat) format;
                Dimension size = this.inputFormat.getSize();
                if (size != null) {
                    this.bounds.setSize(size);
                }
                getComponent().setPreferredSize(size);
                return format;
            }
        }
        return null;
    }

    public void start() {
    }

    public void stop() {
    }
}
