package org.jivesoftware.smackx.pubsub.packet;

public enum PubSubNamespace {
    BASIC(null),
    ERROR("errors"),
    EVENT("event"),
    OWNER("owner");
    
    private String fragment;

    private PubSubNamespace(String fragment) {
        this.fragment = fragment;
    }

    public String getXmlns() {
        String ns = "http://jabber.org/protocol/pubsub";
        if (this.fragment != null) {
            return ns + '#' + this.fragment;
        }
        return ns;
    }

    public String getFragment() {
        return this.fragment;
    }

    public static PubSubNamespace valueOfFromXmlns(String ns) {
        if (ns.lastIndexOf(35) != -1) {
            return valueOf(ns.substring(ns.lastIndexOf(35) + 1).toUpperCase());
        }
        return BASIC;
    }
}
