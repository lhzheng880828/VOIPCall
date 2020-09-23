package java.awt;

import java.awt.image.ImageObserver;

public abstract class Image {
    public abstract int getHeight(ImageObserver imageObserver);

    public abstract int getWidth(ImageObserver imageObserver);
}
