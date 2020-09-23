package org.jitsi.gov.nist.javax.sip.clientauthutils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentHashMap;
import org.jitsi.gov.nist.javax.sip.stack.SIPStackTimerTask;
import org.jitsi.gov.nist.javax.sip.stack.timers.SipTimer;
import org.jitsi.javax.sip.header.AuthorizationHeader;

class CredentialsCache {
    /* access modifiers changed from: private */
    public ConcurrentHashMap<String, List<AuthorizationHeader>> authorizationHeaders = new ConcurrentHashMap();
    private SipTimer timer;

    class TimeoutTask extends SIPStackTimerTask {
        String callId;
        String userName;

        public TimeoutTask(String userName, String callId) {
            this.callId = callId;
            this.userName = userName;
        }

        public void runTask() {
            CredentialsCache.this.authorizationHeaders.remove(this.callId);
        }
    }

    CredentialsCache(SipTimer timer) {
        this.timer = timer;
    }

    /* access modifiers changed from: 0000 */
    public void cacheAuthorizationHeader(String callId, AuthorizationHeader authorization, int cacheTime) {
        String user = authorization.getUsername();
        if (callId == null) {
            throw new NullPointerException("Call ID is null!");
        } else if (authorization == null) {
            throw new NullPointerException("Null authorization domain");
        } else {
            List<AuthorizationHeader> authHeaders = (List) this.authorizationHeaders.get(callId);
            if (authHeaders == null) {
                authHeaders = new LinkedList();
                this.authorizationHeaders.put(callId, authHeaders);
            } else {
                String realm = authorization.getRealm();
                ListIterator<AuthorizationHeader> li = authHeaders.listIterator();
                while (li.hasNext()) {
                    if (realm.equals(((AuthorizationHeader) li.next()).getRealm())) {
                        li.remove();
                    }
                }
            }
            authHeaders.add(authorization);
            TimeoutTask timeoutTask = new TimeoutTask(callId, user);
            if (cacheTime != -1) {
                this.timer.schedule(timeoutTask, (long) (cacheTime * 1000));
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public Collection<AuthorizationHeader> getCachedAuthorizationHeaders(String callid) {
        if (callid != null) {
            return (Collection) this.authorizationHeaders.get(callid);
        }
        throw new NullPointerException("Null arg!");
    }

    public void removeAuthenticationHeader(String callId) {
        this.authorizationHeaders.remove(callId);
    }
}
