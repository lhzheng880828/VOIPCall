package org.jitsi.impl.neomedia.jmfext.media.renderer.video;

import com.lti.utils.UnsignedUtils;
import java.lang.reflect.InvocationTargetException;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.format.YUVFormat;
import javax.media.renderer.VideoRenderer;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.Graphics;
import org.jitsi.android.util.java.awt.Rectangle;
import org.jitsi.android.util.javax.swing.SwingUtilities;
import org.jitsi.impl.neomedia.jmfext.media.renderer.AbstractRenderer;
import org.jitsi.util.Logger;
import org.jitsi.util.OSUtils;
import org.jitsi.util.swing.VideoLayout;

public class JAWTRenderer extends AbstractRenderer<VideoFormat> implements VideoRenderer {
    private static final int DEFAULT_COMPONENT_HEIGHT_OR_WIDTH = 16;
    private static final String PLUGIN_NAME = "JAWT Renderer";
    private static final Format[] SUPPORTED_INPUT_FORMATS;
    private static final Logger logger = Logger.getLogger(JAWTRenderer.class);
    private Component component;
    private long handle = 0;
    private int height = 0;
    private final Runnable reflectInputFormatOnComponentInEventDispatchThread = new Runnable() {
        public void run() {
            JAWTRenderer.this.reflectInputFormatOnComponentInEventDispatchThread();
        }
    };
    private int width = 0;

    private static native void close(long j, Component component);

    private static native long open(Component component) throws ResourceUnavailableException;

    static native boolean paint(long j, Component component, Graphics graphics, int i);

    static native boolean process(long j, Component component, int[] iArr, int i, int i2, int i3, int i4);

    private static native String sysctlbyname(String str);

    static {
        Format[] formatArr = new Format[1];
        RGBFormat yUVFormat = OSUtils.IS_LINUX ? new YUVFormat(null, -1, Format.intArray, -1.0f, 2, -1, -1, -1, -1, -1) : OSUtils.IS_ANDROID ? new RGBFormat(null, -1, Format.intArray, -1.0f, 32, UnsignedUtils.MAX_UBYTE, 65280, 16711680) : new RGBFormat(null, -1, Format.intArray, -1.0f, 32, 16711680, 65280, UnsignedUtils.MAX_UBYTE);
        formatArr[0] = yUVFormat;
        SUPPORTED_INPUT_FORMATS = formatArr;
        System.loadLibrary("jnawtrenderer");
    }

    public synchronized void close() {
        if (this.handle != 0) {
            close(this.handle, this.component);
            this.handle = 0;
        }
    }

    public Rectangle getBounds() {
        return null;
    }

    public synchronized Component getComponent() {
        if (this.component == null) {
            StringBuilder componentClassName = new StringBuilder();
            componentClassName.append("org.jitsi.impl.neomedia.jmfext.media.renderer.video.JAWTRenderer");
            if (OSUtils.IS_ANDROID) {
                componentClassName.append("Android");
            }
            componentClassName.append("VideoComponent");
            Throwable reflectiveOperationException = null;
            try {
                this.component = (Component) Class.forName(componentClassName.toString()).getConstructor(new Class[]{JAWTRenderer.class}).newInstance(new Object[]{this});
            } catch (ClassNotFoundException cnfe) {
                reflectiveOperationException = cnfe;
            } catch (IllegalAccessException iae) {
                reflectiveOperationException = iae;
            } catch (InstantiationException ie) {
                reflectiveOperationException = ie;
            } catch (InvocationTargetException ite) {
                reflectiveOperationException = ite;
            } catch (NoSuchMethodException nsme) {
                reflectiveOperationException = nsme;
            }
            if (reflectiveOperationException != null) {
                throw new RuntimeException(reflectiveOperationException);
            }
            this.component.setSize(16, 16);
            reflectInputFormatOnComponentInEventDispatchThread();
        }
        return this.component;
    }

    public long getHandle() {
        return this.handle;
    }

