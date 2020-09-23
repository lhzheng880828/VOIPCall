package net.java.sip.communicator.impl.protocol.jabber;

import net.java.sip.communicator.service.protocol.AbstractOperationSetAvatar;
import net.java.sip.communicator.service.protocol.OperationSetServerStoredAccountInfo;

public class OperationSetAvatarJabberImpl extends AbstractOperationSetAvatar<ProtocolProviderServiceJabberImpl> {
    public OperationSetAvatarJabberImpl(ProtocolProviderServiceJabberImpl parentProvider, OperationSetServerStoredAccountInfo accountInfoOpSet) {
        super(parentProvider, accountInfoOpSet, 96, 96, 0);
    }
}
