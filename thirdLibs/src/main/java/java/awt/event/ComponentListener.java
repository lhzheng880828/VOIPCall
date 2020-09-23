package java.awt.event;

public interface ComponentListener {
    void componentHidden(ComponentEvent componentEvent);

    void componentMoved(ComponentEvent componentEvent);

    void componentResized(ComponentEvent componentEvent);

    void componentShown(ComponentEvent componentEvent);
}
