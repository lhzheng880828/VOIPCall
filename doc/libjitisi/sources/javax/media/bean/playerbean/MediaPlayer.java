package javax.media.bean.playerbean;

import java.applet.AppletContext;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URL;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.CachingControl;
import javax.media.ClockStoppedException;
import javax.media.Control;
import javax.media.Controller;
import javax.media.ControllerListener;
import javax.media.GainControl;
import javax.media.IncompatibleSourceException;
import javax.media.IncompatibleTimeBaseException;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.Time;
import javax.media.TimeBase;
import javax.media.protocol.DataSource;
import net.sf.fmj.utility.LoggerSingleton;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.android.util.java.awt.Container;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.Panel;
import org.jitsi.android.util.java.awt.PopupMenu;

public class MediaPlayer extends Container implements Player, Externalizable {
    private static final Logger logger = LoggerSingleton.logger;
    transient Component cachingComponent;
    private boolean cachingVisible;
    private PropertyChangeSupport changes;
    private long contentLength;
    transient Component controlComponent;
    private Vector controlListeners;
    private transient int controlPanelHeight;
    protected transient String curVolumeLevel;
    protected transient float curVolumeValue;
    protected transient String curZoomLevel;
    protected transient float curZoomValue;
    private boolean displayURL;
    private boolean fixedAspectRatio;
    protected transient GainControl gainControl;
    private boolean isPopupActive;
    private boolean looping;
    protected transient Time mediaTime;
    private AppletContext mpAppletContext;
    private URL mpCodeBase;
    private MediaLocator mrl;
    transient Panel newPanel;
    transient Panel panel;
    private boolean panelVisible;
    transient Player player;
    private int preferredHeight;
    private int preferredWidth;
    private int state;
    private URL url;
    private transient int urlFieldHeight;
    private String urlString;
    transient Panel vPanel;
    transient Component visualComponent;
    private PopupMenu zoomMenu;

    public void addController(Controller newController) throws IncompatibleTimeBaseException {
        if (this.player != null) {
            this.player.addController(newController);
        }
    }

