package net.java.sip.communicator.impl.protocol.sip;

import net.java.sip.communicator.service.protocol.AbstractOperationSetAvatar;
import net.java.sip.communicator.service.protocol.OperationSetServerStoredAccountInfo;

public class OperationSetAvatarSipImpl extends AbstractOperationSetAvatar<ProtocolProviderServiceSipImpl> {
    public OperationSetAvatarSipImpl(ProtocolProviderServiceSipImpl parentProvider, OperationSetServerStoredAccountInfo accountInfoOpSet) {
        super(parentProvider, accountInfoOpSet, 96, 96, 0);
    }
}
