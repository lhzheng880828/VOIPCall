package javax.swing;

import java.net.URL;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;

public class ImageIcon implements Icon {
    int height;
    int width;

    public ImageIcon(URL location) {
    }

    public Image getImage() {
        return null;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
    }

    public int getIconWidth() {
        return this.width;
    }

    public int getIconHeight() {
        return this.height;
    }
}
