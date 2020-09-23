package net.java.sip.communicator.impl.protocol.jabber.extensions.caps;

public interface UserCapsNodeListener {
    void userCapsNodeAdded(String str, String str2, boolean z);

    void userCapsNodeRemoved(String str, String str2, boolean z);
}
