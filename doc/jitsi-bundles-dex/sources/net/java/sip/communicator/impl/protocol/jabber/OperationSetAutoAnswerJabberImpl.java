package net.java.sip.communicator.impl.protocol.jabber;

import java.util.Map;
import net.java.sip.communicator.service.protocol.AbstractOperationSetBasicAutoAnswer;
import net.java.sip.communicator.service.protocol.AccountID;
import net.java.sip.communicator.service.protocol.Call;
import org.jitsi.service.neomedia.MediaDirection;
import org.jitsi.service.neomedia.MediaType;

public class OperationSetAutoAnswerJabberImpl extends AbstractOperationSetBasicAutoAnswer {
    public OperationSetAutoAnswerJabberImpl(ProtocolProviderServiceJabberImpl protocolProvider) {
        super(protocolProvider);
        load();
    }

    /* access modifiers changed from: protected */
    public void save() {
        AccountID acc = this.protocolProvider.getAccountID();
        Map<String, String> accProps = acc.getAccountProperties();
        accProps.put("AUTO_ANSWER_UNCONDITIONAL", null);
        if (this.answerUnconditional) {
            accProps.put("AUTO_ANSWER_UNCONDITIONAL", Boolean.TRUE.toString());
        }
        accProps.put("AUTO_ANSWER_WITH_VIDEO", Boolean.toString(this.answerWithVideo));
        acc.setAccountProperties(accProps);
        JabberActivator.getProtocolProviderFactory().storeAccount(acc);
    }

    /* access modifiers changed from: protected */
    public boolean satisfyAutoAnswerConditions(Call call) {
        return call.isAutoAnswer();
    }

    public boolean autoAnswer(Call call, Map<MediaType, MediaDirection> directions) {
        boolean isVideoCall = false;
        MediaDirection direction = (MediaDirection) directions.get(MediaType.VIDEO);
        if (direction != null) {
            isVideoCall = direction == MediaDirection.SENDRECV;
        }
        return OperationSetAutoAnswerJabberImpl.super.autoAnswer(call, isVideoCall);
    }
}
