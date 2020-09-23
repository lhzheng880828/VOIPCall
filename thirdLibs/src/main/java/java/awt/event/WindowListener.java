package java.awt.event;

public interface WindowListener {
    void windowActivated(WindowEvent windowEvent);

    void windowClosed(WindowEvent windowEvent);

    void windowClosing(WindowEvent windowEvent);

    void windowDeactivated(WindowEvent windowEvent);

    void windowDeiconified(WindowEvent windowEvent);

    void windowIconified(WindowEvent windowEvent);

    void windowOpened(WindowEvent windowEvent);
}
