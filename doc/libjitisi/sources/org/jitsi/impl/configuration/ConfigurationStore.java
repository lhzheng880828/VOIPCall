package org.jitsi.impl.configuration;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import org.jitsi.util.xml.XMLException;

public interface ConfigurationStore {
    Object getProperty(String str);

    String[] getPropertyNames();

    boolean isSystemProperty(String str);

    void reloadConfiguration(File file) throws IOException, XMLException;

    void removeProperty(String str);

    void setNonSystemProperty(String str, Object obj);

    void setSystemProperty(String str);

    void storeConfiguration(OutputStream outputStream) throws IOException;
}
