package org.jitsi.gov.nist.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadAuditor {
    private long pingIntervalInMillisecs = 0;
    private Map<Thread, ThreadHandle> threadHandles = new ConcurrentHashMap();

    public class ThreadHandle {
        private boolean isThreadActive = false;
        private Thread thread = Thread.currentThread();
        private ThreadAuditor threadAuditor;

        public ThreadHandle(ThreadAuditor aThreadAuditor) {
            this.threadAuditor = aThreadAuditor;
        }

        public boolean isThreadActive() {
            return this.isThreadActive;
        }

        /* access modifiers changed from: protected */
        public void setThreadActive(boolean value) {
            this.isThreadActive = value;
        }

        public Thread getThread() {
            return this.thread;
        }

        public void ping() {
            this.threadAuditor.ping(this);
        }

        public long getPingIntervalInMillisecs() {
            return this.threadAuditor.getPingIntervalInMillisecs();
        }

        public String toString() {
            return "Thread Name: " + this.thread.getName() + ", Alive: " + this.thread.isAlive();
        }
    }

    public long getPingIntervalInMillisecs() {
        return this.pingIntervalInMillisecs;
    }

    public void setPingIntervalInMillisecs(long value) {
        this.pingIntervalInMillisecs = value;
    }

    public boolean isEnabled() {
        return this.pingIntervalInMillisecs > 0;
    }

    public ThreadHandle addCurrentThread() {
        ThreadHandle threadHandle = new ThreadHandle(this);
        if (isEnabled()) {
            this.threadHandles.put(Thread.currentThread(), threadHandle);
        }
        return threadHandle;
    }

    public void removeThread(Thread thread) {
        this.threadHandles.remove(thread);
    }

    public void ping(ThreadHandle threadHandle) {
        threadHandle.setThreadActive(true);
    }

    public void reset() {
        this.threadHandles.clear();
    }

    public String auditThreads() {
        String auditReport = null;
        for (ThreadHandle threadHandle : this.threadHandles.values()) {
            if (!threadHandle.isThreadActive()) {
                Thread thread = threadHandle.getThread();
                if (auditReport == null) {
                    auditReport = "Thread Auditor Report:\n";
                }
                auditReport = auditReport + "   Thread [" + thread.getName() + "] has failed to respond to an audit request.\n";
            }
            threadHandle.setThreadActive(false);
        }
        return auditReport;
    }

    public synchronized String toString() {
        String toString;
        toString = "Thread Auditor - List of monitored threads:\n";
        for (ThreadHandle threadHandle : this.threadHandles.values()) {
            toString = toString + "   " + threadHandle.toString() + Separators.RETURN;
        }
        return toString;
    }
}