    public void addControllerListener(ControllerListener listener) {
        if (this.player != null) {
            this.player.addControllerListener(listener);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener c) {
        throw new UnsupportedOperationException();
    }

    public void close() {
        if (this.player != null) {
            this.player.close();
        }
    }

    public void deallocate() {
        if (this.player != null) {
            this.player.deallocate();
        }
    }

    public Control getControl(String forName) {
        if (this.player == null) {
            return null;
        }
        return this.player.getControl(forName);
    }

    public Component getControlPanelComponent() {
        if (this.player == null) {
            return null;
        }
        return this.player.getControlPanelComponent();
    }

    public int getControlPanelHeight() {
        return this.controlPanelHeight;
    }

    public Control[] getControls() {
        if (this.player == null) {
            return new Control[0];
        }
        return this.player.getControls();
    }

    public Time getDuration() {
        if (this.player == null) {
            return DURATION_UNKNOWN;
        }
        return this.player.getDuration();
    }

    public GainControl getGainControl() {
        if (this.player == null) {
            return null;
        }
        return this.player.getGainControl();
    }

    public String getMediaLocation() {
        if (this.player == null) {
            return " ";
        }
        throw new UnsupportedOperationException();
    }

    public int getMediaLocationHeight() {
        return this.urlFieldHeight;
    }

    /* access modifiers changed from: protected */
    public MediaLocator getMediaLocator(String filename) {
        return new MediaLocator("file://" + filename);
    }

    public long getMediaNanoseconds() {
        if (this.player == null) {
            return CachingControl.LENGTH_UNKNOWN;
        }
        return this.player.getMediaNanoseconds();
    }

    public Time getMediaTime() {
        if (this.player == null) {
            return new Time((long) CachingControl.LENGTH_UNKNOWN);
        }
        return this.player.getMediaTime();
    }

    public boolean getPlaybackLoop() {
        return this.looping;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Dimension getPreferredSize() {
        throw new UnsupportedOperationException();
    }

    public float getRate() {
        if (this.player == null) {
            return 0.0f;
        }
        return this.player.getRate();
    }

    public Time getStartLatency() {
        if (this.player == null) {
            return new Time((long) CachingControl.LENGTH_UNKNOWN);
        }
        return this.player.getStartLatency();
    }

    public int getState() {
        if (this.player == null) {
            return 100;
        }
        return this.player.getState();
    }

    public Time getStopTime() {
        if (this.player == null) {
            return null;
        }
        return this.player.getStopTime();
    }

    public Time getSyncTime() {
        if (this.player == null) {
            return new Time((long) CachingControl.LENGTH_UNKNOWN);
        }
        return this.player.getSyncTime();
    }

    public int getTargetState() {
        if (this.player == null) {
            return 100;
        }
        return this.player.getTargetState();
    }

    public TimeBase getTimeBase() {
        if (this.player == null) {
            return null;
        }
        return this.player.getTimeBase();
    }

    public Component getVisualComponent() {
        if (this.player == null) {
            return null;
        }
        return this.player.getVisualComponent();
    }

    public String getVolumeLevel() {
        return this.curVolumeLevel;
    }

    public String getZoomTo() {
        return this.curZoomLevel;
    }

    public boolean isCachingControlVisible() {
        return this.cachingVisible;
    }

    public boolean isControlPanelVisible() {
        return this.panelVisible;
    }

    public boolean isFixedAspectRatio() {
        throw new UnsupportedOperationException();
    }

    public boolean isMediaLocationVisible() {
        return this.displayURL;
    }

    public boolean isPlayBackLoop() {
        return this.looping;
    }

    public Time mapToTimeBase(Time t) throws ClockStoppedException {
        if (this.player == null) {
            return new Time((long) CachingControl.LENGTH_UNKNOWN);
        }
        return this.player.mapToTimeBase(t);
    }

    public void prefetch() {
        if (this.player != null) {
            this.player.prefetch();
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        throw new UnsupportedOperationException();
    }

    public void realize() {
        if (this.player != null) {
            this.player.realize();
        }
    }

    public void removeController(Controller oldController) {
        if (this.player != null) {
            this.player.removeController(oldController);
        }
    }

    public void removeControllerListener(ControllerListener listener) {
        if (this.player != null) {
            this.player.removeControllerListener(listener);
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener c) {
        throw new UnsupportedOperationException();
    }

    public void restoreMediaTime() {
        throw new UnsupportedOperationException();
    }

    public void saveMediaTime() {
        throw new UnsupportedOperationException();
    }

    public void setBounds(int x, int y, int w, int h) {
        throw new UnsupportedOperationException();
    }

    public void setCachingControlVisible(boolean isVisible) {
        this.cachingVisible = isVisible;
    }

    public void setCodeBase(URL cb) {
        this.mpCodeBase = cb;
    }

    public void setControlPanelVisible(boolean isVisible) {
        this.panelVisible = isVisible;
    }

    public void setDataSource(DataSource ds) {
        try {
            this.player = Manager.createPlayer(ds);
        } catch (NoPlayerException e) {
            logger.log(Level.WARNING, "" + e, e);
        } catch (IOException e2) {
            logger.log(Level.WARNING, "" + e2, e2);
        }
    }

    public void setFixedAspectRatio(boolean isFixed) {
        throw new UnsupportedOperationException();
    }

    public void setMediaLocation(String location) {
        throw new UnsupportedOperationException();
    }

    public void setMediaLocationVisible(boolean val) {
        this.displayURL = val;
    }

    public void setMediaLocator(MediaLocator locator) {
        try {
            this.player = Manager.createPlayer(locator);
        } catch (NoPlayerException e) {
            logger.log(Level.WARNING, "" + e, e);
        } catch (IOException e2) {
            logger.log(Level.WARNING, "" + e2, e2);
        }
    }

    public void setMediaTime(Time now) {
        if (this.player != null) {
            this.player.setMediaTime(now);
        }
    }

    public void setPlaybackLoop(boolean val) {
        this.looping = val;
    }

    public void setPlayer(Player newPlayer) {
        this.player = newPlayer;
    }

    public void setPopupActive(boolean isActive) {
        this.isPopupActive = isActive;
    }

    public float setRate(float factor) {
        if (this.player == null) {
            return 0.0f;
        }
        return this.player.setRate(factor);
    }

    public void setSource(DataSource source) throws IOException, IncompatibleSourceException {
        if (this.player != null) {
            this.player.setSource(source);
        }
    }

    public void setStopTime(Time stopTime) {
        if (this.player != null) {
            this.player.setStopTime(stopTime);
        }
    }

    public void setTimeBase(TimeBase master) throws IncompatibleTimeBaseException {
        if (this.player != null) {
            this.player.setTimeBase(master);
        }
    }

    public void setVolumeLevel(String volumeString) {
        this.curVolumeLevel = volumeString;
    }

    public void setZoomTo(String scale) {
        this.curZoomLevel = scale;
    }

    public void start() {
        if (this.player != null) {
            this.player.start();
        }
    }

    public void stop() {
        if (this.player != null) {
            this.player.stop();
        }
    }

    public void stopAndDeallocate() {
        stop();
        deallocate();
    }

    public void syncStart(Time at) {
        if (this.player != null) {
            this.player.syncStart(at);
        }
    }

    public synchronized void waitForState(int s) {
        throw new UnsupportedOperationException();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        throw new UnsupportedOperationException();
    }
}
