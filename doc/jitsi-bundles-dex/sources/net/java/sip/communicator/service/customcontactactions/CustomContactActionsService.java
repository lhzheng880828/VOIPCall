package net.java.sip.communicator.service.customcontactactions;

import java.util.Iterator;

public interface CustomContactActionsService<T> {
    Class<T> getContactSourceClass();

    Iterator<ContactAction<T>> getCustomContactActions();

    Iterator<ContactActionMenuItem<T>> getCustomContactActionsMenuItems();
}
