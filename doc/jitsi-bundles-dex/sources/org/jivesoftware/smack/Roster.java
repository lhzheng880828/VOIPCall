package org.jivesoftware.smack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smack.packet.RosterPacket.Item;
import org.jivesoftware.smack.packet.RosterPacket.ItemType;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.util.StringUtils;

public class Roster {
    private static SubscriptionMode defaultSubscriptionMode = SubscriptionMode.accept_all;
    /* access modifiers changed from: private */
    public Connection connection;
    /* access modifiers changed from: private|final */
    public final Map<String, RosterEntry> entries;
    /* access modifiers changed from: private|final */
    public final Map<String, RosterGroup> groups;
    /* access modifiers changed from: private */
    public Map<String, Map<String, Presence>> presenceMap;
    private PresencePacketListener presencePacketListener;
    boolean rosterInitialized = false;
    private final List<RosterListener> rosterListeners;
    /* access modifiers changed from: private */
    public SubscriptionMode subscriptionMode = getDefaultSubscriptionMode();
    /* access modifiers changed from: private|final */
    public final List<RosterEntry> unfiledEntries;

    private class PresencePacketListener implements PacketListener {
        private PresencePacketListener() {
        }

        public void processPacket(Packet packet) {
            Presence presence = (Presence) packet;
            String from = presence.getFrom();
            String key = Roster.this.getPresenceMapKey(from);
            Map<String, Presence> userPresences;
            Presence response;
            if (presence.getType() == Type.available) {
                if (Roster.this.presenceMap.get(key) == null) {
                    userPresences = new ConcurrentHashMap();
                    Roster.this.presenceMap.put(key, userPresences);
                } else {
                    userPresences = (Map) Roster.this.presenceMap.get(key);
                }
                userPresences.remove("");
                userPresences.put(StringUtils.parseResource(from), presence);
                if (((RosterEntry) Roster.this.entries.get(key)) != null) {
                    Roster.this.fireRosterPresenceEvent(presence);
                }
            } else if (presence.getType() == Type.unavailable) {
                if ("".equals(StringUtils.parseResource(from))) {
                    if (Roster.this.presenceMap.get(key) == null) {
                        userPresences = new ConcurrentHashMap();
                        Roster.this.presenceMap.put(key, userPresences);
                    } else {
                        userPresences = (Map) Roster.this.presenceMap.get(key);
                    }
                    userPresences.put("", presence);
                } else if (Roster.this.presenceMap.get(key) != null) {
                    ((Map) Roster.this.presenceMap.get(key)).put(StringUtils.parseResource(from), presence);
                }
                if (((RosterEntry) Roster.this.entries.get(key)) != null) {
                    Roster.this.fireRosterPresenceEvent(presence);
                }
            } else if (presence.getType() == Type.subscribe) {
                if (Roster.this.subscriptionMode == SubscriptionMode.accept_all) {
                    response = new Presence(Type.subscribed);
                    response.setTo(presence.getFrom());
                    Roster.this.connection.sendPacket(response);
                } else if (Roster.this.subscriptionMode == SubscriptionMode.reject_all) {
                    response = new Presence(Type.unsubscribed);
                    response.setTo(presence.getFrom());
                    Roster.this.connection.sendPacket(response);
                }
            } else if (presence.getType() == Type.unsubscribe) {
                if (Roster.this.subscriptionMode != SubscriptionMode.manual) {
                    response = new Presence(Type.unsubscribed);
                    response.setTo(presence.getFrom());
                    Roster.this.connection.sendPacket(response);
                }
            } else if (presence.getType() == Type.error && "".equals(StringUtils.parseResource(from))) {
                if (Roster.this.presenceMap.containsKey(key)) {
                    userPresences = (Map) Roster.this.presenceMap.get(key);
                    userPresences.clear();
                } else {
                    userPresences = new ConcurrentHashMap();
                    Roster.this.presenceMap.put(key, userPresences);
                }
                userPresences.put("", presence);
                if (((RosterEntry) Roster.this.entries.get(key)) != null) {
                    Roster.this.fireRosterPresenceEvent(presence);
                }
            }
        }
    }

    private class RosterPacketListener implements PacketListener {
        private RosterPacketListener() {
        }

