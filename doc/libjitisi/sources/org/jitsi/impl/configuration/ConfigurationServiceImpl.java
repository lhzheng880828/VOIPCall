package org.jitsi.impl.configuration;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import org.jitsi.impl.configuration.xml.XMLConfigurationStore;
import org.jitsi.service.configuration.ConfigPropertyVetoException;
import org.jitsi.service.configuration.ConfigVetoableChangeListener;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.fileaccess.FailSafeTransaction;
import org.jitsi.service.fileaccess.FileAccessService;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.util.Logger;
import org.jitsi.util.OSUtils;
import org.jitsi.util.xml.XMLException;

public class ConfigurationServiceImpl implements ConfigurationService {
    private static final String DEFAULT_CONFIGURATION_STORE_CLASS_NAME = "net.java.sip.communicator.impl.configuration.SQLiteConfigurationStore";
    private static final String DEFAULT_OVERRIDES_PROPS_FILE_NAME = "jitsi-default-overrides.properties";
    private static final String DEFAULT_PROPS_FILE_NAME = "jitsi-defaults.properties";
    private static final String SYS_PROPS_FILE_NAME_PROPERTY = "net.java.sip.communicator.SYS_PROPS_FILE_NAME";
    private final ChangeEventDispatcher changeEventDispatcher = new ChangeEventDispatcher(this);
    private File configurationFile = null;
    private Map<String, String> defaultProperties = new HashMap();
    private final FileAccessService faService = LibJitsi.getFileAccessService();
    private Map<String, String> immutableDefaultProperties = new HashMap();
    private final Logger logger = Logger.getLogger(ConfigurationServiceImpl.class);
    private ConfigurationStore store;

    public ConfigurationServiceImpl() {
        try {
            debugPrintSystemProperties();
            preloadSystemPropertyFiles();
            loadDefaultProperties();
            reloadConfiguration();
        } catch (IOException ex) {
            this.logger.error("Failed to load the configuration file", ex);
        }
    }

    public void setProperty(String propertyName, Object property) throws ConfigPropertyVetoException {
        setProperty(propertyName, property, false);
    }

