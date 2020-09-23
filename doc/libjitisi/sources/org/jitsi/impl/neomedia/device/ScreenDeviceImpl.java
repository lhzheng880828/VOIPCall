package org.jitsi.impl.neomedia.device;

import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.DisplayMode;
import org.jitsi.android.util.java.awt.GraphicsDevice;
import org.jitsi.android.util.java.awt.GraphicsEnvironment;
import org.jitsi.android.util.java.awt.Point;
import org.jitsi.android.util.java.awt.Rectangle;
import org.jitsi.service.neomedia.device.ScreenDevice;

public class ScreenDeviceImpl implements ScreenDevice {
    private static final ScreenDevice[] EMPTY_SCREEN_DEVICE_ARRAY = new ScreenDevice[0];
    private final int index;
    private final GraphicsDevice screen;

    public static ScreenDevice[] getAvailableScreenDevices() {
        GraphicsEnvironment ge;
        try {
            ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                ThreadDeath t2 = (ThreadDeath) t;
            } else {
                ge = null;
            }
        }
        ScreenDevice[] screens = null;
        if (!(ge == null || ge.isHeadlessInstance())) {
            GraphicsDevice[] devices = ge.getScreenDevices();
            if (!(devices == null || devices.length == 0)) {
                screens = new ScreenDevice[devices.length];
                int i = 0;
                for (GraphicsDevice dev : devices) {
                    screens[i] = new ScreenDeviceImpl(i, dev);
                    i++;
                }
            }
        }
        return screens == null ? EMPTY_SCREEN_DEVICE_ARRAY : screens;
    }

    public static ScreenDevice getDefaultScreenDevice() {
        int width = 0;
        int height = 0;
        ScreenDevice best = null;
        for (ScreenDevice screen : getAvailableScreenDevices()) {
            Dimension size = screen.getSize();
            if (size != null && (width < size.width || height < size.height)) {
                width = size.width;
                height = size.height;
                best = screen;
            }
        }
        return best;
    }

    protected ScreenDeviceImpl(int index, GraphicsDevice screen) {
        this.index = index;
        this.screen = screen;
    }

    public boolean containsPoint(Point p) {
        return this.screen.getDefaultConfiguration().getBounds().contains(p);
    }

    public Rectangle getBounds() {
        return this.screen.getDefaultConfiguration().getBounds();
    }

    public int getIndex() {
        return this.index;
    }

    public String getName() {
        return this.screen.getIDstring();
    }

    public Dimension getSize() {
        DisplayMode displayMode = this.screen.getDisplayMode();
        return displayMode == null ? null : new Dimension(displayMode.getWidth(), displayMode.getHeight());
    }
}
