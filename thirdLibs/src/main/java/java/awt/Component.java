package java.awt;

import java.awt.event.ComponentListener;
import java.awt.event.HierarchyListener;
import java.awt.image.ImageObserver;

public class Component implements ImageObserver{

    public static final float TOP_ALIGNMENT = 0.0F;
    public static final float CENTER_ALIGNMENT = 0.5F;
    public static final float BOTTOM_ALIGNMENT = 1.0F;
    public static final float LEFT_ALIGNMENT = 0.0F;
    public static final float RIGHT_ALIGNMENT = 1.0F;
    private int height;
    private int prefHeight;
    private boolean prefSizeIsSet;
    private int prefWidth;
    private int width;

    int x;
    int y;

    public void setBounds(Rectangle var1) {
        this.setBounds(var1.x, var1.y, var1.width, var1.height);
    }

    public void setBounds(int var1, int var2, int var3, int var4) {
       // this.reshape(var1, var2, var3, var4);
    }

    public void addComponentListener(ComponentListener componentListener) {
    }

    public void addHierarchyListener(HierarchyListener hierarchyListener) {
    }

    public Color getBackground() {
        return null;
    }

    public int getHeight() {
        return this.height;
    }

    public String getName() {
        return null;
    }

    public Container getParent() {
        return null;
    }

    public Dimension getPreferredSize() {
        return new Dimension(this.prefWidth, this.prefHeight);
    }

    public Dimension getSize() {
        return new Dimension(this.width, this.height);
    }

    public int getWidth() {
        return this.width;
    }

    public boolean isDisplayable() {
        return true;
    }

    public boolean isEnabled() {
        return false;
    }

    public boolean isPreferredSizeSet() {
        return this.prefSizeIsSet;
    }

    public boolean isVisible() {
        return true;
    }

    public void paint(Graphics graphics) {
    }

    public void removeHierarchyListener(HierarchyListener hierarchyListener) {
    }

    public void repaint() {
        update(null);
    }

    public void setBackground(Color color) {
    }

    public void setEnabled(boolean z) {
    }

    public void setLocation(int i, int i2) {
    }

    public void setMaximumSize(Dimension dimension) {
    }

    public void setName(String str) {
    }

    public boolean isOpaque() {
       /* if (this.getPeer() == null) {
            return false;
        } else {
            return !this.isLightweight();
        }*/
       return false;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public void setPreferredSize(Dimension dimension) {
        if (dimension == null) {
            this.prefWidth = 0;
            this.prefHeight = 0;
            this.prefSizeIsSet = false;
            return;
        }
        this.prefWidth = dimension.width;
        this.prefHeight = dimension.height;
        this.prefSizeIsSet = true;
    }

    public void setSize(Dimension dimension) {
        if (dimension == null) {
            setSize(0, 0);
        } else {
            setSize(dimension.width, dimension.height);
        }
    }

    public void setSize(int i, int i2) {
        this.width = i;
        this.height = i2;
    }

    public void setVisible(boolean z) {
    }

    public void update(Graphics graphics) {
        paint(graphics);
    }

    public void removeNotify() {

    }

    public void addNotify() {
    }

    Dimension maxSize;
    boolean maxSizeSet;

    public boolean isMaximumSizeSet() {
        return this.maxSizeSet;
    }

    public Dimension getMaximumSize() {
        return this.isMaximumSizeSet() ? new Dimension(this.maxSize) : new Dimension(32767, 32767);
    }

    public Dimension getMinimumSize() {
        //return this.minimumSize();
        return new Dimension(0,0);
    }
}
