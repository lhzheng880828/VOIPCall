package org.jivesoftware.smack;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.xmlpull.mxp1.MXParser;

public final class SmackConfiguration {
    private static final String SMACK_VERSION = "3.2.2";
    private static Vector<String> defaultMechs = new Vector();
    private static int keepAliveInterval;
    private static boolean localSocks5ProxyEnabled;
    private static int localSocks5ProxyPort;
    private static int packetCollectorSize;
    private static int packetReplyTimeout;

    static {
        packetReplyTimeout = 5000;
        keepAliveInterval = 30000;
        localSocks5ProxyEnabled = true;
        localSocks5ProxyPort = 7777;
        packetCollectorSize = 5000;
        try {
            for (ClassLoader classLoader : getClassLoaders()) {
                Enumeration configEnum = classLoader.getResources("META-INF/smack-config.xml");
                while (configEnum.hasMoreElements()) {
                    InputStream systemStream = null;
                    try {
                        systemStream = ((URL) configEnum.nextElement()).openStream();
                        XmlPullParser parser = new MXParser();
                        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
                        parser.setInput(systemStream, "UTF-8");
                        int eventType = parser.getEventType();
                        do {
                            if (eventType == 2) {
                                if (parser.getName().equals("className")) {
                                    parseClassToLoad(parser);
                                } else if (parser.getName().equals("packetReplyTimeout")) {
                                    packetReplyTimeout = parseIntProperty(parser, packetReplyTimeout);
                                } else if (parser.getName().equals("keepAliveInterval")) {
                                    keepAliveInterval = parseIntProperty(parser, keepAliveInterval);
                                } else if (parser.getName().equals("mechName")) {
                                    defaultMechs.add(parser.nextText());
                                } else if (parser.getName().equals("localSocks5ProxyEnabled")) {
                                    localSocks5ProxyEnabled = Boolean.parseBoolean(parser.nextText());
                                } else if (parser.getName().equals("localSocks5ProxyPort")) {
                                    localSocks5ProxyPort = parseIntProperty(parser, localSocks5ProxyPort);
                                } else if (parser.getName().equals("packetCollectorSize")) {
                                    packetCollectorSize = parseIntProperty(parser, packetCollectorSize);
                                }
                            }
                            eventType = parser.next();
                        } while (eventType != 1);
                        try {
                            systemStream.close();
                        } catch (Exception e) {
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                        try {
                            systemStream.close();
                        } catch (Exception e3) {
                        }
                    } catch (Throwable th) {
                        try {
                            systemStream.close();
                        } catch (Exception e4) {
                        }
                        throw th;
                    }
                }
            }
        } catch (Exception e22) {
            e22.printStackTrace();
        }
    }

    private SmackConfiguration() {
    }

    public static String getVersion() {
        return SMACK_VERSION;
    }

    public static int getPacketReplyTimeout() {
        if (packetReplyTimeout <= 0) {
            packetReplyTimeout = 5000;
        }
        return packetReplyTimeout;
    }

    public static void setPacketReplyTimeout(int timeout) {
        if (timeout <= 0) {
            throw new IllegalArgumentException();
        }
        packetReplyTimeout = timeout;
    }

    public static int getKeepAliveInterval() {
        return keepAliveInterval;
    }

    public static void setKeepAliveInterval(int interval) {
        keepAliveInterval = interval;
    }

    public static int getPacketCollectorSize() {
        return packetCollectorSize;
    }

    public static void setPacketCollectorSize(int collectorSize) {
        packetCollectorSize = collectorSize;
    }

    public static void addSaslMech(String mech) {
        if (!defaultMechs.contains(mech)) {
            defaultMechs.add(mech);
        }
    }

    public static void addSaslMechs(Collection<String> mechs) {
        for (String mech : mechs) {
            addSaslMech(mech);
        }
    }

    public static void removeSaslMech(String mech) {
        if (defaultMechs.contains(mech)) {
            defaultMechs.remove(mech);
        }
    }

    public static void removeSaslMechs(Collection<String> mechs) {
        for (String mech : mechs) {
            removeSaslMech(mech);
        }
    }

    public static List<String> getSaslMechs() {
        return defaultMechs;
    }

    public static boolean isLocalSocks5ProxyEnabled() {
        return localSocks5ProxyEnabled;
    }

    public static void setLocalSocks5ProxyEnabled(boolean localSocks5ProxyEnabled) {
        localSocks5ProxyEnabled = localSocks5ProxyEnabled;
    }

    public static int getLocalSocks5ProxyPort() {
        return localSocks5ProxyPort;
    }

    public static void setLocalSocks5ProxyPort(int localSocks5ProxyPort) {
        localSocks5ProxyPort = localSocks5ProxyPort;
    }

    private static void parseClassToLoad(XmlPullParser parser) throws Exception {
        String className = parser.nextText();
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            System.err.println("Error! A startup class specified in smack-config.xml could not be loaded: " + className);
        }
    }

    private static int parseIntProperty(XmlPullParser parser, int defaultValue) throws Exception {
        try {
            return Integer.parseInt(parser.nextText());
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            return defaultValue;
        }
    }

    private static ClassLoader[] getClassLoaders() {
        ClassLoader[] classLoaders = new ClassLoader[]{SmackConfiguration.class.getClassLoader(), Thread.currentThread().getContextClassLoader()};
        List<ClassLoader> loaders = new ArrayList();
        for (ClassLoader classLoader : classLoaders) {
            if (classLoader != null) {
                loaders.add(classLoader);
            }
        }
        return (ClassLoader[]) loaders.toArray(new ClassLoader[loaders.size()]);
    }
}
