package net.java.sip.communicator.impl.protocol.jabber;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import net.java.sip.communicator.service.protocol.ProtocolIcon;
import net.java.sip.communicator.util.Logger;
import org.jitsi.service.resources.ResourceManagementService;
import org.osgi.framework.ServiceReference;

public class ProtocolIconJabberImpl implements ProtocolIcon {
    private static final Logger logger = Logger.getLogger(ProtocolIconJabberImpl.class);
    private static ResourceManagementService resourcesService;
    private final String iconPath;
    private final Hashtable<String, String> iconPathsTable = new Hashtable();
    private final Hashtable<String, byte[]> iconsTable = new Hashtable();

    public ProtocolIconJabberImpl(String iconPath) {
        this.iconPath = iconPath;
        this.iconsTable.put("IconSize16x16", loadIcon(iconPath + "/status16x16-online.png"));
        this.iconsTable.put("IconSize32x32", loadIcon(iconPath + "/logo32x32.png"));
        this.iconsTable.put("IconSize48x48", loadIcon(iconPath + "/logo48x48.png"));
        this.iconPathsTable.put("IconSize16x16", iconPath + "/status16x16-online.png");
        this.iconPathsTable.put("IconSize32x32", iconPath + "/logo32x32.png");
        this.iconPathsTable.put("IconSize48x48", iconPath + "/logo48x48.png");
    }

    public Iterator<String> getSupportedSizes() {
        return this.iconsTable.keySet().iterator();
    }

    public boolean isSizeSupported(String iconSize) {
        return this.iconsTable.containsKey(iconSize);
    }

    public byte[] getIcon(String iconSize) {
        return (byte[]) this.iconsTable.get(iconSize);
    }

    public String getIconPath(String iconSize) {
        return (String) this.iconPathsTable.get(iconSize);
    }

    public byte[] getConnectingIcon() {
        return loadIcon(this.iconPath + "/status16x16-connecting.gif");
    }

    public static byte[] loadIcon(String imagePath) {
        InputStream is = null;
        try {
            is = new URL(imagePath).openStream();
        } catch (Exception e) {
        }
        if (is == null) {
            is = getResources().getImageInputStreamForPath(imagePath);
        }
        if (is == null) {
            return new byte[0];
        }
        byte[] icon = null;
        try {
            icon = new byte[is.available()];
            is.read(icon);
            return icon;
        } catch (IOException e2) {
            logger.error("Failed to load icon: " + imagePath, e2);
            return icon;
        }
    }

    public static ResourceManagementService getResources() {
        if (resourcesService == null) {
            ServiceReference serviceReference = JabberActivator.bundleContext.getServiceReference(ResourceManagementService.class.getName());
            if (serviceReference == null) {
                return null;
            }
            resourcesService = (ResourceManagementService) JabberActivator.bundleContext.getService(serviceReference);
        }
        return resourcesService;
    }
}
