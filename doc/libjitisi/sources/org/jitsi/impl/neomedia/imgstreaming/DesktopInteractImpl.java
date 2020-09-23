package org.jitsi.impl.neomedia.imgstreaming;

import org.jitsi.android.util.java.awt.AWTException;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.Rectangle;
import org.jitsi.android.util.java.awt.Robot;
import org.jitsi.android.util.java.awt.Toolkit;
import org.jitsi.android.util.java.awt.image.BufferedImage;
import org.jitsi.util.Logger;
import org.jitsi.util.OSUtils;

public class DesktopInteractImpl implements DesktopInteract {
    private static final Logger logger = Logger.getLogger(DesktopInteractImpl.class);
    private Robot robot;

    public DesktopInteractImpl() throws AWTException, SecurityException {
        this.robot = null;
        this.robot = new Robot();
    }

    public boolean captureScreen(int display, byte[] output) {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        return captureScreen(display, 0, 0, dim.width, dim.height, output);
    }

    public boolean captureScreen(int display, long buffer, int bufferLength) {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        return captureScreen(display, 0, 0, dim.width, dim.height, buffer, bufferLength);
    }

    public boolean captureScreen(int display, int x, int y, int width, int height, byte[] output) {
        if (OSUtils.IS_LINUX || OSUtils.IS_MAC || OSUtils.IS_WINDOWS) {
            return ScreenCapture.grabScreen(display, x, y, width, height, output);
        }
        return false;
    }

    public boolean captureScreen(int display, int x, int y, int width, int height, long buffer, int bufferLength) {
        if (OSUtils.IS_LINUX || OSUtils.IS_MAC || OSUtils.IS_WINDOWS) {
            return ScreenCapture.grabScreen(display, x, y, width, height, buffer, bufferLength);
        }
        return false;
    }

    public BufferedImage captureScreen() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        return captureScreen(0, 0, dim.width, dim.height);
    }

    public BufferedImage captureScreen(int x, int y, int width, int height) {
        BufferedImage img;
        if (this.robot == null) {
            img = null;
            return null;
        }
        if (logger.isInfoEnabled()) {
            logger.info("Begin capture: " + System.nanoTime());
        }
        BufferedImage img2 = this.robot.createScreenCapture(new Rectangle(x, y, width, height));
        if (logger.isInfoEnabled()) {
            logger.info("End capture: " + System.nanoTime());
        }
        img = img2;
        return img2;
    }
}
