package net.java.sip.communicator.impl.protocol.sip.security;

import java.util.Vector;
import net.java.sip.communicator.service.protocol.UserCredentials;

class CredentialsCacheEntry {
    private Vector<String> transactionHistory = new Vector();
    public UserCredentials userCredentials = null;

    CredentialsCacheEntry() {
    }

    /* access modifiers changed from: 0000 */
    public void pushBranchID(String requestBranchID) {
        this.transactionHistory.add(requestBranchID);
    }

    /* access modifiers changed from: 0000 */
    public boolean popBranchID(String responseBranchID) {
        return this.transactionHistory.remove(responseBranchID);
    }

    /* access modifiers changed from: 0000 */
    public boolean containsBranchID(String branchID) {
        return this.transactionHistory.contains(branchID);
    }
}
