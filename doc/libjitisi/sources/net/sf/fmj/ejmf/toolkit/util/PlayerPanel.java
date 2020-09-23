package net.sf.fmj.ejmf.toolkit.util;

import java.io.IOException;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoPlayerException;
import javax.media.Player;
import org.jitsi.android.util.java.awt.BorderLayout;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.android.util.javax.swing.JPanel;

public class PlayerPanel extends JPanel {
    private static final String LOADLABEL = "Loading Media...";
    private static final Object _CPLOC = "South";
    private static final Object _VISLOC = "Center";
    private JPanel mediaPanel = new JPanel();
    private Player player;

    public PlayerPanel(MediaLocator locator) throws IOException, NoPlayerException {
        this.player = Manager.createPlayer(locator);
        setLayout(new BorderLayout());
        this.mediaPanel.setLayout(new BorderLayout());
    }

    public PlayerPanel(Player p) throws IOException, NoPlayerException {
        this.player = p;
        setLayout(new BorderLayout());
        this.mediaPanel.setLayout(new BorderLayout());
    }

    private Component addComponent(Component c, Object constraints) {
        if (c != null) {
            this.mediaPanel.add(c, constraints);
        }
        return c;
    }

    public Component addControlComponent() {
        return addControlComponent(this.player.getControlPanelComponent());
    }

    public Component addControlComponent(Component cc) {
        addMediaPanel();
        return addComponent(cc, _CPLOC);
    }

    public void addMediaPanel() {
        if (!isAncestorOf(this.mediaPanel)) {
            add(this.mediaPanel, "Center");
        }
    }

    public Component addVisualComponent() {
        return addVisualComponent(this.player.getVisualComponent());
    }

    public Component addVisualComponent(Component cc) {
        addMediaPanel();
        return addComponent(cc, _VISLOC);
    }

    public JPanel getMediaPanel() {
        return this.mediaPanel;
    }

    public Player getPlayer() {
        return this.player;
    }
}