        public void processPacket(Packet packet) {
            if (packet.getError() != null) {
                Roster.this.fireRosterChangedEvent(packet.getError(), packet);
                return;
            }
            Collection<String> addedEntries = new ArrayList();
            Collection<String> updatedEntries = new ArrayList();
            Collection<String> deletedEntries = new ArrayList();
            for (Item item : ((RosterPacket) packet).getRosterItems()) {
                RosterGroup group;
                RosterEntry entry = new RosterEntry(item.getUser(), item.getName(), item.getItemType(), item.getItemStatus(), Roster.this, Roster.this.connection);
                if (ItemType.remove.equals(item.getItemType())) {
                    if (Roster.this.entries.containsKey(item.getUser())) {
                        Roster.this.entries.remove(item.getUser());
                    }
                    if (Roster.this.unfiledEntries.contains(entry)) {
                        Roster.this.unfiledEntries.remove(entry);
                    }
                    Roster.this.presenceMap.remove(StringUtils.parseName(item.getUser()) + Separators.AT + StringUtils.parseServer(item.getUser()));
                    deletedEntries.add(item.getUser());
                } else {
                    if (Roster.this.entries.containsKey(item.getUser())) {
                        RosterEntry oldEntry = (RosterEntry) Roster.this.entries.put(item.getUser(), entry);
                        Item oldItem = RosterEntry.toRosterItem(oldEntry);
                        if (!(oldEntry != null && oldEntry.equalsDeep(entry) && item.getGroupNames().equals(oldItem.getGroupNames()))) {
                            updatedEntries.add(item.getUser());
                        }
                    } else {
                        Roster.this.entries.put(item.getUser(), entry);
                        addedEntries.add(item.getUser());
                    }
                    if (!item.getGroupNames().isEmpty()) {
                        Roster.this.unfiledEntries.remove(entry);
                    } else if (!Roster.this.unfiledEntries.contains(entry)) {
                        Roster.this.unfiledEntries.add(entry);
                    }
                }
                List<String> currentGroupNames = new ArrayList();
                for (RosterGroup group2 : Roster.this.getGroups()) {
                    if (group2.contains(entry)) {
                        currentGroupNames.add(group2.getName());
                    }
                }
                if (!ItemType.remove.equals(item.getItemType())) {
                    List<String> newGroupNames = new ArrayList();
                    for (String groupName : item.getGroupNames()) {
                        newGroupNames.add(groupName);
                        group2 = Roster.this.getGroup(groupName);
                        if (group2 == null) {
                            group2 = Roster.this.createGroup(groupName);
                            Roster.this.groups.put(groupName, group2);
                        }
                        group2.addEntryLocal(entry);
                    }
                    for (String newGroupName : newGroupNames) {
                        currentGroupNames.remove(newGroupName);
                    }
                }
                for (String groupName2 : currentGroupNames) {
                    group2 = Roster.this.getGroup(groupName2);
                    group2.removeEntryLocal(entry);
                    if (group2.getEntryCount() == 0) {
                        Roster.this.groups.remove(groupName2);
                    }
                }
                for (RosterGroup group22 : Roster.this.getGroups()) {
                    if (group22.getEntryCount() == 0) {
                        Roster.this.groups.remove(group22.getName());
                    }
                }
            }
            synchronized (Roster.this) {
                Roster.this.rosterInitialized = true;
                Roster.this.notifyAll();
            }
            Roster.this.fireRosterChangedEvent(addedEntries, updatedEntries, deletedEntries);
        }
    }

    public enum SubscriptionMode {
        accept_all,
        reject_all,
        manual
    }

    public static SubscriptionMode getDefaultSubscriptionMode() {
        return defaultSubscriptionMode;
    }

    public static void setDefaultSubscriptionMode(SubscriptionMode subscriptionMode) {
        defaultSubscriptionMode = subscriptionMode;
    }

    Roster(Connection connection) {
        this.connection = connection;
        this.groups = new ConcurrentHashMap();
        this.unfiledEntries = new CopyOnWriteArrayList();
        this.entries = new ConcurrentHashMap();
        this.rosterListeners = new CopyOnWriteArrayList();
        this.presenceMap = new ConcurrentHashMap();
        connection.addPacketListener(new RosterPacketListener(), new PacketTypeFilter(RosterPacket.class));
        PacketFilter presenceFilter = new PacketTypeFilter(Presence.class);
        this.presencePacketListener = new PresencePacketListener();
        connection.addPacketListener(this.presencePacketListener, presenceFilter);
        final ConnectionListener connectionListener = new AbstractConnectionListener() {
            public void connectionClosed() {
                Roster.this.setOfflinePresences();
            }

            public void connectionClosedOnError(Exception e) {
                Roster.this.setOfflinePresences();
            }
        };
        if (this.connection.isConnected()) {
            connection.addConnectionListener(connectionListener);
        } else {
            Connection.addConnectionCreationListener(new ConnectionCreationListener() {
                public void connectionCreated(Connection connection) {
                    if (connection.equals(Roster.this.connection)) {
                        Roster.this.connection.addConnectionListener(connectionListener);
                    }
                }
            });
        }
    }

