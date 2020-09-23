package net.java.sip.communicator.impl.protocol.sip;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import net.java.sip.communicator.service.protocol.ProtocolIcon;
import net.java.sip.communicator.util.Logger;
import org.jitsi.service.resources.ResourceManagementService;

public class ProtocolIconSipImpl implements ProtocolIcon {
    private static final Logger logger = Logger.getLogger(ProtocolIconSipImpl.class);
    private final String iconPath;
    private Hashtable<String, String> iconPathsTable;
    private Hashtable<String, byte[]> iconsTable;

    public ProtocolIconSipImpl(String iconPath) {
        this.iconPath = iconPath;
    }

    public Iterator<String> getSupportedSizes() {
        return getIconsTable().keySet().iterator();
    }

    public boolean isSizeSupported(String iconSize) {
        return getIconsTable().containsKey(iconSize);
    }

    public byte[] getIcon(String iconSize) {
        return (byte[]) getIconsTable().get(iconSize);
    }

    public String getIconPath(String iconSize) {
        return (String) getIconPathsTable().get(iconSize);
    }

    private synchronized Map<String, String> getIconPathsTable() {
        if (this.iconPathsTable == null) {
            loadIconsFromIconPath();
        }
        return this.iconPathsTable;
    }

    private synchronized Map<String, byte[]> getIconsTable() {
        if (this.iconsTable == null) {
            loadIconsFromIconPath();
        }
        return this.iconsTable;
    }

    public byte[] getConnectingIcon() {
        return loadIcon(this.iconPath + "/sip-connecting.gif");
    }

    private void loadIconFromIconPath(String iconSize, String iconFileName) {
        String iconFilePath = this.iconPath + '/' + iconFileName;
        byte[] icon = loadIcon(iconFilePath);
        if (icon != null) {
            this.iconsTable.put(iconSize, icon);
            this.iconPathsTable.put(iconSize, iconFilePath);
        }
    }

    private synchronized void loadIconsFromIconPath() {
        this.iconsTable = new Hashtable();
        this.iconPathsTable = new Hashtable();
        loadIconFromIconPath("IconSize16x16", "sip16x16.png");
        loadIconFromIconPath("IconSize32x32", "sip32x32.png");
        loadIconFromIconPath("IconSize48x48", "sip48x48.png");
        loadIconFromIconPath("IconSize64x64", "sip64x64.png");
    }

    public static byte[] loadIcon(String imagePath) {
        ResourceManagementService resources = SipActivator.getResources();
        byte[] icon = null;
        if (resources != null) {
            InputStream is = resources.getImageInputStreamForPath(imagePath);
            if (is == null) {
                return null;
            }
            try {
                icon = new byte[is.available()];
                is.read(icon);
            } catch (IOException ioex) {
                logger.error("Failed to load protocol icon: " + imagePath, ioex);
            }
        }
        return icon;
    }
}
