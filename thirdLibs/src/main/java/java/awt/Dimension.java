package java.awt;

public class Dimension {
    private static final long serialVersionUID = 0;
    public int height;
    public int width;

    public Dimension(Dimension d) {
        this(d.width, d.height);
    }

    public Dimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Dimension)) {
            return false;
        }
        Dimension dim = (Dimension) obj;
        if (this.width == dim.width && this.height == dim.height) {
            return true;
        }
        return false;
    }

    public double getHeight() {
        return (double) this.height;
    }

    public double getWidth() {
        return (double) this.width;
    }

    public int hashCode() {
        return (this.width << 16) | (this.height >> 16);
    }
}