    public SubscriptionMode getSubscriptionMode() {
        return this.subscriptionMode;
    }

    public void setSubscriptionMode(SubscriptionMode subscriptionMode) {
        this.subscriptionMode = subscriptionMode;
    }

    public void reload() {
        if (!this.connection.isAuthenticated()) {
            throw new IllegalStateException("Not logged in to server.");
        } else if (this.connection.isAnonymous()) {
            throw new IllegalStateException("Anonymous users can't have a roster.");
        } else {
            this.connection.sendPacket(new RosterPacket());
        }
    }

    public void addRosterListener(RosterListener rosterListener) {
        if (!this.rosterListeners.contains(rosterListener)) {
            this.rosterListeners.add(rosterListener);
        }
    }

    public void removeRosterListener(RosterListener rosterListener) {
        this.rosterListeners.remove(rosterListener);
    }

    public RosterGroup createGroup(String name) {
        if (!this.connection.isAuthenticated()) {
            throw new IllegalStateException("Not logged in to server.");
        } else if (this.connection.isAnonymous()) {
            throw new IllegalStateException("Anonymous users can't have a roster.");
        } else if (this.groups.containsKey(name)) {
            throw new IllegalArgumentException("Group with name " + name + " alread exists.");
        } else {
            RosterGroup group = new RosterGroup(name, this.connection);
            this.groups.put(name, group);
            return group;
        }
    }

    public void createEntry(String user, String name, String[] groups) throws XMPPException {
        if (!this.connection.isAuthenticated()) {
            throw new IllegalStateException("Not logged in to server.");
        } else if (this.connection.isAnonymous()) {
            throw new IllegalStateException("Anonymous users can't have a roster.");
        } else {
            RosterPacket rosterPacket = new RosterPacket();
            rosterPacket.setType(IQ.Type.SET);
            Item item = new Item(user, name);
            if (groups != null) {
                for (String group : groups) {
                    if (group != null && group.trim().length() > 0) {
                        item.addGroupName(group);
                    }
                }
            }
            rosterPacket.addRosterItem(item);
            PacketCollector collector = this.connection.createPacketCollector(new PacketIDFilter(rosterPacket.getPacketID()));
            this.connection.sendPacket(rosterPacket);
            IQ response = (IQ) collector.nextResult((long) SmackConfiguration.getPacketReplyTimeout());
            collector.cancel();
            if (response == null) {
                throw new XMPPException("No response from the server.");
            } else if (response.getType() == IQ.Type.ERROR) {
                throw new XMPPException(response.getError());
            } else {
                Presence presencePacket = new Presence(Type.subscribe);
                presencePacket.setTo(user);
                this.connection.sendPacket(presencePacket);
            }
        }
    }

    public void removeEntry(RosterEntry entry) throws XMPPException {
        if (!this.connection.isAuthenticated()) {
            throw new IllegalStateException("Not logged in to server.");
        } else if (this.connection.isAnonymous()) {
            throw new IllegalStateException("Anonymous users can't have a roster.");
        } else if (this.entries.containsKey(entry.getUser())) {
            RosterPacket packet = new RosterPacket();
            packet.setType(IQ.Type.SET);
            Item item = RosterEntry.toRosterItem(entry);
            item.setItemType(ItemType.remove);
            packet.addRosterItem(item);
            PacketCollector collector = this.connection.createPacketCollector(new PacketIDFilter(packet.getPacketID()));
            this.connection.sendPacket(packet);
            IQ response = (IQ) collector.nextResult((long) SmackConfiguration.getPacketReplyTimeout());
            collector.cancel();
            if (response == null) {
                throw new XMPPException("No response from the server.");
            } else if (response.getType() == IQ.Type.ERROR) {
                throw new XMPPException(response.getError());
            }
        }
    }

    public int getEntryCount() {
        return getEntries().size();
    }

    public Collection<RosterEntry> getEntries() {
        Set<RosterEntry> allEntries = new HashSet();
        for (RosterGroup rosterGroup : getGroups()) {
            allEntries.addAll(rosterGroup.getEntries());
        }
        allEntries.addAll(this.unfiledEntries);
        return Collections.unmodifiableCollection(allEntries);
    }

    public int getUnfiledEntryCount() {
        return this.unfiledEntries.size();
    }

    public Collection<RosterEntry> getUnfiledEntries() {
        return Collections.unmodifiableList(this.unfiledEntries);
    }

    public RosterEntry getEntry(String user) {
        if (user == null) {
            return null;
        }
        return (RosterEntry) this.entries.get(user.toLowerCase());
    }

