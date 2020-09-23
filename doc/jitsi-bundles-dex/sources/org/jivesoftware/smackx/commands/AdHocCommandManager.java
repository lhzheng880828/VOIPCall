package org.jivesoftware.smackx.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.packet.XMPPError.Condition;
import org.jivesoftware.smackx.NodeInformationProvider;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.commands.AdHocCommand.SpecificErrorCondition;
import org.jivesoftware.smackx.packet.AdHocCommandData;
import org.jivesoftware.smackx.packet.AdHocCommandData.SpecificError;
import org.jivesoftware.smackx.packet.DiscoverInfo.Identity;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.jivesoftware.smackx.packet.DiscoverItems.Item;

public class AdHocCommandManager {
    private static final String DISCO_NAMESPACE = "http://jabber.org/protocol/commands";
    private static final int SESSION_TIMEOUT = 120;
    private static final String discoNode = "http://jabber.org/protocol/commands";
    /* access modifiers changed from: private|static */
    public static Map<Connection, AdHocCommandManager> instances = new ConcurrentHashMap();
    private Map<String, AdHocCommandInfo> commands;
    /* access modifiers changed from: private */
    public Connection connection;
    /* access modifiers changed from: private */
    public Map<String, LocalCommand> executingCommands;
    private Thread sessionsSweeper;

    private static class AdHocCommandInfo {
        private LocalCommandFactory factory;
        private String name;
        private String node;
        private String ownerJID;

        public AdHocCommandInfo(String node, String name, String ownerJID, LocalCommandFactory factory) {
            this.node = node;
            this.name = name;
            this.ownerJID = ownerJID;
            this.factory = factory;
        }

        public LocalCommand getCommandInstance() throws InstantiationException, IllegalAccessException {
            return this.factory.getInstance();
        }

        public String getName() {
            return this.name;
        }

        public String getNode() {
            return this.node;
        }

        public String getOwnerJID() {
            return this.ownerJID;
        }
    }

    static {
        Connection.addConnectionCreationListener(new ConnectionCreationListener() {
            public void connectionCreated(Connection connection) {
                AdHocCommandManager adHocCommandManager = new AdHocCommandManager(connection);
            }
        });
    }

    public static AdHocCommandManager getAddHocCommandsManager(Connection connection) {
        return (AdHocCommandManager) instances.get(connection);
    }

    private AdHocCommandManager(Connection connection) {
        this.commands = new ConcurrentHashMap();
        this.executingCommands = new ConcurrentHashMap();
        this.connection = connection;
        init();
    }

    public void registerCommand(String node, String name, final Class clazz) {
        registerCommand(node, name, new LocalCommandFactory() {
            public LocalCommand getInstance() throws InstantiationException, IllegalAccessException {
                return (LocalCommand) clazz.newInstance();
            }
        });
    }

    public void registerCommand(String node, final String name, LocalCommandFactory factory) {
        this.commands.put(node, new AdHocCommandInfo(node, name, this.connection.getUser(), factory));
        ServiceDiscoveryManager.getInstanceFor(this.connection).setNodeInformationProvider(node, new NodeInformationProvider() {
            public List<Item> getNodeItems() {
                return null;
            }

            public List<String> getNodeFeatures() {
                List<String> answer = new ArrayList();
                answer.add(SpecificError.namespace);
                answer.add("jabber:x:data");
                return answer;
            }

            public List<Identity> getNodeIdentities() {
                List<Identity> answer = new ArrayList();
                Identity identity = new Identity("automation", name);
                identity.setType("command-node");
                answer.add(identity);
                return answer;
            }
        });
    }

    public DiscoverItems discoverCommands(String jid) throws XMPPException {
        return ServiceDiscoveryManager.getInstanceFor(this.connection).discoverItems(jid, SpecificError.namespace);
    }