    public void setProperty(String propertyName, Object property, boolean isSystem) throws ConfigPropertyVetoException {
        Object oldValue = getProperty(propertyName);
        if (this.changeEventDispatcher.hasVetoableChangeListeners(propertyName)) {
            this.changeEventDispatcher.fireVetoableChange(propertyName, oldValue, property);
        }
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(propertyName + "( oldValue=" + oldValue + ", newValue=" + property + ".");
        }
        doSetProperty(propertyName, property, isSystem);
        try {
            storeConfiguration();
        } catch (IOException e) {
            this.logger.error("Failed to store configuration after a property change");
        }
        if (this.changeEventDispatcher.hasPropertyChangeListeners(propertyName)) {
            this.changeEventDispatcher.firePropertyChange(propertyName, oldValue, property);
        }
    }

    public void setProperties(Map<String, Object> properties) throws ConfigPropertyVetoException {
        String propertyName;
        Map<String, Object> oldValues = new HashMap(properties.size());
        for (Entry<String, Object> property : properties.entrySet()) {
            propertyName = (String) property.getKey();
            Object oldValue = getProperty(propertyName);
            oldValues.put(propertyName, oldValue);
            if (this.changeEventDispatcher.hasVetoableChangeListeners(propertyName)) {
                this.changeEventDispatcher.fireVetoableChange(propertyName, oldValue, property.getValue());
            }
        }
        for (Entry<String, Object> property2 : properties.entrySet()) {
            doSetProperty((String) property2.getKey(), property2.getValue(), false);
        }
        try {
            storeConfiguration();
        } catch (IOException e) {
            this.logger.error("Failed to store configuration after property changes");
        }
        for (Entry<String, Object> property22 : properties.entrySet()) {
            propertyName = (String) property22.getKey();
            if (this.changeEventDispatcher.hasPropertyChangeListeners(propertyName)) {
                this.changeEventDispatcher.firePropertyChange(propertyName, oldValues.get(propertyName), property22.getValue());
            }
        }
    }

    private void doSetProperty(String propertyName, Object property, boolean isSystem) {
        if (isSystemProperty(propertyName)) {
            isSystem = true;
        }
        if (!this.immutableDefaultProperties.containsKey(propertyName)) {
            if (property == null) {
                this.store.removeProperty(propertyName);
                if (isSystem) {
                    System.setProperty(propertyName, "");
                }
            } else if (isSystem) {
                System.setProperty(propertyName, property.toString());
                this.store.setSystemProperty(propertyName);
            } else {
                this.store.setNonSystemProperty(propertyName, property);
            }
        }
    }

    public void removeProperty(String propertyName) {
        for (String pName : getPropertyNamesByPrefix(propertyName, false)) {
            removeProperty(pName);
        }
        Object oldValue = getProperty(propertyName);
        if (this.changeEventDispatcher.hasVetoableChangeListeners(propertyName)) {
            this.changeEventDispatcher.fireVetoableChange(propertyName, oldValue, null);
        }
        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Will remove prop: " + propertyName + ".");
        }
        this.store.removeProperty(propertyName);
        if (this.changeEventDispatcher.hasPropertyChangeListeners(propertyName)) {
            this.changeEventDispatcher.firePropertyChange(propertyName, oldValue, null);
        }
        try {
            storeConfiguration();
        } catch (IOException e) {
            this.logger.error("Failed to store configuration after a property change");
        }
    }

    public Object getProperty(String propertyName) {
        Object result = this.immutableDefaultProperties.get(propertyName);
        if (result != null) {
            return result;
        }
        result = this.store.getProperty(propertyName);
        Object obj;
        if (result != null) {
            obj = result;
            return result;
        }
        obj = result;
        return this.defaultProperties.get(propertyName);
    }

    public List<String> getAllPropertyNames() {
        List<String> resultKeySet = new LinkedList();
        for (String key : this.store.getPropertyNames()) {
            resultKeySet.add(key);
        }
        return resultKeySet;
    }

    public List<String> getPropertyNamesByPrefix(String prefix, boolean exactPrefixMatch) {
        Set<String> propertyNameSet;
        HashSet<String> resultKeySet = new HashSet();
        if (this.immutableDefaultProperties.size() > 0) {
            propertyNameSet = this.immutableDefaultProperties.keySet();
            getPropertyNamesByPrefix(prefix, exactPrefixMatch, (String[]) propertyNameSet.toArray(new String[propertyNameSet.size()]), resultKeySet);
        }
        getPropertyNamesByPrefix(prefix, exactPrefixMatch, this.store.getPropertyNames(), resultKeySet);
        if (this.defaultProperties.size() > 0) {
            propertyNameSet = this.defaultProperties.keySet();
            getPropertyNamesByPrefix(prefix, exactPrefixMatch, (String[]) propertyNameSet.toArray(new String[propertyNameSet.size()]), resultKeySet);
        }
        return new ArrayList(resultKeySet);
    }

    private Set<String> getPropertyNamesByPrefix(String prefix, boolean exactPrefixMatch, String[] names, Set<String> resultSet) {
        for (String key : names) {
            int ix = key.lastIndexOf(46);
            if (ix != -1) {
                String keyPrefix = key.substring(0, ix);
                if (exactPrefixMatch) {
                    if (prefix.equals(keyPrefix)) {
                        resultSet.add(key);
                    }
                } else if (keyPrefix.startsWith(prefix)) {
                    resultSet.add(key);
                }
            }
        }
        return resultSet;
    }

    public List<String> getPropertyNamesBySuffix(String suffix) {
        List<String> resultKeySet = new LinkedList();
        for (String key : this.store.getPropertyNames()) {
            int ix = key.lastIndexOf(46);
            if (ix != -1 && suffix.equals(key.substring(ix + 1))) {
                resultKeySet.add(key);
            }
        }
        return resultKeySet;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.changeEventDispatcher.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.changeEventDispatcher.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        this.changeEventDispatcher.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        this.changeEventDispatcher.removePropertyChangeListener(propertyName, listener);
    }

    public void addVetoableChangeListener(ConfigVetoableChangeListener listener) {
        this.changeEventDispatcher.addVetoableChangeListener(listener);
    }

    public void removeVetoableChangeListener(ConfigVetoableChangeListener listener) {
        this.changeEventDispatcher.removeVetoableChangeListener(listener);
    }

    public void addVetoableChangeListener(String propertyName, ConfigVetoableChangeListener listener) {
        this.changeEventDispatcher.addVetoableChangeListener(propertyName, listener);
    }

    public void removeVetoableChangeListener(String propertyName, ConfigVetoableChangeListener listener) {
        this.changeEventDispatcher.removeVetoableChangeListener(propertyName, listener);
    }

    public void reloadConfiguration() throws IOException {
        this.configurationFile = null;
        File file = getConfigurationFile();
        if (!(file == null || this.faService == null)) {
            try {
                this.faService.createFailSafeTransaction(file).restoreFile();
            } catch (Exception e) {
                this.logger.error("Failed to restore configuration file " + file, e);
            }
        }
        try {
            this.store.reloadConfiguration(file);
        } catch (XMLException xmle) {
            IOException ioe = new IOException();
            ioe.initCause(xmle);
            throw ioe;
        }
    }

    public synchronized void storeConfiguration() throws IOException {
        storeConfiguration(getConfigurationFile());
    }

    private void storeConfiguration(File file) throws IOException {
        OutputStream stream;
        String readOnly = System.getProperty(ConfigurationService.PNAME_CONFIGURATION_FILE_IS_READ_ONLY);
        if ((readOnly == null || !Boolean.parseBoolean(readOnly)) && this.faService != null) {
            FailSafeTransaction trans = file == null ? null : this.faService.createFailSafeTransaction(file);
            Throwable exception = null;
            if (trans != null) {
                try {
                    trans.beginTransaction();
                } catch (IllegalStateException isex) {
                    exception = isex;
                } catch (IOException ioex) {
                    exception = ioex;
                } catch (Throwable th) {
                    if (stream != null) {
                        stream.close();
                    }
                }
            }
            if (file == null) {
                stream = null;
            } else {
                stream = new FileOutputStream(file);
            }
            this.store.storeConfiguration(stream);
            if (stream != null) {
                stream.close();
            }
            if (trans != null) {
                trans.commit();
            }
            if (exception != null) {
                this.logger.error("can't write data in the configuration file", exception);
                if (trans != null) {
                    trans.rollback();
                }
            }
        }
    }

    public String getConfigurationFilename() {
        try {
            File file = getConfigurationFile();
            if (file != null) {
                return file.getName();
            }
        } catch (IOException ex) {
            this.logger.error("Error loading configuration file", ex);
        }
        return null;
    }

    private File getConfigurationFile() throws IOException {
        if (this.configurationFile == null) {
            createConfigurationFile();
            getScHomeDirLocation();
            getScHomeDirName();
        }
        return this.configurationFile;
    }

    private void createConfigurationFile() throws IOException {
        File configurationFile = getConfigurationFile("xml", false);
        if (configurationFile == null) {
            setConfigurationStore(XMLConfigurationStore.class);
            return;
        }
        String name = configurationFile.getName();
        int extensionBeginIndex = name.lastIndexOf(46);
        if (".properties".equalsIgnoreCase(extensionBeginIndex > -1 ? name.substring(extensionBeginIndex) : null)) {
            this.configurationFile = configurationFile;
            if (!(this.store instanceof PropertyConfigurationStore)) {
                this.store = new PropertyConfigurationStore();
                return;
            }
            return;
        }
        File parentFile = configurationFile.getParentFile();
        StringBuilder stringBuilder = new StringBuilder();
        if (extensionBeginIndex > -1) {
            name = name.substring(0, extensionBeginIndex);
        }
        File newConfigurationFile = new File(parentFile, stringBuilder.append(name).append(".properties").toString());
        if (newConfigurationFile.exists()) {
            this.configurationFile = newConfigurationFile;
            if (!(this.store instanceof PropertyConfigurationStore)) {
                this.store = new PropertyConfigurationStore();
            }
        } else if (getSystemProperty(ConfigurationService.PNAME_CONFIGURATION_FILE_NAME) == null) {
            Class<? extends ConfigurationStore> defaultConfigurationStoreClass = getDefaultConfigurationStoreClass();
            if (configurationFile.exists()) {
                ConfigurationStore xmlStore = new XMLConfigurationStore();
                try {
                    xmlStore.reloadConfiguration(configurationFile);
                    setConfigurationStore(defaultConfigurationStoreClass);
                    if (this.store != null) {
                        copy(xmlStore, this.store);
                    }
                    Throwable exception = null;
                    try {
                        storeConfiguration(this.configurationFile);
                    } catch (IllegalStateException isex) {
                        exception = isex;
                    } catch (IOException ioex) {
                        exception = ioex;
                    }
                    if (exception == null) {
                        configurationFile.delete();
                        return;
                    }
                    this.configurationFile = configurationFile;
                    this.store = xmlStore;
                    return;
                } catch (XMLException xmlex) {
                    IOException ioex2 = new IOException();
                    ioex2.initCause(xmlex);
                    throw ioex2;
                }
            }
            setConfigurationStore(defaultConfigurationStoreClass);
        } else {
            if (!configurationFile.exists()) {
                configurationFile = getConfigurationFile("xml", true);
            }
            this.configurationFile = configurationFile;
            if (!(this.store instanceof XMLConfigurationStore)) {
                this.store = new XMLConfigurationStore();
            }
        }
    }

    public String getScHomeDirLocation() {
        String scHomeDirLocation = null;
        if (this.store != null) {
            scHomeDirLocation = getString(ConfigurationService.PNAME_SC_HOME_DIR_LOCATION);
        }
        if (scHomeDirLocation == null) {
            scHomeDirLocation = getSystemProperty(ConfigurationService.PNAME_SC_HOME_DIR_LOCATION);
            if (scHomeDirLocation == null) {
                scHomeDirLocation = getSystemProperty("user.home");
            }
            if (this.store != null) {
                this.store.setNonSystemProperty(ConfigurationService.PNAME_SC_HOME_DIR_LOCATION, scHomeDirLocation);
            }
        }
        return scHomeDirLocation;
    }

    public String getScHomeDirName() {
        String scHomeDirName = null;
        if (this.store != null) {
            scHomeDirName = getString(ConfigurationService.PNAME_SC_HOME_DIR_NAME);
        }
        if (scHomeDirName == null) {
            scHomeDirName = getSystemProperty(ConfigurationService.PNAME_SC_HOME_DIR_NAME);
            if (scHomeDirName == null) {
                scHomeDirName = ".sip-communicator";
            }
            if (this.store != null) {
                this.store.setNonSystemProperty(ConfigurationService.PNAME_SC_HOME_DIR_NAME, scHomeDirName);
            }
        }
        return scHomeDirName;
    }

    private File getConfigurationFile(String extension, boolean create) throws IOException {
        String pFileName = getSystemProperty(ConfigurationService.PNAME_CONFIGURATION_FILE_NAME);
        if (pFileName == null) {
            pFileName = "sip-communicator." + extension;
        }
        File configFileInCurrentDir = new File(pFileName);
        if (configFileInCurrentDir.exists()) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Using config file in current dir: " + configFileInCurrentDir.getAbsolutePath());
            }
            return configFileInCurrentDir;
        }
        File configDir = new File(getScHomeDirLocation() + File.separator + getScHomeDirName());
        File configFileInUserHomeDir = new File(configDir, pFileName);
        if (!configFileInUserHomeDir.exists()) {
            InputStream in = getClass().getClassLoader().getResourceAsStream(pFileName);
            if (in == null) {
                if (create) {
                    configDir.mkdirs();
                    configFileInUserHomeDir.createNewFile();
                }
                if (!this.logger.isDebugEnabled()) {
                    return configFileInUserHomeDir;
                }
                this.logger.debug("Created an empty file in $HOME: " + configFileInUserHomeDir.getAbsolutePath());
                return configFileInUserHomeDir;
            }
            if (this.logger.isTraceEnabled()) {
                this.logger.trace("Copying config file from JAR into " + configFileInUserHomeDir.getAbsolutePath());
            }
            configDir.mkdirs();
            try {
                copy(in, configFileInUserHomeDir);
                return configFileInUserHomeDir;
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        } else if (!this.logger.isDebugEnabled()) {
            return configFileInUserHomeDir;
        } else {
            this.logger.debug("Using config file in $HOME/.sip-communicator: " + configFileInUserHomeDir.getAbsolutePath());
            return configFileInUserHomeDir;
        }
    }

    private static Class<? extends ConfigurationStore> getDefaultConfigurationStoreClass() {
        Class<? extends ConfigurationStore> defaultConfigurationStoreClass = null;
        if (DEFAULT_CONFIGURATION_STORE_CLASS_NAME != null) {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(DEFAULT_CONFIGURATION_STORE_CLASS_NAME);
            } catch (ClassNotFoundException e) {
            }
            if (clazz != null && ConfigurationStore.class.isAssignableFrom(clazz)) {
                defaultConfigurationStoreClass = clazz;
            }
        }
        if (defaultConfigurationStoreClass == null) {
            return PropertyConfigurationStore.class;
        }
        return defaultConfigurationStoreClass;
    }

    private static void copy(ConfigurationStore src, ConfigurationStore dest) {
        for (String name : src.getPropertyNames()) {
            if (src.isSystemProperty(name)) {
                dest.setSystemProperty(name);
            } else {
                dest.setNonSystemProperty(name, src.getProperty(name));
            }
        }
    }

    private static void copy(InputStream inputStream, File outputFile) throws IOException {
        OutputStream outputStream = new FileOutputStream(outputFile);
        try {
            byte[] bytes = new byte[4096];
            while (true) {
                int bytesRead = inputStream.read(bytes);
                if (bytesRead == -1) {
                    break;
                }
                outputStream.write(bytes, 0, bytesRead);
            }
        } finally {
            outputStream.close();
        }
    }

    private static String getSystemProperty(String propertyName) {
        String retval = System.getProperty(propertyName);
        if (retval == null || retval.trim().length() != 0) {
            return retval;
        }
        return null;
    }

    public String getString(String propertyName) {
        Object propValue = getProperty(propertyName);
        if (propValue == null) {
            return null;
        }
        String propStrValue = propValue.toString().trim();
        if (propStrValue.length() <= 0) {
            propStrValue = null;
        }
        return propStrValue;
    }

    public String getString(String propertyName, String defaultValue) {
        String value = getString(propertyName);
        return value != null ? value : defaultValue;
    }

    public boolean getBoolean(String propertyName, boolean defaultValue) {
        String stringValue = getString(propertyName);
        return stringValue == null ? defaultValue : Boolean.parseBoolean(stringValue);
    }

    public int getInt(String propertyName, int defaultValue) {
        String stringValue = getString(propertyName);
        int intValue = defaultValue;
        if (stringValue == null || stringValue.length() <= 0) {
            return intValue;
        }
        try {
            return Integer.parseInt(stringValue);
        } catch (NumberFormatException ex) {
            this.logger.error(propertyName + " does not appear to be an integer. " + "Defaulting to " + defaultValue + ".", ex);
            return intValue;
        }
    }

    public long getLong(String propertyName, long defaultValue) {
        String stringValue = getString(propertyName);
        long longValue = defaultValue;
        if (stringValue == null || stringValue.length() <= 0) {
            return longValue;
        }
        try {
            return Long.parseLong(stringValue);
        } catch (NumberFormatException ex) {
            this.logger.error(propertyName + " does not appear to be a longinteger. " + "Defaulting to " + defaultValue + ".", ex);
            return longValue;
        }
    }

    private boolean isSystemProperty(String propertyName) {
        return this.store.isSystemProperty(propertyName);
    }

    public void purgeStoredConfiguration() {
        if (this.configurationFile != null) {
            this.configurationFile.delete();
            this.configurationFile = null;
        }
        if (this.store != null) {
            for (String name : this.store.getPropertyNames()) {
                this.store.removeProperty(name);
            }
        }
    }

    private void debugPrintSystemProperties() {
        if (this.logger.isInfoEnabled()) {
            for (Entry<Object, Object> e : System.getProperties().entrySet()) {
                this.logger.info(e.getKey() + "=" + e.getValue());
            }
        }
    }

    private void preloadSystemPropertyFiles() {
        String propertyFilesListStr = System.getProperty(SYS_PROPS_FILE_NAME_PROPERTY);
        if (propertyFilesListStr != null && propertyFilesListStr.trim().length() != 0) {
            StringTokenizer tokenizer = new StringTokenizer(propertyFilesListStr, ";,", false);
            while (tokenizer.hasMoreTokens()) {
                String fileName = tokenizer.nextToken();
                try {
                    fileName = fileName.trim();
                    Properties fileProps = new Properties();
                    fileProps.load(ClassLoader.getSystemResourceAsStream(fileName));
                    for (Entry<Object, Object> entry : fileProps.entrySet()) {
                        System.setProperty((String) entry.getKey(), (String) entry.getValue());
                    }
                } catch (Exception ex) {
                    this.logger.error("Failed to load property file: " + fileName, ex);
                }
            }
        }
    }

    private void setConfigurationStore(Class<? extends ConfigurationStore> clazz) throws IOException {
        File file;
        String extension = null;
        if (PropertyConfigurationStore.class.isAssignableFrom(clazz)) {
            extension = "properties";
        } else if (XMLConfigurationStore.class.isAssignableFrom(clazz)) {
            extension = "xml";
        }
        if (extension == null) {
            file = null;
        } else {
            file = getConfigurationFile(extension, true);
        }
        this.configurationFile = file;
        if (!clazz.isInstance(this.store)) {
            Throwable exception = null;
            try {
                this.store = (ConfigurationStore) clazz.newInstance();
            } catch (IllegalAccessException iae) {
                exception = iae;
            } catch (InstantiationException ie) {
                exception = ie;
            }
            if (exception != null) {
                throw new RuntimeException(exception);
            }
        }
    }

    private void loadDefaultProperties() {
        loadDefaultProperties(DEFAULT_PROPS_FILE_NAME);
        loadDefaultProperties(DEFAULT_OVERRIDES_PROPS_FILE_NAME);
    }

    private void loadDefaultProperties(String fileName) {
        try {
            InputStream fileStream;
            Properties fileProps = new Properties();
            if (OSUtils.IS_ANDROID) {
                fileStream = getClass().getClassLoader().getResourceAsStream(fileName);
            } else {
                fileStream = ClassLoader.getSystemResourceAsStream(fileName);
            }
            fileProps.load(fileStream);
            fileStream.close();
            for (Entry<Object, Object> entry : fileProps.entrySet()) {
                String name = (String) entry.getKey();
                String value = (String) entry.getValue();
                if (!(name == null || value == null || name.trim().length() == 0)) {
                    if (name.startsWith("*")) {
                        name = name.substring(1);
                        if (name.trim().length() != 0) {
                            this.immutableDefaultProperties.put(name, value);
                            this.defaultProperties.remove(name);
                        }
                    } else {
                        this.defaultProperties.put(name, value);
                        this.immutableDefaultProperties.remove(name);
                    }
                }
            }
        } catch (Exception ex) {
            this.logger.info("No defaults property file loaded: " + fileName + ". Not a problem.");
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("load exception", ex);
            }
        }
    }
}
