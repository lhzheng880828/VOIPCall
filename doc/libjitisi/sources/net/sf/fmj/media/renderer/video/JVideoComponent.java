package net.sf.fmj.media.renderer.video;

import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.Graphics;
import org.jitsi.android.util.java.awt.Graphics2D;
import org.jitsi.android.util.java.awt.Image;
import org.jitsi.android.util.javax.swing.JComponent;

public final class JVideoComponent extends JComponent {
    private Image image;
    private boolean scaleKeepAspectRatio = false;

    public JVideoComponent() {
        setDoubleBuffered(false);
        setOpaque(false);
    }

    public Image getImage() {
        return this.image;
    }

    /* access modifiers changed from: protected */
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Dimension size;
        if (this.scaleKeepAspectRatio) {
            if (this.image != null) {
                int w;
                int h;
                int x;
                int y;
                Dimension preferredSize = getPreferredSize();
                size = getSize();
                if (((float) size.width) / ((float) preferredSize.width) < ((float) size.height) / ((float) preferredSize.height)) {
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
                g.drawImage(this.image, x, y, w, h, null);
            }
        } else if (this.image != null) {
            size = getSize();
            g.drawImage(this.image, 0, 0, size.width, size.height, null);
        }
    }

    public void setImage(Image image) {
        this.image = image;
        repaint();
    }
}