    public Object getHandleLock() {
        return this;
    }

    public String getName() {
        return PLUGIN_NAME;
    }

    public Format[] getSupportedInputFormats() {
        return (Format[]) SUPPORTED_INPUT_FORMATS.clone();
    }

    public void open() throws ResourceUnavailableException {
        boolean addNotify;
        Component component;
        synchronized (this) {
            if (this.handle == 0) {
                addNotify = (this.component == null || this.component.getParent() == null) ? false : true;
                component = getComponent();
                this.handle = open(component);
                if (this.handle == 0) {
                    throw new ResourceUnavailableException("Failed to open the native JAWTRenderer.");
                }
            }
            addNotify = false;
            component = null;
        }
        if (addNotify) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    component.addNotify();
                }
            });
        }
    }

    public synchronized int process(Buffer buffer) {
        int i;
        if (buffer.isDiscard()) {
            i = 0;
        } else {
            int bufferLength = buffer.getLength();
            if (bufferLength == 0) {
                i = 0;
            } else {
                Format format = buffer.getFormat();
                if (format != null && format != this.inputFormat && !format.equals(this.inputFormat) && setInputFormat(format) == null) {
                    i = 1;
                } else if (this.handle == 0) {
                    i = 1;
                } else {
                    Dimension size = null;
                    if (format != null) {
                        size = ((VideoFormat) format).getSize();
                    }
                    if (size == null) {
                        size = ((VideoFormat) this.inputFormat).getSize();
                        if (size == null) {
                            i = 1;
                        }
                    }
                    if (size.width >= 4 && size.height >= 4) {
                        Component component = getComponent();
                        if (process(this.handle, component, (int[]) buffer.getData(), buffer.getOffset(), bufferLength, size.width, size.height)) {
                            component.repaint();
                        }
                    }
                    i = 0;
                }
            }
        }
        return i;
    }

    private void reflectInputFormatOnComponent() {
        if (SwingUtilities.isEventDispatchThread()) {
            reflectInputFormatOnComponentInEventDispatchThread();
        } else {
            SwingUtilities.invokeLater(this.reflectInputFormatOnComponentInEventDispatchThread);
        }
    }

    /* access modifiers changed from: private */
    public void reflectInputFormatOnComponentInEventDispatchThread() {
        if (this.component != null && this.width > 0 && this.height > 0) {
            Dimension prefSize = this.component.getPreferredSize();
            if (prefSize == null || prefSize.width < 1 || prefSize.height < 1 || !VideoLayout.areAspectRatiosEqual(prefSize, this.width, this.height) || prefSize.width < this.width || prefSize.height < this.height) {
                this.component.setPreferredSize(new Dimension(this.width, this.height));
            }
            if (this.component.isPreferredSizeSet() && this.component.getParent() == null) {
                Dimension size = this.component.getSize();
                prefSize = this.component.getPreferredSize();
                if (size.width < 1 || size.height < 1 || !VideoLayout.areAspectRatiosEqual(size, prefSize.width, prefSize.height)) {
                    this.component.setSize(prefSize.width, prefSize.height);
                }
            }
        }
    }

    public void setBounds(Rectangle bounds) {
    }

    public boolean setComponent(Component component) {
        return false;
    }

    public synchronized Format setInputFormat(Format format) {
        Format newInputFormat;
        Format oldInputFormat = this.inputFormat;
        newInputFormat = super.setInputFormat(format);
        if (oldInputFormat != this.inputFormat) {
            if (logger.isDebugEnabled()) {
                logger.debug(getClass().getName() + " 0x" + Integer.toHexString(hashCode()) + " set to input in " + this.inputFormat);
            }
            Dimension size = ((VideoFormat) this.inputFormat).getSize();
            if (size == null) {
                this.height = 0;
                this.width = 0;
            } else {
                this.width = size.width;
                this.height = size.height;
            }
            reflectInputFormatOnComponent();
        }
        return newInputFormat;
    }

    public void start() {
    }

    public void stop() {
    }
}
