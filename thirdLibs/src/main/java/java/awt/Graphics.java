package java.awt;

import java.awt.image.ImageObserver;

public abstract class Graphics {
    public abstract boolean drawImage(Image image, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, ImageObserver imageObserver);

    public abstract boolean drawImage(Image image, int i, int i2, int i3, int i4, ImageObserver imageObserver);

    public abstract boolean drawImage(Image image, int i, int i2, Color color, ImageObserver imageObserver);

    public abstract boolean drawImage(Image image, int i, int i2, ImageObserver imageObserver);

    public abstract void fillRect(int i, int i2, int i3, int i4);

    public abstract void setColor(Color color);

    protected Graphics() {
    }
}
