package net.java.sip.communicator.service.customcontactactions;

import net.java.sip.communicator.service.protocol.OperationFailedException;

public interface ContactAction<T> {
    void actionPerformed(T t, int i, int i2) throws OperationFailedException;

    byte[] getIcon();

    byte[] getPressedIcon();

    byte[] getRolloverIcon();

    String getToolTipText();

    boolean isVisible(T t);
}
