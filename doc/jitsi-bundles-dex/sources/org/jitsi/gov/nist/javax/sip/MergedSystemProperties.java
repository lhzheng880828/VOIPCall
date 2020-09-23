package org.jitsi.gov.nist.javax.sip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Enumeration;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.Set;

public class MergedSystemProperties extends Properties {
    private static final long serialVersionUID = -7922854860297151103L;
    private Properties parent;

    public MergedSystemProperties(Properties props) {
        this.parent = props;
    }

    public void list(PrintStream out) {
        this.parent.list(out);
    }

    public void list(PrintWriter out) {
        this.parent.list(out);
    }

    public synchronized void load(InputStream inStream) throws IOException {
        this.parent.load(inStream);
    }

    public synchronized void load(Reader reader) throws IOException {
        throw new RuntimeException("Not implemented for Java 5 compatibility");
    }

    public synchronized void loadFromXML(InputStream in) throws IOException, InvalidPropertiesFormatException {
        this.parent.loadFromXML(in);
    }

    public Enumeration<?> propertyNames() {
        return this.parent.propertyNames();
    }

    public synchronized void save(OutputStream out, String comments) {
        this.parent.save(out, comments);
    }

    public synchronized Object setProperty(String key, String value) {
        return this.parent.setProperty(key, value);
    }

    public void store(OutputStream out, String comments) throws IOException {
        this.parent.store(out, comments);
    }

    public void store(Writer writer, String comments) throws IOException {
        throw new RuntimeException("Not implemented for Java 5 compatibility");
    }

    public synchronized void storeToXML(OutputStream os, String comment, String encoding) throws IOException {
        this.parent.storeToXML(os, comment, encoding);
    }

    public synchronized void storeToXML(OutputStream os, String comment) throws IOException {
        this.parent.storeToXML(os, comment);
    }

    public Set<String> stringPropertyNames() {
        throw new RuntimeException("Not implemented for Java 5 compatibility");
    }

    public String getProperty(String key, String defaultValue) {
        if (System.getProperty(key) != null) {
            return System.getProperty(key);
        }
        return this.parent.getProperty(key, defaultValue);
    }

    public String getProperty(String key) {
        if (System.getProperty(key) != null) {
            return System.getProperty(key);
        }
        return this.parent.getProperty(key);
    }

    public boolean containsKey(Object key) {
        return this.parent.containsKey(key);
    }

    public String toString() {
        return super.toString() + this.parent.toString();
    }
}
