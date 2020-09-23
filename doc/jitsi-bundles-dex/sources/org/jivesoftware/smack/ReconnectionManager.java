package org.jivesoftware.smack;

import java.util.Random;
import org.jivesoftware.smack.packet.StreamError;

public class ReconnectionManager implements ConnectionListener {
    /* access modifiers changed from: private */
    public Connection connection;
    boolean done;
    /* access modifiers changed from: private */
    public int randomBase;
    private Thread reconnectionThread;

    static {
        Connection.addConnectionCreationListener(new ConnectionCreationListener() {
            public void connectionCreated(Connection connection) {
                connection.addConnectionListener(new ReconnectionManager(connection));
            }
        });
    }

    private ReconnectionManager(Connection connection) {
        this.randomBase = new Random().nextInt(11) + 5;
        this.done = false;
        this.connection = connection;
    }

    /* access modifiers changed from: private */
    public boolean isReconnectionAllowed() {
        return (this.done || this.connection.isConnected() || !this.connection.isReconnectionAllowed()) ? false : true;
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void reconnect() {
        if (isReconnectionAllowed() && (this.reconnectionThread == null || !this.reconnectionThread.isAlive())) {
            this.reconnectionThread = new Thread() {
                private int attempts = 0;

                private int timeDelay() {
                    this.attempts++;
                    if (this.attempts > 13) {
                        return (ReconnectionManager.this.randomBase * 6) * 5;
                    }
                    if (this.attempts > 7) {
                        return ReconnectionManager.this.randomBase * 6;
                    }
                    return ReconnectionManager.this.randomBase;
                }

                public void run() {
                    while (ReconnectionManager.this.isReconnectionAllowed()) {
                        int remainingSeconds = timeDelay();
                        while (ReconnectionManager.this.isReconnectionAllowed() && remainingSeconds > 0) {
                            try {
                                Thread.sleep(1000);
                                remainingSeconds--;
                                ReconnectionManager.this.notifyAttemptToReconnectIn(remainingSeconds);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                                ReconnectionManager.this.notifyReconnectionFailed(e1);
                            }
                        }
                        try {
                            if (ReconnectionManager.this.isReconnectionAllowed()) {
                                ReconnectionManager.this.connection.connect();
                            }
                        } catch (XMPPException e) {
                            ReconnectionManager.this.notifyReconnectionFailed(e);
                        }
                    }
                }
            };
            this.reconnectionThread.setName("Smack Reconnection Manager");
            this.reconnectionThread.setDaemon(true);
            this.reconnectionThread.start();
        }
    }

    /* access modifiers changed from: protected */
    public void notifyReconnectionFailed(Exception exception) {
        if (isReconnectionAllowed()) {
            for (ConnectionListener listener : this.connection.connectionListeners) {
                listener.reconnectionFailed(exception);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifyAttemptToReconnectIn(int seconds) {
        if (isReconnectionAllowed()) {
            for (ConnectionListener listener : this.connection.connectionListeners) {
                listener.reconnectingIn(seconds);
            }
        }
    }

    public void connectionClosed() {
        this.done = true;
    }

    public void connectionClosedOnError(Exception e) {
        this.done = false;
        if (e instanceof XMPPException) {
            StreamError error = ((XMPPException) e).getStreamError();
            if (error != null) {
                if ("conflict".equals(error.getCode())) {
                    return;
                }
            }
        }
        if (isReconnectionAllowed()) {
            reconnect();
        }
    }

    public void reconnectingIn(int seconds) {
    }

    public void reconnectionFailed(Exception e) {
    }

    public void reconnectionSuccessful() {
    }
}
