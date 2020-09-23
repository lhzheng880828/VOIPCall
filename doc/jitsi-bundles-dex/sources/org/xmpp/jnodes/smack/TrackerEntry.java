package org.xmpp.jnodes.smack;

public class TrackerEntry {
    private String jid;
    private Policy policy;
    private String protocol = "udp";
    private Type type;
    private boolean verified = false;

    public enum Policy {
        _public,
        _roster;

        public String toString() {
            return name().substring(1);
        }
    }

    public enum Type {
        relay,
        tracker
    }

    public TrackerEntry(Type type, Policy policy, String jid, String protocol) {
        this.type = type;
        this.policy = policy;
        this.jid = jid;
        this.protocol = protocol;
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getJid() {
        return this.jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public boolean isVerified() {
        return this.verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public Policy getPolicy() {
        return this.policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
