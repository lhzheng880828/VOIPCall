package net.java.sip.communicator.impl.protocol.sip.security;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import org.jitsi.javax.sip.header.AuthorizationHeader;

class CredentialsCache {
    private Hashtable<String, AuthorizationHeader> authenticatedCalls = new Hashtable();
    private Hashtable<String, CredentialsCacheEntry> authenticatedRealms = new Hashtable();

    CredentialsCache() {
    }

    /* access modifiers changed from: 0000 */
    public void cacheEntry(String realm, CredentialsCacheEntry cacheEntry) {
        this.authenticatedRealms.put(realm, cacheEntry);
    }

    /* access modifiers changed from: 0000 */
    public CredentialsCacheEntry get(String realm) {
        return (CredentialsCacheEntry) this.authenticatedRealms.get(realm);
    }

    /* access modifiers changed from: 0000 */
    public List<String> getRealms(String branchID) {
        List<String> realms = new LinkedList();
        for (Entry<String, CredentialsCacheEntry> entry : this.authenticatedRealms.entrySet()) {
            if (((CredentialsCacheEntry) entry.getValue()).containsBranchID(branchID)) {
                realms.add(entry.getKey());
            }
        }
        return realms;
    }

    /* access modifiers changed from: 0000 */
    public CredentialsCacheEntry remove(String realm) {
        return (CredentialsCacheEntry) this.authenticatedRealms.remove(realm);
    }

    /* access modifiers changed from: 0000 */
    public void clear() {
        this.authenticatedRealms.clear();
    }

    /* access modifiers changed from: 0000 */
    public void cacheAuthorizationHeader(String callid, AuthorizationHeader authorization) {
        this.authenticatedCalls.put(callid, authorization);
    }

    /* access modifiers changed from: 0000 */
    public AuthorizationHeader getCachedAuthorizationHeader(String callid) {
        return (AuthorizationHeader) this.authenticatedCalls.get(callid);
    }
}
