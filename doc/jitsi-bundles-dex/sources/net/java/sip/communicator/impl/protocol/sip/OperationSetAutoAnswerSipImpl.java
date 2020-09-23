package net.java.sip.communicator.impl.protocol.sip;

import java.util.Iterator;
import java.util.Map;
import javax.sdp.MediaDescription;
import net.java.sip.communicator.impl.protocol.sip.sdp.SdpUtils;
import net.java.sip.communicator.service.protocol.AbstractOperationSetBasicAutoAnswer;
import net.java.sip.communicator.service.protocol.AccountID;
import net.java.sip.communicator.service.protocol.Call;
import net.java.sip.communicator.service.protocol.CallPeer;
import net.java.sip.communicator.service.protocol.OperationSetAdvancedAutoAnswer;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.javax.sip.ServerTransaction;
import org.jitsi.javax.sip.Transaction;
import org.jitsi.javax.sip.address.AddressFactory;
import org.jitsi.javax.sip.header.ContactHeader;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;
import org.jitsi.service.neomedia.MediaDirection;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.util.StringUtils;

public class OperationSetAutoAnswerSipImpl extends AbstractOperationSetBasicAutoAnswer implements OperationSetAdvancedAutoAnswer {
    private static final Logger logger = Logger.getLogger(OperationSetBasicTelephonySipImpl.class);
    private boolean answerConditional = false;
    private String callFwdTo = null;
    private String headerName = null;
    private String headerValue = null;

    public OperationSetAutoAnswerSipImpl(ProtocolProviderServiceSipImpl protocolProvider) {
        super(protocolProvider);
        load();
    }

    /* access modifiers changed from: protected */
    public void load() {
        OperationSetAutoAnswerSipImpl.super.load();
        AccountID acc = this.protocolProvider.getAccountID();
        this.headerName = acc.getAccountPropertyString("AUTO_ANSWER_CONDITIONAL_NAME");
        this.headerValue = acc.getAccountPropertyString("AUTO_ANSWER_CONDITIONAL_VALUE");
        if (!StringUtils.isNullOrEmpty(this.headerName)) {
            this.answerConditional = true;
        }
        this.callFwdTo = acc.getAccountPropertyString("AUTO_ANSWER_FWD_NUM");
    }

    /* access modifiers changed from: protected */
    public void save() {
        AccountID acc = this.protocolProvider.getAccountID();
        Map<String, String> accProps = acc.getAccountProperties();
        accProps.put("AUTO_ANSWER_UNCONDITIONAL", null);
        accProps.put("AUTO_ANSWER_CONDITIONAL_NAME", null);
        accProps.put("AUTO_ANSWER_CONDITIONAL_VALUE", null);
        accProps.put("AUTO_ANSWER_FWD_NUM", null);
        if (this.answerUnconditional) {
            accProps.put("AUTO_ANSWER_UNCONDITIONAL", Boolean.TRUE.toString());
        } else if (this.answerConditional) {
            accProps.put("AUTO_ANSWER_CONDITIONAL_NAME", this.headerName);
            if (!StringUtils.isNullOrEmpty(this.headerValue)) {
                accProps.put("AUTO_ANSWER_CONDITIONAL_VALUE", this.headerValue);
            }
        } else if (!StringUtils.isNullOrEmpty(this.callFwdTo)) {
            accProps.put("AUTO_ANSWER_FWD_NUM", this.callFwdTo);
        }
        accProps.put("AUTO_ANSWER_WITH_VIDEO", Boolean.toString(this.answerWithVideo));
        acc.setAccountProperties(accProps);
        SipActivator.getProtocolProviderFactory().storeAccount(acc);
    }

    public void setAutoAnswerCondition(String headerName, String value) {
        clearLocal();
        this.answerConditional = true;
        this.headerName = headerName;
        this.headerValue = value;
        save();
    }

    public boolean isAutoAnswerConditionSet() {
        return this.answerConditional;
    }

    public void setCallForward(String numberTo) {
        clearLocal();
        this.callFwdTo = numberTo;
        save();
    }

    public String getCallForward() {
        return this.callFwdTo;
    }

    /* access modifiers changed from: protected */
    public void clearLocal() {
        OperationSetAutoAnswerSipImpl.super.clearLocal();
        this.answerConditional = false;
        this.headerName = null;
        this.headerValue = null;
        this.callFwdTo = null;
    }

    public String getAutoAnswerHeaderName() {
        return this.headerName;
    }

    public String getAutoAnswerHeaderValue() {
        return this.headerValue;
    }

    public boolean forwardCall(Request invite, ServerTransaction serverTransaction) {
        if (StringUtils.isNullOrEmpty(this.callFwdTo)) {
            return false;
        }
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("will send moved temporally response: ");
            }
            Response response = ((ProtocolProviderServiceSipImpl) this.protocolProvider).getMessageFactory().createResponse(302, invite);
            ContactHeader contactHeader = (ContactHeader) response.getHeader("Contact");
            AddressFactory addressFactory = ((ProtocolProviderServiceSipImpl) this.protocolProvider).getAddressFactory();
            String destination = getCallForward();
            if (!destination.startsWith("sip")) {
                destination = "sip:" + destination;
            }
            contactHeader.setAddress(addressFactory.createAddress(addressFactory.createURI(destination)));
            serverTransaction.sendResponse(response);
            if (logger.isDebugEnabled()) {
                logger.debug("sent a moved temporally response: " + response);
            }
            return true;
        } catch (Throwable ex) {
            logger.error("Error while trying to send a request", ex);
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean satisfyAutoAnswerConditions(Call call) {
        Iterator<? extends CallPeer> peers = call.getCallPeers();
        if (call.isAutoAnswer()) {
            return true;
        }
        if (this.answerConditional) {
            while (peers.hasNext()) {
                Transaction transaction = ((CallPeerSipImpl) ((CallPeer) peers.next())).getLatestInviteTransaction();
                if (transaction != null) {
                    SIPHeader callAnswerHeader = (SIPHeader) transaction.getRequest().getHeader(this.headerName);
                    if (!(callAnswerHeader == null || StringUtils.isNullOrEmpty(this.headerValue))) {
                        String value = callAnswerHeader.getHeaderValue();
                        if (value != null && this.headerValue.equals(value)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean autoAnswer(Call call) {
        if (!this.answerUnconditional && !satisfyAutoAnswerConditions(call)) {
            return false;
        }
        answerCall(call, doesRequestContainsActiveVideoMediaType(call));
        return true;
    }

    private boolean doesRequestContainsActiveVideoMediaType(Call call) {
        Iterator<? extends CallPeer> peers = call.getCallPeers();
        while (peers.hasNext()) {
            Transaction transaction = ((CallPeerSipImpl) peers.next()).getLatestInviteTransaction();
            if (transaction != null) {
                Request inviteReq = transaction.getRequest();
                if (!(inviteReq == null || inviteReq.getRawContent() == null)) {
                    for (MediaDescription mediaDescription : SdpUtils.extractMediaDescriptions(SdpUtils.parseSdpString(SdpUtils.getContentAsString(inviteReq)))) {
                        if (SdpUtils.getMediaType(mediaDescription) == MediaType.VIDEO && SdpUtils.getDirection(mediaDescription) == MediaDirection.SENDRECV) {
                            return true;
                        }
                    }
                    continue;
                }
            }
        }
        return false;
    }
}
