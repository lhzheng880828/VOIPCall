package net.java.sip.communicator.impl.protocol.jabber.extensions.caps;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import net.java.sip.communicator.impl.protocol.jabber.JabberActivator;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingleinfo.JingleInfoQueryIQ;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.util.Logger;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jitsi.org.xmlpull.v1.XmlPullParserException;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.util.OSUtils;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketExtensionFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.Base64;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.packet.DataForm;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.packet.DiscoverInfo.Feature;
import org.jivesoftware.smackx.packet.DiscoverInfo.Identity;
import org.xmlpull.mxp1.MXParser;

public class EntityCapsManager {
    private static final String CAPS_PROPERTY_NAME_PREFIX = "net.java.sip.communicator.impl.protocol.jabber.extensions.caps.EntityCapsManager.CAPS.";
    private static final UserCapsNodeListener[] NO_USER_CAPS_NODE_LISTENERS = new UserCapsNodeListener[0];
    private static final Map<Caps, DiscoverInfo> caps2discoverInfo = new ConcurrentHashMap();
    private static String entityNode;
    private static final Logger logger = Logger.getLogger(EntityCapsManager.class);
    private final Set<CapsVerListener> capsVerListeners = new CopyOnWriteArraySet();
    private String currentCapsVersion = null;
    private final Map<String, Caps> userCaps = new ConcurrentHashMap();
    private final List<UserCapsNodeListener> userCapsNodeListeners = new LinkedList();

    public static class Caps {
        public String ext;
        public final String hash;
        public final String node;
        private final String nodeVer;
        public final String ver;

        public Caps(String node, String hash, String ver, String ext) {
            if (node == null) {
                throw new NullPointerException("node");
            } else if (hash == null) {
                throw new NullPointerException("hash");
            } else if (ver == null) {
                throw new NullPointerException("ver");
            } else {
                this.node = node;
                this.hash = hash;
                this.ver = ver;
                this.ext = ext;
                this.nodeVer = this.node + '#' + this.ver;
            }
        }

        public final String getNodeVer() {
            return this.nodeVer;
        }

        public boolean isValid(DiscoverInfo discoverInfo) {
            if (discoverInfo != null) {
                if (discoverInfo.getNode() == null) {
                    discoverInfo.setNode(getNodeVer());
                }
                if (getNodeVer().equals(discoverInfo.getNode()) && !this.hash.equals("") && this.ver.equals(EntityCapsManager.capsToHash(this.hash, EntityCapsManager.calculateEntityCapsString(discoverInfo)))) {
                    return true;
                }
            }
            return false;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Caps caps = (Caps) o;
            if (!this.hash.equals(caps.hash)) {
                return false;
            }
            if (!this.node.equals(caps.node)) {
                return false;
            }
            if (this.ver.equals(caps.ver)) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return (((this.hash.hashCode() * 31) + this.node.hashCode()) * 31) + this.ver.hashCode();
        }
    }

    private class CapsPacketListener implements PacketListener {
        private CapsPacketListener() {
        }

        public void processPacket(Packet packet) {
            CapsPacketExtension ext = (CapsPacketExtension) packet.getExtension(CapsPacketExtension.ELEMENT_NAME, CapsPacketExtension.NAMESPACE);
            String hash = ext.getHash();
            if (hash == null) {
                hash = "";
            }
            if (hash != null) {
                boolean online = (packet instanceof Presence) && ((Presence) packet).isAvailable();
                if (online) {
                    EntityCapsManager.this.addUserCapsNode(packet.getFrom(), ext.getNode(), hash, ext.getVersion(), ext.getExtensions(), online);
                } else {
                    EntityCapsManager.this.removeUserCapsNode(packet.getFrom());
                }
            }
        }
    }

    static {
        String str;
        if (OSUtils.IS_ANDROID) {
            str = "http://android.jitsi.org";
        } else {
            str = "http://jitsi.org";
        }
        entityNode = str;
        ProviderManager.getInstance().addExtensionProvider(CapsPacketExtension.ELEMENT_NAME, CapsPacketExtension.NAMESPACE, new CapsProvider());
    }

    public static void addDiscoverInfoByCaps(Caps caps, DiscoverInfo info) {
        cleanupDiscoverInfo(info);
        info.setNode(caps.getNodeVer());
        synchronized (caps2discoverInfo) {
            DiscoverInfo oldInfo = (DiscoverInfo) caps2discoverInfo.put(caps, info);
            if (oldInfo == null || !oldInfo.equals(info)) {
                String xml = info.getChildElementXML();
                if (!(xml == null || xml.length() == 0)) {
                    JabberActivator.getConfigurationService().setProperty(getCapsPropertyName(caps), xml);
                }
            }
        }
    }