    public boolean contains(String user) {
        return getEntry(user) != null;
    }

    public RosterGroup getGroup(String name) {
        return (RosterGroup) this.groups.get(name);
    }

    public int getGroupCount() {
        return this.groups.size();
    }

    public Collection<RosterGroup> getGroups() {
        return Collections.unmodifiableCollection(this.groups.values());
    }

    public Presence getPresence(String user) {
        Map<String, Presence> userPresences = (Map) this.presenceMap.get(getPresenceMapKey(StringUtils.parseBareAddress(user)));
        Presence presence;
        if (userPresences == null) {
            presence = new Presence(Type.unavailable);
            presence.setFrom(user);
            return presence;
        }
        presence = null;
        for (String resource : userPresences.keySet()) {
            Presence p = (Presence) userPresences.get(resource);
            if (p.isAvailable()) {
                if (presence == null || p.getPriority() > presence.getPriority()) {
                    presence = p;
                } else if (p.getPriority() == presence.getPriority()) {
                    Mode pMode = p.getMode();
                    if (pMode == null) {
                        pMode = Mode.available;
                    }
                    Mode presenceMode = presence.getMode();
                    if (presenceMode == null) {
                        presenceMode = Mode.available;
                    }
                    if (pMode.compareTo(presenceMode) < 0) {
                        presence = p;
                    }
                }
            }
        }
        if (presence != null) {
            return presence;
        }
        presence = new Presence(Type.unavailable);
        presence.setFrom(user);
        return presence;
    }

    public Presence getPresenceResource(String userWithResource) {
        String key = getPresenceMapKey(userWithResource);
        String resource = StringUtils.parseResource(userWithResource);
        Map<String, Presence> userPresences = (Map) this.presenceMap.get(key);
        Presence presence;
        if (userPresences == null) {
            presence = new Presence(Type.unavailable);
            presence.setFrom(userWithResource);
            return presence;
        }
        presence = (Presence) userPresences.get(resource);
        if (presence != null) {
            return presence;
        }
        presence = new Presence(Type.unavailable);
        presence.setFrom(userWithResource);
        return presence;
    }

    public Iterator<Presence> getPresences(String user) {
        Map<String, Presence> userPresences = (Map) this.presenceMap.get(getPresenceMapKey(user));
        if (userPresences == null) {
            new Presence(Type.unavailable).setFrom(user);
            return Arrays.asList(new Presence[]{presence}).iterator();
        }
        Collection<Presence> answer = new ArrayList();
        for (Presence presence : userPresences.values()) {
            if (presence.isAvailable()) {
                answer.add(presence);
            }
        }
        if (!answer.isEmpty()) {
            return answer.iterator();
        }
        new Presence(Type.unavailable).setFrom(user);
        return Arrays.asList(new Presence[]{presence}).iterator();
    }

    /* access modifiers changed from: 0000 */
    public void cleanup() {
        this.rosterListeners.clear();
    }

    /* access modifiers changed from: private */
    public String getPresenceMapKey(String user) {
        if (user == null) {
            return null;
        }
        String key = user;
        if (!contains(user)) {
            key = StringUtils.parseBareAddress(user);
        }
        return key.toLowerCase();
    }

    /* access modifiers changed from: private */
    public void setOfflinePresences() {
        for (String user : this.presenceMap.keySet()) {
            Map<String, Presence> resources = (Map) this.presenceMap.get(user);
            if (resources != null) {
                for (String resource : resources.keySet()) {
                    Presence packetUnavailable = new Presence(Type.unavailable);
                    packetUnavailable.setFrom(user + Separators.SLASH + resource);
                    this.presencePacketListener.processPacket(packetUnavailable);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void fireRosterChangedEvent(Collection<String> addedEntries, Collection<String> updatedEntries, Collection<String> deletedEntries) {
        for (RosterListener listener : this.rosterListeners) {
            if (!addedEntries.isEmpty()) {
                listener.entriesAdded(addedEntries);
            }
            if (!updatedEntries.isEmpty()) {
                listener.entriesUpdated(updatedEntries);
            }
            if (!deletedEntries.isEmpty()) {
                listener.entriesDeleted(deletedEntries);
            }
        }
    }

    /* access modifiers changed from: private */
    public void fireRosterChangedEvent(XMPPError error, Packet packet) {
        for (RosterListener listener : this.rosterListeners) {
            listener.rosterError(error, packet);
        }
    }

    /* access modifiers changed from: private */
    public void fireRosterPresenceEvent(Presence presence) {
        for (RosterListener listener : this.rosterListeners) {
            listener.presenceChanged(presence);
        }
    }
}
