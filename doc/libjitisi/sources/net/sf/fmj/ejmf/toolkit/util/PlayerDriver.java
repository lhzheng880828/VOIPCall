package net.sf.fmj.ejmf.toolkit.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.MediaLocator;
import javax.media.NoPlayerException;
import net.sf.fmj.utility.FmjStartup;
import net.sf.fmj.utility.LoggerSingleton;
import org.jitsi.android.util.java.awt.event.ContainerEvent;
import org.jitsi.android.util.java.awt.event.ContainerListener;
import org.jitsi.android.util.java.awt.event.WindowAdapter;
import org.jitsi.android.util.java.awt.event.WindowEvent;
import org.jitsi.android.util.javax.swing.JApplet;
import org.jitsi.android.util.javax.swing.JFrame;

public abstract class PlayerDriver extends JApplet {
    private static final Logger logger = LoggerSingleton.logger;
    /* access modifiers changed from: private */
    public JFrame frame;
    private PlayerPanel playerpanel;

    public abstract void begin();

    public static void main(PlayerDriver driver, String[] args) {
        if (args.length == 0) {
            logger.severe("Media parameter not specified");
            return;
        }
        try {
            driver.initialize(Utility.appArgToMediaLocator(args[0]));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not connect to media: " + e, e);
            System.exit(1);
        } catch (NoPlayerException e2) {
            logger.log(Level.WARNING, "Player not found for media: " + e2, e2);
            System.exit(1);
        }
    }

    public PlayerDriver() {
        getRootPane().putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);
    }

    public void destroy() {
        PlayerDriver.super.destroy();
        if (getPlayerPanel() != null && getPlayerPanel().getPlayer() != null) {
            getPlayerPanel().getPlayer().stop();
            getPlayerPanel().getPlayer().close();
        }
    }

    public JFrame getFrame() {
        return this.frame;
    }

    public PlayerPanel getPlayerPanel() {
        return this.playerpanel;
    }

    public void init() {
        FmjStartup.initApplet();
        String media = getParameter("MEDIA");
        if (media == null) {
            logger.warning("Error: MEDIA parameter not specified");
            return;
        }
        try {
            this.playerpanel = new PlayerPanel(Utility.appletArgToMediaLocator(this, media));
            this.playerpanel.getMediaPanel().addContainerListener(new ContainerListener() {
                public void componentAdded(ContainerEvent e) {
                    PlayerDriver.this.pack();
                }

                public void componentRemoved(ContainerEvent e) {
                    PlayerDriver.this.pack();
                }
            });
            getContentPane().add(this.playerpanel);
            pack();
            begin();
        } catch (IOException e) {
            logger.warning("Could not connect to media");
            destroy();
        } catch (NoPlayerException e2) {
            logger.warning("Player not found for media");
            destroy();
        }
    }

    public void initialize(MediaLocator locator) throws IOException, NoPlayerException {
        this.playerpanel = new PlayerPanel(locator);
        this.frame = new JFrame(locator.toString());
        this.frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        this.playerpanel.getMediaPanel().addContainerListener(new ContainerListener() {
            public void componentAdded(ContainerEvent e) {
                PlayerDriver.this.frame.pack();
            }

            public void componentRemoved(ContainerEvent e) {
                PlayerDriver.this.frame.pack();
            }
        });
        this.frame.getContentPane().add(this.playerpanel);
        this.frame.pack();
        this.frame.setVisible(true);
        begin();
    }

    public void pack() {
        setSize(getPreferredSize());
        validate();
    }

    public void redraw() {
        this.frame.pack();
    }
}
