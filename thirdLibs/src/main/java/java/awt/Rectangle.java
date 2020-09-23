package java.awt;

public class Rectangle {
    public int height;
    public int width;
    public int x;
    public int y;

    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rectangle(Dimension var1) {
        this(0, 0, var1.width, var1.height);
    }

    public void setSize(Dimension d) {
        setSize(d.width, d.height);
    }

    public Dimension getSize() {
        return new Dimension(this.width, this.height);
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setBounds(Rectangle r) {
        setBounds(r.x, r.y, r.width, r.height);
    }
}
