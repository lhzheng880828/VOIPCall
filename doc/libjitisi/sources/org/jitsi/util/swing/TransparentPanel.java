package org.jitsi.util.swing;

import org.jitsi.android.util.java.awt.LayoutManager;
import org.jitsi.android.util.javax.swing.JPanel;

public class TransparentPanel extends JPanel {
    private static final long serialVersionUID = 0;

    public TransparentPanel() {
        setOpaque(false);
    }

    public TransparentPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }
}
