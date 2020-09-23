package org.jitsi.service.configuration;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ConfigurationService {
    public static final String PNAME_CONFIGURATION_FILE_IS_READ_ONLY = "net.java.sip.communicator.CONFIGURATION_FILE_IS_READ_ONLY";
    public static final String PNAME_CONFIGURATION_FILE_NAME = "net.java.sip.communicator.CONFIGURATION_FILE_NAME";
    public static final String PNAME_SC_CACHE_DIR_LOCATION = "net.java.sip.communicator.SC_CACHE_DIR_LOCATION";
    public static final String PNAME_SC_HOME_DIR_LOCATION = "net.java.sip.communicator.SC_HOME_DIR_LOCATION";
    public static final String PNAME_SC_HOME_DIR_NAME = "net.java.sip.communicator.SC_HOME_DIR_NAME";
    public static final String PNAME_SC_LOG_DIR_LOCATION = "net.java.sip.communicator.SC_LOG_DIR_LOCATION";

    void addPropertyChangeListener(PropertyChangeListener propertyChangeListener);

    void addPropertyChangeListener(String str, PropertyChangeListener propertyChangeListener);

    void addVetoableChangeListener(String str, ConfigVetoableChangeListener configVetoableChangeListener);

    void addVetoableChangeListener(ConfigVetoableChangeListener configVetoableChangeListener);

    List<String> getAllPropertyNames();

    boolean getBoolean(String str, boolean z);

    String getConfigurationFilename();

    int getInt(String str, int i);

    long getLong(String str, long j);

    Object getProperty(String str);

    List<String> getPropertyNamesByPrefix(String str, boolean z);

    List<String> getPropertyNamesBySuffix(String str);

    String getScHomeDirLocation();

    String getScHomeDirName();

    String getString(String str);

    String getString(String str, String str2);

    void purgeStoredConfiguration();

    void reloadConfiguration() throws IOException;

    void removeProperty(String str);

    void removePropertyChangeListener(PropertyChangeListener propertyChangeListener);

    void removePropertyChangeListener(String str, PropertyChangeListener propertyChangeListener);

    void removeVetoableChangeListener(String str, ConfigVetoableChangeListener configVetoableChangeListener);

    void removeVetoableChangeListener(ConfigVetoableChangeListener configVetoableChangeListener);

    void setProperties(Map<String, Object> map);

    void setProperty(String str, Object obj);

    void setProperty(String str, Object obj, boolean z);

    void storeConfiguration() throws IOException;
}
