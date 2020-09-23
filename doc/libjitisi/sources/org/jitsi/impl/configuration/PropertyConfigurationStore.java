package org.jitsi.impl.configuration;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class PropertyConfigurationStore extends HashtableConfigurationStore<Properties> {
    public PropertyConfigurationStore() {
        super(new SortedProperties());
    }

    public void reloadConfiguration(File file) throws IOException {
        ((Properties) this.properties).clear();
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        try {
            ((Properties) this.properties).load(in);
        } finally {
            in.close();
        }
    }

    public void setNonSystemProperty(String name, Object value) {
        ((Properties) this.properties).setProperty(name, value.toString());
    }

    public void storeConfiguration(OutputStream out) throws IOException {
        ((Properties) this.properties).store(out, null);
    }
}
