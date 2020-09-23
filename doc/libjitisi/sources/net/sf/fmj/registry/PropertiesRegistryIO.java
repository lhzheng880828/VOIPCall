package net.sf.fmj.registry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Vector;

class PropertiesRegistryIO implements RegistryIO {
    private static final int MAX = 100;
    final String CONTENT_PREFIX_STRING = "content-prefix";
    final String[] PLUGIN_TYPE_STRINGS = new String[]{"demux", "codec", "effect", "renderer", "mux"};
    final String PROTOCOL_PREFIX_STRING = "protocol-prefix";
    private final RegistryContents contents;

    public PropertiesRegistryIO(RegistryContents contents) {
        this.contents = contents;
    }

    private void fromProperties(Properties p) {
        Vector<String> v;
        int j;
        String s;
        for (int i = 0; i < this.contents.plugins.length; i++) {
            String typeStr = this.PLUGIN_TYPE_STRINGS[i];
            v = this.contents.plugins[i];
            for (j = 0; j < 100; j++) {
                s = p.getProperty(typeStr + j);
                if (!(s == null || s.equals(""))) {
                    v.add(s);
                }
            }
        }
        v = this.contents.contentPrefixList;
        for (j = 0; j < 100; j++) {
            s = p.getProperty("content-prefix" + j);
            if (!(s == null || s.equals(""))) {
                v.add(s);
            }
        }
        v = this.contents.protocolPrefixList;
        for (j = 0; j < 100; j++) {
            s = p.getProperty("protocol-prefix" + j);
            if (!(s == null || s.equals(""))) {
                v.add(s);
            }
        }
    }

    public void load(InputStream is) throws IOException {
        Properties p = new Properties();
        p.load(is);
        fromProperties(p);
    }

    private Properties toProperties() {
        Vector<String> v;
        int j;
        Properties p = new Properties();
        for (int i = 0; i < this.contents.plugins.length; i++) {
            String typeStr = this.PLUGIN_TYPE_STRINGS[i];
            v = this.contents.plugins[i];
            for (j = 0; j < v.size(); j++) {
                p.setProperty(typeStr + j, (String) v.get(j));
            }
        }
        v = this.contents.contentPrefixList;
        for (j = 0; j < v.size(); j++) {
            p.setProperty("content-prefix" + j, (String) v.get(j));
        }
        v = this.contents.protocolPrefixList;
        for (j = 0; j < v.size(); j++) {
            p.setProperty("protocol-prefix" + j, (String) v.get(j));
        }
        return p;
    }

    public void write(OutputStream os) throws IOException {
        toProperties().store(os, "FMJ registry");
    }
}
