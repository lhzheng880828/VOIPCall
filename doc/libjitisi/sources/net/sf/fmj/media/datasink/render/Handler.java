package net.sf.fmj.media.datasink.render;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.CannotRealizeException;
import javax.media.IncompatibleSourceException;
import javax.media.Manager;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.protocol.DataSource;
import net.sf.fmj.ejmf.toolkit.util.PlayerPanel;
import net.sf.fmj.media.AbstractDataSink;
import net.sf.fmj.utility.LoggerSingleton;
import org.jitsi.android.util.java.awt.event.ContainerEvent;
import org.jitsi.android.util.java.awt.event.ContainerListener;
import org.jitsi.android.util.java.awt.event.WindowAdapter;
import org.jitsi.android.util.java.awt.event.WindowEvent;
import org.jitsi.android.util.javax.swing.JFrame;

public class Handler extends AbstractDataSink {
    private static final Logger logger = LoggerSingleton.logger;
    private Player player;
    private DataSource source;

    public void close() {
        try {
            stop();
        } catch (IOException e) {
            logger.log(Level.WARNING, "" + e, e);
        }
    }

    public String getContentType() {
        if (this.source != null) {
            return this.source.getContentType();
        }
        return null;
    }

    public Object getControl(String controlType) {
        logger.warning("TODO: getControl " + controlType);
        return null;
    }

    public Object[] getControls() {
        logger.warning("TODO: getControls");
        return new Object[0];
    }

    public void open() throws IOException, SecurityException {
        try {
            this.player = Manager.createRealizedPlayer(this.source);
        } catch (NoPlayerException e) {
            logger.log(Level.WARNING, "" + e, e);
            throw new IOException("" + e);
        } catch (CannotRealizeException e2) {
            logger.log(Level.WARNING, "" + e2, e2);
            throw new IOException("" + e2);
        }
    }

    public void setSource(DataSource source) throws IOException, IncompatibleSourceException {
        this.source = source;
    }

    public void start() throws IOException {
        if (this.player.getVisualComponent() != null) {
            try {
                PlayerPanel playerpanel = new PlayerPanel(this.player);
                playerpanel.addVisualComponent();
                final JFrame frame = new JFrame("Renderer");
                frame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                    }
                });
                playerpanel.getMediaPanel().addContainerListener(new ContainerListener() {
                    public void componentAdded(ContainerEvent e) {
                        frame.pack();
                    }

                    public void componentRemoved(ContainerEvent e) {
                        frame.pack();
                    }
                });
                frame.getContentPane().add(playerpanel);
                frame.pack();
                frame.setVisible(true);
            } catch (NoPlayerException e) {
                logger.log(Level.WARNING, "" + e, e);
                throw new IOException("" + e);
            }
        }
        this.player.start();
    }

    public void stop() throws IOException {
        if (this.player != null) {
            this.player.stop();
        }
    }
}
