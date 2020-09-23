package net.java.sip.communicator.service.customcontactactions;

import net.java.sip.communicator.service.protocol.OperationFailedException;

public interface ContactActionMenuItem<T> {
    void actionPerformed(T t) throws OperationFailedException;

    byte[] getIcon();

    char getMnemonics();

    String getText(T t);

    boolean isCheckBox();

    boolean isEnabled(T t);

    boolean isSelected(T t);

    boolean isVisible(T t);
}
