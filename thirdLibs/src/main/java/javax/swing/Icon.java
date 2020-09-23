package javax.swing;

import java.awt.Component;
import java.awt.Graphics;

public interface Icon {
    int getIconHeight();

    int getIconWidth();

    void paintIcon(Component component, Graphics graphics, int i, int i2);
}