    private static String getCapsPropertyName(Caps caps) {
        return CAPS_PROPERTY_NAME_PREFIX + caps.node + '#' + caps.hash + '#' + caps.ver;
    }

    /* access modifiers changed from: private */
    public void addUserCapsNode(String user, String node, String hash, String ver, String ext, boolean online) {
        if (user != null && node != null && hash != null && ver != null) {
            Caps caps = (Caps) this.userCaps.get(user);
            if (caps == null || !caps.node.equals(node) || !caps.hash.equals(hash) || !caps.ver.equals(ver)) {
                UserCapsNodeListener[] listeners;
                caps = new Caps(node, hash, ver, ext);
                this.userCaps.put(user, caps);
                synchronized (this.userCapsNodeListeners) {
                    listeners = (UserCapsNodeListener[]) this.userCapsNodeListeners.toArray(NO_USER_CAPS_NODE_LISTENERS);
                }
                if (listeners.length != 0) {
                    String nodeVer = caps.getNodeVer();
                    for (UserCapsNodeListener listener : listeners) {
                        listener.userCapsNodeAdded(user, nodeVer, online);
                    }
                }
            }
        }
    }

    public void addUserCapsNodeListener(UserCapsNodeListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener");
        }
        synchronized (this.userCapsNodeListeners) {
            if (!this.userCapsNodeListeners.contains(listener)) {
                this.userCapsNodeListeners.add(listener);
            }
        }
    }

    public void removeContactCapsNode(Contact contact) {
        Caps caps = null;
        String lastRemovedJid = null;
        Iterator<String> iter = this.userCaps.keySet().iterator();
        while (iter.hasNext()) {
            String jid = (String) iter.next();
            if (StringUtils.parseBareAddress(jid).equals(contact.getAddress())) {
                caps = (Caps) this.userCaps.get(jid);
                lastRemovedJid = jid;
                iter.remove();
            }
        }
        if (caps != null) {
            UserCapsNodeListener[] listeners;
            synchronized (this.userCapsNodeListeners) {
                listeners = (UserCapsNodeListener[]) this.userCapsNodeListeners.toArray(NO_USER_CAPS_NODE_LISTENERS);
            }
            if (listeners.length != 0) {
                String nodeVer = caps.getNodeVer();
                for (UserCapsNodeListener listener : listeners) {
                    listener.userCapsNodeRemoved(lastRemovedJid, nodeVer, false);
                }
            }
        }
    }

    public void removeUserCapsNode(String user) {
        Caps caps = (Caps) this.userCaps.remove(user);
        if (caps != null) {
            UserCapsNodeListener[] listeners;
            synchronized (this.userCapsNodeListeners) {
                listeners = (UserCapsNodeListener[]) this.userCapsNodeListeners.toArray(NO_USER_CAPS_NODE_LISTENERS);
            }
            if (listeners.length != 0) {
                String nodeVer = caps.getNodeVer();
                for (UserCapsNodeListener listener : listeners) {
                    listener.userCapsNodeRemoved(user, nodeVer, false);
                }
            }
        }
    }

    public void removeUserCapsNodeListener(UserCapsNodeListener listener) {
        if (listener != null) {
            synchronized (this.userCapsNodeListeners) {
                this.userCapsNodeListeners.remove(listener);
            }
        }
    }

    public Caps getCapsByUser(String user) {
        return (Caps) this.userCaps.get(user);
    }

    public DiscoverInfo getDiscoverInfoByUser(String user) {
        Caps caps = (Caps) this.userCaps.get(user);
        return caps == null ? null : getDiscoverInfoByCaps(caps);
    }

    public String getCapsVersion() {
        return this.currentCapsVersion;
    }

    public String getNode() {
        return entityNode;
    }

    public void setNode(String node) {
        entityNode = node;
    }

    public static DiscoverInfo getDiscoverInfoByCaps(Caps caps) {
        DiscoverInfo discoverInfo;
        synchronized (caps2discoverInfo) {
            discoverInfo = (DiscoverInfo) caps2discoverInfo.get(caps);
            if (discoverInfo == null) {
                ConfigurationService configurationService = JabberActivator.getConfigurationService();
                String capsPropertyName = getCapsPropertyName(caps);
                String xml = configurationService.getString(capsPropertyName);
                if (!(xml == null || xml.length() == 0)) {
                    IQProvider discoverInfoProvider = (IQProvider) ProviderManager.getInstance().getIQProvider(JingleInfoQueryIQ.ELEMENT_NAME, "http://jabber.org/protocol/disco#info");
                    if (discoverInfoProvider != null) {
                        XmlPullParser parser = new MXParser();
                        try {
                            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
                            parser.setInput(new StringReader(xml));
                            parser.next();
                        } catch (XmlPullParserException e) {
                            parser = null;
                        } catch (IOException e2) {
                            parser = null;
                        }
                        if (parser != null) {
                            try {
                                discoverInfo = (DiscoverInfo) discoverInfoProvider.parseIQ(parser);
                            } catch (Exception e3) {
                            }
                            if (discoverInfo != null) {
                                if (caps.isValid(discoverInfo)) {
                                    caps2discoverInfo.put(caps, discoverInfo);
                                } else {
                                    logger.error("Invalid DiscoverInfo for " + caps.getNodeVer() + ": " + discoverInfo);
                                    configurationService.removeProperty(capsPropertyName);
                                }
                            }
                        }
                    }
                }
            }
        }
        return discoverInfo;
    }

    private static void cleanupDiscoverInfo(DiscoverInfo info) {
        info.setFrom(null);
        info.setTo(null);
        info.setPacketID(null);
    }

    private static Iterator<Feature> getDiscoverInfoFeatures(DiscoverInfo discoverInfo) {
        try {
            Method getFeaturesMethod = DiscoverInfo.class.getDeclaredMethod("getFeatures", new Class[0]);
            getFeaturesMethod.setAccessible(true);
            try {
                return (Iterator) getFeaturesMethod.invoke(discoverInfo, new Object[0]);
            } catch (IllegalAccessException iaex) {
                throw new UndeclaredThrowableException(iaex);
            } catch (InvocationTargetException itex) {
                throw new UndeclaredThrowableException(itex);
            }
        } catch (NoSuchMethodException nsmex) {
            throw new UndeclaredThrowableException(nsmex);
        }
    }

    public void addPacketListener(XMPPConnection connection) {
        connection.addPacketListener(new CapsPacketListener(), new AndFilter(new PacketTypeFilter(Presence.class), new PacketExtensionFilter(CapsPacketExtension.ELEMENT_NAME, CapsPacketExtension.NAMESPACE)));
    }

    /* JADX WARNING: Missing block: B:15:?, code skipped:
            return;
     */
    public void addCapsVerListener(net.java.sip.communicator.impl.protocol.jabber.extensions.caps.CapsVerListener r3) {
        /*
        r2 = this;
        r1 = r2.capsVerListeners;
        monitor-enter(r1);
        r0 = r2.capsVerListeners;	 Catch:{ all -> 0x001d }
        r0 = r0.contains(r3);	 Catch:{ all -> 0x001d }
        if (r0 == 0) goto L_0x000d;
    L_0x000b:
        monitor-exit(r1);	 Catch:{ all -> 0x001d }
    L_0x000c:
        return;
    L_0x000d:
        r0 = r2.capsVerListeners;	 Catch:{ all -> 0x001d }
        r0.add(r3);	 Catch:{ all -> 0x001d }
        r0 = r2.currentCapsVersion;	 Catch:{ all -> 0x001d }
        if (r0 == 0) goto L_0x001b;
    L_0x0016:
        r0 = r2.currentCapsVersion;	 Catch:{ all -> 0x001d }
        r3.capsVerUpdated(r0);	 Catch:{ all -> 0x001d }
    L_0x001b:
        monitor-exit(r1);	 Catch:{ all -> 0x001d }
        goto L_0x000c;
    L_0x001d:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x001d }
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.protocol.jabber.extensions.caps.EntityCapsManager.addCapsVerListener(net.java.sip.communicator.impl.protocol.jabber.extensions.caps.CapsVerListener):void");
    }

    public void removeCapsVerListener(CapsVerListener listener) {
        synchronized (this.capsVerListeners) {
            this.capsVerListeners.remove(listener);
        }
    }

    /* JADX WARNING: Missing block: B:6:0x000c, code skipped:
            r0 = r3.iterator();
     */
    /* JADX WARNING: Missing block: B:8:0x0014, code skipped:
            if (r0.hasNext() == false) goto L_0x0025;
     */
    /* JADX WARNING: Missing block: B:9:0x0016, code skipped:
            ((net.java.sip.communicator.impl.protocol.jabber.extensions.caps.CapsVerListener) r0.next()).capsVerUpdated(r6.currentCapsVersion);
     */
    /* JADX WARNING: Missing block: B:14:0x0025, code skipped:
            return;
     */
    private void fireCapsVerChanged() {
        /*
        r6 = this;
        r2 = 0;
        r5 = r6.capsVerListeners;
        monitor-enter(r5);
        r3 = new java.util.ArrayList;	 Catch:{ all -> 0x0022 }
        r4 = r6.capsVerListeners;	 Catch:{ all -> 0x0022 }
        r3.<init>(r4);	 Catch:{ all -> 0x0022 }
        monitor-exit(r5);	 Catch:{ all -> 0x0026 }
        r0 = r3.iterator();
    L_0x0010:
        r4 = r0.hasNext();
        if (r4 == 0) goto L_0x0025;
    L_0x0016:
        r1 = r0.next();
        r1 = (net.java.sip.communicator.impl.protocol.jabber.extensions.caps.CapsVerListener) r1;
        r4 = r6.currentCapsVersion;
        r1.capsVerUpdated(r4);
        goto L_0x0010;
    L_0x0022:
        r4 = move-exception;
    L_0x0023:
        monitor-exit(r5);	 Catch:{ all -> 0x0022 }
        throw r4;
    L_0x0025:
        return;
    L_0x0026:
        r4 = move-exception;
        r2 = r3;
        goto L_0x0023;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.protocol.jabber.extensions.caps.EntityCapsManager.fireCapsVerChanged():void");
    }

    /* access modifiers changed from: private|static */
    public static String capsToHash(String hashAlgorithm, String capsString) {
        try {
            return Base64.encodeBytes(MessageDigest.getInstance(hashAlgorithm).digest(capsString.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            logger.error("Unsupported XEP-0115: Entity Capabilities hash algorithm: " + hashAlgorithm);
            return null;
        }
    }

    private static void formFieldValuesToCaps(Iterator<String> ffValuesIter, StringBuilder capsBldr) {
        SortedSet<String> fvs = new TreeSet();
        while (ffValuesIter.hasNext()) {
            fvs.add(ffValuesIter.next());
        }
        for (String fv : fvs) {
            capsBldr.append(fv).append('<');
        }
    }

    /* access modifiers changed from: private|static */
    public static String calculateEntityCapsString(DiscoverInfo discoverInfo) {
        StringBuilder bldr = new StringBuilder();
        Iterator<Identity> identities = discoverInfo.getIdentities();
        SortedSet<Identity> is = new TreeSet(new Comparator<Identity>() {
            public int compare(Identity i1, Identity i2) {
                int category = i1.getCategory().compareTo(i2.getCategory());
                if (category != 0) {
                    return category;
                }
                int type = i1.getType().compareTo(i2.getType());
                if (type != 0) {
                }
                return type;
            }
        });
        if (identities != null) {
            while (identities.hasNext()) {
                is.add(identities.next());
            }
        }
        for (Identity i : is) {
            bldr.append(i.getCategory()).append('/').append(i.getType()).append("//").append(i.getName()).append('<');
        }
        Iterator<Feature> features = getDiscoverInfoFeatures(discoverInfo);
        SortedSet<String> fs = new TreeSet();
        if (features != null) {
            while (features.hasNext()) {
                fs.add(((Feature) features.next()).getVar());
            }
        }
        for (String f : fs) {
            bldr.append(f).append('<');
        }
        DataForm extendedInfo = (DataForm) discoverInfo.getExtension("x", "jabber:x:data");
        if (extendedInfo != null) {
            synchronized (extendedInfo) {
                FormField f2;
                SortedSet<FormField> fs2 = new TreeSet(new Comparator<FormField>() {
                    public int compare(FormField f1, FormField f2) {
                        return f1.getVariable().compareTo(f2.getVariable());
                    }
                });
                FormField formType = null;
                Iterator<FormField> fieldsIter = extendedInfo.getFields();
                while (fieldsIter.hasNext()) {
                    f2 = (FormField) fieldsIter.next();
                    if (f2.getVariable().equals("FORM_TYPE")) {
                        formType = f2;
                    } else {
                        fs2.add(f2);
                    }
                }
                if (formType != null) {
                    formFieldValuesToCaps(formType.getValues(), bldr);
                }
                for (FormField f22 : fs2) {
                    bldr.append(f22.getVariable()).append('<');
                    formFieldValuesToCaps(f22.getValues(), bldr);
                }
            }
        }
        return bldr.toString();
    }

    public void calculateEntityCapsVersion(DiscoverInfo discoverInfo) {
        setCurrentCapsVersion(discoverInfo, capsToHash(CapsPacketExtension.HASH_METHOD, calculateEntityCapsString(discoverInfo)));
    }

    public void setCurrentCapsVersion(DiscoverInfo discoverInfo, String capsVersion) {
        Caps caps = new Caps(getNode(), CapsPacketExtension.HASH_METHOD, capsVersion, null);
        discoverInfo.setNode(caps.getNodeVer());
        if (caps.isValid(discoverInfo)) {
            this.currentCapsVersion = capsVersion;
            addDiscoverInfoByCaps(caps, discoverInfo);
            fireCapsVerChanged();
            return;
        }
        throw new IllegalArgumentException("The specified discoverInfo must be valid with respect to the specified capsVersion");
    }
}
