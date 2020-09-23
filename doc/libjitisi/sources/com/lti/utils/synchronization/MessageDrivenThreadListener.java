package com.lti.utils.synchronization;

public interface MessageDrivenThreadListener {
    void onMessage(MessageDrivenThread messageDrivenThread, Object obj);
}
