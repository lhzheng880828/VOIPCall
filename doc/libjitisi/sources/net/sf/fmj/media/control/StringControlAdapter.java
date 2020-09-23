package net.sf.fmj.media.control;

import javax.media.Control;
import org.jitsi.android.util.java.awt.Component;

public class StringControlAdapter extends AtomicControlAdapter implements StringControl {
    String title;
    String value;

    public StringControlAdapter() {
        super(null, true, null);
    }

    public StringControlAdapter(Component c, boolean def, Control parent) {
        super(c, def, parent);
    }

    public String getTitle() {
        return this.title;
    }

    public String getValue() {
        return this.value;
    }

    public String setTitle(String title) {
        this.title = title;
        informListeners();
        return title;
    }

    public String setValue(String value) {
        this.value = value;
        informListeners();
        return value;
    }
}
