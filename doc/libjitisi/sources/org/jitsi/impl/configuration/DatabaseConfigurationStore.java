package org.jitsi.impl.configuration;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import org.jitsi.util.xml.XMLException;

public abstract class DatabaseConfigurationStore extends HashtableConfigurationStore<Hashtable> {
    public abstract void reloadConfiguration() throws IOException;

    protected DatabaseConfigurationStore() {
        this(new Hashtable());
    }

    protected DatabaseConfigurationStore(Hashtable properties) {
        super(properties);
    }

    public void reloadConfiguration(File file) throws IOException, XMLException {
        this.properties.clear();
        reloadConfiguration();
    }

    /* access modifiers changed from: protected */
    public void storeConfiguration() throws IOException {
    }

    public void storeConfiguration(OutputStream out) throws IOException {
        storeConfiguration();
    }
}
