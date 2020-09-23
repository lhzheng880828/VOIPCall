package net.sf.fmj.ejmf.toolkit.util;

import java.applet.Applet;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.media.Control;
import javax.media.Controller;
import javax.media.GainControl;
import javax.media.MediaLocator;
import javax.media.Player;
import javax.media.Time;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.android.util.javax.swing.GrayFilter;
import org.jitsi.android.util.javax.swing.Icon;
import org.jitsi.android.util.javax.swing.ImageIcon;
import org.jitsi.android.util.javax.swing.JFrame;
import org.jitsi.impl.neomedia.portaudio.Pa;

public class Utility {
    public static MediaLocator appArgToMediaLocator(String arg) {
        try {
            return new MediaLocator(new URL(arg));
        } catch (MalformedURLException e) {
            try {
                return new MediaLocator(fileToURL(arg));
            } catch (IOException | MalformedURLException e2) {
                return new MediaLocator(arg);
            }
        }
    }

    public static MediaLocator appletArgToMediaLocator(Applet applet, String arg) {
        try {
            return new MediaLocator(new URL(arg));
        } catch (MalformedURLException e) {
            try {
                return new MediaLocator(new URL(applet.getDocumentBase(), arg));
            } catch (MalformedURLException e2) {
                return new MediaLocator(arg);
            }
        }
    }

    public static Icon createDisabledIcon(ImageIcon imageIcon) {
        return new ImageIcon(GrayFilter.createDisabledImage(imageIcon.getImage()));
    }

    public static URL fileToURL(String fileName) throws IOException, MalformedURLException {
        File file = new File(fileName);
        if (file.exists()) {
            return new URL("file:///" + file.getCanonicalPath());
        }
        throw new IOException("File " + fileName + " does not exist.");
    }

    public static String getExtension(File f) {
        return getExtension(f.getName());
    }

    public static String getExtension(String filename) {
        int i = filename.lastIndexOf(46);
        if (i <= 0 || i >= filename.length() - 1) {
            return null;
        }
        return filename.substring(i + 1).toLowerCase();
    }

    public static Time getMaximumLatency(Controller[] controllers) {
        Time maxLatency = new Time((double) Pa.LATENCY_UNSPECIFIED);
        for (int i = 0; i < controllers.length; i++) {
            if (controllers[i].getState() >= Controller.Realized) {
                Time thisTime = controllers[i].getStartLatency();
                if (thisTime != Controller.LATENCY_UNKNOWN && thisTime.getSeconds() > Pa.LATENCY_UNSPECIFIED) {
                    maxLatency = thisTime;
                }
            }
        }
        return maxLatency;
    }

    public static int pickAMaster(Player[] players) {
        for (int i = 0; i < players.length; i++) {
            GainControl gain = players[i].getGainControl();
            if (gain != null && gain.getControlComponent() != null) {
                return i;
            }
        }
        return 0;
    }

    public static void showControls(Controller controller) {
        Control[] controls = controller.getControls();
        for (int i = 0; i < controls.length; i++) {
            Component c = controls[i].getControlComponent();
            if (!(c == null || c.isShowing())) {
                JFrame frame = new JFrame(controls[i].getClass().getName());
                frame.getContentPane().add(c);
                frame.pack();
                frame.setVisible(true);
            }
        }
    }

    public static String stateToString(int state) {
        switch (state) {
            case 100:
                return "Unrealized";
            case 200:
                return "Realizing";
            case Controller.Realized /*300*/:
                return "Realized";
            case Controller.Prefetching /*400*/:
                return "Prefetching";
            case 500:
                return "Prefetched";
            case Controller.Started /*600*/:
                return "Started";
            default:
                return null;
        }
    }
}
