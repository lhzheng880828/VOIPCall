package java.awt.event;

public class ActionEvent extends AWTEvent {
    public ActionEvent(Object source, int id, String command) {
        super(source, id);
    }
}