    public void publishCommands(String jid) throws XMPPException {
        ServiceDiscoveryManager serviceDiscoveryManager = ServiceDiscoveryManager.getInstanceFor(this.connection);
        DiscoverItems discoverItems = new DiscoverItems();
        for (AdHocCommandInfo info : getRegisteredCommands()) {
            Item item = new Item(info.getOwnerJID());
            item.setName(info.getName());
            item.setNode(info.getNode());
            discoverItems.addItem(item);
        }
        serviceDiscoveryManager.publishItems(jid, SpecificError.namespace, discoverItems);
    }

    public RemoteCommand getRemoteCommand(String jid, String node) {
        return new RemoteCommand(this.connection, node, jid);
    }

    private void init() {
        instances.put(this.connection, this);
        this.connection.addConnectionListener(new ConnectionListener() {
            public void connectionClosed() {
                AdHocCommandManager.instances.remove(AdHocCommandManager.this.connection);
            }

            public void connectionClosedOnError(Exception e) {
                AdHocCommandManager.instances.remove(AdHocCommandManager.this.connection);
            }

            public void reconnectionSuccessful() {
                AdHocCommandManager.instances.put(AdHocCommandManager.this.connection, AdHocCommandManager.this);
            }

            public void reconnectingIn(int seconds) {
            }

            public void reconnectionFailed(Exception e) {
            }
        });
        ServiceDiscoveryManager.getInstanceFor(this.connection).addFeature(SpecificError.namespace);
        ServiceDiscoveryManager.getInstanceFor(this.connection).setNodeInformationProvider(SpecificError.namespace, new NodeInformationProvider() {
            public List<Item> getNodeItems() {
                List<Item> answer = new ArrayList();
                for (AdHocCommandInfo info : AdHocCommandManager.this.getRegisteredCommands()) {
                    Item item = new Item(info.getOwnerJID());
                    item.setName(info.getName());
                    item.setNode(info.getNode());
                    answer.add(item);
                }
                return answer;
            }

            public List<String> getNodeFeatures() {
                return null;
            }

            public List<Identity> getNodeIdentities() {
                return null;
            }
        });
        this.connection.addPacketListener(new PacketListener() {
            public void processPacket(Packet packet) {
                AdHocCommandManager.this.processAdHocCommand((AdHocCommandData) packet);
            }
        }, new PacketTypeFilter(AdHocCommandData.class));
        this.sessionsSweeper = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    for (String sessionId : AdHocCommandManager.this.executingCommands.keySet()) {
                        LocalCommand command = (LocalCommand) AdHocCommandManager.this.executingCommands.get(sessionId);
                        if (command != null) {
                            if (System.currentTimeMillis() - command.getCreationDate() > 240000) {
                                AdHocCommandManager.this.executingCommands.remove(sessionId);
                            }
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        });
        this.sessionsSweeper.setDaemon(true);
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Missing block: B:55:0x0135, code skipped:
            if (org.jivesoftware.smackx.commands.AdHocCommand.Action.execute.equals(r0) != false) goto L_0x0137;
     */
    public void processAdHocCommand(org.jivesoftware.smackx.packet.AdHocCommandData r15) {
        /*
        r14 = this;
        r9 = r15.getType();
        r10 = org.jivesoftware.smack.packet.IQ.Type.SET;
        if (r9 == r10) goto L_0x0009;
    L_0x0008:
        return;
    L_0x0009:
        r7 = new org.jivesoftware.smackx.packet.AdHocCommandData;
        r7.m2185init();
        r9 = r15.getFrom();
        r7.setTo(r9);
        r9 = r15.getPacketID();
        r7.setPacketID(r9);
        r9 = r15.getNode();
        r7.setNode(r9);
        r9 = r15.getTo();
        r7.setId(r9);
        r8 = r15.getSessionID();
        r2 = r15.getNode();
        if (r8 != 0) goto L_0x00e0;
    L_0x0034:
        r9 = r14.commands;
        r9 = r9.containsKey(r2);
        if (r9 != 0) goto L_0x0042;
    L_0x003c:
        r9 = org.jivesoftware.smack.packet.XMPPError.Condition.item_not_found;
        r14.respondError(r7, r9);
        goto L_0x0008;
    L_0x0042:
        r9 = 15;
        r8 = org.jivesoftware.smack.util.StringUtils.randomString(r9);
        r1 = r14.newInstanceOfCmd(r2, r8);	 Catch:{ XMPPException -> 0x0064 }
        r9 = org.jivesoftware.smack.packet.IQ.Type.RESULT;	 Catch:{ XMPPException -> 0x0064 }
        r7.setType(r9);	 Catch:{ XMPPException -> 0x0064 }
        r1.setData(r7);	 Catch:{ XMPPException -> 0x0064 }
        r9 = r15.getFrom();	 Catch:{ XMPPException -> 0x0064 }
        r9 = r1.hasPermission(r9);	 Catch:{ XMPPException -> 0x0064 }
        if (r9 != 0) goto L_0x0086;
    L_0x005e:
        r9 = org.jivesoftware.smack.packet.XMPPError.Condition.forbidden;	 Catch:{ XMPPException -> 0x0064 }
        r14.respondError(r7, r9);	 Catch:{ XMPPException -> 0x0064 }
        goto L_0x0008;
    L_0x0064:
        r3 = move-exception;
        r6 = r3.getXMPPError();
        r9 = org.jivesoftware.smack.packet.XMPPError.Type.CANCEL;
        r10 = r6.getType();
        r9 = r9.equals(r10);
        if (r9 == 0) goto L_0x007f;
    L_0x0075:
        r9 = org.jivesoftware.smackx.commands.AdHocCommand.Status.canceled;
        r7.setStatus(r9);
        r9 = r14.executingCommands;
        r9.remove(r8);
    L_0x007f:
        r14.respondError(r7, r6);
        r3.printStackTrace();
        goto L_0x0008;
    L_0x0086:
        r0 = r15.getAction();	 Catch:{ XMPPException -> 0x0064 }
        if (r0 == 0) goto L_0x009d;
    L_0x008c:
        r9 = org.jivesoftware.smackx.commands.AdHocCommand.Action.unknown;	 Catch:{ XMPPException -> 0x0064 }
        r9 = r0.equals(r9);	 Catch:{ XMPPException -> 0x0064 }
        if (r9 == 0) goto L_0x009d;
    L_0x0094:
        r9 = org.jivesoftware.smack.packet.XMPPError.Condition.bad_request;	 Catch:{ XMPPException -> 0x0064 }
        r10 = org.jivesoftware.smackx.commands.AdHocCommand.SpecificErrorCondition.malformedAction;	 Catch:{ XMPPException -> 0x0064 }
        r14.respondError(r7, r9, r10);	 Catch:{ XMPPException -> 0x0064 }
        goto L_0x0008;
    L_0x009d:
        if (r0 == 0) goto L_0x00b0;
    L_0x009f:
        r9 = org.jivesoftware.smackx.commands.AdHocCommand.Action.execute;	 Catch:{ XMPPException -> 0x0064 }
        r9 = r0.equals(r9);	 Catch:{ XMPPException -> 0x0064 }
        if (r9 != 0) goto L_0x00b0;
    L_0x00a7:
        r9 = org.jivesoftware.smack.packet.XMPPError.Condition.bad_request;	 Catch:{ XMPPException -> 0x0064 }
        r10 = org.jivesoftware.smackx.commands.AdHocCommand.SpecificErrorCondition.badAction;	 Catch:{ XMPPException -> 0x0064 }
        r14.respondError(r7, r9, r10);	 Catch:{ XMPPException -> 0x0064 }
        goto L_0x0008;
    L_0x00b0:
        r1.incrementStage();	 Catch:{ XMPPException -> 0x0064 }
        r1.execute();	 Catch:{ XMPPException -> 0x0064 }
        r9 = r1.isLastStage();	 Catch:{ XMPPException -> 0x0064 }
        if (r9 == 0) goto L_0x00c8;
    L_0x00bc:
        r9 = org.jivesoftware.smackx.commands.AdHocCommand.Status.completed;	 Catch:{ XMPPException -> 0x0064 }
        r7.setStatus(r9);	 Catch:{ XMPPException -> 0x0064 }
    L_0x00c1:
        r9 = r14.connection;	 Catch:{ XMPPException -> 0x0064 }
        r9.sendPacket(r7);	 Catch:{ XMPPException -> 0x0064 }
        goto L_0x0008;
    L_0x00c8:
        r9 = org.jivesoftware.smackx.commands.AdHocCommand.Status.executing;	 Catch:{ XMPPException -> 0x0064 }
        r7.setStatus(r9);	 Catch:{ XMPPException -> 0x0064 }
        r9 = r14.executingCommands;	 Catch:{ XMPPException -> 0x0064 }
        r9.put(r8, r1);	 Catch:{ XMPPException -> 0x0064 }
        r9 = r14.sessionsSweeper;	 Catch:{ XMPPException -> 0x0064 }
        r9 = r9.isAlive();	 Catch:{ XMPPException -> 0x0064 }
        if (r9 != 0) goto L_0x00c1;
    L_0x00da:
        r9 = r14.sessionsSweeper;	 Catch:{ XMPPException -> 0x0064 }
        r9.start();	 Catch:{ XMPPException -> 0x0064 }
        goto L_0x00c1;
    L_0x00e0:
        r9 = r14.executingCommands;
        r1 = r9.get(r8);
        r1 = (org.jivesoftware.smackx.commands.LocalCommand) r1;
        if (r1 != 0) goto L_0x00f3;
    L_0x00ea:
        r9 = org.jivesoftware.smack.packet.XMPPError.Condition.bad_request;
        r10 = org.jivesoftware.smackx.commands.AdHocCommand.SpecificErrorCondition.badSessionid;
        r14.respondError(r7, r9, r10);
        goto L_0x0008;
    L_0x00f3:
        r4 = r1.getCreationDate();
        r10 = java.lang.System.currentTimeMillis();
        r10 = r10 - r4;
        r12 = 120000; // 0x1d4c0 float:1.68156E-40 double:5.9288E-319;
        r9 = (r10 > r12 ? 1 : (r10 == r12 ? 0 : -1));
        if (r9 <= 0) goto L_0x0111;
    L_0x0103:
        r9 = r14.executingCommands;
        r9.remove(r8);
        r9 = org.jivesoftware.smack.packet.XMPPError.Condition.not_allowed;
        r10 = org.jivesoftware.smackx.commands.AdHocCommand.SpecificErrorCondition.sessionExpired;
        r14.respondError(r7, r9, r10);
        goto L_0x0008;
    L_0x0111:
        monitor-enter(r1);
        r0 = r15.getAction();	 Catch:{ all -> 0x012a }
        if (r0 == 0) goto L_0x012d;
    L_0x0118:
        r9 = org.jivesoftware.smackx.commands.AdHocCommand.Action.unknown;	 Catch:{ all -> 0x012a }
        r9 = r0.equals(r9);	 Catch:{ all -> 0x012a }
        if (r9 == 0) goto L_0x012d;
    L_0x0120:
        r9 = org.jivesoftware.smack.packet.XMPPError.Condition.bad_request;	 Catch:{ all -> 0x012a }
        r10 = org.jivesoftware.smackx.commands.AdHocCommand.SpecificErrorCondition.malformedAction;	 Catch:{ all -> 0x012a }
        r14.respondError(r7, r9, r10);	 Catch:{ all -> 0x012a }
        monitor-exit(r1);	 Catch:{ all -> 0x012a }
        goto L_0x0008;
    L_0x012a:
        r9 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x012a }
        throw r9;
    L_0x012d:
        if (r0 == 0) goto L_0x0137;
    L_0x012f:
        r9 = org.jivesoftware.smackx.commands.AdHocCommand.Action.execute;	 Catch:{ all -> 0x012a }
        r9 = r9.equals(r0);	 Catch:{ all -> 0x012a }
        if (r9 == 0) goto L_0x013b;
    L_0x0137:
        r0 = r1.getExecuteAction();	 Catch:{ all -> 0x012a }
    L_0x013b:
        r9 = r1.isValidAction(r0);	 Catch:{ all -> 0x012a }
        if (r9 != 0) goto L_0x014b;
    L_0x0141:
        r9 = org.jivesoftware.smack.packet.XMPPError.Condition.bad_request;	 Catch:{ all -> 0x012a }
        r10 = org.jivesoftware.smackx.commands.AdHocCommand.SpecificErrorCondition.badAction;	 Catch:{ all -> 0x012a }
        r14.respondError(r7, r9, r10);	 Catch:{ all -> 0x012a }
        monitor-exit(r1);	 Catch:{ all -> 0x012a }
        goto L_0x0008;
    L_0x014b:
        r9 = org.jivesoftware.smack.packet.IQ.Type.RESULT;	 Catch:{ XMPPException -> 0x0183 }
        r7.setType(r9);	 Catch:{ XMPPException -> 0x0183 }
        r1.setData(r7);	 Catch:{ XMPPException -> 0x0183 }
        r9 = org.jivesoftware.smackx.commands.AdHocCommand.Action.next;	 Catch:{ XMPPException -> 0x0183 }
        r9 = r9.equals(r0);	 Catch:{ XMPPException -> 0x0183 }
        if (r9 == 0) goto L_0x01a5;
    L_0x015b:
        r1.incrementStage();	 Catch:{ XMPPException -> 0x0183 }
        r9 = new org.jivesoftware.smackx.Form;	 Catch:{ XMPPException -> 0x0183 }
        r10 = r15.getForm();	 Catch:{ XMPPException -> 0x0183 }
        r9.m1932init(r10);	 Catch:{ XMPPException -> 0x0183 }
        r1.next(r9);	 Catch:{ XMPPException -> 0x0183 }
        r9 = r1.isLastStage();	 Catch:{ XMPPException -> 0x0183 }
        if (r9 == 0) goto L_0x017d;
    L_0x0170:
        r9 = org.jivesoftware.smackx.commands.AdHocCommand.Status.completed;	 Catch:{ XMPPException -> 0x0183 }
        r7.setStatus(r9);	 Catch:{ XMPPException -> 0x0183 }
    L_0x0175:
        r9 = r14.connection;	 Catch:{ XMPPException -> 0x0183 }
        r9.sendPacket(r7);	 Catch:{ XMPPException -> 0x0183 }
    L_0x017a:
        monitor-exit(r1);	 Catch:{ all -> 0x012a }
        goto L_0x0008;
    L_0x017d:
        r9 = org.jivesoftware.smackx.commands.AdHocCommand.Status.executing;	 Catch:{ XMPPException -> 0x0183 }
        r7.setStatus(r9);	 Catch:{ XMPPException -> 0x0183 }
        goto L_0x0175;
    L_0x0183:
        r3 = move-exception;
        r6 = r3.getXMPPError();	 Catch:{ all -> 0x012a }
        r9 = org.jivesoftware.smack.packet.XMPPError.Type.CANCEL;	 Catch:{ all -> 0x012a }
        r10 = r6.getType();	 Catch:{ all -> 0x012a }
        r9 = r9.equals(r10);	 Catch:{ all -> 0x012a }
        if (r9 == 0) goto L_0x019e;
    L_0x0194:
        r9 = org.jivesoftware.smackx.commands.AdHocCommand.Status.canceled;	 Catch:{ all -> 0x012a }
        r7.setStatus(r9);	 Catch:{ all -> 0x012a }
        r9 = r14.executingCommands;	 Catch:{ all -> 0x012a }
        r9.remove(r8);	 Catch:{ all -> 0x012a }
    L_0x019e:
        r14.respondError(r7, r6);	 Catch:{ all -> 0x012a }
        r3.printStackTrace();	 Catch:{ all -> 0x012a }
        goto L_0x017a;
    L_0x01a5:
        r9 = org.jivesoftware.smackx.commands.AdHocCommand.Action.complete;	 Catch:{ XMPPException -> 0x0183 }
        r9 = r9.equals(r0);	 Catch:{ XMPPException -> 0x0183 }
        if (r9 == 0) goto L_0x01c7;
    L_0x01ad:
        r1.incrementStage();	 Catch:{ XMPPException -> 0x0183 }
        r9 = new org.jivesoftware.smackx.Form;	 Catch:{ XMPPException -> 0x0183 }
        r10 = r15.getForm();	 Catch:{ XMPPException -> 0x0183 }
        r9.m1932init(r10);	 Catch:{ XMPPException -> 0x0183 }
        r1.complete(r9);	 Catch:{ XMPPException -> 0x0183 }
        r9 = org.jivesoftware.smackx.commands.AdHocCommand.Status.completed;	 Catch:{ XMPPException -> 0x0183 }
        r7.setStatus(r9);	 Catch:{ XMPPException -> 0x0183 }
        r9 = r14.executingCommands;	 Catch:{ XMPPException -> 0x0183 }
        r9.remove(r8);	 Catch:{ XMPPException -> 0x0183 }
        goto L_0x0175;
    L_0x01c7:
        r9 = org.jivesoftware.smackx.commands.AdHocCommand.Action.prev;	 Catch:{ XMPPException -> 0x0183 }
        r9 = r9.equals(r0);	 Catch:{ XMPPException -> 0x0183 }
        if (r9 == 0) goto L_0x01d6;
    L_0x01cf:
        r1.decrementStage();	 Catch:{ XMPPException -> 0x0183 }
        r1.prev();	 Catch:{ XMPPException -> 0x0183 }
        goto L_0x0175;
    L_0x01d6:
        r9 = org.jivesoftware.smackx.commands.AdHocCommand.Action.cancel;	 Catch:{ XMPPException -> 0x0183 }
        r9 = r9.equals(r0);	 Catch:{ XMPPException -> 0x0183 }
        if (r9 == 0) goto L_0x0175;
    L_0x01de:
        r1.cancel();	 Catch:{ XMPPException -> 0x0183 }
        r9 = org.jivesoftware.smackx.commands.AdHocCommand.Status.canceled;	 Catch:{ XMPPException -> 0x0183 }
        r7.setStatus(r9);	 Catch:{ XMPPException -> 0x0183 }
        r9 = r14.executingCommands;	 Catch:{ XMPPException -> 0x0183 }
        r9.remove(r8);	 Catch:{ XMPPException -> 0x0183 }
        goto L_0x0175;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jivesoftware.smackx.commands.AdHocCommandManager.processAdHocCommand(org.jivesoftware.smackx.packet.AdHocCommandData):void");
    }

    private void respondError(AdHocCommandData response, Condition condition) {
        respondError(response, new XMPPError(condition));
    }

    private void respondError(AdHocCommandData response, Condition condition, SpecificErrorCondition specificCondition) {
        XMPPError error = new XMPPError(condition);
        error.addExtension(new SpecificError(specificCondition));
        respondError(response, error);
    }

    private void respondError(AdHocCommandData response, XMPPError error) {
        response.setType(Type.ERROR);
        response.setError(error);
        this.connection.sendPacket(response);
    }

    private LocalCommand newInstanceOfCmd(String commandNode, String sessionID) throws XMPPException {
        AdHocCommandInfo commandInfo = (AdHocCommandInfo) this.commands.get(commandNode);
        try {
            LocalCommand command = commandInfo.getCommandInstance();
            command.setSessionID(sessionID);
            command.setName(commandInfo.getName());
            command.setNode(commandInfo.getNode());
            return command;
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new XMPPException(new XMPPError(Condition.interna_server_error));
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new XMPPException(new XMPPError(Condition.interna_server_error));
        }
    }

    /* access modifiers changed from: private */
    public Collection<AdHocCommandInfo> getRegisteredCommands() {
        return this.commands.values();
    }
}
