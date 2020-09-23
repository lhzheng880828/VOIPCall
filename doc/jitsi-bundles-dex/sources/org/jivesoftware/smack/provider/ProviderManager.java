package org.jivesoftware.smack.provider;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.PacketExtension;
import org.xmlpull.mxp1.MXParser;

public class ProviderManager {
    private static ProviderManager instance;
    private Map<String, Object> extensionProviders = new ConcurrentHashMap();
    private Map<String, Object> iqProviders = new ConcurrentHashMap();

    public static synchronized ProviderManager getInstance() {
        ProviderManager providerManager;
        synchronized (ProviderManager.class) {
            if (instance == null) {
                instance = new ProviderManager();
                instance.initialize();
            }
            providerManager = instance;
        }
        return providerManager;
    }

    public static synchronized void setInstance(ProviderManager providerManager) {
        synchronized (ProviderManager.class) {
            if (instance != null) {
                throw new IllegalStateException("ProviderManager singleton already set");
            }
            instance = providerManager;
        }
    }

    /* access modifiers changed from: protected */
    public void initialize() {
        try {
            for (ClassLoader classLoader : getClassLoaders()) {
                Enumeration providerEnum = classLoader.getResources("META-INF/smack.providers");
                while (providerEnum.hasMoreElements()) {
                    InputStream providerStream = null;
                    try {
                        providerStream = ((URL) providerEnum.nextElement()).openStream();
                        XmlPullParser parser = new MXParser();
                        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
                        parser.setInput(providerStream, "UTF-8");
                        int eventType = parser.getEventType();
                        do {
                            if (eventType == 2) {
                                String elementName;
                                String namespace;
                                String className;
                                String key;
                                Class provider;
                                if (parser.getName().equals("iqProvider")) {
                                    parser.next();
                                    parser.next();
                                    elementName = parser.nextText();
                                    parser.next();
                                    parser.next();
                                    namespace = parser.nextText();
                                    parser.next();
                                    parser.next();
                                    className = parser.nextText();
                                    key = getProviderKey(elementName, namespace);
                                    if (!this.iqProviders.containsKey(key)) {
                                        provider = Class.forName(className);
                                        if (IQProvider.class.isAssignableFrom(provider)) {
                                            this.iqProviders.put(key, provider.newInstance());
                                        } else if (IQ.class.isAssignableFrom(provider)) {
                                            this.iqProviders.put(key, provider);
                                        }
                                    }
                                } else if (parser.getName().equals("extensionProvider")) {
                                    parser.next();
                                    parser.next();
                                    elementName = parser.nextText();
                                    parser.next();
                                    parser.next();
                                    namespace = parser.nextText();
                                    parser.next();
                                    parser.next();
                                    className = parser.nextText();
                                    key = getProviderKey(elementName, namespace);
                                    if (!this.extensionProviders.containsKey(key)) {
                                        try {
                                            provider = Class.forName(className);
                                            if (PacketExtensionProvider.class.isAssignableFrom(provider)) {
                                                this.extensionProviders.put(key, provider.newInstance());
                                            } else if (PacketExtension.class.isAssignableFrom(provider)) {
                                                this.extensionProviders.put(key, provider);
                                            }
                                        } catch (ClassNotFoundException cnfe) {
                                            cnfe.printStackTrace();
                                        }
                                    }
                                }
                            }
                            eventType = parser.next();
                        } while (eventType != 1);
                    } catch (ClassNotFoundException cnfe2) {
                        cnfe2.printStackTrace();
                    } catch (Throwable th) {
                        try {
                            providerStream.close();
                        } catch (Exception e) {
                        }
                    }
                    try {
                        providerStream.close();
                    } catch (Exception e2) {
                    }
                }
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
    }

    public Object getIQProvider(String elementName, String namespace) {
        return this.iqProviders.get(getProviderKey(elementName, namespace));
    }

    public Collection<Object> getIQProviders() {
        return Collections.unmodifiableCollection(this.iqProviders.values());
    }

    public void addIQProvider(String elementName, String namespace, Object provider) {
        if ((provider instanceof IQProvider) || ((provider instanceof Class) && IQ.class.isAssignableFrom((Class) provider))) {
            this.iqProviders.put(getProviderKey(elementName, namespace), provider);
            return;
        }
        throw new IllegalArgumentException("Provider must be an IQProvider or a Class instance.");
    }

    public void removeIQProvider(String elementName, String namespace) {
        this.iqProviders.remove(getProviderKey(elementName, namespace));
    }

    public Object getExtensionProvider(String elementName, String namespace) {
        return this.extensionProviders.get(getProviderKey(elementName, namespace));
    }

    public void addExtensionProvider(String elementName, String namespace, Object provider) {
        if ((provider instanceof PacketExtensionProvider) || (provider instanceof Class)) {
            this.extensionProviders.put(getProviderKey(elementName, namespace), provider);
            return;
        }
        throw new IllegalArgumentException("Provider must be a PacketExtensionProvider or a Class instance.");
    }

    public void removeExtensionProvider(String elementName, String namespace) {
        this.extensionProviders.remove(getProviderKey(elementName, namespace));
    }

    public Collection<Object> getExtensionProviders() {
        return Collections.unmodifiableCollection(this.extensionProviders.values());
    }

    private String getProviderKey(String elementName, String namespace) {
        StringBuilder buf = new StringBuilder();
        buf.append(Separators.LESS_THAN).append(elementName).append("/><").append(namespace).append("/>");
        return buf.toString();
    }

    private ClassLoader[] getClassLoaders() {
        ClassLoader[] classLoaders = new ClassLoader[]{ProviderManager.class.getClassLoader(), Thread.currentThread().getContextClassLoader()};
        List<ClassLoader> loaders = new ArrayList();
        for (ClassLoader classLoader : classLoaders) {
            if (classLoader != null) {
                loaders.add(classLoader);
            }
        }
        return (ClassLoader[]) loaders.toArray(new ClassLoader[loaders.size()]);
    }
}
