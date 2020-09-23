package net.sf.fmj.registry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;
import javax.media.CaptureDeviceInfo;
import net.sf.fmj.media.RegistryDefaults;
import net.sf.fmj.utility.LoggerSingleton;
import net.sf.fmj.utility.PlugInInfo;
import net.sf.fmj.utility.PlugInUtility;

public class Registry {
    private static final int DEFAULT_REGISTRY_WRITE_FORMAT = 0;
    public static final int NUM_PLUGIN_TYPES = 5;
    private static final boolean READD_JAVAX = false;
    private static final int[] REGISTRY_FORMATS = new int[]{0, 1};
    private static final String SYSTEM_PROPERTY_DISABLE_COMMIT = "net.sf.fmj.utility.JmfRegistry.disableCommit";
    private static final String SYSTEM_PROPERTY_DISABLE_LOAD = "net.sf.fmj.utility.JmfRegistry.disableLoad";
    private static final Logger logger = LoggerSingleton.logger;
    private static Registry registry = null;
    private static Object registryMutex = new Object();
    private final boolean disableCommit;
    private final RegistryContents registryContents = new RegistryContents();

    public static Registry getInstance() {
        Registry registry;
        synchronized (registryMutex) {
            if (registry == null) {
                registry = new Registry();
            }
            registry = registry;
        }
        return registry;
    }

    private Registry() {
        String TRUE = Boolean.TRUE.toString();
        String FALSE = Boolean.FALSE.toString();
        this.disableCommit = System.getProperty(SYSTEM_PROPERTY_DISABLE_COMMIT, FALSE).equals(TRUE);
        try {
            if (System.getProperty(SYSTEM_PROPERTY_DISABLE_LOAD, FALSE).equals(TRUE)) {
                setDefaults();
                return;
            }
        } catch (SecurityException e) {
        }
        if (!load()) {
            logger.fine("Using registry defaults.");
            setDefaults();
        }
    }

    public synchronized boolean addDevice(CaptureDeviceInfo newDevice) {
        return this.registryContents.captureDeviceInfoList.add(newDevice);
    }

    public synchronized void addMimeType(String extension, String type) {
        this.registryContents.mimeTable.addMimeType(extension, type);
    }

    public synchronized void commit() throws IOException {
        if (!this.disableCommit) {
            File file = getRegistryFile(0);
            FileOutputStream fos = new FileOutputStream(file);
            try {
                RegistryIOFactory.createRegistryIO(0, this.registryContents).write(fos);
                fos.flush();
                logger.info("Wrote registry file: " + file.getAbsolutePath());
            } finally {
                fos.close();
            }
        }
    }

    public synchronized Vector<String> getContentPrefixList() {
        return (Vector) this.registryContents.contentPrefixList.clone();
    }

    public synchronized String getDefaultExtension(String mimeType) {
        return this.registryContents.mimeTable.getDefaultExtension(mimeType);
    }

    public synchronized Vector<CaptureDeviceInfo> getDeviceList() {
        return (Vector) this.registryContents.captureDeviceInfoList.clone();
    }

    public synchronized List<String> getExtensions(String mimeType) {
        return this.registryContents.mimeTable.getExtensions(mimeType);
    }

    public synchronized Hashtable<String, String> getMimeTable() {
        return this.registryContents.mimeTable.getMimeTable();
    }

    public synchronized String getMimeType(String extension) {
        return this.registryContents.mimeTable.getMimeType(extension);
    }

    public synchronized List<String> getPluginList(int pluginType) {
        return (List) this.registryContents.plugins[pluginType - 1].clone();
    }

    public synchronized Vector<String> getProtocolPrefixList() {
        return (Vector) this.registryContents.protocolPrefixList.clone();
    }

    private File getRegistryFile(int registryFormat) {
        String filename = System.getProperty("net.sf.fmj.utility.JmfRegistry.filename", registryFormat == 1 ? ".fmj.registry.properties" : ".fmj.registry.xml");
        File file = new File(filename);
        if (file.isAbsolute()) {
            return file;
        }
        return new File(System.getProperty("user.home"), filename);
    }

    private InputStream getRegistryResourceStream(int registryFormat) {
        return Registry.class.getResourceAsStream(registryFormat == 1 ? "/fmj.registry.properties" : "/fmj.registry.xml");
    }

    private synchronized boolean load() {
        boolean z = true;
        synchronized (this) {
            for (int registryFormat : REGISTRY_FORMATS) {
                if (loadFromResource(registryFormat)) {
                    break;
                }
            }
            for (int registryFormat2 : REGISTRY_FORMATS) {
                if (loadFromFile(registryFormat2)) {
                    break;
                }
            }
            z = READD_JAVAX;
        }
        return z;
    }

    private synchronized boolean loadFromFile(int registryFormat) {
        boolean z;
        try {
            File f = getRegistryFile(registryFormat);
            if (f.isFile() && f.length() > 0) {
                RegistryIOFactory.createRegistryIO(registryFormat, this.registryContents).load(new FileInputStream(f));
                logger.info("Loaded registry from file: " + f.getAbsolutePath());
                z = true;
            }
        } catch (Throwable t) {
            logger.warning("Problem loading registry from file: " + t.getMessage());
        }
        z = READD_JAVAX;
        return z;
    }

    private synchronized boolean loadFromResource(int registryFormat) {
        boolean z = READD_JAVAX;
        synchronized (this) {
            try {
                InputStream is = getRegistryResourceStream(registryFormat);
                if (is != null) {
                    RegistryIOFactory.createRegistryIO(registryFormat, this.registryContents).load(is);
                    logger.info("Loaded registry from resource, format: " + (registryFormat == 1 ? "Properties" : "XML"));
                    z = true;
                }
            } catch (Throwable t) {
                logger.warning("Problem loading registry from resource: " + t.getMessage());
            }
        }
        return z;
    }

    public synchronized boolean removeDevice(CaptureDeviceInfo device) {
        return this.registryContents.captureDeviceInfoList.remove(device);
    }

    public synchronized boolean removeMimeType(String fileExtension) {
        return this.registryContents.mimeTable.removeMimeType(fileExtension);
    }

    public synchronized void setContentPrefixList(List<String> list) {
        this.registryContents.contentPrefixList.clear();
        this.registryContents.contentPrefixList.addAll(list);
    }

    private void setDefaults() {
        int flags = RegistryDefaults.getDefaultFlags();
        this.registryContents.protocolPrefixList.addAll(RegistryDefaults.protocolPrefixList(flags));
        this.registryContents.contentPrefixList.addAll(RegistryDefaults.contentPrefixList(flags));
        for (PlugInInfo o : RegistryDefaults.plugInList(flags)) {
            PlugInInfo i;
            if (o instanceof PlugInInfo) {
                i = o;
                this.registryContents.plugins[i.type - 1].add(i.className);
            } else {
                i = PlugInUtility.getPlugInInfo((String) o);
                if (i != null) {
                    this.registryContents.plugins[i.type - 1].add(i.className);
                }
            }
        }
    }

    public synchronized void setPluginList(int pluginType, List<String> plugins) {
        Vector<String> pluginList = this.registryContents.plugins[pluginType - 1];
        pluginList.clear();
        pluginList.addAll(plugins);
    }

    public synchronized void setProtocolPrefixList(List<String> list) {
        this.registryContents.protocolPrefixList.clear();
        this.registryContents.protocolPrefixList.addAll(list);
    }
}
