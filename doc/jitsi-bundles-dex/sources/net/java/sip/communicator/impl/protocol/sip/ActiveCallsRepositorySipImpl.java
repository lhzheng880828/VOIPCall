package net.java.sip.communicator.impl.protocol.sip;

import java.util.Iterator;
import javax.sdp.SdpConstants;
import net.java.sip.communicator.service.protocol.ActiveCallsRepository;
import net.java.sip.communicator.service.protocol.Call;
import net.java.sip.communicator.service.protocol.CallPeer;
import net.java.sip.communicator.service.protocol.event.CallChangeEvent;
import net.java.sip.communicator.util.Logger;
import org.jitsi.javax.sip.Dialog;
import org.jitsi.javax.sip.Transaction;
import org.jitsi.javax.sip.header.CallIdHeader;
import org.jitsi.javax.sip.header.Header;

public class ActiveCallsRepositorySipImpl extends ActiveCallsRepository<CallSipImpl, OperationSetBasicTelephonySipImpl> {
    private static final Logger logger = Logger.getLogger(ActiveCallsRepositorySipImpl.class);

    public ActiveCallsRepositorySipImpl(OperationSetBasicTelephonySipImpl opSet) {
        super(opSet);
    }

    public CallSipImpl findCall(Dialog dialog) {
        Iterator<CallSipImpl> activeCalls = getActiveCalls();
        if (dialog == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Cannot find a peer with a null dialog. Returning null");
            }
            return null;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Looking for peer with dialog: " + dialog + " among " + getActiveCallCount() + " calls");
        }
        while (activeCalls.hasNext()) {
            CallSipImpl call = (CallSipImpl) activeCalls.next();
            if (call.contains(dialog)) {
                return call;
            }
        }
        return null;
    }

    public CallPeerSipImpl findCallPeer(Dialog dialog) {
        if (dialog == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Cannot find a peer with a null dialog. Returning null");
            }
            return null;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Looking for peer with dialog: " + dialog + " among " + getActiveCallCount() + " calls");
        }
        Iterator<CallSipImpl> activeCalls = getActiveCalls();
        while (activeCalls.hasNext()) {
            CallPeerSipImpl callPeer = ((CallSipImpl) activeCalls.next()).findCallPeer(dialog);
            if (callPeer != null) {
                if (!logger.isTraceEnabled()) {
                    return callPeer;
                }
                logger.trace("Returning peer " + callPeer);
                return callPeer;
            }
        }
        return null;
    }

    public CallPeerSipImpl findCallPeer(String callID, String localTag, String remoteTag) {
        if (logger.isTraceEnabled()) {
            logger.trace("Looking for call peer with callID " + callID + ", localTag " + localTag + ", and remoteTag " + remoteTag + " among " + getActiveCallCount() + " calls.");
        }
        Iterator<CallSipImpl> activeCalls = getActiveCalls();
        while (activeCalls.hasNext()) {
            Iterator<? extends CallPeer> callPeerIter = ((CallSipImpl) activeCalls.next()).getCallPeers();
            while (callPeerIter.hasNext()) {
                CallPeerSipImpl callPeer = (CallPeerSipImpl) callPeerIter.next();
                Dialog dialog = callPeer.getDialog();
                if (dialog != null && callID.equals(dialog.getCallId().getCallId())) {
                    String dialogLocalTag = dialog.getLocalTag();
                    if (localTag == null || SdpConstants.RESERVED.equals(localTag)) {
                        if (!(dialogLocalTag == null || SdpConstants.RESERVED.equals(dialogLocalTag))) {
                        }
                    } else if (!localTag.equals(dialogLocalTag)) {
                        continue;
                    }
                    String dialogRemoteTag = dialog.getRemoteTag();
                    if (remoteTag == null || SdpConstants.RESERVED.equals(remoteTag)) {
                        if (dialogRemoteTag == null || SdpConstants.RESERVED.equals(dialogRemoteTag)) {
                            return callPeer;
                        }
                    } else if (remoteTag.equals(dialogRemoteTag)) {
                        return callPeer;
                    }
                }
            }
        }
        return null;
    }

    public CallPeerSipImpl findCallPeer(String branchID, String callID) {
        Iterator<CallSipImpl> activeCallsIter = getActiveCalls();
        while (activeCallsIter.hasNext()) {
            Iterator<CallPeerSipImpl> callPeersIter = ((CallSipImpl) activeCallsIter.next()).getCallPeers();
            while (callPeersIter.hasNext()) {
                CallPeerSipImpl cp = (CallPeerSipImpl) callPeersIter.next();
                Dialog cpDialog = cp.getDialog();
                Transaction cpTran = cp.getLatestInviteTransaction();
                if (cpDialog != null && cpDialog.getCallId() != null && cpTran != null && cp.getLatestInviteTransaction() != null && cpDialog.getCallId().getCallId().equals(callID) && branchID.equals(cpTran.getBranchId())) {
                    return cp;
                }
            }
        }
        return null;
    }

    public CallPeerSipImpl findCallPeer(String branchID, Header cidHeader) {
        if (cidHeader == null || !(cidHeader instanceof CallIdHeader)) {
            return null;
        }
        return findCallPeer(branchID, ((CallIdHeader) cidHeader).getCallId());
    }

    public CallSipImpl findCall(String callID, String localTag, String remoteTag) {
        CallPeerSipImpl peer = findCallPeer(callID, localTag, remoteTag);
        return peer == null ? null : (CallSipImpl) peer.getCall();
    }

    /* access modifiers changed from: protected */
    public void fireCallEvent(int eventID, Call sourceCall, CallChangeEvent cause) {
        ((OperationSetBasicTelephonySipImpl) this.parentOperationSet).fireCallEvent(eventID, sourceCall);
    }
}
